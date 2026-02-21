package ru.skomorokhin.documentoperator.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import ru.skomorokhin.documentoperator.model.enums.DocumentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class DocumentFilter {

    private List<Long> ids;

    private String documentNumber;

    private String author;

    private List<DocumentStatus> status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdTo;
}
