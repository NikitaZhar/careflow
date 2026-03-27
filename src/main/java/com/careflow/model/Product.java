package com.careflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Service name must not be blank")
    private String serviceName;
    
    @NotNull(message = "Service price must not be null")
    @Positive(message = "Service price must be greater than 0")
    private Double servicePrice;

    public Product() {
    }

    public Product(String serviceName, Double servicePrice) {
        this.serviceName = serviceName;
        this.servicePrice = servicePrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Double getServicePrice() {
        return servicePrice;
    }

    public void setServicePrice(Double servicePrice) {
        this.servicePrice = servicePrice;
    }

    @Override
    public String toString() {
        return "Product" +
                "id = " + id +
                ", serviceName='" + serviceName + '\'' +
                ", servicePrice=" + servicePrice;
    }
}