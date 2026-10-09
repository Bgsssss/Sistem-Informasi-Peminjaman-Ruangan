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
  /**
   * Check-in tolerance in minutes before auto-cancellation.
   *//*
  public static final int CHECKIN_TOLERANCE_MINUTES = 10;*/

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @Column(name = "booking_date", nullable = false)
  private LocalDate bookingDate;

  @Column(name = "slot_start", nullable = false)
  private LocalTime startTime;

  @Column(name = "slot_end", nullable = false)
  private LocalTime endTime;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookingStatus status = BookingStatus.ACTIVE;

  @Column(name = "checkin_time")
  private LocalDateTime checkedInAt;

  @Column(name = "cancelled_at")
  private LocalDateTime cancelledAt;

  @Column(name = "cancel_reason")
  private String cancelReason;

  /**
   * Admin (user with admin role) who canceled the booking. Null if not canceled by an admin.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cancelled_by_admin_id")
  private User cancelledByAdmin;


  // The generated column slot_taken is intentionally not mapped. The database fills it.

  /**
   * A slot is always 1 hour, so endTime is derived from startTime.
   */
  public static Booking create(Student student, Room room, LocalDate date, LocalTime startTime) {
    return Booking.builder()
        .student(student)
        .room(room)
        .bookingDate(date)
        .startTime(startTime)
        .endTime(startTime.plusHours(1))
        .build();
  }

  public LocalDateTime startDateTime() {
    return LocalDateTime.of(bookingDate, startTime);
  }

  public LocalDateTime endDateTime() {
    return LocalDateTime.of(bookingDate, endTime);
  }

  public void checkIn(LocalDateTime now) {
    this.checkedInAt = now;
    this.status = BookingStatus.ATTENDED;
  }

  public void cancelByUser(LocalDateTime now) {
    this.status = BookingStatus.CANCELLED_BY_USER;
    this.cancelledAt = now;
  }

  public void cancelBySystem(LocalDateTime now, int toleranceMinutes) {
    this.status = BookingStatus.CANCELLED_BY_SYSTEM;
    this.cancelledAt = now;
    this.cancelReason = "No check-in within " + toleranceMinutes + " minutes";
  }

  public void cancelByAdmin(User admin, String reason, LocalDateTime now) {
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("A reason is required for an admin cancellation");
    }
    this.status = BookingStatus.CANCELLED_BY_ADMIN;
    this.cancelledAt = now;
    this.cancelReason = reason;
    this.cancelledByAdmin = admin;
  }
}
