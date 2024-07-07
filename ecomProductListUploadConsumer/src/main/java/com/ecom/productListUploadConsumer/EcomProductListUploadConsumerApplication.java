package com.ecom.productListUploadConsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;



//@EnableJpaRepositories
@SpringBootApplication
//@EntityScan(basePackages = {"com.ecom.productListUploadConsumer.entity"})
public class EcomProductListUploadConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcomProductListUploadConsumerApplication.class, args);
	}

}
