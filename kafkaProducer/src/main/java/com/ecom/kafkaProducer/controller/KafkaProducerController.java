package com.ecom.kafkaProducer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.kafkaProducer.dto.Employee;
import com.ecom.kafkaProducer.service.KafkaProducerService;




@CrossOrigin(origins= {"*"}, maxAge = 4800, allowCredentials = "false" )
@RestController
@RequestMapping("/ecom")
public class KafkaProducerController {
	
	@Autowired
	public KafkaProducerService producerService;
	
	@PostMapping("/publish/employee")
	public ResponseEntity<?> publishMessage(@RequestBody Employee emp){
		try {
			producerService.sendMessageToTopic( emp);
			return ResponseEntity.ok("message published successfully");
		}catch(Exception e){
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}
