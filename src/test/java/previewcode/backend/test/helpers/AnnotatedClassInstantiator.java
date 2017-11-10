package previewcode.backend.test.helpers;

import io.vavr.control.Try;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class AnnotatedClassInstantiator<T> {

    public Try<T> instantiateAnnotationValue(Class<? extends Annotation> annotation, Class<?> annotatedClass) {
        return getAnnotation(annotation, annotatedClass)
                .flatMap(this::getAnnotationValue)
                .flatMap(this::tryInstantiate);
    }

    public Try<Annotation> getAnnotation(Class<? extends Annotation> annotation, Class<?> annotatedClass) {
        if (annotatedClass.isAnnotationPresent(annotation)) {
            return Try.success(annotatedClass.getAnnotation(annotation));
        } else {
            return Try.failure(new RuntimeException(this.getClass().getSimpleName() + " can only be used via the " + annotation.getSimpleName() + " annotation."));
        }
    }

    public Try<Class<? extends T>> getAnnotationValue(Annotation annotation) {
        try {
            return Try.success((Class<T>) annotation.annotationType().getMethod("value").invoke(annotation));
        } catch (IllegalAccessException e) {
            return Try.failure(new RuntimeException(annotation.annotationType().getSimpleName() + ".value() must be public"));
        } catch (NoSuchMethodException | InvocationTargetException e) {
            return Try.failure(new RuntimeException(annotation.annotationType().getSimpleName() + " must have a `value` member"));

        }
    }

    private <R> Try<R> tryInstantiate(Class<? extends R> moduleClass) {
        try {
            return Try.success(moduleClass.newInstance());
        } catch (InstantiationException  e) {
            return tryInstantiateInnerClass(moduleClass);
        } catch (IllegalAccessException e) {
            return tryFixAccessibility(moduleClass);
        }
    }

    private <R> Try<R> tryInstantiateInnerClass(Class<? extends R> moduleClass) {
        Class<?> enclosingClass = moduleClass.getEnclosingClass();
        if (enclosingClass != null) {
            return tryInstantiate(enclosingClass)
                    .flatMap(parentInstance ->
                            tryGetConstructor(moduleClass, enclosingClass)
                                    .flatMap(ctor -> {
                                        ctor.setAccessible(true);
                                        return tryInstantiateViaConstructor(moduleClass, ctor, parentInstance);
                                    } ));
        }
        return Try.failure(new RuntimeException("Unable to instantiate " + moduleClass.getSimpleName() + ".\n" +
                "Make sure the class is accessible and has a public no-args constructor"));
    }

    private <R> Try<R> tryFixAccessibility(Class<? extends R> moduleClass) {
        return tryGetConstructor(moduleClass).map(constructor -> {
            constructor.setAccessible(true);
            return constructor;
        }).flatMap(constructor -> tryInstantiateViaConstructor(moduleClass, constructor));
    }

    private <R> Try<Constructor<? extends R>> tryGetConstructor(Class<? extends R> moduleClass, Class<?> ... parameterTypes) {
        try {
            return Try.success(moduleClass.getConstructor(parameterTypes));
        } catch (NoSuchMethodException e) {
            return Try.failure(new RuntimeException("Unable to instantiate " + moduleClass.getSimpleName() + ".\n" +
                    "Make sure the class has a no-args constructor"));
        }
    }

    private <R> Try<? extends R> tryInstantiateViaConstructor(Class<? extends R> moduleClass, Constructor<? extends R> constructor, Object ... ctorArgs) {
        try {
            return Try.success(constructor.newInstance(ctorArgs));
        } catch (InstantiationException | IllegalAccessException e) {
            return Try.failure(new RuntimeException("Unable to instantiate " + moduleClass.getSimpleName() + ".\n" +
                    "Make sure the class is accessible and has a public no-args constructor", e));
        } catch (InvocationTargetException e) {
            return Try.failure(new RuntimeException("Unable to instantiate " + moduleClass.getSimpleName() + ". Exception in class constructor", e));
        }
    }

}
