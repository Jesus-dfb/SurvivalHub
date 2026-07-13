package com.survivalhub.service;

import com.survivalhub.model.AppUser;
import com.survivalhub.model.RegisterRequest;
import com.survivalhub.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> getAllUsers() {
        return appUserRepository.findAllByOrderByDisplayNameAsc();
    }

    public Optional<AppUser> getUserById(Long id) {
        return appUserRepository.findById(id);
    }

    public Optional<AppUser> registerUser(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return Optional.empty();
        }

        if (request.getPassword() == null || request.getPassword().length() < 6) {
            return Optional.empty();
        }

        String username = request.getUsername().trim();
        String email = request.getEmail() == null ? "" : request.getEmail().trim();

        if (appUserRepository.findByUsernameIgnoreCase(username).isPresent()) {
            return Optional.empty();
        }

        if (!email.isBlank() && appUserRepository.findByEmailIgnoreCase(email).isPresent()) {
            return Optional.empty();
        }

        String displayName = request.getDisplayName();

        if (displayName == null || displayName.isBlank()) {
            displayName = username;
        }

        AppUser userToSave = new AppUser(
                null,
                username,
                email.isBlank() ? null : email,
                passwordEncoder.encode(request.getPassword()),
                displayName.trim(),
                LocalDateTime.now()
        );

        return Optional.of(appUserRepository.save(userToSave));
    }

    public Optional<AppUser> findByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) {
            return Optional.empty();
        }

        String value = usernameOrEmail.trim();
        Optional<AppUser> userByUsername = appUserRepository.findByUsernameIgnoreCase(value);

        if (userByUsername.isPresent()) {
            return userByUsername;
        }

        return appUserRepository.findByEmailIgnoreCase(value);
    }
}
