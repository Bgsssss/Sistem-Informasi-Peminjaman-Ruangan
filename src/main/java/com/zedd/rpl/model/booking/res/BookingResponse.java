package com.zedd.rpl.model.booking.res;

import com.zedd.rpl.enums.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * scorePenaltyIfCancelled: points lost if canceled right now (0 or 5), null when the booking
 * cannot be canceled. The UI uses it for the confirmation dialog before cancelling.
 */
public record BookingResponse(
    Long id,
    Long roomId,
    String roomName,
    String roomLocation,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    BookingStatus status,
    LocalDateTime checkedInAt,
    LocalDateTime checkInDeadline,
    boolean canCheckIn,
    boolean canCancel,
    Integer scorePenaltyIfCancelled,
    String cancelReason
) {}

