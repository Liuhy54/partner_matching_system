package com.hai_friend.hai_friend_backend.contant;

/**
 * Redisson constant interface
 */
public interface redissonConstant {
    /**
     * redis channel name
     */
    String REDIS_CHANNEL_USER_RECOMMEND = "haiFriend:user:recommend:";


    String REDIS_CHANNEL_PRECACHEJOB_CACHE_LOCK = "haiFriend:cache:lock:";
}
