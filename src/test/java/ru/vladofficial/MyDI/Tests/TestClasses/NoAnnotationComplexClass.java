package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Component;

@Component
public class NoAnnotationComplexClass {
    private SimpleClass dependency;
}
