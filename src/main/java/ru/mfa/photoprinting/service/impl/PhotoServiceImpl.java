package ru.mfa.photoprinting.service.impl;

import ru.mfa.photoprinting.model.Photo;
import ru.mfa.photoprinting.repository.PhotoRepository;
import ru.mfa.photoprinting.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PhotoServiceImpl implements PhotoService {

    @Autowired
    private PhotoRepository photoRepository;

    @Override
    public List<Photo> getAllPhotos() {
        return photoRepository.findAll();
    }

    @Override
    public Photo getPhotoById(Long id) {
        return photoRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Photo createPhoto(Photo photo) {
        return photoRepository.save(photo);
    }

    @Override
    @Transactional
    public Photo updatePhoto(Long id, Photo photoDetails) {
        Photo photo = photoRepository.findById(id).orElse(null);
        if (photo == null) {
            return null;
        }
        photo.setFileName(photoDetails.getFileName());
        photo.setFileUrl(photoDetails.getFileUrl());
        photo.setQuantity(photoDetails.getQuantity());
        photo.setDescription(photoDetails.getDescription());
        photo.setFormatId(photoDetails.getFormatId());
        photo.setOrderId(photoDetails.getOrderId());
        return photoRepository.save(photo);
    }

    @Override
    @Transactional
    public void deletePhoto(Long id) {
        photoRepository.deleteById(id);
    }

    @Override
    public List<Photo> getPhotosByOrderId(Long orderId) {
        return photoRepository.findByOrderId(orderId);
    }
}