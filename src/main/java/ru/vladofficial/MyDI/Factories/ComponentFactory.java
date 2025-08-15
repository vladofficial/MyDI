package ru.vladofficial.MyDI.Factories;

import ru.vladofficial.MyDI.Annotations.Inject;
import ru.vladofficial.MyDI.InterfaceHandling.InterfaceResolver;
import ru.vladofficial.MyDI.Processing.BeanMetadata;
import ru.vladofficial.MyDI.Processing.ComponentMetadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class ComponentFactory {
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

    public Object createUsingBean(Class<?> requiredClass, Map<Class<?>, Set<BeanMetadata>> beans,
                                  Map<Class<?>, ComponentMetadata> components,
                                  InterfaceResolver interfaceResolver) {
        if (!beans.containsKey(requiredClass)) {
            throw new RuntimeException("No bean for class " + requiredClass.getTypeName());
        }

        List<BeanMetadata> beansList = beans.get(requiredClass).stream().toList();
        Method bean = null;
        if (beansList.size() == 1) {
            bean = beansList.getFirst().method();
        } else {
            for (BeanMetadata currentBean : beansList) {
                if (currentBean.primary()) {
                    bean = currentBean.method();
                    break;
                }
            }

            if (bean == null) {
                throw new RuntimeException("No unique bean for class " + requiredClass.getTypeName());
            }
        }

        List<Class<?>> parameters = Arrays.stream(bean.getParameters())
                .<Class<?>>map(Parameter::getType)
                .toList();

        List<Object> createdParameters = parameters.stream()
                .map(parameterClass -> createInstance(
                parameterClass,
                beans,
                components,
                interfaceResolver)).toList();
        try {
            return bean.invoke(null, createdParameters.toArray());
        } catch (Exception exc) {
            throw new RuntimeException("Something went wrong during instantiating using a bean");
        }
    }

    public Object createUsingFieldInjection(Class<?> requiredClass, Map<Class<?>, Set<BeanMetadata>> beans,
                                            Map<Class<?>, ComponentMetadata> components,
                                            InterfaceResolver interfaceResolver) {
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

            Object dependency = createInstance(dependencyClass, beans, components, interfaceResolver);

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

    public Object createUsingConstructorInjection(Class<?> requiredClass, Map<Class<?>, Set<BeanMetadata>> beans,
                                                  Map<Class<?>, ComponentMetadata> components,
                                                  InterfaceResolver interfaceResolver) {

        if (components.get(requiredClass).constructors().isEmpty()) {
            throw new RuntimeException("No constructors for class " + requiredClass.getTypeName());
        }

        Constructor<?> autowired = components.get(requiredClass).autowiredConstructor();

        if (autowired == null) {
            throw new RuntimeException("No Autowired constructor for class " + requiredClass.getTypeName());
        }

        List<Class<?>> parameters = Arrays.stream(autowired.getParameters())
                .<Class<?>>map(Parameter::getType)
                .toList();

        Object[] createdParameters = parameters.stream()
                .map(parameterClass -> createInstance(
                        parameterClass,
                        beans,
                        components,
                        interfaceResolver
                )).toArray();
        try {
            return autowired.newInstance(createdParameters);
        } catch (Exception exc) {
            throw new RuntimeException("Constructor injection failed for class " + requiredClass.getTypeName());
        }
    }

    public Object createInstance(Class<?> requiredClass, Map<Class<?>, Set<BeanMetadata>> beans,
                                 Map<Class<?>, ComponentMetadata> components,
                                 InterfaceResolver interfaceResolver) {

        Object instance = null;
        boolean instantiated = false;

        if (beans.containsKey(requiredClass)) {
            try {
                instance = createUsingBean(requiredClass, beans, components, interfaceResolver);
                instantiated = true;
            } catch (RuntimeException e) { }
        }

        if (!instantiated) {
            try {
                instance = createUsingConstructorInjection(requiredClass, beans, components, interfaceResolver);
                instantiated = true;
            } catch (RuntimeException e) { }
        }

        if (!instantiated) {
            try {
                instance = createUsingFieldInjection(requiredClass, beans, components, interfaceResolver);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to instantiate " + requiredClass.getName() +
                        " using any supported injection method", e);
            }
        }

        return instance;
    }
}
