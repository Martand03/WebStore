package com.dev.WebStore.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.dev.WebStore.models.Product;
import com.dev.WebStore.models.ProductDetail;
import com.dev.WebStore.services.ProductsRepository;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/products")
public class ProductsController {
	
	@Autowired
	private ProductsRepository repository;
	
	
	@GetMapping({"","/"})
	public String showProductList(Model model) {
		
		List<Product> products = repository.findAll();
		model.addAttribute("products", products);
		return "products/index";
		
	}
	
	@GetMapping("/create")
	public String showCreatePage(Model model) {
		ProductDetail productDetail = new ProductDetail();
		model.addAttribute("productDetail",productDetail);
		return "products/CreateProduct";
	}
	
	@PostMapping("/create")
	public String createProduct( 
		@Valid @ModelAttribute ProductDetail productDetail,
		BindingResult result){
		
		if(productDetail.getImageFile().isEmpty()) {
			result.addError(new FieldError("productDetail", "imageFile", "The image file is required"));
		}
		
		if(result.hasErrors()) {
			return "products/CreateProduct";
		}
		
		MultipartFile image = productDetail.getImageFile();
		Date createdAt = new Date(); 
		String storageFile = createdAt.getTime() + " " + image.getOriginalFilename();
		
		try {
			String uploadDir = "public/images/";
			Path uploadPath = Paths.get(uploadDir);
			
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			try(InputStream inputStream = image.getInputStream()){
				Files.copy(inputStream, Paths.get(uploadDir + storageFile),
						StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		
		
		Product product = new Product();
		product.setName(productDetail.getName());
		product.setBrand(productDetail.getBrand());
		product.setCategory(productDetail.getCategory());
		product.setPrice(productDetail.getPrice());
		product.setDescription(productDetail.getDescription());
		product.setCreatedAtDate(createdAt);
		product.setImageFileName(storageFile);
		
		repository.save(product);
		
		
		return "redirect:/products";
	}
	
	
	@GetMapping("/edit")
	public String showEditPage(
			Model model,
			@RequestParam int id) {
		
		try {
			
			Product product = repository.findById(id).get();
			model.addAttribute("product", product);
			
			ProductDetail productDetail = new ProductDetail();
			product.setName(product.getName());
			product.setBrand(product.getBrand());
			product.setCategory(product.getCategory());
			product.setPrice(product.getPrice());
			product.setDescription(product.getDescription());
			
			model.addAttribute("productDetail",productDetail);
			
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
			return "redirect:/products";
		}
		
		
		
		
		return "products/EditProduct";
	}
	
	@PostMapping("/edit")
	public String updateProduct(
			Model model,
			@RequestParam int id,
			@Valid @ModelAttribute ProductDetail productDetail,
			BindingResult result) {
		
		try {
			
			Product product = repository.findById(id).get();
			model.addAttribute("product",product);
			
			if(result.hasErrors()) {
				return "products/EditProduct";
			}
			
			if(!productDetail.getImageFile().isEmpty()) {
				
				String uploadDir = "public/images/";
				Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());
				try {
					Files.delete(oldImagePath);
				} catch (Exception ex) {
					System.out.println("Exception: " + ex.getMessage());
				}
				
				MultipartFile image = productDetail.getImageFile();
				Date createdAt = new Date();
				String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
				
				try (InputStream inputStream = image.getInputStream()) {
					
					Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
					StandardCopyOption.REPLACE_EXISTING);
				} 
				
				product.setImageFileName(storageFileName);
			}
			
			product.setName(productDetail.getName());
			product.setBrand(productDetail.getBrand());
			product.setCategory(productDetail.getCategory());
			product.setPrice(productDetail.getPrice());
			product.setDescription(productDetail.getDescription());
			
			repository.save(product);
			
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		
		return "redirect:/products";
	}
	
	
	@GetMapping("/delete")
	public String deleteProduct(
			@RequestParam int id) {
		
		try {
			
			Product product = repository.findById(id).get();
			
			Path imagePath = Paths.get("public/images/" + product.getImageFileName());
			
			try {
				
				Files.delete(imagePath);
				
			} catch (Exception ex) {
				System.out.println("Exception: " + ex.getMessage());
			}
			
			repository.delete(product);
			
			
		} catch (Exception ex) {
			System.out.println("Exception: " + ex.getMessage());
		}
		
		return "redirect:/products";
	}
	
}

