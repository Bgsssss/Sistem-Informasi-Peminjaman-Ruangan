package com.zedd.rpl.model.student;

import com.zedd.rpl.model.trustcore.res.ViolationLogRow;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

  Optional<Student> findByStudentNumber(String studentNumber);

  boolean existsByStudentNumber(String studentNumber);

  boolean existsByStudentNumberAndIdNot(String studentNumber, Long id);

  /**
   * Locks the student row. Every operation that changes the score or checks booking limits
   * starts its transaction with this call, so two requests from the same student run one after
   * the other. Lock order is always student first, then booking.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Student s where s.id = :id")
  Optional<Student> findByIdForUpdate(@Param("id") Long id);

  /** Violation log, ordered from the lowest score. */
  @Query(value = """
        SELECT s.id AS studentId, s.npm AS studentNumber, s.name AS name,
               s.faculty AS faculty, s.trust_score AS trustScore,
               CAST(COALESCE(SUM(h.type = 'NO_SHOW'), 0) AS SIGNED)           AS noShowCount,
               CAST(COALESCE(SUM(h.type = 'LATE_CANCELLATION'), 0) AS SIGNED) AS lateCancellationCount,
               MAX(CASE WHEN h.delta < 0 THEN h.created_at END)               AS lastViolationAt
        FROM students s
        LEFT JOIN trust_score_history h ON h.student_id = s.id
        GROUP BY s.id, s.npm, s.name, s.faculty, s.trust_score
        ORDER BY s.trust_score ASC, s.name ASC
        """, nativeQuery = true)
  List<ViolationLogRow> findViolationLog();
}

