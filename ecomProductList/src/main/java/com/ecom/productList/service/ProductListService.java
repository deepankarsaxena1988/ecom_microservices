package com.ecom.productList.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.ecom.productList.entity.Product;
import com.ecom.productList.reporsitory.ProductRepository;

@Service
public class ProductListService {

	@Autowired
	ProductRepository productRepository;
	
	public List<Product> getAllProducts(){
		List<Product> allProducts = productRepository.findAll();
		System.out.println(allProducts);
		return allProducts;
	}
}
