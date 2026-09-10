#ifdef WIN32
#pragma warning (disable : 4786)
#endif

#include "trMisc.h"
#include "tr_err.h"
#include "state.h"
#include "handler.h"
#include "ddp.h"

#define stateDk "d2MSTBuildState"


void initialUpdateStates()
{
	try
	{
		ddpQuery Q( *handler::ddpConn, stateDk );

		ddpList Lst;
		Q.read( Lst );

		ddpTransaction T( *handler::ddpConn );
		ddpList L;


		ddpList::iterator it;
		for( it = Lst.begin(); it != Lst.end(); ++it )
		{
			ddpObject& obj = *it;

			EBuildState state = (EBuildState)( obj["state"].asInt() );

			if( ( state != buildFailed ) && ( state != buildAborted ) && ( state != finished ) )
			{
				obj["time"]		= hb_time(0);
				obj["state"]	= buildAborted;
				obj["result"]	= "";

				L.push_back( obj );
			}
		}

		if( Lst.size() )
		{
			T.put( L );

			T.commit();
		}
	}
	catch( const hbException& ex )
	{
		HbErr( "Fehler beim initialen Aktualisieren von 'd2MSTBuildState' !" << endl << ex )
		HbErr( endl << "Der Prozess wird beendet !" << endl )

		::exit( -1 );
	}
};



void writeState( const hbString& mstName, unsigned version, EBuildState state, 
				 const hbString& result, time_t _t )
{
	try
	{
		time_t t = _t;
		if( _t == -1 )
		{
			ddpQuery Q( *handler::ddpConn, stateDk );
			Q.condition( hbString("mst = '") + mstName + hbString("' and version = ") + version );

			ddpList Lst;
			Q.read( Lst );

			if( Lst.size() == 0 )
				t = hb_time(0);
			else
				t = Lst.front()["buildStarted"].asLong();
		}

		ddpTransaction T( *handler::ddpConn );

		ddpObject obj( *handler::ddpConn, stateDk );

		obj["buildStarted"] = t;
		obj["mst"]			= mstName;
		obj["version"]		= version;
		obj["state"]		= state;
		obj["result"]		= result;

		ddpList L;
		L.push_back( obj );

		T.put( L );

		T.commit();
	}
	catch( const hbException& ex )
	{
		HbErr( "Fehler beim Schreiben von 'd2MSTBuildState' !" << endl << ex )
		HbErr( endl << "Der Prozess wird beendet !" << endl )

		::exit( -1 );
	}
}
