package com.dawood.peeng.identity.dtos.response;

public record TeamOverview(
        int operators,
        int activeOperators,
        int pendingOperators,
        int suspendedOperators
) {
}
