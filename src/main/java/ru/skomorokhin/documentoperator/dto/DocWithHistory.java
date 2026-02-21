package ru.skomorokhin.documentoperator.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DocWithHistory {
    private Long id;
    private String documentNumber;
    private String author;
    private String title;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DocHistoryDto> history;
}
