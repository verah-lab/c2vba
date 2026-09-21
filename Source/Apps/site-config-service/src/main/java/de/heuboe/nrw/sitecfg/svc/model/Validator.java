package de.heuboe.nrw.sitecfg.svc.model;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.heuboe.nrw.guisvc.cfgTypes.ObjectType;
import de.heuboe.nrw.sitecfg.svc.test.CompareServices;
import de.heuboe.nrw.srv.model.util.Tasks;
import de.heuboe.sitecfg.grpc.CfgType;
import de.heuboe.sitecfg.grpc.data.CfgSiteItems;
import de.heuboe.sitecfg.grpc.data.Util;
import de.heuboe.sitesconfig.CfgConfig;
import de.heuboe.sitesconfig.CfgDevice;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgQLoc;
import de.heuboe.sitesconfig.CfgQs;
import de.heuboe.sitesconfig.CfgSite;
import de.heuboe.sitesconfig.CfgView;
import de.heuboe.sitesconfig.CfgViewQ;
import de.heuboe.sitesconfig.CfgViewQs;
import de.heuboe.zst.model.ICfgModel;
import de.heuboe.zst.model.ICfgModel.Type;
import de.heuboe.zst.model.IObjHandler;
import lombok.extern.slf4j.Slf4j;
import multimap.MultiMap.HSet;
import progress.ProgressObserverAdapter;
import stageGraph.util.IStreaming;
import stageGraph.util.MeasureTime;

