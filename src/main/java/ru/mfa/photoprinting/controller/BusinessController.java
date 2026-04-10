package ru.mfa.photoprinting.controller;

import ru.mfa.photoprinting.dto.*;
import ru.mfa.photoprinting.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    // Операция 1: Создание полного заказа (доступно USER и ADMIN)
    @PostMapping("/orders/full")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> createFullOrder(@RequestBody OrderRequestDTO request) {
        return ResponseEntity.ok(businessService.createFullOrder(request));
    }

    // Операция 2: Обработка платежа (доступно USER и ADMIN)
    @PostMapping("/orders/{orderId}/payment")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> processPayment(
            @PathVariable Long orderId,
            @RequestBody PaymentRequestDTO payment) {
        return ResponseEntity.ok(businessService.processPayment(orderId, payment));
    }

    // Операция 3: Обновление статуса заказа (только ADMIN)
    @PutMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        return ResponseEntity.ok(businessService.updateOrderStatus(orderId, status));
    }

    // Операция 4: Создание доставки (только ADMIN)
    @PostMapping("/orders/{orderId}/delivery")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeliveryResponseDTO> createDelivery(
            @PathVariable Long orderId,
            @RequestBody DeliveryRequestDTO delivery) {
        return ResponseEntity.ok(businessService.createDeliveryForOrder(orderId, delivery));
    }

    // Операция 5: Статистика заказов (только ADMIN)
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderStatisticsDTO> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(businessService.getOrderStatistics(startDate, endDate));
    }

    // Операция 6: Отмена заказа (доступно USER и ADMIN)
    @PostMapping("/orders/{orderId}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(businessService.cancelOrder(orderId));
    }

    // Доп. операция: Заказы клиента (доступно USER и ADMIN)
    @GetMapping("/customers/{customerId}/orders")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<OrderResponseDTO>> getCustomerOrders(@PathVariable Long customerId) {
        return ResponseEntity.ok(businessService.getCustomerOrders(customerId));
    }
}