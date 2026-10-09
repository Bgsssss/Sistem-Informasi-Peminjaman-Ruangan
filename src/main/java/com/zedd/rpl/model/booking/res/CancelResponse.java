package com.zedd.rpl.model.booking.res;

import com.zedd.rpl.enums.BookingStatus;

public record CancelResponse(
    Long bookingId,
    BookingStatus status,
    int scoreChange,
    int currentScore,
    String message
) {}

