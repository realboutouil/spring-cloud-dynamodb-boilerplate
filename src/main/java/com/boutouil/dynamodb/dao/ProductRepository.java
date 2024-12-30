package com.boutouil.dynamodb.dao;

import com.boutouil.dynamodb.domain.Product;
import io.awspring.cloud.dynamodb.DynamoDbTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepository {

    private final DynamoDbTemplate dynamoDbTemplate;

    public void save(Product product) {
        log.info("Saving product: {}", product);
        dynamoDbTemplate.save(product);
    }

    public void update(Product product) {
        log.info("Updating product: {}", product);
        product.setVersion(null);
        dynamoDbTemplate.update(product);
    }

    public Optional<Product> findById(String id) {
        log.info("Finding product by id: {}", id);
        return Optional.ofNullable(dynamoDbTemplate.load(
                Key.builder().partitionValue(id).build(),
                Product.class));
    }

    public void deleteById(String id) {
        log.info("Deleting product by id: {}", id);
        dynamoDbTemplate.delete(
                Key.builder().partitionValue(id).build(),
                Product.class);
    }

    public List<Product> findAll() {
        log.info("Finding all products");
        return dynamoDbTemplate.scan(ScanEnhancedRequest.builder().build(), Product.class)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    // New methods
    public List<Product> findByCategory(String category) {
        log.info("Finding products by category: {}", category);
        Expression filterExpression = Expression.builder()
                .expression("category = :category")
                .expressionValues(Map.of(":category", AttributeValue.builder().s(category).build()))
                .build();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        return dynamoDbTemplate.scan(scanRequest, Product.class)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public List<Product> findByPriceRange(Double minPrice, Double maxPrice) {
        log.info("Finding products by price range: {} - {}", minPrice, maxPrice);
        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":minPrice", AttributeValue.builder().n(String.valueOf(minPrice)).build());
        expressionValues.put(":maxPrice", AttributeValue.builder().n(String.valueOf(maxPrice)).build());

        Expression filterExpression = Expression.builder()
                .expression("price between :minPrice and :maxPrice")
                .expressionValues(expressionValues)
                .build();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        return dynamoDbTemplate.scan(scanRequest, Product.class)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public List<Product> findLowStockProducts(Integer threshold) {
        log.info("Finding low stock products below threshold: {}", threshold);
        Expression filterExpression = Expression.builder()
                .expression("stock_quantity <= :threshold")
                .expressionValues(Map.of(":threshold", AttributeValue.builder().n(String.valueOf(threshold)).build()))
                .build();

        ScanEnhancedRequest scanRequest = ScanEnhancedRequest.builder()
                .filterExpression(filterExpression)
                .build();

        return dynamoDbTemplate.scan(scanRequest, Product.class)
                .items()
                .stream()
                .collect(Collectors.toList());
    }

    public void incrementRetryCount(String id) {
        log.info("Incrementing retry count for product id: {}", id);
        findById(id).ifPresent(product -> {
            product.setRetryCount(product.getRetryCount() + 1);
            update(product);
        });
    }

    public void updateStock(String id, Integer quantity) {
        log.info("Updating stock for product id: {} with quantity: {}", id, quantity);
        findById(id).ifPresent(product -> {
            product.setStockQuantity(quantity);
            update(product);
        });
    }
}