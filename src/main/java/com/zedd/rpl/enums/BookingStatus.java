package com.zedd.rpl.enums;

import java.util.List;
public enum BookingStatus {
  ACTIVE,
  ATTENDED,
  COMPLETED,
  CANCELLED_BY_USER,
  CANCELLED_BY_SYSTEM,
  CANCELLED_BY_ADMIN;

  public static final List<BookingStatus> OCCUPYING_SLOT = List.of(ACTIVE, ATTENDED, COMPLETED);

  public static final List<BookingStatus> ONGOING = List.of(ACTIVE, ATTENDED);

  public boolean isCancelled() {
    return this == CANCELLED_BY_USER
        || this == CANCELLED_BY_SYSTEM
        || this == CANCELLED_BY_ADMIN;
  }
}
