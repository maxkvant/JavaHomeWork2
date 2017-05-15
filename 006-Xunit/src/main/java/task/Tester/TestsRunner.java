package task.Tester;

import org.jetbrains.annotations.NotNull;
import task.classesLoader.Loader;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;

public class TestsRunner {
    public static void run(@NotNull Path path, @NotNull String rootPackage) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Loader loader = new Loader();
        List<Class<?>> testClasses = loader.load(path, rootPackage);
        for (Class<?> clazz : testClasses) {
            new Tester(clazz).execute();
        }
    }
}
