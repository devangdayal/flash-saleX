package com.devangdayal.flashsale.product.service;

import org.springframework.stereotype.Service;
import com.devangdayal.flashsale.product.entity.Product;
import com.devangdayal.flashsale.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }
    
}
