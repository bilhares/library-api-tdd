package com.cursotdd;

import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.cursotdd.service.EmailService;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {

//	@Autowired
//	private EmailService emailService;

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

//	@Bean
//	public CommandLineRunner runner() {
//		return args -> {
//			List<String> mailsList = Arrays.asList("");
//			emailService.send("teste", mailsList);
//		};
//	}

}
