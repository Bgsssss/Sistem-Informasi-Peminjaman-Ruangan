package com.zedd.rpl.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
public enum ErrorCode {

  PASSWORD_MISMATCH("ERR_PASSWORD_MISMATCH", "The password and confirmation do not match", BAD_REQUEST),
  CHANGE_PASSWORD_MISMATCH("ERR_PASSWORD_MISMATCH", "New password and confirmation do not match", BAD_REQUEST),
  ERR_USER_DISABLED("ERR_USER_DISABLED",
      "User account is disabled, please activate your account or contact the administrator",
      HttpStatus.UNAUTHORIZED),
  INVALID_CURRENT_PASSWORD("INVALID_CURRENT_PASSWORD", "The current password is incorrect", BAD_REQUEST),
  USER_NOT_FOUND("USER_NOT_FOUND", "User not found", NOT_FOUND),
  ACCOUNT_ALREADY_DEACTIVATED("ACCOUNT_ALREADY_DEACTIVATED", "Account has been deactivated", BAD_REQUEST),
  ACCOUNT_ALREADY_ACTIVE("ACCOUNT_ALREADY_ACTIVE", "Account has been active", BAD_REQUEST),
  BAD_CREDENTIALS("BAD_CREDENTIALS", "Username and / or password is incorrect", HttpStatus.UNAUTHORIZED),
  INTERNAL_EXCEPTION("INTERNAL_EXCEPTION",
      "An internal exception occurred, please try again or contact the admin",
      HttpStatus.INTERNAL_SERVER_ERROR),
  UNAUTHORIZED("UNAUTHORIZED", "Authentication is required", HttpStatus.UNAUTHORIZED),
  USERNAME_NOT_FOUND("USERNAME_NOT_FOUND", "Cannot find user with the provided username", NOT_FOUND),

  // General
  VALIDATION_ERROR("VALIDATION_ERROR", "Validation failed", HttpStatus.BAD_REQUEST),
  DATA_CONFLICT("DATA_CONFLICT", "The data conflicts with existing data", HttpStatus.CONFLICT),

  // Not found
  STUDENT_NOT_FOUND("STUDENT_NOT_FOUND", "Student not found", HttpStatus.NOT_FOUND),
  ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND", "Account not found", HttpStatus.NOT_FOUND),
  ROOM_NOT_FOUND("ROOM_NOT_FOUND", "Room not found", HttpStatus.NOT_FOUND),
  BOOKING_NOT_FOUND("BOOKING_NOT_FOUND", "Booking not found", HttpStatus.NOT_FOUND),

  // Booking rules
  ACCOUNT_INACTIVE("ACCOUNT_INACTIVE", "Your account is inactive", HttpStatus.FORBIDDEN),
  TRUST_SCORE_TOO_LOW("TRUST_SCORE_TOO_LOW", "Your trust score is below the minimum required to book", HttpStatus.FORBIDDEN),
  DATE_IN_PAST("DATE_IN_PAST", "The date is in the past", HttpStatus.BAD_REQUEST),
  DATE_NOT_OPEN("DATE_NOT_OPEN", "Booking for this date is not open yet", HttpStatus.UNPROCESSABLE_ENTITY),
  INVALID_SLOT("INVALID_SLOT", "Invalid time slot", HttpStatus.BAD_REQUEST),
  SLOT_ALREADY_STARTED("SLOT_ALREADY_STARTED", "This slot has already started", HttpStatus.UNPROCESSABLE_ENTITY),
  SLOT_ALREADY_BOOKED("SLOT_ALREADY_BOOKED", "The slot has already been booked by someone else. Please choose another slot or room.", HttpStatus.CONFLICT),
  ROOM_INACTIVE("ROOM_INACTIVE", "The room is currently inactive", HttpStatus.UNPROCESSABLE_ENTITY),
  ACTIVE_BOOKING_LIMIT_REACHED("ACTIVE_BOOKING_LIMIT_REACHED", "Active booking limit reached", HttpStatus.UNPROCESSABLE_ENTITY),
  DAILY_HOURS_LIMIT_EXCEEDED("DAILY_HOURS_LIMIT_EXCEEDED", "Daily booking duration limit exceeded", HttpStatus.UNPROCESSABLE_ENTITY),

  // Booking actions
  INVALID_BOOKING_STATUS("INVALID_BOOKING_STATUS", "The booking status does not allow this action", HttpStatus.UNPROCESSABLE_ENTITY),
  CANCELLATION_AFTER_START("CANCELLATION_AFTER_START", "The booking has already started and can no longer be cancelled", HttpStatus.UNPROCESSABLE_ENTITY),
  CHECK_IN_TOO_EARLY("CHECK_IN_TOO_EARLY", "Check-in is not open yet", HttpStatus.UNPROCESSABLE_ENTITY),
  CHECK_IN_WINDOW_CLOSED("CHECK_IN_WINDOW_CLOSED", "The check-in window has closed. The booking will be cancelled automatically.", HttpStatus.UNPROCESSABLE_ENTITY),

  // Accounts and students
  STUDENT_NUMBER_NOT_REGISTERED("STUDENT_NUMBER_NOT_REGISTERED", "Student number is not registered", HttpStatus.BAD_REQUEST),
  STUDENT_NUMBER_ALREADY_USED("STUDENT_NUMBER_ALREADY_USED", "Student number is already registered", HttpStatus.CONFLICT),
  ACCOUNT_ALREADY_EXISTS("ACCOUNT_ALREADY_EXISTS", "This student number already has an account", HttpStatus.CONFLICT),
  EMAIL_ALREADY_USED("EMAIL_ALREADY_USED", "Email is already in use", HttpStatus.CONFLICT),
  PASSWORD_REQUIRED("PASSWORD_REQUIRED", "Password is required", HttpStatus.BAD_REQUEST),
  STUDENT_HAS_BOOKINGS("STUDENT_HAS_BOOKINGS", "The student has booking history. Deactivate the account instead of deleting it.", HttpStatus.CONFLICT),

  // Rooms
  ROOM_NAME_ALREADY_USED("ROOM_NAME_ALREADY_USED", "Room name is already in use", HttpStatus.CONFLICT),
  INVALID_CAPACITY("INVALID_CAPACITY", "Capacity is outside the allowed range for the room type", HttpStatus.BAD_REQUEST),
  ROOM_HAS_BOOKINGS("ROOM_HAS_BOOKINGS", "The room has booking history. Deactivate it so it no longer appears in searches.", HttpStatus.CONFLICT),

  // Trust score and reports
  SCORE_UNCHANGED("SCORE_UNCHANGED", "The score already has this value", HttpStatus.BAD_REQUEST),
  INVALID_PERIOD("INVALID_PERIOD", "Period must be one of: daily, weekly, monthly", HttpStatus.BAD_REQUEST);
  ;

  private final String code;
  private final String defaultMessage;
  private final HttpStatus status;

  ErrorCode(final String code,
            final String defaultMessage,
            final HttpStatus status) {
    this.code = code;
    this.defaultMessage = defaultMessage;
    this.status = status;
  }
}
