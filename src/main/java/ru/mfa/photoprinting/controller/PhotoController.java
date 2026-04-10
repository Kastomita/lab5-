package ru.mfa.photoprinting.controller;

import ru.mfa.photoprinting.model.Photo;
import ru.mfa.photoprinting.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Photo>> getAllPhotos() {
        return ResponseEntity.ok(photoService.getAllPhotos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Photo> getPhotoById(@PathVariable Long id) {
        Photo photo = photoService.getPhotoById(id);
        if (photo != null) {
            return ResponseEntity.ok(photo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Photo> createPhoto(@RequestBody Photo photo) {
        Photo createdPhoto = photoService.createPhoto(photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPhoto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Photo> updatePhoto(@PathVariable Long id, @RequestBody Photo photo) {
        Photo updatedPhoto = photoService.updatePhoto(id, photo);
        if (updatedPhoto != null) {
            return ResponseEntity.ok(updatedPhoto);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Photo>> getPhotosByOrderId(@PathVariable Long orderId) {
        List<Photo> photos = photoService.getPhotosByOrderId(orderId);
        return ResponseEntity.ok(photos);
    }
}