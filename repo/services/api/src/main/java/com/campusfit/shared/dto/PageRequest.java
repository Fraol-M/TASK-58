package com.campusfit.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 25;

    private String sort;

    @Builder.Default
    private String direction = "ASC";

    public Pageable toSpringPageable() {
        if (sort != null && !sort.isBlank()) {
            Sort.Direction dir = Sort.Direction.fromOptionalString(direction)
                .orElse(Sort.Direction.ASC);
            return org.springframework.data.domain.PageRequest.of(page, size, Sort.by(dir, sort));
        }
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}
