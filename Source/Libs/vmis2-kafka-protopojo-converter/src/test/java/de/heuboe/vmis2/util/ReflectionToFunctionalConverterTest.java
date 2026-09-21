package de.heuboe.vmis2.util;

import static de.heuboe.vmis2.util.ReflectionToFunctionalConverter.methodAsFunction;
import static de.heuboe.vmis2.util.ReflectionToFunctionalConverter.publicMethodAsFunction;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

public class ReflectionToFunctionalConverterTest {

    private static final String PRIVATE_NAME = "privateTest";
    private static final String PUBLIC_NAME = "publicTest";
    private static final String PRIMITIVE_NAME = "primitive";
    private static final String ARRAY_NAME = "array";
    private static final String MISSING_NAME = "missing";
    private static final Class<Object> OBJECT_CLASS = Object.class;
    private static final Class<Integer> PRIMITIVE_CLASS = int.class;
    private static final Class<Object[]> ARRAY_CLASS = Object[].class;
    private static final Class<Methods> METHOD_CLAZZ = Methods.class;
    private static final Class<OtherMethods> OTHER_METHODS_CLAZZ = OtherMethods.class;

    private static final Lookup LOOKUP = Methods.LOOKUP;
    private static final Method PUBLIC_TEST;
    private static final Method PRIVATE_TEST;
    private static final Method PRIMITIVE_IN_TEST;
    private static final Method PRIMITIVE_OUT_TEST;
    private static final Method ARRAY_TEST;
    private static final Method NO_RETURN_TEST;
    private static final Method NO_PARAM_TEST;
    private static final Method TWO_PARAM_TEST;
    private static final Method OTHER_PUBLIC_TEST;
    private static final Method OTHER_PRIVATE_TEST;

    static {
        PUBLIC_TEST = uncheckedGetMethod(METHOD_CLAZZ, PUBLIC_NAME, OBJECT_CLASS);
        PRIVATE_TEST = uncheckedGetMethod(METHOD_CLAZZ, PRIVATE_NAME, OBJECT_CLASS);

        PRIMITIVE_IN_TEST = uncheckedGetMethod(METHOD_CLAZZ, PRIMITIVE_NAME, PRIMITIVE_CLASS);
        PRIMITIVE_OUT_TEST = uncheckedGetMethod(METHOD_CLAZZ, PRIMITIVE_NAME, OBJECT_CLASS);
        ARRAY_TEST = uncheckedGetMethod(METHOD_CLAZZ, ARRAY_NAME, ARRAY_CLASS);
        NO_RETURN_TEST = uncheckedGetMethod(METHOD_CLAZZ, "noReturn", OBJECT_CLASS);
        NO_PARAM_TEST = uncheckedGetMethod(METHOD_CLAZZ, "noParam");
        TWO_PARAM_TEST = uncheckedGetMethod(METHOD_CLAZZ, "twoParam", OBJECT_CLASS, OBJECT_CLASS);

        OTHER_PUBLIC_TEST = uncheckedGetMethod(OTHER_METHODS_CLAZZ, PUBLIC_NAME, OBJECT_CLASS);
        OTHER_PRIVATE_TEST = uncheckedGetMethod(OTHER_METHODS_CLAZZ, PRIVATE_NAME, OBJECT_CLASS);
    }

