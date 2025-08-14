package ru.vladofficial.MyDI.Container;

import ru.vladofficial.MyDI.Annotations.*;
import ru.vladofficial.MyDI.InterfaceHandling.InterfaceResolver;
import ru.vladofficial.MyDI.Processing.BeanMetadata;
import ru.vladofficial.MyDI.Processing.ComponentMetadata;
import ru.vladofficial.MyDI.Processing.MetadataExtractor;
import ru.vladofficial.MyDI.Scanning.ClassPathScanner;
import ru.vladofficial.MyDI.Scanning.ScanningResult;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class DIContainer {

    private final Map<Class<?>, Object> cachedClasses = new HashMap<>();
    private final InterfaceResolver interfaceResolver = new InterfaceResolver();
    private final MetadataExtractor metadataExtractor = new MetadataExtractor();
    private final ClassPathScanner scanner = new ClassPathScanner();
    private final Map<Class<?>, Set<BeanMetadata>> beans = new HashMap<>();
    private final Map<Class<?>, ComponentMetadata> components = new HashMap<>();

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

    private Object createUnInjectedInstance(Class<?> requiredClass) {
        Constructor<?> constructor;
        try {
            constructor = requiredClass.getDeclaredConstructor();
        } catch (NoSuchMethodException exc) {
            throw new RuntimeException("No default constructor found for " + requiredClass.getName(), exc);
        }

        constructor.setAccessible(true);
        Object instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException exc) {
            throw new RuntimeException("Couldn't instantiate" + requiredClass.getName(), exc);
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        return instance;
    }

    public Object createUsingBean(Class<?> requiredClass) {
        if (!beans.containsKey(requiredClass)) {
            throw new RuntimeException("No bean for class " + requiredClass.getTypeName());
        }

        List<BeanMetadata> beansList = beans.get(requiredClass).stream().toList();
        Method bean;
        if (beansList.size() == 1) {
            bean = beansList.getFirst().method();
        } else {
            for (BeanMetadata currentBean : beansList) {
                if (currentBean.primary()) {
                    bean = currentBean.method();
                    break;
                }
            }

            throw new RuntimeException("No unique bean for class " + requiredClass.getTypeName());
        }

        List<Class<?>> parameters = Arrays.stream(bean.getParameters())
                .<Class<?>>map(Parameter::getType)
                .toList();

        List<Object> createdParameters = parameters.stream()
                .<Object>map(this::createInstance).toList();
        try {
            return bean.invoke(null, createdParameters.toArray());
        } catch (Exception exc) {
            throw new RuntimeException("Something went wrong during instantiating using a bean");
        }
    }

    public Object createUsingFieldInjection(Class<?> requiredClass) {
        Object instance = createUnInjectedInstance(requiredClass);
        boolean injectedSomething = false;
        for (Field field : instance.getClass().getDeclaredFields()) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }

            Class<?> dependencyClass;
            if (field.getType().isInterface()) {
                dependencyClass = interfaceResolver.resolveInterfaceForField(field);
            } else {
                dependencyClass = field.getType();
            }

            Object dependency = createInstance(dependencyClass);

            field.setAccessible(true);
            try {
                field.set(instance, dependency);
                injectedSomething = true;
            } catch (IllegalAccessException exc) {
                throw new RuntimeException("Failed to inject dependency into " + field, exc);
            }
        }

        if (!injectedSomething) {
            System.err.println("Warning: haven't injected any fields when instantiating class "
             + requiredClass.getTypeName() + "\nMake sure this is correct behaviour.");
        }

        return instance;
    }

    public Object createUsingConstructorInjection(Class<?> requiredClass) {
        if (components.get(requiredClass).constructors().isEmpty()) {
            throw new RuntimeException("No constructors for class " + requiredClass.getTypeName());
        }

        List<Constructor<?>> classConstructors = components.get(requiredClass).constructors();
        Constructor<?> autowired = components.get(requiredClass).autowiredConstructor();

        if (autowired == null) {
            throw new RuntimeException("No Autowired constructor for class " + requiredClass.getTypeName());
        }

        List<Class<?>> parameters = Arrays.stream(autowired.getParameters())
                .<Class<?>>map(Parameter::getType)
                .toList();

        Object[] createdParameters = parameters.stream()
                .map(this::createInstance).toArray();
        try {
            return autowired.newInstance(createdParameters);
        } catch (Exception exc) {
            throw new RuntimeException("Constructor injection failed for class " + requiredClass.getTypeName());
        }
    }

    public Object createInstance(Class<?> requiredClass) {
        if (requiredClass.isInterface()) {
            try {
                return createUsingBean(requiredClass);
            } catch (RuntimeException exc) {
                requiredClass = interfaceResolver.resolveInterface(requiredClass);
            }
        }

        if (cachedClasses.containsKey(requiredClass)) {
            return cachedClasses.get(requiredClass);
        }

        Object instance = null;
        boolean instantiated = false;

        if (beans.containsKey(requiredClass)) {
            try {
                instance = createUsingBean(requiredClass);
                instantiated = true;
            } catch (RuntimeException e) { }
        }

        if (!instantiated) {
            try {
                instance = createUsingConstructorInjection(requiredClass);
                instantiated = true;
            } catch (RuntimeException e) { }
        }

        if (!instantiated) {
            try {
                instance = createUsingFieldInjection(requiredClass);
                instantiated = true;
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to instantiate " + requiredClass.getName() +
                        " using any supported injection method", e);
            }
        }

        if (requiredClass.isAnnotationPresent(Singleton.class)) {
            cachedClasses.put(requiredClass, instance);
        }

        return instance;
    }
}
