package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Component;
import ru.vladofficial.MyDI.Annotations.Inject;

@Component
public class ComplexFieldClass {
    @Inject
    private SimpleClass dependency;
}
