package com.zedd.rpl.model.availability;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookableDateRepository extends JpaRepository<BookableDate, LocalDate> {
}
