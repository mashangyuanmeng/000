package java.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * <pre>
 *      CountDownLatch 允许一个或多个线程等待其他线程完成操作。
 *      运用：
 *          一般都是主线程中创建 CountDownLatch 对象，指定计数器初始值，然后在子线程中对该计数器进行递减，
 *          此时主线程会阻塞，直到计数器减小到 0 ，主线程才会继续执行下去。
 * </pre>
 * @since 1.5
 * @author Doug Lea
 */
public class CountDownLatch {

    /**
     * 内部类，用于实现 AQS『抽象队列同步器』
     * Synchronization control For CountDownLatch.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 4982264981922014374L;

        /**
         * 构造函数，传入计数器
         * @param count
         */
        Sync(int count) {
            setState(count);
        }

        /**
         * 获取计数器的值
         * @return
         */
        int getCount() {
            return getState();
        }

        /**
         * 共享方式。尝试获取资源。负数表示获取失败；0表示获取成功，但没有剩余可用资源；正数表示获取成功，且有剩余资源。
         * @param acquires
         * @return
         */
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        /**
         * 共享方式。尝试释放资源，如果释放后允许唤醒后续等待结点返回true，否则返回false。
         * @param releases
         * @return
         */
        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0) {
                    return false;
                }
                int nextc = c-1;
                if (compareAndSetState(c, nextc)) {
                    return nextc == 0;
                }
            }
        }
    }

    /**
     * 持有内部类
     */
    private final Sync sync;

    /**
     * 构造函数，需要传入计数器初始值『唯一的构造函数』
     */
    public CountDownLatch(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count < 0");
        }
        // 将计数器给到 sync
        this.sync = new Sync(count);
    }

    /**
     * 用于阻塞调用该方法的线程，一般用于阻塞主线程
     */
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

    /**
     * 用于阻塞调用该方法的线程，一般用于阻塞主线程
     * @param timeout 阻塞的时间
     * @param unit 时间的单位
     * @return
     * @throws InterruptedException
     */
    public boolean await(long timeout, TimeUnit unit)
        throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }

    /**
     * 计数器递减
     */
    public void countDown() {
        sync.releaseShared(1);
    }

    /**
     * 获取当前计数器值
     */
    public long getCount() {
        return sync.getCount();
    }

    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
