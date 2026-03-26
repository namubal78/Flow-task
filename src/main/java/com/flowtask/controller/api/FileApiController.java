package com.flowtask.controller.api;

import com.flowtask.dto.response.FileUploadResultDto;
import com.flowtask.dto.response.UploadedFileDto;
import com.flowtask.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileApiController {

    private final FileUploadService fileUploadService;

    // 업로드된 파일 목록 조회
    @GetMapping
    public ResponseEntity<List<UploadedFileDto>> getFiles() {
        return ResponseEntity.ok(fileUploadService.getFiles());
    }

    // 파일 업로드 (차단 검사 포함, 다중)
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResultDto> uploadFiles(
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        return ResponseEntity.ok(fileUploadService.uploadFiles(files));
    }

    // 개별 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) throws IOException {
        fileUploadService.deleteFile(id);
        return ResponseEntity.ok().build();
    }

    // 전체 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteAllFiles() throws IOException {
        fileUploadService.deleteAllFiles();
        return ResponseEntity.ok().build();
    }
}
