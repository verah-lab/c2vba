// ******************************************************************************************
//
//	Author   : P. Schmitz/Heusch-Boesefeldt GmbH
//	Kommentar: 
//			   
//
// ******************************************************************************************
#ifdef WIN32
#pragma warning ( disable : 4786 4996)
#endif



#define MB_NO_SUB_OBJ_ID		"-"

#define QTYPE_TLS 10
#define QTYPE_VKE 100

#define EQUIPMENT_TLS "TLS-Erfassung"
#define EQUIPMENT_VKE "VKE-Erfassung"




const long NO_OFFSET = -999999999;


#include "dbg_area_def.h"


extern "C" {

#ifdef DEBUGAREA
#undef DEBUGAREA
#endif
#define DEBUGAREA 1

#ifdef LOGGINGAREA
#undef LOGGINGAREA
#endif
#define LOGGINGAREA 1


#ifdef DEBUG
#undef DEBUG
#endif
#define DEBUG
#include "rnzdebug.h"
}


#include "LogBase.h"


#include "mstBuilderImpl.h"
using namespace mstBuilder;


#include <hbMTSafeQTmpl.h>
#include <hbSync.h>
#include "mstLocMatch.h"
#include "locLocationInfo.h"
#include "xmlDefReader.h"
#include "xmlConfReader.h"
#include "ddp.h"
#include "ddpZdfQuery.h"
#include "mstConst.h"
#include "mstGeodyn2WLS.h"

static hbString COL_NAME_EAID				= "eaid";
static hbString COL_NAME_ERFASS_PERIODE		= "ErfassPeriode";




static hbString toString( const mstBuilderException& ex )
{
	return hbString(ex.source) + hbString(": Error ") + ex.code + ", " + hbString(ex.description);
}

static int toDBState( mstBuilder::state S )
{
	return S - 1;
}

static hbString toMSTKey( const hbString& id, const hbString& version )
{
	return id + "[" + version +"]";
};

static mstBuilder::state toMSTState( int dbState )
{
	return (state)(dbState + 1);
}


mstBuilderImpl::mstBuilderImpl( ddpConnection* connection, 
								const hbString& d2Version,
								const hbString& geodyn2WLSAddress,
								bool dimsoecr,
								bool useLocNameAsMsId,
								bool carriagewayFromGlane,
								const hbString& lveBetriebsParamDatakind ) :
		ddpCompoundQuery( *connection )
{
	m_connection = connection;
	m_d2Version = d2Version;
	m_dimsoecr = dimsoecr;
	m_useLocNameAsMsId = useLocNameAsMsId;
	m_carriagewayFromGlane = carriagewayFromGlane;

	hbString mappingDef;

	try
	{
		if( connection->hasDatakind( GD_LOC_Q_DK ) )
		{
			ddpQuery Q( *connection, GD_LOC_Q_DK );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				ddpObject& obj = *itl;
				m_qid2LocId [ obj[ DB_COL_ID_Q ] ] = obj[ DB_COL_ID_LOC ].asLong();
			}
		}

		m_lveBAQuery = new ddpQuery( *m_connection, lveBetriebsParamDatakind );
		m_ufdBAQuery = new ddpQuery( *m_connection, UFD_BETRIEBS_PARAM_IST_DK );
	
		if( !connection->datakind( lveBetriebsParamDatakind ).hasColumn( COL_NAME_EAID ) )
			COL_NAME_EAID = "id";

		{
			ddpQuery Q( *connection, lveBetriebsParamDatakind );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				ddpObject& obj = *itl;

				unsigned len = obj[ DB_COL_IV_LAENGE_KURZ ].asUnsignedInt();
				if( len )
					m_eaid2Cycle [ obj[COL_NAME_EAID] ] = len * 15;
			}
		}

		if( !connection->datakind( UFD_BETRIEBS_PARAM_IST_DK ).hasColumn( COL_NAME_ERFASS_PERIODE ) )
			COL_NAME_ERFASS_PERIODE = "PeriodenDauer";

		{
			ddpQuery Q( *connection, UFD_BETRIEBS_PARAM_IST_DK );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				ddpObject& obj = *itl;

				unsigned len = obj[ COL_NAME_ERFASS_PERIODE ].asUnsignedInt();
				if( len )
					m_eaid2Cycle [ obj[COL_NAME_EAID] ] = len;
			}
		}

		{
			ddpQuery Q( *connection, TR_RDS_DEFAULT_VERSION_DK );
			ddpList Lst;
			Q.read( Lst );

			if( Lst.size() )
			{
				const ddpObject& obj = Lst.front();
				m_lclVersion = obj[DB_COL_DBID].asString() + ":" + obj[DB_COL_VERSION].asString();
			}
		}


		{
			m_min_x = -1;
			m_min_y = -1;
			m_max_x = -1;
			m_max_y = -1;

			if( connection->hasDatakind("extreme_coor") )
			{
				ddpQuery q( *connection , "extreme_coor" );         
				ddpList dkLst;
				q.read( dkLst );

				if( dkLst.size() > 0 )
				{
					ddpObject& obj = dkLst.front();

					m_min_x = obj["min_x"].asLong() + 500L;
					m_min_y = obj["min_y"].asLong() + 500L;
					m_max_x = obj["max_x"].asLong() - 500L;
					m_max_y = obj["max_y"].asLong() - 500L;
				}
			}
		}


		// Start query on 'DaLVEBetriebsParamIst'
		m_lveBAQuery->time( ddpTime( ddpTimeNow, ddpTimeForever ) );
		m_lveBAQuery->startJob();

		addQuery( *m_lveBAQuery );
		addQuery( *m_lveBAQuery );

		startJob();

		ZdfQueryConnection::instanciate( *m_connection );
		m_locConnection = new locConnection( *m_connection );

		MST_LOC_MATCH.setLocConnection( m_locConnection );

		MST_GEODYN2WLS.connect( m_connection, m_locConnection, geodyn2WLSAddress );

		hbString error;
		if( !readMSTConfig( d2Version, error ) )
		{
			::exit(-1);
		}
	}
	catch( const hbException& ex )
	{
		hbString errTxt = hbString("Fehler beim Lesen der Mapping-Definition: \n") + ex.what();
		HbErr( errTxt )
		::exit(-1);
	}

	readMSTs();
}

bool mstBuilderImpl::readMSTConfig( const hbString& d2Version, hbString& error )
{
	cLog << "Start readMSTConfig()" << endl;
	cLog << "d2Version: " << d2Version << endl;

	hbString mappingDef;

	try
	{
		ddpQuery Q( *m_connection, "datex2MSTConfig" );
		Q.condition( "d2Version = '" + d2Version + "'" );
		Q.order( "count" );

		ddpList Lst;
		Q.read( Lst );

		ddpList::iterator itl;
		for( itl = Lst.begin(); itl != Lst.end(); ++itl )
		{
			ddpObject& obj = *itl;
			mappingDef += obj["defPart"].asString();
		}
	}
	catch( const hbException& ex )
	{
		hbString errTxt = hbString("Fehler beim Lesen der Mapping-Definition: \n") + ex.what();
		HbErr( errTxt )

		error = errTxt;
		return false;
	}

	hbString errTxt;
	if( !XML_CONF_READER.parseDef( mappingDef, errTxt ) )
	{
		errTxt = "Fehlerhafte MST-Konfiguration: \n" + errTxt;
		HbErr( errTxt )

		error = errTxt;
		return false;
	}

	mbDatakindMappings datakindMappings = XML_CONF_READER.getDatakindMappings();

	try
	{
		datakindMappings.check( m_connection );
	}
	catch( const mstBuilderException& ex )
	{
		HbErr( toString( ex ) );

		error = toString( ex );
		return false;
	}

	m_datakindMappings = datakindMappings;
	m_d2Version = d2Version;

	cLog << "MST-Konfiguration <" << d2Version  << "> erfoglreich gelesen."  << endl;

	return true;
}



