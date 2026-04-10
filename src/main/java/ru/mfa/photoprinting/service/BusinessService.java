package ru.mfa.photoprinting.service;

import ru.mfa.photoprinting.dto.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

public interface BusinessService {

    // Операция 1: Создание полного заказа (затрагивает Order, Photo, Format)
    @Transactional
    OrderResponseDTO createFullOrder(OrderRequestDTO request);

    // Операция 2: Обработка платежа
    @Transactional
    OrderResponseDTO processPayment(Long orderId, PaymentRequestDTO payment);

    // Операция 3: Обновление статуса заказа
    @Transactional
    OrderResponseDTO updateOrderStatus(Long orderId, String status);

    // Операция 4: Создание доставки для заказа
    @Transactional
    DeliveryResponseDTO createDeliveryForOrder(Long orderId, DeliveryRequestDTO request);

    // Операция 5: Получение статистики заказов
    @Transactional(readOnly = true)
    OrderStatisticsDTO getOrderStatistics(LocalDate startDate, LocalDate endDate);

    // Операция 6: Отмена заказа
    @Transactional
    OrderResponseDTO cancelOrder(Long orderId);

    // Доп. операция: Получение всех заказов клиента
    @Transactional(readOnly = true)
    List<OrderResponseDTO> getCustomerOrders(Long customerId);
}