package com.wanderlust.api.common;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionTest {

    @Test
    void notFound_producesCorrectStatusAndMessage() {
        UUID id = UUID.randomUUID();

        ApiException ex = ApiException.notFound("User", id);

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("User not found with id: " + id);
    }

    @Test
    void forbidden_producesCorrectStatusAndMessage() {
        ApiException ex = ApiException.forbidden("Access denied");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(ex.getMessage()).isEqualTo("Access denied");
    }

    @Test
    void badRequest_producesCorrectStatusAndMessage() {
        ApiException ex = ApiException.badRequest("Invalid input");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo("Invalid input");
    }

    @Test
    void conflict_producesCorrectStatusAndMessage() {
        ApiException ex = ApiException.conflict("Already exists");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("Already exists");
    }
}
