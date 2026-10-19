package com.zedd.rpl.model.availability.impl;

import com.zedd.rpl.model.availability.BookableDate;
import com.zedd.rpl.model.availability.BookableDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookableDateService {

  private final BookableDateRepository repository;
  private final Clock clock;

  @Transactional(readOnly = true)
  public boolean isOpen(LocalDate date) {
    return repository.findById(date).map(BookableDate::isOpen).orElse(false);
  }

  /** Idempotent: safe to run again for the same date. */
  @Transactional
  public void openDate(LocalDate date) {
    BookableDate bookable = repository.findById(date).orElseGet(() -> new BookableDate(date));
    if (!bookable.isOpen()) {
      bookable.markOpen(LocalDateTime.now(clock));
      repository.save(bookable);
      log.info("Date {} opened for booking", date);
    }
  }

  /** Called by the daily job at 20:00 (FR-9). */
  public void openTomorrow() {
    openDate(LocalDate.now(clock).plusDays(1));
  }
}

