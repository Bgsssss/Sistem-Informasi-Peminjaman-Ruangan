package com.zedd.rpl.model.room.controller;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.enums.RoomType;
import com.zedd.rpl.model.room.impl.RoomService;
import com.zedd.rpl.model.room.res.RoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;

  /** Active rooms only. Optional filter: ?type=SMALL or ?type=LARGE. */
  @GetMapping
  public ApiResponse<List<RoomResponse>> list(@RequestParam(required = false) RoomType type) {
    return ApiResponse.ok(roomService.list(type));
  }

  /** An inactive room can still be opened. Check "active" in the response to hide the booking button. */
  @GetMapping("/{id}")
  public ApiResponse<RoomResponse> getDetail(@PathVariable Long id) {
    return ApiResponse.ok(roomService.getDetail(id));
  }
}
