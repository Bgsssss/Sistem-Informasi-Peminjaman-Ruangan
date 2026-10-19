package com.zedd.rpl.model.dashboard;

import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.model.booking.BookingRepository;
import com.zedd.rpl.model.booking.res.PopularRoomRow;
import com.zedd.rpl.model.dashboard.res.DashboardResponse;
import com.zedd.rpl.model.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

  private final BookingRepository bookingRepository;
  private final RoomRepository roomRepository;
  private final RplProperties props;
  private final Clock clock;

  @Transactional(readOnly = true)
  public DashboardResponse getSummary() {
    LocalDate today = LocalDate.now(clock);

    long totalToday = bookingRepository.countByBookingDateAndStatusIn(today, BookingStatus.OCCUPYING_SLOT);
    long activeRooms = roomRepository.countByActiveTrue();

    List<PopularRoomRow> top = bookingRepository.findTopRooms(
        today.minusDays(29), today, BookingStatus.OCCUPYING_SLOT, PageRequest.of(0, 1));
    DashboardResponse.TopRoom topRoom = top.isEmpty() ? null
        : new DashboardResponse.TopRoom(top.get(0).getName(), top.get(0).getBookingCount());

    double occupancy = percent(totalToday, activeRooms * props.slotsPerDay());
    return new DashboardResponse(today, totalToday, topRoom, activeRooms, occupancy);
  }

  static double percent(long part, long total) {
    if (total <= 0) {
      return 0;
    }
    double value = Math.min(100.0, part * 100.0 / total);
    return Math.round(value * 10) / 10.0;
  }
}
