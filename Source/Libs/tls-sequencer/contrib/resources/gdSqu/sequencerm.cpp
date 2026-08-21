#include <hbtimewrap.h>
#include "rnIdent.h"

#include <zdfdef.h>
#include "nipd.h"
#include <nkomm.h>

#ifndef DB_IS_ZDF

#include "ddp.h"
#include "ddpZdfQuery.h"
#include "gdtls.h"
#include <hbMonStatusDrainIf.h>
#include <hbMonitor.h>

gdTls* iface; // z.z. wg. bibliothek

#endif

#include "tokens.h"
#include "seqLexer.h"
#include "Sequencer.h"
#include <PBlackBox.h>
#include <nipd.h>
#include <zdfacc.h>
//#include <ProcArg.h>
#include "IdList.h"
#include "DaInfo.h"
#include "DbgAnseq.h"
#include "dkinfshm.h"

#ifdef WIN32
#include "windows.h"
#include "process.h"
#define popen _popen
#define pclose _pclose
#ifndef __PRETTY_FUNCTION__
#define __PRETTY_FUNCTION__ "func n/a"
#endif
#endif

typedef ANTLRCommonToken ANTLRToken;

FILE *dbg = stdout;
NIPD_ADR *zdf = 0;
int lastLine;

TypKommRoot *OSI7Shm;

unsigned short parseErrors = 0;
char *cpp_program	= 0;
char *db			= 0;
char *server		= 0;
char *user			= 0;
char *passwd		= 0;
char *dbmsname		= 0;
char *env			= 0;
char *appl			= 0;
char *special		= 0;

int InitDebug ()
{
	//DebugInfo( AREA_DaInfo );
	//DebugInfo( AREA_ZdfIn );
	//DebugAnswer( MTRACE );
	//DebugAnswer( DISTR );
	//DebugAnswer( FORCEGC );
	return (1);
}

int InitDebugDone = InitDebug();

//extern int mallfd;
extern char *inputFileName;

avlTree<long,DaInfo*> daList;
DaInfo **daArray;
char *cpp_opt = "";

ddpConnection *dbConnection;
extern "C" void zdfDatakindInit( ddpConnection *con );
extern void buildSeekTables();

class dSequencer : public Sequencer {
	public:
		dSequencer(ANTLRTokenBuffer *in) : Sequencer( in ) {}
		void syn(	_ANTLRTokenPtr tok,
						ANTLRChar *egroup,
						SetWordType *eset,
   						ANTLRTokenType etok,
   						int k)
   		{
   			parseError = true;
   			fprintf( stderr, "FATAL   file %s\n  ", inputFileName );
   			Sequencer::syn( tok, egroup, eset, etok, k );
   		}
		//void init() { Sequencer::init(); }
};

template class ParserBlackBox<seqLexer, dSequencer, ANTLRToken>;

#ifdef haveProcArg
static void doParse( char *&file )
#else
static void doParse( char *file )
#endif
{
	// Spezifikationsdatei(en) parsen
	char cmd[1024];
#ifndef WIN32
	sprintf ( cmd, "exec /lib/cpp %s %s", cpp_opt?cpp_opt:"", file );
#else
	sprintf ( cmd, "%s %s %s", cpp_program?cpp_program:"cpp", cpp_opt?cpp_opt:"", file );
#endif
	//sprintf( cmd, "exec /lib/cpp %s %s", cpp_opt, file );
	FILE *parseFile = popen( cmd, "r" );
	if (!parseFile) {
		fprintf( stderr, "Couldn't execute: %s\n", cmd );
		cleanup_libs( 10 );
	}
	ParserBlackBox<seqLexer, dSequencer, ANTLRToken> parser(parseFile);
	parser.parser()->seq();
	parseErrors += parser.parser()->parseError;
	pclose( parseFile );
}

#ifdef haveProcArg
bool test;
unsigned short pDbgLevel;

