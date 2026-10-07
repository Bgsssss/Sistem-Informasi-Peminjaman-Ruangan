package com.zedd.rpl.model.trustcore;

import com.zedd.rpl.enums.ScoreChangeType;
import com.zedd.rpl.model.booking.Booking;
import com.zedd.rpl.model.student.Student;
import com.zedd.rpl.model.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "trust_score_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrustScoreHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id")
  private Booking booking;

  /** Terisi bila jenis = PENYESUAIAN_ADMIN. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "admin_id")
  private User admin;

  /** Delta bertanda, misalnya -15, -5, +5. */
  @JdbcTypeCode(SqlTypes.SMALLINT)
  @Column(nullable = false)
  private Integer delta;

  @JdbcTypeCode(SqlTypes.TINYINT)
  @Column(name = "score_after", nullable = false)
  private Integer scoreAfter;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ScoreChangeType type;

  @Column
  private String note;

  @Column(name = "created_at",insertable = false, updatable = false)
  private LocalDateTime createdAt;

  public TrustScoreHistory(Student student, Booking booking, User admin,
                           int delta, int scoreAfter,
                           ScoreChangeType type, String note) {
    if (type == ScoreChangeType.ADMIN_ADJUSTMENT && (note == null || note.isBlank())) {
      throw new IllegalArgumentException("Note is required for admin score adjustments");
    }
    this.student = student;
    this.booking = booking;
    this.admin = admin;
    this.delta = delta;
    this.scoreAfter = scoreAfter;
    this.type = type;
    this.note = note;
  }
}
