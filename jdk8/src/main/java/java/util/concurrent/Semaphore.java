package java.util.concurrent;

import java.util.Collection;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * <pre>
 *     实现限流的类，
 * </pre>
 */
public class Semaphore implements java.io.Serializable {

    private static final long serialVersionUID = -3222578661600680210L;

    /** All mechanics via AbstractQueuedSynchronizer subclass */
    private final Sync sync;

    /**
     * 内部类，也是继承自 AQS
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1192457210091910933L;

        /**
         * 这里的 permits 就是资源数目，也就是限流的最大值
         * @param permits
         */
        Sync(int permits) {
            setState(permits);
        }

        final int getPermits() {
            return getState();
        }

        /**
         * 非公平的共享模式
         * @param acquires
         * @return
         */
        final int nonfairTryAcquireShared(int acquires) {
            for (;;) {
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining))
                    return remaining;
            }
        }

        /**
         * 释放资源
         * @param releases
         * @return
         */
        protected final boolean tryReleaseShared(int releases) {
            for (;;) {
                int current = getState();
                int next = current + releases;
                if (next < current) // overflow
                    throw new Error("Maximum permit count exceeded");
                if (compareAndSetState(current, next))
                    return true;
            }
        }

        final void reducePermits(int reductions) {
            for (;;) {
                int current = getState();
                int next = current - reductions;
                if (next > current) // underflow
                    throw new Error("Permit count underflow");
                if (compareAndSetState(current, next))
                    return;
            }
        }

        final int drainPermits() {
            for (;;) {
                int current = getState();
                if (current == 0 || compareAndSetState(current, 0))
                    return current;
            }
        }
    }

    /**
     * 非公平『默认』
     */
    static final class NonfairSync extends Sync {

        private static final long serialVersionUID = -2694183684443567898L;

        NonfairSync(int permits) {
            super(permits);
        }

        protected int tryAcquireShared(int acquires) {
            return nonfairTryAcquireShared(acquires);
        }
    }

    /**
     * 公平
     */
    static final class FairSync extends Sync {

        private static final long serialVersionUID = 2014338818796000944L;

        FairSync(int permits) {
            super(permits);
        }

        protected int tryAcquireShared(int acquires) {
            for (;;) {
                if (hasQueuedPredecessors()) {
                    return -1;
                }
                int available = getState();
                int remaining = available - acquires;
                if (remaining < 0 ||
                    compareAndSetState(available, remaining)) {
                    return remaining;
                }
            }
        }
    }

    /**
     *
     */
    public Semaphore(int permits) {
        sync = new NonfairSync(permits);
    }

    /**
     * 创建限流对象，指定是否公平，默认为非公平
     */
    public Semaphore(int permits, boolean fair) {
        sync = fair ? new FairSync(permits) : new NonfairSync(permits);
    }

    /**
     *
     */
    public void acquire() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     *
     */
    public void acquireUninterruptibly() {
        sync.acquireShared(1);
    }

    /**
     *
     */
    public boolean tryAcquire() {
        return sync.nonfairTryAcquireShared(1) >= 0;
    }

    /**
     *
     */
    public boolean tryAcquire(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     *
     */
    public void release() {
        sync.releaseShared(1);
    }

    /**
     *
     */
    public void acquire(int permits) throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireSharedInterruptibly(permits);
    }

    /**
     *
     */
    public void acquireUninterruptibly(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.acquireShared(permits);
    }

    /**
     *
     */
    public boolean tryAcquire(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.nonfairTryAcquireShared(permits) >= 0;
    }

    /**
     *
     */
    public boolean tryAcquire(int permits, long timeout, TimeUnit unit)
        throws InterruptedException {
        if (permits < 0) throw new IllegalArgumentException();
        return sync.tryAcquireSharedNanos(permits, unit.toNanos(timeout));
    }

    /**
     *
     */
    public void release(int permits) {
        if (permits < 0) throw new IllegalArgumentException();
        sync.releaseShared(permits);
    }

    /**
     *
     */
    public int availablePermits() {
        return sync.getPermits();
    }

    /**
     *
     */
    public int drainPermits() {
        return sync.drainPermits();
    }

    /**
     *
     */
    protected void reducePermits(int reduction) {
        if (reduction < 0) throw new IllegalArgumentException();
        sync.reducePermits(reduction);
    }

    /**
     *
     */
    public boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     *
     */
    public final boolean hasQueuedThreads() {
        return sync.hasQueuedThreads();
    }

    /**
     *
     */
    public final int getQueueLength() {
        return sync.getQueueLength();
    }

    /**
     *
     */
    protected Collection<Thread> getQueuedThreads() {
        return sync.getQueuedThreads();
    }

    /**
     *
     */
    public String toString() {
        return super.toString() + "[Permits = " + sync.getPermits() + "]";
    }
}
