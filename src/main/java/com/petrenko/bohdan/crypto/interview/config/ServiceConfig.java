package com.petrenko.bohdan.crypto.interview.config;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ServiceConfig {

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

	@Bean
	public RestTemplate restTemplate(ObjectMapper objectMapper) {
		return new RestTemplateBuilder()
				.setConnectTimeout(Duration.ofSeconds(15))
				.setReadTimeout(Duration.ofMinutes(2))
				.additionalMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
				.build();
	}
}
