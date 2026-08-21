package de.heuboe.tls.receiver.interfaces;

import java.util.List;

import de.heuboe.tls.receiver.core.TransformationRules;
import de.heuboe.tls.receiver.impl.DataObject;
import de.heuboe.tls.tlstele.TlsTele;


/**
 * @author Ronald Nikel
 * Interface defining the relevant methods when receiving a telegram (Sammeltelegramm) in order to analyse it
 */
public interface Transformer {
	/**
	 * @param addressConverter The address converter to be used
	 * Sets the address converter to be used
	 */
	public void setAddressConverter(AddressConverter addressConverter);
	/**
	 * @param transformationRules The transformation rules to be used
	 * Set the the transformation rules to be used
	 */
	public void setTransformationRules(TransformationRules transformationRules);
	/**
	 * After setting the address converter and the transformation rules the init method should be called in order to intialize the object
	 */
	public void init();
	/**
	 * After setting the address converter and the transformation rules the init method should be called in order to intialize the object
	 * @param behaviour The behaviour to be applied to the transformation process
	 */
	public void init(Behaviour behaviour);
	
	/**
	 * @param tlsTele The telegram to be analysed (Sammeltelegramm) 
	 * @return A List of DataObjects
	 * This call decomposes a telegram into its parts. The transformation rules and the address converter are involved to do the analysis 
	 */
	public List<DataObject> transform(TlsTele tlsTele);

	public static class Behaviour {
	        private boolean badBlockToBeDelivered = true; // -> ast01
                private Behaviour() {}
                /** Create an Behaviour object
                 * @return An Behaviour object
                 */
                static public Behaviour create() {
                        return new Behaviour(); 
                }
                /**
                 * @param newVal New value for behaviour concerning delivery of bad blocks. Default ist to deliver bad blocks (i.e. true)
                 * @return The Behaviour-Object.
                 */
                public Behaviour setBadBlocksDelivery( boolean newVal  ) {
                        badBlockToBeDelivered = newVal;
                        return this;
                }
                /**
                 * @return the current state of bad block delivery
                 */
                public boolean isBadBlocksDelivery() {
                        return badBlockToBeDelivered;
                }
        }
}
