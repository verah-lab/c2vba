package de.heuboe.tls.grammar.sequencer.functions;

import de.heuboe.tls.grammar.interfaces.Value;
import de.heuboe.tls.grammar.interfaces.Variable;
import de.heuboe.tls.grammar.sequencer.ArrayVariable;
import de.heuboe.tls.sequencer.utils.SequencerBeanContainer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class GetAtIndex extends Function {
    
    Variable arrayVariable;
    Variable indexVariable;
    /**
     * The constructor with the parameter count representation as string. The parameter can be a simple integer value if
     * the function will support exact the amount of parameter. If a range of parameter is necessary the different
     * amounts must be delimited by a pipe symbol. For example if a function should support 0, 2 and 4 parameter, the
     * parameterCount must be 0|2|4.
     *
     * @param parameterCount         The amount of parameters the function should support as string.
     * @param sequencerBeanContainer The SequencerBeanContainer that hold several beans for usage inside of functions.
     */
    public GetAtIndex( String parameterCount, SequencerBeanContainer sequencerBeanContainer ) {
        super( parameterCount, sequencerBeanContainer );
    }
    
    @Override
    public void addParameters( List<Variable> parameters ) {
        if ( 2 != parameters.size() )  {
            log.error("The function '{}' is used incorrectly. A wrong parameter count was detected. Received {} " +
                      "parameter but only exactly 2 are allowed!",
                     this.getClassName(), parameters.size() );
            throw new IllegalArgumentException("Wrong parameter count!");
        }
        super.addParameters( parameters );
        arrayVariable = parameters.get(0);
        indexVariable = parameters.get(1);
    }
    
    /**
     * This is the main logic of the extending function.
     *
     * @param dataFromBroker The data from the broker.
     * @param variableTable  The map of variables in the current context.
     * @return the {@link Value} of the function. This can also be null!
     */
    @Override
    public Value execute( Object dataFromBroker, Map< String, Variable > variableTable ) {
        Value index = indexVariable.eval( dataFromBroker, variableTable );
        int indexValue = index.getIntValue();
        String arrayName = arrayVariable.getName();
        arrayName = arrayName.replaceFirst( "null\\.\\.", "" );
        Variable variable = variableTable.get( arrayName );
        if( variable instanceof ArrayVariable arr ) {
            return arr.getAtIndex( indexValue );
        } else {
            log.debug( "The variable '{}' is not an array variable!", arrayName );
        }
        return null;
    }
}
