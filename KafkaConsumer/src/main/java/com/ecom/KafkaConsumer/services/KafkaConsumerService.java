package com.ecom.KafkaConsumer.services;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.ecom.KafkaConsumer.dto.Employee;

@Service
public class KafkaConsumerService {
	
	
	
	@KafkaListener(topics="${topic.name}",groupId = "ecomConfigGrp")
	public void consume(ConsumerRecord<String,Employee>  employee) {
		System.out.println("message Consumed Key="+employee.key()+" and value="+employee.value());
	}
	

}