bool mstBuilderImpl::readMSTDefinition( const hbString& mstId, 
									    const hbString& defVersion,
										mstDefinition& def,
										hbString& error )
{
	hbString defStr;

	hbString d2Version;

	try
	{
		ddpQuery Q( *m_connection, "datex2MSTDefinition" );
		Q.condition( hbString( "( mst = '" + mstId + "' ) and " ) + 
					 hbString( "( defVersion = '" + defVersion + "' )" ) );

		ddpList Lst;
		Q.read( Lst );

		if( Lst.size() == 0 )
		{
			error = hbString( "Keine MST-Definition zu: " ) + 
					hbString( "mstId='" + mstId + "', " ) +
					hbString( "defVersion='" + defVersion + "'" );

			HbErr( error )

			return false;
		}
		else
		{
			ddpObject& obj = Lst.front();
			d2Version = obj["d2Version"].asString();
		}
	}
	catch( const hbException& ex )
	{
		error = hbString( "Fehler beim Lesen der MST-Definition: \n") + ex.what();
		HbErr( error )

		return false;
	}


	try
	{
		ddpQuery Q( *m_connection, "datex2MSTDefinitionDetail" );
		Q.condition( hbString( "( mst = '" + mstId + "' ) and " ) + 
					 hbString( "( defVersion = '" + defVersion + "' )" ) );
		Q.order( "count" );

		ddpList Lst;
		Q.read( Lst );

		if( Lst.size() == 0 )
		{
			error = hbString( "Keine MST-Definition zu: " ) + 
					hbString( "mstId='" + mstId + "', " ) +
					hbString( "defVersion='" + defVersion + "'" );

			HbErr( error )

			return false;
		}

		ddpList::iterator itl;
		for( itl = Lst.begin(); itl != Lst.end(); ++itl )
		{
			ddpObject& obj = *itl;
			defStr += obj["defPart"].asString();
		}
	}
	catch( const hbException& ex )
	{
		error = hbString( "Fehler beim Lesen der MST-Definition: \n") + ex.what();
		HbErr( error )

		return false;
	}

	if( d2Version != m_d2Version )
	{
		if( !readMSTConfig( d2Version, error ) )
		{
			return false;
		}
	}

	if( !XML_DEF_READER.parseDef( defStr, error ) )
	{
		error = "Fehlerhafte MST-Definition: \n" + error;
		HbErr( error )

		return false;
	}

	def = XML_DEF_READER.getDefinition();

	return true;
}


void mstBuilderImpl::readMSTs()
{
	try
	{
		{
			ddpQuery Q( *m_connection, "datex2MST" );
			// Q.condition( "d2Version = '" + m_d2Version + "'" );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				mstDefinition _mst;

				ddpObject& obj = *itl;
				hbString mstId = obj["mst"].asString();
				hbString version = obj["version"].asString();
				hbString mstKey = toMSTKey( mstId, version );

				_mst.version		= (*itl)["version"].asString().c_str();
				_mst.description	= (*itl)["description"].asString().c_str();
				_mst.active			= (*itl)["active"].asBool();

				m_msts[mstKey] = _mst;
			}
		}

		{
			ddpQuery Q( *m_connection, "datex2MSTDataSource" );
			//Q.condition( "mst in (select mst from datex2MST where d2Version = '" + m_d2Version + "')" );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				ddpObject& obj = *itl;

				hbString mstId = obj["mst"].asString();
				hbString version = obj["version"].asString();
				hbString mstKey = toMSTKey( mstId, version );

				map< hbString, mstDefinition >::iterator itm = m_msts.find( mstKey );

				if( itm == m_msts.end() )
				{
					hbString errTxt = hbString("Unbekannte MST-Referenz '" + mstId + "[" + version + "]' in Datenart 'datex2MSTDataSource'");
					HbErr( errTxt )
					::exit(-1);
				}
				mstEntry _mste;

				_mste.datakindName			= (*itl)["datakind"].asString().c_str();
				_mste.column				= (*itl)["valueCol"].asString().c_str();
				_mste.columnCharacteristic	= (*itl)["valueColCharacteristic"].asString().c_str();
				_mste.validInterval			= (*itl)["validInterval"].asUnsignedInt();

				mstEntries& Es = itm->second.columnEntries;

				unsigned len = Es.length();
				Es.length( len + 1 );
				Es[len] = _mste;
			}
		}

		{
			ddpQuery Q( *m_connection, "datex2MSTObjectFilter" );
			// Q.condition( "mst in (select mst from datex2MST where d2Version = '" + m_d2Version + "')" );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				ddpObject& obj = *itl;
				hbString mstId = obj["mst"].asString();
				hbString version = obj["version"].asString();
				hbString mstKey = toMSTKey( mstId, version );

				map< hbString, mstDefinition >::iterator itm = m_msts.find( mstKey );

				if( itm == m_msts.end() )
				{
					hbString errTxt = hbString("Unbekannte MST-Referenz '" + mstId + "[" + version + "]' in Datenart 'datex2MSTObjectFilter'");
					HbErr( errTxt )
					::exit(-1);
				}

				hbString objId = (*itl)["locId"].asString();
				mstObjFilters& of = itm->second.filter.objFilters;

				unsigned i;
				for( i = 0; i < of.length(); i++ )
				{
					if( hbString(of[i].id) == objId )
						break;
				}
				
				unsigned len = of.length();
				if( i == len )
				{
					of.length( len + 1 );
					of[len].id = objId.c_str();
				}

				hbString subObjId = (*itl)["laneId"].asString();
				if( subObjId != MB_NO_SUB_OBJ_ID )
				{
					subObjNames& son = of[i].subObjIds;

					unsigned _len = son.item.length();
					son.item.length( _len + 1 );
					son.item[_len] = subObjId.c_str();
				}
			}
		}

		{
			ddpQuery Q( *m_connection, "datex2MSTRoadFilter" );
			// Q.condition( "mst in (select mst from datex2MST where d2Version = '" + m_d2Version + "')" );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				ddpObject& obj = *itl;
				hbString mstId = obj["mst"].asString();
				hbString version = obj["version"].asString();
				hbString mstKey = toMSTKey( mstId, version );

				map< hbString, mstDefinition >::iterator itm = m_msts.find( mstKey );

				if( itm == m_msts.end() )
				{
					hbString errTxt = hbString("Unbekannte MST-Referenz '" + mstId + "[" + version + "]' in Datenart 'datex2MSTRoadFilter'");
					HbErr( errTxt )
					::exit(-1);
				}
				mstRoadFilters& rfs = itm->second.filter.roadFilters;

				unsigned len = rfs.length();
				rfs.length( len + 1 );
				rfs[len].name = (*itl)["roadName"].asString().c_str();
				rfs[len].startOffset = (*itl)["fromKm"];
				rfs[len].endOffset = (*itl)["toKm"];
			}
		}

		{
			ddpQuery Q( *m_connection, "datex2MSTBuildState" );
			// Q.condition( "mst in (select mst from datex2MST where d2Version = '" + m_d2Version + "')" );
			ddpList Lst;
			Q.read( Lst );

			ddpList::iterator itl;
			for( itl = Lst.begin(); itl != Lst.end(); ++itl )
			{
				ddpObject& obj = *itl;
				hbString mstId = obj["mst"].asString();
				hbString version = obj["version"].asString();
				hbString mstKey = toMSTKey( mstId, version );

				mstBuilder::state S = toMSTState( (*itl)["state"].asInt() );

				if( ( S !=  buildFailed ) || ( S !=  buildAborted ) || ( S !=  finished ) )
				{
					S = buildFailed;
				}

				mstState state;

				state.currentState		= S;
				state.buildHasStarted	= false;
				state.error				= (*itl)["result"].asString().c_str();

				m_mst2State[ mstKey ] = state;
			}
		}

		map< hbString, mstDefinition >::iterator itm;
		for( itm = m_msts.begin(); itm != m_msts.end(); ++itm )
		{
			if( m_mst2State.find( itm->first ) == m_mst2State.end() )
			{
				mstState S;
				S.currentState = mstBuilder::unknown;
				S.buildHasStarted = false;
				S.error = hbString().c_str();

				m_mst2State[ itm->first ] = S;
			}
		}
	}
	catch( const hbException& ex )
	{
		hbString errTxt = hbString("Fehler beim Lesen der MSTs: \n") + ex.what();
		HbErr( errTxt )
		::exit(-1);
	}
}



bool mstBuilderImpl::checkMST( const mstBuilder::mstDefinition & oneMST, long& errCode, hbString& errTxt ) 
{
	unsigned i;

	for( i = 0; i < oneMST.columnEntries.length(); i++ )
	{
		const mstEntry& mste = oneMST.columnEntries[i];

		hbString dk = hbString( mste.datakindName );
        hbString col = hbString( mste.column );
		hbString colc = hbString( mste.columnCharacteristic );

		if( !m_datakindMappings.hasMapping( dk, col, colc ) )
		{
			errCode = MB_UNKNOWN_IN_MAPPING;
			errTxt = "No mapping for {" + dk + "," + col;

			if( !colc.empty() )
				errTxt += "," + colc;

			errTxt += "}";

			return false;
		}
	}
	 

	return true;
}


typedef list<ddpObject> _ddpList;

static void getEAIDs( const hbString& dkName, vector<long>& eaids, hbString& errTxt )
{
	eaids.clear();

	try
	{
		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( ZdfPartSpace( "EA", "EA" ).
			                                             Type( hbString(dkName) ) ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_EAIDS;
			ex.description = hbString( "ZdfPartRequest read failed").c_str();
			ex.source = hbString("::getEAIDS()").c_str();

			throw ex;
		}

		_ddpList::iterator itl; 
		for( itl = qLst.begin(); itl != qLst.end(); ++itl )
			eaids.push_back( (*itl)["eaid"] );
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_EAIDS;
		ex.description = ( hbString( "ZdfPartRequest read failed\n") + _ex.what() ).c_str();
		ex.source = hbString("::getEAIDS()").c_str();

		throw ex;
	}
}

