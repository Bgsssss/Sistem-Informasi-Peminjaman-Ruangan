package com.zedd.rpl.model.availability.res;

import com.zedd.rpl.enums.RoomType;
import com.zedd.rpl.enums.SlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AvailabilityResponse(
    LocalDate date,
    RoomType type,
    List<SlotSummary> summary,
    List<RoomAvailability> rooms
) {
  /** Number of available rooms per hour, shown under the slot grid. */
  public record SlotSummary(LocalTime startTime, LocalTime endTime, long availableRooms) {}

  public record RoomAvailability(Long roomId, String name, String location,
                                 Integer capacity, List<SlotInfo> slots) {}

  public record SlotInfo(LocalTime startTime, LocalTime endTime, SlotStatus status) {}
}

