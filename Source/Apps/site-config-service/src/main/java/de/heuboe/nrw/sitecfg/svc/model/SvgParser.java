package de.heuboe.nrw.sitecfg.svc.model;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.VisitorSupport;
import org.dom4j.io.SAXReader;

import de.heuboe.sitecfg.grpc.data.CfgRoadSegStyle;
import de.heuboe.sitecfg.grpc.data.Util;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("serial")
public class SvgParser {
	// -----------------------------------------------------------------------------------------------------------------------------
	public static final String		SYMBOLS_SUFFIX	= "-SymbolDefs";
	public static final String		NS_SVG			= "xmlns='http://www.w3.org/2000/svg'";
	public static final String		NS_XLINK		= "xmlns:xlink='http://www.w3.org/1999/xlink'";
	public static final String		NO_SVG			= " xmlns=\"http://www.w3.org/2000/svg\"";
	public static final String		NO_XLINK		= " xmlns:xlink=\"http://www.w3.org/1999/xlink\"";
	public static final String[]	REF_TOKENS		= new String[] { "xlink:href=\"#\"", "url(#)" };
	public static final Dimension	ROADSEG_AREA	= new Dimension(28, 60);
	// -----------------------------------------------------------------------------------------------------------------------------
	private SAXReader				reader			= new SAXReader();
	private Document				doc;
	private String					svgId;
	private boolean					hasSymbolDefs	= false;
	private Map<String, Node>		idDefNodeMap	= new TreeMap<>();
	// -----------------------------------------------------------------------------------------------------------------------------
	public SvgParser( String svgId, InputStream is ) throws DocumentException {
		this.svgId = svgId;
		doc = reader.read(is);
		if( hasSymbolDefs = svgId.endsWith(SYMBOLS_SUFFIX) ) {
			selectAbsNodes("defs", "g", "radialGradient").forEach(node -> idDefNodeMap.put(node.valueOf("@id"), node));
			selectAbsNodes("defs", "g", "pattern").forEach(node -> idDefNodeMap.put(node.valueOf("@id"), node));
			selectAbsNodes("defs", "g", "symbol").forEach(node -> idDefNodeMap.put(node.valueOf("@id"), node));
			selectAbsNodes("g", "defs", "g", "pattern").forEach(node -> idDefNodeMap.put(node.valueOf("@id"), node));
			selectAbsNodes("g", "defs", "g", "mask").forEach(node -> idDefNodeMap.put(node.valueOf("@id"), node));
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public String svgId() {
		return svgId;
	}
	public boolean hasSymbolDefs() {
		return hasSymbolDefs;
	}
	public boolean hasSymbol( String symbolId ) {
		return idDefNodeMap.containsKey(symbolId);
	}
	public Node getSymbolNode( String symbolId ) {
		return idDefNodeMap.get(symbolId);
	}
//	public boolean hasSymbol( String symbolId ) {
//		return getSymbolNode(symbolId) != null;
//	}
//	public Node getSymbolNode( String symbolId ) {
//		if( hasSymbolDefs ) {
//			List<Node> symbolNodes = doc.getRootElement().selectNodes(getXPathForSymbols(symbolId));
//			if( symbolNodes != null && !symbolNodes.isEmpty() ) {
//				return symbolNodes.get(0);
//			}
//		}
//		return null;
//	}
	// -----------------------------------------------------------------------------------------------------------------------------
	// add 'gant' group style classes - without style data (if there is any)
	// the AQ will be rendered differently in its info view and the state view
	public String getAQasSVG( String aqID ) {
		StringBuilder sb = new StringBuilder();
		int yPos = 0, vGap = 2;
		List<Node> styleNodes = doc.getRootElement().selectNodes(getXPathForGantryStyles());
		if( styleNodes != null && !styleNodes.isEmpty() ) {
			sb.append(styleNodes.get(0).asXML()).append("\n");
		}
		List<Node> gantryNodes = doc.getRootElement().selectNodes(getXPathForGantries(aqID));
		if( gantryNodes != null ) {
			for( Node gantryNode : gantryNodes ) {
				translateNode(gantryNode, 0, yPos);
				sb.append(gantryNode.asXML()).append("\n");
				yPos += calcNodeSize(gantryNode).height + vGap;
			}
			return String.format("<svg %s height='%d'>%n%s</svg>", NS_SVG, yPos - vGap, sb.toString());
		} else {
			return String.format("<svg %s></svg>", NS_SVG);
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	// does not work yet
	public String createRoadSegSvg( List<CfgRoadSegStyle> descs, int dx ) {
		SVG svg = new SVG();
		for( int i = 0; i < descs.size(); ++i ) {
			createRoadSegSvg(descs.get(i), svg, dx + i * (ROADSEG_AREA.width + dx), 0);
		}
		return svg.print(dx + descs.size() * (ROADSEG_AREA.width + dx), 0);
	}
	public String createRoadSegSvg( CfgRoadSegStyle desc ) {
		return createRoadSegSvg(desc, new SVG(), 0, 0);
	}
	public String createRoadSegSvg( CfgRoadSegStyle desc, SVG svg, int px, int py ) {
		Rectangle r = new Rectangle(new Point(px, py), ROADSEG_AREA);
		if( desc.hasAreaColor() ) {
			StringBuilder styles = new StringBuilder();
			if( desc.hasAreaPattern() ) {
				String mskId = desc.getAreaPattern();
				String patId = mskId.replaceAll("\\.", "");
				if( idDefNodeMap.containsKey(patId) && idDefNodeMap.containsKey(mskId) ) {
					svg.addRef(mskId, n -> {});
					svg.addRef(patId, n -> {});
					styles.append(String.format(" mask='url(#%s)'", mskId));
				}
			}
			styles.append(String.format(" fill='%s'", desc.getAreaColor()));
			if( desc.hasAreaOpacity() ) {
				styles.append(String.format(" opacity='%s'", String.valueOf(desc.getAreaOpacity())));
			}
			int wRect = (desc.isMeasure() ? 18 : 12);
			Rectangle box = new Rectangle(r.x + (r.width - wRect) / 2, r.y, wRect, r.height);
			svg.addRect(styles.toString(), box);
			desc.setAreaStyle(styles.toString());
		}
		if( desc.hasIcon() ) {
			int w = desc.getIconWidth ();
			int h = desc.getIconHeight();
			int x = r.x + (r.width  - w) / 2;
			int y = r.y + (r.height - h) / 2;
			createSymbolShapes(svg, desc.getIconId(), x, y);
			desc.setIconSvg(createSymbolShapes(new SVG(), desc.getIconId(), 0, 0).print(w, h));
		}
		String areaSvg = svg.print(r);
		desc.setAreaSvg(areaSvg);
		return areaSvg;
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public String createSymbolSvg( String symbolId ) {
		SVG svg = new SVG();
		int[] wh = Util.getSymbolSize(symbolId);
		if( wh[0] > 0 && wh[1] > 0 ) {
			return createSymbolShapes(svg, symbolId, 0, 0).print(wh[0], wh[1]); 
		}
		return svg.print("");
	}
	public SVG createSymbolShapes( SVG svg, String symbolId, int x, int y ) {
		if( hasSymbol(symbolId) ) {
			Node symbolNode = getSymbolNode(symbolId);
			selectRelNodes(symbolNode.getParent(), "style").forEach(styleNode -> svg.addFirst(styleNode));
			svg.addDef(symbolId, n -> Arrays.asList(REF_TOKENS).forEach(t -> addRefNodes(svg, symbolNode, t)));
			svg.addShape(String.format("<use xlink:href='#%s' x='%d' y='%d'/>", symbolId, x, y));
		}
		return svg;
	}
	private void addRefNodes( SVG svg, Node curNode, String refToken ) {
		int len = refToken.length() - 1;
		char quote = refToken.charAt(len);
		String token = refToken.substring(0, len);
		String xml = curNode.asXML();
		for( int pos = xml.indexOf(token, 0) + len; pos >= len; xml.indexOf(token, pos) ) {
			int end = xml.indexOf(quote, pos);
			String refId = xml.substring(pos, end);
			pos = xml.indexOf(token, end + 1) + len;
			svg.addRef(refId, refNode -> Arrays.asList(REF_TOKENS).forEach(t -> addRefNodes(svg, refNode, t)));
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	class SVG extends LinkedList<Node> {
		Set<String> defIdSet = new HashSet<>();
		List<String> shapes = new ArrayList<>();
		// -----------------------------------------------------------------------------------------------------------------------------
		void addRef( String defId, Consumer<Node> refNodeHandler ) {
			addDef(defId, this::addFirst, refNodeHandler);
		}
		void addDef( String defId, Consumer<Node> refNodeHandler ) {
			addDef(defId, this::addLast, refNodeHandler);
		}
		void addDef( String defId, Consumer<Node> adder, Consumer<Node> handler ) {
			if(!defIdSet.contains(defId) && idDefNodeMap.containsKey(defId) ) {
				defIdSet.add(defId);
				Node refNode = idDefNodeMap.get(defId);
				adder.accept(refNode);
				handler.accept(refNode);
			}
		}
		void addShape( String shape ) {
			shapes.add(shape);
		}
		void addRect( String styles, Rectangle r ) {
			shapes.add(String.format("<rect %s x='%d' y='%d' %s/>", styles, r.x, r.y, createSizeProps(r.width, r.height)));
		}
		// -----------------------------------------------------------------------------------------------------------------------------
		String print( Dimension d ) {
			return print(createSizeProps(d.width, d.height));
		}
		String print( Rectangle r ) {
			return print(createSizeProps(r.x + r.width, r.y + r.height));
		}
		String print( int w, int h ) {
			return print(createSizeProps(w, h));
		}
		String print( String sizeProps ) {
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("<svg %s %s %s>%n", NS_SVG, NS_XLINK, sizeProps));
			if(!isEmpty() ) {
				sb.append("\t<defs>\n\t\t<g>\n\t\t\t<style>pattern{ fill: white; }</style>\n");
				forEach(node -> sb.append("\t\t\t").append(node.asXML()).append("\n"));
				sb.append("\t\t</g>\n\t</defs>\n");
			}
			shapes.forEach(shape -> sb.append("\t").append(shape).append("\n"));
			sb.append("</svg>");
			return sb.toString().replaceAll(NO_SVG, "").replaceAll(NO_XLINK, "");
		}
		private String createSizeProps( int w, int h ) {
			return String.format("width='%d' height='%d'", w, h);
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	// at least try to calc svg height only considering rect shapes - 
	// wont be correct for WWW with round corner path shapes but does not matter anyway
	private Dimension calcNodeSize( Node gantryNode ) {
		Dimension dim = new Dimension(0, 45);
		for( Node rectNode : selectRelNodes(gantryNode, "rect") ) {
			try {
				int x = Float.valueOf(rectNode.valueOf("@x")).intValue();
				int y = Float.valueOf(rectNode.valueOf("@y")).intValue();
				int w = Float.valueOf(rectNode.valueOf("@width")).intValue();
				int h = Float.valueOf(rectNode.valueOf("@height")).intValue();
				dim.width = Math.max(dim.width, x + w);
				dim.height = Math.max(dim.height, y + h);
			} catch( Exception e ) {
				log.debug("failed to read coords from rect shape: " + rectNode.asXML());
			}
		}
		return dim;
	}
	private Node translateNode( Node node, int x, int y ) {
		node.accept(new VisitorSupport() {
			@Override
			public void visit( Attribute nodeAttribute ) {
				if( "transform".equals(nodeAttribute.getName()) ) {
					nodeAttribute.setValue("translate(" + x + " " + y + ")");
				}
			}
		});
		return node;
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	private String getXPathForGantries( String aqID ) {
		return getXPathForGantryGroup() + selectGroup(aqID);
	}
	private String getXPathForGantryStyles() {
		return getXPathForGantryGroup() + selectNodes("style");
	}
	private String getXPathForGantryGroup() {
		return getXPathForGroup("root", "gant");
	}
	private String getXPathForGroup( String... ids ) {
		StringBuilder sb = new StringBuilder("/svg");
		for( String id : ids ) {
			sb.append(selectGroup(id));
		}
		return sb.toString();
	}
	// -----------------------------------------------------------------------------------------------------------------------------
//	private String getXPathForSymbols( String symbolId ) {
//		StringBuilder sb = new StringBuilder("/svg");
//		sb.append(selectNodes("defs"));
//		sb.append(selectNodes("g"));
//		sb.append(selectNodes("symbol", symbolId));
//		return sb.toString();
//	}
	// -----------------------------------------------------------------------------------------------------------------------------
	private List<Node> selectAbsNodes( String... names ) {
		return doc.getRootElement().selectNodes(getXPathForNames("/svg", names));
	}
	private List<Node> selectRelNodes( Node node, String... names ) {
		return node.selectNodes(getXPathForNames(".", names));
	}
//	private Node selectRelNode( Node node, String... names ) {
//		return node.selectSingleNode(getXPathForNames(".", names));
//	}
	private String getXPathForNames( String prefix, String... names ) {
		StringBuilder sb = new StringBuilder(prefix);
		for( String name : names ) {
			sb.append(selectNodes(name));
		}
		return sb.toString();
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	private String selectGroup( String id ) {
		return selectNodes("g", id);
	}
	private String selectNodes( String name ) {
		return nodeSelector(nameSelector(name));
	}
	private String selectNodes( String name, String id ) {
		return nodeSelector(String.join(" and ", nameSelector(name), idSelector(id)));
	}
	private String nodeSelector( String cond ) {
		return String.format("/*[%s]", cond);
	}
	private String nameSelector( String s ) {
		return String.format("name()='%s'", s);
	}
	private String idSelector( String s ) {
		return s != null && !s.isEmpty() ? String.format("@id='%s'", s) : "";
	}
	// -----------------------------------------------------------------------------------------------------------------------------
}
