package de.heuboe.vmis2.util;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

/**
 * Utility class that can convert reflection calls to native method invocations through functional
 * interfaces, this increases the performance of those calls greatly (up to 25%).
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@SuppressWarnings("squid:S1192") // Wrongly detected possible constants
public final class ReflectionToFunctionalConverter {

    /**
     * A lookup instance with private access enabled, but only usable for public methods.
     */
    private static final Lookup LOOKUP = MethodHandles.lookup();

    /**
     * Creates a new {@link Function} from the given class using the given method name.
     *
     * @param <I> The input type.
     * @param <O> The output type.
     * @param clazz The class that has the method.
     * @param methodName The name of the method. The method must be public, accept a single instance of
     *        the given input class as a parameter and return non-void.
     * @param inputClass The non-primitive class of the input for the function.
     * @return The newly created Function.
     * @throws RuntimeException If there is no such method in the given class, the method uses the wrong
     *         parameters or the method handle could not be converted to a Function.
     * @see #publicMethodAsFunction(Method)
     */
    public static <I, O> Function<I, O> publicMethodAsFunction(final Class<?> clazz,
            final String methodName, final Class<? super I> inputClass) {
        requireNonNull(clazz, "clazz");
        requireNonNull(methodName, "methodName");
        requireNonNull(inputClass, "inputClass");
        if (inputClass.isPrimitive()) {
            // Works with eclipse though...
            throw new IllegalArgumentException("Input parameters cannot be primitive!");
        }
        final Method method;
        try {
            // MethodHandles created via reflection are ~5% faster than those being looked
            // up directly.
            method = clazz.getMethod(methodName, inputClass);
        } catch (final NoSuchMethodException | SecurityException e) {
            throw newFailedToFindMethodException(clazz, methodName, inputClass, e);
        }
        return publicMethodAsFunction(method);
    }

    /**
     * Creates a new {@link Function} from the given class using the given method name. This method uses
     * a default {@link Lookup}.
     *
     * <p>
     * <b>Note:</b> The lookup instance must have access to the given method. The default method
     * visibility rules applies.
     * </p>
     *
     * @param <I> The input type.
     * @param <O> The output type.
     * @param lookup The lookup to use for this. The lookup must have private access mode enabled.
     * @param clazz The class that has the method.
     * @param methodName The name of the method. The method must accept a single instance of the given
     *        input class as a parameter and return non-void.
     * @param inputClass The non-primitive class of the input for the function.
     * @return The newly created Function.
     * @throws RuntimeException If there is no such method in the given class, the method uses the wrong
     *         parameters or the method handle could not be converted to a Function.
     * @see #methodAsFunction(MethodHandles.Lookup, Method)
     */
    public static <I, O> Function<I, O> methodAsFunction(final Lookup lookup, final Class<?> clazz,
            final String methodName, final Class<? super I> inputClass) {
        requireNonNull(lookup, "lookup");
        requireNonNull(clazz, "clazz");
        requireNonNull(methodName, "methodName");
        requireNonNull(inputClass, "inputClass");
        if (inputClass.isPrimitive()) {
            // Works with eclipse though...
            throw new IllegalArgumentException("Input parameters cannot be primitive!");
        }
        final Method method;
        try {
            // MethodHandles created via reflection are ~5% faster than those being looked
            // up directly.
            method = clazz.getDeclaredMethod(methodName, inputClass);
        } catch (final NoSuchMethodException | SecurityException e) {
            throw newFailedToFindMethodException(clazz, methodName, inputClass, e);
        }
        return methodAsFunction(lookup, method);
    }

    /**
     * Helper method that creates an Exception with an readable method definition.
     *
     * @param clazz The class that was searched.
     * @param methodName The method name that was searched.
     * @param inputClass The input class that was searched.
     * @param cause The reason why the method was not found.
     * @return The newly created Exception.
     */
    private static IllegalArgumentException newFailedToFindMethodException(final Class<?> clazz,
            final String methodName,
            final Class<?> inputClass, final Throwable cause) {
        final String parameterType;
        if (inputClass.isArray()) {
            parameterType = inputClass.getComponentType().getName() + "[]";
        } else {
            parameterType = inputClass.getName();
        }
        return new IllegalArgumentException("Failed to find method " +
                clazz.getName() + "#" + methodName + "(" + parameterType + ")", cause);
    }

    /**
     * Creates a new {@link Function} from the given class using the given method. This method uses a
     * default {@link Lookup}.
     *
     * @param <I> The input type.
     * @param <O> The output type.
     * @param method The method to invoke in the Function. The method must be public, have only a single
     *        non-primitive parameter and return non-void.
     * @return The newly created converter.
     * @throws RuntimeException If the method has a wrong parameter count or the method handle could not
     *         be converted to a Function.
     * @see #methodAsFunction(MethodHandles.Lookup, Method)
     */
    public static <I, O> Function<I, O> publicMethodAsFunction(final Method method) {
        requireNonNull(method, "method");
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("Method " + method + " must be public to be converted to a Function");
        }
        return methodAsFunction(LOOKUP, method);
    }

    /**
     * Creates a new {@link Function} from the given class using the given method.
     *
     * <p>
     * <b>Note:</b> The lookup instance must have access to the given method. The default method
     * visibility rules applies.
     * </p>
     *
     * @param <I> The input type.
     * @param <O> The output type.
     * @param lookup The lookup to use for this. The lookup must have private access mode enabled.
     * @param method The method to invoke in the Function. The method must have only a single
     *        non-primitive parameter and return non-void.
     * @return The newly created converter.
     * @throws RuntimeException If the method has a wrong parameter count or the method handle could not
     *         be converted to a Function.
     * @see #methodHandleAsFunction(MethodHandles.Lookup, MethodHandle)
     */
    public static <I, O> Function<I, O> methodAsFunction(final Lookup lookup, final Method method) {
        requireNonNull(lookup, "lookup");
        requireNonNull(method, "method");
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                    "Method " + method + " can only be converted to a Function if it has only one parameter!");
        }
        if (parameterTypes[0].isPrimitive()) {
            // Works with eclipse though...
            throw new IllegalArgumentException(
                    "Method " + method + " can only be converted to a Function if its parameter is not primitive!");
        }
        if (void.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalArgumentException(
                    "Method " + method + " can only be converted to a Function if it returns non-void!");
        }
        final MethodHandle handle;
        try {
            // MethodHandles created via reflection are ~5% faster than those being looked
            // up directly.
            handle = lookup.unreflect(method);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(
                    "Lookup for " + lookup.lookupClass().getName() + " does not have access to " + method, e);
        }
        return methodHandleAsFunction(lookup, handle);
    }

    /**
     * Creates a new {@link Function} from the given method handle. This method uses Java-API features
     * to virtually inline the method invocation, thus erasing the {@link Function#apply(Object)} from
     * the stacktrace. Calling apply in the resulting Function is equivalent to calling the source
     * method directly.
     *
     * @param <I> The input type.
     * @param <O> The output type.
     * @param lookup The lookup to use for this. The lookup must have private access mode enabled.
     * @param handle The method handle to convert.
     * @return The newly created converter for the given method handle.
     * @throws RuntimeException If the method handle could not be converted to a Function.
     */
    // This method could be public, but I think no one will ever call it directly.
    private static <I, O> Function<I, O> methodHandleAsFunction(final Lookup lookup, final MethodHandle handle) {
        requireNonNull(lookup, "lookup");
        requireNonNull(handle, "handle");
        try {
            final CallSite site = LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    // signature of method Function.apply after type erasure
                    MethodType.methodType(Object.class, Object.class),
                    handle,
                    handle.type());
            return (Function<I, O>) site.getTarget().invokeExact();
        } catch (final Throwable e) {
            // Should never happen...
            throw new IllegalArgumentException("Failed to convert method handle to Function.", e);
        }
    }

    @SuppressWarnings("squid:S1213") // Util class - should not be instantiated
    private ReflectionToFunctionalConverter() {}

}
