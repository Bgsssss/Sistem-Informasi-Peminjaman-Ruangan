package com.zedd.rpl.model.booking.impl;

import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.enums.ScoreChangeType;
import com.zedd.rpl.model.booking.Booking;
import com.zedd.rpl.model.booking.BookingRepository;
import com.zedd.rpl.model.booking.internal.CandidateBooking;
import com.zedd.rpl.model.student.Student;
import com.zedd.rpl.model.student.StudentRepository;
import com.zedd.rpl.model.trustcore.impl.TrustScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Logic of the scheduled jobs. Each booking is processed in its own transaction, so one failure
 * does not roll back the others. Safe to run again: it only touches bookings still in the expected status.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingAutomationService {

  private final BookingRepository bookingRepository;
  private final StudentRepository studentRepository;
  private final TrustScoreService trustScoreService;
  private final TransactionTemplate transactionTemplate;
  private final RplProperties props;
  private final Clock clock;

  /** Cancels ACTIVE bookings past the check-in tolerance, then deducts the no-show penalty. */
  public int cancelLateBookings() {
    LocalDateTime now = LocalDateTime.now(clock);
    List<CandidateBooking> candidates = bookingRepository.findCandidates(BookingStatus.ACTIVE, now.toLocalDate());

    int processed = 0;
    for (CandidateBooking c : candidates) {
      LocalDateTime deadline = LocalDateTime.of(c.bookingDate(), c.startTime())
          .plusMinutes(props.getCheckInToleranceMinutes());
      if (!now.isAfter(deadline)) {
        continue;
      }
      try {
        Boolean done = transactionTemplate.execute(status -> cancelLate(c, now));
        if (Boolean.TRUE.equals(done)) {
          processed++;
        }
      } catch (RuntimeException e) {
        log.error("Auto-cancel of booking {} failed", c.id(), e);
      }
    }
    if (processed > 0) {
      log.info("Auto-cancel: {} bookings cancelled for missing check-in", processed);
    }
    return processed;
  }

  /** Marks ATTENDED bookings whose time has passed as COMPLETED and adds score. */
  public int completeFinishedBookings() {
    LocalDateTime now = LocalDateTime.now(clock);
    List<CandidateBooking> candidates = bookingRepository.findCandidates(BookingStatus.ATTENDED, now.toLocalDate());

    int processed = 0;
    for (CandidateBooking c : candidates) {
      if (now.isBefore(LocalDateTime.of(c.bookingDate(), c.endTime()))) {
        continue;
      }
      try {
        Boolean done = transactionTemplate.execute(status -> complete(c));
        if (Boolean.TRUE.equals(done)) {
          processed++;
        }
      } catch (RuntimeException e) {
        log.error("Completing booking {} failed", c.id(), e);
      }
    }
    if (processed > 0) {
      log.info("Completed bookings: {} bookings marked COMPLETED", processed);
    }
    return processed;
  }

  // Lock order: student first, then booking (same as BookingService).

  private boolean cancelLate(CandidateBooking c, LocalDateTime now) {
    Student student = studentRepository.findByIdForUpdate(c.studentId()).orElseThrow();
    Booking booking = bookingRepository.findByIdForUpdate(c.id()).orElseThrow();
    if (booking.getStatus() != BookingStatus.ACTIVE) {
      return false;   // already checked in or canceled by someone else
    }
    booking.cancelBySystem(now, props.getCheckInToleranceMinutes());
    trustScoreService.apply(student, -props.getScore().getNoShow(), ScoreChangeType.NO_SHOW, booking,
        "Auto-cancel: no check-in within " + props.getCheckInToleranceMinutes() + " minutes");
    return true;
  }

  private boolean complete(CandidateBooking c) {
    Student student = studentRepository.findByIdForUpdate(c.studentId()).orElseThrow();
    Booking booking = bookingRepository.findByIdForUpdate(c.id()).orElseThrow();
    if (booking.getStatus() != BookingStatus.ATTENDED) {
      return false;
    }
    booking.setStatus(BookingStatus.COMPLETED);
    // Check-in is only possible within the tolerance, so every ATTENDED booking was on time.
    trustScoreService.apply(student, props.getScore().getCompletedOnTime(),
        ScoreChangeType.COMPLETED_ON_TIME, booking, "Booking completed with on-time check-in");
    return true;
  }
}

