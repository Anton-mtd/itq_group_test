package ru.skomorokhin.documentoperator.generator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.service.DocumentService;

@Component
public class DocumentGenerator {

    private final DocumentService documentService;
    private final int totalDocuments;
    private final String authorPrefix;
    private final String titlePrefix;
    private final int batchSize;

    public DocumentGenerator(DocumentService documentService,
                             @Value("${document-generator.totalDocuments}") int totalDocuments,
                             @Value("${document-generator.authorPrefix}") String authorPrefix,
                             @Value("${document-generator.titlePrefix}") String titlePrefix,
                             @Value("${document-generator.batchSize}") int batchSize) {
        this.documentService = documentService;
        this.totalDocuments = totalDocuments;
        this.authorPrefix = authorPrefix;
        this.titlePrefix = titlePrefix;
        this.batchSize = batchSize;
    }

    public void run() {
        int created = 0;
        while (created < totalDocuments) {
            int end = Math.min(created + batchSize, totalDocuments);
            for (int i = created; i < end; i++) {
                String author = authorPrefix + i;
                String title = titlePrefix + " #" + i;
                Document doc = documentService.createDocument(author, title);
                System.out.println("[DocumentGenerator] Created document: " + doc.getDocumentNumber());
            }
            created = end;
        }
        System.out.println("[DocumentGenerator] Finished creating " + totalDocuments + " documents.");
    }
}
