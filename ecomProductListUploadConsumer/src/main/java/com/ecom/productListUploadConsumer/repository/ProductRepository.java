package com.ecom.productListUploadConsumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.productListUploadConsumer.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
