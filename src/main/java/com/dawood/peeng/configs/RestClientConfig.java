package com.dawood.peeng.configs;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

  @Bean
  public RestClient restClientConfig() {

    HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

    factory.setConnectionRequestTimeout(Duration.ofSeconds(5));
    factory.setReadTimeout(Duration.ofSeconds(5));

    return RestClient.builder()
        .requestFactory(factory)
        .defaultHeader("User-Agent", "Peeng/1.0 API Monitor")
        .build();

  }

}
