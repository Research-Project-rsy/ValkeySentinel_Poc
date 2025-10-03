package Server.Poc.domain.writeRequest.service;

import Server.Poc.domain.writeRequest.helper.TimeHelper;
import Server.Poc.domain.writeRequest.manager.SessionValkeyWriteManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WriteService {

    /**
     * Service 클래스는 오케스트레이션 클래스로, 흐름 제어를 담당합니다.
     *
     * Helper : 보조 클래스
     * Manager : 핵심 비즈니스 로직 클래스
     *
     */

    private final SessionValkeyWriteManager sessionValkeyWriteManager;
    private static final long TTL_MINUTES = 30;

    // 세션 서버에 테스트 데이터 전송 메서드
    public String testWrite() {

        // 현재 시간을 정해진 포맷 형식으로 포맷
        String currentTime = TimeHelper.getCurrentFormattedTime();

        // 세션 서버에 저장할 Key 설정
        String key = "test:currentTime";

        // 세션 서버에 현재의 시간대 저장
        sessionValkeyWriteManager.setValue(key, currentTime, TTL_MINUTES);
        log.info("Session Server에 테스트 데이터 저장 완료: key={}, value={}", key, currentTime);

        return currentTime;
    }

}
