package com.h4j4x.expenses.api.model;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record PageData<TYPE>(List<TYPE> list, int pageIndex, int pageSize, long totalCount) {
    public static <T> PageData<T> empty() {
        return new PageData<>(Collections.emptyList(), 0, 0, 0L);
    }

    public <TARGET> PageData<TARGET> map(Function<TYPE, TARGET> mapper) {
        var targetList = list.stream().map(mapper).toList();
        return new PageData<>(targetList, pageIndex, pageSize, totalCount);
    }
}
