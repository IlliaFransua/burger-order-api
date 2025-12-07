package com.fransua.burger_order_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fransua.burger_order_api.dto.request.OrderRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonReadersConfig {

  private final ObjectMapper mapper;

  public JacksonReadersConfig(ObjectMapper mapper) {
    this.mapper = mapper;
    this.mapper.findAndRegisterModules();
  }

  @Bean
  public ObjectReader orderReader() {
    return mapper.readerFor(OrderRequest.class);
  }
}
