package com.flowtask.dto.response;

import com.flowtask.domain.entity.BlockedSub;
import lombok.Getter;

@Getter
public class CustomExtensionDto {
    private final Long id;
    private final String extension;

    public CustomExtensionDto(BlockedSub sub) {
        this.id = sub.getId();
        this.extension = sub.getBlockedExtension();
    }
}
