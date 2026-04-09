package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.util.IdGenerator;
import com.group52.tarecruitment.util.ValidationUtil;
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
    public User login(String identifier, String password) {
        String normalizedIdentifier = ValidationUtil.requireText(identifier, "User ID or Email");
        String normalizedPassword = ValidationUtil.requireText(password, "Password");
        if (normalizedIdentifier.contains("@")) {
            ValidationUtil.requireEmail(normalizedIdentifier, "Email");
        }

        // Try by user ID first, then by email
        Optional<User> user = userRepository.findById(normalizedIdentifier);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(normalizedIdentifier);
        }

        User matchedUser = user.orElseThrow(
                () -> new IllegalArgumentException("No account matches the provided user ID or email."));
        if (!matchedUser.isActive()) {
            throw new IllegalArgumentException("This account is inactive.");
        }
        if (!matchedUser.getPassword().equals(normalizedPassword)) {
            throw new IllegalArgumentException("Incorrect password.");
        }

        return matchedUser;
    }

    /**
     * TA self-registration with a student ID.
     * Student ID format: digits only, 9-12 characters (e.g. 231226244).
     */
    public User registerTa(String studentId, String name, String email, String password) {
        String normalizedStudentId = ValidationUtil.requireStudentId(studentId);
        String normalizedName = ValidationUtil.requireText(name, "Name");
        String normalizedEmail = ValidationUtil.requireEmail(email, "Email");
        String normalizedPassword = ValidationUtil.requirePassword(password);

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
                normalizedPassword,
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
        String normalizedName = ValidationUtil.requireText(name, "Name");
        String normalizedEmail = ValidationUtil.requireEmail(email, "Email");
        String normalizedPassword = ValidationUtil.requirePassword(password);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        User user = new User(
                IdGenerator.nextId("TA"),
                Role.TA,
                normalizedName,
                normalizedEmail,
                normalizedPassword,
                "",
                0,
                "",
                0,
                true);
        userRepository.save(user);
        return user;
    }
}
