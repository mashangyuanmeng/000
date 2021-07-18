package java.util.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <pre>
 *     可以用于多线程计算数据，最后合并计算结果的场景。
 *     CountDownLatch 是一次性的，CyclicBarrier 是可循环利用的；
 *     CountDownLatch 参与的线程的职责是不一样的，有的在倒计时，有的在等待倒计时结束。
 * </pre>
 *
 * <pre>
 *     几个概念：
 *          1、栅栏：拦截所有线程的一道门，需要等到所有线程都达到栅栏，才会开门；
 *          2、换代：即所有线程均已到达栅栏，打破栅栏后需要重置计数器，并将所有线程唤醒；
 *          3、
 * </pre>
 * <pre>
 *     示例代码：
 *     https://www.jianshu.com/p/333fd8faa56e
 *
 *     public class CyclicBarrierDemo {
 *
 *      static class TaskThread extends Thread {
 *
 *         CyclicBarrier barrier;
 *
 *         public TaskThread(CyclicBarrier barrier) {
 *             this.barrier = barrier;
 *         }
 *
 *         @Override
 *         public void run() {
 *             try {
 *                 Thread.sleep(1000);
 *                 System.out.println(getName() + " 到达栅栏 A");
 *                 barrier.await();
 *                 System.out.println(getName() + " 冲破栅栏 A");
 *
 *                 Thread.sleep(2000);
 *                 System.out.println(getName() + " 到达栅栏 B");
 *                 barrier.await();
 *                 System.out.println(getName() + " 冲破栅栏 B");
 *             } catch (Exception e) {
 *                 e.printStackTrace();
 *             }
 *         }
 *     }
 *
 *     public static void main(String[] args) {
 *         int threadNum = 5;
 *         CyclicBarrier barrier = new CyclicBarrier(threadNum, new Runnable() {
 *
 *             @Override
 *             public void run() {
 *                 System.out.println(Thread.currentThread().getName() + " 完成最后任务");
 *             }
 *         });
 *         for(int i = 0; i < threadNum; i++) {
 *             new TaskThread(barrier).start();
 *         }
 *     }
 * }
 * </pre>
 * @since 1.5
 * @see CountDownLatch
 * @author Doug Lea
 */
public class CyclicBarrier {

    /**
     * 内部类，用该类的对象代表栅栏的当前代，这里代的概念，就是 count 值减小到 0，就会换一代「也就是所有线程都已经到达栅栏屏障，此时就是换代的时候了」
     */
    private static class Generation {
        // 是否冲破了栅栏屏障，默认是没有冲破，只有当全部线程都到达栅栏屏障，才算打破
        boolean broken = false;
    }

    /**
     * 同步操作锁
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * 线程拦截器
     */
    private final Condition trip = lock.newCondition();

    /**
     * 每次需要拦截的线程数
     */
    private final int parties;

    /**
     * 当前正在执行的任务
     */
    private final Runnable barrierCommand;

    /**
     * 当前代
     */
    private Generation generation = new Generation();

    /**
     * 计数器，初始值和需要拦截的线程数一致
     */
    private int count;

    /**
     * 1、唤醒所有线程；
     * 2、重置计数器；
     * 3、换代；
     */
    private void nextGeneration() {
        // signal completion of last generation
        trip.signalAll();
        // set up next generation
        count = parties;
        generation = new Generation();
    }

    /**
     * 全部线程都已经到达栅栏屏障，打破栅栏屏障，重置计数器，并唤醒所有线程
     */
    private void breakBarrier() {
        generation.broken = true;
        count = parties;
        trip.signalAll();
    }

