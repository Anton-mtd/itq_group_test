package ru.skomorokhin.documentoperator.service;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import ru.skomorokhin.documentoperator.dto.DocWithHistory;
import ru.skomorokhin.documentoperator.dto.DocumentDto;
import ru.skomorokhin.documentoperator.dto.DocumentFilter;
import ru.skomorokhin.documentoperator.dto.PageResponse;
import ru.skomorokhin.documentoperator.model.entity.Document;

import java.util.List;
import java.util.Map;

public interface DocumentService {

    Document createDocument(String author, String title);

    DocWithHistory getDocument(Long id);

    List<DocWithHistory> getDocuments(List<Long> ids);

    PageResponse<DocWithHistory> getDocumentsByFilter(DocumentFilter filter, Pageable pageable);

    List<DocumentDto> submitDocuments(List<Long> ids, String performedBy);

    List<DocumentDto> approveDocuments(List<Long> ids, String performedBy);

    Map<String, Object> checkConcurrentApproval(Long documentId, int threads, int attempts);

    String generateDocumentNumber();

    @Transactional
    void saveAll(List<Document> docs);

}
