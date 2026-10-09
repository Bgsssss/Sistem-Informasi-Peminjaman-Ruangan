package com.zedd.rpl.model.trustcore.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScoreAdjustmentRequest(
    @NotNull
    @Min(0) @Max(100)
    Integer newScore,
    @NotBlank
    @Size(max = 255)
    String note
) {}

