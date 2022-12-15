package org.sunbird.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sunbird.common.util.CbExtServerProperties;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

	@Autowired
	CbExtServerProperties cbProperties;

	public JedisPoolConfig buildPoolConfig() {
		final JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(128);
		poolConfig.setMaxTotal(3000);
		poolConfig.setMinIdle(100);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(120));
		poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		return poolConfig;
	}

	@Bean
	public JedisPool jedisPool()
	{
		final JedisPoolConfig poolConfig = buildPoolConfig();
		JedisPool jedisPool = new JedisPool(poolConfig, cbProperties.getRedisHostName());
		return jedisPool;
	}
}
