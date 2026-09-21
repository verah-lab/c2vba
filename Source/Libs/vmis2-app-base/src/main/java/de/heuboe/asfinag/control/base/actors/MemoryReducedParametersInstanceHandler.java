package de.heuboe.asfinag.control.base.actors;

import eu.vmis_ehe.vmis2.paramservice.pojo.PParameterSetList;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Optional;

/**
 * A special instance handler used to reduce the memory usage of parameter set list objects. Reduction is realized
 * by caching objects of different types ({@link ParametersMemoryReducer}).
 */
@RequiredArgsConstructor
public class MemoryReducedParametersInstanceHandler
        implements AbstractParameterActor.InstanceHandler<PParameterSetList> {

    /**
     * Base instance handler used to match instances and transform them into parameter set list objects.
     */
    private final AbstractParameterActor.InstanceHandler<PParameterSetList> baseInstanceHandler;

    /**
     * Reduction function used to reduce the memory usage of a parameter set list object.
     */
    private final ParametersMemoryReducer parametersMemoryReducer;

    @Override
    public boolean matchesInstance(ConsumerRecord<String, byte[]> r) {
        return baseInstanceHandler.matchesInstance(r);
    }

    @Override
    public Optional<AbstractParameterActor.Parameters<PParameterSetList>> toParameters(
            ConsumerRecord<String, byte[]> cr) {
        return baseInstanceHandler.toParameters(cr)
                .map(parametersMemoryReducer);
    }
}
