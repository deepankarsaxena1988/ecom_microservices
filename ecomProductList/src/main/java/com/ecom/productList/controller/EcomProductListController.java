package com.ecom.productList.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.productList.entity.dto.ProductDTO;
import com.ecom.productList.entity.dto.ProductDetailDTO;
import com.ecom.productList.service.ProductListService;

@CrossOrigin(origins = { "*" }, maxAge = 4800, allowCredentials = "false")
@RestController
public class EcomProductListController {

	@Autowired
	ProductListService productListService;

	@GetMapping({ "/ecom/productList", "/productList" })
	public List<ProductDTO> getAllProducts() {
		return productListService.getAllProducts();
	}

	@GetMapping("/ecom/productDetail/{productId}")
	public ProductDetailDTO getProductDetail(@PathVariable Long productId) {
		return productListService.getProductDetail(productId);
	}
}
