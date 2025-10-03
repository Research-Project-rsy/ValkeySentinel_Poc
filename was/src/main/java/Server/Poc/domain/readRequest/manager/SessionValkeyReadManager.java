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

    public String getValue(String key) {
        RedisTemplate<String, String> template = nextTemplate();
        return template.opsForValue().get(key);
    }

    private RedisTemplate<String, String> nextTemplate() {
        int size = readTemplates.size();
        int index = rrCounter.getAndUpdate(i -> (i + 1) % size);
        return readTemplates.get(index);
    }

}
