package com.ecom.productList.entity.dto;

import java.math.BigDecimal;
import java.util.List;

import com.ecom.productList.entity.ProductReviewEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;

public class ProductDetailDTO {

	private Long id;

	
	String productCode;

	String productName;

	String productImage;

	String productDescHeading;

	String productDescDetail;
	
	String domain;
	
	BigDecimal productRating;

	
	List<ProductReviewEntity> productReview;
	 
	  
	  public List<ProductReviewEntity> getProductReview() { return productReview; }
	  
	  public void setProductReview(List<ProductReviewEntity> productReview) {
	  this.productReview = productReview; }
	 

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
