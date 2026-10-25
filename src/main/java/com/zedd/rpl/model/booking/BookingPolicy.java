package com.zedd.rpl.model.booking;

import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.enums.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/** Booking time rules in one place, used by services and the mapper so the results always agree. */
@Component
@RequiredArgsConstructor
public class BookingPolicy {

  private final RplProperties props;

  /** Last moment to check in: start time plus the tolerance. */
  public LocalDateTime checkInDeadline(Booking b) {
    return b.startDateTime().plusMinutes(props.getCheckInToleranceMinutes());
  }

  /** Check-in is open from the start time until the tolerance deadline. */
  public boolean canCheckIn(Booking b, LocalDateTime now) {
    return b.getStatus() == BookingStatus.ACTIVE
        && !now.isBefore((b.startDateTime().minusMinutes(props.getCheckInToleranceMinutes())))
        && !now.isAfter(checkInDeadline(b));
  }

  /** Self-cancellation is only possible before the schedule starts. After that, auto-cancel takes over. */
  public boolean canCancel(Booking b, LocalDateTime now) {
    return b.getStatus() == BookingStatus.ACTIVE && now.isBefore(b.startDateTime());
  }

  /** Penalty if cancelled now: 0 when at least N hours before the schedule, otherwise a late cancellation. */
  public int cancellationPenalty(Booking b, LocalDateTime now) {
    Duration remaining = Duration.between(now, b.startDateTime());
    boolean late = remaining.compareTo(Duration.ofHours(props.getFreeCancellationHours())) < 0;
    return late ? props.getScore().getLateCancellation() : 0;
  }
}

