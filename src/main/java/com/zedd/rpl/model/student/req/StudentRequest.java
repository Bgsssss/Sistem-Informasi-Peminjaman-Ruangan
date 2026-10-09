package com.zedd.rpl.model.student.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** password is required when creating and optional when updating (blank = keep the current one). */
public record StudentRequest(
    @NotBlank
    @Size(max = 20)
    String studentNumber,
    @NotBlank
    @Size(max = 100)
    String name,
    @NotBlank
    @Size(max = 100)
    String faculty,
    @NotBlank
    @Email
    @Size(max = 100)
    String email,
    @Size(min = 8, max = 72)
    String password,
    Boolean active
) {}

