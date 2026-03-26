package com.flowtask.dto.response;

import com.flowtask.domain.entity.UploadedFile;
import lombok.Getter;

@Getter
public class UploadedFileDto {
    private final Long id;
    private final String originalName;
    private final Long fileSize;
    private final String extension;

    public UploadedFileDto(UploadedFile file) {
        this.id           = file.getId();
        this.originalName = file.getOriginalName();
        this.fileSize     = file.getFileSize();
        this.extension    = file.getExtension();
    }
}
