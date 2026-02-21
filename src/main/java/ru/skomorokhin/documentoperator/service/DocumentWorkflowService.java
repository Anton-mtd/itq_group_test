package ru.skomorokhin.documentoperator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skomorokhin.documentoperator.dto.DocumentDto;
import ru.skomorokhin.documentoperator.exception.BusinessException;
import ru.skomorokhin.documentoperator.exception.ErrorCode;
import ru.skomorokhin.documentoperator.model.entity.ApprovalRegistry;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.entity.DocumentHistory;
import ru.skomorokhin.documentoperator.model.enums.DocumentAction;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.repository.ApprovalRegistryRepository;
import ru.skomorokhin.documentoperator.repository.DocumentHistoryRepository;
import ru.skomorokhin.documentoperator.repository.DocumentRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentWorkflowService {

    private final DocumentRepository documentRepository;
    private final DocumentHistoryRepository historyRepository;
    private final ApprovalRegistryRepository registryRepository;

    @Transactional
    public void submitDocument(Long id, String performedBy) {

        Document doc = documentRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND)
                );

        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BusinessException(
                    ErrorCode.INVALID_STATUS,
                    "Document must be in DRAFT status"
            );
        }

        doc.setStatus(DocumentStatus.SUBMITTED);
        documentRepository.save(doc);

        DocumentHistory history = new DocumentHistory();
        history.setDocument(doc);
        history.setAction(DocumentAction.SUBMIT);
        history.setPerformedBy(performedBy);
        historyRepository.save(history);
    }

    @Transactional
    public void approveDocument(Long id, String performedBy) {

        Document doc = documentRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND)
                );

        if (doc.getStatus() != DocumentStatus.SUBMITTED) {
            throw new BusinessException(
                    ErrorCode.INVALID_STATUS,
                    "Document must be in SUBMITTED status"
            );
        }

        doc.setStatus(DocumentStatus.APPROVED);

        documentRepository.save(doc);

        DocumentHistory history = new DocumentHistory();
        history.setDocument(doc);
        history.setAction(DocumentAction.APPROVE);
        history.setPerformedBy(performedBy);
        historyRepository.save(history);

        try {

            ApprovalRegistry registry = new ApprovalRegistry();
            registry.setDocument(doc);
            registry.setApprovedBy(performedBy);

            registryRepository.save(registry);

        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.REGISTRY_ERROR);
        }
    }
}
