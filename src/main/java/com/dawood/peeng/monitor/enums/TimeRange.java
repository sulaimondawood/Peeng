package com.dawood.peeng.monitor.enums;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

public enum TimeRange {
    ONE_HOUR("1h") {
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusHours(1);
        }
    },
    SIX_HOURS("6h"){
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusHours(6);
        }
    },
    TWENTY_FOUR_HOURS("24h"){
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusDays(1);
        }
    },
    SEVEN_DAYS("7d"){
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusDays(7);
        }
    },
    THIRTY_DAYS("30d"){
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusDays(30);
        }
    };

    private final String timeRange;

    private TimeRange(String value){
        this.timeRange = value;
    }

    public String getTimeRange(){
        return timeRange;
    }

    private static final List<TimeRange> VALUES = List.of(values());

    public static TimeRange fromString(String value){
        if(value == null){
            throw new IllegalArgumentException("Time range cannot be null");
        }

        String cleanedValue = value.trim().toLowerCase();

       return VALUES.stream()
                .filter(range->range.timeRange.equals(cleanedValue))
                .findFirst()
                .orElseThrow(()->new IllegalArgumentException("Unsupported range: " + value));
    }

    public abstract LocalDateTime getFromTimestamp(LocalDateTime to);


}
