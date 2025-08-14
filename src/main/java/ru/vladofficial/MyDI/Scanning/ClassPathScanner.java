package ru.vladofficial.MyDI.Scanning;

import com.google.common.reflect.ClassPath;
import ru.vladofficial.MyDI.Annotations.Component;
import ru.vladofficial.MyDI.Annotations.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ClassPathScanner {
    @SuppressWarnings("all")
    public ScanningResult scanComponents(String packageName) {
        try {
            List<Class<?>> components = new ArrayList<>();
            List<Class<?>> configs = new ArrayList<>();
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            ClassPath classPath = ClassPath.from(classLoader);

            for (ClassPath.ClassInfo classInfo : classPath.getTopLevelClassesRecursive(packageName)) {
                Class<?> scannedClass = classInfo.load();
                if (scannedClass.isAnnotationPresent(Configuration.class)) {
                    configs.add(scannedClass);
                }

                if (scannedClass.isAnnotationPresent(Component.class)) {
                    components.add(scannedClass);
                }
            }

            return new ScanningResult(components, configs);
        } catch (Exception exc) {
            throw new RuntimeException("Component scanning failed");
        }
    }
}
