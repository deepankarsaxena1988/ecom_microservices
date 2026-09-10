package com.ecom.productList.entity;



import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "ecom_product")
public class ProductEntity {

	/*
	 * Initially, I was using the @GeneratedValue annotation with strategy i.e
	 * GenerationType.IDENTITY on my entity class. Hibernate has disabled batch
	 * updates with this strategy because it has to make a select call to get the id
	 * from the database to insert each row. MySQL doesn’t support creating
	 * sequences. To get around this, I created a table with the name of the
	 * sequence having a single field called next_val . Then I added a single row
	 * with an initial value.
	 */

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

	@Column(name = "product_category_id")
	private Long productCategoryId;

	@ManyToOne
	@JoinColumn(name = "product_category_id", referencedColumnName = "category_id", insertable = false, updatable = false)
	private ProductCategoryEntity productCategory;

	

	public BigDecimal getProductRating() {
		return productRating;
	}

	public void setProductRating(BigDecimal productRating) {
		this.productRating = productRating;
	}

	public ProductCategoryEntity getProductCategory() {
		return productCategory;
	}

	public void setProductCategory(ProductCategoryEntity productCategory) {
		this.productCategory = productCategory;
	}

	public Long getProductCategoryId() {
		return productCategoryId;
	}

	public void setProductCategoryId(Long productCategoryId) {
		this.productCategoryId = productCategoryId;
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

	@Override
	public String toString() {
		return "ProductEntity{" +
				"id=" + id +
				", productCode='" + productCode + '\'' +
				", productName='" + productName + '\'' +
				", productImage='" + productImage + '\'' +
				", productDescHeading='" + productDescHeading + '\'' +
				", productDescDetail='" + productDescDetail + '\'' +
				", domain='" + domain + '\'' +
				", productRating=" + productRating +
				", productCategoryId=" + productCategoryId +
				", productCategory=" + (productCategory != null ? productCategory.getCategoryName() : null) +
				'}';
	}

}