static void getQIDs( long qType, vector<long>& eaids, hbString& errTxt )
{
	eaids.clear();

	try
	{
		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( ZdfPartSpace( "Q", "Q" ).
			                                             Type( qType ) ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_QIDS;
			ex.description = hbString( "ZdfPartRequest read failed").c_str();
			ex.source = hbString("::getQIDS()").c_str();

			throw ex;
		}

		_ddpList::iterator itl; 
		for( itl = qLst.begin(); itl != qLst.end(); ++itl )
			eaids.push_back( (*itl)["qid"] );
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_QIDS;
		ex.description = ( hbString( "ZdfPartRequest read failed\n") + _ex.what() ).c_str();
		ex.source = hbString("::getQIDS()").c_str();

		throw ex;
	}
}


void  mstBuilderImpl::filterRDSVersion( const hbString& objType, list<long>& IDs )
{
	if( objType == TR_RDS_SEGMENT_DK )
	{
		set<long> vIDs;

		ddpQuery Q( *m_connection, TR_RDS_SEGMENT_LOOKUP_DK );
		ddpList Lst;

		Q.condition( "dbid = '" + m_lclVersion + "'" );
		Q.read( Lst );

		ddpList::iterator itl;
		for( itl = Lst.begin(); itl != Lst.end(); ++itl )
		{
			ddpObject& obj = *itl;
			vIDs.insert( obj["id"].asLong() );
		}

		// Segmente, die nicht zur LCL-Version gehören, werden entfernt.
		list<long>::iterator iti = IDs.begin();
		while( iti != IDs.end() )
		{
			if( vIDs.find( *iti ) == vIDs.end() )
				iti = IDs.erase( iti );
			else
				++iti;
		}
	}
}


void  mstBuilderImpl::readLayer( const hbString& objType, list<long>& IDs )
{
	locLayer loc_layer( *m_locConnection, m_connection->datakind( objType ).id() );

	loc_layer.getLocations( IDs );

	filterRDSVersion( objType, IDs );
}

void  mstBuilderImpl::_readLocations( const hbString& objType, const list<long>& IDs )
{
	if( m_locInfos.find( objType ) == m_locInfos.end() )
	{
		list< locLocationInfoExt > locInfos;
		m_locConnection->getLocationsInfoExt( IDs, false, false, true, true, locInfos );

		bool hasRDSInfo = false;

		locInfoMap& lim = m_locInfos[ objType ];
		list< locLocationInfoExt >::iterator iti;
		for( iti = locInfos.begin(); iti != locInfos.end(); ++iti )
		{
			lim[ iti->id() ] = *iti;
			if( iti->hasRDSInfo() )
				hasRDSInfo = true;
		}

		if( !hasRDSInfo && MST_GEODYN2WLS.connected() )
		{
			// Get RDS info from geodyn2WLS
			map<long,locRDSInfo> rdsInfos;
			MST_GEODYN2WLS.getRdsInfo( objType, IDs, rdsInfos );

			// Add retrieved RDS info
			map<long,locRDSInfo>::iterator itm;
			for( itm = rdsInfos.begin(); itm != rdsInfos.end(); ++itm )
			{
				long id = itm->first;
				locInfoMap::iterator iti = lim.find( id );
				if( iti != lim.end() )
				{
					locLocationInfoExt& llie = iti->second;
					llie.hasRDSInfo( true );
					list<locRDSInfo> lst;
					lst.push_back( itm->second  );
					llie.RDSInfo( lst );
				}
			}
		}
	}
}


void  mstBuilderImpl::readLocations( const hbString& objType )
{
	try
	{
		list<long> IDs;
		readLayer( objType, IDs );
		_readLocations( objType, IDs ); 
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_LOC_IDS;
		ex.description = ( hbString( "Exception determining location IDs for layer '" ) + 
			objType + "'\n" + hbString(_ex.what()) ).c_str();
		ex.source = hbString("mstBuilderImpl::readLocations()").c_str();

		throw ex;
	}
}



void mstBuilderImpl::getLocIds( const hbString& objType, vector<long>& eaids )
{
	eaids.clear();

	try
	{
		set<long> sIDs;
		list<long> IDs;
		readLayer( objType, IDs );
		_readLocations( objType, IDs ); 

		list<long>::iterator it;
		for( it = IDs.begin() ; it != IDs.end() ; ++it )
		{
			if( sIDs.find( *it ) == sIDs.end() )
			{
				sIDs.insert( *it );
				eaids.push_back( *it );
			}
		}

	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_LOC_IDS;
		ex.description = ( hbString( "Exception determining location IDs for layer '" ) + 
			               objType + "'\n" + hbString(_ex.what()) ).c_str();
		ex.source = hbString("mstBuilderImpl::getLocIds()").c_str();

		throw ex;
	}
}

long mstBuilderImpl::qid2LocId( long qid )
{
	if( m_qid2LocId.size() == 0 )
		return qid;

	map< long, long >::const_iterator it = m_qid2LocId.find( qid );
	if( it != m_qid2LocId.end() )
		return it->second;

	return -1;
}

long mstBuilderImpl::getLocId( mbReferenceTypeEnum refType, long eaid )
{
	if( ( refType == mbGEODYN2_REF ) || ( refType == mbGEODYN2_REF_NON_Q ) )
		return eaid;

	if( refType == mbTLS_Q_COORD_SUPPLY )
		return eaid;

	try
	{
		ZdfPartSpace zps( "EA", eaid, "Q" );

		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( zps.Upward().Type( QTYPE_TLS ) ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_LOC_ID;
			ex.description = ( hbString( "Exception determining location id for EAID '" ) + 
							   eaid + "'").c_str();
			ex.source = hbString("::getLocId()").c_str();

			throw ex;
		}

		if( qLst.size() == 0 )
			return -1;
		
		return qLst.front()["qid"];
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_LOC_ID;
		ex.description = ( hbString( "Exception determining location id for EAID '" ) + 
			               eaid + "'\n" + hbString(_ex.what()) ).c_str();
		ex.source = hbString("::getLocId()").c_str();

		throw ex;
	}
}



static hbString toD2Lane( int lc )
{
	if( lc == 0 )
		return "hardShoulder";
	else if( lc == 1 )
		return "lane1";
	else if( lc == 2 )
		return "lane2";
	else if( lc == 3 )
		return "lane3";
	else if( lc == 4 )
		return "lane4";
	else if( lc == 5 )    
		return "lane5";
			
    return "unknown";			
}

static hbString toEquipmentStr( mbReferenceTypeEnum refType, int equipId )
{
	if( equipId == QTYPE_TLS ) 
		return EQUIPMENT_TLS;

	else if( equipId == QTYPE_VKE ) 
		return EQUIPMENT_VKE;

	else if( refType != mbGEODYN2_REF_NON_Q )
		return EQUIPMENT_TLS;
	else
		return "";
}


static int getLaneNumber( long eaid )
{
	try
	{
		ZdfPartSpace zps( "EA", eaid, "LANEDESC" );

		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( zps.Upward() ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_LOC_ID;
			ex.description = ( hbString( "Exception determining lane number for EAID '" ) + 
							   eaid + "'").c_str();
			ex.source = hbString("::getLaneNumber()").c_str();

			throw ex;
		}

		if( qLst.size() == 0 )
			return -1;

		return qLst.front()["lanelage"].asInt();
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_LOC_ID;
		ex.description = ( hbString( "Exception determining lane number for EAID '" ) + 
			               eaid + "'\n" + hbString(_ex.what()) ).c_str();
		ex.source = hbString("::getLaneNumber()").c_str();

		throw ex;
	}
}


static int getEquipment( long locId )
{
	try
	{
		ZdfPartSpace zps( "Q", locId, "Q" );

		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( zps ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_EQUIP;
			ex.description = ( hbString( "Exception determining detection type for qid '" ) + 
							   locId + "'").c_str();
			ex.source = hbString("::getEquipment()").c_str();

			throw ex;
		}

		if( qLst.size() == 0 )
			return -1;

		return qLst.front()["qtype"].asInt();
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_EQUIP;
		ex.description = ( hbString( "Exception determining detection type for qid '" ) + 
						   locId + "'\n" + _ex.what() ).c_str();
		ex.source = hbString("::getEquipment()").c_str();

		throw ex;
	}
}


static long sfold( const hbString&  s, int M ) 
{
	int intLength = s.size() / 4;
	long sum = 0;
	for (int j = 0; j < intLength; j++) {
		hbString c = s.substr(j * 4, (j * 4) + 4);
		long mult = 1;
		for (unsigned k = 0; k < c.size(); k++) 
		{
			sum += c[k] * mult;
			mult *= 256;
		}
	}

	hbString c = s.substr(intLength * 4);
	long mult = 1;
	for (unsigned k = 0; k < c.size(); k++) 
	{
		sum += c[k] * mult;
		mult *= 256;
	}

	return (abs(sum) % M);
}


