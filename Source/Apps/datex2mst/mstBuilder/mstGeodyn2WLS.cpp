#ifdef WIN32
#pragma warning (disable : 4786)
#endif


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
#include "mstGeodyn2WLS.h"

#include "mstBuilderLib.h"
using namespace mstBuilder;

#include "CORBAObjectHandle.h"
#include "mstConst.h"

mstGeodyn2WLS* mstGeodyn2WLS::_instance;

// static CORBAObjectHandle<geodyn2WLSService::Service> serviceHandle = 0;


void mstGeodyn2WLS::connect( ddpConnection* connection,
							 locConnection* locConnection,
							 const hbString& geodyn2WLSAddress )
{
	m_connection = connection;
	m_locConnection = locConnection;

	if( !geodyn2WLSAddress.empty() )
	{
		// Verbindung zum CORBAService aufnehmen ...
		try
		{
			m_serviceHandle = COF.getServer( geodyn2WLSAddress, (geodyn2WLSService::Service*)0, true, UINT_MAX, 15 );
		}
		catch( const CORBA::Exception& cex )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_CONN_GEODYN_WLS_SRV;
			ex.description = "Fehler bei Verbindungsaufnahme zum CORBA-Server\n" + 
							 what( cex );
			ex.source = hbString("mstGeodyn2WLS::connect()").c_str();

			throw ex;
		}
	}
}

bool mstGeodyn2WLS::connected()
{
	return m_serviceHandle.valid();
}

locLocation::Direction toGeodynDir( geodyn2WLSService::AlertCDirection dir )
{
	switch( dir )
	{
		case geodyn2WLSService::positive:
			return locLocation::Negativ;
		case geodyn2WLSService::negative:
			return locLocation::Positiv;
		case geodyn2WLSService::both:
			return locLocation::Both;
		case geodyn2WLSService::none:
			return locLocation::None;
	}

	return locLocation::None;
}

void mstGeodyn2WLS::getRdsInfo( const hbString& objType, 
							    const list<long> locIds,
								map<long,locRDSInfo>& lRdsInfos )
{
	if( !m_serviceHandle.valid())
		return;

	map<long,RoadElementPosition> rePositions;
	if( objType == TR_DETECTION_SITE_DK )
	{
		getMatchingREPositions( TR_DETECTION_SITE_DK, 
								TR_ROAD_ELEMENT_DK,
								locIds,
								rePositions );
	}

	LocationLst locLst;
	locLst.length( locIds.size() );
	list<long>::const_iterator itl;
	int i = 0;
	for( itl = locIds.begin(); itl != locIds.end(); ++itl )
	{
		long locId = *itl;

		geodyn2WLSService::Location loc;
		loc.objId = locId;
		loc.objType = objType.c_str();
		loc.coordX = -1.0;
		loc.coordY = -1.0;
		loc.reId = -1;
		loc.reOffset = -1;

		map<long,RoadElementPosition>::iterator itre = rePositions.find(locId);
		if( itre != rePositions.end() )
		{
			RoadElementPosition& rePos = itre->second;
			loc.reId = rePos.m_reId;
			loc.reOffset = rePos.m_offset;
		}

		locLst[i] = loc;
		i++;
	}
	geodyn2WLSService::RdsInfoLst_var rdsInfos = m_serviceHandle->getRdsInfo( locLst );

	for( unsigned i = 0; i < rdsInfos->length(); i++ )
	{
		geodyn2WLSService::RdsInfo& rdsInfo = rdsInfos[i];
		locRDSInfo lRdsInfo;

		lRdsInfo.dbid( rdsInfo.rdsDbId + ":" + rdsInfo.rdsDbVersion );
		lRdsInfo.primaryLoc( rdsInfo.rdsPrimCode );
		lRdsInfo.secondaryLoc( rdsInfo.rdsSecCode );
		lRdsInfo.primaryOffset( rdsInfo.rdsPrimOffset );
		lRdsInfo.secondaryOffset( rdsInfo.rdsSecOffset );

		if( rdsInfo.alcLocType == geodyn2WLSService::primary )
		{
			lRdsInfo.primaryOffset( 0 );
			lRdsInfo.secondaryOffset( 0 );
			lRdsInfo.secondaryLoc( rdsInfo.rdsPrimCode );
		}
		else if( rdsInfo.alcLocType == geodyn2WLSService::primarySecondary )
		{
			lRdsInfo.primaryOffset( 0 );
			lRdsInfo.secondaryOffset( 0 );
		}
		else if( rdsInfo.alcLocType == geodyn2WLSService::primaryWithDistance )
		{
			lRdsInfo.secondaryOffset( rdsInfo.rdsPrimOffset );
			lRdsInfo.secondaryLoc( rdsInfo.rdsPrimCode );
		}

		lRdsInfo.extent( 0 );
		lRdsInfo.direction( toGeodynDir( rdsInfo.direction ) );
		lRdsInfo.method( locLocation::SingleOffset );

		lRdsInfos[ rdsInfo.objId ] = lRdsInfo;
	}
}


void  mstGeodyn2WLS::getMatchingREPositions( const hbString& objType, 
											 const hbString& matchingType,
											 const list<long>& IDs,
											 map<long,RoadElementPosition>& positions )
{
	long objTypeId = m_connection->datakind( objType ).id();
	long matchingTypeId = m_connection->datakind( matchingType ).id();

	list< pair<long,locMatch> > match_lst;
	m_locConnection->getMatchingLocations( objTypeId, 
										   matchingTypeId, 
										   match_lst );

	list< pair<long,locMatch> >::iterator it;
	for( it = match_lst.begin(); it != match_lst.end(); ++it )
	{
		long locId = it->first;
		locMatch& lm = it->second;

		RoadElementPosition rePos;
		rePos.m_reId = lm.id();
		rePos.m_offset = lm.offset();

		positions[ locId ] = rePos;
	}
}


