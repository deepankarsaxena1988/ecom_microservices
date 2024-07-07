package com.ecom.ecom.controllers;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.ecom.entities.Product;
import com.ecom.ecom.services.ProductListService;
@CrossOrigin(origins= {"*"}, maxAge = 4800, allowCredentials = "false" )
@RestController
public class ProducstList {
	
@Autowired	
ProductListService productListService;
	
@GetMapping("/productList")	
public List<Product> getProductList() {
	//ClassPathResource classPathResource = new ClassPathResource("product.json");
	String content="";
	List<Product> products=new ArrayList<Product>();
	try {
		products = productListService.getProducts();
		System.out.println(products.size());
		
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Access-Control-Allow-Origin", "*");
	    responseHeaders.set("Baeldung-Example-Header", 
	      "Value-ResponseEntityBuilderWithHttpHeaders");
		ResponseEntity.ok().headers(responseHeaders);
		
		
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	return products;
}

}
