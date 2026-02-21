package ru.skomorokhin.documentoperator.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;

import java.time.LocalDateTime;
import java.util.List;

public class DocumentSpecification {

    public static Specification<Document> idsIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return null;
        return (root, query, cb) -> root.get("id").in(ids);
    }

    public static Specification<Document> documentNumberContains(String docNum) {
        if (docNum == null || docNum.isBlank()) return null;
        return (root, query, cb) -> cb.like(root.get("documentNumber"), "%" + docNum + "%");
    }

    public static Specification<Document> authorContains(String author) {
        if (author == null || author.isBlank()) return null;
        return (root, query, cb) -> cb.like(root.get("author"), "%" + author + "%");
    }

    public static Specification<Document> statusIn(List<DocumentStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    public static Specification<Document> createdFrom(LocalDateTime from) {
        if (from == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Document> createdTo(LocalDateTime to) {
        if (to == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
