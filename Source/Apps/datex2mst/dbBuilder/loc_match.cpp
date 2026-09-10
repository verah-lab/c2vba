#ifdef WIN32
#pragma warning (disable : 4786)
#endif

#include "tr_err.h"
#include "loc_match.h"
#include "loc_man.h"

#include "gd_ddp_gd_loc_man.h"
#include "gd_ddp_gd_road_net.h"

#include "handler.h"


#include "trMisc.h"
#include "psString.h"

loc_match* loc_match::_instance = 0;

#define		DIR_ALL		'A'


bool loc_match::init( const d2RoadFilterLst& roadFilter, const d2ObjNameFilterLst& objNameFilter )
{
	hbPrefixT pref;
	LogE1(cout << pref << __FUNCTION__ << "()" <<  endl;);

	try
	{
		list<locLocation*>::iterator itl;
		for( itl = m_locList.begin(); itl != m_locList.end(); ++itl )
			delete *itl;

		m_locList.clear();
		m_objNameFilter.clear();
		
		locConnection loc_conn( *(handler::ddpConn) );
		
		d2RoadFilterLst::const_iterator it;
		for( it = roadFilter.begin(); it != roadFilter.end(); ++it )
		{
			const d2RoadFilter& rf = *it;
			string road = rf.getName();
			char dir = rf.getDirection();

			string locName = road;
			long type = ddp_tr_road::Id();
			if( dir != DIR_ALL )
			{
				locName = road + " " + dir;
				type = ddp_tr_directed_road::Id();
			}    
			
			// @@@ Offsets berücksichtigen
			
			locLayer loc_layer( loc_conn , type );
			
			list<long> loc_list;
			loc_layer.getLocations( loc_list );
			
			locLocation* ptLoc = 0;
			for( list<long>::const_iterator it = loc_list.begin() ; 
            it != loc_list.end() ; ++it )
			{
				locLocation loc( loc_conn , *it );
				LogF2( cout << "Location Name: " << loc.name() << endl; );

				if ( loc.name() == locName )
				{
					ptLoc = new locLocation( loc ); 
					m_locList.push_back( ptLoc );
					break;
				}
			}
			
			if( !ptLoc )
			{
				hbString errStr = "Road '" + locName + "' kann nicht instanziiert werden.";
				HbErr( errStr << endl; )
					
				list<string> errTexts;
				errTexts.push_back( errStr );
				throwErrorMsg( errTexts );
				
				return false;
			}
		}

		m_objNameFilter = objNameFilter;

		d2ObjNameFilterLst::iterator ito;
		for( ito = m_objNameFilter.begin(); ito != m_objNameFilter.end(); ++ito )
			m_objNameSet.insert( ito->getName() );
	}    
	catch( const hbException& ex )
	{
		hbString errStr = "Fehler in loc_match::init()";
		HbErr( "!!! " << errStr << " !!!" << endl )
		HbErr( ex << endl )
			
		list<string> errTexts;
		errTexts.push_back( errStr );
		errTexts.push_back( ex.asString() );
		throwErrorMsg( errTexts );

		return false;
	}

	return true;
}


bool loc_match::match( long id, const hbString& locName )
{
	try
	{
		if( !m_locList.size() && !m_objNameSet.size() )
			return true;

		list<locLocation*>::iterator it;
		for( it = m_locList.begin() ; it != m_locList.end() ; ++it )
		{
			locLocation::MatchType t = (*it)->match( id );     
			
			if( ( t != locLocation::Unknown ) && ( t != locLocation::NoMatch ) )
				return true;
		}
		
		if( m_objNameSet.size() )
			return ( m_objNameSet.find( locName ) != m_objNameSet.end() );
		else
			return false;
	}
	catch( const hbException& ex )
	{
		cerr << "!!! error in loc_match::match() !!!" << endl;
		cerr << ex << endl;
		return false;
	}
}
