package com.example.SpringTeleEcom.service;

import com.example.SpringTeleEcom.model.Product;
import com.example.SpringTeleEcom.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepo productRepo;


    public List<Product> getAllProducts() {
        return productRepo.findAll();

    }

    public Product getProductById(Long id) {
        return productRepo.findById(Math.toIntExact(id)).orElse(null);
    }

    public Product addOrUpdateProduct(Product product, MultipartFile imageFile) throws IOException {
        product.setImageName(imageFile.getOriginalFilename());
        product.setImageType(imageFile.getContentType());
        product.setImageData(imageFile.getBytes());

        return productRepo.save(product);
    }


    public void deleteProduct(Long id) {
        productRepo.deleteById(Math.toIntExact(id));
    }

    public List<Product> searchProduct(String keyword) {
        return productRepo.findByProductNameContaining(keyword);
    }
}
