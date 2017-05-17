package task.annotations;

import org.jetbrains.annotations.NotNull;

/**
 * Encapsulates default values for Test
 * @see Test
 */
public class Checks {
    final static @NotNull String nullString = "";
    final class MyThrowable extends Throwable {}

    /**
     * checks is exception set in test
     */
    public static boolean hasException(Test test) {
        return test.exception() != MyThrowable.class;
    }

    /**
     * checks is ignored set in test
     */
    public static boolean isIgnored(Test test) {
        return !test.ignore().equals(nullString);
    }

}