    private static Method uncheckedGetMethod(final Class<?> clazz, final String name,
            final Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Someone screwed the test up", e);
        }
    }

    @Test
    void testPublicMethodAsFunctionByName() {
        // Method 1
        final Function<String, Object> publicOne = publicMethodAsFunction(METHOD_CLAZZ, PUBLIC_NAME, OBJECT_CLASS);
        publicOne.apply("Test");

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(METHOD_CLAZZ, PRIVATE_NAME, OBJECT_CLASS));

        // Method 2
        final Function<String, Object> publicTwo =
                publicMethodAsFunction(OTHER_METHODS_CLAZZ, PUBLIC_NAME, OBJECT_CLASS);
        publicTwo.apply("Test");

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(OTHER_METHODS_CLAZZ, PRIVATE_NAME, OBJECT_CLASS));

        // Other
        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(METHOD_CLAZZ, PRIMITIVE_NAME, PRIMITIVE_CLASS));

        final Function<Object, Object> primitive = publicMethodAsFunction(METHOD_CLAZZ, PRIMITIVE_NAME, OBJECT_CLASS);
        primitive.apply(1);

        final Function<Object[], Object> array = publicMethodAsFunction(METHOD_CLAZZ, ARRAY_NAME, ARRAY_CLASS);
        array.apply(null);

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(METHOD_CLAZZ, MISSING_NAME, OBJECT_CLASS));

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(METHOD_CLAZZ, MISSING_NAME, ARRAY_CLASS));
    }

    @Test
    void testMethodAsFunctionByName() {
        // Method 1
        final Function<String, Object> publicOne = methodAsFunction(LOOKUP, METHOD_CLAZZ, PUBLIC_NAME, OBJECT_CLASS);
        publicOne.apply("Test");

        final Function<String, Object> privateOne = methodAsFunction(LOOKUP, METHOD_CLAZZ, PRIVATE_NAME, OBJECT_CLASS);
        privateOne.apply("Test");

        // Method 2
        final Function<String, Object> publicTwo =
                methodAsFunction(LOOKUP, OTHER_METHODS_CLAZZ, PUBLIC_NAME, OBJECT_CLASS);
        publicTwo.apply("Test");

        // - The lookup does not belong to this class/method
        // - - Does no longer apply to Java 11 or later
        //assertThrows(IllegalArgumentException.class,
        //        () -> methodAsFunction(LOOKUP, OTHER_METHODS_CLAZZ, PRIVATE_NAME, OBJECT_CLASS));

        // Other
        assertThrows(IllegalArgumentException.class,
                () -> methodAsFunction(LOOKUP, METHOD_CLAZZ, PRIMITIVE_NAME, PRIMITIVE_CLASS));

        final Function<Object, Object> primitive = methodAsFunction(LOOKUP, METHOD_CLAZZ, PRIMITIVE_NAME, OBJECT_CLASS);
        primitive.apply(1);

        final Function<Object[], Object> array = methodAsFunction(LOOKUP, METHOD_CLAZZ, ARRAY_NAME, ARRAY_CLASS);
        array.apply(null);

        assertThrows(IllegalArgumentException.class,
                () -> methodAsFunction(LOOKUP, METHOD_CLAZZ, MISSING_NAME, OBJECT_CLASS));

        assertThrows(IllegalArgumentException.class,
                () -> methodAsFunction(LOOKUP, METHOD_CLAZZ, MISSING_NAME, ARRAY_CLASS));
    }

    @Test
    void testPublicMethodAsFunctionByReflection() {
        // Method 1
        final Function<String, Object> publicOne = publicMethodAsFunction(PUBLIC_TEST);
        publicOne.apply("Test");

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(PRIVATE_TEST));

        // Method 2
        final Function<String, Object> publicTwo = publicMethodAsFunction(OTHER_PUBLIC_TEST);
        publicTwo.apply("Test");

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(OTHER_PRIVATE_TEST));

        // Other
        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(PRIMITIVE_IN_TEST));

        final Function<Object, Object> primitive = publicMethodAsFunction(PRIMITIVE_OUT_TEST);
        primitive.apply(1);

        final Function<Object[], Object> array = publicMethodAsFunction(ARRAY_TEST);
        array.apply(null);

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(NO_RETURN_TEST));

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(NO_PARAM_TEST));

        assertThrows(IllegalArgumentException.class,
                () -> publicMethodAsFunction(TWO_PARAM_TEST));
    }

    @Test
    void testMethodAsFunctionByReflection() {
        // Method 1
        final Function<String, Object> publicOne = methodAsFunction(LOOKUP, PUBLIC_TEST);
        publicOne.apply("Test");

        final Function<String, Object> privateOne = methodAsFunction(LOOKUP, PRIVATE_TEST);
        privateOne.apply("Test");

        // Method 2
        final Function<String, Object> publicTwo = methodAsFunction(LOOKUP, OTHER_PUBLIC_TEST);
        publicTwo.apply("Test");

        // - The lookup does not belong to this class/method
        // - - Does no longer apply to Java 11 or later
        //assertThrows(IllegalArgumentException.class,
        //        () -> methodAsFunction(LOOKUP, OTHER_PRIVATE_TEST));

        // Other
        assertThrows(IllegalArgumentException.class,
                () -> methodAsFunction(LOOKUP, PRIMITIVE_IN_TEST));

        final Function<Object, Object> primitive = methodAsFunction(LOOKUP, PRIMITIVE_OUT_TEST);
        primitive.apply(1);

        final Function<Object[], Object> array = methodAsFunction(LOOKUP, ARRAY_TEST);
        array.apply(null);

        assertThrows(IllegalArgumentException.class,
                () -> methodAsFunction(LOOKUP, NO_RETURN_TEST));

        assertThrows(IllegalArgumentException.class,
                () -> methodAsFunction(LOOKUP, NO_PARAM_TEST));

        assertThrows(IllegalArgumentException.class,
                () -> methodAsFunction(LOOKUP, TWO_PARAM_TEST));
    }

    @SuppressWarnings("unused")
    public static final class Methods {

        private static final Lookup LOOKUP = MethodHandles.lookup();

        public static Object publicTest(final Object object) {
            return object;
        }

        private static Object privateTest(final Object object) {
            return object;
        }

        public static Object primitive(final int object) {
            return object;
        }

        public static int primitive(final Object object) {
            return (int) object;
        }

        public static Object array(final Object[] object) {
            return null;
        }

        public static void noReturn(final Object object) {}

        public static Object noParam() {
            return null;
        }

        public static Object twoParam(final Object object1, final Object object2) {
            return null;
        }

    }

    @SuppressWarnings("unused")
    public static final class OtherMethods {

        public static Object publicTest(final Object object) {
            return object;
        }

        private static Object privateTest(final Object object) {
            return object;
        }

    }

}
