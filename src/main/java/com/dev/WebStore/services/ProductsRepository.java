package com.dev.WebStore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.WebStore.models.Product;

public interface ProductsRepository extends JpaRepository<Product, Integer> {

}
