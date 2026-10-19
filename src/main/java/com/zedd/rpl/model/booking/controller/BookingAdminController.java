package com.zedd.rpl.model.booking.controller;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.common.PageResponse;
import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.model.booking.impl.BookingService;
import com.zedd.rpl.model.booking.req.AdminCancelRequest;
import com.zedd.rpl.model.booking.res.AdminBookingResponse;
import com.zedd.rpl.model.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class BookingAdminController {

  private final BookingService bookingService;

  /**
   * All bookings with optional filters, newest first.
   * Example: GET /api/admin/bookings?room=3&status=ACTIVE&page=0&size=20
   * (the link returned when a room is deactivated points here).
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<PageResponse<AdminBookingResponse>> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(name = "room", required = false) Long roomId,
      @RequestParam(required = false) BookingStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(bookingService.listForAdmin(date, roomId, status, page, size));
  }

  /** Reason is required, the action is written to the audit log, and the student's score is not changed. */
  @PatchMapping(
      path = "/{id}/cancel",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<AdminBookingResponse> cancel(@AuthenticationPrincipal User admin,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody AdminCancelRequest request) {
    return ApiResponse.ok("Booking cancelled by admin",
        bookingService.cancelByAdmin(admin.getId(), id, request));
  }
}
