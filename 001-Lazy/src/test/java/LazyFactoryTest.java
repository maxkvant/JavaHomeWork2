import org.junit.Test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LazyFactoryTest {
    private final int nThreads = 4000;

    @Test
    public void createLazySimpleTests() throws Exception {
        doSimpleLazySemanticsTest(LazyFactory::createLazySimple);
        doThreadTest(LazyFactory::createLazySimple);
    }

    @Test
    public void createLazyMultiThreadTest() throws Exception {
        doSimpleLazySemanticsTest(LazyFactory::createLazyMultiThread);
        doThreadTest(LazyFactory::createLazyMultiThread);
    }

    @Test
    public void createLazyAtomicTest() throws Exception {
        doSimpleLazySemanticsTest(LazyFactory::createLazyAtomic);
        doThreadTest(LazyFactory::createLazyAtomic);
    }

    @Test
    public void createLazyAtomicReferenceTest() throws Exception {
        doSimpleLazySemanticsTest(LazyFactory::createLazyAtomicReference);
        doThreadTest(LazyFactory::createLazyAtomicReference);
    }


    private void doThreadTest(Function<Supplier<Integer>, Lazy<Integer>> lazyFactory) throws InterruptedException {
        final String res = "abacabadabacaba";
        SupplierCount<Integer> supplier = new SupplierCount<>(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new Random().nextInt();
        });

        Lazy<Integer> lazy = lazyFactory.apply(supplier);
        List<Thread> threads = new ArrayList<>();
        final List<Integer> values = new ArrayList<>();

        for (int i = 0; i < nThreads; i++) {
            Thread thread = new Thread(() -> {
                Integer value = lazy.get();
                synchronized (values) {
                    values.add(value);
                }
            });
            threads.add(thread);
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        Collections.sort(values);
        assertEquals(values.get(0), values.get(values.size() - 1));

        System.out.println("supplier calls: " + supplier.count);
    }

    private void doSimpleLazySemanticsTest(Function<Supplier, Lazy> lazyFactory) {
        doSimpleConstTest(lazyFactory, null);
        doSimpleConstTest(lazyFactory, new Object());
        doSimpleConstTest(lazyFactory, 1);
        doEqualGetTest1(lazyFactory);
    }


    @SuppressWarnings("unchecked")
    private void doEqualGetTest1(Function<Supplier, Lazy> lazyFactory) {
        Lazy<Object> lazy = lazyFactory.apply(Object::new);
        assertEquals(lazy.get(), lazy.get());
    }


    @SuppressWarnings("unchecked")
    private <T> void doSimpleConstTest(Function<Supplier, Lazy> lazyFactory, T constValue) {
        SupplierCount<T> supplier = new SupplierCount<>(() -> constValue);
        Lazy<T> lazy = lazyFactory.apply(supplier);

        SupplierCount<Lazy<T>> supplier1 = new SupplierCount<>(() -> lazyFactory.apply(supplier));
        Lazy<Lazy<T>> lazy1 = lazyFactory.apply(supplier1);

        assertEquals(0, supplier.count);
        assertEquals(constValue, lazy.get());
        assertEquals(1, supplier.count);
        assertEquals(constValue, lazy.get());
        assertEquals(1, supplier.count);


        assertEquals(0, supplier1.count);
        lazy1.get();
        assertEquals(1, supplier.count);
        assertEquals(constValue, lazy1.get().get());
        assertEquals(2, supplier.count);
        assertEquals(1, supplier1.count);
        assertEquals(constValue, lazy1.get().get());
        assertEquals(1, supplier1.count);
    }

    private class SupplierCount<T> implements Supplier<T> {
        private final Supplier<T> supplier;

        private SupplierCount(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            count++;
            return supplier.get();
        }

        public int count = 0;
    }
}