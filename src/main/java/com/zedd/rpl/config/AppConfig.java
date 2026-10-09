package com.zedd.rpl.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(RplProperties.class)
public class AppConfig {

  /** One time source for every service, so the time zone is consistent and easy to test. */
  @Bean
  public Clock clock(RplProperties props) {
    return Clock.system(ZoneId.of(props.getTimezone()));
  }
}
