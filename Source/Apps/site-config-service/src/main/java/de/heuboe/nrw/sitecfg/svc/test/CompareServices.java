package de.heuboe.nrw.sitecfg.svc.test;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.protobuf.Empty;

import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteBabSeg;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteDkSets;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteFonts;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteItems;
import de.heuboe.nrw.guisvc.sitecfg.data.FileSitesDescs;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteConfigError;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteViewsDescs;
import de.heuboe.nrw.guisvc.sitecfg.iface.SiteCfgService;
import de.heuboe.nrw.sitecfg.svc.model.IdMap;
import de.heuboe.nrw.sitecfg.svc.model.Model;
import de.heuboe.sitecfg.grpc.CfgType;
import de.heuboe.sitecfg.grpc.XmlProtoConverter;
import de.heuboe.sitecfg.grpc.data.CfgBabSeg;
import de.heuboe.sitecfg.grpc.data.Util;
import de.heuboe.sitesconfig.CfgDevice;
import de.heuboe.sitesconfig.CfgDeviceFontSet;
import de.heuboe.sitesconfig.CfgDeviceFontSets;
import de.heuboe.sitesconfig.CfgDkSet;
import de.heuboe.sitesconfig.CfgDkSets;
import de.heuboe.sitesconfig.CfgFontDesc;
import de.heuboe.sitesconfig.CfgFontDescs;
import de.heuboe.sitesconfig.CfgFontSpacing;
import de.heuboe.sitesconfig.CfgFontSpacings;
import de.heuboe.sitesconfig.CfgMunit;
import de.heuboe.sitesconfig.CfgMunits;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgQLoc;
import de.heuboe.sitesconfig.CfgQs;
import de.heuboe.sitesconfig.CfgSiteDesc;
import de.heuboe.sitesconfig.CfgTS;
import de.heuboe.sitesconfig.CfgTSs;
import de.heuboe.sitesconfig.CfgView;
import de.heuboe.sitesconfig.CfgViewDesc;
import de.heuboe.sitesconfig.CfgViewType;
import de.heuboe.sitesconfig.CfgWzgCodeLock;
import de.heuboe.sitesconfig.CfgWzgCodeLocks;
import de.heuboe.sitesconfig.CfgZS;
import de.heuboe.sitesconfig.CfgZSs;
import de.heuboe.sitesconfig.CfgZsSymbol;
import de.heuboe.sitecfg.grpc.DkSets;
import de.heuboe.sitecfg.grpc.IdList;
import de.heuboe.sitecfg.grpc.SiteConfigServiceGrpc;
import de.heuboe.sitecfg.grpc.SiteConfigServiceGrpc.SiteConfigServiceBlockingStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import multimap.MultiMap.TLst;
import multimap.MultiMap.TSet;
import stageGraph.util.MeasureTime;

