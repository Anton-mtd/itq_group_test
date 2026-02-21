package ru.skomorokhin.documentoperator.config;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "document-generator")
public class DocumentGeneratorProperties {

    @Min(1)
    private int totalDocuments;

    private String authorPrefix;

    private String titlePrefix;

    @Min(1)
    private int batchSize;
}
