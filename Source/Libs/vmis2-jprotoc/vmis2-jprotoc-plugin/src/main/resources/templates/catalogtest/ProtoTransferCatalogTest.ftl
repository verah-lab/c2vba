// Generated from ${protoFile} on ${date}
// by ${plugin.groupId}:${plugin.artifactId}:${plugin.version}

package ${javaPackage};

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The catalog for the {@link BaseTransfer transfer defintions} specified in:
 * {@code ${protoFile}}
 */
public final class ${catalogClass}Test {

    /**
     * Tests that the interface version of the file matches the project version.
     */
    @org.junit.jupiter.api.Test
    void testInterfaceVersion() {
        final String interfaceVersion = ${catalogClass}.getInterfaceVersion();
        assertNotNull(interfaceVersion);
        if ("${version}".endsWith("-SNAPSHOT")) {
            assertEquals("${version}".replace("-SNAPSHOT", ""), interfaceVersion.replace("-SNAPSHOT", ""));
        } else {
            assertEquals("${version}", interfaceVersion);
        }
    }

}
