package org.xide.models.enums;

public enum SubmissionState {
    WA,
    ACCEPTED,
    PRESENTATION_ERROR,
    TIME_LIMITE_EXCEEDED,
    MEMORY_EXCEEDED;

    public static String getTableCreate() {
        return """
                ENUM('wa', 'accepted', 'presentation_error', 'time_limite_exceeded', 'memory_exceeded') NOT NULL
                """;
    }
}
