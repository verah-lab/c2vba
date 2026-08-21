package de.heuboe.tls.receiver.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.heuboe.log.Logger;
import de.heuboe.tls.receiver.core.DeBlockDefinition;
import de.heuboe.tls.receiver.core.TlsReceiverException;
import de.heuboe.tls.receiver.core.TransformationRules;
import de.heuboe.tls.receiver.impl.DataObject.DeMeta;
import de.heuboe.tls.receiver.impl.DataObject.ETelMeta;
import de.heuboe.tls.receiver.interfaces.AddressConverter;
import de.heuboe.tls.receiver.interfaces.DataItem;
import de.heuboe.tls.receiver.interfaces.DataItem.DataItemType;
import de.heuboe.tls.receiver.interfaces.GetterRule;
import de.heuboe.tls.receiver.interfaces.Transformer;
import de.heuboe.tls.receiver.item.GregorianItem;
import de.heuboe.tls.receiver.item.IntegerItem;
import de.heuboe.tls.tlstele.TlsDeBlock;
import de.heuboe.tls.tlstele.TlsETel;
import de.heuboe.tls.tlstele.TlsTele;

public class TransformerImpl implements Transformer {

	private static final Logger LOGGER = Logger.getLogger(TransformerImpl.class);
	private Behaviour behaviour;
	
	private AddressConverter addressConverter;
	private TransformationRules transformationRules;
	private boolean initChecked = false;
	
	@Override
	public void setAddressConverter(AddressConverter addressConverter) {
		this.addressConverter = addressConverter;
		
	}

	@Override
	public void setTransformationRules(TransformationRules transformationRules) {
		this.transformationRules = transformationRules;		
	}

	@Override
	public void init() {
	        init(Behaviour.create());
	}
	
	@Override
	public void init( Behaviour behaviour) {
	        this.behaviour = behaviour;
	        if (addressConverter == null) {
	                throw new IllegalStateException("TransformerImpl: Address converter is not set");
	        }
	        if (transformationRules == null) {
	                throw new IllegalStateException("TransformerImpl: transformation rules are not set");
	        }
	        initChecked = true;
	}

	@Override
	public List<DataObject> transform(TlsTele tlsTele) {
	        if (!initChecked) {
	                throw new IllegalStateException( "Init was no called" );
	        }
	        boolean deliverBadBlocks = behaviour.isBadBlocksDelivery();
		DeBlockDefinition unknown = new DeBlockDefinition();
		unknown.setName("-withoutScript:Fg/Id/Typ-");
		int node = tlsTele.getLogAddress();
		List<DataObject> dataObjects = new ArrayList<>();
		
		GregorianCalendar rcvTimeBase = tlsTele.getRcvTimeBase();
                GregorianCalendar actualTime = new GregorianCalendar();
                actualTime.setTimeInMillis( tlsTele.getTimeStamp().getTime() );

                for(TlsETel etel : tlsTele.getEtels()) {
			eTelAnalysis( tlsTele, etel, unknown, node, dataObjects, rcvTimeBase, actualTime, deliverBadBlocks );
		}
		return dataObjects;
	}

