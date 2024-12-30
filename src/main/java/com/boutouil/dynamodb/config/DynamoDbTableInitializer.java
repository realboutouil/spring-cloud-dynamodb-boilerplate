package com.boutouil.dynamodb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle of DynamoDB tables in a Spring Boot application.
 * This initializer automatically creates tables for entity classes during startup
 * and optionally cleans them up during shutdown.
 *
 * <p>The initializer implements both {@link InitializingBean} for table creation
 * and {@link DisposableBean} for cleanup operations.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Automatic table creation for entity classes</li>
 *   <li>Table status monitoring with configurable retry attempts</li>
 *   <li>Optional table cleanup during application shutdown</li>
 *   <li>Comprehensive error handling and logging</li>
 * </ul>
 *
 * @see InitializingBean
 * @see DisposableBean
 */
@Slf4j
public class DynamoDbTableInitializer implements InitializingBean, DisposableBean {

    /** Maximum number of attempts to wait for table activation */
    private static final int MAX_WAIT_ATTEMPTS = 60;

    /** Interval between table status checks */
    private static final Duration WAIT_INTERVAL = Duration.ofSeconds(2);

    private final DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private final DynamoDbClient dynamoDbClient;
    private final Map<String, Class<?>> entityClasses;
    private final Map<String, TableSchema<?>> managedTables = new ConcurrentHashMap<>();
    private final boolean ddlEnabled;

    /**
     * Constructs a new DynamoDbTableInitializer.
     *
     * @param dynamoDbEnhancedClient The enhanced client for DynamoDB operations
     * @param dynamoDbClient The base DynamoDB client for table operations
     * @param entityClasses Map of table names to their corresponding entity classes
     * @param ddlEnabled Flag indicating whether DDL operations are enabled
     */
    public DynamoDbTableInitializer(DynamoDbEnhancedClient dynamoDbEnhancedClient,
                                    DynamoDbClient dynamoDbClient, Map<String, Class<?>> entityClasses,
                                    boolean ddlEnabled) {
        this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
        this.dynamoDbClient = dynamoDbClient;
        this.entityClasses = entityClasses;
        this.ddlEnabled = ddlEnabled;
    }

    /**
     * Initializes DynamoDB tables for all registered entity classes.
     * This method is called automatically after the bean properties are set.
     *
     * @throws RuntimeException if table creation or verification fails
     */
    @Override
    public void afterPropertiesSet() {
        entityClasses.forEach((tableName, entityClass) -> {
            try {
                TableSchema<?> schema = TableSchema.fromBean(entityClass);
                if (!tableExists(tableName)) {
                    log.info("Creating DynamoDB table: {}", tableName);
                    dynamoDbEnhancedClient.table(tableName, schema).createTable();
                    waitForTableToBecomeActive(tableName);
                    log.info("Successfully created DynamoDB table: {}", tableName);
                } else {
                    log.info("Table {} already exists, tracking for management", tableName);
                }
                managedTables.put(tableName, schema);
            } catch (Exception e) {
                log.error("Failed to create/verify table {}: {}", tableName, e.getMessage(), e);
                throw new RuntimeException("Failed to initialize DynamoDB table: " + tableName, e);
            }
        });

        log.info("Initialized {} DynamoDB tables", managedTables.size());
    }

    /**
     * Checks if a table exists in DynamoDB.
     *
     * @param tableName the name of the table to check
     * @return true if the table exists, false otherwise
     */
    private boolean tableExists(String tableName) {
        try {
            DescribeTableRequest request = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();
            dynamoDbClient.describeTable(request);
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }

    /**
     * Waits for a table to become active, with retry logic.
     * Will attempt to check table status up to MAX_WAIT_ATTEMPTS times,
     * with WAIT_INTERVAL between attempts.
     *
     * @param tableName the name of the table to monitor
     * @throws RuntimeException if the table does not become active within the maximum attempts
     */
    private void waitForTableToBecomeActive(String tableName) {
        log.info("Waiting for table {} to become active...", tableName);
        int attempts = 0;

        while (attempts < MAX_WAIT_ATTEMPTS) {
            try {
                DescribeTableRequest request = DescribeTableRequest.builder()
                        .tableName(tableName)
                        .build();
                DescribeTableResponse response = dynamoDbClient.describeTable(request);
                TableStatus status = response.table().tableStatus();

                if (status == TableStatus.ACTIVE) {
                    log.info("Table {} is now active", tableName);
                    return;
                }

                log.debug("Table {} status: {}, waiting...", tableName, status);
                Thread.sleep(WAIT_INTERVAL.toMillis());
                attempts++;
            } catch (Exception e) {
                log.warn("Error while waiting for table {}: {}", tableName, e.getMessage());
                attempts++;
            }
        }

        throw new RuntimeException("Timeout waiting for table " + tableName + " to become active");
    }

    /**
     * Cleans up DynamoDB tables during application shutdown if DDL operations are enabled.
     * This method is called automatically when the application context is destroyed.
     *
     * <p>If ddlEnabled is false, this method will log a message and take no action.</p>
     */
    @Override
    public void destroy() {
        if (!ddlEnabled) {
            log.info("DDL operations are disabled. Skipping table deletion.");
            return;
        }

        managedTables.forEach((tableName, schema) -> {
            try {
                if (tableExists(tableName)) {
                    log.info("Deleting DynamoDB table: {}", tableName);
                    dynamoDbEnhancedClient.table(tableName, schema).deleteTable();
                    log.info("Successfully deleted DynamoDB table: {}", tableName);
                }
            } catch (Exception e) {
                log.warn("Failed to delete table {}: {}", tableName, e.getMessage());
            }
        });
    }
}