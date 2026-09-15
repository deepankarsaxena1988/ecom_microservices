package com.ecom.pgvector.search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductVectorSearchController {

    private final ProductVectorSearchService productVectorSearchService;

    public ProductVectorSearchController(ProductVectorSearchService productVectorSearchService) {
        this.productVectorSearchService = productVectorSearchService;
    }

    @GetMapping("/search")
    public List<ProductVectorQueryResult> search(@RequestParam String query,
                                               @RequestParam(defaultValue = "10") int limit) {
        return productVectorSearchService.search(query, limit);
    }
}
