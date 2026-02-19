package ru.skomorokhin.documentoperator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.skomorokhin.documentoperator.dto.DocumentResultDto;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.service.DocumentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public Document createDocument(@RequestParam String author, @RequestParam String title) {
        return documentService.createDocument(author, title);
    }

    @GetMapping("/{id}")
    public Document getDocument(@PathVariable Long id) {
        return documentService.getDocument(id);
    }

    @GetMapping
    public Page<Document> getDocuments(@RequestParam List<Long> ids,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        return documentService.getDocuments(ids, PageRequest.of(page, size));
    }

    @PostMapping("/submit")
    public List<DocumentResultDto> submitDocuments(@RequestBody List<Long> ids,
                                                   @RequestParam String performedBy) {
        return documentService.submitDocuments(ids, performedBy);
    }

    @PostMapping("/approve")
    public List<DocumentResultDto> approveDocuments(@RequestBody List<Long> ids,
                                                    @RequestParam String performedBy) {
        return documentService.approveDocuments(ids, performedBy);
    }

    @GetMapping("/search")
    public Page<Document> searchDocuments(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        LocalDateTime fromDt = from != null ? LocalDateTime.parse(from) : null;
        LocalDateTime toDt = to != null ? LocalDateTime.parse(to) : null;

        return documentService.searchDocuments(status, author, fromDt, toDt, PageRequest.of(page, size));
    }

    @PostMapping("/check-concurrent")
    public Map<String, Object> checkConcurrentApproval(@RequestParam Long documentId,
                                                       @RequestParam int threads,
                                                       @RequestParam int attempts) {
        return documentService.checkConcurrentApproval(documentId, threads, attempts);
    }

}
