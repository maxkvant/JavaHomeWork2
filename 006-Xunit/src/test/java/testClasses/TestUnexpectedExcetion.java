package testClasses;

import task.annotations.Test;

public class TestUnexpectedExcetion {
    @Test(exception = IllegalAccessError.class)
    public void test() {
        throw new UnsupportedOperationException();
    }
}
