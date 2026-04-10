package ru.mfa.photoprinting.service.impl;

import ru.mfa.photoprinting.model.Delivery;
import ru.mfa.photoprinting.repository.DeliveryRepository;
import ru.mfa.photoprinting.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Override
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }

    @Override
    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Delivery createDelivery(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public Delivery updateDelivery(Long id, Delivery deliveryDetails) {
        Delivery delivery = deliveryRepository.findById(id).orElse(null);
        if (delivery == null) {
            return null;
        }
        delivery.setAddress(deliveryDetails.getAddress());
        delivery.setStatus(deliveryDetails.getStatus());
        delivery.setTrackingNumber(deliveryDetails.getTrackingNumber());
        delivery.setOrderId(deliveryDetails.getOrderId());
        return deliveryRepository.save(delivery);
    }

    @Override
    @Transactional
    public void deleteDelivery(Long id) {
        deliveryRepository.deleteById(id);
    }

    @Override
    public Delivery getDeliveryByOrderId(Long orderId) {
        return deliveryRepository.findByOrderId(orderId).orElse(null);
    }
}