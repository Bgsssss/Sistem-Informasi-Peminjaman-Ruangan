package com.zedd.rpl.model.student;

import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.model.student.res.ProfileResponse;
import com.zedd.rpl.model.student.res.StudentResponse;
import com.zedd.rpl.model.trustcore.res.ScoreHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


import java.util.List;

@Component
@RequiredArgsConstructor
public class StudentMapper {

  private final RplProperties props;

  public StudentResponse toResponse(Student s, String email) {
    return new StudentResponse(
        s.getId(),
        s.getStudentNumber(),
        s.getName(),
        s.getFaculty(),
        email,
        s.isActive(),
        s.getTrustScore());
  }

  public ProfileResponse toProfile(Student s, String email, List<ScoreHistoryResponse> scoreHistoryResponses) {
    int min = props.getScore().getMinToBook();
    boolean canBook = s.isActive() && s.getTrustScore() >= min;
    String blockedMessage = null;
    if (s.getTrustScore() < min) {
      blockedMessage = "Your trust score is " + s.getTrustScore() + ", below the minimum of " + min
          + ". New bookings reopen after the facility admin adjusts your score.";
    }
    return new ProfileResponse(s.getStudentNumber(), s.getName(), s.getFaculty(), email,
        s.getTrustScore(), min, canBook, blockedMessage, scoreHistoryResponses);
  }
}

