package ru.skomorokhin.documentoperator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.skomorokhin.documentoperator.repository.DocumentRepository;
import ru.skomorokhin.documentoperator.scheduler.ApproveWorker;
import ru.skomorokhin.documentoperator.scheduler.SubmitWorker;
import ru.skomorokhin.documentoperator.service.DocumentService;

@Configuration
public class WorkerConfig {

    @Bean
    @ConditionalOnProperty(prefix = "document-operator.worker", name = "enable-submit", havingValue = "true")
    public SubmitWorker submitWorker(
            DocumentRepository repo,
            DocumentService service,
            @Value("${document-operator.batchSize}") int batchSize
    ) {
        return new SubmitWorker(repo, service, batchSize);
    }

    @Bean
    @ConditionalOnProperty(prefix = "document-operator.worker", name = "enable-approve", havingValue = "true")
    public ApproveWorker approveWorker(
            DocumentRepository repo,
            DocumentService service,
            @Value("${document-operator.batchSize}") int batchSize
    ) {
        return new ApproveWorker(repo, service, batchSize);
    }
}
