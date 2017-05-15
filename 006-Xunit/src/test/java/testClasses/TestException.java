package testClasses;

import task.annotations.Test;

public class TestException {
    @Test
    public void test() {
        throw new RuntimeException();
    }
}
