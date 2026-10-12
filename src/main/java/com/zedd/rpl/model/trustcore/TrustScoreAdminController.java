package com.zedd.rpl.model.trustcore;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.model.trustcore.impl.TrustScoreService;
import com.zedd.rpl.model.trustcore.req.ScoreAdjustmentRequest;
import com.zedd.rpl.model.trustcore.res.ScoreAdjustmentResponse;
import com.zedd.rpl.model.trustcore.res.ViolationLogResponse;
import com.zedd.rpl.model.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class TrustScoreAdminController {

  private final TrustScoreService trustScoreService;

  /** Violation log. ?sort=asc (lowest score first, default) or ?sort=desc. */
  @GetMapping(
      path = "/violations",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<List<ViolationLogResponse>> violationLog(
      @RequestParam(defaultValue = "asc") String sort
  ) {
    return ApiResponse.ok(trustScoreService.violationLog(sort));
  }

  /** Manual score adjustment. newScore is the final value (0-100), and a note is required. */
  @PatchMapping(
      path = "/trust-scores/{studentId}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<ScoreAdjustmentResponse> adjustScore(
      @AuthenticationPrincipal User admin,
      @PathVariable Long studentId,
      @Valid @RequestBody ScoreAdjustmentRequest request
  ) {
    return ApiResponse.ok("Trust score adjusted",
        trustScoreService.adjustManually(admin.getId(), studentId, request));
  }
}
