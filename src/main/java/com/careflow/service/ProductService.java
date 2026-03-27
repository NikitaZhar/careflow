package com.careflow.service;

import java.util.List;
import org.springframework.stereotype.Service;

import com.careflow.exception.ProductNotFoundException;
import com.careflow.model.Product;
import com.careflow.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public Product findById(Long id) {
        return findProductByIdOrThrow(id);
    }
    
    public Product update(Long id, Product updatedProduct) {
    	Product existingProduct = findProductByIdOrThrow(id);

        existingProduct.setServiceName(updatedProduct.getServiceName());
        existingProduct.setServicePrice(updatedProduct.getServicePrice());

        return productRepository.save(existingProduct);
    }
    
    public void deleteById(Long id) {
    	Product existingProduct = findProductByIdOrThrow(id);
    	productRepository.delete(existingProduct);
    }
    
    private Product findProductByIdOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}