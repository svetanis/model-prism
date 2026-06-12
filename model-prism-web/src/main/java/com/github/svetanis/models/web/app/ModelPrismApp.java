package com.github.svetanis.models.web.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Run from the model-prism root directory:
// mvn spring-boot:run -pl model-prism-web

@SpringBootApplication
@org.springframework.context.annotation.ComponentScan(basePackages = "com.github.svetanis.models.web")
public class ModelPrismApp {

  public static void main(String[] args) {
    SpringApplication.run(ModelPrismApp.class, args);
  }
}

