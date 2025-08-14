package ru.vladofficial.MyDI.Tests.TestClasses;

import ru.vladofficial.MyDI.Annotations.Bean;
import ru.vladofficial.MyDI.Annotations.Configuration;

@Configuration
public class TestConfig {
    @Bean
    public static ComplexConfigClass makeComplexClass() {
        SimpleClass simpleClass = new SimpleClass();
        return new ComplexConfigClass(simpleClass);
    }
}
