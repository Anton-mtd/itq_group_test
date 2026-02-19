package ru.skomorokhin.documentoperator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skomorokhin.documentoperator.dto.DocumentResultDto;
import ru.skomorokhin.documentoperator.model.entity.ApprovalRegistry;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.entity.DocumentHistory;
import ru.skomorokhin.documentoperator.model.enums.DocumentAction;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.repository.ApprovalRegistryRepository;
import ru.skomorokhin.documentoperator.repository.DocumentHistoryRepository;
import ru.skomorokhin.documentoperator.repository.DocumentRepository;
import ru.skomorokhin.documentoperator.service.DocumentService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository historyRepository;
    private final ApprovalRegistryRepository registryRepository;

    @Override
    public Document createDocument(String author, String title) {
        Document doc = new Document();
        doc.setAuthor(author);
        doc.setTitle(title);
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setDocumentNumber(generateDocumentNumber());
        return documentRepository.save(doc);
    }

    @Override
    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found"));
    }

    @Override
    public Page<Document> getDocuments(List<Long> ids, Pageable pageable) {
        List<Document> docs = documentRepository.findAllById(ids);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), docs.size());
        List<Document> content = docs.subList(start, end);
        return new PageImpl<>(content, pageable, docs.size());
    }

    @Override
    public List<DocumentResultDto> submitDocuments(List<Long> ids, String performedBy) {
        List<DocumentResultDto> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                DocumentResultDto result = submitDocument(id, performedBy);
                results.add(result);
            } catch (Exception e) {
                results.add(new DocumentResultDto(id, false, e.getMessage()));
            }
        }
        return results;
    }

    @Transactional
    public DocumentResultDto submitDocument(Long id, String performedBy) {
        Optional<Document> optional = documentRepository.findById(id);
        if (optional.isEmpty()) {
            return new DocumentResultDto(id, false, "Not found");
        }
        Document doc = optional.get();
        if (doc.getStatus() != DocumentStatus.DRAFT) {
            return new DocumentResultDto(id, false, "Conflict: not in DRAFT status");
        }

        doc.setStatus(DocumentStatus.SUBMITTED);
        documentRepository.save(doc);

        DocumentHistory history = new DocumentHistory();
        history.setDocument(doc);
        history.setAction(DocumentAction.SUBMIT);
        history.setPerformedBy(performedBy);
        historyRepository.save(history);

        return new DocumentResultDto(id, true, "Submitted successfully");
    }

    @Override
    public List<DocumentResultDto> approveDocuments(List<Long> ids, String performedBy) {
        List<DocumentResultDto> results = new ArrayList<>();
        for (Long id : ids) {
            try {
                DocumentResultDto result = approveDocument(id, performedBy);
                results.add(result);
            } catch (Exception e) {
                results.add(new DocumentResultDto(id, false, e.getMessage()));
            }
        }
        return results;
    }

    @Transactional
    public DocumentResultDto approveDocument(Long id, String performedBy) {
        Optional<Document> optional = documentRepository.findById(id);
        if (optional.isEmpty()) {
            return new DocumentResultDto(id, false, "Not found");
        }
        Document doc = optional.get();
        if (doc.getStatus() != DocumentStatus.SUBMITTED) {
            return new DocumentResultDto(id, false, "Conflict: not in SUBMITTED status");
        }

        doc.setStatus(DocumentStatus.APPROVED);
        documentRepository.save(doc);

        DocumentHistory history = new DocumentHistory();
        history.setDocument(doc);
        history.setAction(DocumentAction.APPROVE);
        history.setPerformedBy(performedBy);
        historyRepository.save(history);

        ApprovalRegistry registry = new ApprovalRegistry();
        registry.setDocument(doc);
        registry.setApprovedBy(performedBy);
        try {
            registryRepository.save(registry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register approval in registry");
        }

        return new DocumentResultDto(id, true, "Approved successfully");
    }

    @Override
    public Page<Document> searchDocuments(DocumentStatus status, String author, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        List<Document> docs = documentRepository.findAll().stream()
                .filter(d -> status == null || d.getStatus() == status)
                .filter(d -> author == null || d.getAuthor().equalsIgnoreCase(author))
                .filter(d -> from == null || !d.getCreatedAt().isBefore(from))
                .filter(d -> to == null || !d.getCreatedAt().isAfter(to))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), docs.size());
        List<Document> content = docs.subList(start, end);
        return new PageImpl<>(content, pageable, docs.size());
    }

    @Override
    public Map<String, Object> checkConcurrentApproval(Long documentId, int threads, int attempts) {
        Map<String, Object> result = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<DocumentResultDto>> futures = new ArrayList<>();

        for (int i = 0; i < attempts; i++) {
            futures.add(executor.submit(() -> approveDocument(documentId, "concurrent_user")));
        }

        int success = 0;
        int conflict = 0;
        int failed = 0;
        for (Future<DocumentResultDto> f : futures) {
            try {
                DocumentResultDto r = f.get();
                if (r.isSuccess()) success++;
                else if (r.getMessage().contains("Conflict")) conflict++;
                else failed++;
            } catch (Exception e) {
                failed++;
            }
        }

        Document doc = documentRepository.findById(documentId).orElse(null);
        result.put("success", success);
        result.put("conflict", conflict);
        result.put("failed", failed);
        result.put("finalStatus", doc != null ? doc.getStatus() : null);

        executor.shutdown();
        return result;
    }

    private String generateDocumentNumber() {
        return "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
