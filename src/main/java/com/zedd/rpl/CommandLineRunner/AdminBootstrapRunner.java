package com.zedd.rpl.CommandLineRunner;

import com.zedd.rpl.config.RplProperties;
import com.zedd.rpl.enums.Role;
import com.zedd.rpl.model.user.User;
import com.zedd.rpl.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the first admin account at startup when no admin exists yet.
 * The password comes from configuration (rpl.bootstrap.admin-password), never from code.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RplProperties props;

  @Override
  @Transactional
  public void run(String... args) {
    if (userRepository.existsByRole(Role.ROLE_ADMIN)) {
      return;
    }
    RplProperties.Bootstrap config = props.getBootstrap();
    if (config.getAdminPassword() == null || config.getAdminPassword().isBlank()) {
      log.warn("No admin account exists and rpl.bootstrap.admin-password is not set. "
          + "Set it to create the first admin.");
      return;
    }
    userRepository.save(User.builder()
        .username(config.getAdminUsername())
        .password(passwordEncoder.encode(config.getAdminPassword()))
        .role(Role.ROLE_ADMIN)
        .build());
    log.info("Bootstrap admin account '{}' created", config.getAdminUsername());
  }
}