bool  mstBuilderImpl::getLocName( long locId, mbMSTLocation& mstLoc )
{
	map< hbString, locInfoMap >::iterator itl = m_locInfos.find( m_currentObjType );
	if( itl != m_locInfos.end() )
	{
		locInfoMap& lim = itl->second;
		locInfoMap::iterator iti = lim.find( locId );
		if( iti != lim.end() )
		{
			locLocationInfoExt& llie = iti->second;

			mstLoc.m_locName = llie.name();
			if( m_useLocNameAsMsId )
			{
				mstLoc.m_d2Id = hbString("R_") + llie.name(); 
			}
			else
			{
				mstLoc.m_d2Id = hbString("R") + locId; 
			}
			if( m_currentObjType == TR_RDS_SEGMENT_OBJ_TYPE )
			{
				if( llie.hasRDSInfo() )
				{
					list<locRDSInfo> rdsInfos = llie.RDSInfo();
					if( rdsInfos.size() > 0 )
					{
						const locRDSInfo& rdsInfo = rdsInfos.front();
						long pl = rdsInfo.primaryLoc();
						long sl = rdsInfo.secondaryLoc();

						locRDSLocationCode plInfo;
						if( m_locConnection->getRDSLocationCode( m_lclVersion, pl, plInfo ) )
						{
							locRDSLocationCode slInfo;
							if( m_locConnection->getRDSLocationCode( m_lclVersion, sl, slInfo ) )
							{
								mstLoc.m_locName =
										plInfo.route_desc() + hbString(" ") + 
										slInfo.first_name() + hbString(" (") + sl + hbString(") --> ") +
										plInfo.first_name() + hbString(" (") + pl + hbString(")");                
							}
						}
					}
				}
			}

			return true;
		}
	}

	return false;
}

bool mstBuilderImpl::getQName( const hbString& version, const hbString& refVersion, 
							   const hbString& qNameMode,
							   long  locId, 
							   mbMSTLocation& mstLoc, hbString& errTxt )
{
	try
	{
		ZdfPartSpace zps( "Q", locId, "Q" );

		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( zps ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_QNAME;
			ex.description = ( hbString( "Exception determining Q name for qid '" ) + 
							   locId + "'").c_str();
			ex.source = hbString("::getQName()").c_str();

			throw ex;
		}

		if( qLst.size() == 0 )
			return false;

		mstLoc.m_locName = qLst.front()[ qNameMode ];

		if( refVersion.empty() )
		{
			if( m_useLocNameAsMsId )
			{
				mstLoc.m_d2Id = "R_" + qLst.front()["qname"].asString(); 
			}
			else
			{
				mstLoc.m_d2Id = "R" + qLst.front()["qid"].asString(); 
			}
		}
		else
		{
			int mult = atoi( refVersion );
			int id = qLst.front()["qid"].asInt();

			int sign = id % 2;
			int idMod = id / 2;
			if( sign )
				id = 30000000 + idMod;
			else
				id = 30000000 - idMod;

			long base = sfold( refVersion, 1013 ) + (long)mult * (long)4711 + (long)id;
			mstLoc.m_d2Id = hbString("R") + base;
		}

		return true;
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_QNAME;
		ex.description = ( hbString( "Exception determining Q name for qid '" ) + 
						   locId + "'\n" + _ex.what() ).c_str();
		ex.source = hbString("::getQName()").c_str();

		throw ex;
	}
}

int mstBuilderImpl::getCycleLengthZQL_Q( long eaid )
{
	map< long, unsigned >::iterator it = m_eaid2Cycle.find( eaid );

	if( it == m_eaid2Cycle.end() )
		return 60;

	return it->second;
}

int mstBuilderImpl::getCycleLength( long eaid )
{
	map< long, unsigned >::iterator it = m_eaid2Cycle.find( eaid );

	if( it == m_eaid2Cycle.end() )
		return -1;

	return it->second;
}


int mstBuilderImpl::getCycleLengthQ( long locId )
{
	try
	{
		ZdfPartSpace zps( "Q", locId, "LANEDESC" );

		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( zps ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_EQUIP;
			ex.description = ( hbString( "Exception determining detection cycle length for qid '" ) + 
							   locId + "'").c_str();
			ex.source = hbString("::getCycleLengthQ()").c_str();

			throw ex;
		}

		if( qLst.size() == 0 )
			return -1;

		_ddpList::iterator ito;
		for( ito = qLst.begin(); ito != qLst.end(); ++ito )
		{
			int iv = getCycleLength( (*ito)["laneid"] );
			if( iv != -1 )
				return iv;

		}

		return -1;
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_EQUIP;
		ex.description = ( hbString( "Exception determining detection cycle length for qid '" ) + 
						   locId + "'\n" + _ex.what() ).c_str();
		ex.source = hbString("::getCycleLengthQ()").c_str();

		throw ex;
	}
}

int mstBuilderImpl::getCarriageway( long qid )
{
	try
	{
		ZdfPartSpace zps( "Q", qid, "GLANE" );

		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( zps ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_EQUIP;
			ex.description = ( hbString( "Exception determining carriageway for qid '" ) + 
				               qid + "'").c_str();
			ex.source = hbString("::getCarriageway()").c_str();

			throw ex;
		}

		if( qLst.size() == 0 )
			return -1;

		return qLst.front()["glanetype"];
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_LOC;
		ex.description = ( hbString( "Exception determining carriageway for qid '" ) + 
			qid + "'\n" + _ex.what() ).c_str();
		ex.source = hbString("::getCarriageway()").c_str();

		throw ex;
	}
}


bool mstBuilderImpl::getLocation( long locId, mbReferenceTypeEnum refType,
								  mbMSTLocation& mstLoc, hbString& errTxt )
{
	try
	{
		bool noInfo = true;
		bool hasRDSInfo = false;
		locRDSInfo rds_info;
		hbString permanentId;

		bool hasCoordinates;
		locGeo coor;

		map< hbString, locInfoMap >::iterator itl = m_locInfos.find( m_currentObjType );
		if( itl != m_locInfos.end() )
		{
			locInfoMap& lim = itl->second;
			locInfoMap::iterator iti = lim.find( locId );
			if( iti != lim.end() )
			{
				noInfo = false;
				locLocationInfoExt& llie = iti->second;
				permanentId = llie.permanentId();

				if( llie.RDSInfo().size() > 0 )
				{
					hasRDSInfo = llie.hasRDSInfo();
					rds_info = llie.RDSInfo().front();
				}

				hasCoordinates = llie.hasCoordinates();
				coor = llie.Coordinates();
			}
		}


		if( noInfo )
		{
			locLocation loc( *m_locConnection, locId ); 
			permanentId = loc.permanentId();

			if( loc.getRDSInfo( rds_info ) )
				hasRDSInfo = true;

			hbString loc_name = loc.name();
			LogB2( "Name: " << loc_name << endl; );

			try
			{
				loc.getCoordinates(coor);
				hasCoordinates = true;
			}
			catch( const hbException& coEx )
			{
				HbErr( "Exception retrieving coordinates of location with ID '" << locId << "'" )
				HbErr( coEx )
			}	
		}

		if( !MST_LOC_MATCH.match( locId, permanentId ) )
		{
			LogB3( "Location-Filter nicht erfuellt" << endl; )
			return false;
		}

		// RDS-Location-Angaben bestimmen
		
		mstLoc.m_endCoordX = -2;
		mstLoc.m_endCoordY = -2;
		mstLoc.m_startCoordX = -2;
		mstLoc.m_startCoordY = -2;

		if( hasCoordinates )
		{
			list<locUnit> ulst = coor.unit();
			if ( ulst.size() )
			{
				locUnit u = *(ulst.begin()); 
				list<locPoint> plst = u.coor(); 	
				if( plst.size() )
				{
					locPoint pt = *(plst.begin());
					mstLoc.m_startCoordX = pt.x();
					mstLoc.m_startCoordY = pt.y();

					if( m_min_x != -1 )
					{
						// 'virtuelle', nicht-reale Koordinaten
						if( 
							( pt.x() < m_min_x ) ||
							( pt.x() > m_max_x ) ||
							( pt.y() < m_min_y ) ||
							( pt.y() > m_max_y ) 
							)
						{
							if( m_dimsoecr )
							{
								mstLoc.m_startCoordX = -1;
								mstLoc.m_startCoordY = -1;
							}
							else 
								hasCoordinates = false;
						}
					}

				}
				else
					hasCoordinates = false;

				if( refType == mbGEODYN2_REF_NON_Q )
				{
					if( plst.size() > 1 )
					{
						const locPoint& pt = plst.back();
						mstLoc.m_endCoordX = pt.x();
						mstLoc.m_endCoordY = pt.y();

						if( m_min_x != -1 )
						{
							// 'virtuelle', nicht-reale Koordinaten
							if( 
								( pt.x() < m_min_x ) ||
								( pt.x() > m_max_x ) ||
								( pt.y() < m_min_y ) ||
								( pt.y() > m_max_y ) 
								)
							{
								if( m_dimsoecr )
								{
									mstLoc.m_endCoordX = -1;
									mstLoc.m_endCoordY = -1;
								}
								else 
									hasCoordinates = false;
							}
						}
					}
				}
			}
			else 
				hasCoordinates = false;
		}

		if( !hasCoordinates && !hasRDSInfo )
		{
			LogB3( "Keine Verortung verfügbar !" << endl; )
			return false;
		}
		
		if( hasRDSInfo )
		{
			hbString rds_loc_tbl_ref = rds_info.dbid(); 
			hbString rds_db_ver;
			unsigned pos = rds_loc_tbl_ref.find( ":" );
			if( pos != string::npos )
			{
				rds_db_ver = rds_loc_tbl_ref.substr( pos + 1 );
				rds_loc_tbl_ref = rds_loc_tbl_ref.substr( 0, pos );
			}

   			mstLoc.m_rdsLocTblRef = rds_loc_tbl_ref;
   			mstLoc.m_rdsLocTblVer = rds_db_ver;


			mstLoc.m_rdsPrimLocCode = rds_info.primaryLoc();
			
			locLocation::Direction dir = rds_info.direction();
			switch( dir )
			{
				case locLocation::None:   
					mstLoc.m_rdsDirection = "U";
					break;  
				case locLocation::Both:   
					mstLoc.m_rdsDirection = "B";
					break;  
				case locLocation::Negativ:   
					mstLoc.m_rdsDirection = "P";
					break;  
				case locLocation::Positiv:   
					mstLoc.m_rdsDirection = "N";
					break;  
			}
			
			mstLoc.m_rdsPrimLocDist = long(rds_info.primaryOffset());

			if( refType == mbGEODYN2_REF_NON_Q )
			{
				mstLoc.m_rdsSecLocCode = rds_info.secondaryLoc();
				mstLoc.m_rdsSecLocDist = long(rds_info.secondaryOffset());
			}
			else
			{
				mstLoc.m_rdsSecLocCode = 0;
				mstLoc.m_rdsSecLocDist = 0;
			}
		}
		else
		{
   			mstLoc.m_rdsLocTblRef		= "";
   			mstLoc.m_rdsLocTblVer		= "";
			mstLoc.m_rdsPrimLocCode		= 0;
			mstLoc.m_rdsDirection		= "";
			
			mstLoc.m_rdsPrimLocDist		= 0;

			mstLoc.m_rdsSecLocCode		= 0;
   			mstLoc.m_rdsSecLocDist		= 0;
		}

		mstLoc.m_equipment = toEquipmentStr( refType, getEquipment( locId ) );

	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_LOC_ID;
		ex.description = ( hbString( "Exception determining location with ID '" ) + 
			               locId + "'\n" + hbString(_ex.what()) ).c_str();
		ex.source = hbString("::getLocation()").c_str();

		HbErr( toString(ex) );

		return false;
	}

	return true;
}

