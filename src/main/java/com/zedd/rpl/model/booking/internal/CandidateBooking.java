package com.zedd.rpl.model.booking.internal;

import java.time.LocalDate;
import java.time.LocalTime;

/** Lightweight data for scheduled jobs. The entity is reloaded (with a lock) inside a per-booking transaction. */
public record CandidateBooking(
    Long id,
    Long studentId,
    LocalDate bookingDate,
    LocalTime startTime,
    LocalTime endTime
) {}

