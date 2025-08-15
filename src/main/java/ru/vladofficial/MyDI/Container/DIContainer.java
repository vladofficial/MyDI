package ru.vladofficial.MyDI.Container;

import lombok.SneakyThrows;
import ru.vladofficial.MyDI.Annotations.*;
import ru.vladofficial.MyDI.Factories.ComponentFactory;
import ru.vladofficial.MyDI.InterfaceHandling.InterfaceResolver;
import ru.vladofficial.MyDI.Processing.BeanMetadata;
import ru.vladofficial.MyDI.Processing.ComponentMetadata;
import ru.vladofficial.MyDI.Processing.MetadataExtractor;
import ru.vladofficial.MyDI.Scanning.ClassPathScanner;
import ru.vladofficial.MyDI.Scanning.ScanningResult;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class DIContainer {

    private final Map<Class<?>, Object> cachedClasses = new HashMap<>();
    private final InterfaceResolver interfaceResolver = new InterfaceResolver();
    private final MetadataExtractor metadataExtractor = new MetadataExtractor();
    private final ClassPathScanner scanner = new ClassPathScanner();
    private final Map<Class<?>, Set<BeanMetadata>> beans = new HashMap<>();
    private final Map<Class<?>, ComponentMetadata> components = new HashMap<>();
    private final ComponentFactory componentFactory = new ComponentFactory();

    @SneakyThrows
    private Object doCreate(Class<?> requiredClass, Function<Class<?>, Object> creationLogic) {
        if (requiredClass.isInterface()) {
            try {
                return componentFactory.createUsingBean(requiredClass, beans, components, interfaceResolver);
            } catch (RuntimeException exc) {
                requiredClass = interfaceResolver.resolveInterface(requiredClass);
            }
        }

        if (cachedClasses.containsKey(requiredClass)) {
            return cachedClasses.get(requiredClass);
        }

        Object instance = creationLogic.apply(requiredClass);

        if (components.containsKey(requiredClass) && components.get(requiredClass).postConstruct() != null) {
            Method postConstruct = components.get(requiredClass).postConstruct();
            postConstruct.setAccessible(true);
            try {
                postConstruct.invoke(requiredClass);
            } catch (Exception exc) {
                throw new RuntimeException("Something went wrong while performing @PostConstruct method of class" +
                        requiredClass.getTypeName());
            }
        }

        if (requiredClass.isAnnotationPresent(Singleton.class)) {
            cachedClasses.put(requiredClass, instance);
        }

        return instance;
    }

    public void scanAndProcessComponents(Class<?> mainClass) {
        scanAndProcessComponents(mainClass.getPackageName());
    }

    public void scanAndProcessComponents(String packageName) {
        ScanningResult result = scanner.scanComponents(packageName);
        List<ComponentMetadata> componentsMetadataList = metadataExtractor.extractComponents(result.components());
        Map<Class<?>, List<BeanMetadata>> beansMetadata = metadataExtractor.extractBeans(result.configs());

        componentsMetadataList.forEach(
                m -> components.put(m.type(), m)
        );

        for (Map.Entry<Class<?>, List<BeanMetadata>> entry : beansMetadata.entrySet()) {
            beans
                    .computeIfAbsent(entry.getKey(), k -> new HashSet<>())
                    .addAll(entry.getValue());
        }

        interfaceResolver.registerImplementations(componentsMetadataList);
    }

    public Object createUsingBean(Class<?> requiredClass) {
        return doCreate(requiredClass, cls ->
                componentFactory.createUsingBean(cls, beans, components, interfaceResolver)
        );
    }

    public <T> T createUsingFieldInjection(Class<T> requiredClass) {
        return requiredClass.cast(doCreate(requiredClass, cls ->
                componentFactory.createUsingFieldInjection(cls, beans, components, interfaceResolver)
        ));
    }

    public <T> T createUsingConstructorInjection(Class<T> requiredClass) {
        return requiredClass.cast(doCreate(requiredClass, cls ->
                componentFactory.createUsingConstructorInjection(cls, beans, components, interfaceResolver)
        ));
    }

    public <T> T createInstance(Class<T> requiredClass) {
        return requiredClass.cast(doCreate(requiredClass, cls ->
                componentFactory.createInstance(cls, beans, components, interfaceResolver)
        ));
    }
}
