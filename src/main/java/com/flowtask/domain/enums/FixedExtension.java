package com.flowtask.domain.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 고정 확장자 목록 - 코드에서 관리 (DB 조작으로 목록 자체가 변경되지 않도록)
 * 체크 상태만 DB(blocked_sub)에 저장
 */
@Getter
public enum FixedExtension {
    BAT("bat"), CMD("cmd"), COM("com"), CPL("cpl"), EXE("exe"), SCR("scr"), JS("js");

    private final String value;

    FixedExtension(String value) {
        this.value = value;
    }

    public static boolean contains(String ext) {
        return Arrays.stream(values())
                .anyMatch(e -> e.value.equalsIgnoreCase(ext));
    }

    public static List<String> getValues() {
        return Arrays.stream(values())
                .map(FixedExtension::getValue)
                .collect(Collectors.toList());
    }
}
