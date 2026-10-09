package com.zedd.rpl.model.booking.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCancelRequest(
    @NotBlank
    @Size(max = 255)
    String reason
) {}

