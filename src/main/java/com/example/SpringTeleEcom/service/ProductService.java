package com.example.SpringTeleEcom.service;

import com.example.SpringTeleEcom.model.Product;
import com.example.SpringTeleEcom.repo.ProductRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepo productRepo;

    public ProductService(ProductRepo productRepo) {
        this.productRepo = productRepo;
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    // Get product by Long id (controller uses Long, entity uses int)
    public Product getProductById(Long id) {
        if (id == null) return null;
        Optional<Product> productOpt = productRepo.findById(Math.toIntExact(id));
        return productOpt.orElse(null);
    }

    /**
     * Add or update product.
     * - If imageFile is provided → store new image.
     * - If imageFile is null/empty and product has id → keep existing image.
     */
    public Product addOrUpdateProduct(Product product, MultipartFile imageFile) throws IOException {

        // Validate required fields
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null) {
            throw new IllegalArgumentException("Product price is required");
        }

        Product productToSave;

        // If updating an existing product, load it first
        if (product.getId() != 0) {
            productToSave = productRepo.findById(product.getId())
                    .orElse(new Product());
        } else {
            productToSave = new Product();
        }

        // Copy basic fields (except image fields)
        productToSave.setName(product.getName());
        productToSave.setDescription(product.getDescription() != null ? product.getDescription() : "");
        productToSave.setBrand(product.getBrand() != null ? product.getBrand() : "");
        productToSave.setPrice(product.getPrice());
        productToSave.setCategory(product.getCategory() != null ? product.getCategory() : "");
        productToSave.setReleaseDate(product.getReleaseDate());
        productToSave.setStockQuantity(product.getStockQuantity());

        // Automatically set productAvailable based on stock quantity
        productToSave.setProductAvailable(product.getStockQuantity() > 0);

        // Handle image (only replace if a new file is sent)
        if (imageFile != null && !imageFile.isEmpty()) {
            productToSave.setImageName(imageFile.getOriginalFilename());
            productToSave.setImageType(imageFile.getContentType());
            productToSave.setImageData(imageFile.getBytes());
        }

        return productRepo.save(productToSave);
    }

    public void deleteProduct(Long id) {
        if (id == null) return;
        productRepo.deleteById(Math.toIntExact(id));
    }

    public List<Product> searchProduct(String keyword) {
        return productRepo.findByProductNameContaining(keyword);
    }
}
