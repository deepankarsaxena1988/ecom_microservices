package com.ecom.pgvector.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(EmbeddingModelProperties.class)
public class EcomPgVectorSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcomPgVectorSearchApplication.class, args);
    }
}
