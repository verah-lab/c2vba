package de.heuboe.tls.iface.routing.csv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import de.heuboe.data.DataReader;
import de.heuboe.data.DataStore;
import de.heuboe.data.csv.CsvDataStore;
import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.IfaceRouting;
import de.heuboe.tls.iface.iface.RoutingEntry;

public class RoutingCsv implements IfaceRouting {

	private static final Logger LOGGER = Logger.getLogger(RoutingCsv.class);
	
	private List<RoutingEntry> routingList = new ArrayList<>();
	
	public RoutingCsv(String filename) {
		Properties csvProps = new Properties();
		if (filename.toLowerCase().endsWith(".txt")) {
			csvProps.setProperty(CsvDataStore.SEPARATOR_KEY, "\t");
		}
		if (filename.toLowerCase().endsWith(".csv")) {
			csvProps.setProperty(CsvDataStore.SEPARATOR_KEY, ";");
		}
		DataStore store = new CsvDataStore(null,filename,csvProps);
		DataReader reader = store.getReader();
		if (reader == null) {
			throw new RuntimeException("cannot read config file for data definitions");
		}
		try {
			while(reader.hasNext()) {
				de.heuboe.data.Data record = reader.next();
				RoutingEntry r = getRoutingEntry(record);
				if (r != null) {
					routingList.add(r);
				}
			}			
		} finally {
			reader.close();
		}
		LOGGER.info("RoutingCSV: " + routingList.size() + " routing entries loaded");
	}
	
	private RoutingEntry getRoutingEntry(de.heuboe.data.Data record) {
		int key;
		try {
			key = record.getMember("KEY").getAsInt();
		} catch(NumberFormatException e) {
			return null;
		}
		if (key == 0) {
			return null;
		}
		int node;
		try {
			node = record.getMember("NODE").getAsInt();
		} catch(NumberFormatException e) {
			return null;
		}
		if (node == 0) {
			return null;
		}
		List<Integer> route = new ArrayList<>();
		for(int i=0; i<14; ++i) {
			int osi2=0;
			try {
				osi2 = record.getMember("OSI2-"+Integer.toString(i+1)).getAsInt();
			} catch(NumberFormatException e) {
				// ignore
			}
			if (osi2 == 0) {
				break;
			}
			route.add(osi2);
		}
		if (!route.isEmpty()) {
			byte[] routing = new byte[route.size()];
			for(int i=0; i<route.size(); ++i) {
				routing[i] = getByte(route.get(i));
			}
			return new RoutingEntry(node, routing, key);
		}
		return null;
	}
	
	private byte getByte(int b) {
		return (byte) b;
	}

	@Override
	public Integer getNodeNumber(byte[] routing) {
		for(RoutingEntry r : routingList) {
			if (r.equalsRouting(routing)) {
				return r.getNode();
			}
		}
		return null;
	}

	@Override
	public byte[] getRouting(int node) {
		for(RoutingEntry r : routingList) {
			if (r.equalsNode(node)) {
				return r.getRouting();
			}
		}
		return null;
	}

	@Override
	public Collection<RoutingEntry> getRoutingTable() {
		return Collections.unmodifiableList(routingList);
	}

}
