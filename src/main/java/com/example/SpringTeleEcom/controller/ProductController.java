package com.example.SpringTeleEcom.controller;

import com.example.SpringTeleEcom.model.Product;
import com.example.SpringTeleEcom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin("http://localhost:5173")
public class ProductController {

    @Autowired
    private ProductService productService;
        
        @GetMapping("/product")
        public ResponseEntity<List<Product>> getProducts() {
            return new ResponseEntity<>(productService.getAllProducts(), HttpStatus.OK);
        }

        @GetMapping("/product/{id}")
        public ResponseEntity<Product> getProductById(@PathVariable Long id) {
            Product product=productService.getProductById(id);
                    if(product!=null){
                        return new ResponseEntity<>(product, HttpStatus.OK);
                    }
                    else {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }

        }
        @GetMapping("product/{productId}/image")
        public ResponseEntity<byte[]> getImageByProductId(@PathVariable Long productId){
            Product product=productService.getProductById(productId);
            if(product!=null){
                return new ResponseEntity<>(product.getImageData(),HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }


        @PostMapping("/product")
        public ResponseEntity<?> addProduct(@RequestPart Product product,@RequestPart MultipartFile imageFile){
            Product saveProduct= null;
            try {
                saveProduct = productService.addOrUpdateProduct(product,imageFile);
                return new ResponseEntity<>(saveProduct,HttpStatus.CREATED);
            } catch (IOException e) {
                return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
            }
        }
        @PutMapping("/product/{id}")
        public ResponseEntity<String> updateProduct(@PathVariable Long id,@RequestPart Product product,@RequestPart MultipartFile imageFile ) throws IOException {
            Product updateProduct=productService.addOrUpdateProduct(product,imageFile);
            if(updateProduct!=null){
                return new ResponseEntity<>("Product updated successfully",HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        @DeleteMapping("/product/{id}")
        public ResponseEntity<String> deleteProduct(@PathVariable Long id){
            Product product=productService.getProductById(id);
            if(product!=null){
                productService.deleteProduct(id);
                return new ResponseEntity<>("Product deleted successfully",HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        @GetMapping("/product/search")
        public ResponseEntity<List<Product>> searchProduct(@RequestParam String keyword){
            List<Product> products=productService.searchProduct(keyword);
            System.out.println("Seached products"+products);
            return  new ResponseEntity<>(products,HttpStatus.OK);
        }

    }

