package com.nature.distribution.util;

import com.nature.distribution.definition.CacheProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存工具类
 * @author nature
 * @version 1.0.0
 * @since 2018/11/22 10:06
 */
public class CacheUtil {

    /**
     * 缓存提供者
     */
    private static CacheProvider cacheProvider;

    /**
     * 设置缓存提供者
     * @param cacheProvider 缓存提供者
     */
    public static void setCacheProvider(CacheProvider cacheProvider) {
        CacheUtil.cacheProvider = cacheProvider;
    }

    /**
     * 获取缓存提供者
     * @return 缓存提供者
     */
    private static CacheProvider getCacheProvider() {
        if (CacheUtil.cacheProvider == null) {
            throw new NullPointerException("cache provider is null");
        }
        return CacheUtil.cacheProvider;
    }

    /**
     * 设置值
     * @param key   键
     * @param value 值
     */
    public static void set(String key, Object value) {
        getCacheProvider().set(key, value);
    }

    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    public static Object get(String key) {
        return getCacheProvider().get(key);
    }

    /**
     * 获取值
     * @param key 键
     * @return 值
     */
    @SuppressWarnings("all")
    public static <V> V get(String key, Class<V> vClass) {
        return (V) getCacheProvider().get(key);
    }

    /**
     * 是否有key
     * @param key key
     * @return 结果
     */
    public static boolean hasKey(String key) {
        return getCacheProvider().hasKey(key);
    }

    /**
     * 递增并获取结果
     * @param key   key
     * @param value 增量
     * @return 结果
     */
    public static Integer incrementAndGet(String key, int value) {
        return getCacheProvider().incrementAndGet(key, value);
    }

    /**
     * 批量清除key
     * @param key key集合
     */
    public static void delete(String key) {
        getCacheProvider().remove(key);
    }

    /**
     * 批量清除key
     * @param keys key集合
     */
    public static void delete(Set<String> keys) {
        getCacheProvider().delete(keys);
    }

    /**
     * 锁定指定时间
     * @param lockKey     锁定key
     * @param lockSeconds 锁定时间（秒）
     * @return 锁定结果
     */
    public static boolean lock(String lockKey, Long lockSeconds) {
        return getCacheProvider().lock(lockKey, lockSeconds);
    }

    /**
     * 解锁
     * @param lockKey 锁定key
     */
    public static void unlock(String lockKey) {
        getCacheProvider().remove(lockKey);
    }

    /**
     * 设置过期时间
     * @param key        键
     * @param expireTime 过期时间
     */
    public static void expire(String key, Long expireTime) {
        getCacheProvider().expire(key, expireTime);
    }

    /**
     * 设置哈希值
     * @param key     map对应key
     * @param hashKey map中的哈希key
     * @param value   值
     */
    public static void setHash(String key, String hashKey, Object value) {
        getCacheProvider().setHash(key, hashKey, value);
    }

    /**
     * 获取哈希值
     * @param key     map对应key
     * @param hashKey map中的哈希key
     * @return 值
     */
    public static Object getHash(String key, String hashKey) {
        return getCacheProvider().getHash(key, hashKey);
    }

    /**
     * 获取哈希值
     * @param key     map对应key
     * @param hashKey map中的哈希key
     * @param vClass  值类型
     * @return 值
     */
    public static <V> V getHash(String key, String hashKey, Class<V> vClass) {
        return getCacheProvider().getHash(key, hashKey, vClass);
    }

    /**
     * 从map中删除
     * @param key     key
     * @param hashKey hashKey
     */
    public static void deleteHash(String key, String hashKey) {
        getCacheProvider().deleteHash(key, hashKey);
    }

    /**
     * 获取整个map
     * @param key 键
     * @return map
     */
    public static Map<String, Object> getMap(String key) {
        return getCacheProvider().getMap(key);
    }

    /**
     * 获取整个map的key集合
     * @param key 键
     * @return set
     */
    public static Set<String> getMapKeys(String key) {
        return getCacheProvider().getMapKeys(key);
    }

    /**
     * 获取指定map的全部值集合
     * @param key key
     * @return 指定map的全部值集合
     */
    public static List<Object> getMapValues(String key) {
        return getCacheProvider().getMapValues(key);
    }

    /**
     * 添加到set
     * @param key   键
     * @param value 值
     */
    public static void addToSet(String key, Object value) {
        getCacheProvider().addToSet(key, value);
    }

    /**
     * set中是否存在
     * @param key   键
     * @param value 值
     * @return true：存在
     */
    public static boolean hasInSet(String key, Object value) {
        return getCacheProvider().hasInSet(key, value);
    }

    /**
     * 获取set size
     * @param key 键
     * @return size
     */
    public static int getSetSize(String key) {
        return getCacheProvider().getSetSize(key);
    }

    /**
     * 从缓存中取出set
     * @param key    键
     * @param vClass 值类
     * @param <V>    值类
     * @return set
     */
    public static <V> Set<V> getSet(String key, Class<V> vClass) {
        return getCacheProvider().getSet(key, vClass);
    }

    /**
     * 按匹配规则查询全部key
     * @param pattern 匹配规则
     * @return keys
     */
    public static Set<String> keys(String pattern) {
        return getCacheProvider().keys(pattern);
    }

    /**
     * 添加到list
     * @param key   key
     * @param datum 数据
     */
    public static void addToList(String key, Object datum) {
        getCacheProvider().addToList(key, datum);
    }

    /**
     * 全部添加到list
     * @param key     key
     * @param objects 集合
     */
    public static void addAllToList(String key, Collection<Object> objects) {
        getCacheProvider().addAllToList(key, objects);
    }

    /**
     * 从list中弹出
     * @param key    key
     * @param vClass 类型
     * @param <V>    类型
     * @return 数据
     */
    public static <V> V popFromList(String key, Class<V> vClass) {
        return getCacheProvider().popFromList(key, vClass);
    }

    /**
     * 获取整个list
     * @param key key
     * @return list
     */
    public static List<Object> getList(String key) {
        return getCacheProvider().getList(key);
    }

    /**
     * 获取list size
     * @param key key
     * @return size
     */
    public static Integer getListSize(String key) {
        return getCacheProvider().getListSize(key);
    }

    /**
     * 获取子list
     * @param key   key
     * @param start 开始位置
     * @param end   结束位置
     * @return 子集
     */
    public static List<Object> sublist(String key, int start, int end) {
        return getCacheProvider().sublist(key, start, end);
    }

}
