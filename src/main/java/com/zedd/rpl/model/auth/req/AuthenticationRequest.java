package com.zedd.rpl.model.auth.req;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationRequest {

    @NotBlank
    @JsonAlias("email")
    private String identifier;

    @NotBlank
    private String password;
}
