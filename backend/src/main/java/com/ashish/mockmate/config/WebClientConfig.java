package com.ashish.mockmate.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

	@Value("${gemini.base-url}")
	private String geminiBaseUrl;

	@Bean
	public WebClient geminiWebClient() {
		return WebClient.builder().baseUrl(geminiBaseUrl)
				.codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
				.build();
	}
}
