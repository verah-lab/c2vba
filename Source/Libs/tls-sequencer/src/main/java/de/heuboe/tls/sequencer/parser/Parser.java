package de.heuboe.tls.sequencer.parser;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import de.heuboe.tls.cfglib.Osi7Cfg;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgDevice;
import de.heuboe.tls.grammar.base.*;
import de.heuboe.tls.grammar.exceptions.ThrowingErrorListener;
import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.grammar.interfaces.sequencer.BlockDefinition;
import de.heuboe.tls.grammar.sequencer.*;
import de.heuboe.tls.grammar.sequencer.flops.FlopStatement;
import de.heuboe.tls.grammar.sequencer.parser.SequencerLexer;
import de.heuboe.tls.grammar.sequencer.parser.SequencerParser;
import de.heuboe.tls.parser.proto.DataType;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.parser.proto.model.AccessMember;
import de.heuboe.tls.parser.proto.model.AccessPath;
import de.heuboe.tls.parser.proto.model.DataField;
import de.heuboe.tls.sequencer.config.SequencerProperties;
import de.heuboe.tls.sequencer.model.SequencerDataType;
import de.heuboe.tls.sequencer.model.SequencerGlobals;
import de.heuboe.tls.sequencer.services.SequencerMessageManagement;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import de.heuboe.tls.sequencer.utils.SequencerUtils;
import de.heuboe.tls.tlstele.meta.Osi7Id;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static de.heuboe.tls.sequencer.utils.SequencerUtils.*;

/**
 * This class is responsible for parsing the sequencer scripts.
 */
@Component
@Slf4j
public class Parser {

    private static final String SPEC_FILE = "spec.yaml";
    private static final String SCRIPT_FILE_EXTENSION = ".txt";
    private static final String SCRIPT_FILE_PREFIX = "seq-uz_";

    private final SequencerMessageManagement sequencerMessageManagement;

    // flag to determine if one of the objects has been changed
    private boolean modified = false;
    private final String scriptPath;
    private final List<String> globalScripts;
    private final String stageName;
    private final String systemName;
    private final String testModePath;
    private final String fileSeparator;
    private final Map<String, SequencerParser> scripts = new LinkedHashMap<>();
    private final HashMap<String, Variable> variableTable = new HashMap<>();
    private final Map<String, SequencerGlobals> globals = new HashMap<>();
    @Getter
    private final Set<SequencerDataType> topics = new HashSet<>();
    @Setter
    private Osi7Cfg osi7Cfg;
    @Setter
    SequencerBeanContainer sequencerBeanContainer;

    /**
     * This constructor will create the {@link Parser} and initialize basic properties.
     *
     * @param sequencerMessageManagement The {@link SequencerMessageManagement} Bean.
     * @param scriptPath                 The path were specification file and scripts should be loaded from.
     * @param globalScripts              A list of scripts that will be loaded every time.
     * @param stageName                  The name of the current stage to determine which scripts should be loaded.
     * @param systemName                 The name of the current system to determine which scripts should be loaded.
     * @param testModePath               A file path where test scripts are placed. Can be empty.
     * @param fileSeparator              The system file separator to assemble file names and paths.
     */
    public Parser(SequencerMessageManagement sequencerMessageManagement,
                  @org.springframework.beans.factory.annotation.Value("${de.heuboe.tls.sequencer.script.path:/config}")
                  String scriptPath,
                  @org.springframework.beans.factory.annotation.Value("#{'${de.heuboe.tls.sequencer.script.globalScripts}'.split(',')}")
                  List<String> globalScripts,
                  @org.springframework.beans.factory.annotation.Value("${de.heuboe.tls.sequencer.script.stageName:}")
                  String stageName,
                  @org.springframework.beans.factory.annotation.Value("${de.heuboe.tls.sequencer.script.systemName:}")
                  String systemName,
                  @org.springframework.beans.factory.annotation.Value("${de.heuboe.tls.sequencer.script.testModePath:}")
                  String testModePath,
                  @org.springframework.beans.factory.annotation.Value("${file.separator}")
                  String fileSeparator) {
        this.sequencerMessageManagement = sequencerMessageManagement;
        this.globalScripts = new ArrayList<>();
        this.stageName = stageName;
        this.systemName = systemName;
        this.testModePath = testModePath;
        this.fileSeparator = fileSeparator;
        globalScripts.forEach(script -> this.globalScripts.add(script.trim()));

        // check if path exists
        if (new File(scriptPath).exists()) {
            this.scriptPath = scriptPath;
        } else {
            this.scriptPath = ""; // necessary to keep variable final
            String errMsg = "The path to script files '" + scriptPath + "' does not exist. Stopping sequencer!";
            log.error(errMsg);
            sequencerMessageManagement.sendMessage(errMsg);
            System.exit(-1);
        }

        init();
    }

