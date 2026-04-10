package ru.mfa.photoprinting.controller;

import ru.mfa.photoprinting.model.Delivery;
import ru.mfa.photoprinting.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Delivery>> getAllDeliveries() {
        return ResponseEntity.ok(deliveryService.getAllDeliveries());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable Long id) {
        Delivery delivery = deliveryService.getDeliveryById(id);
        if (delivery != null) {
            return ResponseEntity.ok(delivery);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Delivery> createDelivery(@RequestBody Delivery delivery) {
        Delivery createdDelivery = deliveryService.createDelivery(delivery);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDelivery);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Delivery> updateDelivery(@PathVariable Long id, @RequestBody Delivery delivery) {
        Delivery updatedDelivery = deliveryService.updateDelivery(id, delivery);
        if (updatedDelivery != null) {
            return ResponseEntity.ok(updatedDelivery);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDelivery(@PathVariable Long id) {
        deliveryService.deleteDelivery(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Delivery> getDeliveryByOrderId(@PathVariable Long orderId) {
        Delivery delivery = deliveryService.getDeliveryByOrderId(orderId);
        if (delivery != null) {
            return ResponseEntity.ok(delivery);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}