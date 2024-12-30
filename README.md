# 🚀 **Spring Cloud DynamoDB Boilerplate: Simplify Table Creation and Backend Scalability**

Integrating **AWS DynamoDB** with **Spring Boot** can be challenging, from manual table creation to managing
configurations across environments. This guide provides a ready-to-use **Spring Cloud DynamoDB Boilerplate** to
streamline:

✅ **Automatic Table Creation**
✅ **Environment-Specific Configuration**
✅ **Efficient CRUD Operations**
✅ **Real-World Integration Use Cases**

Using a **Product Management Example**, you'll learn to build scalable, efficient backend systems with minimal
boilerplate.

---

## **1. Overview**

### **1.1 Why Use DynamoDB with Spring Cloud?**

**AWS DynamoDB** offers:

- **Fully Managed Service:** No operational overhead.
- **High Performance:** Single-digit millisecond response times.
- **Flexible Data Model:** Supports key-value and document schemas.

**Challenges in Integration:**

- **Manual Table Creation:** Repetitive and error-prone.
- **Environment Differences:** Local vs cloud inconsistencies.
- **Data Mapping:** Complex schema mapping.

### **1.2 What You’ll Learn**

This guide covers:

- ✅ **Automatic Table Initialization**
- ✅ **Dynamic Environment Configuration**
- ✅ **Structured CRUD Operations**
- ✅ **Real-World Integration Use Cases**

By the end, you'll have a solid foundation for building scalable backend systems with **Spring Cloud DynamoDB**.

---

## **2. Project Setup**

### **2.1 Initialize the Project**

