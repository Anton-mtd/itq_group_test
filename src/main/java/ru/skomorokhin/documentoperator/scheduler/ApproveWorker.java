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
        long remainingBefore = documentRepository.countByStatus(DocumentStatus.SUBMITTED);
        if (remainingBefore == 0) return;

        var submitted = documentRepository.findByStatus(DocumentStatus.SUBMITTED)
                .stream()
                .limit(batchSize)
                .toList();

        if (submitted.isEmpty()) return;

        long startBatch = System.currentTimeMillis();
        var ids = submitted.stream().map(Document::getId).toList();
        documentService.approveDocuments(ids, "approve-worker")
                .forEach(r -> log.debug("[ApproveWorker] {} -> {}", r.getDocumentId(), r.getMessage()));

        long batchMs = System.currentTimeMillis() - startBatch;
        long remainingAfter = documentRepository.countByStatus(DocumentStatus.SUBMITTED);
        long processed = submitted.size();
        log.info("Отправка на утверждение: пачка {} док., время: {} мс; осталось SUBMITTED: {}", processed, batchMs, remainingAfter);
    }
}
