#ifdef WIN32
#pragma warning (disable : 4786)
#include <winsock2.h>
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


#include "psString.h"
#include "trMisc.h"
#include "handler.h"
#include "state.h"


#include "imcPQ.h"
#include "ddpmapDDP.h"

#include "hbCORBA.h"
#include "CORBAObjectFactory.h"
#include "d2MSTDbBuilderIfaceImpl.h"

#include "resource.h"



#define TAO_LOOP_TIME_SLICE 500



#include "d2Conf.h"
#include "loc_match.h"
#include "xmlDefReader.h"
#include "state.h"


bool getMSTNameVersion( const hbString& def, hbString& mstName, unsigned& version, hbString& errTxt )
{
	d2MST					mst;
	d2RoadFilterLst			roadFilter;
	d2ObjNameFilterLst		objNameFilter;
	d2DkAttrDefLst			dkAttrDefLst;

	if( !XML_DEF_READER.parseDef( def, mst, roadFilter, objNameFilter, dkAttrDefLst, errTxt ) )
	{
		errTxt = "Fehlerhafte MST-Definition: " + errTxt;
		return false;
	}

	if( !LOC_MATCH.init( roadFilter, objNameFilter ) )
	{
		errTxt = "Ungueltige Location-Filter";
		return false;
	}

	mstName = mst.getName();
	version = mst.getVersion();

	return true;
}

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
	_Loop( d2MSTDbBuilderIfaceImpl* mstBuilder )
	{
		m_mstBuilder = mstBuilder;
	}

	void nextEvent( long timeout )
	{
		m_mstBuilder->delMSTs();

		mstDef def;
		if( m_mstBuilder->getNextDef( def ) )
		{
			hbString mstName;
			unsigned version;

			hbString errTxt;
			if( !getMSTNameVersion( def, mstName, version, errTxt ) )
			{
				writeState( mstName, version, buildFailed, errTxt );
			}

			HANDLER.currentMST( mstName );
			bool failed = false;

			try
			{
				writeState( mstName, version, buildStarted, "" );
				HANDLER.buildMST( def );
			}
			catch( const d2MSTException& ex )
			{
				failed = true;
				writeState( mstName, version, buildFailed, ex.getErrMsg() );
				HbErr( endl << "Fehler beim Anlegen der MSt:" << endl << ex << endl )
			}
			catch( const hbException& ex )
			{
				failed = true;
				writeState( mstName, version, buildFailed, ex.asString() );
				HbErr( endl << "Fehler beim Anlegen der MSt:" << endl << ex << endl )
			}

			HANDLER.currentMST( "" );

			if( !failed )
			{
				writeState( mstName, version, recordBuildingFinished, "Objekt-Details zur MST erfolgreich angelegt" );
				cLog << endl << "MST erfolgreich angelegt" << endl;
			}
		}
	};

