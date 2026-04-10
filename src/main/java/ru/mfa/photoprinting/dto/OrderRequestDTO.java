package ru.mfa.photoprinting.dto;

import java.util.List;

public class OrderRequestDTO {
    private Long customerId;
    private List<PhotoRequestDTO> photos;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<PhotoRequestDTO> getPhotos() { return photos; }
    public void setPhotos(List<PhotoRequestDTO> photos) { this.photos = photos; }
}