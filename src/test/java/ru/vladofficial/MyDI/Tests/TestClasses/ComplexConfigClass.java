package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Component;

@Component
public class ComplexConfigClass {
    private final SimpleClass dependency;


    public ComplexConfigClass(SimpleClass simpleClass)
    {
        dependency = simpleClass;
    }
}
