package com.zedd.rpl.model.dashboard;

import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.exception.ErrorCode;
import com.zedd.rpl.model.booking.BookingRepository;
import com.zedd.rpl.model.dashboard.res.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final BookingRepository bookingRepository;
  private final RplProperties props;
  private final Clock clock;

  /** period: daily (today), weekly (last 7 days), monthly (last 30 days). */
  @Transactional(readOnly = true)
  public ReportResponse getUsageReport(String period) {
    String normalized = period == null ? "" : period.toLowerCase();
    LocalDate to = LocalDate.now(clock);
    LocalDate from = switch (normalized) {
      case "daily"   -> to;
      case "weekly"  -> to.minusDays(6);
      case "monthly" -> to.minusDays(29);
      default -> throw new BusinessException(ErrorCode.INVALID_PERIOD);
    };

    long slotCapacity = (ChronoUnit.DAYS.between(from, to) + 1) * props.slotsPerDay();

    List<ReportResponse.Row> rows = bookingRepository.findUsageReport(from, to).stream()
        .map(r -> {
          long used = r.getTotalBookings() - r.getTotalNoShows() - r.getTotalOtherCancellations();
          return new ReportResponse.Row(r.getRoomId(), r.getRoom(), r.getType(),
              r.getTotalBookings(), r.getTotalAttended(),
              r.getTotalNoShows(), r.getTotalOtherCancellations(),
              DashboardService.percent(used, slotCapacity));
        })
        .toList();
    return new ReportResponse(normalized, from, to, rows);
  }
}

