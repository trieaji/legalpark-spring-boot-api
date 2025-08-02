package com.soloproject.LegalPark;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LegalParkApplication {

	public static void main(String[] args) {
		SpringApplication.run(LegalParkApplication.class, args);
		System.out.println("Hello adick - adick");
	}

	@Bean // Memberitahu Spring bahwa metode ini menghasilkan sebuah bean yang akan dikelola oleh Spring Context
	public ModelMapper modelMapper() {
		return new ModelMapper(); // Membuat instance ModelMapper
	}

}
