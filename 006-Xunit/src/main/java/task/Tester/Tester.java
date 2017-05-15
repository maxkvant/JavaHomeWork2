package task.Tester;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import task.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.Callable;

public class Tester implements Callable<List<TestResult>> {
    private final static List<Class<? extends Annotation> > supportedAnnotations = ImmutableList.of(
            Test.class,
            After.class,
            Before.class,
            AfterClass.class,
            BeforeClass.class);

    private final Constructor constructor;
    private final Map<Class<? extends Annotation>, ArrayList<Method>> annotatedMethods;

    public Tester(@NotNull Class<?> clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        constructor = clazz.getConstructor();

        Method[] methods = clazz.getMethods();
        annotatedMethods = new HashMap<>();

        supportedAnnotations.forEach(clazz1 -> annotatedMethods.put(clazz1, new ArrayList<Method>()));

        for (Method method : methods) {
            for (Annotation annotation : method.getAnnotations()) {
                Class<? extends Annotation> key = annotation.annotationType();
                if (annotatedMethods.get(annotation.annotationType()) != null) {
                    annotatedMethods.get(key).add(method);
                }
            }
        }
    }

    public List<TestResult> call() {
        List<TestResult> res = new ArrayList<>();
        try {
            for (Method method : annotatedMethods.get(BeforeClass.class)) {
                if (isStatic(method)) {
                    runMethod(null, method);
                }
            }

            for (@NotNull Method method : annotatedMethods.get(Test.class)) {
                Object test = constructor.newInstance();

                for (Method method1 : annotatedMethods.get(Before.class)) {
                    runMethod(test, method1);
                }

                TestResult testResult = runTest(test, method);
                res.add(testResult);
                System.out.println(testResult.ok);
                testResult.print(System.out);

                for (Method method1 : annotatedMethods.get(After.class)) {
                    runMethod(test, method1);
                }
            }

            for (Method method : annotatedMethods.get(AfterClass.class)) {
                if (isStatic(method)) {
                    runMethod(null, method);
                }
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new ArrayList<>();
        }
        return res;
    }

    private static void runMethod(Object object, Method method) throws Throwable {
        try {
            method.invoke(object);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static TestResult runTest(@NotNull Object test, @NotNull Method method) {
        @NotNull Test annotation = method.getAnnotation(Test.class);

        if (!Checks.isIgnored(annotation)) {
            long startTime = System.currentTimeMillis();

            Class<? extends Throwable> expectedClass = annotation.exception();
            Throwable throwable = null;
            try {
                runMethod(test, method);
            } catch (Throwable e) {
                throwable = e;
            }

            long time = System.currentTimeMillis() - startTime;

            if (throwable != null) {
                if (!Checks.hasException(annotation) || throwable.getClass() != expectedClass) {
                    return TestResult.exception(method, throwable, time);
                }
            } else if (Checks.hasException(annotation)) {
                return TestResult.expectedException(method, annotation.exception(), time);
            }

            return TestResult.ok(method, time);
        } else {
            return TestResult.ignored(method, annotation.ignore());
        }
    }

    private boolean isStatic(@NotNull Method method) {
        return Modifier.isStatic(method.getModifiers());
    }
}
