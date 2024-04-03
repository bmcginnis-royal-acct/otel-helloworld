package com.example.rccl.hellootel;

import com.example.rccl.hellootel.config.EnvironmentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class HelloOtelApplication {

	public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	@Bean
	public ObjectMapper objectMapper() {
		return OBJECT_MAPPER;
	}

	public static void main(String[] args) {
		SpringApplication.run(HelloOtelApplication.class, args);
	}

	@Bean(name="webClient")
	public WebClient webClient(@Autowired EnvironmentConfig config) {
		return WebClient.builder()
				.baseUrl(config.inventoryBaseUrl)
				.defaultHeader("SERVICE-ZERO", config.getServiceName())
				.build();
	}

}
