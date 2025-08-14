package ru.vladofficial.MyDI.Processing;

import ru.vladofficial.MyDI.Annotations.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class MetadataExtractor {

    public List<ComponentMetadata> extractComponents(List<Class<?>> components) {
        List<ComponentMetadata> result = new ArrayList<>();

        for (Class<?> component : components) {
            List<Constructor<?>> constructors = Arrays.stream(component.getConstructors()).toList();
            Constructor<?> autowiredConstructor = null;
            for (Constructor<?> constructor : constructors) {
                if (constructor.isAnnotationPresent(Autowired.class)) {
                    if (autowiredConstructor != null) {
                        throw new RuntimeException(
                                "Multiple autowired constructors for class " + component.getTypeName());
                    }

                    autowiredConstructor = constructor;
                }
            }

            List<Field> injectableFields = new ArrayList<>();
            List<Field> fields = Arrays.stream(component.getDeclaredFields()).toList();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    injectableFields.add(field);
                }
            }

            boolean singleton = component.isAnnotationPresent(Singleton.class);
            boolean primaryImplementation = component.isAnnotationPresent(PrimaryImplementation.class);

            result.add(new ComponentMetadata(component, constructors, autowiredConstructor, injectableFields,
                    singleton, primaryImplementation));
        }

        return result;
    }

    public Map<Class<?>, List<BeanMetadata>> extractBeans(List<Class<?>> configs) {
        Map<Class<?>, List<BeanMetadata>> result = new HashMap<>();

        for (Class<?> config : configs) {
            List<Method> beans = Arrays.stream(config.getDeclaredMethods()).toList();

            for (Method beanMethod : beans) {
                boolean primaryBean = beanMethod.isAnnotationPresent(PrimaryBean.class);
                BeanMetadata metadata = new BeanMetadata(config, beanMethod, beanMethod.getReturnType(), primaryBean);
                List<BeanMetadata> list = result.getOrDefault(metadata.returnType(), new ArrayList<>());
                list.add(metadata);

                result.put(beanMethod.getReturnType(), list);
            }
        }

        return result;
    }
}
