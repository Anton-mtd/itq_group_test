package ru.skomorokhin.documentoperator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentDto {
    private Long documentId;
    private boolean success;
    private String message;
}