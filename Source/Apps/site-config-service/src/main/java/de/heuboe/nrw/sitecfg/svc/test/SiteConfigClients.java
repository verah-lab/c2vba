package de.heuboe.nrw.sitecfg.svc.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import com.google.protobuf.Empty;

import de.heuboe.nrw.guisvc.sitecfg.data.CfgBabSeg;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgLocSegs;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteBabSeg;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteDkSets;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteFonts;
import de.heuboe.nrw.guisvc.sitecfg.data.CfgSiteItems;
import de.heuboe.nrw.guisvc.sitecfg.data.FileSitesDescs;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteConfigError;
import de.heuboe.nrw.guisvc.sitecfg.data.SiteViewsDescs;
import de.heuboe.nrw.guisvc.sitecfg.iface.SiteCfgService;
import de.heuboe.sitecfg.grpc.CfgType;
import de.heuboe.sitecfg.grpc.XmlProtoConverter;
import de.heuboe.sitecfg.grpc.data.CfgAqSvgs;
import de.heuboe.sitecfg.grpc.data.CfgRoadSegKey;
import de.heuboe.sitecfg.grpc.data.CfgRoadSegStyle;
import de.heuboe.sitecfg.grpc.data.CfgViewAggrDesc;
import de.heuboe.sitesconfig.CfgAggrDesc;
import de.heuboe.sitesconfig.CfgDkSets;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgSitClasses;
import de.heuboe.sitesconfig.CfgSitDisplayTypes;
import de.heuboe.sitesconfig.CfgView;
import de.heuboe.sitesconfig.CfgViewType;
import de.heuboe.sitesconfig.QType;
import de.heuboe.sitecfg.grpc.IdList;
import de.heuboe.sitecfg.grpc.CfgMultiFileItems;
import de.heuboe.sitecfg.grpc.SiteConfigServiceGrpc.SiteConfigServiceBlockingStub;
import stageGraph.util.MeasureTime;

