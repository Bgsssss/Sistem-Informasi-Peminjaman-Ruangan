package com.zedd.rpl.model.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BookingMapper {

  private final BookingPolicy policy;

  public BookingResponse toResponse(Booking b, LocalDateTime now) {
    boolean canCancel = policy.canCancel(b, now);
    return new BookingResponse(
        b.getId(),
        b.getRoom().getId(),
        b.getRoom().getName(),
        b.getRoom().getLocation(),
        b.getBookingDate(),
        b.getStartTime(),
        b.getEndTime(),
        b.getStatus(),
        b.getCheckedInAt(),
        policy.checkInDeadline(b),
        policy.canCheckIn(b, now),
        canCancel,
        canCancel ? policy.cancellationPenalty(b, now) : null,
        b.getCancelReason());
  }

  public AdminBookingResponse toAdminResponse(Booking b) {
    return new AdminBookingResponse(
        b.getId(),
        b.getStudent().getStudentNumber(),
        b.getStudent().getName(),
        b.getRoom().getId(),
        b.getRoom().getName(),
        b.getBookingDate(),
        b.getStartTime(),
        b.getEndTime(),
        b.getStatus(),
        b.getCheckedInAt(),
        b.getCancelledAt(),
        b.getCancelReason(),
        b.getCancelledByAdmin() != null ? b.getCancelledByAdmin().getUsername() : null,
        b.getCreatedDate());
  }
}

