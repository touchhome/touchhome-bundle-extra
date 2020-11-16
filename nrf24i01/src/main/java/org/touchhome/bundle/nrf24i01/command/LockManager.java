package org.touchhome.bundle.nrf24i01.command;

import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class LockManager<T> {

    private Map<String, LockContext<T>> lockContextMap = new HashMap<>();

    public void signalAll(String key, T value) {
        if (lockContextMap.containsKey(key)) {
            lockContextMap.get(key).signalAll(value);
        } else {
            log.warn("Unable to find lock with key: " + key);
        }
    }

    public T await(String key, int time) {
        LockContext<T> lockContext = new LockContext<>();
        lockContextMap.put(key, lockContext);
        return lockContext.await(time);
    }

    private static class LockContext<T> {
        private final Condition condition;
        private final ReentrantLock lock;
        private T value;

        public LockContext() {
            this.lock = new ReentrantLock();
            this.condition = lock.newCondition();
        }

        public void signalAll(T value) {
            try {
                lock.lock();
                this.value = value;
                condition.signalAll();
            } catch (Exception ex) {
                log.error("Unrecognized error while call lock signalAll", ex);
            } finally {
                lock.unlock();
            }
        }

        public T await(int time) {
            try {
                lock.lock();
                condition.await(time, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                if (!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception ex) {
                log.error("No signal found for lock message");
            } finally {
                lock.unlock();
            }
            return value;
        }
    }
}