    /**
     * 核心等待方法
     * @param timed 是否等待
     * @param nanos 等待的时间
     * @return
     * @throws InterruptedException
     * @throws BrokenBarrierException
     * @throws TimeoutException
     */
    private int dowait(boolean timed, long nanos)
        throws InterruptedException, BrokenBarrierException,
               TimeoutException {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final Generation g = generation;
            if (g.broken) {
                /**
                 * 检查当前栅栏是否被打破，如果打破则抛出异常
                 */
                throw new BrokenBarrierException();
            }
            if (Thread.interrupted()) {
                /**
                 * 检查当前线程是否被中断，如果线程被中断的话，会依次执行下面步骤：
                 * 1、打破当前栅栏
                 * 2、唤醒所有线程
                 * 3、抛出中断异常
                 */
                breakBarrier();
                throw new InterruptedException();
            }
            /**
             * 栅栏没有被打破，线程没有异常，继续执行
             */
            int index = --count;
            if (index == 0) {
                /**
                 * 经过当前线程后，计数器为 0，需要换代，栅栏会被打破，需要唤醒其他线程
                 */
                boolean ranAction = false;
                try {
                    final Runnable command = barrierCommand;
                    if (command != null) {
                        command.run();
                    }
                    ranAction = true;
                    // 换代
                    nextGeneration();
                    return 0;
                } finally {
                    if (!ranAction) {
                        breakBarrier();
                    }
                }
            }

            // loop until tripped, broken, interrupted, or timed out
            for (;;) {
                try {
                    if (!timed) {
                        trip.await();
                    } else if (nanos > 0L) {
                        nanos = trip.awaitNanos(nanos);
                    }
                } catch (InterruptedException ie) {
                    if (g == generation && ! g.broken) {
                        breakBarrier();
                        throw ie;
                    } else {
                        // We're about to finish waiting even if we had not
                        // been interrupted, so this interrupt is deemed to
                        // "belong" to subsequent execution.
                        Thread.currentThread().interrupt();
                    }
                }
                if (g.broken) {
                    throw new BrokenBarrierException();
                }
                if (g != generation) {
                    return index;
                }
                if (timed && nanos <= 0L) {
                    breakBarrier();
                    throw new TimeoutException();
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 构造函数
     * @param parties 参与线程的个数
     * @param barrierAction 需要执行的任务
     */
    public CyclicBarrier(int parties, Runnable barrierAction) {
        if (parties <= 0) {
            throw new IllegalArgumentException();
        }
        this.parties = parties;
        this.count = parties;
        this.barrierCommand = barrierAction;
    }

    /**
     * 构造函数
     * @param parties 参与线程的个数
     */
    public CyclicBarrier(int parties) {
        this(parties, null);
    }

    /**
     * 获取参数线程的个数
     */
    public int getParties() {
        return parties;
    }

    /**
     * 线程调用 await() 表示自己已经到达栅栏，一直等待
     * @return
     * @throws InterruptedException
     * @throws BrokenBarrierException 表示栅栏已经被破坏，破坏的原因可能是其中一个线程 await() 时被中断或者超时
     */
    public int await() throws InterruptedException, BrokenBarrierException {
        try {
            return dowait(false, 0L);
        } catch (TimeoutException toe) {
            throw new Error(toe); // cannot happen
        }
    }

    /**
     * 线程调用 await() 表示自己已经到达栅栏，等待一定的时间
     * @param timeout 等待的时间
     * @param unit 时间单位
     * @return
     * @throws InterruptedException
     * @throws BrokenBarrierException
     * @throws TimeoutException
     */
    public int await(long timeout, TimeUnit unit)
        throws InterruptedException,
               BrokenBarrierException,
               TimeoutException {
        return dowait(true, unit.toNanos(timeout));
    }

    /**
     * 判断所有线程是否到达栅栏屏障
     */
    public boolean isBroken() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return generation.broken;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 重置栅栏屏障
     */
    public void reset() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            breakBarrier();   // break the current generation
            nextGeneration(); // start a new generation
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前已经到达栅栏屏障的线程数
     */
    public int getNumberWaiting() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return parties - count;
        } finally {
            lock.unlock();
        }
    }
}