        private void eTelAnalysis( TlsTele tlsTele, TlsETel etel, DeBlockDefinition unknownDeBlock, int node, List<DataObject> dataObjects, GregorianCalendar rcvTimeBase,
                        GregorianCalendar actualTime, boolean deliverBadBlocks ) {
                int fg = etel.getFg();
                int id = etel.getTlsId();
                boolean subsequent = false;
                DataObject header = null;
                int cnt = 0;
                
                Map<String, DataItem> etelVars = new HashMap<>();
                etelVars.put("#Node", new IntegerItem("#Node", node, 0)); // log address
                etelVars.put("#TlsFg", new IntegerItem("#TlsFg", etel.getFg(), 0));
                etelVars.put("#TlsId", new IntegerItem("#TlsId", etel.getTlsId(), 0));
                etelVars.put("#TlsJob", new IntegerItem("#TlsJob", etel.getJob(), 0));
                
                etelVars.put("#ActualTime", new GregorianItem("#ActualTime", actualTime, 0));
                if (null != rcvTimeBase) {
                        etelVars.put("#RcvTimeBase", new GregorianItem("#RcvTimeBase", rcvTimeBase, 0));
                        subsequent = true;
                }
                ETelMeta etelMeta = new ETelMeta(etel.getFg(), etel.getTlsId(), etel.getJob(), etel.getDeblocks().size());
                for(TlsDeBlock deblock : etel.getDeblocks()) {
                	header = handleDeBlock( tlsTele, etel, unknownDeBlock, node, dataObjects, fg, id, subsequent, header, cnt, etelVars, etelMeta,
                                        deblock, deliverBadBlocks );
                	cnt++;
                }
                
                if (header != null) {
                	// add data of header deblock to all deblocks
                	for(DataObject dataObject : dataObjects) {
                		for(DataItem item : header.getItems()) {
                			dataObject.getItems().add(item);
                		}
                	}
                } else {
                        if (null != rcvTimeBase) {
                                LOGGER.error( "Subsequent delivered data without timestamp: telegram skipped" );
                                throw new IllegalStateException( "Subsequent delivered data without timestamp: telegram skipped" );
                        }
                }
        }

        private DataObject handleDeBlock( final TlsTele tlsTele, final TlsETel etel, DeBlockDefinition unknownDeBlock, int node, List<DataObject> dataObjects, int fg,
                        int id, boolean subsequent, DataObject header, int cnt, Map<String, DataItem> etelVars, ETelMeta etelMeta, TlsDeBlock deblock,
                        boolean deliverBadBlocks ) {
                boolean unknownType = false;
                
                int de = deblock.getDeNr();
                int typ = deblock.getDeTyp();
                etelVars.put( "#De", new IntegerItem( "#De", de, 0 ) );
                etelVars.put( "#DeLen", new IntegerItem( "#DeLen", deblock.getSize(), 0 ) );
                
                String address = addressConverter.convert( node, fg, de );
                DeBlockDefinition deBlockDefinition = getRulesForDeBlock( node, fg, id, typ );
                if ( deBlockDefinition == null ) {
                        logBadDeBlock( etel, deblock, node, "Unknown Fg/Id/Typ: " + fg + "/" + id + "/" + typ );
                        if ( !deliverBadBlocks ) {
                                return header;
                        }
                        unknownType = true;
                        deBlockDefinition = unknownDeBlock;
                }
                
                DeMeta deMeta = new DeMeta( de, typ, cnt, deblock.getContent() );

                DataObject dataObject = new DataObject( deBlockDefinition.getName(), address );
                dataObject.setEtelMeta( etelMeta );
                dataObject.setDeMeta( deMeta );
                dataObject.setEtel( etel );
                dataObject.setTele( tlsTele );
                dataObject.setSubsequent( subsequent );

                int ofs = 0;
                boolean ok = true;
                for ( GetterRule rule : deBlockDefinition.getGetterRules() ) {
                        DataItem item = null;
                        try {
                                item = rule.get( deblock.getContent(), ofs, etelVars );
                                if (isSubsequentItem( item, dataObject, subsequent )) {
                                        continue;
                                }
                        }
                        catch ( IllegalArgumentException | TlsReceiverException e ) {
                                logBadDeBlock( etel, deblock, node, e.getMessage() );
                                ok = false;
                                if ( !deliverBadBlocks ) {
                                        return header;
                                }
                                break;
                        }
                        ofs += item.getConsumedSize();
                        addItemByType( etelVars, dataObject, item );
                }
                if ( !ok ) {
                        addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorGetterRules-" );
                        return header;
                }
                if ( !unknownType && ( ofs < deblock.getSize() - 2 ) ) {
                        logBadDeBlock( etel, deblock, node, "DE Block too long" );
                        addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorDEBlock2long-" );
                        return header;
                }
                if ( deBlockDefinition.isHeader() ) {
                        if ( cnt != 0 ) {
                                logBadDeBlock( etel, deblock, node, "Header DE Block is not the first one in ETel" );
                                addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorHeaderNotFirst-" );
                                return header;
                        }
                        if ( deblock.getDeNr() != 255 ) {
                                logBadDeBlock( etel, deblock, node, "DE number of Header DE Block is not 255" );
                                addOnlyIfWanted( dataObjects, deliverBadBlocks, dataObject, "-errorHeaderNot#255-" );
                                return header;
                        }
                        header = dataObject;
                } else {
                        dataObjects.add( dataObject );
                }
                return header;
        }