private:
	d2MSTDbBuilderIfaceImpl* m_mstBuilder;
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
	hbArgParser argParser( "d2MSTPubDBBuilder" );

	argParser += hbArgOpt( "appl",		"Applikationsname", "d2MSTPubDBBuilder" );
	argParser += hbArgMand( "db",		"Datenbankname");
	argParser += hbArgMand( "dbms",		"informix, oracle or postgres");
	argParser += hbArgMand( "dbserver",	"Datenbankserver");
	argParser += hbArgMand( "env",		"Systemumgebung der VRZ");
	argParser += hbArgOpt( "usr",		"Benutzer");
	argParser += hbArgOpt( "pwd",		"Passwort");
	argParser += hbArgOpt( "special",	"Prozess-Variante");

	argParser += hbArgOpt( "defFile",			"MST-Definition", "" );
	argParser += hbArgOpt( "jfile",				"Join-Definitons-Datei");
	argParser += hbArgMand( "schemaLocation",	"Pfad der XML-Schema-Datei zu 'd2defMST'" );
	argParser += hbArgOpt("dataDk2GdObjDkFile",	"Lookup von Geodyn-Objekt-Datenarten", "dataDk2GdObjDk.txt" );
	argParser += hbArgFlag( "coordinatesNotMandatory", "Eine MS wird nicht verworfen, wenn keine Koordinaten ermittelt werden können" );
	


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
		hbORBServer::instance("d2MSTPubDBBuilder" );
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

	hbString defFileName = argParser["defFile"].asString();

	XML_DEF_READER.setSchemaFileName( argParser["schemaLocation"].asString() );
	
	try
	{
		ddpDBMS::DBMS dbms = ddpDBMS::Unknown;
		string dbmsType = argParser[ "dbms"	];

		if ( dbmsType == "informix" )		dbms = ddpDBMS::Informix;
		else if ( dbmsType == "oracle" )	dbms = ddpDBMS::Oracle;
		else if ( dbmsType == "postgres" )	dbms = ddpDBMS::Postgres;
		else {
			hblog << "Wrong dbms. Valid names are \"informix\" or \"oracle\" or \"postgres\"" << endl;
			return 1;
		}
		handler::ddpConn = new ddpConnection(
												string( argParser["db"] ) , 
												string( argParser["dbserver"] ) , 
												string( argParser["usr"] ) , 
												string( argParser["pwd"] ) , 
												string( argParser["appl"] ), 
												string( argParser["special"] ), 
												string( argParser["env"] ),
												dbms
											);
	}
	catch( const hbException& ex )
	{
		HbErr( "DDP-Connection kann nicht angelegt werden !" )
		HbErr( ex << endl )
		return EXIT_FAILURE;
	}	

	try
	{
		HANDLER.init( argParser["jfile"] );    
	}
	catch( const d2MSTException& ex )
	{
		HbErr( "Prozess-Initialisierung fehlgeschlagen !" )
		HbErr( ex << endl )

		return EXIT_FAILURE;
	}
	
	if( !argParser["dataDk2GdObjDkFile"].isEmpty() )
		HANDLER.m_dataDk2GdObjDkFile = argParser["dataDk2GdObjDkFile"].asString();
	HANDLER.m_coordinatesMandatory = !argParser["coordinatesNotMandatory"].asBool();
	
	DDP_CACHE_FACT.add_item( "q", "qid" );
	DDP_CACHE_FACT.add_item( "DaLVEBetriebsParamIst" , "eaid" );
   
	if( defFileName.size() )
	{
		ifstream inp;
		inp.open( defFileName.c_str() , ios::in);
		
		if (!inp.is_open())
		{
			string errStr = "Datei '" + string(argParser["cfile"]) +"' kann nicht geoeffnet werden !";
			cerr << "!!! " << errStr << " !!!" << endl;

			return EXIT_FAILURE;
		}

		try
		{
			HANDLER.buildMST( defFileName );
			cLog << "MST erfolgreich angelegt !" << endl;
		}
		catch( const d2MSTException& ex )
		{
			HbErr( hbString( "Fehler-Code: " ) + ex.getErrCode() + hbString("\n") + ex.getErrMsg() );         
		}
		catch( const hbException& ex )
		{
			HbErr( hbString( "HBException: " ) + D2_MST_ERR_HB_EXCEPTION + hbString( "\n" ) + ex.asString() );         
		}
	}
	else
	{
		// CORBA-Server

		initialUpdateStates();

		d2MSTDbBuilderIfaceImpl mstBuilder;

		(new CORBALoopThread())->start();

		// timManager instanziieren, im Naming-Service anmelden
		if( !CORBA_OBJECT_FACTORY.registerServer( &mstBuilder, "d2MSTDbBuilderIface/Server" ) )
		{
			hbText hbt( ERROR_NAMESERVICE_REGISTER, "d2MSTDbBuilderIface/Server" );
			HB_JOURNAL.log( true, hbJournal::Fatal, hbt, MSG_CORBA, MSG_APPL_INIT );

			return EXIT_FAILURE;
		}

		cLog << endl << "d2MSTPubDBBuilder running" << endl;

		_loop( new _Loop( &mstBuilder ) );
	}
	
	
	return 0;
};


