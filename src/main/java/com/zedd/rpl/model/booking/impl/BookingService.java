package com.zedd.rpl.model.booking.impl;

import com.zedd.rpl.common.PageResponse;
import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.enums.ScoreChangeType;
import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.exception.ErrorCode;
import com.zedd.rpl.model.auditlog.AuditLog;
import com.zedd.rpl.model.auditlog.AuditLogRepository;
import com.zedd.rpl.model.availability.impl.BookableDateService;
import com.zedd.rpl.model.booking.Booking;
import com.zedd.rpl.model.booking.BookingMapper;
import com.zedd.rpl.model.booking.BookingPolicy;
import com.zedd.rpl.model.booking.BookingRepository;
import com.zedd.rpl.model.booking.req.AdminCancelRequest;
import com.zedd.rpl.model.booking.req.BookingRequest;
import com.zedd.rpl.model.booking.res.AdminBookingResponse;
import com.zedd.rpl.model.booking.res.BookingResponse;
import com.zedd.rpl.model.booking.res.CancelResponse;
import com.zedd.rpl.model.booking.res.CheckInResponse;
import com.zedd.rpl.model.room.Room;
import com.zedd.rpl.model.room.RoomRepository;
import com.zedd.rpl.model.student.Student;
import com.zedd.rpl.model.student.StudentRepository;
import com.zedd.rpl.model.trustcore.impl.TrustScoreService;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

  private final BookingRepository bookingRepository;
  private final StudentRepository studentRepository;
  private final RoomRepository roomRepository;
  private final UserRepository userRepository;
  private final AuditLogRepository auditLogRepository;
  private final BookableDateService bookableDateService;
  private final TrustScoreService trustScoreService;
  private final BookingPolicy policy;
  private final BookingMapper mapper;
  private final RplProperties props;
  private final Clock clock;

  /**
   * Creates a booking (FR-6 to FR-12, FR-18, FR-19, FR-24).
   * The student row is locked first so the booking-limit checks cannot be skipped by two
   * parallel requests. The same slot is guarded by the database unique constraint.
   */
  @Transactional
  public BookingResponse create(Long studentId, BookingRequest req) {
    LocalDateTime now = LocalDateTime.now(clock);

    Student student = studentRepository.findByIdForUpdate(studentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    if (!student.isActive()) {
      throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE);
    }
    int minScore = props.getScore().getMinToBook();
    if (student.getTrustScore() < minScore) {
      throw new BusinessException(ErrorCode.TRUST_SCORE_TOO_LOW,
          "Your trust score is " + student.getTrustScore() + ", below the minimum of " + minScore
              + ". Contact the facility admin to have your score restored.");
    }

    validateDateAndSlot(req.date(), req.startTime(), now);

    Room room = roomRepository.findById(req.roomId())
        .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    if (!room.isActive()) {
      throw new BusinessException(ErrorCode.ROOM_INACTIVE);
    }

    long ongoing = bookingRepository.countOngoing(studentId, BookingStatus.ONGOING,
        now.toLocalDate(), now.toLocalTime());
    if (ongoing >= props.getMaxActiveBookings()) {
      throw new BusinessException(ErrorCode.ACTIVE_BOOKING_LIMIT_REACHED,
          "Active booking limit reached (" + props.getMaxActiveBookings()
              + "). Finish or cancel an existing booking first.");
    }

    long hoursThatDay = bookingRepository.countByStudentIdAndBookingDateAndStatusIn(
        studentId, req.date(), BookingStatus.OCCUPYING_SLOT);
    if (hoursThatDay + 1 > props.getMaxHoursPerDay()) {
      throw new BusinessException(ErrorCode.DAILY_HOURS_LIMIT_EXCEEDED,
          "Total booking duration on " + req.date() + " cannot exceed "
              + props.getMaxHoursPerDay() + " hours");
    }

    if (bookingRepository.existsByRoomIdAndBookingDateAndStartTimeAndStatusIn(
        room.getId(), req.date(), req.startTime(), BookingStatus.OCCUPYING_SLOT)) {
      throw new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED);
    }

    Booking booking = Booking.create(student, room, req.date(), req.startTime());
    try {
      // saveAndFlush so a unique-constraint violation surfaces here, not at commit time
      bookingRepository.saveAndFlush(booking);
    } catch (DataIntegrityViolationException e) {
      throw new BusinessException(ErrorCode.SLOT_ALREADY_BOOKED);
    }
    return mapper.toResponse(booking, now);
  }

  /** The student's booking history with an optional status filter (FR-13). */
  @Transactional(readOnly = true)
  public List<BookingResponse> getHistory(Long studentId, BookingStatus status) {
    LocalDateTime now = LocalDateTime.now(clock);
    return bookingRepository.findHistory(studentId, status).stream()
        .map(b -> mapper.toResponse(b, now))
        .toList();
  }

  /**
   * Self-cancellation (FR-14). At least N hours before the schedule: no penalty.
   * Closer than that: late cancellation, the score drops. After the schedule starts the booking
   * cannot be canceled, so students cannot trade the no-show penalty for the smaller cancel penalty.
   */
  @Transactional
  public CancelResponse cancel(Long studentId, Long bookingId) {
    LocalDateTime now = LocalDateTime.now(clock);

    Student student = studentRepository.findByIdForUpdate(studentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    Booking booking = findOwnedBooking(bookingId, studentId);

    if (booking.getStatus() != BookingStatus.ACTIVE) {
      throw new BusinessException(ErrorCode.INVALID_BOOKING_STATUS,
          "A booking with status " + booking.getStatus() + " cannot be cancelled");
    }
    if (!policy.canCancel(booking, now)) {
      throw new BusinessException(ErrorCode.CANCELLATION_AFTER_START,
          "The schedule has already started. A booking without check-in is cancelled automatically.");
    }

    int penalty = policy.cancellationPenalty(booking, now);
    booking.cancelByUser(now);

    int scoreChange = 0;
    String message = "Booking cancelled with no score penalty";
    if (penalty > 0) {
      scoreChange = trustScoreService.apply(student, -penalty, ScoreChangeType.LATE_CANCELLATION, booking,
          "Cancelled less than " + props.getFreeCancellationHours() + " hours before the schedule");
      message = "Booking cancelled. Your trust score dropped by " + penalty + " points";
    }
    return new CancelResponse(booking.getId(), booking.getStatus(), scoreChange,
        student.getTrustScore(), message);
  }

  /** Manual check-in (FR-15): only between the start time and the tolerance deadline. */
  @Transactional
  public CheckInResponse checkIn(Long studentId, Long bookingId) {
    LocalDateTime now = LocalDateTime.now(clock);
    Booking booking = findOwnedBooking(bookingId, studentId);

    if (booking.getStatus() != BookingStatus.ACTIVE) {
      throw new BusinessException(ErrorCode.INVALID_BOOKING_STATUS,
          "A booking with status " + booking.getStatus() + " cannot be checked in");
    }
    if (now.isBefore(booking.startDateTime())) {
      throw new BusinessException(ErrorCode.CHECK_IN_TOO_EARLY,
          "Check-in opens at " + booking.getStartTime().minusMinutes(props.getCheckInToleranceMinutes()));
    }
    if (now.isAfter(policy.checkInDeadline(booking))) {
      throw new BusinessException(ErrorCode.CHECK_IN_WINDOW_CLOSED);
    }
    booking.checkIn(now);
    return new CheckInResponse(booking.getId(), booking.getStatus(), booking.getCheckedInAt());
  }

  // =====================================================================
  // Admin
  // =====================================================================

  @Transactional(readOnly = true)
  public PageResponse<AdminBookingResponse> listForAdmin(LocalDate date, Long roomId,
                                                         BookingStatus status, int page, int size) {
    PageRequest paging = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
        Sort.by(Sort.Direction.DESC, "bookingDate").and(Sort.by(Sort.Direction.DESC, "startTime")));
    Page<AdminBookingResponse> result = bookingRepository
        .searchForAdmin(date, roomId, status, paging)
        .map(mapper::toAdminResponse);
    return PageResponse.of(result);
  }

  /** Manual admin cancellation: reason required, written to the audit log, no score penalty (FR-33). */
  @Transactional
  public AdminBookingResponse cancelByAdmin(Long adminId, Long bookingId, AdminCancelRequest req) {
    Booking booking = bookingRepository.findByIdForUpdate(bookingId)
        .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
    if (booking.getStatus() != BookingStatus.ACTIVE) {
      throw new BusinessException(ErrorCode.INVALID_BOOKING_STATUS,
          booking.getStatus(),"cancelled by an admin");
    }
    User admin = userRepository.getReferenceById(adminId);
    String reason = req.reason().trim();

    booking.cancelByAdmin(admin, reason, LocalDateTime.now(clock));
    auditLogRepository.save(new AuditLog(admin, "BOOKING_CANCEL", "booking", booking.getId(), reason));
    return mapper.toAdminResponse(booking);
  }

  // =====================================================================
  // Helpers
  // =====================================================================

  /** A booking owned by someone else is reported as "not found" so its existence does not leak. */
  private Booking findOwnedBooking(Long bookingId, Long studentId) {
    return bookingRepository.findByIdForUpdate(bookingId)
        .filter(b -> b.getStudent().getId().equals(studentId))
        .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
  }

  private void validateDateAndSlot(LocalDate date, LocalTime slot, LocalDateTime now) {
    if (date.isBefore(now.toLocalDate())) {
      throw new BusinessException(ErrorCode.DATE_IN_PAST);
    }
    if (!bookableDateService.isOpen(date)) {
      throw new BusinessException(ErrorCode.DATE_NOT_OPEN,
          "Date " + date + " is not open yet. Booking for tomorrow opens at 20:00.");
    }
    boolean validSlot = slot.getMinute() == 0 && slot.getSecond() == 0 && slot.getNano() == 0
        && !slot.isBefore(props.getOpeningTime())
        && slot.isBefore(props.getClosingTime());
    if (!validSlot) {
      throw new BusinessException(ErrorCode.INVALID_SLOT,
          "Slots are whole hours between " + props.getOpeningTime() + " and " + props.getClosingTime());
    }
    if (LocalDateTime.of(date, slot).isBefore(now)) {
      throw new BusinessException(ErrorCode.SLOT_ALREADY_STARTED,
          "The " + slot + " slot has already started");
    }
  }
}

