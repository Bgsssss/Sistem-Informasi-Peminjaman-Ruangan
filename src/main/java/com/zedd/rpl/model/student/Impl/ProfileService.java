package com.zedd.rpl.model.student.Impl;


import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.exception.ErrorCode;
import com.zedd.rpl.model.student.Student;
import com.zedd.rpl.model.student.StudentMapper;
import com.zedd.rpl.model.student.StudentRepository;
import com.zedd.rpl.model.student.res.ProfileResponse;
import com.zedd.rpl.model.trustcore.TrustScoreHistoryRepository;
import com.zedd.rpl.model.trustcore.TrustScoreMapper;
import com.zedd.rpl.model.trustcore.res.ScoreHistoryResponse;
import com.zedd.rpl.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final TrustScoreHistoryRepository historyRepository;
  private final StudentMapper studentMapper;
  private final TrustScoreMapper trustScoreMapper;

  /** The score is visible only to the owner and admins, so the id always comes from the token, never a parameter. */
  @Transactional(readOnly = true)
  public ProfileResponse getProfile(Long studentId) {
    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
    String email = userRepository.findByStudentId(studentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND))
        .getUsername();
    List<ScoreHistoryResponse> history = historyRepository
        .findByStudentIdOrderByCreatedAtDescIdDesc(studentId).stream()
        .map(trustScoreMapper::toHistory)
        .toList();
    return studentMapper.toProfile(student, email, history);
  }
}