ProcessArguments
descr (
	   "Sequencer. Veraenderung der Daten von der ZDF fuer die ZDF"
	   ),
	   parseDbgLvl (
	   pDbgLevel,
	   "pdbg",
	   "Steuerung des Debugoutputs waehrend des Parsens"
	   ),
	   doTest (
	   test,
	   "test",
	   "Nur testen der Spezifikationdatei(en)"
	   ),
	   CppOpt (
	   cpp_opt,
	   "cpp_opt",
	   "Optionen fuer /lib/cpp im Vorlauf des Parsens"
	   ),
	   DoParse (
	   doParse,
	   "spec",
	   "Namen der Spezifikationdatei(en)",
	   pa_DEFAULT_REST | pa_REQUIRED
	   ),
	   setDebugAreas (
	   DMOptSetDebugLevel,
	   "dbg",
	   "Initiales Setzen der Debugareas."
	   );

#else

#include <Bool.h>
#include <nopts.h>
#include <DM.h>

static void DMOptSetDebugLevel(unsigned long &ds)
{
	DebugManager dm;
	dm.optSetDebugLevel( ds );
	//::DMOptSetDebugLevel( &ds );
}

Bool test = False;
unsigned short pDbgLevel = 0;

void usage( char *s ) {
	printf ( "%s: Sequencer. Veraenderung der Daten von der ZDF fuer die ZDF\n\n", s );
	printf ( "Benutztung: %s [Optionen]\n", s );
	printf ( "Optionen sind wie folgt:\n" );
	printf ( "\
-spec=<Dateiname> Namen der Spezifikationdatei\n\
                  Mehrfachangabe moeglich\n\
-cpp_opt=<String> Optionen fuer cpp im Vorlauf des Parsens\n\
-test             usage ausgeben\n\
-pdbg=<Level>     Debugsteuerung(DM) fuer den Parser dieses Prozess\n\
-dbg=<Level>      Debugsteuerung(DM) fuer diesen Prozess\n\
\n\
-db               Datenbank\n\
-server           Datenbank-Server\n\
-user             Datenbank-User\n\
-passwd           Passwort fuer Datenbank-User\n\
-dbmsname         DBMS, informix oder oracle\n\
-appl             Application name\n\
-special          Param fuer Application\n\
-env              DDP-Environment\n\
-myDev            eigene Knotennummer\n\
\n\
-?                usage ausgeben\n\
-h                usage ausgeben\n\
-help             usage ausgeben\n" );
	exit( 1 );
}

void OptSetDebugLevel( long *dbg ) {

        DebugManager dm;
        dm.optSetDebugLevel( *dbg );

}

#endif

long simulationId = 0;

int StartCom ( Bool )
{
	static char com_started = 0;
	if ( ! com_started ) {
cerr << rsTime().timeStr() << ": StartCom: Start" << endl;
		com_started = 1;
		
		ddpDBMS::DBMS	dbms = ddpDBMS::Unknown;
#if (! defined( INFORMIX ) && ! defined ( ORACLE )) || (defined( INFORMIX ) && defined ( ORACLE ))
		hbString dbmsName = dbmsname ? dbmsname : "";
		if ( dbmsName == "informix" )		dbms = ddpDBMS::Informix;
		else if ( dbmsName == "oracle" )	dbms = ddpDBMS::Oracle;
		else								{ cerr << "Wrong dbms. Valid names are \"informix\" or \"oracle\"" << endl; exit ( 1 ); }
#endif
#ifdef INFORMIX
		dbms = ddpDBMS::Informix;
#endif
#ifdef ORACLE
		dbms = ddpDBMS::Oracle;
#endif
		try 
		{
			dbConnection = new ddpConnection( simulationId, db, server, user, passwd, appl, special, env, dbms );
			dbConnection->environment()->dontUseTimeAndVolumeWindows();
			//buildSeekTables();
			zdfDatakindInit( dbConnection );
#ifndef NO_ZQL_SERVER
			bool haveZQC = ZdfQueryConnection::instanciate( *dbConnection );
			if (!haveZQC) {
				fprintf( stderr, "Kann ZdfQueryConnection nicht instanziieren. Laeuft der Server?" );
				exit( 1 );
			}
#endif
		}
		catch (exception& error) {
			cerr << "Error in 'new ddpConnection':" << error.what() << endl;
			exit ( 1 );
		}
		catch (...) {
			cerr << "Unrecognized error during 'new ddpConnection'" << endl;
			exit ( 1 );
		}

		try 
		{
			if ( ! ( OSI7Shm = InitOSI7Shm ( dbConnection ) ) ) {
				fprintf ( stderr, "Kann Shm-Segmente nicht anbinden\n" );
				cleanup_libs ( 10 );
			}
		}
		catch (exception& error) {
			cerr << "Error in InitOSI7Shm:" << error.what() << endl;
			exit ( 1 );
		}
		catch (...) {
			cerr << "Unrecognized error during 'InitOSI7Shm'" << endl;
			exit ( 1 );
		}
cerr << rsTime().timeStr() << ": StartCom: Done" << endl;
	}
	return (1);
}

