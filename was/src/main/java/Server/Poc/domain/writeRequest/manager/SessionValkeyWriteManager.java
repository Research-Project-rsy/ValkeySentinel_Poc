package Server.Poc.domain.writeRequest.manager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
// @RequiredArgsConstructor
public class SessionValkeyWriteManager {

    private final List<RedisTemplate<String, String>> writeTemplates;
    private final AtomicInteger rrCounter = new AtomicInteger(0);

    public SessionValkeyWriteManager(
            @Qualifier("sessionWriteTemplates") List<RedisTemplate<String, String>> writeTemplates) {
        this.writeTemplates = writeTemplates;
    }

    /**
     * Valkey 값 입력 메서드
     * @param key 입력 키
     * @param value 입력되는 값
     * @param ttlMinutes TTL
     */
    public void setValue(String key, String value, long ttlMinutes) {
        RedisTemplate<String, String> template = nextTemplate(); // RR로 Template 선택
        ValueOperations<String, String> ops = template.opsForValue();
        ops.set(key, value, ttlMinutes, TimeUnit.MINUTES);
    }

    /**
     * Round Robin 방식으로 Redis 읽기 노드를 순환 선택하는 메서드
     * @return 0 → 1 → 2 → … → N-1 → 0 순환 반복.
     */
    private RedisTemplate<String, String> nextTemplate() {
        int size = writeTemplates.size();
        int index = rrCounter.getAndUpdate(i -> (i + 1) % size);
        return writeTemplates.get(index);
    }
}
// 세션 서버에 값 저장
//    public void setValue(String key, String value, long ttlMinutes) {
//        ValueOperations<String, String> ops = writeTemplates.opsForValue();
//        ops.set(key, value, ttlMinutes, TimeUnit.MINUTES);
//    }
