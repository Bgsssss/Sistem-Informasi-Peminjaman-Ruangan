package com.zedd.rpl.model.trustcore.res;

import com.zedd.rpl.enums.ScoreChangeType;

import java.time.LocalDateTime;

public record ScoreHistoryResponse(
    LocalDateTime createdAt,
    ScoreChangeType type,
    int delta,
    int scoreAfter,
    String note
) {}
