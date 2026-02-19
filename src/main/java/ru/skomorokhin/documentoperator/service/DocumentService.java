package ru.skomorokhin.documentoperator.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.skomorokhin.documentoperator.dto.DocumentResultDto;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface DocumentService {

    Document createDocument(String author, String title);

    Document getDocument(Long id);

    Page<Document> getDocuments(List<Long> ids, Pageable pageable);

    List<DocumentResultDto> submitDocuments(List<Long> ids, String performedBy);

    List<DocumentResultDto> approveDocuments(List<Long> ids, String performedBy);

    Page<Document> searchDocuments(DocumentStatus status,
                                   String author,
                                   LocalDateTime from,
                                   LocalDateTime to,
                                   Pageable pageable);

    Map<String, Object> checkConcurrentApproval(Long documentId, int threads, int attempts);
}
