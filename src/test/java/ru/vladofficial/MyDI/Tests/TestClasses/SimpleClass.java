package ru.vladofficial.MyDI.Tests.TestClasses;

import lombok.NoArgsConstructor;
import ru.vladofficial.MyDI.Annotations.Component;

@Component
public class SimpleClass {
    public SimpleClass() {
        System.out.println("Simple class created");
    }
}
