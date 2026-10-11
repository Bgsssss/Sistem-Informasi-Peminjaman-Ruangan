package com.zedd.rpl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** System parameters, configured in application.properties under the "rpl" prefix. */
@ConfigurationProperties(prefix = "rpl")
@Getter
@Setter
public class RplProperties {

  private LocalTime openingTime = LocalTime.of(8, 0);
  private LocalTime closingTime = LocalTime.of(21, 0);
  private int checkInToleranceMinutes = 10;
  private int maxActiveBookings = 2;
  private int maxHoursPerDay = 3;
  /** Cancelling at least this many hours before the schedule is "normal" (no score penalty). */
  private int freeCancellationHours = 2;
  private String timezone = "Asia/Jakarta";
  private Score score = new Score();
  private Bootstrap bootstrap = new Bootstrap();

  @Getter
  @Setter
  public static class Score {
    private int minToBook = 60;
    private int noShow = 15;
    private int lateCancellation = 5;
    private int completedOnTime = 5;
  }

  @Getter
  @Setter
  public static class Bootstrap {
    private String adminUsername = "admin@gmail.com";
    /** No default on purpose. The admin account is only created when a password is provided. */
    private String adminPassword;
  }

  public int slotsPerDay() {
    return (int) Duration.between(openingTime, closingTime).toHours();
  }

  /** Start time of every slot, for example 08:00, 09:00, ..., 20:00. */
  public List<LocalTime> slotStartTimes() {
    List<LocalTime> slots = new ArrayList<>();
    for (LocalTime t = openingTime; t.isBefore(closingTime); t = t.plusHours(1)) {
      slots.add(t);
    }
    return slots;
  }
}
