package com.ecom.kafkaProducer.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.ecom.kafkaProducer.dto.Employee;

@Service
public class KafkaProducerService {
	
	@Autowired
	public KafkaTemplate<String, Employee> kafkaTemplate;
	
	@Value("${topic.name}")
	private String topicName;
	
	public void sendMessageToTopic(Employee emp){
		System.out.println("topic name"+ topicName);
		try {
			CompletableFuture<SendResult<String, Employee>> future = kafkaTemplate.send(topicName,UUID.randomUUID().toString(), emp);
			future.whenComplete((result, ex) ->{
				if(ex!=null) {
					System.out.println("message send ["+emp+"] with offset ["+result.getRecordMetadata().offset()+"] on topic");
				}else {
					System.out.println("unable to send message ["+emp+"] on topicdue to :"+ex.getMessage());
				}
			});
		} catch (KafkaProducerException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}catch (KafkaException ex) {
			ex.printStackTrace();
		}catch (Exception exc) {
			System.out.println(exc);
		}
	}

}
