package ru.skomorokhin.documentoperator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> items;

    private MetaPageInfo meta;

    @Data
    @Builder
    public static class MetaPageInfo {

        private Integer totalPages;
        private Integer pageSize;
        private Integer page;
        private Integer itemsCount;
    }
}
