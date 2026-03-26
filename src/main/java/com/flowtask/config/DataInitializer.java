package com.flowtask.config;

import com.flowtask.domain.entity.BlockedSystem;
import com.flowtask.domain.repository.BlockedSystemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final BlockedSystemRepository blockedSystemRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (blockedSystemRepository.count() == 0) {
            BlockedSystem system = BlockedSystem.create("test");
            blockedSystemRepository.save(system);
            log.info("BlockedSystem 초기 데이터 생성 완료 (id={})", system.getId());
        }
    }
}