        /**
         * @param node If definitions are location specific we have to test against this node 
         * @param fg
         * @param id
         * @param typ
         * @return DeBlockDefinition The set of rules to be applied to an DE-Block
         */
        private DeBlockDefinition getRulesForDeBlock( int node, int fg, int id, int typ ) {
                List<DeBlockDefinition> deBlockDefinitions = transformationRules.getDefinition( fg, id, typ );
                if (null == deBlockDefinitions || 0 == deBlockDefinitions.size()) {
                        return null;
                }
                if (1 == deBlockDefinitions.size()) {
                        return deBlockDefinitions.get( 0 );
                } else {
                        for (DeBlockDefinition deBlockDefinitionLoop : deBlockDefinitions) {
                                Set<Integer> locs = deBlockDefinitionLoop.getSpecialLocations();
                                boolean match = false;
                                if (locs.contains( node )) {
                                        match = true;
                                }
                                if (null != deBlockDefinitionLoop.getSpecialLocationsExcludedName()) {
                                        if (match) {
                                                continue;
                                        } else {
                                                return deBlockDefinitionLoop;
                                        }
                                }
                                if (match && null != deBlockDefinitionLoop.getSpecialLocationsName()) {
                                        return deBlockDefinitionLoop;
                                }
                        }
                }
                return null;
        }

        boolean isSubsequentItem( DataItem item, DataObject dataObject, boolean calledAsubsequent ) throws TlsReceiverException {
                if (item.getName().equalsIgnoreCase( "#Puffer" )) {
                        if (item.getType() == DataItemType.INTEGER && 0 == item.getConsumedSize()) {
                                if (0 != item.getAsLong().intValue()) {
                                        dataObject.setSubsequent( true );
                                } else {
                                        dataObject.setSubsequent( false || calledAsubsequent );
                                }
                        } else {
                                throw new TlsReceiverException( "#Puffer has illegal properties: either type or consumed size" );
                        }
                        return true;
                } else {
                        return false;
                }
        }

        private void addItemByType( Map<String, DataItem> etelVars, DataObject dataObject, DataItem item ) {
                if ( item.getType() == DataItemType.LIST ) {
                        for ( DataItem itm : item.getAsItemList() ) {
                                addItem( itm, etelVars, dataObject );
                        }
                } else {
                        addItem( item, etelVars, dataObject );
                }
        }

        private void addOnlyIfWanted( List<DataObject> dataObjects, boolean deliverBadBlocks, DataObject dataObject, String name ) {
                if ( deliverBadBlocks ) {
                        dataObject.setName( name );
                        dataObjects.add( dataObject );
                }
        }

	private void addItem(DataItem item, Map<String, DataItem> etelVars, DataObject dataObject) {
		if (item.getName().startsWith("$")) {
			// add to etel vars
			etelVars.put(item.getName(), item);
		} else {
			// add to data object
			dataObject.getItems().add(item);
		}
	}

	private void logBadDeBlock(TlsETel etel, TlsDeBlock deblock, int logAddress, String message) {
		LOGGER.error("Bad DE Block: " + message + ": Node: " + logAddress/256 + "-" + logAddress%256 + 
		", Fg " + etel.getFg() + ", Id " + etel.getTlsId() + ", Typ " + deblock.getDeTyp() + ", DE Block: " + Arrays.toString(deblock.getBytes())); 
	}

}