@Slf4j
public class Validator implements IStreaming {
	// ------------------------------------------------------------------------------------------------------------------------------
	public final static boolean isCluster( CfgDevice device ) {
		return "CLUSTER".equals(device.getType());
	}
	protected AtomicBoolean					cfgModelOk		= new AtomicBoolean(false);
	protected AtomicBoolean					cfgLoading		= new AtomicBoolean(false);
	protected AtomicBoolean					validating		= new AtomicBoolean(false);
	protected Stack<String>					loadConfigJobs	= new Stack<>();
	protected Stack<String>					validationJobs	= new Stack<>();
	// -----------------------------------------------------------------------------------------------------------------------------
	protected final Model					model;
	protected final ICfgModel				cfgModel;
	protected final CompareServices			compareServices;
	protected final boolean					propagateInvalidView;
	protected final boolean					propagateInvalidSite;
	protected final boolean					onlyCfg;
	protected final boolean					onlyXml;
	protected final boolean					likeSoap;
	protected final Set<String>				qsOnlyXml		= new TreeSet<>();
	protected final Map<String, String>		qDevsDiffCnt	= new TreeMap<>();
	protected final HSet<String, String>	qDevsOnlyXml	= new HSet<>();
	protected final HSet<String, String>	qDevsOnlyCfg	= new HSet<>();
	protected final HSet<String, String>	qClusOnlyXml	= new HSet<>();
	protected final HSet<String, String>	qClusOnlyCfg	= new HSet<>();
	// -----------------------------------------------------------------------------------------------------------------------------
	public Validator( Model model, ICfgModel cfgModel, CompareServices compareServices, boolean propagateInvalidView, boolean propagateInvalidSite, boolean onlyCfg, boolean onlyXml, boolean likeSoap ) {
		this.model = model;
		this.cfgModel = cfgModel;
		this.compareServices = compareServices;
		this.propagateInvalidView = propagateInvalidView;
		this.propagateInvalidSite = propagateInvalidSite;
		this.onlyCfg = onlyCfg;
		this.onlyXml = onlyXml;
		this.likeSoap = likeSoap;
		loadConfig("initial");
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public void loadConfig( String jobName ) {
		if( cfgLoading.get() ) {
			loadConfigJobs.push(jobName);
		} else {
			doLoadConfig(jobName);
		}
	}
	protected void doLoadConfig( String jobName ) {
		cfgLoading.set(true);
		String msg = String.format("**** Loading CfgModel (%s)", jobName);
		log.info(msg);
		MeasureTime.enter("Validator.doLoadConfig");
		cfgModel.reload(new ProgressObserverAdapter() {
			@Override
			public void taskFinished( String task, boolean success ) {
				if( Tasks.LOAD_PROGENIES.equals(task) ) {
					MeasureTime.leave("Validator.doLoadConfig");
					cfgLoading.set(false);
					cfgModelOk.set(!cfgModel.getTypedObjects(ObjectType.AQ).isEmpty());
					if( cfgModelOk.get() ) {
						log.info(msg + " done");
						validate(jobName);
						if(!loadConfigJobs.isEmpty() ) {
							doLoadConfig(loadConfigJobs.pop());
						}
					} else {
						log.warn(msg + " failed");
						new Timer().schedule(new TimerTask() {
							@Override
							public void run() {
								loadConfig("repeat after failure");
							}
						}, 10000);
					}
				}
			}
		});
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	public void validate( String jobName ) {
		if( validating.get() ) {
			validationJobs.push(jobName);
		} else {
			doValidate(jobName);
		}
	}
	protected void doValidate( String jobName ) {
		validating.set(true);
		log.info("**** Validating site config (" + jobName + ")");
		verify();
		log.info("**** Validating site config (" + jobName + ") done");
		if( compareServices != null ) {
			log.info("**** Comparing responses of all requests to those of the service version publishing a soap iface");
			compareServices.start(model);
			log.info("**** Comparing responses done");
		}
		validating.set(false);
		if(!validationJobs.isEmpty() ) {
			doValidate(validationJobs.pop());
		}
	}
	protected void verify() {
		lookForDoubles(ObjectType.LVEQ, ObjectType.LVE_SENSOR, ObjectType.AQ, ObjectType.WZG, ObjectType.UFD_SENSOR, ObjectType.VLT_SENSOR);
		Collection<IObjHandler> eqHdls = cfgModel.getTypedObjects(ObjectType.LVEQ).values();
		Collection<IObjHandler> aqHdls = cfgModel.getTypedObjects(ObjectType.AQ).values();
		model.afterCreate(eqHdls, aqHdls);
		verifyAllQs();
        checkViewAndSiteValidity(propagateInvalidView, propagateInvalidSite, onlyCfg, onlyXml, false, likeSoap);
        checkViewAndSiteValidity(false, false, true, true, true, likeSoap);
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	private void lookForDoubles( ObjectType... objTypes ) {
		lookForDoubles("NodeNumDeFg", IObjHandler::getTlsId, objTypes);
		lookForDoubles("PermanentId", IObjHandler::getId, objTypes);
		lookForDoubles("PermanentId", IObjHandler::getId, ObjectType.S_ST);
	}
	private void lookForDoubles( String label, Function<IObjHandler, String> keyGetter, ObjectType... objTypes ) {
		StringBuilder sb = new StringBuilder(String.format("%ncheck for duplicate %s IDs {%n", label));
		int len = sb.length();
		IObjHandler.IdMap idHdlMap = new IObjHandler.IdMap();
		for( ObjectType objType : objTypes ) {
			for( IObjHandler curHdl : cfgModel.getTypedObjects(objType).values() ) {
				String key = keyGetter.apply(curHdl);
				if(!key.isEmpty() ) {
					if( idHdlMap.containsKey(key) ) {
						IObjHandler othHdl = idHdlMap.get(key);
						sb.append(String.format(" key:'%s'%n", key));
						sb.append(String.format(" old: pid:'%s', tls:'%s', name:'%s', dbId:'%s'%n", othHdl.getId(), othHdl.getTlsId(), othHdl.getName(), othHdl.getDbId()));
						sb.append(String.format(" new: pid:'%s', tls:'%s', name:'%s', dbId:'%s'%n", curHdl.getId(), curHdl.getTlsId(), curHdl.getName(), curHdl.getDbId()));
					} else {
						idHdlMap.put(key, curHdl);
					}
				}
			}
		}
		if( len < sb.length() ) {
			log.warn(String.format("%s}", sb.substring(0, sb.length() - 1)));
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	private void verifyAllQs() {
		StringBuilder sb = new StringBuilder("verifyQs {");
		for( String jarName : model.idSet(CfgType.JAR) ) {
			CfgSiteItems si = model.get(CfgType.SI, jarName);
			if( jarName.contains("LVR-SWA_A555_AK_Koeln") ) {
				log.trace("");
			}
			verifyQs(CfgType.EQ, jarName, sb, new IdMap<>(si.getEqs().getQ(), CfgQ::getId), ObjectType.LVEQ, ObjectType.LVE_SENSOR);
			verifyQs(CfgType.AQ, jarName, sb, new IdMap<>(si.getAqs().getQ(), CfgQ::getId), ObjectType.AQ, ObjectType.WZG);
			verifyQs(CfgType.XQ, jarName, sb, new IdMap<>(si.getXqs().getQ(), CfgQ::getId), ObjectType.LSA_DEV, ObjectType.LSA_DEV);
			verifyQs(CfgType.UQ, jarName, sb, new IdMap<>(si.getUqs().getQ(), CfgQ::getId), ObjectType.UFD_SENSOR, null);
			verifyQs(CfgType.VQ, jarName, sb, new IdMap<>(si.getVqs().getQ(), CfgQ::getId), ObjectType.VLT_SENSOR, null);
			verifyQs(CfgType.SM, jarName, sb, new IdMap<>(si.getSms().getQ(), CfgQ::getId), ObjectType.S_ST, null);
		}
//		log.error(sb.append(String.format("%n}")).toString());
		IdMap<CfgQ> allQs = model.idMap(CfgType.QQ);
		log.info("Number of all Qs " + allQs.keySet().size());
		log.info("Counted Qs with non-null cfgQ " + allQs.values().stream().filter(q -> !q.getCfgQ().isEmpty()).count());
	}
	private void verifyQs( CfgType type, String jarName, StringBuilder sb, IdMap<CfgQ> qXmlMap, ObjectType qType, ObjectType dType ) {
		IObjHandler.IdMap qHdlMap = cfgModel.getTypedObjects(qType);
//		IObjHandler.IdMap cHdlMap = cfgModel.getTypedObjects(de.heuboe.zst.model.type.ObjectType.TLS_CLUSTER);
		for( CfgQ q : qXmlMap.values() ) {
			String qId = q.getId();
			if( "Q-11227-2-196-4".equals(qId) ) {
				log.trace("verifyQ " + qId);
			}
			if( !qHdlMap.containsKey(qId) ) {
				qsOnlyXml.add(jarName + qId);
				sb.append(String.format("%n Didn't find %s '%s' in cfgService", qType.name(), qId));
				q.setValid(false);
			} else {
				IObjHandler qHdl = qHdlMap.get(qId);
				IdMap<CfgDevice> cXmlMap = new IdMap<>(fltToLst(q.getDevice(), d ->  isCluster(d)), CfgDevice::getId);
				IdMap<CfgDevice> dXmlMap = new IdMap<>(fltToLst(q.getDevice(), d -> !isCluster(d)), CfgDevice::getId);
				if( !dXmlMap.isEmpty() ) {
					if( qType == dType ) {
						analyzeXmlDevices(jarName, q, dXmlMap, qHdlMap, sb);
					} else {
						IObjHandler.IdMap dHdlMap = qHdl.getChildMap(dType);
						if( dHdlMap.size() < dXmlMap.size() ) {
							qDevsDiffCnt.put(jarName + qId, String.format("dev cnt DB:%2d != XML:%2d", dHdlMap.size(), dXmlMap.size()));
//							sb.append(String.format("%n device cnt DB:%2d != XML:%2d for Q '%s' in file '%s'", dHdlMap.size(), dXmlMap.size(), qId, jarName));
							q.setValid(false);
						}
						analyzeXmlDevices(jarName, q, dXmlMap, dHdlMap, sb);
						analyzeCfgDevices(jarName, q, dXmlMap, dHdlMap, sb);
						CfgQLoc qLoc = q.getLocation();
						if( qLoc != null ) {
							String mXml = String.valueOf(qLoc.getBabm());
							String mHdl = qHdl.getMeter();
							if(!mXml.equals(mHdl) && cfgModel.getType() != Type.CHB ) {
								int babmXml = qLoc.getBabm();
								int babmHdl = Util.s2i(mHdl);
								if( mHdl.startsWith("-") && babmXml > 0 ) {
									babmXml *= -1;
								}
								qLoc.setBabm(babmHdl);
								Util.s2i(mHdl);
							}
						}
					}
				}
				if( !cXmlMap.isEmpty() && ObjectType.AQ == qType && cfgModel.getType() == Type.CHB ) {
					String cl4Type = de.heuboe.zst.base.utils.Util.e2s(de.heuboe.zst.model.type.ObjectType.TLS_CLUSTER_AQ);
					List<String> cHdlIds = fltMapToLst(qHdl.getChildRefs(), ref -> ref.getType().equals(cl4Type), ref -> ref.getId());
					if( cHdlIds.size() != cXmlMap.size() ) {
						q.setValid(false);
					}
					analyzeXmlClusters(jarName, q, cXmlMap, cHdlIds, sb);
					analyzeCfgClusters(jarName, q, cXmlMap, cHdlIds, sb);
				}
			}
		}
	}
	private void analyzeXmlDevices( String jarName, CfgQ q, IdMap<CfgDevice> dXmlMap, IObjHandler.IdMap dHdlMap, StringBuilder sb ) {
		for( String devId : dXmlMap.keySet() ) {
			if( !dHdlMap.containsKey(devId) ) {
				sb.append(String.format("%n XML device '%s' of Q '%s' in file '%s' missing in DB", devId, q.getId(), jarName));
				q.setValid(false);
				dXmlMap.get(devId).setValid(false);
				qDevsOnlyXml.add(jarName + q.getId(), devId);
			}
		}
	}
	private void analyzeCfgDevices( String jarName, CfgQ q, IdMap<CfgDevice> dXmlMap, IObjHandler.IdMap dHdlMap, StringBuilder sb ) {
		for( String devId : dHdlMap.keySet() ) {
			if( !dXmlMap.containsKey(devId) ) {
				sb.append(String.format("%n  DB device '%s' of Q '%s' in file '%s' missing in XML", devId, q.getId(), jarName));
//				q.setValid(false);
				qDevsOnlyCfg.add(jarName + q.getId(), devId);
			}
		}
	}
	private void analyzeXmlClusters( String jarName, CfgQ q, IdMap<CfgDevice> dXmlMap, Collection<String> cHdlIds, StringBuilder sb ) {
		for( String devId : dXmlMap.keySet() ) {
			if( !cHdlIds.contains(devId) ) {
				sb.append(String.format("%n XML cluster '%s' of Q '%s' in file '%s' missing in DB", devId, q.getId(), jarName));
				q.setValid(false);
				dXmlMap.get(devId).setValid(false);
				qClusOnlyXml.add(jarName + q.getId(), devId);
			}
		}
	}
	private void analyzeCfgClusters( String jarName, CfgQ q, IdMap<CfgDevice> dXmlMap, Collection<String> cHdlIds, StringBuilder sb ) {
		for( String devId : cHdlIds ) {
			if( !dXmlMap.containsKey(devId) ) {
				sb.append(String.format("%n  DB cluster '%s' of Q '%s' in file '%s' missing in XML", devId, q.getId(), jarName));
				q.setValid(false);
				qClusOnlyCfg.add(jarName + q.getId(), devId);
			}
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
	private void checkViewAndSiteValidity( boolean propagateInvalidView, boolean propagateInvalidSite, boolean onlyCfg, boolean onlyXml, boolean toLog, boolean likeSoap ) {
		StringBuilder sbErrView = new StringBuilder("invalid files, sites and views");
		StringBuilder sbErrSite = new StringBuilder("invalid files and sites");
		StringBuilder sbLog = new StringBuilder("\ncheck validity of files, sites and views");
		IdMap<CfgConfig> configMap = model.idMap(CfgType.JAR);
		for( String jarName : configMap.keySet() ) {
			boolean validView = true, validSite = true;
			StringBuilder sbView = new StringBuilder(String.format("%n check file '%s.jar'", jarName));
			StringBuilder sbSite = new StringBuilder(String.format("%n check file '%s.jar'", jarName));
			for( CfgSite site : configMap.get(jarName).getSite() ) {
				TypeIdObjMap qMap = model;
				if( likeSoap ) {
					jarName = "";
				} else {
					CfgSiteItems si = model.get(CfgType.SI, jarName);
					qMap = new TypeIdObjMap();
					qMap.add(CfgType.EQ, si.getEqs(), CfgQs::getQ);
					qMap.add(CfgType.UQ, si.getUqs(), CfgQs::getQ);
					qMap.add(CfgType.AQ, si.getAqs(), CfgQs::getQ);
					qMap.add(CfgType.XQ, si.getXqs(), CfgQs::getQ);
				}
				HSet<CfgType, String> qTypeIdsSite = new HSet<>();
				sbView.append(String.format("%n  check site '%s'", site.getDesc().getId()));
				for( CfgView view : site.getViews().getView() ) {
					sbView.append(String.format("%n   check view '%s'", view.getDesc().getId()));
					int lenView = sbView.length();
					checkViewQs(qTypeIdsSite, jarName, qMap, CfgType.EQ, sbView, onlyCfg, onlyXml, view.getEqs());
					checkViewQs(qTypeIdsSite, jarName, qMap, CfgType.UQ, sbView, onlyCfg, onlyXml, view.getUqs());
					checkViewQs(qTypeIdsSite, jarName, qMap, CfgType.AQ, sbView, onlyCfg, onlyXml, view.getAqs());
					checkViewQs(qTypeIdsSite, jarName, qMap, CfgType.XQ, sbView, onlyCfg, onlyXml, view.getXqs());
					if( lenView < sbView.length() ) {
						if( propagateInvalidView ) {
							view.getDesc().setValid(false);
						}
						if( propagateInvalidSite ) {
							site.getDesc().setValid(false);
						}
						validView = false;
					}
				}
				sbSite.append(String.format("%n  check site '%s'", site.getDesc().getId()));
				int lenSite = sbSite.length();
				checkSiteQs(qTypeIdsSite, jarName, qMap, CfgType.EQ, sbSite, onlyCfg, onlyXml);
				checkSiteQs(qTypeIdsSite, jarName, qMap, CfgType.UQ, sbSite, onlyCfg, onlyXml);
				checkSiteQs(qTypeIdsSite, jarName, qMap, CfgType.AQ, sbSite, onlyCfg, onlyXml);
				checkSiteQs(qTypeIdsSite, jarName, qMap, CfgType.XQ, sbSite, onlyCfg, onlyXml);
				if( lenSite < sbSite.length() ) {
					validSite = false;
				}
			}
			sbLog.append(sbView.toString());
			if(!validView ) {
				sbErrView.append(sbView.toString());
			}
			sbLog.append(sbSite.toString());
			if(!validSite ) {
				sbErrSite.append(sbSite.toString());
			}
		}
		if( toLog ) {
			sbErrSite.append(model.multiFileItems().printTypes(0, "Catalogue items in multiple jar files:", "%-30s in jars[%s]", CfgType.EQ, CfgType.UQ, CfgType.AQ, CfgType.VQ, CfgType.XQ));
//			log.info(sbLog.toString());
			model.save("invalidJars.txt", sbErrView.toString(), Charset.forName("CP1252"));
			model.save("invalidJarsCore.txt", sbErrSite.toString(), Charset.forName("CP1252"));
		}
	}
	private void checkViewQs( HSet<CfgType, String> qIdSetSite, String jarName, TypeIdObjMap qMap, CfgType qType, StringBuilder sb, boolean onlyCfg, boolean onlyXml, CfgViewQs viewQs ) {
		if( viewQs != null ) {
			Collection<String> ids = viewQs.getQ().stream().map(CfgViewQ::getId).collect(Collectors.toList());
			qIdSetSite.add(qType, ids);
			checkQs(ids, jarName, qMap, qType, sb, onlyCfg, onlyXml);
		}
	}
	private void checkSiteQs( HSet<CfgType, String> qIdSetSite, String jarName, TypeIdObjMap qMap, CfgType qType, StringBuilder sb, boolean onlyCfg, boolean onlyXml ) {
		qIdSetSite.cnt(qType);
		checkQs(qIdSetSite.get(qType), jarName, qMap, qType, sb, onlyCfg, onlyXml);
	}
	private void checkQs( Collection<String> ids, String jarName, TypeIdObjMap qMap, CfgType qType, StringBuilder sb, boolean onlyCfg, boolean onlyXml ) {
		IdMap<CfgQ> qXmlMap = qMap.idMap(qType);
		for( String id : ids ) {
			if( qXmlMap.containsKey(id) ) {
				if(!qXmlMap.get(id).isValid() ) {
					String jarId = jarName + id;
					StringBuilder sbDev = new StringBuilder();
					if( onlyCfg && qDevsOnlyCfg.containsKey(jarId) ) {
						sbDev.append(String.format("%n     db devices missing in xml: [%s]", String.join(", ", qDevsOnlyCfg.all(jarId))));
					}
					if( onlyXml && qDevsOnlyXml.containsKey(jarId) ) {
						sbDev.append(String.format("%n     xml devices missing in db: [%s]", String.join(", ", qDevsOnlyXml.all(jarId))));
					}
					if( onlyCfg && qClusOnlyCfg.containsKey(jarId) ) {
						sbDev.append(String.format("%n     db clusters missing in xml: [%s]", String.join(", ", qClusOnlyCfg.all(jarId))));
					}
					if( onlyXml && qClusOnlyXml.containsKey(jarId) ) {
						sbDev.append(String.format("%n     xml clusters missing in db: [%s]", String.join(", ", qClusOnlyXml.all(jarId))));
					}
					if( onlyXml && qsOnlyXml.contains(jarId) || sbDev.length() > 0 ) {
						sb.append(String.format("%n    references invalid %s '%s'", qType.name(), id));
						sb.append(sbDev.toString());
					}
				}
			}
		}
	}
	// -----------------------------------------------------------------------------------------------------------------------------
}
