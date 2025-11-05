package com.example.oms.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A generic class to represent paginated API responses.
 *
 * @param <T> The type of the content in the paginated response.
 */
public class PaginatedResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public PaginatedResponse(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.first = first;
        this.last = last;
    }

    @JsonProperty
    public List<T> getContent() {
        return content;
    }

    @JsonProperty
    public int getPage() {
        return page;
    }

    @JsonProperty
    public int getSize() {
        return size;
    }

    @JsonProperty("total_elements")
    public long getTotalElements() {
        return totalElements;
    }

    @JsonProperty("total_pages")
    public int getTotalPages() {
        return totalPages;
    }

    @JsonProperty
    public boolean isFirst() {
        return first;
    }

    @JsonProperty
    public boolean isLast() {
        return last;
    }

    /**
     * Creates a PaginatedResponse from a Spring Data Page object.
     */
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        );
    }
}
