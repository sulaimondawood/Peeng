package com.dawood.peeng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PeengApplication {

	public static void main(String[] args) {
		SpringApplication.run(PeengApplication.class, args);
	}

}
