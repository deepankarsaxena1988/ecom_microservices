package com.ecom.productList.entity;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "ecom_product")
public class ProductDetailEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productSeqGen")
	@SequenceGenerator(name = "productSeqGen", sequenceName = "product_seq", initialValue = 1)
	private Long id;

	@Column(name = "product_code")
	String productCode;

	@Column(name = "product_name")
	String productName;

	@Column(name = "product_img")
	String productImage;

	@Column(name = "product_desc_heading")
	String productDescHeading;

	@Column(name = "product_desc_detail")
	String productDescDetail;
	
	@Column(name = "domain")
	String domain;
	@Column(name = "product_rating")
	BigDecimal productRating;

	
	//Unidirectional oneToMany relation
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id", referencedColumnName = "id") private
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
