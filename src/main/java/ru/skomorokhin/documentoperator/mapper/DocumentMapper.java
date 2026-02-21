package ru.skomorokhin.documentoperator.mapper;

import org.mapstruct.Mapper;
import ru.skomorokhin.documentoperator.dto.DocHistoryDto;
import ru.skomorokhin.documentoperator.dto.DocWithHistory;
import ru.skomorokhin.documentoperator.model.entity.Document;
import ru.skomorokhin.documentoperator.model.entity.DocumentHistory;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    DocWithHistory toDto(Document document);

    DocHistoryDto toDto(DocumentHistory history);
}
