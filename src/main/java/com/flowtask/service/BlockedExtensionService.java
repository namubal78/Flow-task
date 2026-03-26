package com.flowtask.service;

import com.flowtask.config.AppProperties;
import com.flowtask.domain.entity.BlockedSub;
import com.flowtask.domain.entity.BlockedSystem;
import com.flowtask.domain.enums.BlockedType;
import com.flowtask.domain.enums.FixedExtension;
import com.flowtask.domain.repository.BlockedSubRepository;
import com.flowtask.domain.repository.BlockedSystemRepository;
import com.flowtask.dto.response.ApiResponse;
import com.flowtask.dto.response.CheckResultDto;
import com.flowtask.dto.response.CustomExtensionDto;
import com.flowtask.dto.response.ExtensionPageResponse;
import com.flowtask.dto.response.UploadResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockedExtensionService {

    private static final int CUSTOM_MAX = 200;

    private final BlockedSubRepository blockedSubRepository;
    private final BlockedSystemRepository blockedSystemRepository;
    private final AppProperties appProperties;

    // ── 내부 헬퍼

    private Long systemId() {
        return appProperties.getSystemId();
    }

    private BlockedSystem getSystem() {
        return blockedSystemRepository.findById(systemId())
                .orElseThrow(() -> new IllegalStateException("시스템 데이터가 없습니다."));
    }

    // ── 1. 화면 초기 데이터 조회

    public ExtensionPageResponse getPageData(String sort) {
        // 고정 확장자: FixedExtension Enum 기준 순서로 체크 상태 결정
        List<BlockedSub> checkedFixed = blockedSubRepository
                .findBySystemIdAndBlockedType(systemId(), BlockedType.FIXED);
        Set<String> checkedSet = checkedFixed.stream()
                .map(BlockedSub::getBlockedExtension)
                .collect(Collectors.toSet());

        Map<String, Boolean> fixedMap = new LinkedHashMap<>();
        for (FixedExtension fe : FixedExtension.values()) {
            fixedMap.put(fe.getValue(), checkedSet.contains(fe.getValue()));
        }

        return new ExtensionPageResponse(fixedMap, getCustomExtensions(sort));
    }

    // ── 2. 고정 확장자 체크 변경 (체크=INSERT, 언체크=DELETE)

    @Transactional
    public void toggleFixedExtension(String ext, boolean checked) {
        String normalized = ext.toLowerCase().trim();
        if (!FixedExtension.contains(normalized)) {
            throw new IllegalArgumentException("유효하지 않은 고정 확장자입니다: " + ext);
        }
        if (checked) {
            boolean exists = blockedSubRepository
                    .findBySystemIdAndBlockedExtensionIgnoreCaseAndBlockedType(
                            systemId(), normalized, BlockedType.FIXED)
                    .isPresent();
            if (!exists) {
                blockedSubRepository.save(BlockedSub.create(getSystem(), normalized, BlockedType.FIXED));
            }
        } else {
            blockedSubRepository
                    .findBySystemIdAndBlockedExtensionIgnoreCaseAndBlockedType(
                            systemId(), normalized, BlockedType.FIXED)
                    .ifPresent(blockedSubRepository::delete);
        }
    }

    // ── 4. 실시간 중복 체크 (debounce는 프론트에서 처리)

    public CheckResultDto checkExtension(String ext) {
        String normalized = ext.toLowerCase().trim();
        if (FixedExtension.contains(normalized)) {
            return CheckResultDto.fixed();
        }
        if (blockedSubRepository.existsBySystemIdAndBlockedExtensionIgnoreCase(systemId(), normalized)) {
            return CheckResultDto.duplicate();
        }
        return CheckResultDto.available();
    }

    // ── 7. 커스텀 확장자 추가 (단건)
    // TODO: 엑셀 업로드 기능 구현 시 이 메서드를 반복 호출하는 방식으로 일괄 추가 (로직 중복 없음)

    @Transactional
    public ApiResponse<CustomExtensionDto> addCustomExtension(String ext) {
        String normalized = ext.toLowerCase().trim();

        if (FixedExtension.contains(normalized)) {
            throw new IllegalArgumentException("고정 확장자입니다. 위쪽 고정 확장자에서 체크해주세요.");
        }
        if (blockedSubRepository.existsBySystemIdAndBlockedExtensionIgnoreCase(systemId(), normalized)) {
            throw new IllegalArgumentException("이미 차단 목록에 있는 확장자입니다.");
        }

        long currentCount = blockedSubRepository.countBySystemIdAndBlockedType(systemId(), BlockedType.CUSTOM);
        if (currentCount >= CUSTOM_MAX) {
            throw new IllegalStateException("커스텀 확장자는 최대 " + CUSTOM_MAX + "개까지 추가 가능합니다.");
        }

        BlockedSub saved = blockedSubRepository.save(BlockedSub.create(getSystem(), normalized, BlockedType.CUSTOM));
        return ApiResponse.of(new CustomExtensionDto(saved), currentCount + 1);
    }

    // ── 9. 정렬된 커스텀 목록 조회

    public List<CustomExtensionDto> getCustomExtensions(String sort) {
        List<BlockedSub> list = switch (sort) {
            case "alpha_asc"  -> blockedSubRepository
                    .findBySystemIdAndBlockedTypeOrderByBlockedExtensionAsc(systemId(), BlockedType.CUSTOM);
            case "alpha_desc" -> blockedSubRepository
                    .findBySystemIdAndBlockedTypeOrderByBlockedExtensionDesc(systemId(), BlockedType.CUSTOM);
            default           -> blockedSubRepository
                    .findBySystemIdAndBlockedTypeOrderByInsertDateDesc(systemId(), BlockedType.CUSTOM);
        };
        return list.stream().map(CustomExtensionDto::new).collect(Collectors.toList());
    }

    // ── 10. 전체 삭제

    @Transactional
    public void deleteAllCustomExtensions() {
        blockedSubRepository.deleteBySystemIdAndBlockedType(systemId(), BlockedType.CUSTOM);
    }

    // ── 11. 개별 삭제

    @Transactional
    public ApiResponse<Void> deleteCustomExtension(Long id) {
        BlockedSub sub = blockedSubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 확장자입니다."));
        if (sub.getBlockedType() != BlockedType.CUSTOM) {
            throw new IllegalArgumentException("커스텀 확장자만 삭제 가능합니다.");
        }
        blockedSubRepository.delete(sub);
        long remaining = blockedSubRepository.countBySystemIdAndBlockedType(systemId(), BlockedType.CUSTOM);
        return ApiResponse.of(null, remaining);
    }

    public long getCustomCount() {
        return blockedSubRepository.countBySystemIdAndBlockedType(systemId(), BlockedType.CUSTOM);
    }

    // ── 6. 엑셀 업로드 일괄 추가

    @Transactional
    public UploadResultDto processUpload(List<String> rawExtensions) {
        long currentCount = blockedSubRepository.countBySystemIdAndBlockedType(systemId(), BlockedType.CUSTOM);

        // 엑셀 내 중복 제거 + 유효성 필터 (영문·숫자만)
        List<String> unique = rawExtensions.stream()
                .filter(ext -> ext.matches("[a-zA-Z0-9]+"))
                .distinct()
                .collect(Collectors.toList());

        List<String> toAdd      = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();

        for (String ext : unique) {
            if (FixedExtension.contains(ext) ||
                blockedSubRepository.existsBySystemIdAndBlockedExtensionIgnoreCase(systemId(), ext)) {
                duplicates.add(ext);
            } else {
                toAdd.add(ext);
            }
        }

        // 중복 제외 후 실제 추가될 수 기준으로 200개 초과 시 전체 실패 (중복 정보 포함하여 반환)
        if (currentCount + toAdd.size() > CUSTOM_MAX) {
            return UploadResultDto.limitExceeded(currentCount, toAdd.size(), duplicates);
        }

        BlockedSystem system = getSystem();
        // TODO: addCustomExtension(String ext) 단건 메서드와 동일한 검증 로직 공유
        //       단건 추가는 addCustomExtension 호출, 일괄 추가는 여기서 직접 배치 처리
        for (String ext : toAdd) {
            blockedSubRepository.save(BlockedSub.create(system, ext, BlockedType.CUSTOM));
        }

        long newCount = blockedSubRepository.countBySystemIdAndBlockedType(systemId(), BlockedType.CUSTOM);
        return UploadResultDto.success(toAdd.size(), duplicates, newCount);
    }
}
