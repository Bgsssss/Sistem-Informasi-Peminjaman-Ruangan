package com.zedd.rpl.model.user.res;

import com.zedd.rpl.model.trustcore.res.ScoreHistoryResponse;

import java.util.List;

/** blockedMessage is set when the score is below the minimum. The UI shows it on the booking page. */
public record ProfileResponse(
    String studentNumber,
    String name,
    String faculty,
    String email,
    int trustScore,
    int minScoreToBook,
    boolean canBook,
    String blockedMessage,
    List<ScoreHistoryResponse> scoreHistory
) {}
