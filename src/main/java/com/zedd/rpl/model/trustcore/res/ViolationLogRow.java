package com.zedd.rpl.model.trustcore.res;

import java.time.LocalDateTime;

public interface ViolationLogRow {
  Long getStudentId();
  String getStudentNumber();
  String getName();
  String getFaculty();
  Integer getTrustScore();
  Long getNoShowCount();
  Long getLateCancellationCount();
  LocalDateTime getLastViolationAt();
}