    /**
     * This method will start the parsing process for the passed {@link GenericProtoObject}.
     *
     * @param topic            The topic the triggering object came from.
     * @param inputObject      The object that should be parsed by the script.
     * @param sequencerMessage A flag that signals if a sequencer message was received.
     * @return The object results of the script parsing.
     * @throws IOException if script parsing fails.
     */
    public Set<GenericProtoObject> parse(String topic, GenericProtoObject inputObject, boolean sequencerMessage)
            throws IOException {

        if (osi7Cfg == null) {
            log.error("Osi7Cfg was not initialized!");
            System.exit(-1);
        }

        // the resulting object
        Set<GenericProtoObject> result = new LinkedHashSet<>();

        scripts.forEach((script, parser) -> {
            // clear the set of DataTypes to avoid OOM
            parser.getDataTypes().clear();
            // set the sequencerBeanContainer
            parser.setSequencerBeanContainer(sequencerBeanContainer);
            // reset the state of the parser
            parser.reset();
            // and reinitialize the parser to apply the change
            parser.sequencer();

            TransformationRules rules = parser.getTransformationRules();

            if (rules.getDefinitionNames().isEmpty()) {
                log.warn("Script '{}' contains no rules. Skipping script!", script);
                return;
            }

            // add script name to global variables
            variableTable.put("CURRENT_SCRIPT_NAME",
                    new ConstantValue(new ValueCollection.StringValue(script.substring(script.lastIndexOf("\\") + 1))));

            // load global variables for current script
            variableTable.putAll(globals.get(script).getGlobal());

            result.addAll(handleObjects(topic, inputObject, sequencerMessage, script, rules));

            // update globals
            globals.get(script).getGlobal().forEach((name, value) ->
                    value.setValue(variableTable.get(name).getValue()));

            // remove global variables for current script
            globals.get(script).getGlobal().forEach((name, value) -> variableTable.remove(name));

            // remove script name from global variables
            variableTable.remove("CURRENT_SCRIPT_NAME");
        });

        // check if an object has changed
        if (modified) {
            // reset modified flag
            modified = false;
            if (!result.isEmpty()) {
                filterDoubleObjects(result);
                return result;
            } else {
                return Collections.singleton(inputObject);
            }

        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Check set of {@link GenericProtoObject}s for duplication. and remove objects of the same data type with the same id. If double objects appear only the
     * first will remain.
     *
     * @param result The object set that
     */
    private void filterDoubleObjects(Set<GenericProtoObject> result) {
        if (result.size() > 1) {
            Set<GenericProtoObject> markedForDeletion = new HashSet<>();
            Set<GenericProtoObject> copy = new LinkedHashSet<>(result);

            result.forEach(gpoOrigin -> {
                copy.remove(gpoOrigin);
                copy.forEach(gpoCopy -> {
                    // only compare if objects are from the same data type
                    if (gpoOrigin.getClassName().equals(gpoCopy.getClassName())
                            // skip objects that are identically
                            && (gpoOrigin != gpoCopy)
                            // compare ids of both objects
                            && (gpoOrigin.getStringValue("id").equals(gpoCopy.getStringValue("id")))) {
                        // mark object for deletion if the objects are from the same data type, are not
                        // identically but have the same id
                        markedForDeletion.add(gpoCopy);
                    }
                });
            });

            // remove objects from result object that are marked for deletion
            result.removeAll(markedForDeletion);
            markedForDeletion.forEach(gpo -> log.trace("Duplicated object of type '{}' detected. Remove object: {}",
                    gpo.getClassName(), gpo));
        }
    }

    /**
     * Fulfill the main handling of the incoming object against the current script.
     *
     * @param topic            The topic the triggering object came from.
     * @param inputObject      The {@link GenericProtoObject} that must be a list object.
     * @param sequencerMessage The flag that determine that this message was originally sent by the sequencer itself.
     * @param script           The name of the current processed script.
     * @param rules            The {@link TransformationRules} that represent the content of the current script.
     * @return a {@link GenericProtoObject} that contains the manipulated object or null if nothing was modified.
     */
    private Set<GenericProtoObject> handleObjects(String topic,
                                                  GenericProtoObject inputObject,
                                                  boolean sequencerMessage,
                                                  String script,
                                                  TransformationRules rules) {
        // the resulting object
        Set<GenericProtoObject> resultSet = new HashSet<>();

        // the input data can be a normal object or a list object
        List<GenericProtoObject> objects = GenericProtoObject.listToObjects(inputObject);

        ObjectResult objectResult = new ObjectResult();

        for (GenericProtoObject object : objects) {
            // add all attributes of the current object to a global variable table
            addObjectAttributesToVarTable(object);

            // starting to interpret the input object against the parser rules
            BlockDefinition definition = rules.getDefinition(topic);
            if (definition == null) {
                log.debug("In script '{}' no definition block with name '{}' could be found!",
                        script, topic);
            } else if (notSupportedMessage(sequencerMessage, definition)) {
                log.debug("Sequencer message received but definition block '{}' in script '{}' does not " +
                                "support recognition of sequencer messages. Skipping message handling.",
                        topic, script);
            } else {
                handleObjectForRule(inputObject, script, object, definition, objectResult, resultSet);
            }
        }

        // remove all attributes of the input object from a global variable table to restore the origin content
        removeObjectAttributesFromVarTable(inputObject);

        return resultSet;
    }

    /**
     * Handles the processing of an object based on a specific rule, executing associated logic and updating results.
     *
     * @param inputObject  The {@link GenericProtoObject} that must be a list object. Used for logging messages.
     * @param script       The name of the current processed script.
     * @param object       The object to be processed and evaluated against the rules.
     * @param definition   The {@link BlockDefinition} containing the set of rules for processing the object.
     * @param objectResult The result object used to capture the outcome of the rule execution.
     * @param resultSet    The set to collect all relevant result objects after rule processing.
     */
    private void handleObjectForRule(GenericProtoObject inputObject, String script, GenericProtoObject object,
                                     BlockDefinition definition, ObjectResult objectResult,
                                     Set<GenericProtoObject> resultSet) {

        handleObjectProperties(object, definition);

        log.debug("Checking object '{}' against a rule ...", inputObject.getClassName());
        log.trace("Checking object '{}' {} against a rule ...", inputObject.getClassName(), inputObject);
        // execute the rule on the current object
        for (Filler rule : definition.getFillerRules()) {
            execute(rule, objectResult, object, script);
            if (objectResult.getResult() != null) {
                modified = true;
            }
        }

        // add object result to result set
        if (objectResult.getResult() != null) {
            resultSet.addAll((Set) objectResult.getResult());
        }

        // remove all attributes of the object from a global variable table to restore the origin content
        removeObjectAttributesFromVarTable(object);

        if (objectResult.getResult() != null) {
            resultSet.forEach(this::removeObjectAttributesFromVarTable);
        }
    }

    /**
     * Here we will handle all possible {@link ObjectProperty} objects in this context.
     *
     * @param object     The {@link GenericProtoObject} that will be affected by an {@link ObjectProperty}.
     * @param definition The current {@link BlockDefinition} that will be checked for the existence of an
     *                   {@link ObjectProperty}.
     */
    private void handleObjectProperties(GenericProtoObject object, BlockDefinition definition) {
        // if present save the name of the executed rule for later logging
        if (definition.getOptions().containsKey(ObjectProperty.NAME)) {
            object.getMetaData().put(SequencerUtils.SCRIPT_BLOCK_NAME, definition.getOptions().get(ObjectProperty.NAME));
        }

        // if present save the boolean parameter of the object property AUTO_FILL_TLS_TIME
        if (definition.getOptions().containsKey(ObjectProperty.AUTO_FILL_TLS_TIME)) {
            object.getMetaData().put(ObjectProperty.AUTO_FILL_TLS_TIME.name(),
                    definition.getOptions().get(ObjectProperty.AUTO_FILL_TLS_TIME));
        }
    }

    /**
     * Check if we received a sequencer message and if the script rule should react on them.
     *
     * @param sequencerMessage A flag that determine if the received message is a sequencer message.
     * @param definition       A {@link BlockDefinition} for the current context.
     * @return false if the message is not supported else true.
     */
    private boolean notSupportedMessage(boolean sequencerMessage, BlockDefinition definition) {
        return (sequencerMessage //
                && (definition.getOptions().isEmpty() //
                || !definition.getOptions().containsKey(ObjectProperty.SELF)
                || definition.getOptions().get(ObjectProperty.SELF).equals("false")));
    }

    /**
     * Loads the specification file for db2osi as HashMap and all script files as simple list of strings representing
     * the name of the script for usage in the parser environment.
     */
    private void init() {
        loadScripts();
        loadVariables();
        loadTopics();
        loadGlobals();
    }

    /**
     * Load the list of variables defined in the specification file. The resulting hash map will contain the name of the
     * variable as key and the value as {@link ValueCollection}. If the value is specified as hex value (e.g. 0x03) it
     * will be recalculated as integer value. If the value is an array definition it will be mapped into
     * {@link ArrayVariable}.
     */
    private void loadVariables() {

        String currentValue = "";
        String currentKey = "";

        // load yaml file as InputStream
        try (InputStream yamlStream = new FileInputStream(scriptPath + fileSeparator + SPEC_FILE)) {

            log.info("Loading spec file '{}{}{}'.", new File(scriptPath).getPath(), fileSeparator, SPEC_FILE);

            // load yaml input
            Yaml yaml = new Yaml();
            Map<String, Object> spec = yaml.load(yamlStream);

            // load data from spec.yaml
            for (Map.Entry<String, Object> entry : spec.entrySet()) {
                currentValue = entry.getValue().toString();
                currentKey = entry.getKey();

                // differ between hex and numeric entries and save them on different ways in the interpreter environment
                if (currentValue.startsWith("0x")) {
                    // decode hex values into integer
                    variableTable.put(currentKey, new ProtectedBasicVariable(
                            currentKey, new ValueCollection.IntValue(Integer.decode(currentValue))));
                } else if (currentValue.startsWith("[") && currentValue.endsWith("]")) {
                    loadArrayVariables(currentKey, entry);
                } else {
                    variableTable.put(currentKey, new ProtectedBasicVariable(
                            currentKey, new ValueCollection.IntValue(Integer.parseInt(currentValue))));
                }
            }
            log.info("{} variables loaded from specification file '{}'.", variableTable.size(), SPEC_FILE);
        } catch (NumberFormatException nfe) {
            String errMsg = "The hex value of the specification parameter '" + currentKey + "' in '" + SPEC_FILE
                    + "' could not be decoded to an integer value!";
            sequencerMessageManagement.sendMessage(errMsg);
            throw new NumberFormatException(errMsg);
        } catch (FileNotFoundException e) {
            String errMsg = "Specification file '" + SPEC_FILE + "' could not be loaded at path ' " + scriptPath
                    + "'!";
            sequencerMessageManagement.sendMessage(errMsg);
            log.error(errMsg);
            System.exit(-1);
        } catch (IOException e) {
            String errMsg = "Failed to load specification file '" + SPEC_FILE + "' with error message "
                    + e.getLocalizedMessage();
            sequencerMessageManagement.sendMessage(errMsg);
            log.error(errMsg);
            System.exit(-1);
        }
    }

    /**
     * Convert defined array variables from specification file into {@link ArrayVariable}s and add them to the varTable
     * map.
     *
     * @param currentKey The current key of the specification entry.
     * @param entry      The entry for the current key from the specification file.
     */
    private void loadArrayVariables(String currentKey, Map.Entry<String, Object> entry) {
        // first check if the value is of the type list
        if (entry.getValue() instanceof List<?> unknownList && !unknownList.isEmpty()) {
            try {
                ArrayVariable arrayVariable = null;
                // check if the list contains String elements
                if (unknownList.getFirst() instanceof String) {
                    arrayVariable = new ArrayVariable(currentKey,
                            unknownList
                                    .stream()
                                    .map(String.class::cast)
                                    .map(ValueCollection.StringValue::new)
                                    .map( Value.class::cast )
                                    .toList());

                } else if (unknownList.getFirst() instanceof Integer) {
                    arrayVariable = new ArrayVariable(currentKey,
                            unknownList
                                    .stream()
                                    .map(Integer.class::cast)
                                    .map(ValueCollection.IntValue::new)
                                    .map( Value.class::cast )
                                    .toList());
                    variableTable.put(currentKey, arrayVariable);
                } else {
                    log.warn("The datatype for the defined array '{}' is not supported. The value " +
                            "will not be available in script parsing! ", currentKey);
                }

                // add generated ArrayVariable to variableTable map
                if (arrayVariable != null) {
                    variableTable.put(currentKey, arrayVariable);
                }
            } catch (ClassCastException e) {
                log.warn("The datatype of the elements for the defined array '{}' are mixed. " +
                                "This is not allowed. The value will not be available in script parsing! ",
                        currentKey);
            }
        }
    }

    /**
     * Loads all names and scripts as {@link SequencerParser} into the global {@link LinkedHashMap} scripts.
     */
    private void loadScripts() {
        // get all script files from configured folder
        try {
            // extract all scripts from global script path
            scripts.putAll(extractScriptFiles());

            if (!scripts.isEmpty()) {
                log.info("{} script file{} loaded. {}", scripts.size(), (scripts.size() > 1 ? "s" : ""),
                        scripts.keySet());
            } else {
                String msg = "No script files were loaded. Stopping sequencer!";
                sequencerMessageManagement.sendMessage(msg);
                System.exit(-1);
            }

        } catch (MalformedURLException e) {
            log.error("", e);
        }
    }

    /**
     * Search for sequencer scripts in the passed path and returns them as {@link Map} with the script name as
     * key and the {@link SequencerParser} as value sorted by the insertion. It loads script files with path and naming
     * priority. Scripts from the testModePath will be handled with the highest priority. After that scripts will be
     * matched by naming in the following order.
     * <ul>
     * <li>seq-uz_${STAGE_NAME}.txt</li>
     * <li>seq-uz_${SYSTEM_NAME}.txt</li>
     * <li>seq-uz_${SYSTEM_NAME}-${STAGE_NAME}*.txt</li>
     * </ul>
     * At last the global scripts from the globalScripts list will be loaded.
     *
     * @return a {@link Map} with the script name as key and the {@link SequencerParser} as value sorted by insertion.
     * @throws MalformedURLException if the given path is malformed.
     */
    private Map<String, SequencerParser> extractScriptFiles() throws MalformedURLException {
        File path = new File(scriptPath);
        URL url = path.toURI().toURL();

        log.info("Loading script files from '{}'.", path.getPath());

        Set<String> extractedScripts = new LinkedHashSet<>();
        Map<String, SequencerParser> result = new LinkedHashMap<>();

        // search test scripts in configured test mode path
        getTestScripts(extractedScripts);

        // if test scripts were found do not load any further scripts
        if (extractedScripts.isEmpty()) {

            // check for existence is not necessary because it will be checked in constructor
            File f = new File(url.getPath());

            // search for global stage scripts
            getSpecificGlobalScripts(stageName, extractedScripts, f);

            // search for global system scripts
            getSpecificGlobalScripts(systemName, extractedScripts, f);

            // search for global system and stage scripts
            getSpecificGlobalScripts(systemName + "_" + stageName, extractedScripts, f);

            // search global scripts
            extractedScripts.addAll(Arrays.stream(Objects.requireNonNull(f.list()))
                    .filter(globalScripts::contains)
                    .map(file -> scriptPath + fileSeparator + file) // add script path
                    .collect(Collectors.toSet()));
        }

        // load for every script the SequencerParser and put it into the result map with the script name as key
        extractedScripts.forEach(script -> {
            try {
                result.put(script, getSequencerParser(loadScriptFile(script)));
            } catch (IOException e) {
                log.error("", e);
            } catch (ParseCancellationException pce) {
                log.error("Parsing the script '{}' fails with message: {}", script, pce.getMessage());
                System.exit(-1);
            }
        });

        return result;
    }

    /**
     * Retrieves specific global scripts based on the provided name and adds them to the extractedScripts set. It
     * filters files in the given directory matching the name with a specific prefix and extension.
     *
     * @param name             The name to match against the script files in the directory.
     * @param extractedScripts The set where the matched script paths will be added.
     * @param f                The directory file object to search for matching script files.
     */
    private void getSpecificGlobalScripts(String name, Set<String> extractedScripts, File f) {
        if (!name.isEmpty()) {
            extractedScripts.addAll(Arrays.stream(Objects.requireNonNull(f.list()))
                    .filter(file -> file.toLowerCase()
                            .startsWith(SCRIPT_FILE_PREFIX.toLowerCase()
                                    + name.toLowerCase()
                                    + SCRIPT_FILE_EXTENSION.toLowerCase()))
                    .map(file -> scriptPath + fileSeparator + file) // add script path
                    .collect(Collectors.toSet()));
        }
    }

    /**
     * Collects test scripts from the configured test mode path and adds them to the provided set. Only files that start
     * with a configurable prefix and end with a configurable extension are considered.
     *
     * @param extractedScripts The set to which the paths to the test scripts are added.
     */
    private void getTestScripts(Set<String> extractedScripts) {
        if (!testModePath.isEmpty()) {

            // create File object for test mode path
            File f = new File(testModePath);

            // only continue if the path exists to avoid NullPointerExceptions
            if (f.exists()) {
                extractedScripts.addAll(Arrays.stream(Objects.requireNonNull(f.list()))
                        .filter(file -> file.toLowerCase()
                                .startsWith(SCRIPT_FILE_PREFIX.toLowerCase())
                                && file.endsWith(SCRIPT_FILE_EXTENSION.toLowerCase()))
                        .map(file -> testModePath + fileSeparator + file) // add script path
                        .collect(Collectors.toSet()));
            } else {
                log.warn("Configured test mode path '{}' does not exist on file system!", testModePath);
            }
        }
    }

    /**
     * Load the script file as {@link CharStream}. If no script file could be found a {@link FileNotFoundException} will
     * be thrown.
     *
     * @param script The path and name of the script that should be loaded into a {@link CharStream}.
     * @return a {@link CharStream} of the script file.
     * @throws FileNotFoundException if the no script with the given name could be found.
     */
    private CharStream loadScriptFile(String script) throws IOException {
        File file = new File(script);

        // test if script file exists in script uz sub directory
        if (file.exists()) {
            FileInputStream stream = new FileInputStream(file);
            CharStream result = CharStreams.fromStream(stream);
            stream.close();
            return result;
        }

        throw new FileNotFoundException("No script file found at '" + script + "'.");
    }

    /**
     * Load a list of used topics and their history flags from all scripts.
     */
    private void loadTopics() {
        scripts.forEach((script, parser) -> topics.addAll(parser.getDataTypes()));
    }

    /**
     * Load defined global variables from all scripts and save them in a HashMap for later usage.
     */
    private void loadGlobals() {
        scripts.forEach((script, parser) -> globals.put(script, parser.getSequencerGlobals()));
    }

    /**
     * Creates a {@link SequencerParser} based on a {@link CharStream} that should represent a script. The parser will
     * be initialized with the sequencer rule. That means that special get methods of the parser will be initialized.
     *
     * @param stream The {@link CharStream} of the script.
     * @return a SequencerParser based on the input script.
     * @throws ParseCancellationException if parsing the script fails.
     */
    private SequencerParser getSequencerParser(CharStream stream) throws ParseCancellationException {
        SequencerLexer lexer = new SequencerLexer(stream);
        CommonTokenStream token = new CommonTokenStream(lexer);
        SequencerParser parser = new SequencerParser(token);

        // remove all standard error listeners
        parser.removeErrorListeners();
        // add own error listener to be sure that the application start fail if the script could not be parsed
        parser.addErrorListener(new ThrowingErrorListener(sequencerMessageManagement));

        parser.sequencer();
        return parser;
    }

    /**
     * Load all attributes and their values to the variableTable for later processing.
     *
     * @param object The object that contain the variables that should be loaded.
     */
    private void addObjectAttributesToVarTable(GenericProtoObject object) {

        // read special content for list elements
        if ((!object.getClassName().endsWith("List") && object.getStringValue("id") != null)) {
            // collect several info from config service
            Osi7Id ea = osi7Cfg.getOsi7IdOfEa(object.getStringValue("id"));
            TlsCfgDevice node = osi7Cfg.getDeviceOfEa(object.getStringValue("id"));
            String cluster = osi7Cfg.getClusterEaPermId(object.getStringValue("id"));

            if (ea != null) {
                // add DeNummer of the current object
                addVariable(new BasicVariable(VARIABLE_DE_NUMBER, new ValueCollection.IntValue(ea.getDe())));
                // add Eaid of the current object
                addVariable(new BasicVariable(VARIABLE_EAID, new ValueCollection.StringValue(object.getStringValue("id"))));
            } else {
                log.debug("For '{}' with id '{}' no Osi7Id could be retrieved from config service. Variable " +
                                "'{}' and '{}' will not be available in script parsing!",
                        object.getClassName(), object.getStringValue("id"), VARIABLE_DE_NUMBER, VARIABLE_EAID);
            }

            if (node != null) {
                // add associated node id of the current object
                addVariable(new BasicVariable(VARIABLE_NODE_ID, new ValueCollection.StringValue(node.getId())));
            } else {
                log.debug("For '{}' with id '{}' no node could be retrieved from config service. Variable " +
                                "'{}' will not be available in script parsing!",
                        object.getClassName(), object.getStringValue("id"), VARIABLE_NODE_ID);
            }

            if (!StringUtils.isEmpty(cluster)) {
                // add associated cluster id of the current object
                addVariable(new BasicVariable(VARIABLE_CLUSTER_ID, new ValueCollection.StringValue(cluster)));
            } else {
                log.debug("For '{}' with id '{}' no cluster could be retrieved from config service. Variable " +
                                "'{}' will not be available in script parsing!",
                        object.getClassName(), object.getStringValue("id"), VARIABLE_CLUSTER_ID);
            }
        }

        // add data fields of the object
        addDataFields(object);
    }

    /**
     * Add all data fields of the {@link GenericProtoObject} to the variableTable.
     *
     * @param object The {@link GenericProtoObject} whose fields should be added.
     */
    private void addDataFields(GenericProtoObject object) {
        // add data fields of the object
        for (DataField field : object.getFields()) {
            Value value = getValueOfNonRepeatedField( object, field ); // result may be null if field is not repeated
            if (field.repeated() && field.dataType() == DataType.INTEGER) {
                Descriptors.FieldDescriptor fd = object.getFieldDescriptor( field.name() );
                
                Object o = object.getObject().getField( fd );
                List<?> l = (List<?>) o;
                if (l.isEmpty()) {
                    List<Value> emptyList = Collections.emptyList();
                    addVariable( new ArrayVariable( field.name(), emptyList ) );
                } else {
                    addVariable( new ArrayVariable( field.name(),
                             l.stream().map( v -> (Value) (new ValueCollection.IntValue( (Integer) v ) ) ) .toList() ) );
                }
                log.debug( "Adding repeated data field '{}' to variable table.", field.name() );
            } else {
                if( value != null ) {
                    addVariable( new BasicVariable( field.name(), value ) );
                } else {
                    log.debug(
                             "The data type '{}' is currently not supported as value! The value from object field" +
                             " '{}' will not be available in the scripts.", field.dataType(),
                             field.name() );
                }
            }
        }
    }
    
    /**
     * Retrieves the value of a non-repeated field from the given generic protocol object.
     * The method checks the field's data type and returns the corresponding value.
     *
     * @param object The generic protocol object from which the field value is extracted.
     * @param field The data field whose value is to be retrieved. Must not be marked as repeated.
     * @return The value of the field wrapped in the appropriate value type object (e.g., IntValue, DoubleValue,
     *         StringValue, TimestampValue), or null if the field is marked as repeated.
     * @throws IllegalArgumentException If the field's data type is unknown.
     */
    private Value getValueOfNonRepeatedField( GenericProtoObject object, DataField field ) {
        if (!field.repeated()) {
            return switch( field.dataType() ) {
                case INTEGER ->   new ValueCollection.IntValue( object.getIntegerValue( field.name() ) );
                case DOUBLE ->    new ValueCollection.DoubleValue( object.getDoubleValue( field.name() ) );
                case STRING ->    new ValueCollection.StringValue( object.getStringValue( field.name() ) );
                case TIMESTAMP -> new ValueCollection.TimestampValue( object.getTimestampValue( field.name() ) );
                default -> throw new IllegalArgumentException( "Unknown data type: " + field.dataType() );
            };
        }
        return null; // never mind, repeated will be handled later
    }
    
    /**
     * Remove all attributes and their values of the object from the variableTable.
     *
     * @param object The object that contain the variables that should be removed.
     */
    private void removeObjectAttributesFromVarTable(GenericProtoObject object) {
        removeVariable(VARIABLE_DE_NUMBER);
        removeVariable(VARIABLE_EAID);
        removeVariable(VARIABLE_NODE_ID);
        removeVariable(VARIABLE_CLUSTER_ID);

        // remove data fields of the object
        for (DataField field : object.getFields()) {
            switch (field.dataType()) {
                case INTEGER, DOUBLE, STRING, TIMESTAMP, BYTE_STRING:
                    removeVariable(field.name());
                    break;
                case PROTO_OBJECT:
                    if (field.name().equals("elements")) {
                        // for proto list objects we must check every field of every list item
                        ((GeneratedMessageV3) object.get(new AccessPath().add(
                                new AccessMember("elements", 0))
                        )).getAllFields().keySet().forEach(f -> removeVariable(f.getJsonName()));
                    }
                    break;
                default:
                    log.warn("The data type '{}' is currently not supported as value!", field.dataType());
                    break;
            }
        }
    }

    /**
     * Add a Variable in a defined way to the global variable table.
     *
     * @param variable The Variable that should be added to the variable table.
     */
    private void addVariable(Variable variable) {
        variableTable.put(variable.getName(), variable);
    }

    /**
     * Remove an entry from the global variable table that match the passed key string.
     *
     * @param key The name of the variable that should be removed from the variable table.
     */
    private void removeVariable(String key) {
        variableTable.remove(key);
    }

    /**
     * Checks and manipulate the current object against the script rules. It manipulates the {@link ObjectResult} object
     * that is passed into the function by script modifications. Further the input object will be updated as well.
     *
     * @param filler       The parsed script.
     * @param objectResult The result for the objects every filler writes to.
     * @param object       The object that should be parsed by the script.
     * @param script       The name of the currently parsed script used for error messages.
     */
    private void execute(Filler filler, ObjectResult objectResult, GenericProtoObject object, String script) {
        try {
            // only execute if filler rules are from specific types
            if (filler.getClassName().equals(IfStatement.class.getSimpleName())
                    || filler.getClassName().equals(SwitchCaseStatement.class.getSimpleName())
                    || filler.getClassName().equals(FlopStatement.class.getSimpleName())
                    || filler.getClassName().equals(CopyStatement.class.getSimpleName())
                    || filler.getClassName().equals(ObjectAssignStatement.class.getSimpleName())
                    || filler.getClassName().equals(Message.class.getSimpleName())) {
                filler.execute(objectResult, 0, object, variableTable);
            }
        } catch (Exception e) {
            handleErrorCase(object, script, e);
        }
    }

    /**
     * Handles an error scenario for a given input object while executing a script. Logs the error details and sends a
     * message with the error information to the message management system.
     *
     * @param object The {@code GenericProtoObject} that caused the error.
     * @param script The name of the script being executed when the error occurred.
     * @param e      The {@code Exception} that was thrown during the error case.
     */
    private void handleErrorCase(GenericProtoObject object, String script, Exception e) {
        String id = "-";
        // try to find the correct id for the object
        if (variableTable.containsKey(VARIABLE_EAID)) {
            id = variableTable.get(VARIABLE_EAID).getValue().getStringValue();
        } else if (!object.getStringValue("id").isEmpty()) {
            id = object.getStringValue("id");
        }

        String errMsg = String.format("Handling the input object '%s' (id: %s | deNr: %s | FG: %s) in script '%s' "
                        + "failed with error '%s'!",
                object.getClassName(),
                id,
                (variableTable.containsKey(VARIABLE_DE_NUMBER) ?
                        variableTable.get(VARIABLE_DE_NUMBER).getValue().getStringValue() :
                        "-"),
                (variableTable.containsKey("fg") ? variableTable.get("fg").getValue().getStringValue() : "-"),
                script,
                e.getLocalizedMessage());

        log.error(errMsg);

        // only print stack trace if debug logging is enabled
        if (log.isDebugEnabled()) {
            log.error("", e);
        } else {
            log.warn("The stack trace for this error is only available if log level debug is enabled for this " +
                    "class!");
        }

        if (id.equals("-")) {
            sequencerMessageManagement.sendMessage(errMsg);
        } else {
            sequencerMessageManagement.sendMessage(errMsg, id);
        }
    }
}
