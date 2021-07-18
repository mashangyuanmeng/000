package java.lang;

/**
 * 所有类的父类，如果一个类没有显式继承任何类，则会隐式继承 Object 类
 * @author  unascribed
 * @see     java.lang.Class
 * @since   JDK1.0
 */
public class Object {

    private static native void registerNatives();

    static {
        registerNatives();
    }

    /**
     *
     */
    public final native Class<?> getClass();

    /**
     * 调用本地方法获取 hashCode
     */
    public native int hashCode();

    /**
     * 使用的是比较地址，所以子类需要重写该方法
     */
    public boolean equals(Object obj) {
        return (this == obj);
    }

    /**
     * 返回一个对象的拷贝：这个拷贝存在于堆中地址不同于被拷贝的对象「浅拷贝」
     */
    protected native Object clone() throws CloneNotSupportedException;

    /**
     * 类型 + @符号 + 哈希值转为 16 进制
     */
    public String toString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    /**
     * 唤醒在此对象监视器上等待的单个线程
     */
    public final native void notify();

    /**
     * 唤醒在此对象监视器上等待的所有线程
     */
    public final native void notifyAll();

    /**
     * 使当前线程释放锁，并进入超时等待；
     * Thread 类中 sleep 方法不会释放锁。
     * @See Thread
     */
    public final native void wait(long timeout) throws InterruptedException;

    /**
     * 使当前线程释放锁，并进入超时等待
     */
    public final void wait(long timeout, int nanos) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }
        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "nanosecond timeout value out of range");
        }
        if (nanos > 0) {
            timeout++;
        }
        wait(timeout);
    }

    /**
     * 使当前线程释放锁，并进入等待
     */
    public final void wait() throws InterruptedException {
        wait(0);
    }

    /**
     * 当垃圾回收器确定不存在对该对象的更多引用时，由对象的垃圾回收器调用此方法
     */
    protected void finalize() throws Throwable { }
}
