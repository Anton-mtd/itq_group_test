package ru.skomorokhin.documentoperator.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.skomorokhin.documentoperator.model.entity.ApprovalRegistry;
import ru.skomorokhin.documentoperator.model.entity.Document;

import java.util.Optional;

@Repository
public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistry, Long> {

    Optional<ApprovalRegistry> findByDocument(Document document);

}
