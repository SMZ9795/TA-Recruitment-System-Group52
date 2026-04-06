package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.util.List;
import java.util.Optional;

/**
 * Unified authentication service.
 * Supports login by user ID (student ID / staff ID / admin ID) or by email.
 * TA registers with student ID; MO and Admin are pre-created in the system.
 */
public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Unified login: accepts either a user ID or an email address.
     * Automatically detects which format the identifier is in.
     */
    public Optional<User> login(String identifier, String password) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        if (password == null || password.isBlank()) {
            return Optional.empty();
        }

        String trimmedId = identifier.trim();
        String trimmedPw = password.trim();

        // Try by user ID first, then by email
        Optional<User> user = userRepository.findById(trimmedId);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(trimmedId);
        }

        return user.filter(User::isActive)
                .filter(u -> u.getPassword().equals(trimmedPw));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    // Registers a TA with a generated system ID for simple UI flows.
    public User registerTa(String name, String email, String password) {
        return registerTaInternal(IdGenerator.nextId("TA"), name, email, password, false);
    }

    // Registers a TA using the supplied student ID as the login ID.
    public User registerTa(String studentId, String name, String email, String password) {
        String normalizedStudentId = requireText(studentId, "Student ID");
        if (!normalizedStudentId.matches("\\d{9,12}")) {
            throw new IllegalArgumentException("Student ID must be 9 to 12 digits.");
        }
        return registerTaInternal(normalizedStudentId, name, email, password, true);
    }

    private User registerTaInternal(String userId, String name, String email, String password, boolean enforceUniqueId) {
        String normalizedName = requireText(name, "Name");
        String normalizedEmail = requireText(email, "Email");

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (enforceUniqueId && userRepository.findById(userId).isPresent()) {
            throw new IllegalArgumentException("Student ID is already registered.");
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User(
                userId,
                Role.TA,
                normalizedName,
                normalizedEmail,
                password,
                "",
                0,
                "",
                0,
                true);
        userRepository.save(user);
        return user;
    }

    public User createMoAccount(String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User(
                IdGenerator.nextId("MO"),
                Role.MO,
                name,
                email,
                password,
                "",
                0,
                "",
                0,
                true);
        userRepository.save(user);
        return user;
    }

    public void updateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        userRepository.save(user);
    }

    public void updatePassword(String userId, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public void setUserActive(String userId, boolean active) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setActive(active);
        userRepository.save(user);
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
