package com.epam.finaltask.auth.bruteforce;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class BruteForceDefenceMechanism {
    private static final int MAX_ATTEMPT = 5;
    private static final long LOCK_TIME = TimeUnit.MINUTES.toMillis(30);

    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lockCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String key) {
        attemptsCache.remove(key);
        lockCache.remove(key);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.getOrDefault(key, 0);
        attempts++;
        attemptsCache.put(key, attempts);
        if (attempts >= MAX_ATTEMPT) {
            lockCache.put(key, System.currentTimeMillis());
        }
    }

    public boolean isBlocked(String key) {
        if (!lockCache.containsKey(key)) {
            return false;
        }

        long lockTime = lockCache.get(key);
        if (System.currentTimeMillis() - lockTime > LOCK_TIME) {
            lockCache.remove(key);
            return false;
        }

        return true;
    }
}
