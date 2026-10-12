package com.zedd.rpl.model.student.controller;

import com.zedd.rpl.common.ApiResponse;
import com.zedd.rpl.common.PageResponse;
import com.zedd.rpl.model.student.impl.StudentAdminService;
import com.zedd.rpl.model.student.req.ResetPasswordRequest;
import com.zedd.rpl.model.student.req.StudentRequest;
import com.zedd.rpl.model.student.res.StudentResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/students")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class StudentAdminController {

  private final StudentAdminService studentAdminService;

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResponse<PageResponse<StudentResponse>> list(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return ApiResponse.ok(studentAdminService.list(page, size));
  }

  @GetMapping(
      path = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<StudentResponse> getById(@PathVariable Long id) {
    return ApiResponse.ok(studentAdminService.getById(id));
  }

  @PostMapping(
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
    return ApiResponse.ok("Student created", studentAdminService.create(request));
  }

  @PutMapping(
      path = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<StudentResponse> update(@PathVariable Long id,
                                             @Valid @RequestBody StudentRequest request) {
    return ApiResponse.ok("Student updated", studentAdminService.update(id, request));
  }

  @DeleteMapping(
      path = "/{id}",
      produces = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<Void> delete(@PathVariable Long id) {
    studentAdminService.delete(id);
    return ApiResponse.message("Student deleted");
  }

  @PatchMapping(
      path = "/{id}/password",
      produces = MediaType.APPLICATION_JSON_VALUE,
      consumes = MediaType.APPLICATION_JSON_VALUE
  )
  public ApiResponse<Void> resetPassword(@PathVariable Long id,
                                         @Valid @RequestBody ResetPasswordRequest request) {
    studentAdminService.resetPassword(id, request);
    return ApiResponse.message("Password reset");
  }
}

