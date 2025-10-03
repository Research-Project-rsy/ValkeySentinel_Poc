package Server.Poc.domain.readRequest.service;

import Server.Poc.domain.readRequest.manager.SessionValkeyReadManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReadService {

    /* Connection Pool / SingleTone 적용 X - 매 요청마다 Connection 연결&생성 */
    private final SessionValkeyReadManager sessionValkeyReadManager;

    // 세션 서버에 테스트 데이터 조회 메서드
    public String testRead() {

        String key = "test:currentTime";
        String data = sessionValkeyReadManager.getValue(key); // 조회 메서드 호출

        log.info("Session Server에 테스트 데이터 조회 완료 : key={}, data={}", key, data);
        return data;
    }
}
