package org.xide.models.enums;

public enum BigO {
    n1,
    LOGN,
    N,
    NLOGN,
    Ne2,
    Ne3;

    public static String getTableCreate() {
        return """
                ENUM('n1', 'logn', 'n', 'nlogn', 'ne2', 'ne3') NOT NULL
                """;
    }
}
