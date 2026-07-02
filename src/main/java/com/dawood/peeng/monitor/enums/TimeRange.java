package com.dawood.peeng.monitor.enums;

import java.time.LocalDateTime;

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

    public abstract LocalDateTime getFromTimestamp(LocalDateTime to);
}
