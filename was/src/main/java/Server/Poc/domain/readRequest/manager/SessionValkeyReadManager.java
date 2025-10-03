package Server.Poc.domain.readRequest.manager;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SessionValkeyReadManager {

    // ======================= Read (Round Robin) ver 2.0  =========================

    private final RedisTemplate<String, String> read1Template;
    private final RedisTemplate<String, String> read2Template;
    private final AtomicInteger rrCounter = new AtomicInteger(0);

    public SessionValkeyReadManager(
            @Qualifier("sessionRead1Template") RedisTemplate<String, String> read1Template,
            @Qualifier("sessionRead2Template") RedisTemplate<String, String> read2Template
    ) {
        this.read1Template = read1Template;
        this.read2Template = read2Template;
    }

    public String getValue(String key) {
        RedisTemplate<String, String> template = nextTemplate();
        return template.opsForValue().get(key);
    }

    private RedisTemplate<String, String> nextTemplate() {
        int index = rrCounter.getAndUpdate(i -> (i + 1) % 2);
        return (index == 0) ? read1Template : read2Template;
    }

}
