package com.group52.tarecruitment.repository;

import com.group52.tarecruitment.model.Role;
import com.group52.tarecruitment.model.User;
import com.group52.tarecruitment.util.CsvUtil;
import com.group52.tarecruitment.util.FileUtil;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private static final String HEADER =
            "id,role,name,email,password,programme,yearOfStudy,skills,availableHours,active";

    private final Path filePath;

    public UserRepository(Path filePath) {
        this.filePath = filePath;
        FileUtil.ensureFileExists(filePath, List.of(HEADER));
    }

    public List<User> findAll() {
        List<String> lines = FileUtil.readAllLines(filePath);
        List<User> users = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line == null || line.isBlank()) {
                continue;
            }
            List<String> values = CsvUtil.parseLine(line);
            if (values.size() != 10) {
                throw invalidRecord(i + 1, null);
            }
            try {
                users.add(toUser(values));
            } catch (RuntimeException e) {
                throw invalidRecord(i + 1, e);
            }
        }
        return users;
    }

    public Optional<User> findById(String userId) {
        return findAll().stream().filter(user -> user.getId().equalsIgnoreCase(userId)).findFirst();
    }

    public Optional<User> findByEmail(String email) {
        return findAll().stream().filter(user -> user.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    public void save(User user) {
        List<User> users = findAll();
        users.removeIf(existing -> existing.getId().equalsIgnoreCase(user.getId()));
        users.add(user);
        writeAll(users);
    }

    private void writeAll(List<User> users) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (User user : users) {
            lines.add(String.join(",",
                    CsvUtil.escape(user.getId()),
                    CsvUtil.escape(user.getRole().name()),
                    CsvUtil.escape(user.getName()),
                    CsvUtil.escape(user.getEmail()),
                    CsvUtil.escape(user.getPassword()),
                    CsvUtil.escape(user.getProgramme()),
                    CsvUtil.escape(String.valueOf(user.getYearOfStudy())),
                    CsvUtil.escape(user.getSkills()),
                    CsvUtil.escape(String.valueOf(user.getAvailableHours())),
                    CsvUtil.escape(String.valueOf(user.isActive()))));
        }
        FileUtil.writeAllLines(filePath, lines);
    }

    private User toUser(List<String> values) {
        return new User(
                values.get(0),
                Role.valueOf(values.get(1)),
                values.get(2),
                values.get(3),
                values.get(4),
                values.get(5),
                parseInt(values.get(6)),
                values.get(7),
                parseInt(values.get(8)),
                Boolean.parseBoolean(values.get(9)));
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return Integer.parseInt(value.trim());
    }

    private IllegalStateException invalidRecord(int lineNumber, RuntimeException cause) {
        String message = "Invalid user data in " + filePath.getFileName() + " at line " + lineNumber + ".";
        return cause == null ? new IllegalStateException(message) : new IllegalStateException(message, cause);
    }
}
