package com.ecom.productListUpload.services;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.common.KafkaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.ecom.productListUploadAVRO.dto.EcomProductListDTOAVRO;





@Service
public class EcomProductListUploadService {

	@Autowired
	public KafkaTemplate<String, EcomProductListDTOAVRO> kafkaTemplate;
	
	@Value("${topic.name}")
	private String topicName;
	
	public void sendMessageToTopic(EcomProductListDTOAVRO productListTO){
		System.out.println("topic name"+ topicName);
		try {
			CompletableFuture<SendResult<String, EcomProductListDTOAVRO>> future = kafkaTemplate.send(topicName,UUID.randomUUID().toString(), productListTO);
			future.whenComplete((result, ex) ->{
				if(ex==null) {
					System.out.println("message send ["+productListTO+"] with offset ["+result.getRecordMetadata().offset()+"] on topic");
				}else {
					System.out.println("unable to send message ["+productListTO+"] on topicdue to :"+ex.getMessage());
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
