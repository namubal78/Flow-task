package com.flowtask.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class FileUploadResultDto {
    private final int uploadedCount;
    private final List<String> blockedExtensions;
    private final List<UploadedFileDto> uploadedFiles;
    private final String message;

    private FileUploadResultDto(int uploadedCount, List<String> blockedExtensions,
                                 List<UploadedFileDto> uploadedFiles, String message) {
        this.uploadedCount    = uploadedCount;
        this.blockedExtensions = blockedExtensions;
        this.uploadedFiles    = uploadedFiles;
        this.message          = message;
    }

    public static FileUploadResultDto of(int uploadedCount, List<String> blockedExtensions,
                                          List<UploadedFileDto> uploadedFiles) {
        String msg;
        if (!blockedExtensions.isEmpty() && uploadedCount == 0) {
            msg = "차단된 확장자인 [" + String.join(", ", blockedExtensions) + "]로 인해 모든 파일 업로드가 차단됐습니다.";
        } else if (!blockedExtensions.isEmpty()) {
            msg = "차단된 확장자인 [" + String.join(", ", blockedExtensions) + "]를 제외하고 총 "
                    + uploadedCount + "개 파일을 업로드했습니다.";
        } else {
            msg = "총 " + uploadedCount + "개 파일을 업로드했습니다.";
        }
        return new FileUploadResultDto(uploadedCount, blockedExtensions, uploadedFiles, msg);
    }
}
