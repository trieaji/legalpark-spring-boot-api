package com.soloproject.LegalPark;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LegalParkApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegalParkApplication.class, args);
		System.out.println("Hello guys");
	}

	@Bean // Tell Spring that this method produces a bean that will be managed by Spring Context.
	public ModelMapper modelMapper() {
		return new ModelMapper(); // Creating a ModelMapper instance
	}

}
