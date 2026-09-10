// ******************************************************************************************
//
//	Author   : P. Schmitz/Heusch-Boesefeldt GmbH
//	Kommentar: 
//			   
//
// ******************************************************************************************
#ifdef WIN32
#pragma warning ( disable : 4786 4996)
#include <winsock2.h>
#endif

#include "trMisc.h"

#include "d2MSTDbBuilderIfaceImpl.h"
#include "LogBase.h"


#include <hbMTSafeQTmpl.h>
#include <hbSync.h>
#include "d2Conf.h"
#include "loc_match.h"
#include "xmlDefReader.h"
#include "state.h"
#include "ddp.h"
#include "handler.h"



///////////////////////////////////////////////////////////////////////////////////////////////
// ddpMonStateSrvImpl
///////////////////////////////////////////////////////////////////////////////////////////////


#define ERR_MST_PARSE_DEF 1
#define ERR_MST_CONTENT   2


static bool checkMST( const hbString& definition, d2MST& mst, int& errCode, hbString& errTxt )
{
	d2RoadFilterLst			roadFilter;
	d2ObjNameFilterLst		objNameFilter;
	d2DkAttrDefLst			dkAttrDefLst;

	cLog << definition << endl;

	if( !XML_DEF_READER.parseDef( definition, mst, roadFilter, objNameFilter, dkAttrDefLst, errTxt ) )
	{
		errTxt = "Fehlerhafte MST-Definition: " + errTxt;
		errCode = ERR_MST_PARSE_DEF;
		return false;
	}

	//if( !LOC_MATCH.init( roadFilter, objNameFilter ) )
	//{
	//	errTxt = "Ungueltige Location-Filter";
	//	errCode = ERR_MST_CONTENT;
	//	return false;
	//}

	d2DkAttrDefLst::iterator it;

	for( it = dkAttrDefLst.begin(); it != dkAttrDefLst.end(); ++it )
	{
		d2DkAttrDef& def = *it;
		if( !HANDLER.check( mst, def, errTxt ) )
		{
			errCode = ERR_MST_CONTENT;
			return false;
		}
	}


	return true;
}


void d2MSTDbBuilderIfaceImpl::createMST( const char * mstDef )
			throw ( CORBA::SystemException, d2MSTDbBuilderIface::Error )
{
	LogMethodIDL0( "d2MSTDbBuilderIfaceImpl", "createMST" )

	d2MST mst;
	hbString errTxt;
	int errCode;
	if( !checkMST( mstDef, mst, errCode, errTxt ) )
	{
		d2MSTDbBuilderIface::Error E;
		E.source = hbString( "checkMST()" ).c_str();
		E.number = errCode;
		E.message = errTxt.c_str();

		throw E;
	}


	hbSynchronized sync( this );

	m_mstDefs.push_back( mstDef );
	m_mstIds.push_back( mst.getName() );

	if( mst.getName() != HANDLER.currentMST() )
		writeState( mst.getName(), mst.getVersion(), buildQueued, "" );
}


static bool _deleteMST( const hbString& mstId, hbString& errTxt )
{
	try
	{
		ddpTransaction T( *handler::ddpConn );

		hbString cond = "mst='" + mstId + "'";

		T.erase( "d2MSTLocation", cond );
		T.erase( "d2MSTDataSource", cond );
		T.erase( "d2MSTItem", cond );
		T.erase( "d2MSTBuildState", cond );
		T.erase( "d2MSTDef", cond );
		T.erase( "f_D2MdpPub_generator", cond );
		T.erase( "d2MST", cond );

		T.commit();

		return true;
	}
	catch( const hbException& ex )
	{
		HbErr( "Fehler beim Loeschen der MST'" << mstId << "'" << endl << ex )

		errTxt = ex.what();

		return false;
	}
};


static bool _hasMST( const hbString& mstId, unsigned& num, hbString& errTxt )
{

	try
	{
		ddpQuery Q( *handler::ddpConn, "d2MST" );

		ddpList Lst;
		hbString cond = "mst='" + mstId + "'";
		Q.condition( cond );

		Q.read( Lst );

		num = Lst.size();

		return true;
	}
	catch( const hbException& ex )
	{
		HbErr( "Fehler beim Loeschen der MST '" << mstId << "'" << endl << ex )

		errTxt = ex.what();

		return false;
	}
}

void d2MSTDbBuilderIfaceImpl::deleteMST( const char * mstId )
			throw ( CORBA::SystemException, d2MSTDbBuilderIface::Error )
{
	LogMethodIDL0( "d2MSTDbBuilderIfaceImpl", "deleteMST" )

	hbString errTxt;
	unsigned num;
	if( !_hasMST( mstId, num, errTxt ) )
	{
		d2MSTDbBuilderIface::Error E;
		E.source = hbString( "deleteMST()" ).c_str();
		E.number = d2MSTDbBuilderIface::ERR_MST_DB_ERR;
		E.message = errTxt.c_str();

		throw E;
	}
	if( num == 0 )
	{
		d2MSTDbBuilderIface::Error E;
		E.source = hbString( "deleteMST()" ).c_str();
		E.number = d2MSTDbBuilderIface::ERR_MST_UNKNOWN_ID;
		E.message = ( hbString("Unbekannte Id '") + mstId + hbString("'") ).c_str();

		throw E;
	}


	// Anstehende Aufträge zur MST-Erzeugung für die ID 'mstId' löschen

	hbSynchronized sync( this );

	mstIdLst::iterator iti = m_mstIds.begin();
	mstDefLst::iterator itd = m_mstDefs.begin();
	while( ( iti != m_mstIds.end() ) && ( itd != m_mstDefs.end() ) )
	{
		if( *iti == mstId )
		{
			iti = m_mstIds.erase( iti );
			itd = m_mstDefs.erase( itd );
		}
		else
		{
			++iti, 
			++itd;
		}

	}

	// Löschen asynchron da zeitintensiv
	m_delMstIds.insert( mstId );
}

bool d2MSTDbBuilderIfaceImpl::getNextDef( mstDef& def )
{
	hbSynchronized sync( this );

	if( m_mstDefs.size() == 0 )
		return false;

	def = m_mstDefs.front();
	m_mstDefs.pop_front();
	m_mstIds.pop_front();

	return true;
}

void d2MSTDbBuilderIfaceImpl::delMSTs()
{
	mstIdSet mstIds;

 	{
		hbSynchronized sync( this );

		mstIds = m_delMstIds;
		m_delMstIds.clear();
	}


	mstIdSet::iterator it;
	for( it = mstIds.begin(); it != mstIds.end(); ++it )
	{
		hbString errTxt;
		if( !_deleteMST( *it, errTxt ) )
		{
			HbErr( "! Fehler beim Loeschen der MST '" << *it << "':" << endl << errTxt )
		}
	}

}