Using [Spring Initializr](https://start.spring.io/#!type=gradle-project&language=java&platformVersion=3.4.1&packaging=jar&jvmVersion=21&groupId=com.example&artifactId=spring-cloud-dynamodb-boilerplate&name=spring-cloud-dynamodb-boilerplate&description=Spring%20Cloud%20DynamoDB%20Starter%3A%20Auto%20Table%20Creation%20for%20Seamless%20Backend%20Development&packageName=com.example.dynamodb&dependencies=lombok,docker-compose,web,validation,actuator,cloud-starter),
ensure the following dependencies:

- **AWS SDK**
- **Spring Web**
- **Spring Cloud AWS DynamoDB Starter**
- **Docker Compose**

### **2.2 Dependencies**

*Adding DynamoDB Dependencies to Spring Boot Project*

The same configuration is available for both Maven and Gradle. Here's how to set it up in Gradle:

```groovy
dependencies {
    // AWS DynamoDB Dependencies
    implementation 'io.awspring.cloud:spring-cloud-aws-starter-dynamodb'
    implementation 'software.amazon.awssdk:dynamodb-enhanced'
}

dependencyManagement {
    imports {
        mavenBom 'io.awspring.cloud:spring-cloud-aws-dependencies:latest.release'
        mavenBom 'software.amazon.awssdk:bom:latest.release'
    }
}
```

This configuration includes:

1. The platform BOMs to manage AWS dependency versions
2. The Spring Cloud AWS DynamoDB starter
3. The DynamoDB Enhanced Client for improved DynamoDB operations

### **2.3 LocalStack Setup**

```yaml
  localstack:
    image: localstack/localstack:latest
    container_name: localstack
    ports:
      - "4566:4566"
    environment:
      - SERVICES=dynamodb
      - DOCKER_HOST=unix:///var/run/docker.sock
      - AWS_CBOR_DISABLE=1
    volumes:
      - "${LOCALSTACK_VOLUME_DIR:-./volume}:/var/lib/localstack"
      - "/var/run/docker.sock:/var/run/docker.sock"
      - "./.localstack:/etc/localstack/init/ready.d"  # ready hook
```

---

## **3. The Data Model**

### **3.1 Product Entity**

The `Product` class represents the core DynamoDB entity in our application. Each field is mapped to a DynamoDB table
attribute using **AWS SDK annotations**, providing fine control over how data is stored, updated, and retrieved.

### **Annotations Explained**

#### ✅ **@DynamoDbBean**

- **Purpose:** Marks the class as a DynamoDB entity.
- **Usability:** This is required for object-mapping in the **AWS SDK Enhanced Client**.
- **Behavior:** DynamoDB SDK identifies this class as a database table representation.

#### ✅ **@DynamoDbPartitionKey**

- **Purpose:** Defines the **primary key** for the table.
- **Usability:** Uniquely identifies each record in DynamoDB.
- **Behavior:** Required for querying and indexing records efficiently.

#### ✅ **@DynamoDbAutoGeneratedUuid**

- **Purpose:** Automatically generates a **UUID** for the partition key.
- **Usability:** Ensures each record has a unique identifier without manual intervention.

#### ✅ **@DynamoDbAttribute**

- **Purpose:** Maps a field to a DynamoDB attribute.
- **Usability:** Specifies the exact attribute name in DynamoDB.
- **Behavior:** Supports renaming Java field names to match DynamoDB schema conventions.

#### ✅ **@DynamoDbAutoGeneratedTimestampAttribute**

- **Purpose:** Automatically generates and updates timestamps.
- **Usability:** Useful for fields like `lastUpdatedDate` to track changes.

#### ✅ **@DynamoDbVersionAttribute**

- **Purpose:** Tracks the version of an entity for **optimistic locking**.
- **Usability:** Prevents concurrent updates from overwriting data accidentally.

#### ✅ **@DynamoDbAtomicCounter**

- **Purpose:** Auto-increments a numerical field (e.g., `sequenceNumber`).
- **Usability:** Ideal for counters, retry attempts, or versioning sequences.

#### ✅ **@DynamoDbIgnoreNulls**

- **Purpose:** Prevents null values from being written to DynamoDB.
- **Usability:** Reduces unnecessary null attributes in records.

#### ✅ **@DynamoDbIgnore**

- **Purpose:** Excludes a field or method from being stored in DynamoDB.
- **Usability:** Useful for non-persistent fields or static utility methods.

### **Placeholder:** Code snippet for `Product.java`.

**Summary of the Model:**

- `id`: Partition key with auto-generated UUID.
- `name`: Product name.
- `price`: Product price.
- `category`: Product category.
- `stockQuantity`: Tracks available stock.
- `lastUpdatedDate`: Auto-generated timestamp for updates.
- `version`: Optimistic locking version.
- `sequenceNumber`: Auto-incremented counter.
- `retryCount`: Tracks retry attempts.

---

## **4. CRUD Repository**

The **`ProductRepository`** is responsible for **managing interactions with DynamoDB** through the AWS Enhanced Client.
It encapsulates **CRUD operations**, ensuring clean separation from business logic.

### **CRUD Methods Explained**

#### ✅ **1. save(Product product)**

- **Purpose:** Persist a new product entity into DynamoDB.
- **Usability:** Used for **initial creation** of products.
- **Behavior:** If a record with the same `id` exists, it will **overwrite** it.

#### ✅ **2. update(Product product)**

- **Purpose:** Update an existing product entity.
- **Usability:** Ensures partial updates can be applied without rewriting the entire entity.
- **Behavior:** Resets the `version` field to avoid optimistic locking errors.

#### ✅ **3. findById(String id)**

- **Purpose:** Retrieve a single product by its **primary key**.
- **Usability:** Used for **lookup operations** where `id` uniquely identifies a product.
- **Behavior:** Returns an `Optional<Product>` to handle scenarios where the product may not exist.

#### ✅ **4. deleteById(String id)**

- **Purpose:** Remove a product from DynamoDB by its `id`.
- **Usability:** Used when **archiving or cleaning up** outdated records.
- **Behavior:** Ensures the item is permanently removed from the table.

#### ✅ **5. findAll()**

- **Purpose:** Retrieve all products in the DynamoDB table.
- **Usability:** Used for **batch queries** or administrative purposes.
- **Behavior:** Performs a `scan` operation, which can be resource-intensive on large tables.

### **Repository Workflow:**

1. **Create Product:** `save` persists a new product.
2. **Update Product:** `update` modifies existing product details.
3. **Fetch Product:** `findById` retrieves product details.
4. **Delete Product:** `deleteById` removes the product from the table.
5. **Fetch All Products:** `findAll` retrieves all products from the table.

### **Logging and Monitoring:**

Each method logs the operation performed and the associated product or identifier.

### **Placeholder:** Code snippet for `ProductRepository.java`.

---

## **5. Configuration Components

### 5.1 DynamoDB Properties

The `DynamoDbProperties` class manages configuration settings through Spring's `@ConfigurationProperties`.

```java

@ConfigurationProperties(prefix = "dynamodb.entity")
public class DynamoDbProperties {
    /**
     * Set of package names to scan for DynamoDB entities.
     * Defaults to scanning the common domain package if not specified.
     */
    @NotEmpty(message = "At least one package must be specified for entity scanning")
    private Set<String> packages;
    /**
     * Controls whether DDL operations (table deletion) are enabled.
     * When true, tables will be deleted during application shutdown.
     * When false, tables will be preserved.
     * Defaults to false for safety.
     */
    @Builder.Default
    private final boolean ddlEnabled = false;
}
```

#### 5.1.1 Configuration in `application.yml`**

Configuration properties for DynamoDB, including `ddl-enabled` and environment profiles.

```yml
## DYNAMODB ENV ##
DYNAMODB_REGION: eu-west-1
DYNAMODB_ACCESS_KEY: noop
DYNAMODB_SECRET_KEY: noop
DYNAMODB_ENDPOINT: http://localhost:4566

spring:
  cloud:
    aws:
      # General AWS Configuration
      region:
        static: eu-west-1
      # DynamoDB Configuration
      dynamodb:
        enabled: true
        region: ${DYNAMODB_REGION:${spring.cloud.aws.region.static}}
      # AWS Credentials Configuration (choose one approach)
      credentials:
        access-key: ${DYNAMODB_ACCESS_KEY:}
        secret-key: ${DYNAMODB_SECRET_KEY:}
      endpoint: ${DYNAMODB_ENDPOINT:}

# Custom DynamoDB entity scanning configuration
dynamodb:
  entity:
    ddl-enabled: false  # Set to true if you want tables to be dropped on shutdown
    packages:
      - com.boutouil.dynamodb.domain
```

#### 5.1.2 Key Features

1. **Package Scanning**
    - Required property to specify packages containing DynamoDB entities
    - Validates that at least one package is provided
    - Example: `com.example.dynamodb.domain`

2. **DDL Operations Control**
    - `ddlEnabled`: Controls table deletion during shutdown
    - Default: `false` (tables are preserved)
    - When `true`: Tables are automatically deleted during application shutdown

---

### 5.2 DynamoDB Table Initializer

The `DynamoDbTableInitializer` handles table lifecycle management.

#### 5.2.1 Key Capabilities

1. **Table Management**
   ```java
    /** Maximum number of attempts to wait for table activation */
    private static final int MAX_WAIT_ATTEMPTS = 60;
    /** Interval between table status checks */
    private static final Duration WAIT_INTERVAL = Duration.ofSeconds(2);
   ```

2. **Initialization Process**
    - Automatically creates tables if they don't exist
    - Waits for tables to become active (up to 2 minutes)
    - Tracks managed tables for cleanup
   ```java
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
   ```

3. **Table Status Monitoring**
    - Checks table existence
    - Monitors table status until active
    - Implements retry logic with configurable attempts
   ```java
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
      ```

4. **Cleanup Handling**
    - Controlled by `ddlEnabled` property
    - Safely removes tables during shutdown if enabled
    - Logs all cleanup operations
   ```java
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
   ```

#### 5.2.2 Error Handling

- Retries on table creation failures
- Graceful handling of resource not found
- Comprehensive logging of all operations

---

### 5.3 DynamoDB Configuration

The `DynamoDbConfiguration` class sets up the DynamoDB infrastructure.

#### 5.3.1 Core Components

1. **Enhanced Client Configuration**
   ```java
   @Bean
   @Primary
   public DynamoDbEnhancedClient dynamoDbEnhancedClient() {
       return DynamoDbEnhancedClient.builder()
           .extensions(
               AutoGeneratedUuidExtension.create(),
               VersionedRecordExtension.builder().build(),
               AutoGeneratedTimestampRecordExtension.create(),
               AtomicCounterExtension.builder().build())
           .build();
   }
   ```

2. **Automatic Extensions**
    - UUID Generation
    - Version Control
    - Timestamp Management
    - Atomic Counters

3. **Entity Scanning**
    - Scans specified packages for `@DynamoDbBean` annotations
    - Extracts table names from entities
    - Builds table schemas automatically

4. **Table Name Resolution**
    - Attempts to get table name from static method
    - Falls back to lowercase class name
    - Logs table discovery process

## **6. Integration Use Case**

### **6.1 Product Data Loader**

The **`ProductDataLoader`** serves as an integration tool to ensure seamless interaction between your **Spring Boot
application** and **DynamoDB**. It validates core operations and ensures system stability.

**Key Responsibilities:**

- **CRUD Operations Validation:** Verify the correctness of `save`, `findById`, `update`, and `delete` operations.
- **Sample Data Population:** Preload the DynamoDB table with sample product records.
- **Connection Stability:** Ensure a reliable connection between the application and DynamoDB.

### **6.2 Integration Workflow**

1. **Initialize Sample Data:** Load initial product records into the DynamoDB table.
2. **Perform CRUD Operations:** Test `save`, `findById`, `update`, and `delete` methods.
3. **Log Verification:** Review application logs to confirm operations have executed successfully.
4. **Database Inspection:** Check the DynamoDB table for accurate data persistence.

### **6.3 Running the Application**

Follow these steps to start and verify your application:

1. **Run the Application:**

```bash
./gradlew bootRun
```

1. **Automatic Table Creation:**
    - The application will detect Java model classes and auto-create corresponding DynamoDB tables.
    - Tables will remain in a 'creating' state until they are fully active.

2. **Application Ready State:**
    - Once the tables are active, the application will complete initialization.
    - `ProductDataLoader` will execute integration workflows automatically.

3. **Verification:**
    - Review logs for output validation.
   ```text
   d.config.DynamoDbConfiguration       : Found DynamoDB entity: com.boutouil.dynamodb.domain.Product with table name: product
   d.config.DynamoDbConfiguration       : Found 1 DynamoDB entities in specified packages
   d.config.DynamoDbTableInitializer    : Creating DynamoDB table: product
   d.config.DynamoDbTableInitializer    : Waiting for table product to become active...
   d.config.DynamoDbTableInitializer    : Table product is now active
   d.config.DynamoDbTableInitializer    : Successfully created DynamoDB table: product
   d.config.DynamoDbTableInitializer    : Initialized 1 DynamoDB tables
   ```
    - Inspect the DynamoDB table to verify sample product data is present.
   ```text
   dynamodb.data.ProductDataLoader      : Loading sample product data...
   dynamodb.dao.ProductRepository       : Saving product: Product(id=null, name=Laptop, price=1299.99, category=Electronics, stockQuantity=50, lastUpdatedDate=null, version=null, sequenceNumber=null, retryCount=null)
   dynamodb.dao.ProductRepository       : Saving product: Product(id=null, name=Smartphone, price=799.99, category=Electronics, stockQuantity=100, lastUpdatedDate=null, version=null, sequenceNumber=null, retryCount=null)
   dynamodb.dao.ProductRepository       : Saving product: Product(id=null, name=Coffee Maker, price=99.99, category=Home Appliances, stockQuantity=30, lastUpdatedDate=null, version=null, sequenceNumber=null, retryCount=null)
   dynamodb.dao.ProductRepository       : Saving product: Product(id=null, name=Running Shoes, price=129.99, category=Sports, stockQuantity=75, lastUpdatedDate=null, version=null, sequenceNumber=null, retryCount=null)
   dynamodb.data.ProductDataLoader      : Sample products loaded successfully
   dynamodb.dao.ProductRepository       : Finding all products
   dynamodb.data.ProductDataLoader      : Found 4 products
   dynamodb.dao.ProductRepository       : Finding products by category: Electronics
   dynamodb.data.ProductDataLoader      : Found 2 electronics products
   dynamodb.dao.ProductRepository       : Finding products by price range: 100.0 - 1000.0
   dynamodb.data.ProductDataLoader      : Found 2 products in price range $100-$1000
   dynamodb.dao.ProductRepository       : Finding low stock products below threshold: 40
   dynamodb.data.ProductDataLoader      : Found 1 products with low stock
   dynamodb.dao.ProductRepository       : Updating stock for product id: 1f927ab8-4ec4-45cb-b8d5-6191e746b373 with quantity: 25
   dynamodb.dao.ProductRepository       : Finding product by id: 1f927ab8-4ec4-45cb-b8d5-6191e746b373
   dynamodb.dao.ProductRepository       : Updating product: Product(id=1f927ab8-4ec4-45cb-b8d5-6191e746b373, name=Laptop, price=1299.99, category=Electronics, stockQuantity=25, lastUpdatedDate=2024-12-30T11:18:29.753457Z, version=1, sequenceNumber=1, retryCount=0)
   dynamodb.data.ProductDataLoader      : Updated stock for product: 1f927ab8-4ec4-45cb-b8d5-6191e746b373
   dynamodb.dao.ProductRepository       : Incrementing retry count for product id: 1f927ab8-4ec4-45cb-b8d5-6191e746b373
   dynamodb.dao.ProductRepository       : Finding product by id: 1f927ab8-4ec4-45cb-b8d5-6191e746b373
   dynamodb.dao.ProductRepository       : Updating product: Product(id=1f927ab8-4ec4-45cb-b8d5-6191e746b373, name=Laptop, price=1299.99, category=Electronics, stockQuantity=50, lastUpdatedDate=2024-12-30T11:18:29.753457Z, version=1, sequenceNumber=1, retryCount=1)
   dynamodb.data.ProductDataLoader      : Incremented retry count for product: 1f927ab8-4ec4-45cb-b8d5-6191e746b373
   ```

By following these steps, you ensure both your configuration and data operations are working seamlessly.

---

## **7. Conclusion**

### ✅ **What We’ve Built**

- **Custom DynamoDB Setup:** Tailored configuration for scalability.
- **Robust Data Model:** Clear mappings using DynamoDB annotations.
- **Efficient CRUD Repository:** Simplified data access layer.
- **Integration Testing:** Reliable validation with `ProductDataLoader`.

---

## **8. Next Steps**

1. **Integrate DynamoDB Accelerator (DAX):** Enhance read performance with in-memory caching.
2. **Explore DynamoDB as a Vector Database:** Use DynamoDB for vector-based AI/ML use cases.
3. **Enable DynamoDB Streams:** Build real-time workflows.
4. **Implement Query-Based Filtering:** Enhance data retrieval efficiency.

---

## **9. Resources and References**

- [Code Source GitHub](https://github.com/realboutouil/spring-cloud-dynamodb-boilerplate)
- [AWS DynamoDB Documentation](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html)
- [Spring Cloud AWS DynamoDB Starter](https://docs.awspring.io/spring-cloud-aws/docs/3.2.0/reference/html/index.html#spring-cloud-aws-dynamoDb)
- [AWS LocalStack Setup Guide](https://localstack.cloud/)
- [Spring Initializr](https://start.spring.io/)
- [DynamoDB Enhanced Client Documentation](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/dynamodb-en-client.html)

---

**Happy Coding! 🚀✨**