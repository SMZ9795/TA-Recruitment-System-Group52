package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.util.IdGenerator;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(User::isActive)
                .filter(user -> user.getPassword().equals(password));
    }

    public User registerTa(String name, String email, String password) {
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
                IdGenerator.nextId("TA"),
                Role.TA,
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
}
