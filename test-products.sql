-- Test Products SQL Script
-- Run this to add sample products to your database for testing

-- First, ensure the product table exists (should be auto-created by Hibernate)

-- Insert sample products
INSERT INTO product (name, description, brand, price, category, release_date, product_available, stock_quantity, image_name, image_type)
VALUES
    ('iPhone 15 Pro', 'Latest iPhone with A17 Pro chip, titanium design, and advanced camera system', 'Apple', 999.99, 'Smartphones', '2024-09-15', true, 50, 'iphone15pro.jpg', 'image/jpeg'),
    ('Samsung Galaxy S24 Ultra', 'Flagship Android phone with S Pen, 200MP camera, and AI features', 'Samsung', 1199.99, 'Smartphones', '2024-01-17', true, 40, 'galaxys24.jpg', 'image/jpeg'),
    ('MacBook Pro 16"', 'Powerful laptop with M3 Max chip, perfect for professionals', 'Apple', 2499.99, 'Laptops', '2024-11-07', true, 25, 'macbookpro.jpg', 'image/jpeg'),
    ('Dell XPS 15', 'Premium Windows laptop with 4K display and powerful performance', 'Dell', 1799.99, 'Laptops', '2024-05-10', true, 30, 'dellxps15.jpg', 'image/jpeg'),
    ('Sony WH-1000XM5', 'Industry-leading noise cancelling headphones with premium sound', 'Sony', 399.99, 'Audio', '2024-03-15', true, 100, 'sonyxm5.jpg', 'image/jpeg'),
    ('iPad Pro 12.9"', 'Ultimate iPad with M2 chip and stunning Liquid Retina XDR display', 'Apple', 1099.99, 'Tablets', '2024-10-18', true, 60, 'ipadpro.jpg', 'image/jpeg'),
    ('AirPods Pro 2', 'Premium wireless earbuds with active noise cancellation', 'Apple', 249.99, 'Audio', '2024-09-22', true, 150, 'airpodspro.jpg', 'image/jpeg'),
    ('PlayStation 5', 'Next-gen gaming console with ultra-fast SSD and stunning graphics', 'Sony', 499.99, 'Gaming', '2024-11-12', true, 20, 'ps5.jpg', 'image/jpeg'),
    ('Nintendo Switch OLED', 'Portable gaming console with vibrant OLED screen', 'Nintendo', 349.99, 'Gaming', '2024-10-08', true, 45, 'switcholed.jpg', 'image/jpeg'),
    ('Canon EOS R6 Mark II', 'Professional mirrorless camera with 24.2MP full-frame sensor', 'Canon', 2499.99, 'Cameras', '2024-02-14', true, 15, 'canonr6.jpg', 'image/jpeg')
ON CONFLICT DO NOTHING;

-- Verify the insert
SELECT COUNT(*) as total_products FROM product;
SELECT id, name, price, category, product_available, stock_quantity FROM product ORDER BY id;