int main (int cnt, char *arg[])
{
#ifdef DB_IS_ZDF
	// Verbindung zur Datenbank initialisieren
	zdf = db_init( "db<<->>db", "Initphase", 0 );
	if (!zdf) {
		fprintf( stderr, "Keine Verbindung zur ZDF.\n" );
		fprintf( stderr, "Dann mag ich auch nicht. Tschoe.\n" );
		cleanup_libs( 10 );
	}

	// OSI7Shm-Segmente an diesen Prozess anbinden
	if ( ! ( OSI7Shm = InitOSI7Shm () ) ) {
		fprintf ( stderr, "Kann OSI7Shm nicht anbinden\n" );
		cleanup_libs( 10 );
	}
	
#ifdef haveProcArg

	ProcessArguments::printArgs( cnt, arg, stderr );
	ProcessArguments::printArgs( cnt, arg, stdout );
	// Interpretieren der Aufrufparameter
	if(ProcessArguments::getopts( cnt, arg, descr)) {
		ProcessArguments::errorResumee( stderr, descr );
		ProcessArguments::explain( arg[0], descr );
	}

#else

	Bool needusage = False;
	nopts ( &cnt, arg,
		 "spec",           	doParse,		        OPT_STRG|OPT_CLLB,
	     "cpp_opt",        	&cpp_opt,				OPT_STRG,
		 "test",           	&test,					OPT_BOOL,
		 "dbg",            	OptSetDebugLevel,	    OPT_LONG|OPT_CLLB,
		 "pdbg",            &pDbgLevel,				OPT_SHRT,
		 "?",              	&needusage,				OPT_BOOL,
		 "h",              	&needusage,				OPT_BOOL,
		 "help",           	&needusage,				OPT_BOOL,
		 NULL );
	
	if (needusage)
		usage( arg[0] );

#endif
#endif /* DB_IS_ZDF */

#ifndef DB_IS_ZDF
	cpp_program = (char*) calloc( 1, 512 );
	db			= (char*) calloc( 1, 512 );
	server		= (char*) calloc( 1, 512 );
	user		= (char*) calloc( 1, 512 );
	passwd		= (char*) calloc( 1, 512 );
	dbmsname	= (char*) calloc( 1, 512 );
	env			= (char*) calloc( 1, 512 );
	appl		= (char*) calloc( 1, 512 );
	special		= (char*) calloc( 1, 512 );

	strcpy( cpp_program	, "" );
	strcpy( db			, "rheim" );
	strcpy( server		, "merkurtcp" );
	strcpy( user		, "odbc" );
	strcpy( passwd		, "odbc" );
	strcpy( dbmsname	, "informix" );
	strcpy( env			, "saturn:5563:HE+Integration" );
	strcpy( appl		, "ddp<->ddp" );
	strcpy( special		, "Sequencer-SPSL" );

    hbMonitor mon;
    Bool needusage = False;
	nopts ( &cnt, arg,
		"db",				&db,					OPT_STRG,
		"server",			&server,				OPT_STRG,
		"user",				&user,					OPT_STRG,
		"passwd",			&passwd,				OPT_STRG,
		"dbmsname",			&dbmsname,				OPT_STRG,
		"simulation",		&simulationId,			OPT_LONG,
		"appl",				&appl,					OPT_STRG,
		"special",			&special,				OPT_STRG,
		"env",				&env,					OPT_STRG,
		"startCom",			StartCom,				OPT_BOOL|OPT_CLLB,
		
		"cpp_opt",        	&cpp_opt,				OPT_STRG,
		"cpp_program",		&cpp_program,			OPT_STRG,
		"test",           	&test,					OPT_BOOL,
		"spec",           	doParse,		        OPT_STRG|OPT_CLLB,
		//"dbg",            	OptSetDebugLevel,	    OPT_LONG|OPT_CLLB,
		"pdbg",				&pDbgLevel,				OPT_SHRT,

		"?",              	&needusage,				OPT_BOOL,
		"h",              	&needusage,				OPT_BOOL,
		"help",           	&needusage,				OPT_BOOL,
		NULL );
	
	if (needusage)
		usage( arg[0] );
#endif	
	if (test) {
		fprintf( stderr, "Testen beendet %s.\n",
			parseErrors > 0 ? "gut" : "schlecht" );
		cleanup_libs( parseErrors > 0 ? 1 : 0 );
	}
	
	if (parseErrors) {
		fprintf( stderr, "Can't continue because of errors.\n" );
		cleanup_libs( 10 );
	}

	// Initialisiern von Datenstrukturen
	avlTreeIterator<long, DaInfo*> it (daList);
	long maxDataType = 0xffffffff;
	while (it.next()) {
		printf( "DA %-40s[%7.0ld] is ", DkId2DkPtr(it.key())->DkName, it.key() );
		DaInfo *info = it.data();
		if (info->datatype > maxDataType) maxDataType = info->datatype;
		if (info->isTrigger) {
			printf( "trigger for %lu action(s)\n", info->actions.GetNum() );
		} else {
			printf( "used\n" );
		}
	}
	maxDataType++;
	daArray = (DaInfo **) malloc( maxDataType * sizeof( DaInfo * ) );
	memset( daArray, 0, maxDataType * sizeof( DaInfo * ) );
	while (it.next()) {
		DaInfo *info = it.data();
		daArray[info->datatype] = info;
	}
	
	// In den Hintergrund forken
//	{
//		int bak = mallfd;
//		mallfd = -1;
//		if ( zdf_fork ("Sequencer-SPSL") != 0 ) {
//			extern Bool db_exit_quiet;
//			db_exit_quiet = True;
//			cleanup_libs ( 0 );
//		}
//		mallfd = bak;
//	}
	
	// erforderliche Datenarten anmelden
	DaInfo::loginAll();
	
    if (mon.valid()) 
		mon.reportStatus( ps_running, "init done." );
    
    // In einer Endlosschleife die Daten der ZDF bearbeiten
	DaInfo::work();
	
	cleanup_libs( 10 );
	return 1;
}

