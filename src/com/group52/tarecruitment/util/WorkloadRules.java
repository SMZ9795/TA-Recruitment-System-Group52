package com.group52.tarecruitment.util;

/**
 * Centralized rule set for workload balancing.
 *
 * Thresholds stay in one place so the behaviour is explainable, deterministic,
 * and easy to adjust for demos or future policy changes.
 */
public final class WorkloadRules {
    /**
     * Above this value, a TA is classified as overloaded.
     */
    public static final int OVERLOADED_THRESHOLD_HOURS = 12;

    /**
     * Below this value, a TA is classified as underused.
     */
    public static final int UNDERUSED_THRESHOLD_HOURS = 4;

    /**
     * The maximum number of hours we move in one recommendation.
     */
    public static final int DEFAULT_MAX_TRANSFER_HOURS = 4;

    private WorkloadRules() {
        // Utility class.
    }

    public static WorkloadStatus classify(int weeklyWorkloadHours) {
        if (weeklyWorkloadHours > OVERLOADED_THRESHOLD_HOURS) {
            return WorkloadStatus.OVERLOADED;
        }
        if (weeklyWorkloadHours < UNDERUSED_THRESHOLD_HOURS) {
            return WorkloadStatus.UNDERUSED;
        }
        return WorkloadStatus.BALANCED;
    }

    public static int transferableHours(int overloadedHours) {
        if (overloadedHours <= OVERLOADED_THRESHOLD_HOURS) {
            return 0;
        }
        return Math.min(overloadedHours - OVERLOADED_THRESHOLD_HOURS, DEFAULT_MAX_TRANSFER_HOURS);
    }

    public static int missingHoursToBalanced(int underusedHours) {
        if (underusedHours >= UNDERUSED_THRESHOLD_HOURS) {
            return 0;
        }
        return UNDERUSED_THRESHOLD_HOURS - underusedHours;
    }

    public enum WorkloadStatus {
        OVERLOADED,
        BALANCED,
        UNDERUSED
    }
}
