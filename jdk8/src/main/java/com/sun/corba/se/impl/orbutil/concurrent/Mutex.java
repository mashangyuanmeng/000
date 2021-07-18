package com.sun.corba.se.impl.orbutil.concurrent;

/**
 * <pre>
 *     不可重入互斥锁，独占式
 *     锁资源（state）只有两种状态：0：未被锁定；1：锁定。
 * </pre>
 */
public class Mutex implements Sync {

    /**
     * <pre>
     *     锁的状态
     * </pre>
     **/
    protected boolean inuse_ = false;

    /**
     * <pre>
     *     获取锁，获取失败则
     * </pre>
     * @throws InterruptedException
     */
    public void acquire() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        synchronized (this) {
            try {
                while (inuse_) {
                    // 如果锁被其他线程持有，则等待，直到被唤醒
                    wait();
                }
                // 锁空闲，直接获取锁成功
                inuse_ = true;
            } catch (InterruptedException ex) {
                // 获取锁失败，唤醒其他等待该锁的线程
                notify();
                throw ex;
            }
        }
    }

    /**
     * <pre>
     *     释放锁，并唤醒其他等待该锁的线程
     * </pre>
     */
    public synchronized void release() {
        inuse_ = false;
        notify();
    }

    /**
     * <pre>
     *     尝试获取锁，获取成功则直接返回 true，没有获取则自旋指定的时间『单位为毫秒』
     * </pre>
     * @param msecs 自旋时间，毫秒值
     * @return
     * @throws InterruptedException
     */
    public boolean attempt(long msecs) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        synchronized (this) {
            if (!inuse_) {
                // 获取锁成功
                inuse_ = true;
                return true;
            } else if (msecs <= 0) {
                // 超时时间为负数，说明要么获取锁成功，要么直接返回获取锁失败（线程不进行等待）
                return false;
            } else {
                // 获取锁失败，进行循环等待，要么获取锁成功，要么获取锁超时
                long waitTime = msecs;
                long start = System.currentTimeMillis();
                try {
                    for (; ; ) {
                        wait(waitTime);
                        if (!inuse_) {
                            inuse_ = true;
                            return true;
                        } else {
                            waitTime = msecs - (System.currentTimeMillis() - start);
                            if (waitTime <= 0) {
                                // 等待时间已到，还没获取到锁
                                return false;
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    // 获取锁失败，唤醒其他线程
                    notify();
                    throw ex;
                }
            }
        }
    }
}