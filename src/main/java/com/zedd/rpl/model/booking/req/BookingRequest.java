package com.zedd.rpl.model.booking.req;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

/** startTime uses the format "15:00". A slot is always 1 hour, so the end time is computed by the server. */
public record BookingRequest(
    @NotNull Long roomId,
    @NotNull LocalDate date,
    @NotNull LocalTime startTime
) {}

