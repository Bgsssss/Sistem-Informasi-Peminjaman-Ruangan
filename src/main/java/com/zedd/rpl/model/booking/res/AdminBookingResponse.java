package com.zedd.rpl.model.booking.res;

import com.zedd.rpl.enums.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminBookingResponse(
    Long id,
    String studentNumber,
    String studentName,
    Long roomId,
    String roomName,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    BookingStatus status,
    LocalDateTime checkedInAt,
    LocalDateTime cancelledAt,
    String cancelReason,
    String cancelledBy,
    LocalDateTime createdAt
) {}

