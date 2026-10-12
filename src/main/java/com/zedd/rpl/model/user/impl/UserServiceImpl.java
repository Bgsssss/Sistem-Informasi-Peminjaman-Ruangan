package com.zedd.rpl.model.user.impl;

import com.zedd.rpl.exception.BusinessException;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.model.user.UserRepository;
import com.zedd.rpl.model.user.UserService;
import com.zedd.rpl.model.user.req.ChangePasswordRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.zedd.rpl.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void changePassword(ChangePasswordRequest request, Long userId) {
    if (!request.getNewPassword()
        .equals(request.getConfirmNewPassword())) {
      throw new BusinessException(CHANGE_PASSWORD_MISMATCH);
    }

    final User savedUser = this.userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(USER_NOT_FOUND, userId));

    if (!this.passwordEncoder.matches(request.getCurrentPassword(),
        savedUser.getPassword())) {
      throw new BusinessException(INVALID_CURRENT_PASSWORD);
    }

    final String encoded = this.passwordEncoder.encode(request.getNewPassword());
    savedUser.setPassword(encoded);
    this.userRepository.save(savedUser);
  }

  @Override
  public UserDetails loadUserByUsername(final String userEmail) throws UsernameNotFoundException {
    return this.userRepository.findByUsername(userEmail)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with userEmail : " + userEmail));
  }
}
