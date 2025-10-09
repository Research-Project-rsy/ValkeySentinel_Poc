package Server.Poc.global.sessionConnector;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class ValkeyConfig {

    @Value("${session.write.nodes}")
    private String writeNodes; // Write 인스턴스

    @Value("${session.read.nodes}")
    private String readNodes; // Read 인스턴스

    // ======================= Write (Round Robin) without Pool Ver2.0 =========================

    @Bean(name = "sessionWriteTemplates")
    public List<RedisTemplate<String, String>> sessionWriteTemplates() {
        List<RedisTemplate<String, String>> templates = new ArrayList<>();
        for (HostPort hp : parseNodes(writeNodes)) { // Write 노드 파싱

            /* 단순 Factory 생성 (Connection Pool) */
            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(hp.host, hp.port);
            LettuceConnectionFactory cf = new LettuceConnectionFactory(config);
            cf.afterPropertiesSet(); // 초기화
            templates.add(buildTemplate(cf));
        }
        return templates;
    }

    // ======================= Read (Round Robin) with Pool Ver3.0 =========================

    @Bean(name = "sessionReadTemplates")
    public List<RedisTemplate<String, String>> sessionReadTemplates() {
        List<RedisTemplate<String, String>> templates = new ArrayList<>();
        for (HostPort hp : parseNodes(readNodes)) { // Read 노드 파싱

            /* Connection Pool 적용 */
            LettuceConnectionFactory cf = createPooledFactory(hp.host, hp.port);
            cf.afterPropertiesSet();
            templates.add(buildTemplate(cf));
        }
        return templates;
    }

    // ======================= Helper for pooled Read factories =========================

    // Helper method inside the same configuration class
    private LettuceConnectionFactory createPooledFactory(String host, int port) {
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration(host, port);

        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(64);
        poolConfig.setMinIdle(16);

        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(2))
                .poolConfig(poolConfig)
                .build();

        return new LettuceConnectionFactory(serverConfig, clientConfig);
    }

    // ======================= 공용 템플릿 빌더 =========================

    /* 증복 제거용 Helper 메서드 */
    // RedisTemplate 생성 시 중복되는 초기화 로직을 공용 메서드로 뺀 것.
    // sessionWriteTemplate / sessionReadTemplate 모두 동일한 방식으로 일관성 있게 초기화
    private RedisTemplate<String, String> buildTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 문자열을 Java 객체 리스트로 변환하는 파싱 유틸리티
     * @param nodesCsv 대상 노드 문자열
     * @return 포트 리스트 반환
     */
    private List<HostPort> parseNodes(String nodesCsv) {

        // 구분자를 기준으로 application.properties에서 값(대상 노드)을 반환
        return List.of(nodesCsv.split(","))
                .stream()
                .map(s -> s.trim()) // 공백 제거
                .filter(s -> !s.isEmpty()) // 빈 문자열 제거
                .map(s -> {
                    String[] hp = s.split(":"); // 각 "host:port" 문자열을 ":"로 분리.
                    return new HostPort(hp[0], Integer.parseInt(hp[1]));
                })
                .collect(Collectors.toList());
    }

    /**
     * Redis 노드의 host, port 정보를 담는 단순 DTO
     */
    private static class HostPort {
        final String host;
        final int port;

        HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

}

