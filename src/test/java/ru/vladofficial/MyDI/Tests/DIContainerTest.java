package ru.vladofficial.MyDI.Tests;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.vladofficial.MyDI.Container.DIContainer;
import ru.vladofficial.MyDI.Tests.TestClasses.*;

import static org.junit.jupiter.api.Assertions.*;

class DIContainerTest {
    private final DIContainer container;

    public DIContainerTest() {
        container = new DIContainer();
        container.scanAndProcessComponents(DIContainerTest.class);
    }

    @BeforeEach
    public void scanning() {
        container.scanAndProcessComponents(DIContainerTest.class);
    }

    @Test
    public void beanTest() {
        assertDoesNotThrow(() -> container.createUsingBean(ComplexConfigClass.class));
    }

    @Test
    public void constructorTest() {
        assertDoesNotThrow(() -> container.createUsingConstructorInjection(ComplexConstructorClass.class));
    }

    @Test
    public void fieldTest() {
        assertDoesNotThrow(() -> container.createUsingFieldInjection(ComplexFieldClass.class));
    }

    @Test
    public void noBeanTest() {
        try {
            container.createUsingBean(NoAnnotationComplexClass.class);
            fail("Expected an exception");
        } catch (Exception exc) {
            assertEquals("No bean for class ru.vladofficial.MyDI.Tests.TestClasses.NoAnnotationComplexClass",
                    exc.getMessage());
        }
    }

    @Test
    public void noAutowiredConstructorTest() {
        try {
            container.createUsingConstructorInjection(NoAnnotationComplexClass.class);
            fail("Expected an exception");
        } catch (Exception exc) {
            assertEquals("No Autowired constructor for class ru.vladofficial.MyDI.Tests.TestClasses.NoAnnotationComplexClass",
                    exc.getMessage());
        }
    }

    @Test
    public void fieldInjectionAlwaysWorks() {
        assertDoesNotThrow(() -> container.createUsingFieldInjection(NoAnnotationComplexClass.class));
    }

    @Test
    public void interfaceImplementationNotFoundTest() {
        try {
            container.createUsingFieldInjection(NoInterfaceImplementationClass.class);
            fail("Expected an exception");
        } catch (Exception exc) {
            assertEquals("You haven't provided any implementations for interface dependency",
                    exc.getMessage());
        }
    }

    @Test
    public void interfaceImplementationWrongQualifier() {
        try {
            container.createUsingFieldInjection(WrongQualifierClass.class);
            fail("Exception expected");
        } catch (Exception exc) {
            assertEquals("You have provided an invalid class name in Qualifier", exc.getMessage());
        }
    }

    @Test
    public void interfaceImplementationValidQualifier() {
        assertDoesNotThrow(() -> container.createUsingFieldInjection(ValidQualifierClass.class));
    }

    @Test
    public void interfaceSingleImplementation() {
        assertDoesNotThrow(() -> container.createInstance(SingleImplementationInterface.class));
    }

    @Test
    public void interfaceAmbiguousImplementation() {
        try {
            container.createInstance(AmbiguousImplementations.class);
            fail("Expected exception");
        } catch (Exception exc) {
            assertEquals("Ambiguous interface implementations, 2 provided", exc.getMessage());
        }
    }

    @Test
    public void constructorWithArgumentsTest() {
        assertDoesNotThrow(() -> container.createUsingConstructorInjection(MoreComplexClass.class));
    }
}