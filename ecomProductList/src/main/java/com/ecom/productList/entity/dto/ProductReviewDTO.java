package com.ecom.productList.entity.dto;

import jakarta.persistence.Column;

public class ProductReviewDTO {

	private Long id;
	
	private Long productId ;
	
	private Long reviewUserId ;
	
	String productReviewComment;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getReviewUserId() {
		return reviewUserId;
	}

	public void setReviewUserId(Long reviewUserId) {
		this.reviewUserId = reviewUserId;
	}

	public String getProductReviewComment() {
		return productReviewComment;
	}

	public void setProductReviewComment(String productReviewComment) {
		this.productReviewComment = productReviewComment;
	}
	
	
	
}
