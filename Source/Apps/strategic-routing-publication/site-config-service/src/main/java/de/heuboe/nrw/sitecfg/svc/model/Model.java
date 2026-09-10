package de.heuboe.nrw.sitecfg.svc.model;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import de.heuboe.nrw.guisvc.cfgTypes.ObjectType;
import de.heuboe.sitecfg.grpc.CfgMultiFileItems;
import de.heuboe.sitecfg.grpc.CfgType;
import de.heuboe.sitecfg.grpc.RoadSegKey;
import de.heuboe.sitecfg.grpc.RoadSegKeys;
import de.heuboe.sitecfg.grpc.ViewAggrRequest;
import de.heuboe.sitecfg.grpc.XmlProtoConverter;
import de.heuboe.sitecfg.grpc.XmlProtoConverter.CnvBabSeg;
import de.heuboe.sitecfg.grpc.XmlProtoConverter.CnvLocSegs;
import de.heuboe.sitecfg.grpc.data.CfgRoadSegKey;
import de.heuboe.sitecfg.grpc.data.CfgSiteFonts;
import de.heuboe.sitecfg.grpc.data.CfgSiteItems;
import de.heuboe.sitesconfig.CfgConfig;
import de.heuboe.sitesconfig.CfgDevice;
import de.heuboe.sitesconfig.CfgDkSet;
import de.heuboe.sitesconfig.CfgDkSets;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgQLoc;
import de.heuboe.sitesconfig.CfgQs;
import de.heuboe.sitesconfig.CfgSite;
import de.heuboe.sitesconfig.CfgSiteDesc;
import de.heuboe.sitesconfig.CfgView;
import de.heuboe.sitesconfig.CfgViewQ;
import de.heuboe.sitesconfig.CfgViewQs;
import de.heuboe.sitesconfig.CfgViewType;
import de.heuboe.sitesconfig.Direction;
import de.heuboe.sitesconfig.QType;
import de.heuboe.zst.model.IObjHandler;
import lombok.extern.slf4j.Slf4j;
import multimap.MultiMap.HSet;

