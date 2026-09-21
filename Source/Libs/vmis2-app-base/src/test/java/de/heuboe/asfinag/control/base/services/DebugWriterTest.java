package de.heuboe.asfinag.control.base.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.vmis_ehe.vmis2.control.data.pojo.PSituationCatalogue;
import eu.vmis_ehe.vmis2.control.data.pojo.PSituationClass;

public class DebugWriterTest {

    private String path = "data";
    private String fn = "SituationCatalogue";
    private String fnSuffix = "-test";
    private Path stubDataPath = Path.of("src/test/resources/stubData");
    private static PSituationCatalogue situationCatalogue;

    @BeforeAll
    static void init() {

        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory()).registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());

        try {
            List<PSituationClass> l = objectMapper.readValue(new File("src/test/resources/situationClasses.yaml"),
                    new TypeReference<List<PSituationClass>>()
                    {});

            situationCatalogue = PSituationCatalogue.builder().iid("1").situationClassesList(l).build();

        } catch (JsonParseException e) {
            e.printStackTrace();
            fail();
        } catch (JsonMappingException e) {
            e.printStackTrace();
            fail();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }


    }

    @Test
    void writePb2JsonTest() {

        try {
            DebugWriter.writePb2Json(fn, PSituationCatalogue.to(situationCatalogue));

            // check test output content
            assertTrue(stubDataPath.toFile().isDirectory());

            File[] fileArray = stubDataPath.toFile().listFiles();
            assertNotNull(fileArray);
            assertTrue(fileArray.length == 1);

            assertTrue(fileArray[0].getAbsolutePath().contains(fn));
            String situationContent = new String(Files.readAllBytes(Paths.get(fileArray[0].getAbsolutePath())));
            assertFalse(situationContent.isEmpty());

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

    }

    @Test
    void writePb2JsonWithSubfolderTest() {

        try {

            DebugWriter.writePb2JsonWithSubfolder(false, path, fn, fnSuffix,
                    PSituationCatalogue.to(situationCatalogue));

            // check test output content
            File folder = new File(path);
            assertFalse(folder.isDirectory());

            File subfolder = new File(path + "/" + fn);
            assertFalse(subfolder.isDirectory());


            DebugWriter.writePb2JsonWithSubfolder(true, path, fn, fnSuffix, PSituationCatalogue.to(situationCatalogue));

            // check test output content
            assertTrue(folder.isDirectory());

            assertTrue(subfolder.isDirectory());

            File[] fileArray = subfolder.listFiles();
            assertNotNull(fileArray);
            assertTrue(fileArray.length == 1);

            assertTrue(fileArray[0].getAbsolutePath().contains(fn + fnSuffix));
            String situationContent = new String(Files.readAllBytes(Paths.get(fileArray[0].getAbsolutePath())));
            assertFalse(situationContent.isEmpty());

        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

    }

    @AfterEach
    void shutDown() {

        deleteDirectory(new File(path));
        deleteDirectory(stubDataPath.toFile());
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }
}
