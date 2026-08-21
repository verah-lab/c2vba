package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgDevice;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgFg;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgItemType;
import de.heuboe.tls.grammar.base.AssignStatement;
import de.heuboe.tls.grammar.base.BasicVariable;
import de.heuboe.tls.grammar.base.ValueCollection;
import de.heuboe.tls.grammar.interfaces.Expression;
import de.heuboe.tls.grammar.interfaces.Result;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.parser.proto.GenericProtoObject;
import de.heuboe.tls.parser.proto.model.DataField;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import de.heuboe.tls.sequencer.utils.SequencerUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import static de.heuboe.tls.grammar.sequencer.EaTargetType.DE;

/**
 * This class will handle more object based statement assignment for the sequencer.
 */
@Slf4j
public class ObjectAssignStatement extends AssignStatement {

    @Getter
    @Setter
    private EaTargetType eaTargetType;

    @Getter
    @Setter
    private Variable eaTargetId;

    @Getter
    @Setter
    private int fgGroup;

    @Getter
    @Setter
    private String objName;

    @Setter
    @Getter
    private ObjectDirection objDirection;

    @Getter
    @Setter
    private String targetTopic;

    private final SequencerUtils utils = new SequencerUtils();
    private SequencerBeanContainer sequencerBeanContainer;

    /**
     * Constructs an {@link ObjectAssignStatement} with a configObjectId, object name, object direction, target topic
     * (can be null), property name and right handed statement {@link Expression}.
     *
     * @param eaTargetType           The target type of the object.
     * @param eaTargetId             The object id of an object from the ConfigService as {@link Variable}.
     * @param fgGroup                The function group for retrieving eas for nodes.
     * @param objName                The name of the object as string.
     * @param objDirection           The direction of the object should be sent.
     * @param targetTopic            The kafka topic the object should be sent to. Can be null.
     * @param varName                The name of the objects property as string.
     * @param rhs                    A right handed statement {@link Expression}.
     * @param sequencerBeanContainer The {@link SequencerBeanContainer} that contains necessary Beans for execution.
     */
    public ObjectAssignStatement(String eaTargetType, Variable eaTargetId, int fgGroup, String objName,
            ObjectDirection objDirection, String targetTopic, String varName, Expression rhs,
            SequencerBeanContainer sequencerBeanContainer) {
        this(objName, objDirection, targetTopic, varName, rhs, sequencerBeanContainer);
        this.eaTargetType = EaTargetType.findByKeyWord(eaTargetType);
        this.eaTargetId = eaTargetId;
        this.fgGroup = fgGroup;
        this.sequencerBeanContainer = sequencerBeanContainer;
    }

    /**
     * Constructs an {@link ObjectAssignStatement} with an object name, object direction, target topic, property name
     * and right handed statement {@link Expression}.
     *
     * @param objName                The name of the object as string.
     * @param objDirection           The direction of the object should be sent.
     * @param targetTopic            The kafka topic the object should be send to. Can be null.
     * @param varName                The name of the objects property as string.
     * @param rhs                    A right handed statement {@link Expression}.
     * @param sequencerBeanContainer The {@link SequencerBeanContainer} that contains necessary Beans for execution.
     */
    public ObjectAssignStatement(String objName, ObjectDirection objDirection, String targetTopic, String varName,
            Expression rhs, SequencerBeanContainer sequencerBeanContainer) {
        super(varName, rhs);
        this.objName = objName;
        this.objDirection = objDirection;
        this.targetTopic = targetTopic;
        this.sequencerBeanContainer = sequencerBeanContainer;
    }

    @Override
    public int execute(Result result, int ptr, Object inputData, Map<String, Variable> variableTable) {
        BasicVariable value = new BasicVariable(super.getVarName(), getRhs().eval(inputData, variableTable));

        if (inputData instanceof GenericProtoObject gpo) {

            // if no object is defined
            if (objName != null) {

                // check type of target and collect target ids
                Set<String> targetIds = getTargetId(gpo.getStringValue("id"), variableTable, inputData);

                // construct requested objects for target ids and add them to the result object
                utils.handleObjectsForTargetIds(objName, getVarName(), objDirection, targetTopic, targetIds, gpo,
                        value, result, variableTable);

            } else {
                Map<String, Object> metaData = gpo.getMetaData();
                // add direction to meta data
                metaData.put(ObjectDirection.class.getSimpleName(), this.objDirection);
                // add target topic to meta data
                metaData.put(SequencerUtils.TOPIC_TARGET_KEY, targetTopic);

                // write the input data to the field of the object
                writeField(gpo, value, inputData, variableTable, result);
            }
        }
        return 0; // no bytes to advance
    }

