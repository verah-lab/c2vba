package de.heuboe.nrw.sitecfg.svc.model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXB;

import de.heuboe.sitecfg.grpc.CfgType;
import de.heuboe.sitecfg.grpc.data.CfgBabSeg;
import de.heuboe.sitecfg.grpc.data.CfgLocSegs;
import de.heuboe.sitecfg.grpc.data.CfgRoadSegKey;
import de.heuboe.sitecfg.grpc.data.CfgRoadSegStyle;
import de.heuboe.sitecfg.grpc.data.CfgSiteFonts;
import de.heuboe.sitecfg.grpc.data.CfgSiteItems;
import de.heuboe.sitecfg.grpc.data.FileSitesDescs;
import de.heuboe.sitecfg.grpc.data.SiteViewsDescs;
import de.heuboe.sitesconfig.CfgAggrDesc;
import de.heuboe.sitesconfig.CfgBabSegDesc;
import de.heuboe.sitesconfig.CfgConfig;
import de.heuboe.sitesconfig.CfgDeviceFontSets;
import de.heuboe.sitesconfig.CfgDkSet;
import de.heuboe.sitesconfig.CfgFontDescs;
import de.heuboe.sitesconfig.CfgFontSpacings;
import de.heuboe.sitesconfig.CfgMunits;
import de.heuboe.sitesconfig.CfgPixSegDesc;
import de.heuboe.sitesconfig.CfgQs;
import de.heuboe.sitesconfig.CfgSitClass;
import de.heuboe.sitesconfig.CfgSitClassValue;
import de.heuboe.sitesconfig.CfgSitDisplayType;
import de.heuboe.sitesconfig.CfgSite;
import de.heuboe.sitesconfig.CfgSiteDesc;
import de.heuboe.sitesconfig.CfgTSs;
import de.heuboe.sitesconfig.CfgView;
import de.heuboe.sitesconfig.CfgViewDesc;
import de.heuboe.sitesconfig.CfgViewType;
import de.heuboe.sitesconfig.CfgWzgCodeLocks;
import de.heuboe.sitesconfig.CfgZSs;
import de.heuboe.sitesconfig.QType;
import lombok.extern.slf4j.Slf4j;
import resServer.ResServer;
import stageGraph.util.JUtil;

