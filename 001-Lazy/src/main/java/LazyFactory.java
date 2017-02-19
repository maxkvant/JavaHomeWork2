import javax.sql.rowset.spi.SyncProvider;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * class contains 4 factories for Lazy
 * @see Lazy
 */
public class LazyFactory {
    /**
     * Returns simple implementation of Lazy.
     */
    public static <T> Lazy<T> createLazySimple(Supplier<T> supplier) {
        return new LazySimple<>(supplier);
    }

    /**
     * Returns implementation of Lazy optimized for multi threads.
     */
    public static <T> Lazy<T> createLazyMultiThread(Supplier<T> supplier) {return new LazyMultiThread<>(supplier); }

    /**
     * Returns implementation of Lazy based on AtomicReferenceFieldUpdater.
     */
    public static <T> Lazy<T> createLazyAtomic(Supplier<T> supplier) {
        return new LazyAtomic<>(supplier);
    }

    /**
     * Returns implementation of Lazy based on AtomicReference.
     */
    public static <T> Lazy<T> createLazyAtomicReference(Supplier<T> supplier) {
        return new LazyAtomicReference<>(supplier);
    }


    private static abstract class AbstractLazy<T> implements Lazy<T> {
        protected final static Object nullObj = new Object();
        protected volatile Supplier<T> supplier;

        AbstractLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }
    }

    private static class LazySimple<T> extends AbstractLazy<T> {
        private volatile Object value = nullObj;

        LazySimple(Supplier<T> supplier) {
            super(supplier);
        }

        @Override
        public synchronized T get() {
            if (value == nullObj) {
                value = supplier.get();
                supplier = null;
            }
            return (T)value;
        }
    }

    private static class LazyMultiThread<T> extends AbstractLazy<T> {
        private volatile Object value = nullObj;

        LazyMultiThread(Supplier<T> supplier) {
            super(supplier);
        }

        @Override
        public T get() {
            if (value == nullObj) {
                synchronized (this) {
                    if (value == nullObj) {
                        value = supplier.get();
                    }
                }
            }
            return (T)value;
        }
    }

    private static class LazyAtomic<T> extends AbstractLazy<T> {
        private volatile Object value = nullObj;

        static final AtomicReferenceFieldUpdater<LazyAtomic, Object> atomicUpdater =
                AtomicReferenceFieldUpdater.newUpdater(LazyAtomic.class, Object.class, "value");

        LazyAtomic(Supplier<T> supplier) {
            super(supplier);
        }

        @Override
        public T get() {
            if (nullObj == value && atomicUpdater.compareAndSet(this, nullObj, supplier.get())) {
                supplier = null;
            }
            return (T)value;
        }
    }

    private static class LazyAtomicReference<T> extends AbstractLazy<T> {
        private final AtomicReference<Object> atomicValue = new AtomicReference<>(nullObj);

        public LazyAtomicReference(Supplier<T> supplier) {
            super(supplier);
        }

        @Override
        public T get() {
            if (nullObj == atomicValue.get() && atomicValue.compareAndSet(nullObj, supplier.get())) {
                supplier = null;
            }
            return (T)atomicValue.get();
        }
    }
}
