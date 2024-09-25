package com.ecom.productList.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.productList.entity.ProductEntity;
import com.ecom.productList.service.ProductListService;

@RestController
@RequestMapping(path="/ecom")
public class EcomProductListController {
	
	@Autowired
	private ProductListService prodListService;
	
	/* @CrossOrigin(origins = "*") */
	@GetMapping(path = "/productList")
	public List<ProductEntity> getProductList(){
		/*
		 * HttpHeaders responseHeaders = new HttpHeaders();
		 * 
		 * responseHeaders.set("Access-Control-Allow-Origin", "*");
		 * responseHeaders.set("Access-Control-Allow-Methods",
		 * "GET,POST,OPTIONS,DELETE,PUT" );
		 * responseHeaders.set("Baeldung-Example-Header",
		 * "Value-ResponseEntityBuilderWithHttpHeaders");
		 */
		 
		//ResponseEntity.ok().headers(responseHeaders);
		
		return prodListService.getAllProducts();
	}
	
	
	@GetMapping(path = "/productDetail/{productId}")
	public Object getProductDetail(@PathVariable("productId") Long productId){
		/*
		 * HttpHeaders responseHeaders = new HttpHeaders();
		 * 
		 * responseHeaders.set("Access-Control-Allow-Origin", "*");
		 * responseHeaders.set("Access-Control-Allow-Methods",
		 * "GET,POST,OPTIONS,DELETE,PUT" );
		 * responseHeaders.set("Baeldung-Example-Header",
		 * "Value-ResponseEntityBuilderWithHttpHeaders");
		 */
		 
		//ResponseEntity.ok().headers(responseHeaders);
		
		
		 
		 
		 return prodListService.getProductDetail(productId).get();
	}

}
