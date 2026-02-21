package ru.skomorokhin.documentoperator.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.repository.DocumentRepository;
import ru.skomorokhin.documentoperator.service.DocumentService;

@Slf4j
public class ApproveWorker {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final int batchSize;

    public ApproveWorker(
            DocumentRepository documentRepository,
            DocumentService documentService,
            int batchSize
    ) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${document-operator.worker.approve-interval-ms}")
    public void run() {
        var submitted = documentRepository.findByStatus(DocumentStatus.SUBMITTED)
                .stream()
                .limit(batchSize)
                .toList();

        if (submitted.isEmpty()) return;

        documentService.approveDocuments(
                submitted.stream().map(Document::getId).toList(),
                "approve-worker"
        ).forEach(r -> log.info("[ApproveWorker] " + r.getDocumentId() + " -> " + r.getMessage()));
    }
}
