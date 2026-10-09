package com.zedd.rpl.model.trustcore.res;

public record ScoreAdjustmentResponse(Long studentId, int oldScore, int newScore, int delta) {}
