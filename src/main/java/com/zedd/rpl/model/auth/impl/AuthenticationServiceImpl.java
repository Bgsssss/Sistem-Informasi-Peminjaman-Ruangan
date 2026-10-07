package com.zedd.rpl.model.auth.impl;

import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.model.auth.AuthenticationService;
import com.zedd.rpl.model.auth.req.AuthenticationRequest;
import com.zedd.rpl.model.auth.req.RefreshRequest;
import com.zedd.rpl.model.auth.res.AuthenticationResponse;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.model.user.UserMapper;
import com.zedd.rpl.model.user.UserRepository;
import com.zedd.rpl.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.zedd.rpl.exception.ErrorCode.EMAIL_ALREADY_EXISTS;
import static com.zedd.rpl.exception.ErrorCode.PASSWORD_MISMATCH;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserRepository userRepository;
  private final UserMapper userMapper;


  @Override
  public AuthenticationResponse login(AuthenticationRequest request) {
    final Authentication auth = this.authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
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

  private void checkUserEmail(final String email) {
    final boolean emailExists = this.userRepository.existsByEmailIgnoreCase(email);
    if (emailExists) {
      throw new BusinessException(EMAIL_ALREADY_EXISTS);
    }
  }

  private void checkPasswords(final String password,
                              final String confirmPassword) {
    if (password == null || !password.equals(confirmPassword)) {
      throw new BusinessException(PASSWORD_MISMATCH);
    }
  }

}