unsigned long getNumEa( long da )
{
	//fprintf( stderr, "%s of %ld\n", __PRETTY_FUNCTION__, da );
	short Block = Da2BlockSilent (da);
	//fprintf( stderr, "Block %hd\n", Block );
	if (Block < 0) return 0;
	//fprintf( stderr, "Num %lu\n", (unsigned long) OSI7Shm->BlockTable[Block].numea);
	return OSI7Shm->BlockTable[Block].numea;
}

unsigned char *getBase( long DA )
{
	DaInfo *inf = daArray[DA];
	if (!inf) return (0);
	return (inf->getBase());
}

void setWritten( long DA, long indx )
{
	DaInfo *inf = daArray[DA];
	if (!inf) return;
	inf->written(indx);
}

bool SendActualData2ZDF()
{
	DaInfo::sendAll();
	return (true);
}

bool GetIdList (	long 	eaid,
					long 	ea_index,
					int 	destspec,
					long 	from_da,
					long 	to_da,
					bool 	index_valid,
					short	cmd,
					IdList	&res )
{
//fprintf( stderr, "%s cmd %hd fromDA %ld toDA %ld eaid %ld idxvalid %d what %d ea_index %ld\n",
//__PRETTY_FUNCTION__, cmd, from_da, to_da, eaid, index_valid, destspec, ea_index ); //fflush( dbg );
//	IdList *res = 0;
//	if (cmd == EAID_GET || cmd == EAINDX_GET) {
//		res = new IdList;
//		res->dataType = to_da;
//	}
	short fromBlock = Da2BlockSilent (from_da);
if (from_da == 0) {
	fprintf( stderr, "from_da\n" );
	abort();
}
	if (fromBlock < 0) {
		if (cmd == EA_TEST) return 0;
	}
if (to_da == 0) {
	fprintf( stderr, "from_da\n" );
	abort();
}
	short toBlock = Da2BlockSilent (to_da);
	if (toBlock < 0) {
		if (cmd == EA_TEST) return 0;
	}
	register TypBlockTable *to_bl = &OSI7Shm->BlockTable[toBlock];
	if (!index_valid) {
		ea_index = GetEAIndexInBlockByEaid( fromBlock, eaid );
		//fprintf( stderr, "%s[%d] idx %ld of %lu da %ld bl %hd eaid %ld\n",
		//__FILE__, __LINE__, ea_index, (unsigned long) OSI7Shm->BlockTable[fromBlock].numea, from_da, fromBlock, eaid );
		if (ea_index < 0) {
			if (cmd == EA_TEST) return 0;
			return 0;
		}
	}
	//fprintf( stderr, "%s switch\n", __PRETTY_FUNCTION__ ); //fflush( dbg );
	switch (destspec) {
		case GUELTIG_DE:
		{
			/* von ea an ea */
			long ea_idx = 0;
			if (from_da == to_da)	ea_idx = ea_index;
			else							ea_idx = GetEAIndexInBlockByEaid (toBlock, eaid);
			if ( ea_idx < 0 ) {
				if (cmd == EA_TEST) return 0;
				break;
			}
			if (cmd == EA_TEST) return true;
			int rc=0;
			if (cmd == EAID_GET) rc = res.Add (eaid);
			if (cmd == EAINDX_GET) rc = res.Add (ea_idx);
			if (!rc) {
				fprintf (stderr, "%s - %s:\n",
					shorttime (hb_time (0L)), __PRETTY_FUNCTION__);
				fprintf (stderr, "| Out of mem in 'GUELTIG_DE' on DA %ld\n", to_da );
				//delete res;
				return (0);
			}
		}
		break;
		case GUELTIG_KNOTEN:
		{
			register short treepos =
				OSI7Shm->BlockTable[fromBlock].treepos[ea_index];
			register zdfAnzType i, limit = to_bl->numea;
			register short *rTreepos = OSI7Shm->BlockTable[toBlock].treepos;
			//fprintf( stderr, "seek tp %hd\n", treepos );
			for ( i = 0; i < limit; i++ ) {
				//fprintf( stderr, "cmp tp %hd\n", rTreepos[i] );
				if (rTreepos[i] == treepos) {
					//fprintf( stderr, "%s case GUELTIG_KNOTEN 1 cmd %hd\n", __PRETTY_FUNCTION__, cmd );
					if (cmd == EA_TEST) return true;
					int rc=0;
					//fprintf( dbg, "%s case GUELTIG_KNOTEN 2 cmd %hd\n", __PRETTY_FUNCTION__, cmd ); fflush( dbg );
					if (cmd == EAID_GET) rc = res.Add (to_bl->eaid[i]);
					if (cmd == EAINDX_GET) rc = res.Add (i);
					if (!rc) {
						fprintf (stderr, "%s - %s:\n",
							shorttime (hb_time (0L)), __PRETTY_FUNCTION__);
						fprintf( stderr, "Out of mem in 'GUELTIG_KNOTEN' on DA %ld\n", to_da );
						//delete res;
						return (0);
					}
				}
			}
		}
		break;
		case GUELTIG_NORMALE_DES_AN_CLUSTER_DE:
		{
			/* z.B. WVZ hat sich mit folgenummer gemeldet => Cluster abfragen */
			if ( (long) to_bl->clusterid == -1L ) {
				if (cmd == EA_TEST) return 0;
				fprintf (stderr, "%s - %s:\n",
					shorttime (hb_time (0L)), __PRETTY_FUNCTION__);
				fprintf ( stderr,
					"No cluster ids are available in DA %ld.\n",
					to_da );
				break;
			}
			unsigned char de = OSI7Shm->BlockTable[fromBlock].DE[ea_index];
			if ( 192 < de && de < 223 ) break;
			//register short i, limit = to_bl->numea;
			long clusterid =
				OSI7Shm->BlockTable[fromBlock].clusterid[ea_index];
			if (clusterid != 0 && clusterid != UNDEF_REF) {
				int rc=0;
				if (cmd == EAINDX_GET) {
					long eaidx = GetEAIndexInBlockByEaid( toBlock, clusterid );
					if (eaidx < 0) {
						if (cmd == EA_TEST) return 0;
						//delete res;
						return 0;
					}
					rc = res.Add (eaidx);
				}
				if (cmd == EA_TEST) return true;
				if (cmd == EAID_GET) rc = res.Add (clusterid);
				if (!rc) {
					fprintf (stderr, "%s - %s:\n",
						shorttime (hb_time (0L)), __PRETTY_FUNCTION__);
					fprintf (stderr, "Out of mem in 'GUELTIG_NORMALE_DES_AN_CLUSTER_DE' on DA %ld\n", to_da );
					//delete res;
					return (0);
				}
			}
		}
		break;
		case GUELTIG_CLUSTER_DE_AN_NORMALE_DES:
		{
			/* z.B. WVZ hat sich mit folgenummer gemeldet => Cluster abfragen */
			if ( (long) to_bl->clusterid == -1L ) {
				if (cmd == EA_TEST) return 0;
				fprintf (stderr, "%s - %s:\n",
					shorttime (hb_time (0L)), __PRETTY_FUNCTION__);
				fprintf ( stderr,
					"| Bei DA %ld sind keine ClusterIds verfuegbar\n",
					to_da );
				break;
			}
			unsigned char de = OSI7Shm->BlockTable[fromBlock].DE[ea_index];
			if ( ! ( 192 < de && de < 223 ) ) break;
			register short i, limit = to_bl->numea;
			register long clusterid =
				OSI7Shm->BlockTable[fromBlock].eaid[ea_index];
			for ( i = 0; i < limit; i++ ) {
				if ( to_bl->clusterid[i] == clusterid
						&& ( 		to_bl->DE[i] <= 192
								|| to_bl->DE[i] >= 223 ) ) {
					if (cmd == EA_TEST) return true;
					int rc=0;
					if (cmd == EAID_GET) rc = res.Add (to_bl->eaid[i]);
					if (cmd == EAINDX_GET) rc = res.Add ((long)i);
					if (!rc) {
						fprintf (stderr, "%s - %s:\n",
							shorttime (hb_time (0L)), __PRETTY_FUNCTION__);
						fprintf (stderr, "Out of mem in 'GUELTIG_CLUSTER_DE_AN_NORMALE_DES' on DA %ld\n", to_da );
						//delete res;
						return (0);
					}
				}
			}
		}
		break;
		default:
			fprintf (stderr, "Interner Fehler in der Zielangabe. %s[%d]\n",
				__FILE__, __LINE__);
			break;
	}
	return true;
}

