package ru.vladofficial.MyDI.Processing;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;

public record ComponentMetadata(
        Class<?> type,
        List<Constructor<?>> constructors,
        Constructor<?> autowiredConstructor,
        List<Field> injectableFields,
        boolean singleton,
        boolean primaryImplementation
) {}