@Slf4j
@SuppressWarnings("serial")
public class Model extends TypeIdObjMap {
	// -----------------------------------------------------------------------------------------------------------------------------
	enum ViewCategory { HAS_SVG, CAN_HAVE_SEGS }
	// -----------------------------------------------------------------------------------------------------------------------------
	public static final String						TEMPLATE_NAME	= "siteCfgSvcTmpl";
	public static final String						STRAY_SITE_ID	= "Stray_Qs_Site";
	// -----------------------------------------------------------------------------------------------------------------------------
	protected final String							repRoot;
	protected final Set<String>						qsOnlyXml		= new HashSet<>();
	protected final HSet<String, String>			qDevsOnlyXml	= new HSet<>();
	protected final HSet<String, String>			qDevsOnlyCfg	= new HSet<>();
	protected final IdMap<String>					valueClassIdMap	= new IdMap<>();
	protected final HSet<ViewCategory, CfgViewType>	viewCategory	= new HSet<>();
	protected final CfgMultiFileItems				multiFileItems	= new CfgMultiFileItems();
	protected final XmlProtoConverter				cnv				= new XmlProtoConverter();
	protected final CnvBabSeg						cnvBabSeg		= cnv.babSeg();
	protected final CnvLocSegs						cnvLogSegs		= cnv.locSegs();
	// -----------------------------------------------------------------------------------------------------------------------------
	public Model( String repRoot ) {
		this.repRoot = repRoot;
		viewCategory.add(ViewCategory.HAS_SVG      , CfgViewType.ZST, CfgViewType.MAN, CfgViewType.SPR, CfgViewType.GRM, CfgViewType.GRZ);
		viewCategory.add(ViewCategory.CAN_HAVE_SEGS, CfgViewType.ZST, CfgViewType.MAN, CfgViewType.SPR, CfgViewType.GRM, CfgViewType.GRZ, CfgViewType.PRF);
		log.info("Model.ctor");
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public Model create() {
		loadCfg();
		log.info(print());
		collectMultiFileItems();
		log.info(multiFileItems.print(0, "Catalogue items in multiple jar files:", "%-30s in jars[%s]"));
		return this;
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	protected void loadCfg() {}
	protected void loadXml( String jarName, CfgConfig cfg ) {}
	protected boolean isTemplate( String jarName ) {
		return TEMPLATE_NAME.equals(jarName);
	}
	protected boolean hasSvg( CfgViewType viewType ) {
		return viewCategory.all(ViewCategory.HAS_SVG).contains(viewType);
	}
	protected boolean canHaveSegs( CfgViewType viewType ) {
		return viewCategory.all(ViewCategory.CAN_HAVE_SEGS).contains(viewType);
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	@Override
	protected <T> String idObj( CfgType type, T obj ) {
		return type.id(obj);
	}
	@Override
	protected <T> T sameId( CfgType type, T fstObj, T curObj ) {
		return fstObj;
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public void save( String relPath, String s, Charset charset ) {
		String filePath = String.format("%s/%s", repRoot, relPath);
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			PrintWriter f = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos, charset)));
			f.write(s);
			f.close();
		} catch( Exception e ) {
			log.error(String.format("save(%s) failed: '%s'", filePath, e.toString()));
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	/**
	 * Creates a virtual site with all devices not belonging to physical sites. Should be called after creation of a new instance. Then all
	 * devices belonging to the physical sites are known.
	 */
	public void afterCreate( Collection<IObjHandler> eqHdls, Collection<IObjHandler> aqHdls ) {
		if( has(CfgType.JAR, TEMPLATE_NAME) ) {
			CfgConfig cfg = get(CfgType.JAR, TEMPLATE_NAME);
			if( isComplete(cfg) ) {
				CfgSiteDesc siteDesc = cfg.getSite().get(0).getDesc();
				siteDesc.setId(STRAY_SITE_ID);
				siteDesc.setName("-Unzugeordnete Querschnitte"); // - at beginning indicates this site has to ignored by gui
				CfgView view = cfg.getSite().get(0).getViews().getView().get(0);
				if( null == cfg.getEqs() ) {
					cfg.setEqs(new CfgQs());
				}
				if( null == cfg.getAqs() ) {
					cfg.setAqs(new CfgQs());
				}
				if( null == view.getEqs() ) {
					view.setEqs(new CfgViewQs());
				}
				if( null == view.getAqs() ) {
					view.setAqs(new CfgViewQs());
				}
				List<CfgDkSet> dkSetList = new ArrayList<>();
				dkSetList.add(new CfgDkSet(Arrays.asList(""), "dummy"));
				CfgDkSets dkSets = new CfgDkSets().withDkSet(dkSetList);
				cfg.setDkSets(dkSets);
				Set<String> eqXmlIds = idSet(CfgType.EQ);
				Set<String> aqXmlIds = idSet(CfgType.AQ);
				log.info("buildStrayQs LVE");
				createObjects(cfg.getEqs(), view.getEqs(), QType.EQ, ObjectType.LVE_SENSOR, eqHdls, eqXmlIds);
				log.info("buildStrayQs WVZ");
				createObjects(cfg.getAqs(), view.getAqs(), QType.AQ, ObjectType.WZG, aqHdls, aqXmlIds);
				log.info("stray finished");
				loadXml(TEMPLATE_NAME, cfg);
			} else {
				log.debug("Invalid template configuration");
			}
		}
	}
	private boolean isComplete( CfgConfig cfg ) {
		List<CfgSite> sites = cfg.getSite();
		return sites != null && !sites.isEmpty() && sites.get(0) != null && sites.get(0).getViews() != null && sites.get(0).getViews().getView() != null
						&& !sites.get(0).getViews().getView().isEmpty();
	}
	private void createObjects( CfgQs dictQs, CfgViewQs viewQs, QType qType, ObjectType objType, Collection<IObjHandler> qHdls, Set<String> qXmlIds ) {
		for( IObjHandler qHdl : qHdls ) {
			String id = qHdl.getId();
			if( !qXmlIds.contains(id) ) {
				CfgViewQ viewQ = new CfgViewQ().withId(id).withDkSetId("dummy").withLogm(0);
				CfgQLoc locQ = new CfgQLoc("babStray", -1, -1, Direction.U, -1, -1);
				CfgQ strayQ = new CfgQ().withId(id).withName(qHdl.getName()).withKde("0-0-0").withType(qType).withLocation(locQ).withValid(true);
				viewQs.withQ(viewQ);
				dictQs.getQ().add(strayQ);
				for( String dId : qHdl.getChildMap(objType).keySet() ) {
					strayQ.getDevice().add(new CfgDevice().withId(dId).withKde("0-0-0").withType("Q1").withLane("F1").withDe(1).withValid(true));
				}
			}
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public CfgMultiFileItems multiFileItems() {
		return multiFileItems;
	}
	private void collectMultiFileItems() {
		for( String jarName : idSet(CfgType.JAR) ) {
			if(!isTemplate(jarName) ) {
				CfgSiteItems si = get(CfgType.SI, jarName);
				collectMultiFileItems(CfgType.EQ, jarName, si.getEqs().getQ());
				collectMultiFileItems(CfgType.AQ, jarName, si.getAqs().getQ());
				collectMultiFileItems(CfgType.XQ, jarName, si.getXqs().getQ());
				collectMultiFileItems(CfgType.UQ, jarName, si.getUqs().getQ());
				collectMultiFileItems(CfgType.VQ, jarName, si.getVqs().getQ());
				collectMultiFileItems(CfgType.SM, jarName, si.getSms().getQ());
				collectMultiFileItems(CfgType.MU, jarName, si.getMunits().getMunit());
				collectMultiFileItems(CfgType.ZS, jarName, si.getZss().getZs());
				collectMultiFileItems(CfgType.TS, jarName, si.getTss().getTs());
				collectMultiFileItems(CfgType.CL, jarName, si.getLocks().getLock());
				CfgSiteFonts sf = get(CfgType.SF, jarName);
				collectMultiFileItems(CfgType.FD, jarName, sf.getFontDescs().getFont());
				collectMultiFileItems(CfgType.FS, jarName, sf.getFontSpacings().getFontSpacing());
				collectMultiFileItems(CfgType.DF, jarName, sf.getDeviceFontSets().getDeviceFontSet());
			}
		}
		multiFileItems.filter(2);
	}
	private <T> void collectMultiFileItems( CfgType type, String jarName, Iterable<T> lst ) {
		lst.forEach(obj -> multiFileItems.add(type, type.id(obj), jarName));
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public String key( String viewId, QType qType ) {
		return cnv.joinIds(viewId, qType.name());
	}
	public String key( ViewAggrRequest p ) {
		return cnv.joinIds(p.getViewId(), cnv.qType().toPojo(p.getType()).name());
	}
	public String key( RoadSegKey p ) {
		return key(cnv.sitKey().toPojo(p));
	}
	public String key( CfgRoadSegKey p ) {
		return p.getModelKey();
//		if( p.isMeasure() ) {
//			String key = cnv.joinIds(p.getSiteId(), p.getValueId());
//			if( valueClassIdMap.containsKey(key) ) {
//				return cnv.joinIds(p.getSiteId(), valueClassIdMap.get(key), p.getValueId());
//			}
//		}
//		return cnv.joinIds(p.getSiteId(), p.getClassId(), p.getValueId());
	}
	public Set<String> keySet( RoadSegKeys pLst ) {
		return pLst.getItemsList().stream().map(p -> key(p)).collect(Collectors.toSet());
	}
	// -----------------------------------------------------------------------------------------------------------------------------
}
