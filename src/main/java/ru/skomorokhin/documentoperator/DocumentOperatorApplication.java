package ru.skomorokhin.documentoperator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class DocumentOperatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(DocumentOperatorApplication.class, args);
    }
}
