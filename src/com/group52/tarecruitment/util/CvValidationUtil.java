package com.group52.tarecruitment.util;

public final class CvValidationUtil {
    public static final long MAX_CV_SIZE_BYTES = 5L * 1024L * 1024L;

    private CvValidationUtil() {
    }

    public static void validate(String fileName, long fileSizeBytes) {
        String normalizedFileName = ValidationUtil.requireText(fileName, "CV file name");
        if (!isSupportedExtension(normalizedFileName)) {
            throw new IllegalArgumentException("Only .pdf or .txt CV files are supported.");
        }
        if (fileSizeBytes > MAX_CV_SIZE_BYTES) {
            throw new IllegalArgumentException("CV file is too large. Please choose a file <= 5 MB.");
        }
    }

    public static boolean isSupportedExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".pdf") || lowerName.endsWith(".txt");
    }
}