@SuppressWarnings("unchecked")
public class SiteConfigClients {
	// --------------------------------------------------------------------------------------------------
	private SiteCfgService					soapSvc;
	private SiteConfigServiceBlockingStub	grpcSvc;
	private XmlProtoConverter				cnv				= new XmlProtoConverter();
	// --------------------------------------------------------------------------------------------------
	public SiteConfigClients( SiteCfgService soapSvc, SiteConfigServiceBlockingStub grpcSvc ) {
		this.soapSvc = soapSvc;
		this.grpcSvc = grpcSvc;
	}
	// --------------------------------------------------------------------------------------------------
	Set<String> soapSiteIds() throws SiteConfigError {
		return toSet(callSoap("getSiteIds", () -> soapSvc.getSiteIds()));
	}
	Set<String> grpcSiteIds() {
		return callGrpc("getSiteIds", () -> cnv.idSet(grpcSvc.getSiteIds(empty())));
	}
	// --------------------------------------------------------------------------------------------------
	Set<String> soapFileNames() throws SiteConfigError {
		return toSet(callSoap("getFileNames", () -> soapSvc.getFileNames()));
	}
	Set<String> grpcFileNames() {
		return callGrpc("getFileNames", () -> cnv.idSet(grpcSvc.getFileNames(empty())));
	}
	// --------------------------------------------------------------------------------------------------
	List<FileSitesDescs> soapFileSiteDescs( Collection<String> fileNames ) throws SiteConfigError {
		return callSoap("getFileSiteDescs", () -> soapSvc.getFileSiteDescs(toLst(fileNames)));
	}
	List<de.heuboe.sitecfg.grpc.data.FileSitesDescs> grpcFileSiteDescs( Collection<String> fileNames ) {
		return callGrpc("getFileSiteDescs", () -> cnv.fsd().toPojos(grpcSvc.getFileSiteDescs(cnv.toProto(fileNames)).getItemsList()));
	}
	// --------------------------------------------------------------------------------------------------
	CfgMultiFileItems grpcMultiFileItems() {
		return callGrpc("getMultiFileItems", () -> cnv.mfi().toPojo(grpcSvc.getMultiFileItems(empty())));
	}
	// --------------------------------------------------------------------------------------------------
	List<SiteViewsDescs> soapSiteViewDescs( Collection<String> siteIds ) throws SiteConfigError {
		return callSoap("getSiteViewDescs", () -> soapSvc.getSiteViewDescs(toLst(siteIds)));
	}
	List<de.heuboe.sitecfg.grpc.data.SiteViewsDescs> grpcSiteViewDescs( Collection<String> siteIds ) {
		return callGrpc("getSiteViewDescs", () -> cnv.svd().toPojos(grpcSvc.getSiteViewDescs(cnv.toProto(siteIds)).getItemsList()));
	}
	// --------------------------------------------------------------------------------------------------
	CfgView soapViewConfig( String viewId ) throws SiteConfigError {
		return callSoap("getViewConfig", () -> soapSvc.getViewConfig(viewId));
	}
	CfgView grpcViewConfig( String viewId ) {
		return callGrpc("getViewConfig", () -> cnv.vi().toPojo(grpcSvc.getViewConfig(cnv.toProto(viewId))));
	}
	// --------------------------------------------------------------------------------------------------
	CfgSiteItems soapSiteItems( String siteId ) throws SiteConfigError {
		return callSoap("getSiteItems", () -> soapSvc.getSiteItems(siteId));
	}
	de.heuboe.sitecfg.grpc.data.CfgSiteItems grpcSiteItems( String siteId ) {
		return callGrpc("getSiteItems", () -> cnv.si().toPojo(grpcSvc.getSiteItems(cnv.toProto(siteId))));
	}
	// --------------------------------------------------------------------------------------------------
	CfgSiteFonts soapSiteFonts( String siteId ) throws SiteConfigError {
		return callSoap("getSiteFonts", () -> soapSvc.getSiteFonts(siteId));
	}
	de.heuboe.sitecfg.grpc.data.CfgSiteFonts grpcSiteFonts( String siteId ) {
		return callGrpc("getSiteFonts", () -> cnv.sf().toPojo(grpcSvc.getSiteFonts(cnv.toProto(siteId))));
	}
	// --------------------------------------------------------------------------------------------------
	CfgSiteDkSets soapSiteDkSets( String siteId ) throws SiteConfigError {
		return callSoap("getSiteDkSets", () -> soapSvc.getSiteDkSets(siteId));
	}
	CfgDkSets grpcSiteDkSets( String siteId ) {
		return callGrpc("getSiteDkSets", () -> cnv.dk().toPojoW(grpcSvc.getSiteDkSets(cnv.toProto(siteId))));
	}
	// --------------------------------------------------------------------------------------------------
	List<CfgSiteBabSeg> soapSiteBabSegs( String siteId ) throws SiteConfigError {
		return callSoap("getSiteBabSegs", () -> soapSvc.getSiteBabSegs(siteId));
	}
	List<de.heuboe.sitecfg.grpc.data.CfgBabSeg> grpcSiteBabSegs( String siteId ) {
		return callGrpc("getSiteBabSegs", () -> cnv.babSeg().toPojos(grpcSvc.getSiteBabSegs(cnv.toProto(siteId)).getItemsList()));
	}
	// --------------------------------------------------------------------------------------------------
	List<CfgBabSeg> soapBabSegs( String siteId, CfgViewType viewType ) throws SiteConfigError {
		return callSoap("getBabSegs", () -> soapSvc.getBabSegs(siteId, viewType));
	}
	List<CfgBabSeg> soapBabSegs( String siteId, String viewId ) throws SiteConfigError {
		return callSoap("getBabSegs", () -> soapSvc.getViewBabSegs(siteId, viewId));
	}
	List<de.heuboe.sitecfg.grpc.data.CfgBabSeg> grpcBabSegs( String siteId, CfgViewType viewType ) {
		return callGrpc("getBabSegs", () -> cnv.babSeg().toPojos(grpcSvc.getBabSegs(cnv.locSegs().toProto(siteId, viewType.name())).getItemsList()));
	}
	List<de.heuboe.sitecfg.grpc.data.CfgBabSeg> grpcBabSegs( String siteId, String viewId ) {
		return callGrpc("getBabSegs", () -> cnv.babSeg().toPojos(grpcSvc.getBabSegs(cnv.locSegs().toProto(siteId, viewId)).getItemsList()));
	}
	// --------------------------------------------------------------------------------------------------
	CfgLocSegs soapLocSegs( String siteId, CfgViewType viewType ) throws SiteConfigError {
		return callSoap("getLocSegs", () -> soapSvc.getLocSegs(siteId, viewType));
	}
	CfgLocSegs soapLocSegs( String siteId, String viewId ) throws SiteConfigError {
		return callSoap("getLocSegs", () -> soapSvc.getViewLocSegs(siteId, viewId));
	}
	de.heuboe.sitecfg.grpc.data.CfgLocSegs grpcLocSegs( String siteId, CfgViewType viewType ) {
		return callGrpc("getLocSegs", () -> cnv.locSegs().toPojo(grpcSvc.getLocSegs(cnv.locSegs().toProto(siteId, viewType.name()))));
	}
	de.heuboe.sitecfg.grpc.data.CfgLocSegs grpcLocSegs( String siteId, String viewId ) {
		return callGrpc("getLocSegs", () -> cnv.locSegs().toPojo(grpcSvc.getLocSegs(cnv.locSegs().toProto(siteId, viewId))));
	}
	// --------------------------------------------------------------------------------------------------
	CfgSitDisplayTypes soapSitDisplayTypes( String siteId ) throws SiteConfigError {
		return callSoap("getSitDisplayTypes", () -> soapSvc.getSitDisplayTypes(siteId));
	}
	CfgSitClasses soapSitClasses( String siteId ) throws SiteConfigError {
		return callSoap("getSitClasses", () -> soapSvc.getSitClasses(siteId));
	}
	CfgSitDisplayTypes grpcSitDisplayTypes( String siteId ) {
		return callGrpc("getSitDisplayTypes", () -> cnv.sitTyp().toPojoW(grpcSvc.getSitDisplayTypes(cnv.toProto(siteId))));
	}
	CfgSitClasses grpcSitClasses( String siteId ) {
		return callGrpc("getSitClasses", () -> cnv.sitCls().toPojoW(grpcSvc.getSitClasses(cnv.toProto(siteId))));
	}
	// --------------------------------------------------------------------------------------------------
	List<CfgRoadSegStyle> grpcRoadSegStyles( List<CfgRoadSegKey> keys ) {
		return callGrpc("getRoadSegStyles", () -> cnv.sitStyle().toPojoW(grpcSvc.getRoadSegStyles(cnv.sitKey().toProtoW(keys))));
	}
	CfgRoadSegStyle grpcRoadSegStyle( String siteId, String classId, String valueId, boolean isMeasure ) {
		CfgRoadSegKey key = new CfgRoadSegKey(siteId, classId, valueId, isMeasure);
		return callGrpc("getRoadSegStyle", () -> cnv.sitStyle().toPojo(grpcSvc.getRoadSegStyle(cnv.sitKey().toProto(key))));
	}
	String grpcRoadSegSvg( String siteId, String classId, String valueId, boolean isMeasure ) {
		CfgRoadSegKey key = new CfgRoadSegKey(siteId, classId, valueId, isMeasure);
		return callGrpc("getRoadSegSvg", () -> cnv.svg().toPojo(grpcSvc.getRoadSegSvg(cnv.sitKey().toProto(key))));
	}
	String grpcSymbolSvg( String svgId, String symbolId ) {
		return callGrpc("getSymbolSvg", () -> cnv.svg().toPojo(grpcSvc.getSymbolSvg(cnv.toSymbolSvgId(svgId, symbolId))));
	}
	// --------------------------------------------------------------------------------------------------
	List<CfgAggrDesc> grpcViewAggrDescs( String viewId, QType type ) {
		return callGrpc("getViewAggrDescs", () -> cnv.aggrDesc().toPojos(grpcSvc.getViewAggrDescs(cnv.toViewAggrRequest(viewId, type)).getItemsList()));
	}
	List<CfgViewAggrDesc> grpcAllViewAggrDescs() {
		return callGrpc("getAllViewAggrDescs", () -> cnv.viewAggrDesc().toPojos(grpcSvc.getAllViewAggrDescs(empty()).getItemsList()));
	}
	// --------------------------------------------------------------------------------------------------
	String soapAqSvg( String svgId, String aqId ) throws SiteConfigError {
		return callSoap("getAqSvg", () -> soapSvc.getAqSvg(svgId, aqId));
	}
	String grpcAqSvg( String svgId, String aqId ) {
		return callGrpc("getAqSvg", () -> cnv.svg().toPojo(grpcSvc.getAqSvg(cnv.toAqSvgId(svgId, aqId))));
	}
	CfgAqSvgs grpcAqSvgs( String svgId, Iterable<String> aqIds ) {
		return callGrpc("getAqSvgs", () -> cnv.aqSvg().toPojoW(grpcSvc.getAqSvgs(cnv.toAqSvgIds(svgId, aqIds))));
	}
	// --------------------------------------------------------------------------------------------------
	String soapSvg( String svgId ) throws SiteConfigError {
		return callSoap("getSvg", () -> soapSvc.getSvg(svgId));
	}
	String grpcSvg( String svgId ) {
		return callGrpc("getSvg", () -> cnv.svg().toPojo(grpcSvc.getSvg(cnv.toProto(svgId))));
	}
	// --------------------------------------------------------------------------------------------------
	byte[] soapPng( String pngId ) throws SiteConfigError {
		return callSoap("getPng", () -> soapSvc.getPng(pngId));
	}
	byte[] grpcPng( String pngId ) {
		return callGrpc("getPng", () -> cnv.png().toPojo(grpcSvc.getPng(cnv.toProto(pngId))));
	}
	// --------------------------------------------------------------------------------------------------
	<T> List<T> soapDictType( CfgType type, Collection<String> ids ) throws SiteConfigError {
		List<String> idLst = toLst(ids);
		switch(type) {
			case EQ: return (List<T>)callSoap("getEqs", () -> soapSvc.getEqs(idLst));
			case UQ: return (List<T>)callSoap("getUqs", () -> soapSvc.getUqs(idLst));
			case AQ: return (List<T>)callSoap("getAqs", () -> soapSvc.getAqs(idLst));
			case VQ: return (List<T>)callSoap("getVqs", () -> soapSvc.getVqs(idLst));
			case XQ: return (List<T>)callSoap("getXqs", () -> soapSvc.getXqs(idLst));
			case SM: return (List<T>)callSoap("getSms", () -> soapSvc.getSms(idLst));
			case DF: return (List<T>)callSoap("getDeviceFontSets",	() -> soapSvc.getDeviceFontSets(idLst));
			case FD: return (List<T>)callSoap("getFontDescs",		() -> soapSvc.getFontDescs(idLst));
			case FS: return (List<T>)callSoap("getFontSpacings",	() -> soapSvc.getFontSpacings(idLst));
			case ZS: return (List<T>)callSoap("getZss", () -> soapSvc.getZss(idLst));
			case TS: return (List<T>)callSoap("getTss", () -> soapSvc.getTss(idLst));
			case CL: return (List<T>)callSoap("getLocks", () -> soapSvc.getLocks(idLst));
			case MU: return (List<T>)callSoap("getMunits", () -> soapSvc.getMunits(idLst));
			default: return new ArrayList<>();
		}
	}
	<T> List<T> grpcDictType( CfgType type, Collection<String> ids ) {
		IdList idLst = cnv.toProto(toSet(ids));
		switch(type) {
			case EQ: return (List<T>)callGrpc("getEqs", () -> cnv.dq().toPojoW(grpcSvc.getEqs(idLst)).getQ());
			case UQ: return (List<T>)callGrpc("getUqs", () -> cnv.dq().toPojoW(grpcSvc.getUqs(idLst)).getQ());
			case AQ: return (List<T>)callGrpc("getAqs", () -> cnv.dq().toPojoW(grpcSvc.getAqs(idLst)).getQ());
			case VQ: return (List<T>)callGrpc("getVqs", () -> cnv.dq().toPojoW(grpcSvc.getVqs(idLst)).getQ());
			case XQ: return (List<T>)callGrpc("getXqs", () -> cnv.dq().toPojoW(grpcSvc.getXqs(idLst)).getQ());
			case SM: return (List<T>)callGrpc("getSms", () -> cnv.dq().toPojoW(grpcSvc.getSms(idLst)).getQ());
			case DF: return (List<T>)callGrpc("getDeviceFontSets",	() -> cnv.dfs().toPojoW(grpcSvc.getDeviceFontSets(idLst)).getDeviceFontSet());
			case FD: return (List<T>)callGrpc("getFontDescs",		() -> cnv.fd().toPojoW(grpcSvc.getFontDescs(idLst)).getFont());
			case FS: return (List<T>)callGrpc("getFontSpacings",	() -> cnv.fs().toPojoW(grpcSvc.getFontSpacings(idLst)).getFontSpacing());
			case ZS: return (List<T>)callGrpc("getZss", () -> cnv.zs().toPojoW(grpcSvc.getZss(idLst)).getZs());
			case TS: return (List<T>)callGrpc("getTss", () -> cnv.ts().toPojoW(grpcSvc.getTss(idLst)).getTs());
			case CL: return (List<T>)callGrpc("getLocks", () -> cnv.cl().toPojoW(grpcSvc.getLocks(idLst)).getLock());
			case MU: return (List<T>)callGrpc("getMunits", () -> cnv.mu().toPojoW(grpcSvc.getMunits(idLst)).getMunit());
			default: return new ArrayList<>();
		}
	}
	// --------------------------------------------------------------------------------------------------
	List<CfgQ> soapEqs( Collection<String> ids ) throws SiteConfigError {
		return callSoap("getEqs", () -> soapSvc.getEqs(toLst(ids)));
	}
	List<CfgQ> grpcEqs( Collection<String> ids ) {
		return callGrpc("getEqs", () -> cnv.dq().toPojoW(grpcSvc.getEqs(cnv.toProto(ids))).getQ());
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
	@FunctionalInterface
	public interface CheckedSupplier<T> {
	   T get() throws SiteConfigError;
	}
	private <T> T callSoap( String label, CheckedSupplier<T> call ) throws SiteConfigError {
		MeasureTime.enter(String.format("%s.soap", label));
		T result = call.get();
		MeasureTime.leave(String.format("%s.soap", label));
		return result;
	}
	private <T> T callGrpc( String label, Supplier<T> call ) {
		return call(label, "grpc", call);
	}
	private <T> T call( String label, String type, Supplier<T> call ) {
		MeasureTime.enter(String.format("%s.%s", label, type));
		T result = call.get();
		MeasureTime.leave(String.format("%s.%s", label, type));
		return result;
	}
	// --------------------------------------------------------------------------------------------------
}
