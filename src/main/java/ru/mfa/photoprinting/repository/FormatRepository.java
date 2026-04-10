package ru.mfa.photoprinting.repository;

import ru.mfa.photoprinting.model.Format;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FormatRepository extends JpaRepository<Format, Long> {
    Optional<Format> findByName(String name);
}