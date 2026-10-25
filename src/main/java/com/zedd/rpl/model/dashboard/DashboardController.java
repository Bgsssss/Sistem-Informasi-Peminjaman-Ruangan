package com.zedd.rpl.model.dashboard;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.model.dashboard.res.DashboardResponse;
import com.zedd.rpl.model.dashboard.res.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class DashboardController {

  private final DashboardService dashboardService;
  private final ReportService reportService;

  @GetMapping("/dashboard")
  public ApiResponse<DashboardResponse> getSummary() {
    return ApiResponse.ok(dashboardService.getSummary());
  }

  /** ?period=daily (today), weekly (last 7 days) or monthly (last 30 days). Shown as a table. */
  @GetMapping("/reports")
  public ApiResponse<ReportResponse> getUsageReport(@RequestParam(defaultValue = "daily") String period) {
    return ApiResponse.ok(reportService.getUsageReport(period));
  }
}
