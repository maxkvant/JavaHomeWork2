package testClasses;

import task.annotations.Test;

public class TestExpectedExcetion {
    @Test(exception = IllegalArgumentException.class)
    public void test() {
        throw new IllegalArgumentException();
    }
}
