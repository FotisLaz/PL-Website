package com.example.pl_connect.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

// The test profile uses the in-memory cache; this test checks the Redis cache config
@SpringBootTest(properties = "spring.cache.type=redis")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
class CacheTtlTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void cachedPlayerQueriesExpireAfterTenMinutesByDefault() {
        // Getting the cache object does not connect to Redis
        RedisCache cache = (RedisCache) cacheManager.getCache("playersByTeam");

        assertThat(cache).isNotNull();
        assertThat(cache.getCacheConfiguration().getTtl()).isEqualTo(Duration.ofMinutes(10));
    }
}
