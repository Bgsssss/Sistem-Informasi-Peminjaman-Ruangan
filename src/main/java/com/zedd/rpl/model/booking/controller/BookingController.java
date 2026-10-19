package com.zedd.rpl.model.booking.controller;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.enums.BookingStatus;
import com.zedd.rpl.model.booking.impl.BookingService;
import com.zedd.rpl.model.booking.req.BookingRequest;
import com.zedd.rpl.model.booking.res.BookingResponse;
import com.zedd.rpl.model.booking.res.CancelResponse;
import com.zedd.rpl.model.booking.res.CheckInResponse;
import com.zedd.rpl.model.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@PreAuthorize("hasRole('ROLE_STUDENT')")
@RequiredArgsConstructor
public class BookingController {

  private final BookingService bookingService;

  @PostMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<BookingResponse> create(@AuthenticationPrincipal User user,
                                             @Valid @RequestBody BookingRequest request) {
    return ApiResponse.ok("Booking created",
        bookingService.create(user.getStudent().getId(), request));
  }

  /** The student's own history. Optional filter: ?status=ACTIVE|ATTENDED|COMPLETED|CANCELLED_BY_USER|... */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<List<BookingResponse>> getHistory(
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) BookingStatus status) {
    return ApiResponse.ok(bookingService.getHistory(user.getStudent().getId(), status));
  }

  /**
   * No request body. Use "scorePenaltyIfCancelled" from the booking response to show a
   * confirmation dialog before calling this.
   */
  @PatchMapping(
      path = "/{id}/cancel",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<CancelResponse> cancel(@AuthenticationPrincipal User user,
                                            @PathVariable Long id) {
    CancelResponse result = bookingService.cancel(user.getStudent().getId(), id);
    return ApiResponse.ok(result.message(), result);
  }

  @PostMapping(
      path = "/{id}/check-in",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<CheckInResponse> checkIn(@AuthenticationPrincipal User user,
                                              @PathVariable Long id) {
    return ApiResponse.ok("Checked in",
        bookingService.checkIn(user.getStudent().getId(), id));
  }
}
