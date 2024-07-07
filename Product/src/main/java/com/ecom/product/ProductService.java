package com.ecom.product;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import com.ecom.product.models.Product;
import com.ecom.product.models.ProductCategory;
import com.ecom.product.models.ProductRepository;

@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;
	
	@Bean(name="prodServ")
	public ProductService getProdService(){
		return new ProductService();
	}
	
	public void addProduct() {
		
		
		ProductCategory product1Category=new ProductCategory();
		
		product1Category.setCategorName("test");
		product1Category.setCategoryDescription("test description");
		
		
		Product product1=new Product();
		product1.setProductName("test Product");
		product1.setProductCategory(product1Category);
		try {
			productRepository.save(product1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
