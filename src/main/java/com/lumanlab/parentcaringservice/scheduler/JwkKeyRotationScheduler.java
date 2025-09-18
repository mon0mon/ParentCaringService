package com.lumanlab.parentcaringservice.scheduler;

import com.lumanlab.parentcaringservice.security.jwt.application.service.JwkManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwkKeyRotationScheduler {

    private final JwkManager jwkManager;

    @Scheduled(fixedRateString = "#{${jwt.key.rotation-interval}}", timeUnit = TimeUnit.SECONDS)
    public void rotateKeys() {
        log.info("JWT 키 로테이션 스케줄 시작");
        try {
            jwkManager.rotateKey();
            log.info("JWT 키 로테이션 스케줄 완료");
        } catch (Exception e) {
            log.error("JWT 키 로테이션 중 오류 발생", e);
        }
    }
}
