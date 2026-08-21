package de.heuboe.tls.iface.routing.ddp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import de.heuboe.ddp.Connection;
import de.heuboe.ddp.Container;
import de.heuboe.ddp.DDPException;
import de.heuboe.ddp.Dataset;
import de.heuboe.log.Logger;
import de.heuboe.tls.iface.iface.IfaceRouting;
import de.heuboe.tls.iface.iface.RoutingEntry;

public class RoutingDdp implements IfaceRouting {

	private static final Logger LOGGER = Logger.getLogger(RoutingDdp.class);

	private List<RoutingEntry> routingList = new ArrayList<>();

	private Map<Integer, Cable> cableMap;
	private List<Conpoint> conpointList;
	private Map<Integer, Device> deviceMap;
	
	public RoutingDdp(Connection connection, String thisDevice) throws Exception {
		try {
		cableMap = readCables(connection);
		conpointList = readConpoints(connection);
		deviceMap = readDevices(connection);

		Node myNode = getMyNode(thisDevice, deviceMap);
		List<Node> nodeList = new ArrayList<>();
		nodeList.add(myNode);
		
		ListIterator<Node> it = nodeList.listIterator();
		while(it.hasNext()) {
			Node n = it.next();
			List<Node> neighbors = getNeighbors(n, nodeList);
			for(Node neighbor : neighbors) {
				it.add(neighbor);
			}
		}
		
		for(Node node : nodeList) {
			int size = node.routing[0];
			if (size == 0) {
				continue;
			}
			byte[] routing = new byte[size];
			for(int i=0; i<size; ++i) {
				routing[i] = (byte) node.routing[i+1];
			}
			RoutingEntry r = new RoutingEntry(node.nodenum, routing, node.ifacekey);
			LOGGER.debug("adding routing entry: " + r);
			routingList.add(r);
		}
		LOGGER.info("creation of routing table from ddp succeeded");
		} catch(Exception e) {
			LOGGER.fatal("creation of routing table from ddp failed: " + e);
			throw e;
		}
	}

	private List<Node> getNeighbors(Node node, List<Node> nodeList) {
		List<Node> newNodes = new ArrayList<>();
		List<Conpoint> conpoints = getConpointsByDeviceId(node.id);
		for(Conpoint conpoint : conpoints) {
			Cable cable = getCable(conpoint);
			List<Conpoint> nextConpoints = getConpoints(cable);
			for(Conpoint nc : nextConpoints) {
				Device device = getDevice(nc);
				if (!isInNodeList(device, nodeList)) {
					Node n = createNeighborNode(node, device, conpoint, nc);
					newNodes.add(n);
				}
			}
		}
		return newNodes;
	}

	private Node createNeighborNode(Node previousNode, Device device, Conpoint previousConpoint, Conpoint nextConpoint) {
		Node n = new Node();
		n.id = device.id;
		if (previousNode.routing[0] == 0) {
			n.myOutgoingPort = nextConpoint.osi2;
			n.myOutgoingPartner = previousConpoint.osi2;
			n.ifacekey = previousConpoint.ifacekey;
		} else {
			n.myOutgoingPort = previousNode.myOutgoingPort;
			n.myOutgoingPartner = previousNode.myOutgoingPartner;
			n.ifacekey = previousNode.ifacekey;
		}
		n.nodenum = device.nodenum;
		for(int i=0; i<n.routing[0]; ++i) {
		 	n.routing[i+1] = previousNode.routing[i+1];
		}
		int i = n.routing[0];
		n.routing[i+1] = previousConpoint.osi2;
		n.routing[i+2] = nextConpoint.osi2;
		n.routing[0] = i+2;
		return n;
	}

	private boolean isInNodeList(Device device, List<Node> nodeList) {
		for(Node node : nodeList) {
			if (node.id == device.id) {
				return true;
			}
		}
		return false;
	}

