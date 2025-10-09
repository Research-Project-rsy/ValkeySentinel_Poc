package Server.Poc.domain.readRequest.manager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SessionValkeyReadManager {

    // ======================= Read (Round Robin) ver 2.0 =========================

    private final List<RedisTemplate<String, String>> readTemplates;
    private final AtomicInteger rrCounter = new AtomicInteger(0);

    public SessionValkeyReadManager(
            @Qualifier("sessionReadTemplates") List<RedisTemplate<String, String>> readTemplates) {
        this.readTemplates = readTemplates;
    }

    /**
     * Valkey 값 조회 메서드
     * @param key 조회 키
     * @return 조회 값
     */
    public String getValue(String key) {

        /* 읽을 대상의 노드 순환 반복 */
        RedisTemplate<String, String> template = nextTemplate();
        return template.opsForValue().get(key);
    }

    /**
     * Round Robin 방식으로 Redis 읽기 노드를 순환 선택하는 메서드
     * @return 0 → 1 → 2 → … → N-1 → 0 순환 반복.
     */
    private RedisTemplate<String, String> nextTemplate() {
        int size = readTemplates.size();
        int index = rrCounter.getAndUpdate(i -> (i + 1) % size);
        return readTemplates.get(index);
    }

}
