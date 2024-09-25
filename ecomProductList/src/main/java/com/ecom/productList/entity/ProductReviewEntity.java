package com.ecom.productList.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "ecom_product_review")
public class ProductReviewEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ecom_product_review_id_seq")
	@SequenceGenerator(name = "ecom_product_review_id_seq", sequenceName = "ecom_product_review_id_seq", initialValue = 1)
	private Long id;
	
	@Column(name = "product_id")
	private Long productId ;
	
	@Column(name = "review_user_id")
	private Long reviewUserId ;
	
	@Column(name = "product_review_comment")
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
