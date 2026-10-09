package com.zedd.rpl.model.trustcore;


import com.zedd.rpl.model.trustcore.res.ScoreHistoryResponse;
import com.zedd.rpl.model.trustcore.res.ViolationLogResponse;
import com.zedd.rpl.model.trustcore.res.ViolationLogRow;
import org.springframework.stereotype.Component;

@Component
public class TrustScoreMapper {

  public ScoreHistoryResponse toHistory(TrustScoreHistory trustScoreHistory) {
    return new ScoreHistoryResponse(
        trustScoreHistory.getCreatedAt(),
        trustScoreHistory.getType(),
        trustScoreHistory.getDelta(),
        trustScoreHistory.getScoreAfter(),
        trustScoreHistory.getNote());
  }

  public ViolationLogResponse toViolationLog(ViolationLogRow vlg) {
    return new ViolationLogResponse(
        vlg.getStudentId(),
        vlg.getStudentNumber(),
        vlg.getName(),
        vlg.getFaculty(),
        vlg.getTrustScore(),
        vlg.getNoShowCount() == null ? 0 : vlg.getNoShowCount(),
        vlg.getLateCancellationCount() == null ? 0 : vlg.getLateCancellationCount(),
        vlg.getLastViolationAt());
  }
}

