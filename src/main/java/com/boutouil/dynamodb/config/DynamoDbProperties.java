package com.boutouil.dynamodb.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Validated
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
    private boolean ddlEnabled = false;
}