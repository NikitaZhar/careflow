package com.careflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.careflow.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}