package com.zedd.rpl.model.auth;

import com.zedd.rpl.model.auth.req.AuthenticationRequest;
import com.zedd.rpl.model.auth.req.RefreshRequest;
import com.zedd.rpl.model.auth.res.AuthenticationResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API")
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid
            @RequestBody
            final AuthenticationRequest request) {
        return ResponseEntity.ok(this.service.login(request));
    }


  @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
      @RequestBody
            final RefreshRequest request) {
        return ResponseEntity.ok(this.service.refreshToken(request));
    }
}
