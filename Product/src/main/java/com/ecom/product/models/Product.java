package com.ecom.product.models;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="Product")
public class Product implements Serializable{

		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		int id;
		
		@Column(name = "product_name")
		String productName;
		
		/*
		 * @Column(name="product_category_id") int productcategoryId;
		 * 
		 * 
		 * 
		 * public int getProductcategoryId() { return productcategoryId; }
		 * 
		 * public void setProductcategoryId(int productcategoryId) {
		 * this.productcategoryId = productcategoryId; }
		 */

		@ManyToOne(cascade = CascadeType.ALL)
		@JoinColumn(name="product_category_id",referencedColumnName = "id")
		ProductCategory productCategory;
		
		public ProductCategory getProductCategory() {
			return productCategory;
		}

		public void setProductCategory(ProductCategory productCategory) {
			this.productCategory = productCategory;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getProductName() {
			return productName;
		}

		public void setProductName(String productName) {
			this.productName = productName;
		}
		
		
		
}
