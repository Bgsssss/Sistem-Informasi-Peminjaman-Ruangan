package com.zedd.rpl.model.availability.impl;

import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.enums.RoomType;
import com.zedd.rpl.enums.SlotStatus;
import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.exception.ErrorCode;
import com.zedd.rpl.model.availability.res.AvailabilityResponse;
import com.zedd.rpl.model.booking.BookingRepository;
import com.zedd.rpl.model.booking.internal.SlotUsage;
import com.zedd.rpl.model.room.Room;
import com.zedd.rpl.model.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

  private final RoomRepository roomRepository;
  private final BookingRepository bookingRepository;
  private final BookableDateService bookableDateService;
  private final RplProperties props;
  private final Clock clock;

  /** Open to every student, including those below the minimum score (only booking itself is blocked). */
  @Transactional(readOnly = true)
  public AvailabilityResponse check(LocalDate date, RoomType type) {
    LocalDateTime now = LocalDateTime.now(clock);
    if (date.isBefore(now.toLocalDate())) {
      throw new BusinessException(ErrorCode.DATE_IN_PAST);
    }
    if (!bookableDateService.isOpen(date)) {
      throw new BusinessException(ErrorCode.DATE_NOT_OPEN,
          "Date " + date + " is not open yet. Booking for tomorrow opens at 20:00.");
    }

    List<Room> rooms = (type == null)
        ? roomRepository.findByActiveTrueOrderByName()
        : roomRepository.findByTypeAndActiveTrueOrderByName(type);

    Map<Long, Set<LocalTime>> usedSlots = new HashMap<>();
    for (SlotUsage usage : bookingRepository.findSlotUsage(date, type, BookingStatus.OCCUPYING_SLOT)) {
      usedSlots.computeIfAbsent(usage.roomId(), k -> new HashSet<>()).add(usage.startTime());
    }

    List<LocalTime> allSlots = props.slotStartTimes();
    Map<LocalTime, Long> availablePerSlot = new HashMap<>();
    List<AvailabilityResponse.RoomAvailability> roomResults = new ArrayList<>();

    for (Room room : rooms) {
      Set<LocalTime> booked = usedSlots.getOrDefault(room.getId(), Set.of());
      List<AvailabilityResponse.SlotInfo> slots = new ArrayList<>();
      for (LocalTime start : allSlots) {
        SlotStatus status;
        if (LocalDateTime.of(date, start).isBefore(now)) {
          status = SlotStatus.PAST;
        } else if (booked.contains(start)) {
          status = SlotStatus.BOOKED;
        } else {
          status = SlotStatus.AVAILABLE;
          availablePerSlot.merge(start, 1L, Long::sum);
        }
        slots.add(new AvailabilityResponse.SlotInfo(start, start.plusHours(1), status));
      }
      roomResults.add(new AvailabilityResponse.RoomAvailability(
          room.getId(), room.getName(), room.getLocation(), room.getCapacity(), slots));
    }

    List<AvailabilityResponse.SlotSummary> summary = allSlots.stream()
        .map(s -> new AvailabilityResponse.SlotSummary(s, s.plusHours(1), availablePerSlot.getOrDefault(s, 0L)))
        .toList();

    return new AvailabilityResponse(date, type, summary, roomResults);
  }
}
