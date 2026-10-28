package com.zedd.rpl.model.auth.impl;

import com.zedd.rpl.model.auth.AuthenticationService;
import com.zedd.rpl.model.auth.req.AuthenticationRequest;
import com.zedd.rpl.model.auth.req.RefreshRequest;
import com.zedd.rpl.model.auth.res.AuthenticationResponse;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  @Override
  public AuthenticationResponse login(AuthenticationRequest request) {
    final Authentication auth = this.authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getIdentifier().trim(),
            request.getPassword()
        )
    );
    final User user = (User) auth.getPrincipal();
    final String token = this.jwtService.generateAccessToken(user.getUsername());
    final String refreshToken = this.jwtService.generateRefreshToken(user.getUsername());
    final String tokenType = "Bearer";
    return AuthenticationResponse.builder()
        .accessToken(token)
        .refreshToken(refreshToken)
        .tokenType(tokenType)
        .build();
  }

  @Override
  public AuthenticationResponse refreshToken(RefreshRequest req) {
    final String newAccessToken = this.jwtService.refreshAccessToken(req.getRefreshToken());
    final String tokenType = "Bearer";
    return AuthenticationResponse.builder()
        .accessToken(newAccessToken)
        .refreshToken(req.getRefreshToken())
        .tokenType(tokenType)
        .build();
  }
}
