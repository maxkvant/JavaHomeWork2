package testClasses.package1;

import task.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TestObjects {
    private int a = 0;
    public static final List<Integer> results = new ArrayList<>();

    @Test
    public void test1() {
        results.add(++a);
    }

    @Test
    public void test2() {
        results.add(++a);
    }
}
