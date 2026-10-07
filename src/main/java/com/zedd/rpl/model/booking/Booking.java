package com.zedd.rpl.model.booking;

import com.zedd.rpl.common.BaseEntity;
import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.model.room.Room;
import com.zedd.rpl.model.student.Student;
import com.zedd.rpl.model.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder

public class Booking extends BaseEntity {
  /** Check-in tolerance in minutes before auto-cancellation. */
  public static final int CHECKIN_TOLERANCE_MINUTES = 10;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @Column(nullable = false)
  private LocalDate date;

  @Column(name = "start_slot", nullable = false)
  private LocalTime startSlot;

  @Column(name = "end_slot", nullable = false)
  private LocalTime endSlot;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookingStatus status = BookingStatus.ACTIVE;

  @Column(name = "checkin_time")
  private LocalDateTime checkinTime;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  @Column(name = "cancellation_reason", length = 255)
  private String cancellationReason;

  /** Admin (user with admin role) who cancelled the booking. Null if not cancelled by an admin. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cancelled_by_admin_id")
  private User cancelledBy;

  // Generated column 'used_slot' is intentionally unmapped. Handled by database.

  /** Slots are always 1 hour long, so endSlot is computed from startSlot. */
  public static Booking create(Student student, Room room, LocalDate date, LocalTime startSlot) {
    return Booking.builder()
        .student(student)
        .room(room)
        .date(date)
        .startSlot(startSlot)
        .endSlot(startSlot.plusHours(1))
        .build();
  }

  public LocalDateTime startAt() {
    return LocalDateTime.of(date, startSlot);
  }

  public LocalDateTime endAt()   {
    return LocalDateTime.of(date, endSlot);
  }

  /** Check-in deadline: start time plus tolerance. */
  public LocalDateTime checkinDeadline() {
    return startAt().plusMinutes(CHECKIN_TOLERANCE_MINUTES);
  }

  public void checkIn(LocalDateTime now) {
    this.checkinTime = now;
    this.status = BookingStatus.ATTENDED;
  }

  public void cancelByUser(LocalDateTime now) {
    this.status = BookingStatus.CANCELLED_BY_USER;
    this.cancelledAt = now;
  }

  public void cancelBySystem(LocalDateTime now) {
    this.status = BookingStatus.CANCELLED_BY_SYSTEM;
    this.cancelledAt = now;
    this.cancellationReason = "Did not check in within " + CHECKIN_TOLERANCE_MINUTES + " minutes";
  }

  public void cancelByAdmin(User admin, String reason, LocalDateTime now) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("Admin cancellation reason is required");
    }
    this.status = BookingStatus.CANCELLED_BY_ADMIN;
    this.cancelledAt = now;
    this.cancellationReason = reason;
    this.cancelledBy = admin;
  }
}
