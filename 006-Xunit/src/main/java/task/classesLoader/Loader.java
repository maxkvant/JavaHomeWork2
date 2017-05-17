package task.classesLoader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class Loader {
    /**
     * Loads classes from path, which are located in package rootPackage
     */
    public static List<Class<?>> load(@NotNull final Path path, @NotNull String rootPackage) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            URL[] urls = new URL[] {path.toUri().toURL()};
            ClassLoader classLoader = new URLClassLoader(urls);

            try {
                Files.walk(path).filter(Files::isRegularFile)
                        .filter(path1 -> path1.toString().endsWith(".class"))
                        .map(path::relativize)
                        .map(path1 -> {
                            String res = StreamSupport.stream(path1.spliterator(), false)
                                    .map(Path::toString)
                                    .collect(Collectors.joining("."));
                            res = rootPackage + "." + res.substring(0, res.length() - ".class".length());
                            return res;
                        })
                        .forEach((String name) -> {
                                try {
                                    Class<?> clazz = classLoader.loadClass(name);
                                    classes.add(clazz);
                                } catch (ClassNotFoundException e) {
                                //    e.printStackTrace();
                                }
                            }
                        );
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }
}
