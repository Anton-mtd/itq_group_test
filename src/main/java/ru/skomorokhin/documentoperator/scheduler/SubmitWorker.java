package ru.skomorokhin.documentoperator.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.skomorokhin.documentoperator.dto.DocumentResultDto;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.repository.DocumentRepository;
import ru.skomorokhin.documentoperator.service.DocumentService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SubmitWorker {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    private final int batchSize;

    public SubmitWorker(DocumentRepository documentRepository,
                        DocumentService documentService,
                        @Value("${document-operator.batchSize}") int batchSize) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${document-operator.worker.submit-interval-ms}")
    public void run() {
        List<Document> drafts = documentRepository.findByStatus(DocumentStatus.DRAFT)
                .stream()
                .limit(batchSize)
                .collect(Collectors.toList());

        if (drafts.isEmpty()) return;

        List<Long> ids = drafts.stream().map(Document::getId).collect(Collectors.toList());
        List<DocumentResultDto> results = documentService.submitDocuments(ids, "submit-worker");

        results.forEach(r -> System.out.println("[SubmitWorker] " + r.getDocumentId() + " -> " + r.getMessage()));
    }
}