@Slf4j
@SuppressWarnings("serial")
public class CreatorFile extends Model {
	// -----------------------------------------------------------------------------------------------------------------------------
	private static final String		CONFIG_KEY	= "Config";
	// private static final String SVG_PATH = "/svg/";
	// private static final String SCHEMA_PATH = "/xsd/Config.xsd";
	// -----------------------------------------------------------------------------------------------------------------------------
	private final String			repSites;
	private boolean					logRoadSegSvgs	= false;
	private boolean					saveRoadSegSvgs	= false;
	// -----------------------------------------------------------------------------------------------------------------------------
	public CreatorFile( String repRoot, String repSites, boolean logRoadSegSvgs, boolean saveRoadSegSvgs ) {
		super(repRoot);
		this.repSites = repSites;
		this.logRoadSegSvgs = logRoadSegSvgs;
		this.saveRoadSegSvgs = saveRoadSegSvgs;
		log.info("CreatorFile.ctor");
		if( repRoot == null || repRoot.isEmpty() ) {
			repRoot = String.format("%s/unzipped", repSites);
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	@Override
	protected void loadCfg() {
		log.info("CreatorFile.loadCfg()");
		if( repSites != null && !repSites.isEmpty() ) {
			File dir = new File(repSites);
			if( dir.exists() ) {
				for( File jarFile : dir.listFiles(new FileExtFilter("jar")) ) {
					try {
						extractJar(jarFile);
					} catch( IOException e ) {
						log.error(String.format("failed to extract jar '%s'", jarFile.getAbsolutePath()));
					}
				}
			} else {
				log.error(String.format("bad path for site config jars provided: '%s'", dir.getAbsolutePath()));
			}
		} else {
			log.error("no path for site config jars provided");
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	@SuppressWarnings("resource")
	private void extractJar( File jar ) throws IOException {
		boolean hasSvg = false;
		boolean hasXml = false;
		String jarName = jar.getName();
		if( jarName.endsWith(".jar") ) {
			jarName = jarName.substring(0, jarName.length() - 4);
		}
		String rootPath = String.format("%s/%s", repRoot, jarName);
		new File(rootPath).mkdirs();
		JarFile jarFile = new JarFile(jar);
		java.util.Enumeration<JarEntry> enu = jarFile.entries();
		while( enu.hasMoreElements() ) {
			JarEntry je = enu.nextElement();
			String path = String.format("%s/%s", rootPath, je.getName());
			if( je.isDirectory() ) {
				new File(path).mkdirs();
			} else {
				hasSvg = hasSvg || je.getName().toLowerCase().endsWith(".svg");
				hasXml = hasXml || je.getName().toLowerCase().endsWith(".xml");
				ResServer.copyFile(jarFile.getInputStream(je), new File(path));
			}
		}
		if( hasXml ) {
			File dir = new File(rootPath);
			loadDir(dir);
			String xmlName = jarFile.getManifest().getMainAttributes().getValue(CONFIG_KEY);
			ZipEntry ze = jarFile.getEntry(xmlName);
			CfgConfig cfg = JAXB.unmarshal(jarFile.getInputStream(ze), CfgConfig.class);
			add(CfgType.JAR, jarName, cfg);
			if( !isTemplate(jarName) ) {
				loadXml(jarName, cfg);
				loadSit(jarName, cfg);
			}
		}
	}
	private void loadDir( File dir ) {
		for( File file : dir.listFiles() ) {
			if( file.isDirectory() ) {
				loadDir(file);
			} else {
				String path = file.getAbsolutePath().replaceAll("\\\\", "/");
				String name = file.getName();
				try {
					if( name.endsWith(".svg") ) {
						String svgId = name.substring(0, name.length() - 4);
						add(CfgType.SVG_PS, svgId, new SvgParser(svgId, new FileInputStream(file)));
						StringBuilder sb = new StringBuilder();
						if( ResServer.readFile(file.getAbsolutePath(), sb) ) {
							add(CfgType.SVG, svgId, sb.toString());
						}
					}
				} catch( Exception ee ) {
					log.error(String.format("error reading svg file '%s'", path));
				}
				try {
					if( name.endsWith(".png") ) {
						int pos = path.indexOf("/png/");
						if( pos > 0 ) {
							String pngId = path.substring(pos + 5, path.length() - 4);
							pngId = pngId.replaceAll("/", ".");
							BufferedImage image = ImageIO.read(file);
							ByteArrayOutputStream bytes = new ByteArrayOutputStream();
							ImageIO.write(image, "png", bytes);
							add(CfgType.PNG, pngId, bytes.toByteArray());
						}
					}
				} catch( Exception ee ) {
					log.error(String.format("error reading png file '%s': '%s'", path, ee.toString()), ee);
				}
			}
		}
	}
	protected void loadXml( String jarName, CfgConfig cfg ) {
		if( jarName.contains("LVR-AM_Bonn") ) {
			log.trace("");
		}
		FileSitesDescs fileSitesDescs = new FileSitesDescs(jarName);
		List<CfgDkSet> dkSets = cfg.getDkSets().getDkSet();
		add(CfgType.FSD, jarName, fileSitesDescs);
		add(CfgType.DKS, jarName, dkSets);
		add(CfgType.SF, jarName, new CfgSiteFonts(jarName, cfg));
		add(CfgType.SI, jarName, new CfgSiteItems(jarName, cfg));
		for( CfgSite site : cfg.getSite() ) {
			CfgSiteDesc siteDesc = site.getDesc();
			String siteId = siteDesc.getId();
			SiteViewsDescs siteViewsDescs = new SiteViewsDescs(siteDesc);
			fileSitesDescs.add(siteViewsDescs);
			add(CfgType.SD, siteDesc);
			add(CfgType.SV, siteViewsDescs);
			add(CfgType.SF, siteId, new CfgSiteFonts(siteId, cfg));
			add(CfgType.SI, siteId, new CfgSiteItems(siteId, cfg));
			add(CfgType.DKS, siteId, dkSets);
			add(siteId, siteDesc.getBabSeg(), siteDesc.getPixSeg());
			for( CfgView view : site.getViews().getView() ) {
				CfgViewDesc viewDesc = view.getDesc();
				CfgViewType viewType = viewDesc.getType();
				add(CfgType.VI, view);
				add(CfgType.VD, viewDesc);
				siteViewsDescs.add(viewDesc);
				String viewId = viewDesc.getId();
				String svgId = viewDesc.getSvgLayout();
				if( has(CfgType.SVG_PS, svgId) ) {
					SvgParser p = get(CfgType.SVG_PS, svgId);
					view.getAqs().getQ().forEach(viewQ -> {
						String aqId = viewQ.getId();
						add(CfgType.SVG_AQ, cnv.joinIds(svgId, aqId), p.getAQasSVG(aqId));
					});
				}
				if( canHaveSegs(viewType) ) {
					add(cnvLogSegs.key(siteId, viewId), viewDesc.getBabSeg(), viewDesc.getPixSeg());
					add(cnvLogSegs.key(siteId, viewType.name()), viewDesc.getBabSeg(), viewDesc.getPixSeg());
				}
				addAggr(viewId, QType.AQ, view.getAqs().getAggr());
			}
		}
		add(CfgType.EQ, cfg.getEqs(), CfgQs::getQ);
		add(CfgType.UQ, cfg.getUqs(), CfgQs::getQ);
		add(CfgType.AQ, cfg.getAqs(), CfgQs::getQ);
		add(CfgType.VQ, cfg.getVqs(), CfgQs::getQ);
		add(CfgType.XQ, cfg.getXqs(), CfgQs::getQ);
		add(CfgType.SM, cfg.getSms(), CfgQs::getQ);
		add(CfgType.TS, cfg.getTss(), CfgTSs::getTs);
		add(CfgType.ZS, cfg.getZss(), CfgZSs::getZs);
		add(CfgType.MU, cfg.getMunits(), CfgMunits::getMunit);
		add(CfgType.DF, cfg.getDeviceFontSets(), CfgDeviceFontSets::getDeviceFontSet);
		add(CfgType.FD, cfg.getFontDescs(), CfgFontDescs::getFont);
		add(CfgType.FS, cfg.getFss(), CfgFontSpacings::getFontSpacing);
		add(CfgType.CL, cfg.getLocks(), CfgWzgCodeLocks::getLock);
	}
	protected void addAggr( String viewId, QType type, List<CfgAggrDesc> aggrDescLst ) {
		if(!aggrDescLst.isEmpty() ) {
			add(CfgType.AGGR, key(viewId, type), aggrDescLst);
		}
	}
	protected void loadSit( String jarName, CfgConfig cfg ) {
		if( cfg.getSitDisplayTypes() != null && cfg.getSitClasses() != null ) {
			List<CfgSitDisplayType> sitDisplayTypes = cfg.getSitDisplayTypes().getType();
			List<CfgSitClass> sitClasses = cfg.getSitClasses().getSitClass();
			addSit(jarName, sitDisplayTypes, sitClasses);
			cfg.getSite().forEach(site -> addSit(site.getDesc().getId(), sitDisplayTypes, sitClasses));
		}
	}
	protected void addSit( String key, List<CfgSitDisplayType> sitDisplayTypes, List<CfgSitClass> sitClasses ) {
		add(CfgType.SIT_TYP, key, sitDisplayTypes);
		add(CfgType.SIT_CLS, key, sitClasses);
		Map<String, CfgSitDisplayType> sitIdTypeMap = JUtil.toMap(sitDisplayTypes, v -> v.getId(), v -> v);
		for( CfgSitClass sitClass : sitClasses ) {
			for( CfgSitClassValue sitValue : sitClass.getValue() ) {
				String sitTypeId = sitClass.getDisplayTypeId();
				String sitValueId = sitValue.getId();
				if( 'd' == sitValueId.charAt(0) && Character.isDigit(sitValueId.charAt(1)) ) {
					sitValue.setId(sitValueId.substring(1));
				}
				if( sitIdTypeMap.containsKey(sitTypeId) ) {
					CfgSitDisplayType sitType = sitIdTypeMap.get(sitTypeId);
					CfgRoadSegKey segKey = new CfgRoadSegKey(key, sitClass.getId(), sitValue.getId(), sitTypeId.startsWith("Msr"));
					CfgRoadSegStyle segStyle = new CfgRoadSegStyle(segKey, sitType, sitClass, sitValue);
//					add(CfgType.SIT_STYLE, cnv.key(segKey), segStyle);
					add(CfgType.SIT_STYLE, segKey.getModelKey(), segStyle);
				}
				valueClassIdMap.put(cnv.joinIds(key, sitValue.getId()), sitClass.getId());
			}
		}
		String svgId = String.format("%s-SymbolDefs", key);
		if( has(CfgType.SVG_PS, svgId) ) {
			SvgParser p = get(CfgType.SVG_PS, svgId);
			List<CfgRoadSegStyle> segStyleLst = all(CfgType.SIT_STYLE);
//			Set<String> symbolIdSet = new TreeSet<>(JUtil.mapToSet(JUtil.fltToLst(segStyleLst, o -> o.hasIcon()), o -> o.getIconId()));
//			symbolIdSet.forEach(symbolId -> createSymbolSvg(p, key, symbolId));
			segStyleLst.forEach(segStyle -> createRoadSegSvg(p, svgId, segStyle));
//			try {
//				String areasSvg = p.createRoadSegSvg(roadSegstyleLst, 2);
//				save("AllRoadSegs.svg", areasSvg, Charset.forName("UTF-8"));
//			} catch( Exception e ) {
//				log.error(String.format("AllRoadSegs() failed: '%s'", key, e.getMessage()), e);
//			}
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	protected void createSymbolSvg( SvgParser p, String svgId, String symbolId ) {
		try {
			String svg = p.createSymbolSvg(symbolId);
			log.info(String.format("createSymbolSvg(%s):%n%s", symbolId, svg));
			save(String.format("%s.svg", symbolId), svg, Charset.forName("UTF-8"));
			add(CfgType.SVG_SYMBOL, cnv.joinIds(svgId, symbolId), svg);
		} catch( Exception e ) {
			log.error(String.format("createSymbolSvg(%s) failed: '%s'", symbolId, e.getMessage()), e);
		}
	}
	protected void createRoadSegSvg( SvgParser p, String svgId, CfgRoadSegStyle segStyle ) {
		CfgRoadSegKey segKey = segStyle.getKey();
//		String key = cnv.key(segKey);
		String key = segKey.getModelKey();
		try {
			String areaSvg = p.createRoadSegSvg(segStyle);
			if( logRoadSegSvgs ) {
				log.info(String.format("createRoadSegSvg(%s):%n%s", key, areaSvg));
			}
			if( saveRoadSegSvgs ) {
				save(String.format("%s-%s.svg", segKey.getClassId(), segKey.getValueId()), areaSvg, Charset.forName("UTF-8"));
			}
			add(CfgType.SVG_SIT, key, areaSvg);
			if( segStyle.hasIcon() ) {
				String symbolId = segStyle.getIconId();
				String iconSvg = segStyle.getIconSvg();
				if( logRoadSegSvgs ) {
					log.info(String.format("createSymbolSvg(%s):%n%s", symbolId, iconSvg));
				}
				save(String.format("%s.svg", symbolId), iconSvg, Charset.forName("UTF-8"));
				add(CfgType.SVG_SYMBOL, cnv.joinIds(svgId, symbolId), iconSvg);
			}
		} catch( Exception e ) {
			log.error(String.format("createRoadSegSvg(%s) failed: '%s'", key, e.getMessage()), e);
		}
	}
	protected void add( String key, List<CfgBabSegDesc> babSegDescs, List<CfgPixSegDesc> pixSegDescs ) {
		if( babSegDescs != null && !babSegDescs.isEmpty() ) {
			List<CfgBabSeg> babSegs = cnvBabSeg.fromPojosX(babSegDescs);
			add(CfgType.SEG, key, babSegs);
			if( pixSegDescs != null && !pixSegDescs.isEmpty() ) {
				CfgLocSegs locSegs = cnvLogSegs.fromPojosX(babSegDescs, pixSegDescs);
				add(CfgType.LOC, key, locSegs);
			}
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	class FileExtFilter implements FilenameFilter {
		String ext;
		FileExtFilter( String ext ) {
			this.ext = String.format(".%s", ext);
		}
		@Override
		public boolean accept( File dir, String name ) {
			return name.endsWith(ext);
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
}
