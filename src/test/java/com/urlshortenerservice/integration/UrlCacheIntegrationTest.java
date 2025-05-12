package com.urlshortenerservice.integration;

import com.urlshortenerservice.service.cache.UrlCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class UrlCacheIntegrationTest {

    @Autowired
    private UrlCache urlCache;

    @Container
    private static final GenericContainer<?> redisContainer =
            new GenericContainer<>("redis:6.2")
                    .withExposedPorts(6379);

    @DynamicPropertySource
    public static void setupProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port",
                () -> redisContainer.getFirstMappedPort());
    }

    @Test
    public void saveAndGetUrlMapping_Success() {
        String hash = "abc123";
        String longUrl = "https://www.example.com/test";
        urlCache.saveUrlMapping(hash, longUrl);

        String retrievedUrl = urlCache.getLongUrl(hash);
        assertThat(retrievedUrl).isEqualTo(longUrl);
    }
}
