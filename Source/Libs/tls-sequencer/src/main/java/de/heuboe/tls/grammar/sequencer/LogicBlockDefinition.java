package de.heuboe.tls.grammar.sequencer;

import de.heuboe.tls.grammar.interfaces.Filler;
import de.heuboe.tls.grammar.interfaces.sequencer.BlockDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Class holding the necessary contents to analyse one logic block.
 *
 * @author Alexander Schulze
 */
public class LogicBlockDefinition implements BlockDefinition {

    @Getter @Setter
    private String name = "unset";

    @Getter @Setter
    private EnumMap<ObjectProperty, String> options = new EnumMap<>(ObjectProperty.class);

    @Getter @Setter
    private boolean isHeader = false;

    @Getter @Setter
    private List<String> tags = new ArrayList<>();

    @Setter
    private List<Filler> fillerRules;

    @Override
    public List<Filler> getFillerRules() {
        if (fillerRules == null) {
            fillerRules = new ArrayList<>();
        }
        return fillerRules;
    }

    @Override
    public void add(Filler rule) {
        if (fillerRules == null) {
            fillerRules = new ArrayList<>();
        }
        fillerRules.add(rule);
    }

    @Override
    public void complete() {
        // Do nothing
    }
}
