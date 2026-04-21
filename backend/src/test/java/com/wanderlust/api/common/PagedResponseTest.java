package com.wanderlust.api.common;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PagedResponseTest {

    @Test
    void from_convertsSpringPageToPagedResponse() {
        List<String> items = List.of("Paris", "Tokyo", "New York");
        PageRequest pageable = PageRequest.of(0, 10);
        Page<String> springPage = new PageImpl<>(items, pageable, 25);

        PagedResponse<String> result = PagedResponse.from(springPage);

        assertThat(result.getContent()).containsExactly("Paris", "Tokyo", "New York");
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isEqualTo(25);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isLast()).isFalse();
    }

    @Test
    void from_lastPageIsTrue_whenOnFinalPage() {
        List<String> items = List.of("Bali");
        PageRequest pageable = PageRequest.of(2, 10);
        Page<String> springPage = new PageImpl<>(items, pageable, 21);

        PagedResponse<String> result = PagedResponse.from(springPage);

        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.isLast()).isTrue();
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getTotalElements()).isEqualTo(21);
    }

    @Test
    void from_emptyPage_returnsEmptyContent() {
        Page<String> emptyPage = Page.empty(PageRequest.of(0, 10));

        PagedResponse<String> result = PagedResponse.from(emptyPage);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.isLast()).isTrue();
    }
}
