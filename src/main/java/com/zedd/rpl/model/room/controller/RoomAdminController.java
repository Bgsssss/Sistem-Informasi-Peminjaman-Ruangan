package com.zedd.rpl.model.room.controller;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.model.room.impl.RoomService;
import com.zedd.rpl.model.room.req.RoomRequest;
import com.zedd.rpl.model.room.res.RoomAdminResponse;
import com.zedd.rpl.model.room.res.RoomResponse;
import com.zedd.rpl.model.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/rooms")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class RoomAdminController {

  private final RoomService roomService;

  /** Includes inactive rooms. */
  @GetMapping
  public ApiResponse<List<RoomResponse>> listAll() {
    return ApiResponse.ok(roomService.listAll());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<RoomAdminResponse> create(@Valid @RequestBody RoomRequest request) {
    return ApiResponse.ok("Room created", roomService.create(request));
  }

  /**
   * When the room is deactivated and still has active bookings, the response carries the number of
   * affected bookings and a link to the filtered booking list. Those bookings are not canceled automatically.
   */
  @PutMapping("/{id}")
  public ApiResponse<RoomAdminResponse> update(@AuthenticationPrincipal User admin,
                                               @PathVariable Long id,
                                               @Valid @RequestBody RoomRequest request) {
    return ApiResponse.ok("Room updated", roomService.update(admin.getId(), id, request));
  }

  @DeleteMapping("/{id}")
  public ApiResponse<Void> delete(@AuthenticationPrincipal User admin, @PathVariable Long id) {
    roomService.delete(admin.getId(), id);
    return ApiResponse.message("Room deleted");
  }
}
