package com.zedd.rpl.model.trustcore.impl;

import com.zedd.rpl.enums.ScoreChangeType;
import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.exception.ErrorCode;
import com.zedd.rpl.model.booking.Booking;
import com.zedd.rpl.model.student.Student;
import com.zedd.rpl.model.student.StudentRepository;
import com.zedd.rpl.model.trustcore.TrustScoreHistory;
import com.zedd.rpl.model.trustcore.TrustScoreHistoryRepository;
import com.zedd.rpl.model.trustcore.TrustScoreMapper;
import com.zedd.rpl.model.trustcore.req.ScoreAdjustmentRequest;
import com.zedd.rpl.model.trustcore.res.ScoreAdjustmentResponse;
import com.zedd.rpl.model.trustcore.res.ViolationLogResponse;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrustScoreService {

  private final StudentRepository studentRepository;
  private final TrustScoreHistoryRepository historyRepository;
  private final UserRepository userRepository;
  private final TrustScoreMapper mapper;

  /**
   * Changes the score and records the history. The student must already be locked
   * (findByIdForUpdate) in the same transaction. Returns the actual change, which can be
   * smaller than delta because the score is clamped to 0..100.
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public int apply(Student student, int delta, ScoreChangeType type, Booking booking, String note) {
    int before = student.getTrustScore();
    int after = student.changeScore(delta);
    int actual = after - before;
    // Violations are recorded even when the score is already 0, so violation counts stay accurate.
    if (actual != 0 || delta < 0) {
      historyRepository.save(new TrustScoreHistory(student, booking, null, actual, after, type, note));
    }
    return actual;
  }

  /** Manual adjustment by an admin (FR-26). */
  @Transactional
  public ScoreAdjustmentResponse adjustManually(Long adminId, Long studentId, ScoreAdjustmentRequest req) {
    Student student = studentRepository.findByIdForUpdate(studentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    int oldScore = student.getTrustScore();
    if (req.newScore() == oldScore) {
      throw new BusinessException(ErrorCode.SCORE_UNCHANGED, "The score is already " + oldScore);
    }
    int newScore = student.changeScore(req.newScore() - oldScore);
    User admin = userRepository.getReferenceById(adminId);
    historyRepository.save(new TrustScoreHistory(student, null, admin,
        newScore - oldScore, newScore, ScoreChangeType.ADMIN_ADJUSTMENT, req.note().trim()));
    return new ScoreAdjustmentResponse(student.getId(), oldScore, newScore, newScore - oldScore);
  }

  /** Violation log. sort = "asc" (lowest score first, default) or "desc". */
  @Transactional(readOnly = true)
  public List<ViolationLogResponse> violationLog(String sort) {
    List<ViolationLogResponse> result = new ArrayList<>(
        studentRepository.findViolationLog().stream().map(mapper::toViolationLog).toList());
    if ("desc".equalsIgnoreCase(sort)) {
      Collections.reverse(result);
    }
    return result;
  }
}

