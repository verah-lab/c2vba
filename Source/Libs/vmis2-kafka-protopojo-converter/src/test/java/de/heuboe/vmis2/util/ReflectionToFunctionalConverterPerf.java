package de.heuboe.vmis2.util;

import static de.heuboe.vmis2.util.ReflectionToFunctionalConverter.publicMethodAsFunction;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.RunnerException;

/**
 * Dummy class for performance comparisons between inline, method, reflection and {@link Function}
 * calls.
 *
 * <p>
 * <b>Usage:</b>
 * </p>
 *
 * <ul>
 * <li>Run <tt>mvn clean package</tt> to generate the
 * <tt>target/test-classes/META-INF/BenchmarkList</tt> file.</li>
 * <li>Run this class from IDE</li>
 * </ul>
 *
 * <p>
 * <b>Note:</b> If you change the benchmark options you need to rebuild the project using maven.
 * </p>
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Measurement(time = 15, timeUnit = TimeUnit.SECONDS)
@Warmup(time = 15, timeUnit = TimeUnit.SECONDS)
public class ReflectionToFunctionalConverterPerf {

    public static final Random RANDOM = new Random();

    public static final Lookup LOOKUP = MethodHandles.lookup();
    public static final Class<?> CLASS = ReflectionToFunctionalConverterPerf.class;
    public static final String METHOD_NAME = "increment";
    public static final Class<?> PARAMETER_TYPE = Integer.class;
    public static final Method METHOD = uncheckedGetMethod(CLASS, METHOD_NAME, PARAMETER_TYPE);
    public static final Function<Integer, Integer> METHOD_FUNCTION = value -> {
        try {
            return (Integer) METHOD.invoke(null, value);
        } catch (final Throwable e) {
            throw new RuntimeException("Failed", e);
        }
    };
    public static final Function<Integer, Integer> FUNCTION = publicMethodAsFunction(METHOD);

    private static Method uncheckedGetMethod(final Class<?> clazz, final String name,
            final Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException("Someone screwed the test up", e);
        }
    }

    public static int increment(final Integer i) {
        return i + 1;
    }

    @State(Scope.Thread)
    public static class InternalState {

        public int i = RANDOM.nextInt();

    }

    @Benchmark
    public int incrementInlined(final InternalState state) {
        return state.i + 1;
    }

    @Benchmark
    public int incrementDirect(final InternalState state) {
        return increment(state.i);
    }

    @Benchmark
    public int incrementReflectCached(final InternalState state) {
        try {
            return (int) METHOD.invoke(null, state.i);
        } catch (final Throwable e) {
            throw new RuntimeException("Failed", e);
        }
    }

    @Benchmark
    public int incrementReflectUncached(final InternalState state) {
        try {
            return (int) CLASS.getDeclaredMethod(METHOD_NAME, PARAMETER_TYPE).invoke(null, state.i);
        } catch (final Throwable e) {
            throw new RuntimeException("Failed", e);
        }
    }

    @Benchmark
    public int incrementReflectFunction(final InternalState state) {
        return METHOD_FUNCTION.apply(state.i);
    }

    @Benchmark
    public int incrementFunctional(final InternalState state) {
        return FUNCTION.apply(state.i);
    }

    public static void main(final String[] args) throws RunnerException, IOException {
        final File bmFile = new File("META-INF/BenchmarkList");
        if (!bmFile.exists()) {
            final File parent = bmFile.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
                parent.deleteOnExit();
            }
            final File sourceFile = new File("target/test-classes/META-INF/BenchmarkList").getAbsoluteFile();
            Files.copy(sourceFile.toPath(), bmFile.toPath());
            bmFile.deleteOnExit();
        }
        org.openjdk.jmh.Main.main(args);
    }

}
