package com.zedd.rpl.model.availability;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookable_dates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookableDate {

  @Id
  private LocalDate date;

  @Column(name = "is_open", nullable = false)
  private boolean isOpen;

  @Column(name = "opened_at")
  private LocalDateTime openedAt;

  public BookableDate(LocalDate date) {
    this.date = date;
  }

  public void open() {
    this.isOpen = true;
    this.openedAt = LocalDateTime.now();
  }
}
