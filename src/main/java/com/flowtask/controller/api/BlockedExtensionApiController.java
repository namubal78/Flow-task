package com.flowtask.controller.api;

import com.flowtask.dto.request.CustomExtensionRequest;
import com.flowtask.dto.response.*;
import com.flowtask.service.BlockedExtensionService;
import com.flowtask.service.ExcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/extensions")
@RequiredArgsConstructor
public class BlockedExtensionApiController {

    private final BlockedExtensionService service;
    private final ExcelService excelService;

    // 1. 화면 초기 데이터 조회
    @GetMapping
    public ResponseEntity<ExtensionPageResponse> getPageData(
            @RequestParam(defaultValue = "recent") String sort) {
        return ResponseEntity.ok(service.getPageData(sort));
    }

    // 2. 고정 확장자 체크 변경 (체크=INSERT, 언체크=DELETE)
    @PatchMapping("/fixed/{ext}")
    public ResponseEntity<Void> toggleFixed(
            @PathVariable String ext,
            @RequestParam boolean checked) {
        service.toggleFixedExtension(ext, checked);
        return ResponseEntity.ok().build();
    }

    // 4. 실시간 중복 체크
    @GetMapping("/check")
    public ResponseEntity<CheckResultDto> checkExtension(@RequestParam String ext) {
        return ResponseEntity.ok(service.checkExtension(ext));
    }

    // 7. 커스텀 확장자 추가
    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<CustomExtensionDto>> addCustom(
            @Valid @RequestBody CustomExtensionRequest request) {
        return ResponseEntity.ok(service.addCustomExtension(request.getExtension()));
    }

    // 9. 정렬된 커스텀 목록 조회
    @GetMapping("/custom")
    public ResponseEntity<ApiResponse<List<CustomExtensionDto>>> getCustomList(
            @RequestParam(defaultValue = "recent") String sort) {
        List<CustomExtensionDto> list = service.getCustomExtensions(sort);
        return ResponseEntity.ok(ApiResponse.of(list, list.size()));
    }

    // 10. 전체 삭제
    @DeleteMapping("/custom")
    public ResponseEntity<ApiResponse<Void>> deleteAll() {
        service.deleteAllCustomExtensions();
        return ResponseEntity.ok(ApiResponse.of(null, 0L));
    }

    // 11. 개별 삭제
    @DeleteMapping("/custom/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.deleteCustomExtension(id));
    }

    // 5. 양식 다운로드
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] bytes = excelService.createTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"blocked_extension_list.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // 6. 엑셀 업로드
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<UploadResultDto>> uploadExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        List<String> extensions = excelService.parseUpload(file);
        UploadResultDto result = service.processUpload(extensions);
        return ResponseEntity.ok(ApiResponse.of(result, result.getTotalCount()));
    }
}
