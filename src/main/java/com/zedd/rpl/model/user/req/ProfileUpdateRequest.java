package com.zedd.rpl.model.user.req;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {
  private String firstName;
  private String lastName;
  private LocalDate dateOfBirth;
}
