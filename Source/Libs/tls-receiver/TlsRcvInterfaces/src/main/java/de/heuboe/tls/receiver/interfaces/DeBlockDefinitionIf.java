package de.heuboe.tls.receiver.interfaces;

import java.util.List;

/*
 * These objects hold the analysis definition of one deblock
 */
public interface DeBlockDefinitionIf {
        /**
         * get the seqence of instructions to analyse a deblock
         * @return the sequence of rules to apply to a debloc
         */
        public List<GetterRule> getGetterRules();
        
        /**
         * Add a rule the the definition for one deblock
         * @param rule rule to be added
         */
        public void add(GetterRule rule);
        
        /**
         * this method has to called, when a definition for one deblock is complete
         */
        public void complete();
}
