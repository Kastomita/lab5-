package ru.mfa.photoprinting.service;

import ru.mfa.photoprinting.model.Delivery;
import java.util.List;

public interface DeliveryService {
    List<Delivery> getAllDeliveries();
    Delivery getDeliveryById(Long id);
    Delivery createDelivery(Delivery delivery);
    Delivery updateDelivery(Long id, Delivery delivery);
    void deleteDelivery(Long id);
    Delivery getDeliveryByOrderId(Long orderId);
}