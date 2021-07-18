package java.util.concurrent;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public interface ExecutorService extends Executor {

    /**
     * 在完成已提交的任务后封闭办事，不再接管新任务
     */
    void shutdown();

    /**
     * 停止所有正在履行的任务并封闭办事
     */
    List<Runnable> shutdownNow();

    /**
     * 测试是否该ExecutorService已被关闭
     */
    boolean isShutdown();

    /**
     * 测试是否所有任务都履行完毕了
     */
    boolean isTerminated();

    /**
     *
     */
    boolean awaitTermination(long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     * 可用来提交 Callable 任务，并返回代表此任务的Future 对象
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * 可用来提交 Runnable 任务，并返回代表此任务的Future 对象
     */
    <T> Future<T> submit(Runnable task, T result);

    /**
     *
     */
    Future<?> submit(Runnable task);

    /**
     *
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException;

    /**
     *
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,
                                  long timeout, TimeUnit unit)
        throws InterruptedException;

    /**
     *
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException;

    /**
     *
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks,
                    long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
