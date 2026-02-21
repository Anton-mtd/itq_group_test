package ru.skomorokhin.documentoperator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skomorokhin.documentoperator.dto.DocWithHistory;
import ru.skomorokhin.documentoperator.dto.DocumentDto;
import ru.skomorokhin.documentoperator.dto.DocumentFilter;
import ru.skomorokhin.documentoperator.dto.PageResponse;
import ru.skomorokhin.documentoperator.exception.BusinessException;
import ru.skomorokhin.documentoperator.exception.ErrorCode;
import ru.skomorokhin.documentoperator.mapper.DocumentMapper;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.repository.ApprovalRegistryRepository;
import ru.skomorokhin.documentoperator.repository.DocumentHistoryRepository;
import ru.skomorokhin.documentoperator.repository.DocumentRepository;
import ru.skomorokhin.documentoperator.repository.DocumentSpecification;
import ru.skomorokhin.documentoperator.service.DocumentService;
import ru.skomorokhin.documentoperator.service.DocumentWorkflowService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static ru.skomorokhin.documentoperator.exception.ErrorCode.DOCUMENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository historyRepository;
    private final ApprovalRegistryRepository registryRepository;
    private final DocumentWorkflowService documentWorkflowService;
    private final DocumentMapper documentMapper;

    private static final String PREFIX = "DOC-";

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
    public DocWithHistory getDocument(Long id) {
        Document document = documentRepository.findDocumentById(id)
                .orElseThrow(() -> new BusinessException(DOCUMENT_NOT_FOUND));

        return documentMapper.toDto(document);
    }

    @Override
    public List<DocWithHistory> getDocuments(List<Long> ids) {

        return documentRepository.findAllByIdIn(ids).stream()
                .map(documentMapper::toDto)
                .toList();
    }

    @Override
    public PageResponse<DocWithHistory> getDocumentsByFilter(DocumentFilter filter, Pageable pageable) {
        Page<Document> documents = documentRepository.findAll(buildSpecification(filter), pageable);

        Page<DocWithHistory> page = documents.map(documentMapper::toDto);

        return PageResponse.<DocWithHistory>builder()
                .items(page.getContent())
                .meta(PageResponse.MetaPageInfo.builder()
                        .page(page.getNumber())
                        .itemsCount(page.getTotalPages())
                        .pageSize(page.getSize())
                        .totalPages(page.getTotalPages())
                        .build())
                .build();
    }

    @Override
    public List<DocumentDto> submitDocuments(List<Long> ids, String performedBy) {

        List<DocumentDto> results = new ArrayList<>();

        for (Long id : ids) {
            try {
                documentWorkflowService.submitDocument(id, performedBy);
                results.add(new DocumentDto(id, true, "Submitted successfully"));
            } catch (BusinessException e) {
                results.add(new DocumentDto(
                        id,
                        false,
                        e.getErrorCode().getMessage()
                ));
            } catch (Exception e) {
                results.add(new DocumentDto(
                        id,
                        false,
                        ErrorCode.INTERNAL_ERROR.getMessage()
                ));
            }
        }

        return results;
    }

    @Override
    public List<DocumentDto> approveDocuments(List<Long> ids, String performedBy) {

        List<DocumentDto> results = new ArrayList<>();

        for (Long id : ids) {
            try {
                documentWorkflowService.approveDocument(id, performedBy);
                results.add(new DocumentDto(id, true, "Approved successfully"));
            } catch (BusinessException e) {
                results.add(new DocumentDto(
                        id,
                        false,
                        e.getErrorCode().getMessage()
                ));
            } catch (Exception e) {
                results.add(new DocumentDto(
                        id,
                        false,
                        ErrorCode.INTERNAL_ERROR.getMessage()
                ));
            }
        }

        return results;
    }

    @Override
    public Map<String, Object> checkConcurrentApproval(
            Long documentId,
            int threads,
            int attempts
    ) {

        if (threads <= 0 || attempts <= 0) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Threads and attempts must be positive numbers"
            );
        }

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Future<Void>> futures = new ArrayList<>();

        try {

            for (int i = 0; i < attempts; i++) {
                futures.add(
                        executor.submit(() -> {
                            documentWorkflowService.approveDocument(
                                    documentId,
                                    "concurrent_user"
                            );
                            return null;
                        })
                );
            }

            int success = 0;
            int conflict = 0;
            int failed = 0;

            for (Future<Void> future : futures) {
                try {
                    future.get();
                    success++;
                } catch (ExecutionException ex) {

                    Throwable cause = ex.getCause();

                    if (cause instanceof BusinessException be) {

                        if (be.getErrorCode() == ErrorCode.INVALID_STATUS) {
                            conflict++;
                        } else {
                            failed++;
                        }

                    } else {
                        failed++;
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    failed++;
                }
            }

            Document doc = documentRepository.findById(documentId)
                    .orElse(null);

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("conflict", conflict);
            result.put("failed", failed);
            result.put("finalStatus", doc != null ? doc.getStatus() : null);

            return result;

        } finally {
            executor.shutdown();
        }
    }

    @Override
    @Transactional
    public void saveAll(List<Document> docs) {
        documentRepository.saveAll(docs);
    }

    @Override
    public String generateDocumentNumber() {
        return PREFIX + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private Specification<Document> buildSpecification(DocumentFilter filter) {
        return Specification.where(DocumentSpecification.idsIn(filter.getIds()))
                .and(DocumentSpecification.documentNumberContains(filter.getDocumentNumber()))
                .and(DocumentSpecification.authorContains(filter.getAuthor()))
                .and(DocumentSpecification.statusIn(filter.getStatus()))
                .and(DocumentSpecification.createdFrom(filter.getCreatedFrom()))
                .and(DocumentSpecification.createdTo(filter.getCreatedTo()));
    }
}
