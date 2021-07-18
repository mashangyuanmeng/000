package java.util.concurrent.locks;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 *     可重入锁，获取锁的方式为独占式，锁分为公平和非公平，默认非公平
 * </pre>
 *
 * @since 1.5
 * @author Doug Lea
 */
public class ReentrantLock implements Lock, java.io.Serializable {

    private static final long serialVersionUID = 7373984872572414699L;

    /**
     * 持有内部类，用以实现获取锁和释放锁
     */
    private final Sync sync;

    /**
     * 内部类，继承自抽象队列同步器，用于实现 AQS
     */
    abstract static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = -5179523762034025860L;

        /**
         * 获取锁的方法，由本类中非公平锁和公平锁实现
         */
        abstract void lock();

        /**
         * 独占式，非公平
         */
        final boolean nonfairTryAcquire(int acquires) {
            // 获取当前线程
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                /**
                 * 锁空闲，利用 CAS 获取锁，不用判断当前线程在队列中的位置，
                 * 这也是和公平锁的区别所在。
                 */
                if (compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                // 判断当前线程是否是持有锁的线程，如果是，则很大很可能会获取锁成功
                int nextc = c + acquires;
                if (nextc < 0) {
                    // overflow
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }

        /**
         * 独占式，释放资源
         * @param releases
         * @return
         */
        protected final boolean tryRelease(int releases) {
            int c = getState() - releases;
            if (Thread.currentThread() != getExclusiveOwnerThread()) {
                /**
                 * 判断释放资源的线程是否是资源所有者线程
                 */
                throw new IllegalMonitorStateException();
            }
            boolean free = false;
            if (c == 0) {
                free = true;
                setExclusiveOwnerThread(null);
            }
            setState(c);
            return free;
        }

        /**
         * 判读当前线程是否正在独占资源。只有用到condition才需要去实现它。
         * @return
         */
        protected final boolean isHeldExclusively() {
            return getExclusiveOwnerThread() == Thread.currentThread();
        }

        final ConditionObject newCondition() {
            return new ConditionObject();
        }

        // Methods relayed from outer class

        final Thread getOwner() {
            return getState() == 0 ? null : getExclusiveOwnerThread();
        }

        final int getHoldCount() {
            return isHeldExclusively() ? getState() : 0;
        }

        /**
         * 判断当前锁是否空闲
         * @return
         */
        final boolean isLocked() {
            return getState() != 0;
        }

        /**
         *
         */
        private void readObject(java.io.ObjectInputStream s)
            throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            setState(0); // reset to unlocked state
        }
    }

    /**
     * 内部类，非公平锁，默认是非公平
     */
    static final class NonfairSync extends Sync {

        private static final long serialVersionUID = 7316153563782823691L;

        /**
         * 获取锁『非公平』
         */
        final void lock() {
            if (compareAndSetState(0, 1)) {
                // 锁空闲，直接获取锁
                setExclusiveOwnerThread(Thread.currentThread());
            } else {
                //
                acquire(1);
            }
        }

        /**
         * 获取锁『非公平』
         * @param acquires
         * @return
         */
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * 内部类，公平锁
     */
    static final class FairSync extends Sync {

        private static final long serialVersionUID = -3000897897090466540L;

        /**
         * 获取锁『公平』
         */
        final void lock() {
            acquire(1);
        }

