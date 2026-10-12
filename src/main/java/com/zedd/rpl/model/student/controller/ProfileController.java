package com.zedd.rpl.model.student.controller;


import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.model.student.Impl.ProfileService;
import com.zedd.rpl.model.student.res.ProfileResponse;
import com.zedd.rpl.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  /** The student id always comes from the authenticated user, never from a request parameter. */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<ProfileResponse> getProfile(@AuthenticationPrincipal User user) {
    return ApiResponse.ok(profileService.getProfile(user.getStudent().getId()));
  }
}

