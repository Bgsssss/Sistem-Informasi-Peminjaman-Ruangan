package com.zedd.rpl.model.booking.res;

import com.zedd.rpl.enums.BookingStatus;

import java.time.LocalDateTime;

public record CheckInResponse(
    Long bookingId,
    BookingStatus status,
    LocalDateTime checkedInAt
) {}
