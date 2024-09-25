package com.ecom.productList.entity.dto;

import java.math.BigDecimal;

import jakarta.persistence.Column;

public class ProductDTO {

	
	private Long id;

	String productCode;

	String productName;

	String productImage;

	String productDescHeading;

	String productDescDetail;
	
	String domain;
	
	BigDecimal productRating;

	

	public BigDecimal getProductRating() {
		return productRating;
	}

	public void setProductRating(BigDecimal productRating) {
		this.productRating = productRating;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductImage() {
		return productImage;
	}

	public void setProductImage(String productImage) {
		this.productImage = productImage;
	}

	public String getProductDescHeading() {
		return productDescHeading;
	}

	public void setProductDescHeading(String productDescHeading) {
		this.productDescHeading = productDescHeading;
	}

	public String getProductDescDetail() {
		return productDescDetail;
	}

	public void setProductDescDetail(String productDescDetail) {
		this.productDescDetail = productDescDetail;
	}

	
}
