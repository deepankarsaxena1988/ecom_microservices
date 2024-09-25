package com.ecom.productList.service;

import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.productList.entity.ProductDetailEntity;
import com.ecom.productList.entity.ProductEntity;
import com.ecom.productList.entity.dto.ProductDetailDTO;
import com.ecom.productList.reporsitory.ProductDetailRepository;
import com.ecom.productList.reporsitory.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductListService {

	@Autowired
	ProductRepository productRepository;
	
	@Autowired
	ProductDetailRepository productDetailReporsitory;
	//@Autowired
	ModelMapper modelMapper=new ModelMapper();
	 
	 
	public List<ProductEntity> getAllProducts(){
		List<ProductEntity> allProducts = productRepository.findAll();
		System.out.println(allProducts);
		return allProducts;
	}
	
	
	public Optional getProductDetail(Long productId) {
		/**
		 * getReferenceById() :- return a proxy and can throw an exception when passed non-existing id, method uses fetch lazy hence when return from controller directly give   below exception
		 * com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor and no properties discovered to create BeanSerializer
		 * solution :- always use seperate DTO's for return in controller ( this is also a best practice as design point of view)
		 * 
		 * findById() return an  Optional which is eager loading 
		 */
		//ProductDetail productDetail = productDetailReporsitory.getReferenceById(productId);
		Optional<ProductDetailEntity> productDetail = productDetailReporsitory.findById(productId);
		//System.out.println(productDetail.get().getId()+" ");//+productDetail.getProductReview().size());
		convertProductDetailEntityToDTO(productDetail.get());
		return productDetail;
		
	}
	
	
	
	private ProductDetailDTO convertProductDetailEntityToDTO(ProductDetailEntity productDetailEntity) {
		return  modelMapper.map(productDetailEntity, ProductDetailDTO.class);
		//System.out.println(productDetailDTO.getId());
		//System.out.println(productDetailDTO.getProductReview().size());
		//return productDetailDTO;
	}
	
	
}
