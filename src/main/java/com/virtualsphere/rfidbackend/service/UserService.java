package com.virtualsphere.rfidbackend.service;

import com.virtualsphere.rfidbackend.dto.UserRequest;
import com.virtualsphere.rfidbackend.exception.DuplicateResourceException;
import com.virtualsphere.rfidbackend.exception.ResourceNotFoundException;
import com.virtualsphere.rfidbackend.model.Role;
import com.virtualsphere.rfidbackend.model.User;
import com.virtualsphere.rfidbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional
    public User create(UserRequest request) {
        if (userRepository.findByUsernameIgnoreCase(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists: " + request.getUsername());
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required when creating a user");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() == null ? Role.USER : request.getRole());
        user.setLocation(request.getLocation());
        user.setActive(request.getActive() == null || request.getActive());
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, UserRequest request) {
        User user = findById(id);

        if (request.getUsername() != null && !request.getUsername().equalsIgnoreCase(user.getUsername())) {
            userRepository.findByUsernameIgnoreCase(request.getUsername()).ifPresent(existing -> {
                throw new DuplicateResourceException("Username already exists: " + request.getUsername());
            });
            user.setUsername(request.getUsername());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getRole() != null) user.setRole(request.getRole());
        if (request.getLocation() != null) user.setLocation(request.getLocation());
        if (request.getActive() != null) user.setActive(request.getActive());

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }
}
