package task.Tester;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.lang.reflect.Method;

public class TestResult {
    private final @NotNull Method method;
    public final boolean ok;
    public final @NotNull String message;
    public final @Nullable Throwable exception;
    public final long time;

    private TestResult(@NotNull Method method, boolean ok, @NotNull String message, @Nullable Throwable exception, long time) {
        this.method = method;
        this.ok = ok;
        this.message = message;
        this.exception = exception;
        this.time = time;
    }

    public static TestResult ok(@NotNull Method method, long time) {
        return new TestResult(method, true, "ok.", null, time);
    }

    public static TestResult expectedException(@NotNull Method method, @NotNull Class<? extends Throwable> exceptionClass, long time) {
        return new TestResult(method, false, "expected " + exceptionClass.getName(), null, time);
    }

    public static TestResult exception(@NotNull Method method, @NotNull Throwable exception, long time) {
        return new TestResult(method, false, "exception: ", exception, time);
    }

    public static TestResult ignored(@NotNull Method method, @NotNull String message) {
        return new TestResult(method, true, "ignore. \ncause:" + message, null, 0);
    }

    public void print(PrintStream out) {
        out.println("Testing " + method);
        if (ok) {
            out.println("Ok");
            out.println(message);
        } else {
            out.println("Fail");
            out.println(message);
            if (exception != null) {
                exception.printStackTrace(out);
            }
        }
        out.println("time: "+ time + "ms");
        out.println();
        out.println();
    }
}