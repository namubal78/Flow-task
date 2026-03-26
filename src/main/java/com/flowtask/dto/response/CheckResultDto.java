package com.flowtask.dto.response;

import lombok.Getter;

@Getter
public class CheckResultDto {

    public enum Status { AVAILABLE, DUPLICATE, FIXED }

    private final Status status;
    private final String message;

    private CheckResultDto(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static CheckResultDto available() {
        return new CheckResultDto(Status.AVAILABLE, "추가할 수 있습니다");
    }

    public static CheckResultDto duplicate() {
        return new CheckResultDto(Status.DUPLICATE, "차단 목록에 이미 있습니다.");
    }

    public static CheckResultDto fixed() {
        return new CheckResultDto(Status.FIXED, "고정 확장자에서 체크해주세요");
    }
}