        /**
         * 获取锁『公平』
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            if (c == 0) {
                /**
                 * hasQueuedPredecessors() 用于判断线程是否需要排队，
                 * 因为 AQS 实现的队列是 FIFO，所以是有优先级的，等的久的线程需要优先获取锁。
                 * 如果需要排队则返回 true，那么就说明当前线程获取锁失败。
                 * 『大白话就是：如果队列里面就你一个或者队列里面一个都没有，那你可以去获取锁，不然的话，你就先歇菜，别人先排队的先获取锁』
                 */
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    setExclusiveOwnerThread(current);
                    return true;
                }
            } else if (current == getExclusiveOwnerThread()) {
                int nextc = c + acquires;
                if (nextc < 0) {
                    throw new Error("Maximum lock count exceeded");
                }
                setState(nextc);
                return true;
            }
            return false;
        }
    }

    /**
     * 创建锁对象，默认是非公平的锁
     */
    public ReentrantLock() {
        sync = new NonfairSync();
    }

    /**
     * 指定锁是否公平
     */
    public ReentrantLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
    }

    /**
     * 获取锁
     */
    public void lock() {
        sync.lock();
    }

    /**
     *
     */
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    /**
     * 尝试获取锁，获取成功则返回 true
     */
    public boolean tryLock() {
        return sync.nonfairTryAcquire(1);
    }

    /**
     * 尝试获取锁，如果没能立即获取锁，则自旋等待指定的时间
     * @param timeout 等待的时间
     * @param unit 等待时间的单位
     * @return
     * @throws InterruptedException
     */
    public boolean tryLock(long timeout, TimeUnit unit)
            throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(timeout));
    }

    /**
     * 释放锁
     */
    public void unlock() {
        sync.release(1);
    }

    /**
     *
     */
    public Condition newCondition() {
        return sync.newCondition();
    }

    /**
     * 获取当前正在等待锁的线程数目
     * 示例代码：
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *   public void m() {
     *     assert lock.getHoldCount() == 0;
     *     lock.lock();
     *     try {
     *       // ... method body
     *     } finally {
     *       lock.unlock();
     *     }
     *   }
     * }}</pre>
     *
     * @return the number of holds on this lock by the current thread,
     *         or zero if this lock is not held by the current thread
     */
    public int getHoldCount() {
        return sync.getHoldCount();
    }

    /**
     * 判断当前线程是否持有该锁
     * 示例代码：
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert lock.isHeldByCurrentThread();
     *       // ... method body
     *   }
     * }}</pre>
     *
     * <p>It can also be used to ensure that a reentrant lock is used
     * in a non-reentrant manner, for example:
     *
     *  <pre> {@code
     * class X {
     *   ReentrantLock lock = new ReentrantLock();
     *   // ...
     *
     *   public void m() {
     *       assert !lock.isHeldByCurrentThread();
     *       lock.lock();
     *       try {
     *           // ... method body
     *       } finally {
     *           lock.unlock();
     *       }
     *   }
     * }}</pre>
     *
     * @return {@code true} if current thread holds this lock and
     *         {@code false} otherwise
     */
    public boolean isHeldByCurrentThread() {
        return sync.isHeldExclusively();
    }

    /**
     * 判断当前锁是否被持有
     */
    public boolean isLocked() {
        return sync.isLocked();
    }

    /**
     * 判断当前锁是否为公平锁
     */
    public final boolean isFair() {
        return sync instanceof FairSync;
    }

    /**
     * 获取持有该锁的线程
     */
    protected Thread getOwner() {
        return sync.getOwner();
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
    public final boolean hasQueuedThread(Thread thread) {
        return sync.isQueued(thread);
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
    public boolean hasWaiters(Condition condition) {
        if (condition == null)
            throw new NullPointerException();
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject))
            throw new IllegalArgumentException("not owner");
        return sync.hasWaiters((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     *
     */
    public int getWaitQueueLength(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return sync.getWaitQueueLength((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     *
     */
    protected Collection<Thread> getWaitingThreads(Condition condition) {
        if (condition == null) {
            throw new NullPointerException();
        }
        if (!(condition instanceof AbstractQueuedSynchronizer.ConditionObject)) {
            throw new IllegalArgumentException("not owner");
        }
        return sync.getWaitingThreads((AbstractQueuedSynchronizer.ConditionObject)condition);
    }

    /**
     *
     */
    public String toString() {
        Thread o = sync.getOwner();
        return super.toString() + ((o == null) ?
                                   "[Unlocked]" :
                                   "[Locked by thread " + o.getName() + "]");
    }
}
