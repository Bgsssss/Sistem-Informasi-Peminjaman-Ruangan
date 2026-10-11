package com.zedd.rpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ProjectRplApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjectRplApplication.class, args);
	}

}
