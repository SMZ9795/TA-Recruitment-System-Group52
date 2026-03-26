package com.group52.tarecruitment.util;

import java.util.UUID;

public final class IdGenerator {
    private IdGenerator() {
    }

    public static String nextId(String prefix) {
        String compact = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return prefix + compact;
    }
}
