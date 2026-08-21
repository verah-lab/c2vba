package de.heuboe.tls.receiver.rdr.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.heuboe.tls.receiver.interfaces.DeBlockDefinitionIf;
import de.heuboe.tls.receiver.interfaces.TransformationRulesContainer;
import de.heuboe.tls.receiver.rdr.parser.Parser;


/**
 * Objects of this class are used to store all tls analysis descriptions of a given context
 * @author Ronald Nikel
 *
 */
public class TransformationRules implements TransformationRulesContainer {
	
	private static final Logger LOGGER = LogManager.getLogger(TransformationRules.class);
	
	private Map<Integer, List<DeBlockDefinitionIf>> definitions = new HashMap<>();

	/**
	 * Retrieve a list of analysis definitions to matching triple fg/id/typ
	 * @param fg fg of deblock i.e. telegram
	 * @param id id of deblock i.e. telegram
	 * @param typ type of deblock
	 * @return deblock analysis description used to analyse a deblock of the given trip[le fg/ig/typ
	 */
	public List<DeBlockDefinitionIf> getDefinition(int fg, int id, int typ) {
		int key = (fg << 16) + (id << 8) + typ;
		return definitions.get(key);
	}
	
	/**
	 * Add analysis definition to a global table
	 * @param def deblock analysis description to be stored
         * @param ctx piece of information from the parser describing the current context of parsing
	 */
	public void addDefinition(DeBlockDefinition def, ParserRuleContext ctx) {
		for(Integer fg : def.getFg()) {
			for(Integer id : def.getId()) {
				int typ = def.getTyp();
				int key = (fg << 16) + (id << 8) + typ;
				List<DeBlockDefinitionIf> l = definitions.get(key);
				if (null != l) {
				        checkUseOfLocationNames( fg, id, typ, l, ctx );
				} else {
				        l = new ArrayList<>();
				}
				l.add( def );
				logdefinitionAdded( def, fg, id, typ );
				definitions.put(key, l);
			}
		}		
	}

         private void checkUseOfLocationNames( Integer fg, Integer id, int typ, List<DeBlockDefinitionIf> l, ParserRuleContext ctx ) {
                for ( DeBlockDefinitionIf l0 : l ) {
                        DeBlockDefinition ll = (DeBlockDefinition) l0;
                        if (null == ll.getSpecialLocationsName() && null == ll.getSpecialLocationsExcludedName()) {
                                throw new IllegalStateException("duplicate (no locations used) Fg/Id/Typ: " + fg + "/" + id + "/" + typ + " " + Parser.inputLocation( ctx ) );
                        }
                }
        }

        /**
         * Prepare text that may be logged
         * @param def deblock analysis description used to analyse a deblock
         * @param fg fg of deblock i.e. telegram
         * @param id id of deblock i.e. telegram
         * @param typ type of deblock
         */
        private void logdefinitionAdded( DeBlockDefinition def, Integer fg, Integer id, int typ ) {
                String add = "";
                if (null != def.getSpecialLocationsName()) {
                        add = "@" + def.getSpecialLocationsName();
                }
                if (null != def.getSpecialLocationsExcludedName()) {
                        add = "-@" + def.getSpecialLocationsExcludedName();
                }
                LOGGER.info("Add DeBlock Definition " + fg + "/" + id + "/" + typ + " = " + def.getName() + " " + add );
        }
	
        /**
         * @return A set of all keys used in the map. Used to inspect the map. 
         */
        public Set<Integer> getKeySet() {
                return definitions.keySet();
        }
        
}
