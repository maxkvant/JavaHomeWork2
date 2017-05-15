package testClasses;

import task.annotations.*;

import java.util.ArrayList;
import java.util.List;

public class TestMetodOrder {
    public static final List<String> res = new ArrayList<String>();

    @Before
    public void before() {
        res.add("Before");
    }

    @After
    public void after() {
        res.add("After");
    }

    @BeforeClass
    public static void beforeClass() {
        res.add("BeforeClass");
    }

    @AfterClass
    public static void afterClass() {
        res.add("AfterClass");
    }

    @Test
    public void test1() {
        res.add("test");
    }

    @Test
    public void test2() {
        res.add("test");
    }
}
