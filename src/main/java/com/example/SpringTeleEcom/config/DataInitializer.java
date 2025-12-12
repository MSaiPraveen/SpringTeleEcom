package com.example.SpringTeleEcom.config;

import com.example.SpringTeleEcom.model.Product;
import com.example.SpringTeleEcom.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepo productRepo;

    @Override
    public void run(String... args) {
        // Check if products already exist
        long productCount = productRepo.count();

        // DISABLED: Auto-initialization - Products should be uploaded by admin via dashboard
        // Only initialize if explicitly called via API endpoint /api/product/initialize

        System.out.println("📦 Database has " + productCount + " products.");
        System.out.println("ℹ️  Use admin dashboard to upload products or call POST /api/product/initialize");
    }

    public void initializeProducts() {
        System.out.println("🔄 Manual product initialization triggered...");
        addSampleProducts();
        System.out.println("✅ Manual initialization completed!");
    }

    private void addSampleProducts() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        try {
            Product[] products = {
                Product.builder()
                    .name("iPhone 15 Pro")
                    .description("Latest iPhone with A17 Pro chip, titanium design, and advanced camera system")
                    .brand("Apple")
                    .price(new BigDecimal("999.99"))
                    .category("Smartphones")
                    .releaseDate(dateFormat.parse("2024-09-15"))
                    .productAvailable(true)
                    .stockQuantity(50)
                    .imageName("iphone15pro.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("Samsung Galaxy S24 Ultra")
                    .description("Flagship Android phone with S Pen, 200MP camera, and AI features")
                    .brand("Samsung")
                    .price(new BigDecimal("1199.99"))
                    .category("Smartphones")
                    .releaseDate(dateFormat.parse("2024-01-17"))
                    .productAvailable(true)
                    .stockQuantity(40)
                    .imageName("galaxys24.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("MacBook Pro 16\"")
                    .description("Powerful laptop with M3 Max chip, perfect for professionals")
                    .brand("Apple")
                    .price(new BigDecimal("2499.99"))
                    .category("Laptops")
                    .releaseDate(dateFormat.parse("2024-11-07"))
                    .productAvailable(true)
                    .stockQuantity(25)
                    .imageName("macbookpro.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("Dell XPS 15")
                    .description("Premium Windows laptop with 4K display and powerful performance")
                    .brand("Dell")
                    .price(new BigDecimal("1799.99"))
                    .category("Laptops")
                    .releaseDate(dateFormat.parse("2024-05-10"))
                    .productAvailable(true)
                    .stockQuantity(30)
                    .imageName("dellxps15.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("Sony WH-1000XM5")
                    .description("Industry-leading noise cancelling headphones with premium sound")
                    .brand("Sony")
                    .price(new BigDecimal("399.99"))
                    .category("Audio")
                    .releaseDate(dateFormat.parse("2024-03-15"))
                    .productAvailable(true)
                    .stockQuantity(100)
                    .imageName("sonyxm5.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("iPad Pro 12.9\"")
                    .description("Ultimate iPad with M2 chip and stunning Liquid Retina XDR display")
                    .brand("Apple")
                    .price(new BigDecimal("1099.99"))
                    .category("Tablets")
                    .releaseDate(dateFormat.parse("2024-10-18"))
                    .productAvailable(true)
                    .stockQuantity(60)
                    .imageName("ipadpro.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("AirPods Pro 2")
                    .description("Premium wireless earbuds with active noise cancellation")
                    .brand("Apple")
                    .price(new BigDecimal("249.99"))
                    .category("Audio")
                    .releaseDate(dateFormat.parse("2024-09-22"))
                    .productAvailable(true)
                    .stockQuantity(150)
                    .imageName("airpodspro.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("PlayStation 5")
                    .description("Next-gen gaming console with ultra-fast SSD and stunning graphics")
                    .brand("Sony")
                    .price(new BigDecimal("499.99"))
                    .category("Gaming")
                    .releaseDate(dateFormat.parse("2024-11-12"))
                    .productAvailable(true)
                    .stockQuantity(20)
                    .imageName("ps5.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("Nintendo Switch OLED")
                    .description("Portable gaming console with vibrant OLED screen")
                    .brand("Nintendo")
                    .price(new BigDecimal("349.99"))
                    .category("Gaming")
                    .releaseDate(dateFormat.parse("2024-10-08"))
                    .productAvailable(true)
                    .stockQuantity(45)
                    .imageName("switcholed.jpg")
                    .imageType("image/jpeg")
                    .build(),

                Product.builder()
                    .name("Canon EOS R6 Mark II")
                    .description("Professional mirrorless camera with 24.2MP full-frame sensor")
                    .brand("Canon")
                    .price(new BigDecimal("2499.99"))
                    .category("Cameras")
                    .releaseDate(dateFormat.parse("2024-02-14"))
                    .productAvailable(true)
                    .stockQuantity(15)
                    .imageName("canonr6.jpg")
                    .imageType("image/jpeg")
                    .build()
            };

            for (Product product : products) {
                productRepo.save(product);
                System.out.println("  ✓ Added: " + product.getName());
            }

        } catch (Exception e) {
            System.err.println("❌ Error adding sample products: " + e.getMessage());
            System.err.println("   Stack trace: " + e.getClass().getName());
        }
    }
}