    /**
     * Retrieve a set of target ids depending on the target type.
     *
     * @param id            The object id of the executing object.
     * @param variableTable A definition of variables.
     * @param inputData     A data object containing the data that will be used for executing.
     * @return A list of strings that represent the permanent ids of the targets.
     */
    private Set<String> getTargetId(String id, Map<String, Variable> variableTable, Object inputData) {
        Set<String> resultIds = new HashSet<>();
        // if no target type is defined we will use the target of the executing object
        if (eaTargetType == null) {
            eaTargetType = DE;
        }

        // if no target id is defined we will use the id of the executing object
        if (eaTargetId != null) {
            id = buildEaTargetId(variableTable, inputData);
        }

        // the id can be null so we must check this before further handling
        if (id != null) {
            switch (eaTargetType) {
                case EA:
                    // use targetEaId
                    resultIds.add(id);
                    break;
                case CLUSTER_DE:
                    handleClusterDe(id, resultIds);
                    break;
                case NODE_DE:
                    handleNodeDe(id, resultIds);
                    break;
                case NODE_KRI:
                    // get all nodes of a KRI
                    resultIds.addAll(getNodesOfKri(id));
                    break;
                case DES_OF_CLUSTER:
                    handleDesOfCluster(id, resultIds);
                    break;
                case DES_OF_NODE:
                    // get all ea ids for the node and function group
                    resultIds.addAll(getEaIdsForNodeAndFg(id, fgGroup));
                    break;
                case DES_OF_KRI:
                    resultIds.addAll(getEasOfKri(id, fgGroup));
                    break;
                case DE:
                    // use id of executing object
                    resultIds.add(id);
                    break;
                default:
                    log.error("Handling of Ea target type {} is not implemented!", eaTargetType.getKeyWord());
            }
        }
        return resultIds;
    }

    /**
     * Retrieve ids for DEs of a cluster.
     *
     * @param id        The cluster identifier.
     * @param resultIds The result set of the retrieved ea ids for the cluster.
     */
    private void handleClusterDe(String id, Set<String> resultIds) {
        // check if id is a cluster
        if (sequencerBeanContainer.getOsi7Cfg().getClusteredEasOfClusterEaPermId(id) != null) {
            resultIds.add(id);
        } else {
            log.warn("The id '{}' could not be retrieved as cluster in the config service.", id);
        }
    }

    /**
     * Retrieve ids for DEs of a EA.
     *
     * @param id        The EA identifier.
     * @param resultIds The result set of the retrieved device ids for the EA.
     */
    private void handleNodeDe(String id, Set<String> resultIds) {
        // check if id is a node
        if (sequencerBeanContainer.getOsi7Cfg().getDeviceOfEa(id) != null) {
            resultIds.add(sequencerBeanContainer.getOsi7Cfg().getDeviceOfEa(id).getId());
        } else {
            log.warn("The id '{}' could not be retrieved as node in the config service.", id);
        }
    }

    /**
     * Retrieve ids for DEs of a cluster.
     *
     * @param id        The cluster identifier.
     * @param resultIds The result set of the retrieved ea ids for the cluster.
     */
    private void handleDesOfCluster(String id, Set<String> resultIds) {
        // get all ea ids for the cluster
        List<String> clusterEas = sequencerBeanContainer.getOsi7Cfg().getClusteredEasOfClusterEaPermId(id);
        if (clusterEas != null) {
            resultIds.addAll(clusterEas);
        } else {
            log.warn("For cluster id '{}' no devices could be retrieved in the config service.", id);
        }
    }

    /**
     * Build the target id for the current object based on the type {@link Variable} type of the eaTargetId. Currently
     * the {@link String} type and {@link ArrayAccessVariable} type is supported.
     *
     * @param variableTable A definition of variables.
     * @param inputData     A data object containing the data that will be used for executing.
     * @return the generated id as string or null if array access fails.
     */
    private String buildEaTargetId(Map<String, Variable> variableTable, Object inputData) {
        // only handle eaTargetIds that are set in the script
        if (eaTargetId != null) {

            // handle different kinds of eaTargetId types
            if (eaTargetId instanceof ArrayAccessVariable accessVariable) {
                // handle array access
                Variable variable = variableTable.get(accessVariable.getName());
                if (variable instanceof ArrayVariable) {
                    variableTable.put(ArrayVariable.INDEX,
                            new BasicVariable(ArrayVariable.INDEX,
                                    new ValueCollection.StringValue(accessVariable.getIndex())));
                    Value val = variable.eval(inputData, variableTable);
                    if (val != null) {
                        return val.getStringValue();
                    }
                }
            } else if (eaTargetId instanceof BasicVariable) {
                // simply return the string content of the BasicVariable
                return eaTargetId.getValue().getStringValue();
            }
        }

        return null;
    }

