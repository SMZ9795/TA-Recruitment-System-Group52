package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.util.IdGenerator;
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

    /**
     * TA self-registration with a student ID.
     * Student ID format: digits only, 9-12 characters (e.g. 231226244).
     */
    public User registerTa(String studentId, String name, String email, String password) {
        String normalizedStudentId = requireText(studentId, "Student ID");
        String normalizedName = requireText(name, "Name");
        String normalizedEmail = requireText(email, "Email");

        if (!normalizedStudentId.matches("\\d{9,12}")) {
            throw new IllegalArgumentException("Student ID must be 9-12 digits.");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }

        // Check student ID uniqueness
        String taId = "TA" + normalizedStudentId;
        if (userRepository.findById(taId).isPresent()) {
            throw new IllegalArgumentException("This student ID is already registered.");
        }
        // Check email uniqueness
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User(
                taId,
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

    /**
     * Backward-compatible TA registration without student ID (generates random ID).
     */
    public User registerTa(String name, String email, String password) {
        String normalizedName = requireText(name, "Name");
        String normalizedEmail = requireText(email, "Email");

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User(
                IdGenerator.nextId("TA"),
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

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}