void mstBuilderImpl::getLocationCoord(  long locId, mbMSTLocation& mstLoc )
{
	// @@@ ToDo
}

bool mstBuilderImpl::getLocationQ( mbReferenceTypeEnum refType, 
								   const hbString& qNameMode, long locId, 
								   mbMSTLocation& mstLoc, hbString& errTxt )
{
	try
	{
		ZdfPartSpace zps( "Q", locId, "Q" );

		_ddpList qLst;
		unsigned char readFail = ZdfPartRequest().Space( zps ).Read( qLst );

		if( readFail )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_DETERMINE_LOC;
			ex.description = ( hbString( "Exception location attributes for qid '" ) + 
							   locId + "'").c_str();
			ex.source = hbString("mstBuilderImpl::getLocationQ()").c_str();

			throw ex;
		}

		if( qLst.size() == 0 )
			return false;

		mstLoc.m_locName = qLst.front()[ qNameMode ];

		if( m_useLocNameAsMsId )
		{
			mstLoc.m_d2Id = "R_" + qLst.front()["qname"].asString(); 
		}
		else
		{
			mstLoc.m_d2Id = "R" + qLst.front()["qid"].asString(); 
		}


   		mstLoc.m_rdsLocTblRef		= "";
   		mstLoc.m_rdsLocTblVer		= "";
		mstLoc.m_rdsPrimLocCode		= 0;
		mstLoc.m_rdsDirection		= "";
		
		mstLoc.m_rdsPrimLocDist		= 0;

		mstLoc.m_rdsSecLocCode		= 0;
   		mstLoc.m_rdsSecLocDist		= 0;

		mstLoc.m_endCoordX = -2;
		mstLoc.m_endCoordY = -2;

		mstLoc.m_startCoordX = -2;
		mstLoc.m_startCoordY = -2;

		int e = getEquipment( locId );
		if( e != -1 )
		{
			mstLoc.m_equipment = toEquipmentStr( refType, e );
		}

		getLocationCoord( locId, mstLoc );

		return true;
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_LOC;
		ex.description = ( hbString( "Exception determining location attributes qid '" ) + 
						   locId + "'\n" + _ex.what() ).c_str();
		ex.source = hbString("mstBuilderImpl::getLocationQ()").c_str();

		throw ex;
	}
}

static hbString toD2Carriageway( int cw ) 
{
	switch( cw ) 
	{
		case 1:
			return "mainCarriageway"; 	
		case 2:
			return "parallelCarriageway"; 	
		case 4:
			return "exitSlipRoad"; 	
		case 3:
			return "entrySlipRoad"; 	
		default:
			return "";
	}
}

