package com.zedd.rpl.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {

  PASSWORD_MISMATCH(BAD_REQUEST, "The password and confirmation do not match"),
  CHANGE_PASSWORD_MISMATCH(BAD_REQUEST, "New password and confirmation do not match"),
  ERR_USER_DISABLED(HttpStatus.UNAUTHORIZED, "User account is disabled, please activate your account or contact the administrator"),
  INVALID_CURRENT_PASSWORD(BAD_REQUEST, "The current password is incorrect"),
  USER_NOT_FOUND(NOT_FOUND, "User not found"),
  ACCOUNT_ALREADY_DEACTIVATED(BAD_REQUEST, "Account has been deactivated"),
  ACCOUNT_ALREADY_ACTIVE(BAD_REQUEST, "Account has been active"),
  BAD_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Username and / or password is incorrect"),
  INTERNAL_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "An internal exception occurred, please try again or contact the admin"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required"),
  USERNAME_NOT_FOUND(NOT_FOUND, "Cannot find user with the provided username"),

  // General
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
  DATA_CONFLICT(HttpStatus.CONFLICT, "The data conflicts with existing data"),

  // Not found
  STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Student not found"),
  ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "Account not found"),
  ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Room not found"),
  BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "Booking not found"),

  // Booking rules
  ACCOUNT_INACTIVE(HttpStatus.FORBIDDEN, "This account is inactive"),
  TRUST_SCORE_TOO_LOW(HttpStatus.FORBIDDEN,
      "Your trust score is %s, below the minimum of %s. Contact the facility admin to have your score restored."),
  DATE_IN_PAST(HttpStatus.BAD_REQUEST, "The date is in the past"),
  DATE_NOT_OPEN(HttpStatus.UNPROCESSABLE_CONTENT,
      "Date %s is not open yet. Booking for tomorrow opens at 20:00."),
  INVALID_SLOT(HttpStatus.BAD_REQUEST, "Slots are whole hours between %s and %s"),
  SLOT_ALREADY_STARTED(HttpStatus.UNPROCESSABLE_CONTENT, "The %s slot has already started"),
  SLOT_ALREADY_BOOKED(HttpStatus.CONFLICT,
      "The slot has already been booked by someone else. Please choose another slot or room."),
  ROOM_INACTIVE(HttpStatus.UNPROCESSABLE_CONTENT, "The room is currently inactive"),
  ACTIVE_BOOKING_LIMIT_REACHED(HttpStatus.UNPROCESSABLE_CONTENT,
      "Active booking limit reached (%s). Finish or cancel an existing booking first."),
  DAILY_HOURS_LIMIT_EXCEEDED(HttpStatus.UNPROCESSABLE_CONTENT,
      "Total booking duration on %s cannot exceed %s hours"),

  // Booking actions
  INVALID_BOOKING_STATUS(HttpStatus.UNPROCESSABLE_CONTENT, "A booking with status %s cannot be %s"),
  CANCELLATION_AFTER_START(HttpStatus.UNPROCESSABLE_CONTENT,
      "The schedule has already started. A booking without check-in is cancelled automatically."),
  CHECK_IN_TOO_EARLY(HttpStatus.UNPROCESSABLE_CONTENT, "Check-in opens at %s"),
  CHECK_IN_WINDOW_CLOSED(HttpStatus.UNPROCESSABLE_CONTENT,
      "The check-in window has closed. The booking will be cancelled automatically."),

  // Accounts and students
  STUDENT_NUMBER_NOT_REGISTERED(HttpStatus.BAD_REQUEST, "Student number is not registered"),
  STUDENT_NUMBER_ALREADY_USED(HttpStatus.CONFLICT, "Student number is already registered"),
  ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "This student number already has an account"),
  EMAIL_ALREADY_USED(HttpStatus.CONFLICT, "Email is already in use"),
  PASSWORD_REQUIRED(HttpStatus.BAD_REQUEST, "Password is required"),
  STUDENT_HAS_BOOKINGS(HttpStatus.CONFLICT,
      "The student has booking history. Deactivate the account instead of deleting it."),

  // Rooms
  ROOM_NAME_ALREADY_USED(HttpStatus.CONFLICT, "Room name is already in use"),
  INVALID_CAPACITY(HttpStatus.BAD_REQUEST, "Capacity for type %s must be %s-%s people"),
  ROOM_HAS_BOOKINGS(HttpStatus.CONFLICT,
      "The room has booking history. Deactivate it so it no longer appears in searches."),

  // Trust score and reports
  SCORE_UNCHANGED(HttpStatus.BAD_REQUEST, "The score is already %s"),
  INVALID_PERIOD(HttpStatus.BAD_REQUEST, "Period must be one of: daily, weekly, monthly");

  private final HttpStatus status;
  private final String defaultMessage;

  ErrorCode(HttpStatus status, String defaultMessage) {
    this.status = status;
    this.defaultMessage = defaultMessage;
  }

}
