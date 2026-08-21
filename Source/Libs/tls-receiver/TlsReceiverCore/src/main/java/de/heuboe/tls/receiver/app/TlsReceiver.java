package de.heuboe.tls.receiver.app;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.heuboe.log.Logger;
import de.heuboe.tls.receiver.core.TransformationRules;
import de.heuboe.tls.receiver.impl.DataObject;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataWriter;
import de.heuboe.tls.receiver.interfaces.TeleReceiver;
import de.heuboe.tls.receiver.interfaces.TransformationReader;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.tlstele.TlsTele;

/**
 * The tls receiver is the class doing respectively delegating all the work.
 * In its init-method it starts a new thread waiting for telegrams. Received
 * telegrams are transformed to address information and data objects. These
 * are passed to a data writer.
 *  
 * @author ralfz
 *
 */
public class TlsReceiver {

	private static final Logger LOGGER = Logger.getLogger(TlsReceiver.class);
	
	private AddressConverter addressConverter;
	private DataWriter dataWriter;
	private TeleReceiver teleReceiver;
	private TransformationReader transformationReader;
	private Transformer transformer;
	private File specFile;
	private TransformationRules transformationRules;
	private Transformer.Behaviour behaviour;
	private boolean doRun;
	
	/**
	 * Construct a TlsReceiver object. This one ties up a lot of open ends and coordinates the objects implementing the defined interfaces.
	 * Default behaviour during transformation.
	 * @param addressConverter An object implementing the interface AddressConverter. Is used to possibly transform TLS-Identification to another identification (e.g permanent ids).
	 * @param dataWriter An object the handles the data analysed. e.g. coud be a wrtiter to a database
	 * @param teleReceiver An object implementing the interface TeleReceiver. This object will receive telegrams
	 * @param transformationReader An object implementing the interface TransformationReader. This object will return a map of GetterRules.
	 * @param transformer An object implementing the interface Transformer. This object will receive Telegrams in order to analyse them.
	 * @param specFile A File containing the definition for all GetterRules in this context.
	 */
	public TlsReceiver(AddressConverter addressConverter,
			DataWriter dataWriter, TeleReceiver teleReceiver,
			TransformationReader transformationReader, Transformer transformer, File specFile ) {
                this( addressConverter, dataWriter, teleReceiver, transformationReader, transformer, specFile, null );
	}
	
	/**
	 * Construct a TlsReceiver object. This one ties up a lot of open ends and coordinates the objects implementing the defined interfaces.
	 * @param addressConverter An object implementing the interface AddressConverter. Is used to possibly transform TLS-Identification to another identification (e.g permanent ids).
	 * @param dataWriter An object the handles the data analysed. e.g. coud be a wrtiter to a database
	 * @param teleReceiver An object implementing the interface TeleReceiver. This object will receive telegrams
	 * @param transformationReader An object implementing the interface TransformationReader. This object will return a map of GetterRules.
	 * @param transformer An object implementing the interface Transformer. This object will receive Telegrams in order to analyse them.
	 * @param specFile A File containing the definition for all GetterRules in this context.
	 * @param behaviour Define behaviour of transformation. Currently if bad deblocks will pass through
	 */
	public TlsReceiver(AddressConverter addressConverter,
	                DataWriter dataWriter, TeleReceiver teleReceiver,
	                TransformationReader transformationReader, Transformer transformer, File specFile, Transformer.Behaviour behaviour ) {
	        super();
	        this.addressConverter = addressConverter;
	        this.dataWriter = dataWriter;
	        this.teleReceiver = teleReceiver;
	        this.transformationReader = transformationReader;
	        this.transformer = transformer;
	        this.specFile = specFile;
	        this.behaviour = behaviour;
	        this.doRun = true;
	}

	public void setAddressConverter(AddressConverter addressConverter) {
		this.addressConverter = addressConverter;
	}

	public void setDataWriter(DataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}

	public void setTeleReceiver(TeleReceiver teleReceiver) {
		this.teleReceiver = teleReceiver;
	}

	public void setTransformationReader(TransformationReader transformationReader) {
		this.transformationReader = transformationReader;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}
	
	/** 
	 * Initialize the TlsReceiver object.
	 * This will start a thread polling the TlsReceiver. This thread will pass received telegrams to the transformer
	 * @throws IOException An IOException may arise when the given file for the transformation reader is invalid or defect. 
	 */
        public void init() throws IOException {
                transformationRules = transformationReader.createTransformationRules( specFile );
                transformer.setAddressConverter( addressConverter );
                transformer.setTransformationRules( transformationRules );
                if ( null == behaviour ) {
                        transformer.init();
                } else {
                        transformer.init( behaviour );
                }

                Runnable worker = new Runnable() {
			
			@Override
			public void run() {
				try {
					LOGGER.info("Starting Tls Receiver");
					boolean doRunLocal = false;
			                synchronized (this) {
			                        doRunLocal= doRun;
			                }
					while (doRunLocal) {
						List<TlsTele> teleList = teleReceiver.receive();
						if (!teleList.isEmpty()) {
							LOGGER.info("received " + teleList.size() + " telegrams");
						}
						for(TlsTele tlsTele : teleList) {									
							List<DataObject> objList = transformer.transform(tlsTele);
							if (null == objList) {
							        LOGGER.warning( "Telegram transformed to null. Probably an error within. Skipped!" );
							        continue;
							}
							LOGGER.debug("tele transformed to " + objList.size() + " objects");
							for(DataObject obj : objList) {
								dataWriter.write(obj);
							}
						}
	                                        synchronized (this) {
	                                                doRunLocal= doRun;
	                                        }
					}
				} catch(Exception e) {
					LOGGER.fatal("TlsReceiver: " + e);
					e.printStackTrace();
					throw e;
				}
			}	
		};
		Thread tlsReceiver = new Thread(worker, "TlsReceiver");
		tlsReceiver.start();
	}
	
	/**
	 * Allow the thread invoked by the init method to be stopped. Stooping is only possible after the next receipt of a telegram. But this receive action may have also been stopped.
	 */
	public void stop() {
		synchronized (this) {
			doRun = false;
		}
	}

        public TransformationRules getTransformationRules() {
                return transformationRules;
        }
}
