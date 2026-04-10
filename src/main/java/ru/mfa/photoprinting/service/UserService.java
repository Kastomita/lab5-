package ru.mfa.photoprinting.service;

import ru.mfa.photoprinting.dto.RegistrationRequestDTO;
import ru.mfa.photoprinting.model.User;
import java.util.Optional;

public interface UserService {
    User registerUser(RegistrationRequestDTO request);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    User saveUser(User user);
}