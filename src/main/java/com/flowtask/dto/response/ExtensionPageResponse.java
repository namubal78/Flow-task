package com.flowtask.dto.response;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ExtensionPageResponse {
    private final Map<String, Boolean> fixedExtensions;  // ext -> checked 여부
    private final List<CustomExtensionDto> customExtensions;
    private final long customCount;

    public ExtensionPageResponse(Map<String, Boolean> fixedExtensions,
                                  List<CustomExtensionDto> customExtensions) {
        this.fixedExtensions = fixedExtensions;
        this.customExtensions = customExtensions;
        this.customCount = customExtensions.size();
    }
}
