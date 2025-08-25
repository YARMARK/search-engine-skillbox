package searchengine.manager;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Менеджер локов, обеспечивающий эксклюзивное выполнение кода для конкретного сайта.
 * <p>
 * Позволяет безопасно выполнять операции по siteId с использованием ReentrantLock.
 */
@Component
public class SiteScopedLockManager {

    private final ConcurrentMap<Integer, ReentrantLock> siteIdToLock = new ConcurrentHashMap<>();

    /**
     * Выполняет переданный Runnable с блокировкой, специфичной для заданного siteId.
     *
     * @param siteId   идентификатор сайта
     * @param runnable код для выполнения под локом
     */
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
