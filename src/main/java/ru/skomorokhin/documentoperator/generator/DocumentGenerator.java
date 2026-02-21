package ru.skomorokhin.documentoperator.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.skomorokhin.documentoperator.config.DocumentGeneratorProperties;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;
import ru.skomorokhin.documentoperator.service.DocumentService;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentGenerator {

    private final DocumentService documentService;
    private final DocumentGeneratorProperties props;

    public void run() {

        int total = props.getTotalDocuments();
        int batchSize = props.getBatchSize();

        for (int i = 0; i < total; i += batchSize) {

            int end = Math.min(i + batchSize, total);

            List<Document> batch = IntStream.range(i, end)
                    .mapToObj(this::buildDocument)
                    .toList();

            documentService.saveAll(batch);

            log.info("Saved batch {} - {}", i, end);
        }

        log.info("Finished creating {} documents", total);
    }

    private Document buildDocument(int index) {
        Document doc = new Document();
        doc.setAuthor(props.getAuthorPrefix() + index);
        doc.setTitle(props.getTitlePrefix() + " #" + index);
        doc.setStatus(DocumentStatus.DRAFT);
        doc.setDocumentNumber(documentService.generateDocumentNumber());
        return doc;
    }
}