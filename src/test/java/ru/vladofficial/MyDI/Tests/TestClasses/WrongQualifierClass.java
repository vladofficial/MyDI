package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Component;
import ru.vladofficial.MyDI.Annotations.Inject;
import ru.vladofficial.MyDI.Annotations.Qualifier;

@Component
public class WrongQualifierClass {
    @Inject
    @Qualifier(className = "ru.vladofficial.MyDI.Tests.TestClasses.QualifierImplementationClass (but it's wrong now)")
    public QualifierTestInterface interfaceDependency;
}