    /**
     * Checks if the variable is a field in the input object and writes the content of the input data into the field.
     *
     * @param gpo           The object the new values should be set.
     * @param value         The value that contains the name and value of the field that should be set.
     * @param inputData     A data object containing the data that will be used for executing.
     * @param variableTable A definition of variables.
     * @param result        The object for saving the final result.
     */
    private void writeField(GenericProtoObject gpo, BasicVariable value, Object inputData,
            Map<String, Variable> variableTable, Result result) {
        if (gpo.getFieldDescriptor(super.getVarName()) != null) {
            for (DataField field : gpo.getFields()) {
                if (field.name().equals(super.getVarName())) {
                    // fill object parameter with new value for defined parameter
                    if (!utils.setValue(gpo, field, value)) {
                        // end here
                        return;
                    }

                    // update value in variableTable
                    variableTable.put(super.getVarName(), value);
                }
            }
            // write object to result
            utils.addOrUpdateResult(result, (GenericProtoObject) inputData);
        } else {
            StringBuilder sb = new StringBuilder();
            gpo.getFields().forEach(f -> sb.append(f.name()).append(", "));
            log.warn("Field '{}' could not be found in object '{}'. The following fields are usable: '{}'",
                    super.getVarName(), gpo.getClassName(), sb.substring(0, sb.length() - 2));
        }
    }

    /**
     * Retrieves for a node id and a function group all corresponding ids of ea devices.
     *
     * @param nodeId The id of the node.
     * @param fg     The function group for which the ea ids should be retrieved.
     * @return a set of ea ids.
     */
    private Set<String> getEasForNodeAndFg(String nodeId, int fg) {
        Set<String> eas = new HashSet<>();

        try {
            // get all fg elements for the current node id and fg group
            Optional<TlsCfgFg> fgElements = sequencerBeanContainer.getOsi7Cfg().getDevice(nodeId).getFgsList().stream()
                    .filter(group -> group.getNumber() == fg)
                    .findFirst();

            // extract all ea ids for the retrieved fg elements
            fgElements.ifPresent(tlsCfgFg -> tlsCfgFg.getEasList().forEach(ea -> eas.add(ea.getEaid())));
        } catch (NullPointerException e) {
            // do nothing, just ignore it and return the empty set
        }

        return eas;
    }

    /**
     * Get a list of all nodes that are placed under the KRI with the passed id.
     *
     * @param id The id of the KRI all underlying nodes should be retrieved for.
     * @return a list of node ids that are under the requested KRI.
     */
    private List<String> getNodesOfKri(String id) {
        TlsCfgDevice device = sequencerBeanContainer.getOsi7Cfg().getDevice(id);
        List<String> result = new ArrayList<>();

        // check if the input device is a KRI
        if (device.getType() == TlsCfgItemType.KRI) {
            // get all nodes of the KRI
            result.addAll(sequencerBeanContainer.getOsi7Cfg()
                    .getDescendants(id, true, null).stream()
                    .map(dev -> sequencerBeanContainer.getOsi7Cfg()
                            .getDeviceOfEa(sequencerBeanContainer.getOsi7Cfg().getDeviceId(dev)))
                    .filter(dev -> dev.getType() == TlsCfgItemType.RST)
                    .map(TlsCfgDevice::getId)
                    .collect(Collectors.toList()));
        } else {
            log.warn("For the id '{}' a device of the type '{}' was expected but a device of the type '{}' " +
                            "was detected in the config service.",
                    id, TlsCfgItemType.KRI, device.getType());
        }

        return result;
    }

    /**
     * Get a list of eas that are placed under the KRI with the passed id for the passed function group.
     *
     * @param id The id of the KRI all underlying eas should be retrieved for.
     * @param fg The function group for retrieving eas for the KRI.
     * @return a list of ea ids that are under the requested KRI for the defined function group.
     */
    private List<String> getEasOfKri(String id, int fg) {
        List<String> result = new ArrayList<>();

        // get all nodes of the KRI
        List<String> nodes = new ArrayList<>(getNodesOfKri(id));

        for (String node : nodes) {
            // for every node get every EA
            result.addAll(getEaIdsForNodeAndFg(node, fg));
        }

        return result;
    }

    /**
     * Get a list of eas that are placed under the node with the passed id for the passed function group.
     *
     * @param id The id of the node all underlying eas should be retrieved for.
     * @param fg The function group for retrieving eas for the node.
     * @return a list of ea ids that are under the requested node for the defined function group.
     */
    private List<String> getEaIdsForNodeAndFg(String id, int fg) {
        List<String> result = new ArrayList<>();
        Set<String> nodeEas = getEasForNodeAndFg(id, fg);
        if (!nodeEas.isEmpty()) {
            result.addAll(nodeEas);
        } else {
            log.debug("For node id '{}' and fg group '{}' no devices could be retrieved in the config service.",
                    id, fg);
        }
        return result;
    }
}
