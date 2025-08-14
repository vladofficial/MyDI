package ru.vladofficial.MyDI.Processing;

import java.lang.reflect.Method;

public record BeanMetadata(
    Class<?> configClass,
    Method method,
    Class<?> returnType,
    boolean primary
) {}
