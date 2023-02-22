package com.user.py.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @Author ice
 * @Date 2022/10/29 16:47
 * @PackageName:com.user.py.utils
 * @ClassName: RedisCache
 * @Description: 缓存工具类
 * @Version 1.0
 */
@Component
@SuppressWarnings({"all"})
@Slf4j
public class RedisCache {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 缓存map 数据
     *
     * @param key
     * @param map
     * @param <T>
     */
    public <T> boolean setCacheMap(final String key, final Map<String, T> map) {
        if (map != null) {
            try {
                redisTemplate.opsForHash().putAll(key, map);
            } catch (Exception e) {
                log.error("setCacheMap缓存失败");
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    public <T> boolean setCacheMap(final String key, final Map<String, T> map, final long timeout, final TimeUnit unit) {
        if (map != null) {
            try {
                redisTemplate.opsForHash().putAll(key, map);
                return expire(key, timeout, unit);
            } catch (Exception e) {
                log.error("setCacheMap缓存失败!");
                log.error(e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * 获得缓存的Map
     *
     * @param key
     * @return
     */
    public <T> Map<String, T> getCacheMap(final String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 判断 key是否存在
     *
     * @param key 键
     * @return true 存在 false不存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置有效时间
     *
     * @param key     Redis键
     * @param timeout 超时时间
     * @param unit    时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 删除key
     *
     * @param key
     * @return
     */
    public boolean deleteObject(final String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key      缓存的键值
     * @param value    缓存的值
     * @param timeout  时间
     * @param timeUnit 时间颗粒度
     */
    public <T> boolean setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("setCacheObject缓存失败。。。。");
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    public <T> T getCacheObject(final String key) {
        ValueOperations<String, T> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public <T> void increment(final String increment) {
        redisTemplate.opsForValue().increment(increment);
    }

    public <T> long setCacheList(final String key, final List<T> list) {
        Long count;
        try {
            count = redisTemplate.opsForList().rightPushAll(key, list);
        } catch (Exception e) {
            log.error("缓存失败。。。。");
            log.error(e.getMessage());
            return 0;
        }
        return count == null ? 0 : count;
    }

    public <T> long setCacheList(final String key, final List<T> list, final Integer timeout, final TimeUnit timeUnit) {
        Long count;
        try {
            count = redisTemplate.opsForList().rightPushAll(key, list);
            expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("List缓存失败。。");
            log.error(e.getMessage());
            return 0;
        }
        return count == null ? 0 : count;
    }

    public <T> List<T> getCacheList(final String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public long addCacheSet(final String key, final Object value,final Integer timeout, final TimeUnit timeUnit) {
        Long count = null;
        try {
            count = redisTemplate.opsForSet().add(key, value);
            expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Set缓存失败");
        }
        return count == null ? 0 : count;
    }

    public boolean isCachSet(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    public boolean removeCachSet(String key, Object value) {
        Long remove = redisTemplate.opsForSet().remove(key, value);
        return remove != null && remove > 0;
    }

    public Set<Object> getAllCachSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }
}
