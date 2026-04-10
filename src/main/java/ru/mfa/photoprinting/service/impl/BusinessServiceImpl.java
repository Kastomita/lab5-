package ru.mfa.photoprinting.service.impl;

import ru.mfa.photoprinting.dto.*;
import ru.mfa.photoprinting.enums.OrderStatus;
import ru.mfa.photoprinting.enums.PaymentStatus;
import ru.mfa.photoprinting.model.*;
import ru.mfa.photoprinting.repository.*;
import ru.mfa.photoprinting.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private FormatRepository formatRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Override
    @Transactional
    public OrderResponseDTO createFullOrder(OrderRequestDTO request) {
        // 1. Получаем клиента
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + request.getCustomerId()));

        // 2. Создаем заказ
        Order order = new Order();
        order.setCustomerId(customer.getId());
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order = orderRepository.save(order);

        // 3. Добавляем фото к заказу
        double totalPrice = 0.0;
        for (PhotoRequestDTO photoReq : request.getPhotos()) {
            Format format = formatRepository.findById(photoReq.getFormatId())
                    .orElseThrow(() -> new RuntimeException("Format not found with id: " + photoReq.getFormatId()));

            Photo photo = new Photo();
            photo.setFileName(photoReq.getFileName());
            photo.setFileUrl(photoReq.getFileUrl());
            photo.setQuantity(photoReq.getQuantity());
            photo.setDescription(photoReq.getDescription());
            photo.setFormatId(format.getId());
            photo.setOrderId(order.getId());

            photoRepository.save(photo);
            totalPrice += format.getPrice() * photoReq.getQuantity();
        }

        // 4. Обновляем общую сумму заказа
        order.setTotalPrice(totalPrice);
        order = orderRepository.save(order);

        return convertToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO processPayment(Long orderId, PaymentRequestDTO payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Order already paid");
        }

        if (payment.getAmount() >= order.getTotalPrice()) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setStatus(OrderStatus.PROCESSING);
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
            throw new RuntimeException("Insufficient payment amount. Required: " + order.getTotalPrice() + ", Provided: " + payment.getAmount());
        }

        order = orderRepository.save(order);
        return convertToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public OrderResponseDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        OrderStatus newStatus = OrderStatus.valueOf(status);
        order.setStatus(newStatus);

        // Если заказ готов, автоматически создаем доставку
        if (newStatus == OrderStatus.READY) {
            Customer customer = customerRepository.findById(order.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            Delivery delivery = new Delivery();
            delivery.setOrderId(order.getId());
            delivery.setAddress(customer.getAddress());
            delivery.setStatus("PENDING");
            delivery.setTrackingNumber("TRK-" + System.currentTimeMillis());
            deliveryRepository.save(delivery);
        }

        order = orderRepository.save(order);
        return convertToOrderResponseDTO(order);
    }

    @Override
    @Transactional
    public DeliveryResponseDTO createDeliveryForOrder(Long orderId, DeliveryRequestDTO request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() != OrderStatus.READY) {
            throw new RuntimeException("Order is not ready for delivery. Current status: " + order.getStatus());
        }

        Delivery delivery = new Delivery();
        delivery.setOrderId(order.getId());
        delivery.setAddress(request.getAddress());
        delivery.setTrackingNumber(request.getTrackingNumber());
        delivery.setStatus("PENDING");
        delivery.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());

        delivery = deliveryRepository.save(delivery);

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        return convertToDeliveryResponseDTO(delivery);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatisticsDTO getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        List<Order> orders = orderRepository.findAll();

        // Фильтруем по дате
        List<Order> filteredOrders = orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .filter(o -> !o.getCreatedAt().toLocalDate().isBefore(startDate))
                .filter(o -> !o.getCreatedAt().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());

        OrderStatisticsDTO stats = new OrderStatisticsDTO();
        stats.setTotalOrders((long) filteredOrders.size());
        stats.setTotalRevenue(filteredOrders.stream().mapToDouble(Order::getTotalPrice).sum());
        stats.setPaidOrders(filteredOrders.stream().filter(o -> o.getPaymentStatus() == PaymentStatus.PAID).count());
        stats.setCompletedOrders(filteredOrders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count());
        stats.setCancelledOrders(filteredOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count());

        return stats;
    }

    @Override
    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel shipped or delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        order = orderRepository.save(order);
        return convertToOrderResponseDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream().map(this::convertToOrderResponseDTO).collect(Collectors.toList());
    }

    private OrderResponseDTO convertToOrderResponseDTO(Order order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus().toString());
        dto.setPaymentStatus(order.getPaymentStatus().toString());
        dto.setCreatedAt(order.getCreatedAt());

        // Получаем имя клиента
        customerRepository.findById(order.getCustomerId()).ifPresent(c ->
                dto.setCustomerName(c.getName())
        );

        return dto;
    }

    private DeliveryResponseDTO convertToDeliveryResponseDTO(Delivery delivery) {
        DeliveryResponseDTO dto = new DeliveryResponseDTO();
        dto.setId(delivery.getId());
        dto.setAddress(delivery.getAddress());
        dto.setStatus(delivery.getStatus());
        dto.setTrackingNumber(delivery.getTrackingNumber());
        dto.setEstimatedDeliveryDate(delivery.getEstimatedDeliveryDate());
        return dto;
    }
}