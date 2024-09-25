package com.ecom.productList.reporsitory;

import org.springframework.data.jpa.repository.JpaRepository;


import com.ecom.productList.entity.ProductDetailEntity;

public interface ProductDetailRepository extends JpaRepository<ProductDetailEntity, Long> {

}
