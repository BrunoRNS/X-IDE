package org.xide.models.enums;

public enum Lang {
    PYTHON,
    KOTLIN,
    JAVA,
    CPP,
    C,
    RUST,
    GO,
    GROOVY,
    JAVASCRIPT;

    public static String getTableCreate() {
        return """
                ENUM('python', 'kotlin', 'java', 'cpp', 'c', 'rust', 'go', 'groovy', 'javascript') NOT NULL
                """;
    }
}
