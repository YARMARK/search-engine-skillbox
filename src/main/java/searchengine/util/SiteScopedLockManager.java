package searchengine.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SiteScopedLockManager {

    private final ConcurrentMap<Integer, ReentrantLock> siteIdToLock = new ConcurrentHashMap<>();

    public void executeWithLock(int siteId, Runnable runnable) {
        ReentrantLock lock = siteIdToLock.computeIfAbsent(siteId, id -> new ReentrantLock());
        lock.lock();
        try {
            runnable.run();
        } finally {
            lock.unlock();
        }
    }
} 