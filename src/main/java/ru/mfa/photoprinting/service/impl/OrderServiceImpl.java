package ru.mfa.photoprinting.service.impl;

import ru.mfa.photoprinting.model.Order;
import ru.mfa.photoprinting.repository.OrderRepository;
import ru.mfa.photoprinting.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrder(Long id, Order orderDetails) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return null;
        }
        order.setTotalPrice(orderDetails.getTotalPrice());
        order.setStatus(orderDetails.getStatus());
        order.setPaymentStatus(orderDetails.getPaymentStatus());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}