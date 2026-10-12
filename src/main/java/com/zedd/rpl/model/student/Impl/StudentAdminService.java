package com.zedd.rpl.model.student.Impl;


import com.zedd.rpl.common.PageResponse;
import com.zedd.rpl.enums.Role;
import com.zedd.rpl.enums.ScoreChangeType;
import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.exception.ErrorCode;
import com.zedd.rpl.model.booking.BookingRepository;
import com.zedd.rpl.model.student.Student;
import com.zedd.rpl.model.student.StudentMapper;
import com.zedd.rpl.model.student.StudentRepository;
import com.zedd.rpl.model.student.req.ResetPasswordRequest;
import com.zedd.rpl.model.student.req.StudentRequest;
import com.zedd.rpl.model.student.res.StudentResponse;
import com.zedd.rpl.model.trustcore.TrustScoreHistory;
import com.zedd.rpl.model.trustcore.TrustScoreHistoryRepository;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Admin CRUD for dummy students and password reset (FR-29). */
@Service
@RequiredArgsConstructor
public class StudentAdminService {

  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final BookingRepository bookingRepository;
  private final TrustScoreHistoryRepository historyRepository;
  private final PasswordEncoder passwordEncoder;
  private final StudentMapper mapper;

  @Transactional(readOnly = true)
  public PageResponse<StudentResponse> list(int page, int size) {
    Page<Student> result = studentRepository.findAll(
        PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("studentNumber")));
    List<Long> ids = result.getContent().stream().map(Student::getId).toList();
    Map<Long, String> emailByStudent = userRepository.findByStudentIdIn(ids).stream()
        .collect(Collectors.toMap(u -> u.getStudent().getId(), User::getUsername));
    return PageResponse.of(result.map(s -> mapper.toResponse(s, emailByStudent.get(s.getId()))));
  }

  @Transactional(readOnly = true)
  public StudentResponse getById(Long id) {
    return mapper.toResponse(find(id), emailOf(id));
  }

  @Transactional
  public StudentResponse create(StudentRequest req) {
    String studentNumber = req.studentNumber().trim();
    String email = req.email().trim().toLowerCase();
    if (req.password() == null || req.password().isBlank()) {
      throw new BusinessException(ErrorCode.PASSWORD_REQUIRED);
    }
    if (studentRepository.existsByStudentNumber(studentNumber)) {
      throw new BusinessException(ErrorCode.STUDENT_NUMBER_ALREADY_USED);
    }
    if (userRepository.existsByUsername(email)) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_USED);
    }

    Student student = studentRepository.save(Student.builder()
        .studentNumber(studentNumber)
        .name(req.name().trim())
        .faculty(req.faculty().trim())
        .isActive(req.active() == null || req.active())
        .build());
    userRepository.save(User.builder()
        .username(email)
        .password(passwordEncoder.encode(req.password()))
        .role(Role.ROLE_STUDENT)
        .student(student)
        .build());
    historyRepository.save(new TrustScoreHistory(student, null, null,
        student.getTrustScore(), student.getTrustScore(),
        ScoreChangeType.INITIAL_SCORE, "Account created"));
    return mapper.toResponse(student, email);
  }

  @Transactional
  public StudentResponse update(Long id, StudentRequest req) {
    Student student = find(id);
    User user = userRepository.findByStudentId(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    String studentNumber = req.studentNumber().trim();
    String email = req.email().trim().toLowerCase();

    if (studentRepository.existsByStudentNumberAndIdNot(studentNumber, id)) {
      throw new BusinessException(ErrorCode.STUDENT_NUMBER_ALREADY_USED);
    }
    if (userRepository.existsByUsernameAndIdNot(email, user.getId())) {
      throw new BusinessException(ErrorCode.EMAIL_ALREADY_USED);
    }

    student.setStudentNumber(studentNumber);
    student.setName(req.name().trim());
    student.setFaculty(req.faculty().trim());
    if (req.active() != null) {
      student.setActive(req.active());
    }
    user.setUsername(email);
    if (req.password() != null && !req.password().isBlank()) {
      user.setPassword(passwordEncoder.encode(req.password()));
    }
    return mapper.toResponse(student, email);
  }

  /** Students with booking history are not deleted so reports stay intact. Deactivate via update(). */
  @Transactional
  public void delete(Long id) {
    Student student = find(id);
    if (bookingRepository.existsByStudentId(id)) {
      throw new BusinessException(ErrorCode.STUDENT_HAS_BOOKINGS);
    }
    historyRepository.deleteByStudentId(id);
    userRepository.deleteByStudentId(id);
    studentRepository.delete(student);
  }

  @Transactional
  public void resetPassword(Long id, ResetPasswordRequest req) {
    find(id);
    User user = userRepository.findByStudentId(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND));
    user.setPassword(passwordEncoder.encode(req.newPassword()));
  }
  private Student find(Long id) {
    return studentRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.STUDENT_NOT_FOUND));
  }

  private String emailOf(Long studentId) {
    return userRepository.findByStudentId(studentId).map(User::getUsername).orElse(null);
  }
}

