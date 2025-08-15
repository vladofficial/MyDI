package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Autowired;
import ru.vladofficial.MyDI.Annotations.Component;

@Component
public class MoreComplexClass {

    private final ComplexConstructorClass complexConstructorClass;
    private final SimpleClass simpleClass;

    @Autowired
    public MoreComplexClass(ComplexConstructorClass complexDependency, SimpleClass simpleDependency) {
        complexConstructorClass = complexDependency;
        simpleClass = simpleDependency;
    }

}
