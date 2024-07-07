package com.ecom.product;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;





@EnableJpaRepositories
@EntityScan("com.ecom.product.models")
@SpringBootApplication
public class Application implements ApplicationContextAware {

	private static ApplicationContext context; 
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		
		ProductService prodservice=(ProductService)context.getBean("prodServ");
		prodservice.addProduct();
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		 context = applicationContext;
	}
	
	

}
