package com.zedd.rpl.config;

import com.zedd.rpl.model.user.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuditorAware<Long> auditorAware() {
        return new ApplicationAuditorAware();
    }

    /*@Bean
    public UserDetailsService userDetailsService(
        UserRepository userRepository) {

        return username -> userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User tidak ditemukan: " + username
            ));
    }*/
}
