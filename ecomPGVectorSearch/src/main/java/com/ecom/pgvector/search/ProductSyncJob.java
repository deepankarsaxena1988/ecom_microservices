package com.ecom.pgvector.search;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class ProductSyncJob {

    private static final Logger log = LoggerFactory.getLogger(ProductSyncJob.class);

    private static final String PRODUCT_QUERY = """
            SELECT p.id AS product_id,
                   p.product_name,
                   p.product_desc_heading,
                   p.product_desc_detail,
                   c.category_name,
                   p.domain,
                   p.product_rating
            FROM ecom_product p
            LEFT JOIN ecom_product_category c ON p.product_category_id = c.category_id
            ORDER BY p.id
            """;

    private static final String PRODUCT_QUERY_SINCE = """
            SELECT p.id AS product_id,
                   p.product_name,
                   p.product_desc_heading,
                   p.product_desc_detail,
                   c.category_name,
                   p.domain,
                   p.product_rating
            FROM ecom_product p
            LEFT JOIN ecom_product_category c ON p.product_category_id = c.category_id
            WHERE p.id > ?
            ORDER BY p.id
            """;

    private final JdbcTemplate jdbcTemplate;

    public ProductSyncJob(@Qualifier("sourceJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ProductTextData> loadProducts() {
        log.info("Loading all product rows from MySQL for vector indexing");
        return jdbcTemplate.query(PRODUCT_QUERY, productRowMapper());
    }

    public List<ProductTextData> loadProductsSince(Long lastProductId) {
        if (lastProductId == null) {
            return loadProducts();
        }

        log.info("Loading product rows from MySQL after product id {} for incremental sync", lastProductId);
        return jdbcTemplate.query(PRODUCT_QUERY_SINCE, productRowMapper(), lastProductId);
    }

    private RowMapper<ProductTextData> productRowMapper() {
        return new RowMapper<>() {
            @Override
            public ProductTextData mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new ProductTextData(
                        rs.getLong("product_id"),
                        rs.getString("product_name"),
                        rs.getString("product_desc_heading"),
                        rs.getString("product_desc_detail"),
                        rs.getString("category_name"),
                        rs.getString("domain"),
                        rs.getBigDecimal("product_rating")
                );
            }
        };
    }
}
