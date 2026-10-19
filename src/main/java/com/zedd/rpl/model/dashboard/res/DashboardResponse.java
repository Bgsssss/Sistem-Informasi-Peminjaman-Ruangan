package com.zedd.rpl.model.dashboard.res;

import java.time.LocalDate;

public record DashboardResponse(
    LocalDate date,
    long totalBookingsToday,
    TopRoom topRoom,             // last 30 days, null when there are no bookings yet
    long activeRooms,
    double occupancyTodayPercent
) {
  public record TopRoom(String name, long bookingCount) {}
}