void mstBuilderImpl::buildRecords( const mstBuilder::mstDefinition& oneMST )
{
	hbString errTxt;
	if( !MST_LOC_MATCH.init( oneMST.filter.objFilters, oneMST.filter.roadFilters, errTxt ) )
	{
		mstBuilderException ex;
		ex.code = MB_ERR_DETERMINE_EAIDS;
		ex.description = hbString( errTxt ).c_str();
		ex.source = hbString("mstBuilderImpl::buildRecords()").c_str();

		throw ex;
	}

	m_currentLocs.clear();
	m_currentSources.clear();
	m_currentItems.clear();
	m_currentLocIndex.clear();
	m_notLocMatchingEAIDs.clear();
	m_locInfos.clear();

	m_currentLocCount = 0;
	m_currentSrcCount = 0;
	m_currentItemCount = 0;

	hbString mstName( ( const char* )oneMST.name );

	hbString qNameMode( ( const char* )oneMST.qNameMode );
	if( qNameMode.empty() )
		qNameMode = Q_NAME_MODE_QNAME;

	unsigned i;
	for( i = 0; i < oneMST.columnEntries.length(); i++ )
	{
		mstEntry mste = oneMST.columnEntries[i];

		mbMSTSource src;
		src.m_mst		= mstName;
   		src.m_id		= ++m_currentSrcCount;

		src.m_datakind					= mste.datakindName;
		src.m_valueCol					= mste.column;
		src.m_valueColCharacteristic	= mste.columnCharacteristic;
		src.m_validInterval				= mste.validInterval;

		const mbDatakind& dk = m_datakindMappings.getDatakind( src.m_datakind );
		mbReferenceTypeEnum refType = dk.m_referenceType;
		src.m_idColumn = dk.m_idColumn;

		hbString errTxt;
		if( refType != mbTLS_Q_COORD_SUPPLY )
		{
			if( !MST_LOC_MATCH.initLocClass( dk.m_objectType, errTxt ) )
			{
				mstBuilderException ex;
				ex.code = MB_ERR_DETERMINE_FLT_LOCS;
				ex.description = hbString( errTxt ).c_str();
				ex.source = hbString("mstBuilderImpl::buildRecords()").c_str();

				throw ex;
			}
		}
		else
			MST_LOC_MATCH.initLocClass( "", errTxt );

		const mbColumn& col = m_datakindMappings.getColumn( src.m_datakind, src.m_valueCol, 
													        src.m_valueColCharacteristic );
		src.m_validColumn = col.m_validColumn;
		list<hbString>::const_iterator its;
		for( its = col.m_invalidValues.begin(); its != col.m_invalidValues.end(); ++its )
		{
			if( its != col.m_invalidValues.begin() )
				src.m_invalidValues += ";";
			src.m_invalidValues += *its;
		}

		const mbMapping& mapping = m_datakindMappings.getMapping( src.m_datakind, src.m_valueCol, 
																  src.m_valueColCharacteristic );

		const mbMappingTarget& mapTarget = mapping.m_trg;

		if( mapping.m_mappingType == mbFunction )
			src.m_mappingType	= mapping.m_mappingFunction;
		else
			src.m_mappingType	= toString( mapping.m_mappingType );

		src.m_mappingFactor	= mapping.m_factor;
		src.m_valueDataType = mapping.m_valueDataType;

		src.m_d2VehType = toString( mapTarget.m_vehType );
   		src.m_d2DataType = toString( mapTarget.m_valueType );
		src.m_d2BasicDataType = mapTarget.m_basicDataType;
		src.m_d2ValueElement = mapTarget.m_valueElement;
		src.m_d2Value = mapTarget.m_value;
		src.m_d2ValueInner = mapTarget.m_valueInner;
		src.m_d2ValuePath = mapTarget.m_valuePath;

		src.m_period = mste.period;
		src.m_filterColumn = mste.filterColumn;
		unsigned num = mste.filterValue.length();
		for( unsigned i = 0; i < num; i++ )
		{
			const char* fv = mste.filterValue[i];
			src.m_filterValues.push_back( fv );
		}

		m_currentSources.push_back( src );

		LogB1( "Source: " + src.toStringShort() )


		// Bestimme EA-IDs 

		vector<long> eaids;

		m_currentObjType = m_datakindMappings.getObjType( src.m_datakind );	

		if( refType == mbTLS )	
		{
			getEAIDs( src.m_datakind, eaids, errTxt );
			if( !m_currentObjType.empty() )
				readLocations( m_currentObjType );
		}
		else if( refType == mbTLS_Q_COORD_SUPPLY )	
			getQIDs( dk.m_objectType.toL(), eaids, errTxt );
		else
			getLocIds( m_currentObjType, eaids );


		// Iteriere über EA-IDs, bestimme Locations

		vector<long>::iterator itea;
		for( itea = eaids.begin(); itea != eaids.end(); ++itea )
		{
			long eaid = *itea;

			if( m_notLocMatchingEAIDs[src.m_datakind].find( eaid ) != m_notLocMatchingEAIDs[src.m_datakind].end() )
				continue;

			LogB2( "Item-ID: " << eaid )

			mbMSTItem item;


			map< long, mbMSTLocation >::iterator itl = m_eaid2Loc.find( eaid );
			if( itl == m_eaid2Loc.end() )
			{
				long qid = getLocId( refType, eaid );
				long _locId = qid2LocId( qid );

				if( _locId == -1 )
				{
					m_notLocMatchingEAIDs[src.m_datakind].insert(eaid);
					LogB2( hbString("No location for item with EAID '") + eaid + hbString("'") )
					continue;
				}

				map< long, mbMSTLocation >::iterator _itl = m_currentLocs.find( _locId );
				if( _itl != m_currentLocs.end() )
				{
					m_eaid2Loc[ eaid ] = _itl->second;
					itl = m_eaid2Loc.find( eaid );
				}
				else
				{
					mbMSTLocation loc;

					loc.m_mst		= mstName;
					loc.m_id		= ++m_currentLocCount;
					loc.m_locId		= _locId;

					if( m_carriagewayFromGlane )
					{
						int cw = getCarriageway( qid );
						loc.m_carriageway = toD2Carriageway( cw );
					}

					if( refType != mbTLS_Q_COORD_SUPPLY)
					{
						hbString errTxt;
						if( !getLocation( _locId, refType, loc, errTxt ) )
						{
							m_notLocMatchingEAIDs[src.m_datakind].insert(eaid);
							LogB3( hbString("No location for item with EAID '") + eaid + hbString("'") )
							continue;
						}

						if( refType != mbGEODYN2_REF_NON_Q )
						{
							if( !getQName( hbString(oneMST.version), 
										   hbString(oneMST.refVersion), 
										   qNameMode,
										   qid, loc, errTxt ) )
							{
								m_notLocMatchingEAIDs[src.m_datakind].insert(eaid);
								LogB3( hbString("No Q name for item with EAID '") + eaid + hbString("'") )
								continue;
							}
						}
						else
						{
							if( !getLocName( _locId, loc ) )
							{
								m_notLocMatchingEAIDs[src.m_datakind].insert(eaid);
								LogB3( hbString("No name for object with ID '") + _locId + hbString("'") )
								continue;
							}
						}
					}
					else
					{
						hbString errTxt;
						if( !getLocationQ( refType, qNameMode, _locId, loc, errTxt ) )
						{
							m_notLocMatchingEAIDs[src.m_datakind].insert(eaid);
							LogB3( hbString("No location for item with EAID '") + eaid + hbString("'") )
							continue;
						}
					}

					if( refType == mbTLS_Q_COORD_SUPPLY)
					{
						if( !MST_LOC_MATCH.matchName( loc.m_locName ) )
						{
							m_notLocMatchingEAIDs[src.m_datakind].insert(eaid);
							continue;
						}
					}

					if( loc.m_equipment.empty() )
						loc.m_equipment = dk.m_equipment;

					m_eaid2Loc[eaid] = loc;

					itl = m_eaid2Loc.find( eaid );
				}
			}

			mbMSTLocation& loc = itl->second;
			long locId = loc.m_locId;
			
			if( m_currentLocs.find( locId ) == m_currentLocs.end() )
				m_currentLocs[ locId ] = loc;

			item.m_mst		= oneMST.name;
   			item.m_id		= ++m_currentItemCount;
   			item.m_dkRef	= src.m_id;
   			item.m_dkId		= eaid;
   			item.m_locId	= loc.m_id;

			item.m_lane = "unknown";
			if( refType == mbTLS )	
			{
				int laneNum = getLaneNumber( eaid );
				if( laneNum != -1 )
				{
					hbString lstr = hbString() + laneNum;
					bool hasSubType = false;
					if( dk.m_subTypes.size() > 0 )
					{
						hasSubType = false;
						list<hbString>::const_iterator it;
						for( it = dk.m_subTypes.begin(); it != dk.m_subTypes.end(); ++it )
						{
							if( *it == lstr )
							{
								hasSubType = true;
								break;
							}
						}
					}
	
					if( !hasSubType )
						continue;

					hbString laneStr = toD2Lane( laneNum );

					if( !laneStr.empty() )
						item.m_lane = laneStr;
				}
			}
			else
				item.m_lane = "allLanesCompleteCarriageway";

			item.m_carriageway = loc.m_carriageway;

			map< long, unsigned >::iterator _it = m_currentLocIndex.find( locId );
			if( _it == m_currentLocIndex.end() )
				m_currentLocIndex[ locId ] = 0;
			item.m_index	= ++m_currentLocIndex[ locId ];

			item.m_name		= loc.m_locName;	

			if( refType == mbTLS )	
	   			item.m_period = getCycleLength( eaid );
			else if( refType == mbTLS_Q_COORD_SUPPLY )	
	   			item.m_period = getCycleLengthZQL_Q( eaid );
			else	
	   			item.m_period = getCycleLengthQ( eaid );

			m_currentItems.push_back( item );
		}
	}

}

void mstBuilderImpl::buildNextMST()
{
	mstDefinition nextMST;

	try
	{
		{
			hbSynchronized sync( this );

			if( m_requests.size() == 0 )
				return;

			mstRequest request = m_requests.front();
			m_requests.pop_front();

			m_currentMSTId = request.getMstId();
			m_currentMSTDefVersion = request.getDefVersion();
			m_currentMSTVersion = request.getMstVersion();

			nextMST = createMSTDefinition( request );

			m_currentBuildStart = hb_time(0);

			cLog << "Build MST '" << m_currentMSTId << "[" << m_currentMSTVersion << "]'" << endl;

			updateState( m_currentMSTId, m_currentMSTVersion, buildStarted, 1, hbString() ); 
		}

		buildRecords( nextMST );
		saveMST( nextMST, m_currentLocs, m_currentSources, m_currentItems, savingRecords );

		{
			hbSynchronized sync( this );
			updateState( m_currentMSTId, m_currentMSTVersion, recordBuildingFinished, 1, hbString() ); 
		}

		cLog << endl << endl << "MST '" << m_currentMSTId << "[" << m_currentMSTVersion << "]' erfolgreich erzeugt" << endl;
	}
	catch( const mstBuilderException& ex )
	{
		hbSynchronized sync( this );

		updateState( m_currentMSTId, m_currentMSTVersion, buildFailed, 0, toString( ex ) ); 
		cLog << endl << endl << "Fehler beim Erzeugen der MST '" << m_currentMSTId << "[" << m_currentMSTVersion << "]'" << endl;
	}

	{
		hbSynchronized sync( this );

		m_currentMSTId = "";
		m_currentMSTDefVersion = "";
		m_currentMSTVersion = "";
	}
}


