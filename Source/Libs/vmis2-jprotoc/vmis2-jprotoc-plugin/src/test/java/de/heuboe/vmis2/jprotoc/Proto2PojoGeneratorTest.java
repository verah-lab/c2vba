package de.heuboe.vmis2.jprotoc;

import static de.heuboe.vmis2.jprotoc.Proto2PojoGenerator.hasFlagOrBooleanOption;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests for {@link Proto2PojoGenerator}.
 *
 * @author Daniel Theuke (daniel.theuke@heuboe.de)
 */
class Proto2PojoGeneratorTest {

    private static final String BOOLEAN_FLAG = "--boolean";

    /**
     * Tests for {@link Proto2PojoGenerator#hasFlagOrBooleanOption(String[], String)}.
     *
     * @return The tests.
     */
    @TestFactory
    Stream<DynamicTest> testHasFlagOrBooleanOption() {
        return Stream.of(

                dynamicTest("absent", () -> {
                    final String[] args = {};
                    assertFalse(hasFlagOrBooleanOption(args, BOOLEAN_FLAG));
                }),

                dynamicTest("flag", () -> {
                    final String[] args = {
                            BOOLEAN_FLAG
                    };
                    assertTrue(hasFlagOrBooleanOption(args, BOOLEAN_FLAG));
                }),

                dynamicTest("empty-option", () -> {
                    final String[] args = {
                            BOOLEAN_FLAG + '='
                    };
                    assertFalse(hasFlagOrBooleanOption(args, BOOLEAN_FLAG));
                }),

                dynamicTest("true-option", () -> {
                    final String[] args = {
                            BOOLEAN_FLAG + "=true"
                    };
                    assertTrue(hasFlagOrBooleanOption(args, BOOLEAN_FLAG));
                }),

                dynamicTest("false-option", () -> {
                    final String[] args = {
                            BOOLEAN_FLAG + "=false"
                    };
                    assertFalse(hasFlagOrBooleanOption(args, BOOLEAN_FLAG));
                }),

                dynamicTest("undefined-option", () -> {
                    final String[] args = {
                            BOOLEAN_FLAG + "=undefined"
                    };
                    assertFalse(hasFlagOrBooleanOption(args, BOOLEAN_FLAG));
                })

        );

    }

}
