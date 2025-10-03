package Server.Poc.domain.writeRequest.manager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
// @RequiredArgsConstructor
public class SessionValkeyWriteManager {

    private final RedisTemplate<String, String> sessionValkeyTemplate;

    public SessionValkeyWriteManager(
            @Qualifier("sessionWriteTemplate") RedisTemplate<String, String> sessionValkeyTemplate) {
        this.sessionValkeyTemplate = sessionValkeyTemplate;
    }

    // 세션 서버에 값 저장
    public void setValue(String key, String value, long ttlMinutes) {
        ValueOperations<String, String> ops = sessionValkeyTemplate.opsForValue();
        ops.set(key, value, ttlMinutes, TimeUnit.MINUTES);
    }


}
