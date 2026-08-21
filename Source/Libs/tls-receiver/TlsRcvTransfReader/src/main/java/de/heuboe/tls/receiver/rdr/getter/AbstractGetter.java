package de.heuboe.tls.receiver.rdr.getter;

import java.nio.channels.IllegalSelectorException;
import java.util.List;
import java.util.Map;

import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.DataObjectIf;
import de.heuboe.tls.receiver.interfaces.GetterRule;

/**
 * A class implementing some components of the interface @see GetterRule
 * @author Ronald Nikel
 */
public abstract class AbstractGetter implements GetterRule {
        
        protected String name;
        protected DataItemType resType = null;
        protected boolean locationContext = false; // if rule is used in a location context
        private final String EMPTY_STRING = "";

	protected AbstractGetter( String name ) {
	        this.name = name;
	        this.resType = null;
	}

        /* (non-Javadoc)
         * @see de.heuboe.tls.receiver.interfaces.GetterRule#getName()
         * Return the
         */
	@Override
        public String getName() {
                return name;
        }

	@Override
        public DataItemType getType() {
                return resType;
        }
	
	protected void setType( DataItemType type ) {
	        this.resType = type;
	}

        public boolean isLocationContext() {
                return locationContext;
        }

        public void setLocationContext( boolean locationContext ) {
                this.locationContext = locationContext;
        }

//        public String getTargetType() {
//                return EMPTY_STRING;
//        }
}
