package ru.skomorokhin.documentoperator.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.repository.DocumentRepository;
import ru.skomorokhin.documentoperator.service.DocumentService;

@Slf4j
public class SubmitWorker {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final int batchSize;

    public SubmitWorker(
            DocumentRepository documentRepository,
            DocumentService documentService,
            int batchSize
    ) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${document-operator.worker.submit-interval-ms}")
    public void run() {
        var drafts = documentRepository.findByStatus(DocumentStatus.DRAFT)
                .stream()
                .limit(batchSize)
                .toList();

        if (drafts.isEmpty()) return;

        documentService.submitDocuments(
                drafts.stream().map(Document::getId).toList(),
                "submit-worker"
        ).forEach(r -> log.info("[SubmitWorker] {} -> {}", r.getDocumentId(), r.getMessage()));
    }
}
