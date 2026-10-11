package com.zedd.rpl.model.booking;

import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.enums.RoomType;
import com.zedd.rpl.model.booking.internal.CandidateBooking;
import com.zedd.rpl.model.booking.internal.SlotUsage;
import com.zedd.rpl.model.booking.res.PopularRoomRow;
import com.zedd.rpl.model.booking.res.UsageReportRow;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

  // ---------- student ----------
  @Query("""
      select b from Booking b join fetch b.room
      where b.student.id = :studentId
        and (:status is null or b.status = :status)
      order by b.bookingDate desc, b.startTime desc
      """)
  List<Booking> findHistory(@Param("studentId") Long studentId,
                            @Param("status") BookingStatus status);

  /**
   * Ongoing bookings that have not ended yet (limit of 2 active bookings, FR-18).
   */
  @Query("""
      select count(b) from Booking b
      where b.student.id = :studentId and b.status in :statuses
        and (b.bookingDate > :today or (b.bookingDate = :today and b.endTime > :now))
      """)
  long countOngoing(@Param("studentId") Long studentId,
                    @Param("statuses") Collection<BookingStatus> statuses,
                    @Param("today") LocalDate today,
                    @Param("now") LocalTime now);

  /**
   * One booking = one hour, so the row count equals the number of hours (3-hour limit, FR-19).
   */
  long countByStudentIdAndBookingDateAndStatusIn(Long studentId, LocalDate bookingDate,
                                                 Collection<BookingStatus> statuses);

  boolean existsByRoomIdAndBookingDateAndStartTimeAndStatusIn(Long roomId, LocalDate bookingDate,
                                                              LocalTime startTime,
                                                              Collection<BookingStatus> statuses);

  boolean existsByStudentId(Long studentId);

  boolean existsByRoomId(Long roomId);

  /**
   * Used for status changes. Call it after locking the student.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Booking b where b.id = :id")
  Optional<Booking> findByIdForUpdate(@Param("id") Long id);

  // ---------- availability ----------

  @Query("""
      select new com.zedd.rpl.model.booking.internal.SlotUsage(b.room.id, b.startTime)
      from Booking b
      where b.bookingDate = :date and b.status in :statuses
        and (:type is null or b.room.type = :type)
      """)
  List<SlotUsage> findSlotUsage(@Param("date") LocalDate date,
                                @Param("type") RoomType type,
                                @Param("statuses") Collection<BookingStatus> statuses);

  // ---------- scheduled jobs ----------


  @Query("""
      select new com.zedd.rpl.model.booking.internal.CandidateBooking(
             b.id, b.student.id, b.bookingDate, b.startTime, b.endTime)
      from Booking b
      where b.status = :status and b.bookingDate <= :untilDate
      """)
  List<CandidateBooking> findCandidates(@Param("status") BookingStatus status,
                                        @Param("untilDate") LocalDate untilDate);
  // ---------- admin ----------

  @EntityGraph(attributePaths = {"student", "room", "cancelledByAdmin"})
  @Query("""
      select b from Booking b
      where (:date is null or b.bookingDate = :date)
        and (:roomId is null or b.room.id = :roomId)
        and (:status is null or b.status = :status)
      """)
  Page<Booking> searchForAdmin(@Param("date") LocalDate date,
                               @Param("roomId") Long roomId,
                               @Param("status") BookingStatus status,
                               Pageable pageable);

  /**
   * Upcoming active bookings in one room (warning when the room is deactivated).
   */
  long countByRoomIdAndStatusAndBookingDateGreaterThanEqual(Long roomId, BookingStatus status,
                                                            LocalDate bookingDate);

  long countByBookingDateAndStatusIn(LocalDate bookingDate, Collection<BookingStatus> statuses);

  @Query("""
      select b.room.name as name, count(b) as bookingCount
      from Booking b
      where b.bookingDate between :from and :to and b.status in :statuses
      group by b.room.id, b.room.name
      order by count(b) desc
      """)
  List<PopularRoomRow> findTopRooms(@Param("from") LocalDate from,
                                    @Param("to") LocalDate to,
                                    @Param("statuses") Collection<BookingStatus> statuses,
                                    Pageable pageable);

  /**
   * Usage report per room (FR-35). Rooms without bookings still appear with zeros.
   */
  @Query(value = """
      SELECT r.id AS roomId, r.name AS room, r.type AS type,
             COUNT(b.id) AS totalBookings,
             CAST(COALESCE(SUM(b.status IN ('ATTENDED','COMPLETED')), 0) AS SIGNED)                    AS totalAttended,
             CAST(COALESCE(SUM(b.status = 'CANCELLED_BY_SYSTEM'), 0) AS SIGNED)                          AS totalNoShows,
             CAST(COALESCE(SUM(b.status IN ('CANCELLED_BY_USER','CANCELLED_BY_ADMIN')), 0) AS SIGNED)    AS totalOtherCancellations
      FROM room r
      LEFT JOIN booking b ON b.room_id = r.id AND b.booking_date BETWEEN :from AND :to
      GROUP BY r.id, r.name, r.type
      ORDER BY totalBookings DESC, r.name ASC
      """, nativeQuery = true)
  List<UsageReportRow> findUsageReport(@Param("from") LocalDate from, @Param("to") LocalDate to);
}

