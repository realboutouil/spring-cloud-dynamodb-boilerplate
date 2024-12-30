package com.boutouil.dynamodb.data;

import com.boutouil.dynamodb.dao.ProductRepository;
import com.boutouil.dynamodb.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductDataLoader implements ApplicationRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Loading sample product data...");
        loadSampleProducts();
        testRepositoryOperations();
    }

    private void loadSampleProducts() {
        List<Product> sampleProducts = Arrays.asList(
                Product.builder()
                        .name("Laptop")
                        .price(1299.99)
                        .category("Electronics")
                        .stockQuantity(50)
                        .build(),
                Product.builder()
                        .name("Smartphone")
                        .price(799.99)
                        .category("Electronics")
                        .stockQuantity(100)
                        .build(),
                Product.builder()
                        .name("Coffee Maker")
                        .price(99.99)
                        .category("Home Appliances")
                        .stockQuantity(30)
                        .build(),
                Product.builder()
                        .name("Running Shoes")
                        .price(129.99)
                        .category("Sports")
                        .stockQuantity(75)
                        .build()
        );

        sampleProducts.forEach(productRepository::save);
        log.info("Sample products loaded successfully");
    }

    private void testRepositoryOperations() {
        try {
            // Test finding all products
            List<Product> allProducts = productRepository.findAll();
            log.info("Found {} products", allProducts.size());

            // Test finding by category
            List<Product> electronics = productRepository.findByCategory("Electronics");
            log.info("Found {} electronics products", electronics.size());

            // Test finding by price range
            List<Product> midRangeProducts = productRepository.findByPriceRange(100.0, 1000.0);
            log.info("Found {} products in price range $100-$1000", midRangeProducts.size());

            // Test finding low stock products
            List<Product> lowStockProducts = productRepository.findLowStockProducts(40);
            log.info("Found {} products with low stock", lowStockProducts.size());

            // Test updating stock
            if (!allProducts.isEmpty()) {
                Product firstProduct = allProducts.getFirst();
                productRepository.updateStock(firstProduct.getId(), 25);
                log.info("Updated stock for product: {}", firstProduct.getId());

                // Test incrementing retry count
                productRepository.incrementRetryCount(firstProduct.getId());
                log.info("Incremented retry count for product: {}", firstProduct.getId());
            }

        } catch (Exception e) {
            log.error("Error during repository operations test", e);
        }
    }
}