void mstBuilderImpl::saveMST( mstDefinition oneMST, 
							  const mbMSTLocations& locs, 
							  const mbMSTSources& sources, 
							  const mbMSTItems& items,
							  mstBuilder::state S )
{
	LogMethod0( "mstBuilderImpl", "saveMST" )

	hbString mstName = hbString(oneMST.name);
	time_t now = hb_time(0);

	updateState( mstName, hbString(oneMST.version),S, 1, hbString() ); 

	try
	{
		hbString condition = "mst = '" + mstName + "'";
		condition += " and (version = '" +  oneMST.version + "')";

		// erase existing records

		ddpTransaction T( *m_connection );
		T.erase( "datex2MSTLocation", condition );
		T.erase( "datex2MSTDataSource", condition );
		T.erase( "datex2MSTDataSourceFilter", condition );
		T.erase( "datex2MSTItem", condition );
		//T.erase( "datex2MSTObjectFilter", condition );
		//T.erase( "datex2MSTRoadFilter", condition );
		// T.erase( "datex2MST", condition );

		ddpObject mstObj( *m_connection, "datex2MST" );

		// d2MST
		mstObj["time"]				= now;
		mstObj["mst"]				= oneMST.name;
		mstObj["version"]			= oneMST.version;
		mstObj["description"]		= oneMST.description;
		mstObj["d2Version"]			= m_d2Version;
		mstObj["defVersion"]		= m_currentMSTDefVersion;
		mstObj["active"]			= oneMST.active;

		ddpList mstLst;
		mstLst.push_back( mstObj );
		T.put( mstLst );

		ddpList locLst;
		mbMSTLocations::const_iterator itl;
		for( itl = locs.begin(); itl != locs.end(); ++itl )
		{
			const mbMSTLocation& loc = itl->second;

			map< long, unsigned >::iterator it = m_currentLocIndex.find( loc.m_locId );
			if( it == m_currentLocIndex.end() )
				continue;

			ddpObject locObj( *m_connection, "datex2MSTLocation" );
			locObj["time"]				= now;

			locObj["mst"] = mstName;

			if( m_connection->datakind("datex2MSTLocation").hasColumn("version") )
				locObj["version"] = oneMST.version;

			locObj["id"] = loc.m_id;
			locObj["geoDynId"] = loc.m_locId;

			locObj["d2Id"] = loc.m_d2Id;

			locObj["locName"] = loc.m_locName;
			locObj["equipment"] = loc.m_equipment;

   			locObj["rdsLocTblRef"] = loc.m_rdsLocTblRef;
   			locObj["rdsLocTblVer"] = loc.m_rdsLocTblVer;

   			locObj["rdsPrimLocCode"] = loc.m_rdsPrimLocCode;
   			locObj["rdsSecLocCode"] = loc.m_rdsSecLocCode;

   			locObj["rdsDirection"] = loc.m_rdsDirection;
			locObj["rdsPrimLocDist"] = loc.m_rdsPrimLocDist;
			locObj["rdsSecLocDist"] = loc.m_rdsSecLocDist;

   			locObj["startCoordX"] = loc.m_startCoordX;
   			locObj["startCoordY"] = loc.m_startCoordY;

   			locObj["endCoordX"] = loc.m_endCoordX;
   			locObj["endCoordY"] = loc.m_endCoordY;

			locLst.push_back( locObj );
		}
		T.put( locLst );
		cLog << "# Locations: " << locLst.size() << endl;

		ddpList srcLst;
		ddpList srcFltLst;
		mbMSTSources::const_iterator its;
		for( its = sources.begin(); its != sources.end(); ++its )
		{
			const mbMSTSource& src = *its;

			ddpObject srcObj( *m_connection, "datex2MSTDataSource" );
			srcObj["time"]				= now;
			srcObj["mst"] = src.m_mst;

			if( m_connection->datakind("datex2MSTDataSource").hasColumn("version") )
				srcObj["version"] = oneMST.version;

			srcObj["id"] = src.m_id;

			srcObj["datakind"] = src.m_datakind;
			srcObj["valueCol"] = src.m_valueCol;

   			srcObj["valueColCharacteristic"] = src.m_valueColCharacteristic;
   			srcObj["validInterval"] = src.m_validInterval;

			srcObj["idColumn"] = src.m_idColumn;
			srcObj["validColumn"] = src.m_validColumn;
			srcObj["invalidValues"] = src.m_invalidValues;

   			srcObj["d2VehType"] = src.m_d2VehType;
   			srcObj["d2DataType"] = src.m_d2DataType;
			srcObj["d2BasicDataType"] = src.m_d2BasicDataType;
			srcObj["d2ValueElement"] = src.m_d2ValueElement;
			srcObj["d2Value"] = src.m_d2Value; 
			srcObj["d2ValueInner"] = src.m_d2ValueInner; 
			srcObj["d2ValuePath"] = src.m_d2ValuePath; 

			srcObj["mappingType"] = src.m_mappingType;
			srcObj["factor"] = src.m_mappingFactor; 
			srcObj["valueDataType"] = src.m_valueDataType;

			srcLst.push_back( srcObj );

			if( ( ( src.m_period != -1 ) && ( src.m_period != 0 ) ) 
				|| 
				!src.m_filterColumn.empty() )
			{
				ddpObject srcFltObj( *m_connection, "datex2MSTDataSourceFilter" );
				srcFltObj["time"]		= now;
				srcFltObj["mst"]		= src.m_mst;
				srcFltObj["version"]	= oneMST.version;
				srcFltObj["id"]			= src.m_id;

				srcFltObj["period"] = src.m_period;
				srcFltObj["filterCol"] = src.m_filterColumn;

				hbString fvs;
				list<hbString>::const_iterator its;
				for( its = src.m_filterValues.begin(); its != src.m_filterValues.end(); ++its )
				{
					if( !fvs.empty() )
						fvs += ",";
					fvs += *its;
				}
				srcFltObj["filterValues"] = fvs;

				srcFltLst.push_back( srcFltObj );
			}
		}

		T.put( srcLst );
		cLog << "# Sources: " << srcLst.size() << endl;

		if( srcFltLst.size() )
			T.put( srcFltLst );

		cLog << "# SourceFilters: " << srcFltLst.size() << endl;

		ddpList itemLst;
		mbMSTItems::const_iterator iti;
		for( iti = items.begin(); iti != items.end(); ++iti )
		{
			const mbMSTItem& item = *iti;

			ddpObject itemObj( *m_connection, "datex2MSTItem" );
			itemObj["time"]		= now;

			itemObj["mst"]		= item.m_mst;
			if( m_connection->datakind("datex2MSTItem").hasColumn("version") )
				itemObj["version"] = oneMST.version;

			itemObj["id"]		= item.m_id;

			itemObj["index"] = item.m_index;

			itemObj["name"] = item.m_name;
			itemObj["carriageway"] = item.m_carriageway;
			itemObj["lane"] = item.m_lane;
   			itemObj["period"] = item.m_period;

   			itemObj["dkRef"] = item.m_dkRef;
   			itemObj["dkId"] = item.m_dkId;
   			itemObj["locId"] = item.m_locId;

			itemLst.push_back( itemObj );
		}
		T.put( itemLst );
		cLog << "# Items: " << itemLst.size() << endl;

		T.commit();
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_HB_EXCEPTION;
		ex.description = hbString( _ex.what() ).c_str();
		ex.source = hbString("mstBuilderImpl::saveMST()").c_str();

		throw ex;
	}
}




/////////////////////////////////////////////////////////////////////////////////////////////////
// CORBA Interface
/////////////////////////////////////////////////////////////////////////////////////////////////

::mstBuilder::mstDatakinds * mstBuilderImpl::getAllDatakinds()
		ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException))
{
	LogMethodIDL1( "mstBuilderImpl", "getAllDatakinds" )

	mstDatakinds_var mstds = new mstDatakinds();

	const list<mbDatakindMapping>& mappings = m_datakindMappings.getDkMappings();

	mstds->length( mappings.size() );
	unsigned i = 0;
	list<mbDatakindMapping>::const_iterator it;
	for( it = mappings.begin(); it != mappings.end(); ++it, i++ )
	{
		const mbDatakindMapping& mstd = *it;

		mstds[i].name = mstd.m_datakind.m_name.c_str();
		mstds[i].description = mstd.m_datakind.m_description.c_str();

		mstds[i].columns.length( mstd.m_mappings.size() );

		const list<mbMapping>& ms = mstd.m_mappings;

		unsigned j = 0;
		list<mbMapping>::const_iterator itm;
		for( itm = ms.begin(); itm != ms.end(); ++itm, j++ )
		{
			const mbMapping& m = *itm;
			mstds[i].columns[j].name = m.m_src.m_valColumn;
			mstds[i].columns[j].defaultValidInterval = mstd.m_datakind.m_defaultValidInterval;

			mbColumn mbc = mstd.m_datakind.getColumn( m.m_src.m_valColumn );
			if( !mbc.valid() )
			{
				mstBuilderException ex;
				ex.code = MB_INTERNAL_ERROR;
				ex.description = ( "Column not found '" + hbString(m.m_src.m_valColumn) + "'" ).c_str();
				ex.source = hbString("mstBuilderImpl::mstBuilderImpl()").c_str();

				throw ex;
			}

			if( mbc.m_valColumnCharacteristic.empty() )
				mstds[i].columns[j].characteristic = mbc.m_valColumnDescription;
			else
				mstds[i].columns[j].description = ( mbc.m_valColumnDescription + "(" + mbc.m_valColumnCharacteristicDescription + ")" ).c_str();
		}
	}

	return mstds._retn();
}



::mstBuilder::mstInfos * mstBuilderImpl::getAllMST()
		ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException))
{
	LogMethodIDL1( "mstBuilderImpl", "getAllMST" )

	hbSynchronized sync( this );

	mstInfos_var msts = new mstInfos();
	msts->length( m_msts.size() );

	map< hbString, mstDefinition >::iterator itm;
	unsigned i = 0;
	for( itm = m_msts.begin(); itm != m_msts.end(); ++itm, i++ )
	{
		msts[i].mstData = itm->second;

		map< hbString, mstState  >::iterator its = m_mst2State.find( itm->first );
		if( its != m_mst2State.end() )
			msts[i].mstState = its->second;
		else
		{
			msts[i].mstState.buildHasStarted = 0;
			msts[i].mstState.error = hbString().c_str();
			msts[i].mstState.currentState = mstBuilder::unknown;
		}
	}

	return msts._retn();
}

