package com.zedd.rpl.model.trustcore.res;

import java.time.LocalDateTime;

public record ViolationLogResponse(
    Long studentId,
    String studentNumber,
    String name,
    String faculty,
    int trustScore,
    long noShowCount,
    long lateCancellationCount,
    LocalDateTime lastViolationAt
) {}

