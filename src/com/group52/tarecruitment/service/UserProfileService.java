package com.group52.tarecruitment.service;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.repository.UserRepository;
import com.group52.tarecruitment.util.ValidationUtil;

public class UserProfileService {
    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User updateTaProfile(String userId, String programme, String yearOfStudy, String skills,
            String availableHours) {
        return updateTaProfile(userId, programme, yearOfStudy, skills, availableHours, "");
    }

    public User updateTaProfile(String userId, String programme, String yearOfStudy, String skills,
            String availableHours, String cvFilePath) {
        User user = userRepository.findById(ValidationUtil.requireText(userId, "User ID"))
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getRole() != Role.TA) {
            throw new IllegalArgumentException("Only TA users can update a TA profile.");
        }

        user.setProgramme(ValidationUtil.requireText(programme, "Programme"));
        user.setYearOfStudy(ValidationUtil.parseIntInRange(yearOfStudy, "Year of study", 1, 12));
        user.setSkills(ValidationUtil.requireText(skills, "Skills"));
        user.setAvailableHours(ValidationUtil.parseIntInRange(availableHours, "Available hours", 1, 168));
        if (cvFilePath != null && !cvFilePath.isBlank()) {
            user.setCvFilePath(cvFilePath.trim());
        }
        userRepository.save(user);
        return user;
    }

    public String formatProfile(User user) {
        return "Programme: " + formatValue(user.getProgramme()) + System.lineSeparator()
                + "Year of study: " + formatNumber(user.getYearOfStudy()) + System.lineSeparator()
                + "Skills: " + formatValue(user.getSkills()) + System.lineSeparator()
                + "Available hours: " + formatNumber(user.getAvailableHours()) + System.lineSeparator()
                + "CV path: " + formatValue(user.getCvFilePath());
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
