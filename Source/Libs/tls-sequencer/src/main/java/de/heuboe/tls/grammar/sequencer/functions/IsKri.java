package de.heuboe.tls.grammar.sequencer.functions;

import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgDevice;
import de.heuboe.tls.cfgsv.bridge.classes.TlsCfgItemType;
import de.heuboe.tls.grammar.base.ValueCollection;
import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This function checks if the passed parameter is an id and belongs to a device in the config service. Further it will
 * be checked, if a device was found, if this has the type KRI. If the parameter is an id of a KRI the {@link Value} 1
 * will be returned, else null.
 */
@Slf4j
public class IsKri extends Function {

    /**
     * This function checks if the passed parameter is an id and belongs to a device in the config service. Further it
     * will be checked, if a device was found, if this has the type KRI. If the parameter is an id of a KRI the
     * {@link Value} 1 will be returned, else null.
     *
     * @param parameterCount         The amount of possible parameters for this function as {@link String}.
     * @param sequencerBeanContainer The {@link SequencerBeanContainer} that contains the config service.
     */
    public IsKri(String parameterCount, SequencerBeanContainer sequencerBeanContainer) {
        // call the super constructor with the parameter count for this function
        super(parameterCount, sequencerBeanContainer);
    }

    /**
     * This is the main logic of the isKri function. It will request the config service for the id of the executing
     * object and check if the returned object has the type KRI.
     *
     * @param dataFromBroker The data from the broker.
     * @param variableTable  The map of variables in the current context.
     *
     * @return the {@link Value} 1 if the passed parameter is an id of a KRI, else null.
     */
    @Override
    public Value execute(Object dataFromBroker, Map<String, Variable> variableTable) {
        // get the first parameter because we only have one
        Value id = getParameters().getFirst().eval(dataFromBroker, variableTable);

        // if by some strange reasons no id should be available return here
        if (id == null) {
            return null;
        }

        // try to get a device with the passed parameter from the config service
        TlsCfgDevice device = getSequencerBeanContainer().getOsi7Cfg().getDeviceOfEa(id.getStringValue());

        // if no device was found in the config service print a warning and return null
        if (device == null) {
            log.debug("For id '{}' no device could be found in config service!", id.getStringValue());
            return null;
        }

        // check if the device is a KRI
        if (device.getType().equals(TlsCfgItemType.KRI)) {
            return new ValueCollection.IntValue(1);
        }

        log.debug("The device with the id '{}' is not of the type KRI!", id.getStringValue());
        return null;
    }
}
