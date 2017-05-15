package task.annotations;

import org.jetbrains.annotations.NotNull;

public class Checks {
    final static @NotNull String nullString = "";
    final class MyThrowable extends Throwable {}

    public static boolean hasException(Test test) {
        return test.exception() != MyThrowable.class;
    }

    public static boolean isIgnored(Test test) {
        return !test.ignore().equals(nullString);
    }

}
