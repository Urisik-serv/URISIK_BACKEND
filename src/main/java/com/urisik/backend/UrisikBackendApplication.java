package com.urisik.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing(modifyOnCreate = false)
public class UrisikBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(UrisikBackendApplication.class, args);
	}

}
