package de.heuboe.nrw.sitecfg.svc.server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.protobuf.Empty;

import de.heuboe.nrw.sitecfg.svc.model.IdMap;
import de.heuboe.nrw.sitecfg.svc.model.Model;
import de.heuboe.sitecfg.grpc.AggrDescs;
import de.heuboe.sitecfg.grpc.AqSvgId;
import de.heuboe.sitecfg.grpc.AqSvgIds;
import de.heuboe.sitecfg.grpc.AqSvgs;
import de.heuboe.sitecfg.grpc.BabSegs;
import de.heuboe.sitecfg.grpc.CfgMultiFileItems;
import de.heuboe.sitecfg.grpc.CfgType;
import de.heuboe.sitecfg.grpc.DeviceFontSets;
import de.heuboe.sitecfg.grpc.DictQ;
import de.heuboe.sitecfg.grpc.DictQs;
import de.heuboe.sitecfg.grpc.DkSets;
import de.heuboe.sitecfg.grpc.FileSitesDescs;
import de.heuboe.sitecfg.grpc.FontDescs;
import de.heuboe.sitecfg.grpc.FontSpacings;
import de.heuboe.sitecfg.grpc.IdList;
import de.heuboe.sitecfg.grpc.IdRequest;
import de.heuboe.sitecfg.grpc.LocSegs;
import de.heuboe.sitecfg.grpc.LocSegsRequest;
import de.heuboe.sitecfg.grpc.MultiFileItems;
import de.heuboe.sitecfg.grpc.Munits;
import de.heuboe.sitecfg.grpc.PngReply;
import de.heuboe.sitecfg.grpc.RoadSegKey;
import de.heuboe.sitecfg.grpc.RoadSegKeys;
import de.heuboe.sitecfg.grpc.RoadSegStyle;
import de.heuboe.sitecfg.grpc.RoadSegStyles;
import de.heuboe.sitecfg.grpc.SitClasses;
import de.heuboe.sitecfg.grpc.SitDisplayTypes;
import de.heuboe.sitecfg.grpc.SiteConfigServiceGrpc.SiteConfigServiceImplBase;
import de.heuboe.sitecfg.grpc.SiteDescs;
import de.heuboe.sitecfg.grpc.SiteFonts;
import de.heuboe.sitecfg.grpc.SiteItems;
import de.heuboe.sitecfg.grpc.SiteViewsDescs;
import de.heuboe.sitecfg.grpc.SvgReply;
import de.heuboe.sitecfg.grpc.SymbolSvgId;
import de.heuboe.sitecfg.grpc.TsList;
import de.heuboe.sitecfg.grpc.View;
import de.heuboe.sitecfg.grpc.ViewAggrDescs;
import de.heuboe.sitecfg.grpc.ViewAggrRequest;
import de.heuboe.sitecfg.grpc.ViewDescs;
import de.heuboe.sitecfg.grpc.WzgCodeLocks;
import de.heuboe.sitecfg.grpc.XmlProtoConverter;
import de.heuboe.sitecfg.grpc.XmlProtoConverter.CnvLocSegs;
import de.heuboe.sitecfg.grpc.ZsList;
import de.heuboe.sitecfg.grpc.data.CfgAqSvgs;
import de.heuboe.sitecfg.grpc.data.CfgBabSeg;
import de.heuboe.sitecfg.grpc.data.CfgLocSegs;
import de.heuboe.sitecfg.grpc.data.CfgRoadSegStyle;
import de.heuboe.sitecfg.grpc.data.CfgSiteFonts;
import de.heuboe.sitecfg.grpc.data.CfgSiteItems;
import de.heuboe.sitecfg.grpc.data.CfgViewAggrDesc;
import de.heuboe.sitesconfig.CfgAggrDesc;
import de.heuboe.sitesconfig.CfgDeviceFontSet;
import de.heuboe.sitesconfig.CfgDkSet;
import de.heuboe.sitesconfig.CfgFontDesc;
import de.heuboe.sitesconfig.CfgFontSpacing;
import de.heuboe.sitesconfig.CfgMunit;
import de.heuboe.sitesconfig.CfgQ;
import de.heuboe.sitesconfig.CfgSitClass;
import de.heuboe.sitesconfig.CfgSitDisplayType;
import de.heuboe.sitesconfig.CfgSiteDesc;
import de.heuboe.sitesconfig.CfgTS;
import de.heuboe.sitesconfig.CfgView;
import de.heuboe.sitesconfig.CfgViewDesc;
import de.heuboe.sitesconfig.CfgWzgCodeLock;
import de.heuboe.sitesconfig.CfgZS;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@Slf4j
@GrpcService
public class SiteConfigServer extends SiteConfigServiceImplBase {
	// --------------------------------------------------------------------------------------------------
	private final Model				model;
	private final XmlProtoConverter	cnv			= new XmlProtoConverter();
	private final CnvLocSegs		cnvLogSegs	= cnv.locSegs();
	// --------------------------------------------------------------------------------------------------
	public SiteConfigServer( Model model ) {
		this.model = model;
		log.info("SiteConfigServer.ctor");
	}
	// --------------------------------------------------------------------------------------------------
	private XmlProtoConverter cnv() {
		return cnv;
//		return new XmlProtoConverter();
	}
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private void getDictQs( CfgType type, IdList request, StreamObserver<DictQs> responseObserver ) {
		List<CfgQ> data = model.get(type, cnv().idSet(request));
		send(cnv.dq().toProtoW(data), responseObserver);
	}
	// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	private <P> void check( CfgType type, LocSegsRequest request, StreamObserver<P> responseObserver, Consumer<String> func ) {
		String segsId = cnvLogSegs.toPojo(request);
//		if( cnvLogSegs.hasViewType(request) && model.has(type, segsId) ) {
//			func.accept(segsId);
//		} else if( check(type, request.getSiteId(), responseObserver) ) {
//			func.accept(request.getSiteId());
//		}
		if( check(type, segsId, responseObserver) ) {
			func.accept(segsId);
		}
	}
	private <P> boolean check( CfgType type, IdRequest request, StreamObserver<P> responseObserver ) {
		return check(type, cnv().id(request), responseObserver);
	}
	private <P> boolean check( CfgType type, String id, StreamObserver<P> responseObserver ) {
		if(!model.has(type, id) ) {
			String msg = String.format("unknown id:'%s'", id);
			log.error(msg);
            responseObserver.onError(Status.INTERNAL.withDescription(msg).asRuntimeException());
//			responseObserver.onCompleted();
			return false;
		}	return true;
	}
	private <P> void send( P value, StreamObserver<P> responseObserver ) {
		responseObserver.onNext(value);
		responseObserver.onCompleted();
	}
	// --------------------------------------------------------------------------------------------------
	@Override
	public void getMultiFileItems( Empty request, StreamObserver<MultiFileItems> responseObserver ) {
		CfgMultiFileItems data = model.multiFileItems();
		send(cnv.mfi().toProto(data), responseObserver);
	}
	@Override
	public void getFileNames( Empty request, StreamObserver<IdList> responseObserver ) {
		List<String> data = model.idLst(CfgType.JAR);
		send(cnv().toProto(data), responseObserver);
	}
	@Override
	public void getSiteIds( Empty request, StreamObserver<IdList> responseObserver ) {
		List<String> data = model.idLst(CfgType.SD);
		send(cnv().toProto(data), responseObserver);
	}
	@Override
	public void getViewConfig( IdRequest request, StreamObserver<View> responseObserver ) {
		if( check(CfgType.VI, request, responseObserver) ) {
			CfgView data = model.get(CfgType.VI, cnv().id(request));
			send(cnv.vi().toProto(data), responseObserver);
		}
	}
	@Override
	public void getQData( IdRequest request, StreamObserver<DictQ> responseObserver ) {
		if( check(CfgType.QQ, request, responseObserver) ) {
			CfgQ data = model.get(CfgType.QQ, cnv().id(request));
			send(cnv.dq().toProto(data), responseObserver);
		}
	}
	@Override
	public void getSiteDescs( Empty request, StreamObserver<SiteDescs> responseObserver ) {
		List<CfgSiteDesc> data = model.all(CfgType.SD);
		send(cnv.sd().toProtoW(data), responseObserver);
	}
	@Override
	public void getViewDescs( IdList request, StreamObserver<ViewDescs> responseObserver ) {
		List<CfgViewDesc> data = model.get(CfgType.VD, cnv().idSet(request));
		send(cnv.vd().toProtoW(data), responseObserver);
	}
	@Override
	public void getFileSiteDescs( IdList request, StreamObserver<FileSitesDescs> responseObserver ) {
		List<de.heuboe.sitecfg.grpc.data.FileSitesDescs> data = model.get(CfgType.FSD, cnv().idSet(request));
		send(cnv.fsd().toProtoW(data), responseObserver);
	}
	@Override
	public void getSiteViewDescs( IdList request, StreamObserver<SiteViewsDescs> responseObserver ) {
		List<de.heuboe.sitecfg.grpc.data.SiteViewsDescs> data = model.get(CfgType.SV, cnv().idSet(request));
		send(cnv.svd().toProtoW(data), responseObserver);
	}
	@Override
	public void getSiteItems( IdRequest request, StreamObserver<SiteItems> responseObserver ) {
		if( check(CfgType.SI, request, responseObserver) ) {
			CfgSiteItems data = model.get(CfgType.SI, cnv().id(request));
			send(cnv.si().toProto(data), responseObserver);
		}
	}
	@Override
	public void getSiteFonts( IdRequest request, StreamObserver<SiteFonts> responseObserver ) {
		if( check(CfgType.SF, request, responseObserver) ) {
			CfgSiteFonts data = model.get(CfgType.SF, cnv().id(request));
			send(cnv.sf().toProto(data), responseObserver);
		}
	}
	@Override
	public void getLocSegs( LocSegsRequest request, StreamObserver<LocSegs> responseObserver ) {
		check(CfgType.LOC, request, responseObserver, segsId -> {
			CfgLocSegs data = model.get(CfgType.LOC, segsId);
			send(cnvLogSegs.toProto(data), responseObserver);
		});
	}
	@Override
	public void getBabSegs( LocSegsRequest request, StreamObserver<BabSegs> responseObserver ) {
		check(CfgType.SEG, request, responseObserver, segsId -> {
			List<CfgBabSeg> data = model.get(CfgType.SEG, segsId);
			send(cnv.babSeg().toProtoW(data), responseObserver);
		});
	}
	@Override
	public void getSiteBabSegs( IdRequest request, StreamObserver<BabSegs> responseObserver ) {
		if( check(CfgType.SEG, request, responseObserver) ) {
			List<CfgBabSeg> data = model.get(CfgType.SEG, cnv().id(request));
			send(cnv.babSeg().toProtoW(data), responseObserver);
		}
	}
	@Override
	public void getSiteDkSets( IdRequest request, StreamObserver<DkSets> responseObserver ) {
		if( check(CfgType.DKS, request, responseObserver) ) {
			List<CfgDkSet> data = model.get(CfgType.DKS, cnv().id(request));
			send(cnv.dk().toProtoW(data), responseObserver);
		}
	}
	@Override
    public void getSitDisplayTypes( IdRequest request, StreamObserver<SitDisplayTypes> responseObserver ) {
		if( check(CfgType.SIT_TYP, request, responseObserver) ) {
			List<CfgSitDisplayType> data = model.get(CfgType.SIT_TYP, cnv().id(request));
			send(cnv.sitTyp().toProtoW(data), responseObserver);
		}
    }
	@Override
    public void getSitClasses( IdRequest request, StreamObserver<SitClasses> responseObserver ) {
		if( check(CfgType.SIT_CLS, request, responseObserver) ) {
			List<CfgSitClass> data = model.get(CfgType.SIT_CLS, cnv().id(request));
			send(cnv.sitCls().toProtoW(data), responseObserver);
		}
    }
	@Override
    public void getRoadSegStyles( RoadSegKeys request, StreamObserver<RoadSegStyles> responseObserver ) {
		List<CfgRoadSegStyle> data = model.get(CfgType.SIT_STYLE, model.keySet(request));
		send(cnv.sitStyle().toProtoW(data), responseObserver);
    }
	@Override
    public void getRoadSegStyle( RoadSegKey request, StreamObserver<RoadSegStyle> responseObserver ) {
		String key = model.key(request);
		if( check(CfgType.SIT_STYLE, key, responseObserver) ) {
			CfgRoadSegStyle data = model.get(CfgType.SIT_STYLE, key);
			send(cnv.sitStyle().toProto(data), responseObserver);
		}
    }
	@Override
    public void getRoadSegSvg( RoadSegKey request, StreamObserver<SvgReply> responseObserver ) {
		String key = model.key(request);
		if( check(CfgType.SVG_SIT, key, responseObserver) ) {
			String data = model.get(CfgType.SVG_SIT, key);
			send(cnv.svg().toProto(data), responseObserver);
		}
    }
	@Override
    public void getViewAggrDescs( ViewAggrRequest request, StreamObserver<AggrDescs> responseObserver ) {
		String key = model.key(request);
		if( check(CfgType.AGGR, key, responseObserver) ) {
			List<CfgAggrDesc> data = model.get(CfgType.AGGR, key);
			send(cnv.aggrDesc().toProtoW(data), responseObserver);
		}
    }
	@Override
    public void getAllViewAggrDescs( Empty request, StreamObserver<ViewAggrDescs> responseObserver ) {
		List<CfgViewAggrDesc> lst = new ArrayList<>();
		IdMap<List<CfgAggrDesc>> idMap = model.idMap(CfgType.AGGR);
		for( String key : idMap.keySet() ) {
			lst.add(new CfgViewAggrDesc(key, idMap.get(key)));
		}
		send(cnv.viewAggrDesc().toProtoW(lst), responseObserver);
    }
	@Override
	public void getPng( IdRequest request, StreamObserver<PngReply> responseObserver ) {
		if( check(CfgType.PNG, request, responseObserver) ) {
			byte[] data = model.get(CfgType.PNG, cnv().id(request));
			send(cnv.png().toProto(data), responseObserver);
		}
	}
	@Override
	public void getSvg( IdRequest request, StreamObserver<SvgReply> responseObserver ) {
		if( check(CfgType.SVG, request, responseObserver) ) {
			String data = model.get(CfgType.SVG, cnv().id(request));
			send(cnv.svg().toProto(data), responseObserver);
		}
	}
	@Override
	public void getSymbolSvg( SymbolSvgId request, StreamObserver<SvgReply> responseObserver ) {
		String key = cnv().key(request);
		if( check(CfgType.SVG_SYMBOL, key, responseObserver) ) {
			String data = model.get(CfgType.SVG_SYMBOL, key);
			send(cnv.svg().toProto(data), responseObserver);
		}
	}
	@Override
	public void getAqSvg( AqSvgId request, StreamObserver<SvgReply> responseObserver ) {
		String key = cnv().key(request);
		if( check(CfgType.SVG_AQ, key, responseObserver) ) {
			String data = model.get(CfgType.SVG_AQ, key);
			send(cnv.svg().toProto(data), responseObserver);
		}
	}
	@Override
	public void getAqSvgs( AqSvgIds request, StreamObserver<AqSvgs> responseObserver ) {
		String svgId = request.getSvgId();
		CfgAqSvgs aqSvgs = new CfgAqSvgs(svgId);
		request.getIdsList().forEach(aqId -> {
			String key = cnv().joinIds(svgId, aqId);
			String data = (model.has(CfgType.SVG_AQ, key) ? model.get(CfgType.SVG_AQ, key) : "");
			aqSvgs.addItem(aqId, data);
		});
		send(cnv.aqSvg().toProtoW(aqSvgs), responseObserver);
	}
	@Override
	public void getAqs( IdList request, StreamObserver<DictQs> responseObserver ) {
		getDictQs(CfgType.AQ, request, responseObserver);
	}
	@Override
	public void getEqs( IdList request, StreamObserver<DictQs> responseObserver ) {
		getDictQs(CfgType.EQ, request, responseObserver);
	}
	@Override
	public void getUqs( IdList request, StreamObserver<DictQs> responseObserver ) {
		getDictQs(CfgType.UQ, request, responseObserver);
	}
	@Override
	public void getVqs( IdList request, StreamObserver<DictQs> responseObserver ) {
		getDictQs(CfgType.VQ, request, responseObserver);
	}
	@Override
	public void getXqs( IdList request, StreamObserver<DictQs> responseObserver ) {
		getDictQs(CfgType.XQ, request, responseObserver);
	}
	@Override
	public void getSms( IdList request, StreamObserver<DictQs> responseObserver ) {
		getDictQs(CfgType.SM, request, responseObserver);
	}
	@Override
	public void getZss( IdList request, StreamObserver<ZsList> responseObserver ) {
		List<CfgZS> data = model.get(CfgType.ZS, cnv().idSet(request));
		send(cnv.zs().toProtoW(data), responseObserver);
	}
	@Override
	public void getTss( IdList request, StreamObserver<TsList> responseObserver ) {
		List<CfgTS> data = model.get(CfgType.TS, cnv().idSet(request));
		send(cnv.ts().toProtoW(data), responseObserver);
	}
	@Override
	public void getDeviceFontSets( IdList request, StreamObserver<DeviceFontSets> responseObserver ) {
		List<CfgDeviceFontSet> data = model.get(CfgType.DF, cnv().idSet(request));
		send(cnv.dfs().toProtoW(data), responseObserver);
	}
	@Override
	public void getFontDescs( IdList request, StreamObserver<FontDescs> responseObserver ) {
		List<CfgFontDesc> data = model.get(CfgType.FD, cnv().idSet(request));
		send(cnv.fd().toProtoW(data), responseObserver);
	}
	@Override
	public void getFontSpacings( IdList request, StreamObserver<FontSpacings> responseObserver ) {
		List<CfgFontSpacing> data = model.get(CfgType.FS, cnv().idSet(request));
		send(cnv.fs().toProtoW(data), responseObserver);
	}
	@Override
	public void getLocks( IdList request, StreamObserver<WzgCodeLocks> responseObserver ) {
		List<CfgWzgCodeLock> data = model.get(CfgType.CL, cnv().idSet(request));
		send(cnv.cl().toProtoW(data), responseObserver);
	}
	@Override
	public void getMunits( IdList request, StreamObserver<Munits> responseObserver ) {
		List<CfgMunit> data = model.get(CfgType.MU, cnv().idSet(request));
		send(cnv.mu().toProtoW(data), responseObserver);
	}
}
