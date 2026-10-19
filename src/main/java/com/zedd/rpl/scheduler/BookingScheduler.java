package com.zedd.rpl.scheduler;

import com.zedd.rpl.model.availability.impl.BookableDateService;
import com.zedd.rpl.model.booking.impl.BookingAutomationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingScheduler {

  private final BookableDateService bookableDateService;
  private final BookingAutomationService bookingAutomationService;

  /** Opens tomorrow's date for booking every day at 20:00 (FR-9). */
  @Scheduled(cron = "${rpl.schedule.open-date-cron:0 0 20 * * *}",
      zone = "${rpl.timezone:Asia/Jakarta}")
  public void openTomorrow() {
    bookableDateService.openTomorrow();
  }

  /** Auto-cancels bookings with a late check-in. Every 5 minutes (PRD range: 5-10 minutes). */
  @Scheduled(fixedDelayString = "${rpl.schedule.auto-cancel-ms:300000}", initialDelay = 60000)
  public void autoCancel() {
    bookingAutomationService.cancelLateBookings();
  }

  /** Marks bookings whose time has passed as COMPLETED and adds score. */
  @Scheduled(fixedDelayString = "${rpl.schedule.complete-ms:300000}", initialDelay = 90000)
  public void completeBookings() {
    bookingAutomationService.completeFinishedBookings();
  }
}
