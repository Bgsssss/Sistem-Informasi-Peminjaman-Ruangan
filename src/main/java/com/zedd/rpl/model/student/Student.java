package com.zedd.rpl.model.student;

import com.zedd.rpl.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(name = "students")
public class Student extends BaseEntity {

  public static final int INITIAL_SCORE = 100;
  public static final int MAX_SCORE = 100;
  //public static final int BOOKING_SCORE_THRESHOLD = 60;

  @Column(name = "npm",nullable = false, unique = true, length = 20)
  private String studentNumber;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 100)
  private String faculty;

  @Builder.Default
  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  // Tanpa setter: skor hanya berubah lewat ubahSkor() supaya selalu dijepit 0..100.
  @Builder.Default
  @Setter(AccessLevel.NONE)
  @JdbcTypeCode(SqlTypes.TINYINT)
  @Column(name = "trust_score", nullable = false)
  private Integer trustScore = INITIAL_SCORE;


  public int updateScore(int delta) {
    this.trustScore = Math.max(0, Math.min(MAX_SCORE, this.trustScore + delta));
    return this.trustScore;
  }

  /** Scores of 60 and above are allowed to book (FR-24). */
  /*public boolean canBook() {
    return isActive && trustScore >= BOOKING_SCORE_THRESHOLD;
  }*/

}
