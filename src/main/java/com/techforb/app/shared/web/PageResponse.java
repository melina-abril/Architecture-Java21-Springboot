package com.techforb.app.shared.web;

import java.util.List;
import java.util.function.Function;

/**
 * Respuesta paginada de todos los listados: misma forma que Page<T> del front. `page` empieza en 0.
 * Los repositorios en memoria filtran y después llaman a PageResponse.of(lista, page, size, mapper).
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <S, T> PageResponse<T> of(List<S> all, int page, int size, Function<S, T> mapper) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("page >= 0 y 1 <= size <= 100");
        }
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());
        int totalPages = Math.max(1, (int) Math.ceil((double) all.size() / size));
        return new PageResponse<>(all.subList(from, to).stream().map(mapper).toList(), page, size, all.size(), totalPages);
    }
}