bool catchDataType( long da )
{
	DaInfo *found;
	if (daList.find( da, found)) {
#ifdef DB_IS_ZDF
		found = new DaInfo( da );
#else
		found = new DaInfo( da, dbConnection );
#endif
		if (daList.insert( da, found )) {
			fprintf( stderr, "%s> Can't keep info for DA %ld\n",
				shorttime( hb_time( 0L ) ), da );
			return (false);
		}
	}
	return (true);
}

bool catchTriggerDataType( long da, TransOp *action )
{
	DaInfo *found;
	if (daList.find( da, found)) {
		fprintf( stderr, "%s> Can't set trigger DA %ld. DA not known\n",
			shorttime( hb_time( 0L ) ), da );
		return (false);
	}
	found->isTrigger = true;
	if (found->actions.Add( action ))
		return (true);
	return (false);
}

void Sequencer::ParseError( int line, char *fmt, ... )
{
	extern char * inputFileName; va_list args;
	va_start ( args, fmt );
	parseError = true;
	fprintf( stderr, "FATAL   file %s\n  line %d: ", inputFileName, line );
	vfprintf( stderr, fmt, args );
	fprintf( stderr, "\n" );
}
void Sequencer::ParseWarn( int line, char *fmt, ... )
{
	extern char * inputFileName; va_list args;
	va_start ( args, fmt );
	parseWarns = true;
	fprintf( stderr, "WARNING file %s\n  line %d: ", inputFileName, line );
	vfprintf( stderr, fmt, args );
	fprintf( stderr, "\n" );
}
