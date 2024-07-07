package com.ecom.kafkaProducer.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaProducerConfiguration {
	
	/*
	 * @Value("${topic.name}") private String topicName;
	 * 
	 * @Bean public NewTopic createTopic() { return new NewTopic(topicName, 2,
	 * (short)1); }
	 */
}
