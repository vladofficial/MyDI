package ru.vladofficial.MyDI.Tests.TestClasses;

import org.checkerframework.checker.units.qual.C;
import ru.vladofficial.MyDI.Annotations.Component;
import ru.vladofficial.MyDI.Annotations.Inject;

@Component
public class NoInterfaceImplementationClass {
    @Inject
    public TestInterfaceNoImplementation interfaceDependency;
}
