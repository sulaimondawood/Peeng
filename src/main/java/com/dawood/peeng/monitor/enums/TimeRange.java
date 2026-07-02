package com.dawood.peeng.monitor.enums;

import com.dawood.peeng.monitor.dtos.responses.ResponseTimePointProjection;
import com.dawood.peeng.monitor.repository.MonitorCheckRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public enum TimeRange {
    ONE_HOUR("1h") {
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusHours(1);
        }

        @Override
        public List<ResponseTimePointProjection> executeQuery(MonitorCheckRepository repo, UUID tenantId, UUID monitorId, LocalDateTime from, LocalDateTime to) {
            return repo.findHourlyBucket(tenantId,monitorId,from,to);
        }
    },

    TWENTY_FOUR_HOURS("24h"){
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusDays(1);
        }

        @Override
        public List<ResponseTimePointProjection> executeQuery(MonitorCheckRepository repo, UUID tenantId, UUID monitorId, LocalDateTime from, LocalDateTime to) {
            return repo.find24hrBucket(tenantId,monitorId,from,to);
        }
    },
    SEVEN_DAYS("7d"){
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusDays(7);
        }

        @Override
        public List<ResponseTimePointProjection> executeQuery(MonitorCheckRepository repo, UUID tenantId, UUID monitorId, LocalDateTime from, LocalDateTime to) {
            return repo.find7daysBucket(tenantId,monitorId,from,to);
        }
    },
    THIRTY_DAYS("30d"){
        @Override
        public LocalDateTime getFromTimestamp(LocalDateTime to) {
            return to.minusDays(30);
        }
        @Override
        public List<ResponseTimePointProjection> executeQuery(MonitorCheckRepository repo, UUID tenantId, UUID monitorId, LocalDateTime from, LocalDateTime to) {
            return repo.findMonthlyBucket(tenantId,monitorId,from,to);
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

    public abstract List<ResponseTimePointProjection> executeQuery(MonitorCheckRepository repo, UUID tenantId, UUID monitorId, LocalDateTime from, LocalDateTime to);

}
