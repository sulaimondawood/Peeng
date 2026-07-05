package com.dawood.peeng.incident.enums;

import java.time.LocalDateTime;
import java.util.Arrays;

public enum DateRangeBucket {
    ALL_TIME("all") {
        @Override
        public LocalDateTime getFromLocalDateTime(LocalDateTime to) {
            return null;
        }
    },
    TODAY("today"){
        @Override
        public LocalDateTime getFromLocalDateTime(LocalDateTime to) {
            return to.toLocalDate().atStartOfDay();
        }
    },
    LAST_SEVEN_DAYS("7d"){
        @Override
        public LocalDateTime getFromLocalDateTime(LocalDateTime to) {
            return to.minusDays(7);
        }
    },
    LAST_30_DAYS("30d"){
        @Override
        public LocalDateTime getFromLocalDateTime(LocalDateTime to) {
            return to.minusDays(30);
        }
    };

    private final String rangeBucket;

    private DateRangeBucket(String value){
        this.rangeBucket = value;
    }

    public static DateRangeBucket fromString(String value){
        if (value == null) return null;

        String cleanedValue = value.trim().toLowerCase();

       return Arrays.stream(values())
                .filter(bucket->bucket.rangeBucket.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid date range: " + value));

    }

    public abstract  LocalDateTime getFromLocalDateTime(LocalDateTime to);
}
