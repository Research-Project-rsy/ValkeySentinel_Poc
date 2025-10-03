package Server.Poc.global.sessionConnector;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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

    @Value("${session.write.host}")
    private String sessionWriteHost; // Write 인스턴스 Host
    @Value("${session.write.port}")
    private int sessionWritePort; // Write 인스턴스 Port

    @Value("${session.read.nodes}")
    private String readNodes; // "host:port,host:port,..."

    // ======================= Write =========================
    @Bean(name = "sessionWriteTemplate")
    public RedisTemplate<String, String> sessionWriteTemplate(
            @Qualifier("sessionPrimaryConnectionFactory") RedisConnectionFactory connectionFactory) {
        return buildTemplate(connectionFactory);
    }

    @Bean(name = "sessionPrimaryConnectionFactory")
    @Primary // Primary 지정, auto-config 기본 Bean으로 사용됨
    public RedisConnectionFactory sessionPrimaryConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(sessionWriteHost);
        config.setPort(sessionWritePort);
        return new LettuceConnectionFactory(config);
    }

    // ======================= Read (Round Robin) with Pool Ver3.0
    // =========================
    @Bean(name = "sessionReadTemplates")
    public List<RedisTemplate<String, String>> sessionReadTemplates() {
        List<RedisTemplate<String, String>> templates = new ArrayList<>();
        for (HostPort hp : parseNodes(readNodes)) {
            LettuceConnectionFactory cf = createPooledFactory(hp.host, hp.port);
            templates.add(buildTemplate(cf));
        }
        return templates;
    }

    // ======================= Helper for pooled Read factories
    // =========================
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

    private List<HostPort> parseNodes(String nodesCsv) {
        return List.of(nodesCsv.split(","))
                .stream()
                .map(s -> s.trim())
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    String[] hp = s.split(":");
                    return new HostPort(hp[0], Integer.parseInt(hp[1]));
                })
                .collect(Collectors.toList());
    }

    private static class HostPort {
        final String host;
        final int port;

        HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

}
