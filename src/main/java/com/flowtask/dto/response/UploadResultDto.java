package com.flowtask.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class UploadResultDto {
    private final boolean success;
    private final int addedCount;
    private final List<String> duplicates;
    private final long totalCount;
    private final String message;

    private UploadResultDto(boolean success, int addedCount,
                             List<String> duplicates, long totalCount, String message) {
        this.success    = success;
        this.addedCount = addedCount;
        this.duplicates = duplicates;
        this.totalCount = totalCount;
        this.message    = message;
    }

    /** 정상 추가 (일부 중복 건너뜀 포함) */
    public static UploadResultDto success(int addedCount, List<String> duplicates, long totalCount) {
        String msg;
        if (addedCount > 0 && duplicates.isEmpty()) {
            msg = "총 " + addedCount + "개 확장자가 커스텀 확장자 목록에 성공적으로 추가됐습니다.";
        } else if (addedCount > 0) {
            msg = "총 " + addedCount + "개 확장자가 추가됐습니다.\n"
                    + "중복으로 건너뛴 항목: " + String.join(", ", duplicates);
        } else if (!duplicates.isEmpty()) {
            msg = "추가된 확장자가 없습니다. 모두 중복 항목입니다.\n"
                    + "중복 항목: " + String.join(", ", duplicates);
        } else {
            msg = "추가할 유효한 확장자가 없습니다.";
        }
        return new UploadResultDto(true, addedCount, duplicates, totalCount, msg);
    }

    /** 200개 초과로 전체 실패 (중복 정보 포함) */
    public static UploadResultDto limitExceeded(long currentCount, int toAddCount,
                                                 List<String> duplicates) {
        String msg = "엑셀 업로드에 실패했습니다.\n"
                + "추가하면 최대 200개를 초과합니다. (현재: " + currentCount
                + "개, 추가 시도: " + toAddCount + "개)";
        if (!duplicates.isEmpty()) {
            msg += "\n중복으로 제외된 항목 총 " + duplicates.size() + "개\n"
                    + String.join(", ", duplicates);
        }
        return new UploadResultDto(false, 0, duplicates, currentCount, msg);
    }
}