mstBuilder::mstDefinition mstBuilderImpl::createMSTDefinition( mstRequest request )
{
	mstBuilder::mstDefinition oneMST;
	hbString error;

	if( !readMSTDefinition( request.getMstId(), request.getDefVersion(), oneMST, error ) )
	{
		mstBuilderException ex;
		ex.code = MB_MST_CURRENTLY_BUILD;
		ex.description = error.c_str();
		ex.source = hbString("mstBuilderImpl::createMSTDefinition()").c_str();

		throw ex;
	}

	oneMST.version = request.getMstVersion(); 
	oneMST.refVersion = request.getRefVersion();

	long errCode;
	hbString errTxt;
	if( !checkMST( oneMST, errCode, errTxt ) )
	{
		mstBuilderException ex;
		ex.code = errCode;
		ex.description = errTxt.c_str();
		ex.source = hbString("mstBuilderImpl::createMSTDefinition()").c_str();

		throw ex;
	}

	return oneMST;
}

void mstBuilderImpl::createMST( const char * _mstId, 
							    const char * _mstVersion, 
							    const char * defVersion, 
								const char * refVersion,
								const mstBuilder::mstFilter& filter )
		ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ))
{
	LogMethodIDL1( "mstBuilderImpl", "createMST" )

	cLog << "mstId:	     " << _mstId << endl;
	cLog << "mstVersion: " << _mstVersion << endl;
	cLog << "defVersion: " << defVersion << endl;
	cLog << "refVersion: " << refVersion << endl;

	hbSynchronized sync( this );

	hbString mstId = _mstId;
	hbString mstVersion = _mstVersion;

	mstRequest request( mstId, mstVersion, defVersion, refVersion );
	m_requests.push_back( request );

	if( ( mstId != m_currentMSTId ) || ( mstVersion != m_currentMSTVersion ) )
	{
		updateState( mstId, mstVersion, buildQueued, 1, hbString() ); 
	}
}


void mstBuilderImpl::writeMST( const mstBuilder::mstDefinition & oneMST )
		ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ))
{
	LogMethodIDL1( "mstBuilderImpl", "writeMST" )

	mstBuilderException ex;
	ex.code = MB_ERR_METHOD_NOT_SUPPORTED;
	ex.description = hbString("Method not implemented !" ).c_str();
	ex.source = hbString("mstBuilderImpl::writeMST()").c_str();

	throw ex;
}



void mstBuilderImpl::deleteMST( const char * _mstName, const char * _version )
		ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ))
{
	LogMethodIDL1( "mstBuilderImpl", "deleteMST" )

	hbSynchronized sync( this );

	hbString mstName = _mstName;
	hbString version = _version;

	if( ( mstName == m_currentMSTId ) && ( version == m_currentMSTVersion ) )
	{
		mstBuilderException ex;
		ex.code = MB_MST_CURRENTLY_BUILD;
		ex.description = ( "MST '" + hbString(mstName) + "' currently built" ).c_str();
		ex.source = hbString("mstBuilderImpl::deleteMST()").c_str();

		throw ex;
	}

	try
	{
		hbString condition = "(mst = '" + mstName + "')";
        condition += " and (version = '" +  version + "')";

		if( m_msts.find( mstName ) == m_msts.end() )
		{
			mstBuilderException ex;
			ex.code = MB_UNKNOWN_MST_NAME;
			ex.description = ( "Unknown MST '" + mstName + "'" ).c_str();
			ex.source = hbString("mstBuilderImpl::deleteMST()").c_str();

			throw ex;
		}

		ddpTransaction T( *m_connection );
		T.erase( "datex2MSTLocation", condition );
		T.erase( "datex2MSTDataSource", condition );
		T.erase( "datex2MSTItem", condition );
		//T.erase( "datex2MSTObjectFilter", condition );
		//T.erase( "datex2MSTRoadFilter", condition );
		T.erase( "datex2MST", condition );

		T.commit();

		hbString mstKey = toMSTKey( mstName, _version );

		m_msts.erase( mstKey );
		m_mst2State.erase( mstKey );


		// remove pending build requests

		list<mstRequest>::iterator itn = m_requests.begin();
		while( itn != m_requests.end() )
		{
			if( ( itn->getMstId() == mstName ) && ( itn->getMstVersion() == _version ) )
				itn = m_requests.erase( itn );
			else
				++itn;
		}
	}
	catch( const hbException& _ex )
	{
		mstBuilderException ex;
		ex.code = MB_HB_EXCEPTION;
		ex.description = hbString( _ex.what() ).c_str();
		ex.source = hbString("mstBuilderImpl::deleteMST()").c_str();

		throw ex;
	}
}



::mstBuilder::mstBuilderState * mstBuilderImpl::getStatus( const char * _mstName, const char * _version )
		ACE_THROW_SPEC (( CORBA::SystemException, mstBuilder::mstBuilderException ))
{
	LogMethodIDL1( "mstBuilderImpl", "getStatus" )

	hbSynchronized sync( this );

	hbString mstName = _mstName;
	hbString version = _version;
	hbString mstKey = toMSTKey( mstName, version );

	map< hbString, mstState >::iterator its = m_mst2State.find( mstKey );
	if( its == m_mst2State.end() )
	{
		mstBuilderException ex;
		ex.code = MB_UNKNOWN_MST_NAME;
		ex.description = ( "Unknown MST '" + mstName + "[" + version + "]'" ).c_str();
		ex.source = hbString("mstBuilderImpl::getStatus()").c_str();

		throw ex;
	}

	mstBuilderState_var S = new mstBuilderState();
	S->name = mstName.c_str();
	S->state = its->second;

	S->version = _version;

	return S._retn();
}



/////////////////////////////////////////////////////////////////////////////////////////////////
// DDP-Query-Interface
/////////////////////////////////////////////////////////////////////////////////////////////////
void mstBuilderImpl::callback(CallbackType type)
{
	try
	{
		ddpObject obj;
		
		while( getNextObject( obj ) )
		{
			long id = obj[COL_NAME_EAID];
			if( obj.action() != ddpActionErase )
			{
				unsigned len = obj["IVLaengeKurz"].asUnsignedInt();
				if( len )
					m_eaid2Cycle [ id ] = len * 15;
				else
					m_eaid2Cycle.erase( id );
			}
			else
				m_eaid2Cycle.erase( id );
		}
	}
	catch( const hbException& ex )
	{
		HbErr( "Fehler in DDP-Query zu 'DaLVEBetriebsParamIst: \n'" + hbString(ex.what()) )	
		HbErr( "Prozess wird beendet !" )	
		::exit(-1);
	}
}

void mstBuilderImpl::error( long errorCode, const char* errorText )
{
	HbErr( "Fehler in DDP-Query zu 'DaLVEBetriebsParamIst: \n'" + hbString(errorText) )	
	HbErr( "Prozess wird beendet !" )	
	::exit(-1);
}

static hbString toString( const mstState& S )
{
	return hbString("state: ") + S.currentState + 
		   ", buildHasStarted: " + hbString( S.buildHasStarted ? "true" : "false" ) +
		   ", error: " + hbString( S.error );		
}

void mstBuilderImpl::saveState( const hbString& mstName, 
							    const hbString& version,
							    const mstState& S )
{
	try
	{
		ddpTransaction T( *m_connection );

		ddpObject stateObj( *m_connection, "datex2MSTBuildState" );

		stateObj["time"]			= hb_time(0);
		stateObj["mst"]				= mstName;
		stateObj["version"]			= version;

		if( S.currentState == mstBuilder::buildQueued )
			stateObj["buildStarted"]	= hb_time(0);
		else
			stateObj["buildStarted"]	= m_currentBuildStart;

		stateObj["state"]			= toDBState( S.currentState );
		stateObj["result"]			= hbString( S.error );

		ddpList Lst;
		Lst.push_back( stateObj );

		T.put( Lst );

		T.commit();
	}
	catch( const hbException& ex )
	{
		hbString errTxt = hbString( "Fehler beim Schreiben des Status des MST '" + mstName + 
									"': {" + toString( S ) + "}\n") + ex.what();
		HbErr( errTxt )
		::exit(-1);
	}
}

void mstBuilderImpl::updateState( const hbString& mstId, 
								  const hbString& version,
								  mstBuilder::state newState,
								  bool bhs,
								  const hbString& error )
{
	mstState state;

	hbString mstKey = toMSTKey( mstId, version );

	{
		hbSynchronized sync( this );

		map< hbString, mstState >::iterator its = m_mst2State.find( mstKey );
		if( its != m_mst2State.end() )
		{
			its->second.buildHasStarted = bhs;
			its->second.currentState = newState;
			its->second.error = error.c_str();

			state = its->second;
		}
		else
		{
			state.buildHasStarted = bhs;
			state.error = error.c_str();
			state.currentState = newState;

			m_mst2State[ mstKey ] = state;

		}
	}

	saveState( mstId, version, state );
}

								   

