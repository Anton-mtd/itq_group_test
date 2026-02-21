package ru.skomorokhin.documentoperator.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>,
        JpaSpecificationExecutor<Document> {

    List<Document> findByStatus(DocumentStatus status);

    @EntityGraph(value = "Document.withHistory", type = EntityGraph.EntityGraphType.FETCH)
    List<Document> findAllByIdIn(List<Long> ids);

    @EntityGraph(value = "Document.withHistory", type = EntityGraph.EntityGraphType.FETCH)
    Optional<Document> findDocumentById(Long id);

    @NonNull
    @EntityGraph(value = "Document.withHistory", type = EntityGraph.EntityGraphType.FETCH)
    Page<Document> findAll(@NonNull Specification<Document> spec, @NonNull Pageable pageable);

}
