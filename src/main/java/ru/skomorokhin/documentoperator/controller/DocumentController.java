package ru.skomorokhin.documentoperator.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.skomorokhin.documentoperator.dto.DocWithHistory;
import ru.skomorokhin.documentoperator.dto.DocumentDto;
import ru.skomorokhin.documentoperator.dto.DocumentFilter;
import ru.skomorokhin.documentoperator.dto.PageResponse;
import ru.skomorokhin.documentoperator.exception.BusinessException;
import ru.skomorokhin.documentoperator.exception.ErrorCode;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.service.DocumentService;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<Document> createDocument(
            @RequestParam String author,
            @RequestParam String title
    ) {
        Document document = documentService.createDocument(author, title);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }

    @GetMapping("/{id}")
    public DocWithHistory getDocument(@PathVariable Long id) {

        return documentService.getDocument(id);
    }

    @PostMapping("/by-ids")
    public List<DocWithHistory> getDocumentsByIds(
            @RequestBody List<Long> ids) {

        return documentService.getDocuments(ids);
    }

    @PostMapping("/filter-by-ids")
    public PageResponse<DocWithHistory> getDocumentsByFilter(
            @RequestBody DocumentFilter filter,
            @PageableDefault(sort = "documentNumber", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return documentService.getDocumentsByFilter(filter, pageable);
    }

    @PostMapping("/submit")
    public List<DocumentDto> submitDocuments(
            @RequestBody List<Long> ids,
            @RequestParam String performedBy
    ) {
        if (ids == null || ids.isEmpty() || ids.size() > 1000) {
            throw new BusinessException(
                    ErrorCode.VALIDATION_ERROR,
                    "Ids list must contain from 1 to 1000 elements"
            );
        }

        return documentService.submitDocuments(ids, performedBy);
    }

    @PostMapping("/approve")
    public List<DocumentDto> approveDocuments(
            @RequestBody List<Long> ids,
            @RequestParam String performedBy
    ) {
        if (ids == null || ids.isEmpty() || ids.size() > 1000) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Ids list must contain from 1 to 1000 elements");
        }

        return documentService.approveDocuments(ids, performedBy);
    }

    @PostMapping("/check-concurrent")
    public Map<String, Object> checkConcurrentApproval(
            @RequestParam Long documentId,
            @RequestParam int threads,
            @RequestParam int attempts
    ) {
        return documentService.checkConcurrentApproval(documentId, threads, attempts);
    }

}
