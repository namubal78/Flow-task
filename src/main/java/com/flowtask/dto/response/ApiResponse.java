package com.flowtask.dto.response;

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final T data;
    private final long customCount;

    private ApiResponse(T data, long customCount) {
        this.data = data;
        this.customCount = customCount;
    }

    public static <T> ApiResponse<T> of(T data, long customCount) {
        return new ApiResponse<>(data, customCount);
    }
}
