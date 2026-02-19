package ru.skomorokhin.documentoperator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.entity.DocumentHistory;

import java.util.List;

@Repository
public interface DocumentHistoryRepository extends JpaRepository<DocumentHistory, Long> {

    List<DocumentHistory> findByDocumentOrderByPerformedAtAsc(Document document);

}
