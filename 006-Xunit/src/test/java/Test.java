import task.Tester.TestResult;
import task.Tester.Tester;
import task.Tester.TestsRunner;
import testClasses.*;
import testClasses.package1.TestObjects;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.Arrays;


import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class Test {
    @org.junit.Test
    public void testTestsRunner() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestsRunner.run(Paths.get("./build/classes/test/testClasses"), "testClasses");

        assertThat(TestMetodOrder.res, is(equalTo(Arrays.asList("BeforeClass",
                "Before",
                "test",
                "After",
                "Before",
                "test",
                "After",
                "AfterClass"))));

        assertThat(TestObjects.results, is(equalTo(Arrays.asList(1, 1))));
    }

    @org.junit.Test
    public void testException() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestResult result = new Tester(TestException.class).execute().get(0);
        assertThat(result.ok, is(equalTo(false)));
        assertThat(result.exception, allOf(is(instanceOf(RuntimeException.class)), not(is(nullValue()))));
    }

    @org.junit.Test
    public void testExpectedException() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestResult result = new Tester(TestExpectedExcetion.class).execute().get(0);
        assertThat(result.ok, is(equalTo(true)));
        assertThat(result.exception, is(nullValue()));
    }

    @org.junit.Test
    public void testUnexpectedException() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestResult result = new Tester(TestUnexpectedExcetion.class).execute().get(0);
        assertThat(result.ok, is(equalTo(false)));
        assertThat(result.exception, allOf(is(instanceOf(UnsupportedOperationException.class)), not(is(nullValue()))));
    }

    @org.junit.Test
    public void testPassed() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestResult result = new Tester(TestPassed.class).execute().get(0);
        assertThat(result.ok, is(equalTo(true)));
        assertThat(result.exception, is(nullValue()));
    }

    @org.junit.Test
    public void testIgnored() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestResult result = new Tester(TestIgnored.class).execute().get(0);
        assertThat(result.ok, is(equalTo(true)));
        assertThat(result.exception, is(nullValue()));
        assertThat(result.message, containsString("ignore"));
    }
}
