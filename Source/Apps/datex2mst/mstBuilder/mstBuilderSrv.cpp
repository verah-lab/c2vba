#ifdef WIN32
#pragma warning (disable : 4786)
#include <windows.h>
#endif

#include "tao/ORB.h"
#include <stdlib.h>
#include <iostream>
#include <time.h>

#include <stdexcept>

#include <stdlib.h>
#ifndef WIN32
#include <libgen.h>
#endif


#include "hbString.h"
#include "hbError.h"
#include "hbArg.h"
#include "hbMonitor.h"
#include "hbMonStatusDrainIf.h"
#include "ccLog.h"
#include "ddp.h"


#include "imcPQ.h"

#include "hbCORBA.h"
#include "CORBAObjectFactory.h"
#include "mstBuilderLib.h"
#include "mstBuilderImpl.h"
using namespace mstBuilder;

#include "resource.h"



#define TAO_LOOP_TIME_SLICE 500



#include "mstLocMatch.h"
#include "locLocationInfo.h"
#include "xmlDefReader.h"
#include "xmlConfReader.h"



///////////////////////////////////////////////////////////////////////////////
// CORBALoopThread
///////////////////////////////////////////////////////////////////////////////

class CORBALoopThread : public hbThread
{
public:
	CORBALoopThread::CORBALoopThread() : hbThread("CORBALoopThread")
	{
	}

	CORBALoopThread::~CORBALoopThread()
	{
	}


protected:
	void CORBALoopThread::run( const char *threadName )
	{
		// Loop infinite
		while (true)
		{
			try
			{
				// Check CORBA loop
				while ( hbORBServer::work_pending(500) )
				{
					hbORBServer::perform_work(500);
				}
			}
			catch (CORBA::Exception& ex)
			{
				cLog << logError << "CORBA error: " << ex << endl;
			}
		}
	}

	void CORBALoopThread::final()
	{
		delete this;
	}
};





class _Loop : public ddpClientLoop 
{
public:
	_Loop( mstBuilderImpl* mstBuilder )
	{
		m_mstBuilder = mstBuilder;
	}

	void nextEvent( long timeout )
	{
		m_mstBuilder->buildNextMST();
	};

private:
	mstBuilderImpl* m_mstBuilder;
};	


void _loop( _Loop* tl )
{
	hbMonitor mon;
	if( (mon.valid() ) && ( mon.Valid() ) )
	{
		mon.reportStatus( ps_running );
	}

	ddp::applicationInitialized();

	while(1) 
	{
		imcEnvironment::communicate(0,100);
		tl->nextEvent( 1 );
	}
}


