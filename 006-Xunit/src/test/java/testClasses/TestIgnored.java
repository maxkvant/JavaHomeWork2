package testClasses;

import task.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TestIgnored {
    @Test(ignore = "ignore")
    public void test() {
        List<Integer> list = new ArrayList<>();
        list.get(2);
    }
}
