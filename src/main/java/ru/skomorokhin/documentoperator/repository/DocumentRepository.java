package ru.skomorokhin.documentoperator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Optional<Document> findByDocumentNumber(String documentNumber);

    List<Document> findByStatus(DocumentStatus status);

}
