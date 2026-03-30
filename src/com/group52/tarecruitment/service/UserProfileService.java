package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.UserRepository;

public class UserProfileService {
    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User updateTaProfile(String userId, String programme, int yearOfStudy, String skills, int availableHours) {
        User user = userRepository.findById(requireText(userId, "User ID"))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRole() != Role.TA) {
            throw new IllegalArgumentException("Only TA users can update a TA profile.");
        }

        user.setProgramme(requireText(programme, "Programme"));
        user.setYearOfStudy(requirePositiveNumber(yearOfStudy, "Year of study"));
        user.setSkills(requireText(skills, "Skills"));
        user.setAvailableHours(requirePositiveNumber(availableHours, "Available hours"));
        userRepository.save(user);
        return user;
    }

    public String formatProfile(User user) {
        return "Programme: " + formatValue(user.getProgramme()) + System.lineSeparator()
                + "Year of study: " + formatNumber(user.getYearOfStudy()) + System.lineSeparator()
                + "Skills: " + formatValue(user.getSkills()) + System.lineSeparator()
                + "Available hours: " + formatNumber(user.getAvailableHours());
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private int requirePositiveNumber(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
        return value;
    }

    private String formatValue(String value) {
        if (value == null || value.isBlank()) {
            return "Not set";
        }
        return value;
    }

    private String formatNumber(int value) {
        if (value <= 0) {
            return "Not set";
        }
        return String.valueOf(value);
    }
}
