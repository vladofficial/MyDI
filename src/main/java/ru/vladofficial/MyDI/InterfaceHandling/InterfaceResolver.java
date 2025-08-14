package ru.vladofficial.MyDI.InterfaceHandling;

import ru.vladofficial.MyDI.Annotations.Component;
import ru.vladofficial.MyDI.Annotations.PrimaryImplementation;
import ru.vladofficial.MyDI.Annotations.Qualifier;
import ru.vladofficial.MyDI.Processing.ComponentMetadata;

import java.lang.reflect.Field;
import java.util.*;

public class InterfaceResolver {
    private final Map<Class<?>, Set<ComponentMetadata>> implementations = new HashMap<>();

    public void registerImplementations(List<ComponentMetadata> components) {
        for (ComponentMetadata component : components) {
            List<Class<?>> componentInterfaces = Arrays.stream(component.type().getInterfaces()).toList();

            for (Class<?> componentInterface : componentInterfaces) {
                Set<ComponentMetadata> impls = implementations.getOrDefault(componentInterface, new HashSet<>());
                impls.add(component);
                implementations.put(componentInterface, impls);
            }
        }
    }

    public Class<?> resolveInterface(Class<?> interfaceDependency) {

        if (!implementations.containsKey(interfaceDependency)) {
            throw new RuntimeException("You haven't provided any implementations for interface dependency");
        }

        List<ComponentMetadata> interfaceImplementations = implementations.get(interfaceDependency).stream().toList();
        if (interfaceImplementations.isEmpty()) {
            throw new RuntimeException("You haven't provided any implementations for interface dependency");
        }

        if (interfaceImplementations.size() == 1) {
            return interfaceImplementations.getFirst().type();
        }

        for (ComponentMetadata implementation : interfaceImplementations) {
            if (implementation.primaryImplementation()) {
                return implementation.type();
            }
        }

        throw new RuntimeException("Ambiguous interface implementations, " + interfaceImplementations.size() + " provided");
    }

    public Class<?> resolveInterfaceForField(Field interfaceDependency) {
        if (interfaceDependency.isAnnotationPresent(Qualifier.class)) {
            Qualifier qualifier = interfaceDependency.getAnnotation(Qualifier.class);
            String className = qualifier.className();
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException exc) {
                throw new RuntimeException("You have provided an invalid class name in Qualifier");
            }
        }

        return resolveInterface(interfaceDependency.getType());
    }
}
