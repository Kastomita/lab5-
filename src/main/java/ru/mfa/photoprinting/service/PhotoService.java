package ru.mfa.photoprinting.service;

import ru.mfa.photoprinting.model.Photo;
import java.util.List;

public interface PhotoService {
    List<Photo> getAllPhotos();
    Photo getPhotoById(Long id);
    Photo createPhoto(Photo photo);
    Photo updatePhoto(Long id, Photo photo);
    void deletePhoto(Long id);
    List<Photo> getPhotosByOrderId(Long orderId);
}