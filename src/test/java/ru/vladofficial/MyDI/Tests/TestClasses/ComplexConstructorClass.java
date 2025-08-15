package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Autowired;
import ru.vladofficial.MyDI.Annotations.Component;

@Component
public class ComplexConstructorClass {
    private final SimpleClass dependency;

    @Autowired
    public ComplexConstructorClass() {
        dependency = new SimpleClass();
        System.out.println("Complex class created");
    }
}
