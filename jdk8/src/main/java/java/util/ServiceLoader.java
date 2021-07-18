package java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;


/**
 * <pre>
 *     service provider interface JDK内置的中服务发现机制；
 *     Java spi 机制，比较广泛的运用在 spring 自动装配，dubbo 的扩展。
 *     spi 机制就是Java提供的一套用来被第三方实现或者扩展的接口，它可以用来启用框架扩展和替换组件，其作用就是为这些被扩展的 API 寻找服务实现。
 *     也就是服务的提供者可能会提供服务的实现，但是我们可以通过 spi 机制，自己去实现服务
 *     https://www.cnblogs.com/jy107600/p/11464985.html
 *     https://my.oschina.net/kipeng/blog/1789849
 * </pre>
 */
public final class ServiceLoader<S> implements Iterable<S> {

    /**
     * 指定 spi 配置文件的路径，
     * spring.factories
     */
    private static final String PREFIX = "META-INF/services/";

    /**
     * 被加载的类或接口
     */
    private final Class<S> service;

    /**
     * 用于定位、加载和实例化实现方实现的类的类加载器
     */
    private final ClassLoader loader;

    /**
     * 上下文对象
     */
    private final AccessControlContext acc;

    /**
     * 按照实例化的顺序缓存已经实例化的类
     */
    private LinkedHashMap<String, S> providers = new LinkedHashMap<>();

    /**
     * 懒查找迭代器
     */
    private LazyIterator lookupIterator;

    /**
     * 清空已经实例化的类缓存，重新创建迭代器
     */
    public void reload() {
        providers.clear();
        lookupIterator = new LazyIterator(service, loader);
    }

    /**
     *
     * @param svc
     * @param cl
     */
    private ServiceLoader(Class<S> svc, ClassLoader cl) {
        service = Objects.requireNonNull(svc, "Service interface cannot be null");
        loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
        acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
        reload();
    }

    private static void fail(Class<?> service, String msg, Throwable cause)
            throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg,
                cause);
    }

    private static void fail(Class<?> service, String msg)
            throws ServiceConfigurationError {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);
    }

    private static void fail(Class<?> service, URL u, int line, String msg)
            throws ServiceConfigurationError {
        fail(service, u + ":" + line + ": " + msg);
    }

    /**
     * 解析 spi 配置文件的一行内容
     * @param service
     * @param u
     * @param r
     * @param lc
     * @param names
     * @return
     * @throws IOException
     * @throws ServiceConfigurationError
     */
    private int parseLine(Class<?> service,
                          URL u,
                          BufferedReader r,
                          int lc,
                          List<String> names) throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;
        }
        // 判断是否包含#号
        int ci = ln.indexOf('#');
        if (ci >= 0) {
            // 包含#号
            ln = ln.substring(0, ci);
        }
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0)) {
                // 包含空格或者制表符的话，直接报错
                fail(service, u, lc, "Illegal configuration-file syntax");
            }
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp)) {
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.')) {
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
                }
            }
            if (!providers.containsKey(ln) && !names.contains(ln)) {
                names.add(ln);
            }
        }
        return lc + 1;
    }

    /**
     * 解析 spi 配置文件
     * @param service
     * @param u
     * @return
     * @throws ServiceConfigurationError
     */
    private Iterator<String> parse(Class<?> service,
                                   URL u) throws ServiceConfigurationError {
        InputStream in = null;
        BufferedReader r = null;
        // 用来存储实例的名称
        ArrayList<String> names = new ArrayList<>();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            // 经过下面的 while 循环，spi 配置文件中待加载的实例名称都会被 add 到 names 中
            while ((lc = parseLine(service, u, r, lc, names)) >= 0);
        } catch (IOException x) {
            fail(service, "Error reading configuration file", x);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException y) {
                fail(service, "Error closing configuration file", y);
            }
        }
        return names.iterator();
    }

    /**
     * 私有内部类，提供对所有的 service 的类的加载与实例化
     */
    private class LazyIterator implements Iterator<S> {
        Class<S> service;
        ClassLoader loader;
        Enumeration<URL> configs = null;
        Iterator<String> pending = null;
        String nextName = null;

        private LazyIterator(Class<S> service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        private boolean hasNextService() {
            if (nextName != null) {
                return true;
            }
            if (configs == null) {
                try {
                    String fullName = PREFIX + service.getName();
                    if (loader == null) {
                        configs = ClassLoader.getSystemResources(fullName);
                    } else {
                        configs = loader.getResources(fullName);
                    }
                } catch (IOException x) {
                    fail(service, "Error locating configuration files", x);
                }
            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;
                }
                pending = parse(service, configs.nextElement());
            }
            nextName = pending.next();
            return true;
        }

        private S nextService() {
            if (!hasNextService()) {
                throw new NoSuchElementException();
            }
            String cn = nextName;
            nextName = null;
            Class<?> c = null;
            try {
                c = Class.forName(cn, false, loader);
            } catch (ClassNotFoundException x) {
                fail(service,
                        "Provider " + cn + " not found");
            }
            if (!service.isAssignableFrom(c)) {
                fail(service,
                        "Provider " + cn + " not a subtype");
            }
            try {
                S p = service.cast(c.newInstance());
                providers.put(cn, p);
                return p;
            } catch (Throwable x) {
                fail(service,
                        "Provider " + cn + " could not be instantiated",
                        x);
            }
            throw new Error();          // This cannot happen
        }

        public boolean hasNext() {
            if (acc == null) {
                return hasNextService();
            } else {
                PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return hasNextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        public S next() {
            if (acc == null) {
                return nextService();
            } else {
                PrivilegedAction<S> action = new PrivilegedAction<S>() {
                    public S run() {
                        return nextService();
                    }
                };
                return AccessController.doPrivileged(action, acc);
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     *
     */
    public Iterator<S> iterator() {
        return new Iterator<S>() {
            Iterator<Map.Entry<String, S>> knownProviders = providers.entrySet().iterator();

            public boolean hasNext() {
                if (knownProviders.hasNext()) {
                    return true;
                }
                return lookupIterator.hasNext();
            }

            public S next() {
                if (knownProviders.hasNext()) {
                    return knownProviders.next().getValue();
                }
                return lookupIterator.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     *
     */
    public static <S> ServiceLoader<S> load(Class<S> service,
                                            ClassLoader loader) {
        return new ServiceLoader<>(service, loader);
    }

    /**
     *
     */
    public static <S> ServiceLoader<S> load(Class<S> service) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return ServiceLoader.load(service, cl);
    }

    /**
     *
     */
    public static <S> ServiceLoader<S> loadInstalled(Class<S> service) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        ClassLoader prev = null;
        while (cl != null) {
            prev = cl;
            cl = cl.getParent();
        }
        return ServiceLoader.load(service, prev);
    }

    /**
     *
     */
    public String toString() {
        return "java.util.ServiceLoader[" + service.getName() + "]";
    }

}