@Slf4j
@SuppressWarnings("unused")
public class CompareServices {
	// --------------------------------------------------------------------------------------------------
	private SiteConfigClients				svc;
	private String							grpcHost;
	private int								grpcPort;
	private ManagedChannel					channel;
	private SiteConfigServiceBlockingStub	grpcSvc;
	private SiteCfgService					soapSvc;
	private XmlProtoConverter				cnv				= new XmlProtoConverter();
	private Stack<StringBuilder>			sbStack			= new Stack<>();
	private TSet<CfgType, String>			typeIdsMap		= new TSet<>();
	private TSet<String, String>			attrErrorMap	= new TSet<>();
	// --------------------------------------------------------------------------------------------------
	public CompareServices( SiteCfgService soap, String grpcHost, int grpcPort ) {
		this.soapSvc = soap;
		this.grpcHost = grpcHost;
		this.grpcPort = grpcPort;
		log.info("CompareServices.ctor");
	}
	// --------------------------------------------------------------------------------------------------
	public void start( Model model ) {
		try {
			channel = ManagedChannelBuilder.forAddress(grpcHost, grpcPort).usePlaintext().maxInboundMessageSize(Integer.MAX_VALUE).build();
			grpcSvc = SiteConfigServiceGrpc.newBlockingStub(channel);
			svc = new SiteConfigClients(soapSvc, grpcSvc);
			work(model);
		} catch( Exception e ) {
			log.error(String.format("Failed to connect to grpc server SiteConfigService{%s:%d}: '%s'", grpcHost, grpcPort, e.getMessage()), e);
		}
	}
	// --------------------------------------------------------------------------------------------------
	private void work( Model model ) throws SiteConfigError {
		sbClear();
		sb(0, "Test running");
		try {
			log.info(svc.grpcLocSegs("LVR-UZ-KAA-KoMoD", CfgViewType.ZST).print(0, CfgType.LOC.name()));
			log.trace(svc.grpcMultiFileItems().print(0, "Catalogue items in multiple jar files:", "%-30s in jars[%s]"));

			Set<String> fileNames = mergeIds(1, "fileNames", svc.soapFileNames(), svc.grpcFileNames());
			compareFileSiteDescs(1, "FileSitesDescs", svc.soapFileSiteDescs(fileNames), svc.grpcFileSiteDescs(fileNames));

			Set<String> siteIds = mergeIds(1, "siteIds", svc.soapSiteIds(), svc.grpcSiteIds());
			siteIds.remove("LVR-LBA_A3_Breit.-Hild.");
			siteIds.remove(Model.STRAY_SITE_ID);
			compareSiteViewDescs(1, "SiteViewsDescs", svc.soapSiteViewDescs(siteIds), svc.grpcSiteViewDescs(siteIds));
			for( String siteId : siteIds ) {
				compareSite(1, siteId);
			}
			comparePngs(1, "All FontDesc PNGs", model);
			compareSvgs(1, "All View SVGs");
			compareAqSvgs(1, "All View AQ SVGs");
			compareAll(1);
			MeasureTime.report("duration of service calls");
		} catch( Exception e ) {
			sbError(1, "work", e);
		}
		log.trace(sb().toString());
		log.warn(printAttrErrorMap());
		log.info("Test finished");
	}
	private void compareFileSiteDescs( int level, String label, List<FileSitesDescs> soapLst, List<de.heuboe.sitecfg.grpc.data.FileSitesDescs> grpcLst ) {
		sb(level, label);
		try {
			IdMap<FileSitesDescs> soapIdMap = new IdMap<>(soapLst, FileSitesDescs::getId);
			IdMap<de.heuboe.sitecfg.grpc.data.FileSitesDescs> grpcIdMap = new IdMap<>(grpcLst, de.heuboe.sitecfg.grpc.data.FileSitesDescs::getId);
			Set<String> fileNames = mergeIds(level, "FSV-ids", soapIdMap.keySet(), grpcIdMap.keySet());
			for( String fileName : fileNames ) {
				FileSitesDescs soap = soapIdMap.get(fileName);
				de.heuboe.sitecfg.grpc.data.FileSitesDescs grpc = grpcIdMap.get(fileName);
				compareSiteViewDescs(level + 1, "SiteViewsDescs of " + fileName, soap.getSiteViewDescs(), grpc.getSiteViewDescs());
			}
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareSiteViewDescs( int level, String label, List<SiteViewsDescs> soapLst, List<de.heuboe.sitecfg.grpc.data.SiteViewsDescs> grpcLst ) {
		sb(level, label);
		try {
			TLst<String, String> siteViewIds = new TLst<>();
			IdMap<SiteViewsDescs> soapIdMap = new IdMap<>(soapLst, SiteViewsDescs::getId);
			IdMap<de.heuboe.sitecfg.grpc.data.SiteViewsDescs> grpcIdMap = new IdMap<>(grpcLst, de.heuboe.sitecfg.grpc.data.SiteViewsDescs::getId);
			Set<String> siteIds = mergeIds(level, "SV-ids", soapIdMap.keySet(), grpcIdMap.keySet());
			for( String siteId : siteIds ) {
				SiteViewsDescs soap = soapIdMap.get(siteId);
				de.heuboe.sitecfg.grpc.data.SiteViewsDescs grpc = grpcIdMap.get(siteId);
				compareSD(level + 1, CfgType.SD, siteId, soap.getSiteDesc(), grpc.getSiteDesc());
				siteViewIds.add(siteId, compareList(level + 1, CfgType.VD, soap.getViewDescs(), grpc.getViewDescs()));//.forEach(viewId -> viewSiteIds.put(viewId, siteId));
			}
			sb(level, "CfgViews");
			for( String siteId : siteViewIds.keySet() ) {
				for( String viewId : siteViewIds.all(siteId) ) {
					if(!"Template-View".equals(viewId)) {
						compareView(level + 1, siteId, viewId, svc.soapViewConfig(viewId), svc.grpcViewConfig(viewId));
					}
				}
			}
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareView( int level, String siteId, String viewId, CfgView soap, CfgView grpc ) {
		String label = String.format("View:'%s'", viewId);
		sb(level, label);
		try {
			if( soap.getDesc().isValid() != grpc.getDesc().isValid() ) {
				sbWarn(level + 1, "valid", "%s:%s.valid(%b != %b)", CfgType.VI.name(), viewId, soap.getDesc().isValid(), grpc.getDesc().isValid());
				soap.getDesc().setValid(grpc.getDesc().isValid());
			}
			if( siteId.startsWith("WW.") || siteId.startsWith("Q-11251") ) {
				if( soap.getEqs() == null ) soap.setEqs(grpc.getEqs());
				if( soap.getUqs() == null ) soap.setUqs(grpc.getUqs());
				if( soap.getXqs() == null ) soap.setXqs(grpc.getXqs());
				if( soap.getVqs() == null ) soap.setVqs(grpc.getVqs());
				if( soap.getSms() == null ) soap.setSms(grpc.getSms());
				if( soap.getMunits() == null ) soap.setMunits(grpc.getMunits());
				sbWarn(level + 1, "dwista CfgViewQs null", "%s:%s dwista CfgViewQs null in soap", CfgType.VI.name(), viewId);
			}
			String svgId = soap.getDesc().getSvgLayout();
			if( svgId != null && !svgId.isEmpty() && svgId.equals(grpc.getDesc().getSvgLayout()) ) {
				typeIdsMap.add(CfgType.SVG, svgId);
				compareList(level + 1, CfgType.QV, soap.getAqs().getQ(), grpc.getAqs().getQ()).forEach(aqId -> {
					typeIdsMap.add(CfgType.SVG_AQ, cnv.joinIds(svgId, aqId));
				});
			}
			compare(level + 1, CfgType.VI, viewId, soap, grpc);
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareSite( int level, String siteId ) {
		String label = String.format("Site:'%s'", siteId);
		sb(level, label);
		try {
			compareSiteItems(level + 1, "SiteItems", siteId, svc.soapSiteItems(siteId), svc.grpcSiteItems(siteId));
			compareSiteFonts(level + 1, "SiteFonts", siteId, svc.soapSiteFonts(siteId), svc.grpcSiteFonts(siteId));
			compareSiteDkSets(level + 1, "SiteDkSets", siteId, svc.soapSiteDkSets(siteId), svc.grpcSiteDkSets(siteId));
			compareSegs(level, siteId);
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareSiteItems( int level, String label, String siteId, CfgSiteItems soap, de.heuboe.sitecfg.grpc.data.CfgSiteItems grpc ) {
		sb(level, label);
		try {
			compareList(level + 1, CfgType.EQ, getW(soap.getEqs()).getQ(), getW(grpc.getEqs()).getQ());
			compareList(level + 1, CfgType.UQ, getW(soap.getUqs()).getQ(), getW(grpc.getUqs()).getQ());
			compareList(level + 1, CfgType.AQ, getW(soap.getAqs()).getQ(), getW(grpc.getAqs()).getQ());
			compareList(level + 1, CfgType.VQ, getW(soap.getVqs()).getQ(), getW(grpc.getVqs()).getQ());
			compareList(level + 1, CfgType.XQ, getW(soap.getXqs()).getQ(), getW(grpc.getXqs()).getQ());
			compareList(level + 1, CfgType.SM, getW(soap.getSms()).getQ(), getW(grpc.getSms()).getQ());
			compareList(level + 1, CfgType.MU, getW(soap.getMunits()).getMunit(), getW(grpc.getMunits()).getMunit());
			compareList(level + 1, CfgType.ZS, getW(soap.getZss()).getZs(), getW(grpc.getZss()).getZs());
			compareList(level + 1, CfgType.TS, getW(soap.getTss()).getTs(), getW(grpc.getTss()).getTs());
			compareList(level + 1, CfgType.CL, getW(soap.getLocks()).getLock(), getW(grpc.getLocks()).getLock());
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareSiteFonts( int level, String label, String siteId, CfgSiteFonts soap, de.heuboe.sitecfg.grpc.data.CfgSiteFonts grpc ) {
		sb(level, label);
		try {
			compareList(level + 1, CfgType.DF, getW(soap.getDeviceFontSets()).getDeviceFontSet(), getW(grpc.getDeviceFontSets()).getDeviceFontSet());
			compareList(level + 1, CfgType.FD, getW(soap.getFontDescs()).getFont(), getW(grpc.getFontDescs()).getFont());
			compareList(level + 1, CfgType.FS, getW(soap.getFontSpacings()).getFontSpacing(), getW(grpc.getFontSpacings()).getFontSpacing());
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareSiteDkSets( int level, String label, String siteId, CfgSiteDkSets soap, CfgDkSets grpc ) {
		sb(level, label);
		try {
			compareList(level + 1, CfgType.DK, soap.getDkSets(), grpc.getDkSet());
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareSegs( int level, String siteId ) {
		try {
			List<CfgSiteBabSeg> soap = svc.soapSiteBabSegs(siteId);
			compareSegs(level, "SiteBabSegs", CfgType.SEG, siteId, svc.soapSiteBabSegs(siteId), svc.grpcSiteBabSegs(siteId));
			compareSegs(level, "ViewBabSegs", CfgType.SEG, siteId, svc.soapBabSegs(siteId, CfgViewType.SPR), svc.grpcBabSegs(siteId, CfgViewType.SPR));
			if( "LVR-UZ-KAA-KoMoD".equals(siteId) ) {
				compareSegs(level, "ViewLocSegs", CfgType.LOC, siteId, svc.soapLocSegs(siteId, CfgViewType.ZST).getPixSegs(), svc.grpcLocSegs(siteId, CfgViewType.ZST).getPixSegs());
			}
		} catch( Exception e ) {
			sbError(level + 1, "compareSiteBabSegs", e);
		}
	}
	private <S, G> void compareSegs( int level, String label, CfgType type, String siteId, List<S> soap, List<G> grpc ) {
		sb(level, label);
		try {
			if( soap.size() != grpc.size() ) {
				sbWarn(level + 1, label, "%s:%s.count(%d != %d)", type.name(), siteId, soap.size(), grpc.size());
			}
			for( int i=0; i< Math.min(soap.size(), grpc.size()); ++i ) {
				compare(level + 1, type, siteId, soap.get(i), grpc.get(i));
			}
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareAll( int level ) throws SiteConfigError {
		compareAll(level, CfgType.EQ);
		compareAll(level, CfgType.UQ);
		compareAll(level, CfgType.AQ);
		compareAll(level, CfgType.VQ);
		compareAll(level, CfgType.XQ);
		compareAll(level, CfgType.SM);
		compareAll(level, CfgType.DF);
		compareAll(level, CfgType.FD);
		compareAll(level, CfgType.FS);
		compareAll(level, CfgType.ZS);
		compareAll(level, CfgType.TS);
		compareAll(level, CfgType.CL);
		compareAll(level, CfgType.MU);
	}
	private <T> void compareAll( int level, CfgType type ) throws SiteConfigError {
		String label = String.format("All %s", type.name());
		sb(level, label);
		try {
			Set<String> idSet = typeIdsMap.all(type);
			List<T> soapLst = svc.soapDictType(type, idSet);
			List<T> grpcLst = svc.grpcDictType(type, idSet);
			compareList(level + 1, type, soapLst, grpcLst);
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void comparePngs( int level, String label, Model model ) {
		sb(level, label);
		try {
			List<CfgFontDesc> soap = soapSvc.getFontDescs(toLst(typeIdsMap.all(CfgType.FD)));
			List<CfgFontDesc> grpc = cnv.fd().toPojoW(grpcSvc.getFontDescs(cnv.toProto(typeIdsMap.all(CfgType.FD)))).getFont();
			compareList(level + 1, CfgType.FD, soap, grpc);
			typeIdsMap.all(CfgType.FD).forEach(id -> {
				if( model.has(CfgType.FD, id) ) {
					CfgFontDesc fd = model.get(CfgType.FD, id);
					String pngId = fd.getFileName();
					try {
						byte[] soapPng = svc.soapPng(pngId);
						byte[] grpcPng = svc.grpcPng(pngId);
						if(!Arrays.equals(soapPng, grpcPng) ) {
							sbWarn(level + 1, "png", "%s:%s not equal", CfgType.PNG.name(), pngId);
						}
					} catch( SiteConfigError e ) {
						sbError(level + 1, String.format("soapSvc.getPng(%s)", pngId), e);
					}
				}
			});
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareSvgs( int level, String label ) {
		sb(level, label);
		try {
			typeIdsMap.all(CfgType.SVG).forEach(svgId -> {
				try {
					String soapSvg = svc.soapSvg(svgId);
					String grpcSvg = svc.grpcSvg(svgId);
					soapSvg = soapSvg.replaceAll("((?<!\\r)\\n|\\r(?!\\n))", "\r\n");
					if(!soapSvg.equals(grpcSvg) ) {
						sbWarn(level + 1, "svg", "%s:%s not equal", CfgType.SVG.name(), svgId);
					}
				} catch( SiteConfigError e ) {
					sbError(level + 1, String.format("soapSvc.getSvg(%s)", svgId), e);
				}
			});
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	private void compareAqSvgs( int level, String label ) {
		sb(level, label);
		try {
			typeIdsMap.all(CfgType.SVG_AQ).forEach(svgAqId -> {
				String[] ss = svgAqId.split(":");
				try {
					String soapSvg = svc.soapAqSvg(ss[0], ss[1]);
					String grpcSvg = svc.grpcAqSvg(ss[0], ss[1]);
					if(!soapSvg.equals(grpcSvg) ) {
						sbWarn(level + 1, "svg_aq", "%s:%s not equal", CfgType.SVG_AQ.name(), ss[0], ss[1]);
					}
				} catch( SiteConfigError e ) {
					sbError(level + 1, String.format("soapSvc.getAqSvg(%s, %s)", ss[0], ss[1]), e);
				}
			});
		} catch( Exception e ) {
			sbError(level + 1, label, e);
		}
	}
	// --------------------------------------------------------------------------------------------------
	private <S, G> Set<String> compareList( int level, CfgType type, Collection<S> soapLst, Collection<G> grpcLst ) {
		return compareList(level, type, soapLst, grpcLst, type.idGetter(), type.idGetter());
	}
	private <S, G> Set<String> compareList( int level, CfgType type, Collection<S> soapLst, Collection<G> grpcLst, Function<S, String> soapId, Function<G, String> grpcId ) {
		IdMap<S> soapIdMap = new IdMap<>(soapLst, soapId);
		IdMap<G> grpcIdMap = new IdMap<>(grpcLst, grpcId);
		Set<String> ids = mergeIds(level, type.name() + "-ids", soapIdMap.keySet(), grpcIdMap.keySet());
		if( type.hasGroup() && !ids.isEmpty() ) {
			sb(level, "compareList(%3d %ss)", ids.size(), type.name());
		}
		if( ids.contains("C") ) {
			log.trace("");
		}
		for( String id : ids ) {
			S soap = soapIdMap.get(id);
			G grpc = grpcIdMap.get(id);
			if( grpc instanceof CfgQ ) {
				compareQ(level + 1, type, id, CfgQ.class.cast(soap), CfgQ.class.cast(grpc));
			}
			if( grpc instanceof CfgViewDesc ) {
				compareVD(level + 1, type, id, CfgViewDesc.class.cast(soap), CfgViewDesc.class.cast(grpc));
			}
			if( grpc instanceof CfgZS ) {
				compareZS(level + 1, type, id, CfgZS.class.cast(soap), CfgZS.class.cast(grpc));
			}
			compare(level + 1, type, id, soap, grpc);
		}
		typeIdsMap.add(type, ids);
		return ids;
//		return soapIdMap.values().stream().filter(o -> ids.contains(type.id(o))).collect(Collectors.toList());
	}
	private <S, G> void compare( int level, CfgType type, String id, S soap, G grpc ) {
		if( soap.hashCode() != grpc.hashCode() ) {
			sbWarn(level + 1, "hash*", "%s:%s not equal", type.name(), id);
		}
	}
	private void compareSD( int level, CfgType type, String id, CfgSiteDesc soap, CfgSiteDesc grpc ) {
		String soapSyms = soap.getSvgSymbols();
		String grpcSyms = grpc.getSvgSymbols();
		if( soap.isValid() != grpc.isValid() ) {
			sbWarn(level + 1, "valid", "%s:%s.valid(%b != %b)", type.name(), id, soap.isValid(), grpc.isValid());
			soap.setValid(grpc.isValid());
		}
		if(!soapSyms.equals(grpcSyms) && soapSyms.contains("dWiStaSites") ) {
			soap.setSvgSymbols(grpcSyms);
		}
		compare(level + 1, type, id, soap, grpc);
	}
	private void compareVD( int level, CfgType type, String id, CfgViewDesc soap, CfgViewDesc grpc ) {
		String soapSyms = soap.getSvgLayout();
		String grpcSyms = grpc.getSvgLayout();
		if( soap.isValid() != grpc.isValid() ) {
			sbWarn(level + 1, "valid", "%s:%s.valid(%b != %b)", type.name(), id, soap.isValid(), grpc.isValid());
			soap.setValid(grpc.isValid());
		}
		if(!soapSyms.equals(grpcSyms) && soapSyms.contains("dWiStaSites") ) {
			soap.setSvgLayout(grpcSyms);
		}
	}
	private void compareZS( int level, CfgType type, String id, CfgZS soap, CfgZS grpc ) {
		if(!soap.getType().equals(grpc.getType()) ) {
			sbWarn(level + 1, "zs-type", "%s:%s.type not equal(%s != %s)", type.name(), id, soap.getType(), grpc.getType());
			soap.setType(grpc.getType());
			List<CfgZsSymbol> soapSymbols = soap.getSymbol();
			List<CfgZsSymbol> grpcSymbols = grpc.getSymbol();
			for( int i=0; i< Math.min(soapSymbols.size(), grpcSymbols.size()); ++i ) {
				soapSymbols.get(i).setId(grpcSymbols.get(i).getId());
			}
		}
	}
	private void compareQ( int level, CfgType type, String id, CfgQ soap, CfgQ grpc ) {
		compareList(level + 1, CfgType.DD, soap.getDevice(), grpc.getDevice());
		CfgQLoc soapLoc = soap.getLocation();
		CfgQLoc grpcLoc = grpc.getLocation();
		String soapBab = soapLoc.getBabName();
		String grpcBab = grpcLoc.getBabName();
		int soapMtr = soapLoc.getBabm();
		int grpcMtr = grpcLoc.getBabm();
		if( soapMtr != grpcMtr ) {
			if( Math.abs(soapMtr - grpcMtr) > 9 ) {
				sbWarn(level + 1, "meter", "%s:%s.babm(%5d != %5d)", type.name(), id, soapMtr, grpcMtr);
			}
			soapLoc.setBabm(grpcMtr);
		}
		if(!soapBab.equals(grpcBab) ) {
			if( !"-1".equals(soapBab) ) {//&& !Util.filterNumbers(soapBab).equals(Util.filterNumbers(grpcBab)) ) {
				sbWarn(level + 1, "route", "%s:%s.babName(%s != %s)", type.name(), id, soapBab, grpcBab);
			}
			soapLoc.setBabName(grpcBab);
		}
		if( soap.isValid() != grpc.isValid() ) {
			if( CfgType.SM != type ) {
				sbWarn(level + 1, "valid", "%s:%s.valid(%b != %b)", type.name(), id, soap.isValid(), grpc.isValid());
			}
			soap.setValid(grpc.isValid());
		}
		if( soapLoc.hashCode() != grpcLoc.hashCode() ) {
			sbWarn(level + 1, "hashQ", "%s:%s.loc not equal", type.name(), id);
			soap.setLocation(grpc.getLocation());
		}
	}
	// --------------------------------------------------------------------------------------------------
	private Set<String> mergeIds( int level, String label, Set<String> soap, Set<String> grpc ) {
		compareIds(level, label + " only soap", soap, grpc);
		compareIds(level, label + " only grpc", grpc, soap);
		return soap.stream().filter(o -> grpc.contains(o)).collect(Collectors.toCollection(TreeSet::new));
	}
	private void compareIds( int level, String label, Set<String> lhs, Set<String> rhs ) {
		Set<String> diff = lhs.stream().filter(o -> !rhs.contains(o)).collect(Collectors.toCollection(TreeSet::new));
		if( !diff.isEmpty() ) {
			StringBuilder sb = new StringBuilder(String.format("%s [ ", label));
			diff.forEach(s -> sb.append(String.format("%s ", s)));
			sbWarn(level + 1, "idCmp", sb.append("]").toString());
		}
	}
	// --------------------------------------------------------------------------------------------------
	private <T> List<T> toLst( Collection<T> coll ) {
		return new ArrayList<>(coll);
	}
	private <T> Set<T> toSet( Collection<T> coll ) {
		return new TreeSet<>(coll);
	}
	private Empty empty() {
		return Empty.newBuilder().build();
	}
	// --------------------------------------------------------------------------------------------------
	private CfgQs getW( CfgQs w ) {
		return w != null ? w : new CfgQs();
	}
	private CfgMunits getW( CfgMunits w ) {
		return getW(w, new CfgMunits());
	}
	private CfgZSs getW( CfgZSs w ) {
		return getW(w, new CfgZSs());
	}
	private CfgTSs getW( CfgTSs w ) {
		return getW(w, new CfgTSs());
	}
	private CfgDkSets getW( CfgDkSets w ) {
		return getW(w, new CfgDkSets());
	}
	private CfgDeviceFontSets getW( CfgDeviceFontSets w ) {
		return getW(w, new CfgDeviceFontSets());
	}
	private CfgFontDescs getW( CfgFontDescs w ) {
		return getW(w, new CfgFontDescs());
	}
	private CfgFontSpacings getW( CfgFontSpacings w ) {
		return getW(w, new CfgFontSpacings());
	}
	private CfgWzgCodeLocks getW( CfgWzgCodeLocks w ) {
		return getW(w, new CfgWzgCodeLocks());
	}
	private <W> W getW( W w, W defaultValue ) {
		return w != null ? w : defaultValue;
	}
	// --------------------------------------------------------------------------------------------------
	private StringBuilder sb() {
		return sbStack.peek();
	}
	private StringBuilder sbPush() {
		sbStack.push(new StringBuilder());
		return sbStack.peek();
	}
	private StringBuilder sbPop() {
		return sbStack.pop();
	}
	private StringBuilder sbClear() {
		sbStack.clear();
		return sbPush();
	}
	private StringBuilder sb( int level ) {
		return sb().append("\n").append(Util.nParts(level, "  "));
	}
	private StringBuilder sb( int level, String msg ) {
		return sb(level).append(msg);
	}
	private StringBuilder sb( int level, String format, Object... args ) {
		return sb(level, String.format(format, args));
	}
	private StringBuilder sb( String format, Object... args ) {
		return sb().append(String.format(format, args));
	}
	private StringBuilder sbWarn( int level, String attr, String format, Object... args ) {
		String msg = String.format(format, args);
//		log.warn(msg);
		attrErrorMap.add(attr, msg);
		return sb(level, msg);
	}
	private StringBuilder sbError( int level, String label, Exception e ) {
		log.info(sb().toString());
		log.error(String.format("error in method %s:'%s'", label, e.getMessage()), e);
		return sb(level, "ERROR:'%s'", e.getMessage());
	}
	private String printAttrErrorMap() {
		StringBuilder sb = new StringBuilder("attrErrorMap");
		attrErrorMap.keySet().forEach(attr -> {
			sb.append(String.format("%n %s: %3d diffs", attr, attrErrorMap.cnt(attr)));
			attrErrorMap.all(attr).forEach(msg -> {
				sb.append(String.format("%n  %s", msg));
			});
		});
		return sb.toString();
	}
	// --------------------------------------------------------------------------------------------------
	private void enterSoap( String label ) {
		MeasureTime.enter(String.format("%s.soap", label));
	}
	private void leaveSoap( String label ) {
		MeasureTime.leave(String.format("%s.soap", label));
	}
	private void leaventer( String label ) {
		MeasureTime.leave(String.format("%s.soap", label));
		MeasureTime.enter(String.format("%s.grpc", label));
	}
	private void enterGrpc( String label ) {
		MeasureTime.enter(String.format("%s.grpc", label));
	}
	private void leaveGrpc( String label ) {
		MeasureTime.leave(String.format("%s.grpc", label));
	}
	// --------------------------------------------------------------------------------------------------
}