	private Device getDevice(Conpoint conpoint) {
		for(Device device : deviceMap.values()) {
			if (device.id == conpoint.refDevices) {
				return device;
			}
		}
		throw new IllegalArgumentException("Cannot find device for conpoint with id " + conpoint.id + " and curnum " + conpoint.curnum );
	}

	private List<Conpoint> getConpoints(Cable cable) {
		List<Conpoint> conpoints = new ArrayList<>();
		for(Conpoint conpoint : conpointList) {
			if (conpoint.id == cable.refConpoint) {
				conpoints.add(conpoint);
			}
		}
		return conpoints;
	}

	private Cable getCable(Conpoint conpoint) {
		for(Cable cable : cableMap.values()) {
			if (cable.refConpoint == conpoint.id) {
				return cable;
			}
		}
		throw new IllegalArgumentException("Cannot find cable for conpoint with id " + conpoint.id + " and curnum " + conpoint.curnum );
	}

	private List<Conpoint> getConpointsByDeviceId(int id) {
		List<Conpoint> conpoints = new ArrayList<>();
		for(Conpoint c : conpointList) {
			if (c.refDevices == id) {
				conpoints.add(c);				
			}
		}
		return conpoints;
	}

	private Node getMyNode(String thisDevice, Map<Integer, Device> devices) {
		for(Device d : devices.values()) {
			if (d.name.equals(thisDevice)) {
				Node n = new Node();
				n.id = d.id;
				n.myOutgoingPort = 0;
				n.myOutgoingPartner = 0;
				n.nodenum = d.nodenum;
				n.routing[0] = 0;
				return n;
			}
		}
		throw new IllegalArgumentException("Could not find My Device: " + thisDevice);
	}

	private Map<Integer, Cable> readCables(Connection connection) throws DDPException {
		Map<Integer, Cable> cableMap = new TreeMap<>();
		Container[] containers = connection.read("cable");
		for ( Container container : containers ) {
			for (Dataset ds : container ) {
				Cable cable = new Cable();
				cable.id = ds.value("cabid").getInt();
				cable.refConpoint = ds.value("conpoint").getInt();
				cableMap.put(cable.id, cable);
			}
		}
		return cableMap;
	}

	private List<Conpoint> readConpoints(Connection connection) throws DDPException {
		List<Conpoint> conpointMap = new ArrayList<>();
		Container[] containers = connection.read("conpoint");
		for ( Container container : containers ) {
			for (Dataset ds : container ) {
				Conpoint conpoint = new Conpoint();
				conpoint.id = ds.value("conpid").getInt();
				conpoint.curnum = ds.value("curnum").getInt();
				conpoint.osi2 = ds.value("OSI2").getInt();
				conpoint.ifacekey = ds.value("qin").getInt();
				conpoint.refDevices = ds.value("ondev").getInt();
				conpointMap.add(conpoint);
			}
		}
		return conpointMap;
	}

	private Map<Integer, Device> readDevices(Connection connection) throws DDPException {
		Map<Integer, Device> deviceMap = new TreeMap<>();
		Container[] containers = connection.read("devices");
		for ( Container container : containers ) {
			for (Dataset ds : container ) {
				Device device = new Device();
				device.id = ds.value("DevId").getInt();
				device.name = ds.value("DevName").getString();
				device.nodenum = ds.value("OSI7").getInt();
				deviceMap.put(device.id, device);
			}
		}
		return deviceMap;
	}

	private class Cable {
		int id;
		int   refConpoint;
	};

	private class Conpoint {
		int id;
		int curnum;
		int refDevices;
		int osi2;
		int ifacekey;
	};

	private class Device {

		int id;
		String      name;
		int nodenum;
	};

	private class Node {
		int id;              			// Device-Id
		int nodenum;              		// Knotennummer
		int[] routing = new int[15]; 	// Byte 0 enthaelt Laenge
		int myOutgoingPort;        		// eigene Port-Adresse zu diesem Ziel
		int myOutgoingPartner;     		// OSI2-Adresse des Partners zu diesem Ziel
		int ifacekey;
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
