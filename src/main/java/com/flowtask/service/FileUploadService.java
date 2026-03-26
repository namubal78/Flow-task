package com.flowtask.service;

import com.flowtask.config.AppProperties;
import com.flowtask.domain.entity.BlockedSystem;
import com.flowtask.domain.entity.UploadedFile;
import com.flowtask.domain.repository.BlockedSubRepository;
import com.flowtask.domain.repository.BlockedSystemRepository;
import com.flowtask.domain.repository.UploadedFileRepository;
import com.flowtask.dto.response.FileUploadResultDto;
import com.flowtask.dto.response.UploadedFileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileUploadService {

    private final UploadedFileRepository uploadedFileRepository;
    private final BlockedSubRepository   blockedSubRepository;
    private final BlockedSystemRepository blockedSystemRepository;
    private final AppProperties          appProperties;

    private Long systemId() {
        return appProperties.getSystemId();
    }

    private BlockedSystem getSystem() {
        return blockedSystemRepository.findById(systemId())
                .orElseThrow(() -> new IllegalStateException("시스템 데이터가 없습니다."));
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase().trim();
    }

    // ── 파일 목록 조회

    public List<UploadedFileDto> getFiles() {
        return uploadedFileRepository.findBySystemIdOrderByInsertDateDesc(systemId())
                .stream()
                .map(UploadedFileDto::new)
                .collect(Collectors.toList());
    }

    // ── 파일 업로드 (차단 검사 포함)

    @Transactional
    public FileUploadResultDto uploadFiles(List<MultipartFile> files) throws IOException {
        // 현재 차단된 확장자 전체 조회 (체크된 고정 + 커스텀)
        Set<String> blockedExtensions = blockedSubRepository.findBySystemId(systemId())
                .stream()
                .map(b -> b.getBlockedExtension().toLowerCase())
                .collect(Collectors.toSet());

        List<MultipartFile>  allowedFiles      = new ArrayList<>();
        LinkedHashSet<String> blockedExtSet    = new LinkedHashSet<>();

        for (MultipartFile file : files) {
            String ext = extractExtension(file.getOriginalFilename());
            if (blockedExtensions.contains(ext)) {
                blockedExtSet.add(ext);
            } else {
                allowedFiles.add(file);
            }
        }

        // 업로드 디렉토리 생성 (없으면)
        Path uploadPath = Paths.get(appProperties.getUploadDir());
        Files.createDirectories(uploadPath);

        BlockedSystem system = getSystem();
        List<UploadedFileDto> uploadedDtos = new ArrayList<>();

        for (MultipartFile file : allowedFiles) {
            String originalName = file.getOriginalFilename();
            String ext          = extractExtension(originalName);
            String storedName   = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            file.transferTo(uploadPath.resolve(storedName));

            UploadedFile saved = uploadedFileRepository.save(
                    UploadedFile.create(system, originalName, storedName, file.getSize(), ext));
            uploadedDtos.add(new UploadedFileDto(saved));
        }

        return FileUploadResultDto.of(allowedFiles.size(), new ArrayList<>(blockedExtSet), uploadedDtos);
    }

    // ── 개별 삭제

    @Transactional
    public void deleteFile(Long id) throws IOException {
        UploadedFile file = uploadedFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 파일입니다."));
        Files.deleteIfExists(Paths.get(appProperties.getUploadDir()).resolve(file.getStoredName()));
        uploadedFileRepository.delete(file);
    }

    // ── 전체 삭제

    @Transactional
    public void deleteAllFiles() throws IOException {
        List<UploadedFile> files = uploadedFileRepository.findBySystemIdOrderByInsertDateDesc(systemId());
        Path uploadPath = Paths.get(appProperties.getUploadDir());
        for (UploadedFile file : files) {
            Files.deleteIfExists(uploadPath.resolve(file.getStoredName()));
        }
        uploadedFileRepository.deleteBySystemId(systemId());
    }
}
