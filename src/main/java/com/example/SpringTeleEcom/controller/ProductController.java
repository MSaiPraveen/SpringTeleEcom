package com.example.SpringTeleEcom.controller;

import com.example.SpringTeleEcom.model.Product;
import com.example.SpringTeleEcom.service.ProductService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ================== PUBLIC ENDPOINTS ==================

    // Get all products (public)
    @GetMapping("/product")
    public ResponseEntity<List<Product>> getProducts() {
        System.out.println("📋 GET /api/product - Fetching all products");
        List<Product> products = productService.getAllProducts();
        System.out.println("📦 Found " + products.size() + " products");

        if (!products.isEmpty()) {
            System.out.println("📦 Sample product: " + products.get(0).getName());
        } else {
            System.out.println("⚠️ No products in database!");
        }

        return ResponseEntity.ok(products);
    }




    // Get single product by id (public)
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get product image (public)
    // Frontend: GET http://localhost:8080/api/product/{id}/image
    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        if (product != null && product.getImageData() != null) {
            HttpHeaders headers = new HttpHeaders();

            // If you store image type (e.g. "image/jpeg") in DB:
            if (product.getImageType() != null) {
                headers.setContentType(MediaType.parseMediaType(product.getImageType()));
            } else {
                headers.setContentType(MediaType.IMAGE_JPEG); // fallback
            }

            return new ResponseEntity<>(product.getImageData(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Search products (public)
    @GetMapping("/product/search")
    public ResponseEntity<List<Product>> searchProduct(@RequestParam String keyword) {
        List<Product> products = productService.searchProduct(keyword);
        System.out.println("Searched products " + products);
        return ResponseEntity.ok(products);
    }

    // ================== ADMIN-ONLY ENDPOINTS ==================

    // Add product – ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestPart("product") Product product,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            System.out.println("📦 Received product upload request:");
            System.out.println("   Product: " + product.getName());
            System.out.println("   Image file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));

            Product savedProduct = productService.addOrUpdateProduct(product, imageFile);

            System.out.println("✅ Product saved successfully with ID: " + savedProduct.getId());
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (IOException e) {
            System.err.println("❌ Error uploading product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading product: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    // Update product – ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/product/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") Product product,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            System.out.println("📝 Received product update request for ID: " + id);
            System.out.println("   Product: " + product.getName());
            System.out.println("   Image file: " + (imageFile != null ? imageFile.getOriginalFilename() : "null"));

            // Product.id is int in your entity, so we convert Long -> int here
            product.setId(Math.toIntExact(id));

            Product updatedProduct = productService.addOrUpdateProduct(product, imageFile);
            if (updatedProduct != null) {
                System.out.println("✅ Product updated successfully");
                return ResponseEntity.ok(updatedProduct);
            } else {
                System.err.println("❌ Product not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            System.err.println("❌ Error updating product: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating product: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error: " + e.getMessage());
        }
    }

    // Delete product – ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null) {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Product deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
