package com.zedd.rpl.model.availability;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.enums.RoomType;
import com.zedd.rpl.model.availability.impl.AvailabilityService;
import com.zedd.rpl.model.availability.res.AvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class AvailabilityController {

  private final AvailabilityService availabilityService;

  /**
   * Open to every logged-in student, including those below the minimum trust score
   * (only the booking itself is blocked for them).
   * Example: GET /api/availability?date=2026-10-12&type=SMALL
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<AvailabilityResponse> check(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(required = false) RoomType type) {
    return ApiResponse.ok(availabilityService.check(date, type));
  }
}
