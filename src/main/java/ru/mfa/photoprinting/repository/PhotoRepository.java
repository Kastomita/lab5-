package ru.mfa.photoprinting.repository;

import ru.mfa.photoprinting.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByOrderId(Long orderId);
}