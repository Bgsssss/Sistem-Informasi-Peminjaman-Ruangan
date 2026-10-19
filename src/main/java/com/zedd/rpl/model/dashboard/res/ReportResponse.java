package com.zedd.rpl.model.dashboard.res;

import java.time.LocalDate;
import java.util.List;

public record ReportResponse(String period, LocalDate from, LocalDate to, List<Row> rows) {

  /** occupancyPercent = used slots (not canceled) divided by total room slots in the period. */
  public record Row(
      Long roomId,
      String room,
      String type,
      long totalBookings,
      long totalAttended,
      long totalNoShows,
      long totalOtherCancellations,
      double occupancyPercent
  ) {}
}
