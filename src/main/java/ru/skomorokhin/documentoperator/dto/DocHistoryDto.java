package ru.skomorokhin.documentoperator.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Data
public class DocHistoryDto {
    private Long id;
    private String action;
    private String performedBy;
    private LocalDateTime performedAt;
    private String comment;
}
