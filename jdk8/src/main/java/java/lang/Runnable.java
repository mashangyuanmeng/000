package java.lang;

/**
 *
 * @author  Arthur van Hoff
 * @see     java.lang.Thread
 * @see     java.util.concurrent.Callable
 * @since   JDK1.0
 */
@FunctionalInterface
public interface Runnable {
    /**
     *
     * @see     java.lang.Thread#run()
     */
    void run();
}
