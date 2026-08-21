package de.heuboe.tls.receiver.interfaces;

import java.util.List;


/**
 * Objects of this class are used to store all tls analysis descriptions of a given context
 * @author Ronald Nikel
 *
 */

public interface TransformationRulesContainer {

        /**
         * Retrieve a list of analysis definitions to matching triple fg/id/typ
         * @param fg fg of deblock i.e. telegram
         * @param id id of deblock i.e. telegram
         * @param typ type of deblock
         * @return deblock analysis description used to analyse a deblock of the given trip[le fg/ig/typ
         */
        public List<DeBlockDefinitionIf> getDefinition(int fg, int id, int typ);
}
