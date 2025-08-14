package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Component;
import ru.vladofficial.MyDI.Annotations.Inject;
import ru.vladofficial.MyDI.Annotations.Qualifier;

@Component
public class ValidQualifierClass {
    @Inject
    @Qualifier(className = "ru.vladofficial.MyDI.Tests.TestClasses.QualifierImplementationClass")
    public QualifierTestInterface interfaceDependency;
}
