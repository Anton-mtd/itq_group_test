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
public class ApproveWorker {

    private final DocumentRepository documentRepository;
    private final DocumentService documentService;

    private final int batchSize;

    public ApproveWorker(DocumentRepository documentRepository,
                         DocumentService documentService,
                         @Value("${document-operator.batchSize}") int batchSize) {
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${document-operator.worker.approve-interval-ms}")
    public void run() {
        List<Document> submitted = documentRepository.findByStatus(DocumentStatus.SUBMITTED)
                .stream()
                .limit(batchSize)
                .collect(Collectors.toList());

        if (submitted.isEmpty()) return;

        List<Long> ids = submitted.stream().map(Document::getId).collect(Collectors.toList());
        List<DocumentResultDto> results = documentService.approveDocuments(ids, "approve-worker");

        results.forEach(r -> System.out.println("[ApproveWorker] " + r.getDocumentId() + " -> " + r.getMessage()));
    }
}