int main(int argc,char *argv[])
{
	hbArgParser argParser( "mstBuilderSrv" );

	argParser += hbArgOpt( "program",	"Applikationsname", "mstBuilderSrv" );
	argParser += hbArgOpt( "db",		"Datenbankname");
	argParser += hbArgMand( "server",	"Datenbankserver");
	argParser += hbArgMand( "env",		"Systemumgebung der VRZ");
	argParser += hbArgOpt( "user",		"Benutzer");
	argParser += hbArgOpt( "passwd",	"Passwort");
	argParser += hbArgOpt( "special",	"Prozess-Variante");
	argParser += hbArgOpt( "dbms",		"Datenbank-System (Postgres, Informix oder Oracle)", "Postgres");

	argParser += hbArgMand( "configSchemaLocation",			"XML-Schema-Datei zu MST-Konfigurationen" );
	argParser += hbArgMand( "definitionSchemaLocation",		"XML-Schema-Datei zu MST-Definitionen" );
	argParser += hbArgMand( "d2Version",					"Konfigurationsversion" );
	
	argParser += hbArgFlag( "dontIgnoreMSOutsideExtremeCoorRect",	"Measurement Sites mit Koordinaten außerhalb des Rechtecks aus der Datenart extreme_coor werden mit Koordinaten x=-1, y=-1 verortet." );
	argParser += hbArgFlag( "useLocNameAsMsId",	"Location-Name wird als MeasurementSite-ID verwendet" );
	argParser += hbArgOpt( "lveBetriebsParamDatakind", "Datenart der LVE-Betriebsparameter",	"DaLVEBetriebsParamIst" );
	argParser += hbArgFlag( "carriagewayFromGlane", "",	"Fahrbahn (carriageway) über Datenart glane bestimmen" );
	argParser += hbArgOpt( "geodyn2WLSAddress", "CORBA name service address of Geodyn2WLS-Server",	"" );


	hbLogStreamObserver::changeLoggingOutput( "cout" );
	hbLogStreamObserver::setPrompt("");
	hbLogStreamObserver::setIndentString("- ");

	if ( !argParser.parse(argc,argv) ) 
	{
		cerr << "Ungueltige Aufruf-Parameter:"  << endl;
		cerr << argParser.error() << endl;
		return 1;
	}

	cLog << "init ORB ..." << endl;

	try
	{
		hbORBClient::setUseNameServer( true );
		hbORBServer::instance("mstBuilder" );
	}
	catch(const CORBA::Exception& c_ex )
	{
		hbText hbt( EXCEPTION_CORBA_INIT, what( c_ex ).c_str() );
		HB_JOURNAL.log( true, hbJournal::Fatal, hbt, MSG_CORBA, MSG_APPL_INIT );
		
		return false;
	}
	catch( ... )
	{
		hbText hbt( EXCEPTION_CORBA_INIT, "unknown exception" );
		HB_JOURNAL.log( true, hbJournal::Fatal, hbt, MSG_CORBA, MSG_APPL_INIT );

		return false;
	}

	XML_CONF_READER.setSchemaFileName( argParser["configSchemaLocation"].asString() );
	XML_DEF_READER.setSchemaFileName( argParser["definitionSchemaLocation"].asString() );

	ddpDBMS::DBMS dbms = ddpDBMS::Postgres;
	hbString dbmsStr = argParser["dbms"].asString();
	if( dbmsStr == "Informix" )
		dbms = ddpDBMS::Informix;
	else if( dbmsStr == "Oracle" )
		dbms = ddpDBMS::Oracle;
	
	ddpConnection* conn = 0;
	try
	{
		conn = new ddpConnection(   string( argParser["db"] ) , 
									string( argParser["server"] ) , 
									string( argParser["user"] ) , 
									string( argParser["passwd"] ) , 
									string( argParser["program"] ), 
									string( argParser["special"] ), 
									string( argParser["env"].asString() ),
									dbms  
								);
	}
	catch( const hbException& ex )
	{
		HbErr( "DDP-Connection kann nicht angelegt werden !" )
		HbErr( ex << endl )
		return EXIT_FAILURE;
	}	

	// CORBA-Server

	hbString d2Version = argParser["d2Version"].asString();

	bool dontIgnoreMSOutsideExtremeCoorRect = argParser["dontIgnoreMSOutsideExtremeCoorRect"].asBool();
	hbString lveBetriebsParamDatakind = argParser["lveBetriebsParamDatakind"];
	hbString geodyn2WLSAddress = argParser["geodyn2WLSAddress"];
	bool useLocNameAsMsId = argParser["useLocNameAsMsId"].asBool();
	bool carriagewayFromGlane = argParser["carriagewayFromGlane"].asBool();

	mstBuilderImpl mstBuilder( conn, 
		                       d2Version, 
							   geodyn2WLSAddress,
							   dontIgnoreMSOutsideExtremeCoorRect,
							   useLocNameAsMsId,
							   carriagewayFromGlane,
							   lveBetriebsParamDatakind );

	(new CORBALoopThread())->start();

	// timManager instanziieren, im Naming-Service anmelden
	if( !CORBA_OBJECT_FACTORY.registerServer( &mstBuilder, "mstBuilder/Builder" ) )
	{
		hbText hbt( ERROR_NAMESERVICE_REGISTER, "mstBuilder/Builder" );
		HB_JOURNAL.log( true, hbJournal::Fatal, hbt, MSG_CORBA, MSG_APPL_INIT );

		return EXIT_FAILURE;
	}

	cLog << endl << "mstBuilderSrv running" << endl;

	_loop( new _Loop( &mstBuilder ) );
	
	
	return 0;
};


