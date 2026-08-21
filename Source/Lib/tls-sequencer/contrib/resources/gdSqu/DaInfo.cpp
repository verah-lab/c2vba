#include <hbtimewrap.h>
/* deprecated... #include <Tracey.h>*/
#define NSEQ_AREA AREA_DaInfo
#include "nseqDebug.h"

#ifdef DB_IS_ZDF
#include "SEArrayT.hc"
#else
#include "SEArrayT.h"
#endif

#ifdef DB_IS_ZDF
#include <ZdfQuery.h>
#else
#include "list"
#include "asLogRecv.h"
#if defined WIN32 || ( defined __GNUC__ && __GNUC__ >= 3 )
using namespace std;
#endif
extern ddpConnection *dbConnection;
#endif
#include "DaInfo.h"
#include "nkomm.h"
#include <db_sys.h>
#include <dkinfshm.h>
#include <rnBitArr2.h>
#include "Timer.h"
#include <limits.h>
#include <rnList.h>
#include <listmacs.h>
#include <avlTree.h>
#include <DeData.h>
#include <errno.h>
#include <zdfacc.h>
#include <TransOp.h>
#include "CurEnv.h"
#include <rsTime.h>

extern TypKommRoot *OSI7Shm;
extern avlTree<long,DaInfo*> daList;
extern DaInfo **daArray;
extern NIPD_ADR *zdf;

extern CurrentEnv currentEnv;
//extern long currentEaid;
//extern long currentDa;
//extern long currentEaIndx;
//extern long currentDe;
//extern long currentNode;
//extern long currentTime;
//extern long currentASTime;
//extern short currentBlock;
//extern unsigned char *currentData;


SEArray<DaInfo*> DaInfo::writtenDAs;

#ifdef DB_IS_ZDF
DaInfo::DaInfo( long da )
#else
DaInfo::DaInfo( long da, ddpConnection *con )
#endif
{
	datatype = da;
	isTrigger = false;
	shmblock = Da2BlockSilent( da );
	//shmblock = Da2Block( da );
	unitsize = DkId2DkPtr( da )->DkSize;
	wasWritten = false;
	wrData = 0;
	if (shmblock >= 0) {
		modified = new rnBitArray2( OSI7Shm->BlockTable[shmblock].numea );
		modified->ClearAll();
		lastReceived = 0;
		data = (unsigned char *)
			malloc( OSI7Shm->BlockTable[shmblock].numea * unitsize );
		memset( data, 0, OSI7Shm->BlockTable[shmblock].numea * unitsize );
	}
	first = true;
#ifndef DB_IS_ZDF
	ddpDa = ddpDatakind( *con, datatype );
#endif
}

void DaInfo::written( unsigned long indx )
{
	if (shmblock>=0) {
		DebugC2( fprintf( stdout, "Written DA %ld index %lu\n", datatype, indx ); );
		modified->Set( indx );
		DaInfo *forInsert = this;
		if (!wasWritten) {
			atime = currentEnv.currentASTime;
			writtenDAs.Add( forInsert );
			wasWritten = true;
		}
	}
}

#ifdef DB_IS_ZDF
ZdfPartRequest *DaInfo::login()
{
	ZdfPartRequest *res = new ZdfPartRequest( "ZDFASLI" );
	res->Space( ZdfPartSpace( datatype, datatype ) )
		. PmFlags (MUSTREPLY)
		. DFlags (AS_WHEN_WRITTEN|READ_IDS|AS_WRITER_AREA);
	return (res);
}
#else
void DaInfo::login()
{
	AutoDataQuery *aq = new AutoDataQuery( dbConnection, datatype );
	ddpDatakind DA = ddpDatakind( *dbConnection, datatype );
	if (DA.isPersistent())
		aq->time( ddpTime (ddpTimeNow, ddpTimeForever) );
	else
		aq->time( ddpTime (ddpTimeNow, ddpTimeForever, false) );
	myDdpCompQuery * caq = new myDdpCompQuery( dbConnection, aq );
	caq->startJob();
	cerr << "Datenanmeldung fuer DA " << datatype << endl;
}
#endif

#ifdef DB_IS_ZDF
ZdfPartRequest *DaInfo::sendData()
{
	DebugB1( fprintf( stdout, "Send data for DA %ld\n", datatype ); );
	register rnBitArray2Iterator it = modified->begin();
	register rnBitArray2Iterator baEnd = modified->end();
	SEArray<long> ids;
	for (it = modified->begin(); it != baEnd; ++it) {
		DebugB2( fprintf( stdout, "Data from index %lu Id %ld\n", *it, OSI7Shm->BlockTable[shmblock].eaid[*it] ); );
		ids.Add( OSI7Shm->BlockTable[shmblock].eaid[*it] );
	}
	wrData = (unsigned char *) malloc( ids.GetNum() * unitsize );
	unsigned char *ptr = wrData;
	for (it = modified->begin(); it != baEnd; ++it) {
		DebugB3(	fprintf( stdout, "Data from index %lu:\n", *it );
		hd_string( data + *it * unitsize, unitsize, stdout ); );
		memcpy( ptr, data + *it * unitsize, unitsize );
		ptr += unitsize;
	}
	ZdfPartRequest *res = new ZdfPartRequest( "ZDFWRITE" );
	res->Space( ZdfPartSpace( datatype, ids, datatype ) )
		.	PRData( ids.GetNum() * unitsize, wrData );
	modified->ClearAll();
	return (res);
}
#else
void DaInfo::sendData( ddpTransaction &trans )
{
	DebugB1( fprintf( stdout, "Send data for DA %ld\n", datatype ); );
	try {
		register rnBitArray2Iterator it = modified->begin();
		register rnBitArray2Iterator baEnd = modified->end();
		ddpList l( ddpDa );
		SEArray<long> ids;
		for (it = modified->begin(); it != baEnd; ++it) {
			DebugB2( fprintf( stdout, "Data from index %lu Id %ld\n", *it, OSI7Shm->BlockTable[shmblock].eaid[*it] ); );
			ddpObject obj( ddpDa );
			obj.copyFrom( data + *it * unitsize );
			obj[ "eaid" ] = OSI7Shm->BlockTable[shmblock].eaid[*it];
			//obj[ "time" ] = time( 0L );
			obj[ "time" ] = atime;
			l.push_back( obj );
		}
		trans.put( l );
	}
	catch ( exception &err) {
		cerr << "Exception " << _src_ << " " << err.what() << endl;
	}
	try {
		modified->ClearAll();
	}
	catch ( exception &err) {
		cerr << "Exception " << _src_ << " " << err.what() << endl;
	}
}
#endif

void DaInfo::loginAll()
{
#ifdef DB_IS_ZDF
	ZdfRequest rq;
	avlTreeIterator<long, DaInfo*> it (daList);
	while (it.next()) {
		DaInfo *info = it.data();
		ZdfPartRequest *log = info->login();
		rq.Append( *log );
		delete log;
	}
	if (!rq.Send( zdf )) {
		fprintf( stderr, "Couldn't send initial login\n" );
		cleanup_libs( 10 );
	}
#else
	avlTreeIterator<long, DaInfo*> it (daList);
	while (it.next())
		it.data()->login();;
#endif
}

void DaInfo::cleanUp()
{
	if (wrData) { free( wrData ); wrData = 0; }
	wasWritten = false;
}

extern bool execError;

void DaInfo::receive( DAREAD *ans )
{
	if (first) { first = false; return; }
	DebugA1( fprintf( stdout, "In DA %ld #%lu\n", ans->datatype, (unsigned long) ans->idnum ); );
	if (!isTrigger) return;
	register zdfAnzType i;
	currentEnv.currentTime = hb_time( 0L );
	currentEnv.currentDa = ans->datatype;
	currentEnv.currentBlock = shmblock;
	currentEnv.currentASTime = ans->atime;
	for ( i = 0; i < ans->idnum; i++ ) {
		DebugA2( fprintf( stdout, "  In EA %ld\n", ans->id[i] ); );
		currentEnv.currentEaIndx = GetEAIndexInBlockByEaid( shmblock, ans->id[i] );
		currentEnv.currentData = (unsigned char*) ans->resdata + i * unitsize;
		//currentEnv.currentNode = ans->id[i] / 256;
		//currentEnv.currentDe = ans->id[i] % 256;
		currentEnv.currentNode = OSI7Shm->DevTable[ OSI7Shm->BlockTable[shmblock].treepos[currentEnv.currentEaIndx] ].DevInfo.OSI7;
		currentEnv.currentDe = OSI7Shm->BlockTable[shmblock].DE[currentEnv.currentEaIndx];
		currentEnv.currentEaid = ans->id[i];
		DEData2 s, d;
		unsigned long anz = 0;
		d.size = s.size = unitsize;
		d.DE = s.DE = currentEnv.currentData;
		register unsigned long j, limit_act = actions.GetNum();
		for ( j = 0; j < limit_act; j++ ) {
			DebugA2( fprintf( stdout, "Action #%lu\n", j ); );
			DebugA3(	fprintf( stdout, "ACTION:\n" );
						actions[j]->dump( "act" ); );
			//rsTime startOp;
			execError = false;
			actions[j]->OpDo( s, d, anz );
			//rsTime endOp;
			//fprintf( stdout, "took " ); (endOp - startOp).println();
		}
		d.size = s.size = 0;
		d.DE = s.DE = 0;
	}
}

void DaInfo::takeNewData( DAREAD *ans )
{
	register zdfAnzType i;
	for ( i = 0; i < ans->idnum; i++ ) {
		long indx = GetEAIndexInBlockByEaid( shmblock, ans->id[i] );
		if (indx >= 0)
			memcpy( data + indx * unitsize, ans->resdata + i * unitsize, unitsize );
		else 
			cerr << "IGNORE UNKNOWN EA-ID " << ans->id[i] << " datatype " << ans->datatype << endl;
	}
}

List *recvAs(NIPD_ADR *zdf, unsigned long to)
{
	if (to == 0) {
		//cout << "Receive NO WAIT\n"; cout.flush();
		return (asrecnw( zdf ));
	}
	if (to >= LONG_MAX) {
		//cout << "Receive WAIT NEXT MSG\n"; cout.flush();
		return (asrec( zdf ));
	}
	//cout << "Receive WAIT TIMEOUT " << to << endl; cout.flush();
	return (asrecto (zdf, to ));
}

// ggfs modifizierte Daten -> ZDF
void DaInfo::sendAll()
{
	try {
	if ( writtenDAs.GetNum() ) {
	  int i;
	  int limit = writtenDAs.GetNum();
#ifdef DB_IS_ZDF
		ZdfRequest rq;
		for ( i = 0; i < limit; i++ ) {
			ZdfPartRequest *log = writtenDAs[i]->sendData();
			rq.Append( *log );
			delete log;
		}
		if (!rq.Send( zdf )) {
			fprintf( stderr, "Couldn't send changed data\n" );
			cleanup_libs( 10 );
		}
		for ( i = 0; i < limit; i++ ) {
			writtenDAs[i]->cleanUp();
		}
		writtenDAs.RemAll();
#else
		ddpTransaction trans( *dbConnection );
		for ( i = 0; i < limit; i++ ) {
			writtenDAs[i]->sendData( trans );
		}
		trans.commit();
		for ( i = 0; i < limit; i++ ) {
			writtenDAs[i]->cleanUp();
		}
		writtenDAs.RemAll();
#endif
	}
	}
	catch (exception &ex) {
		cerr << _src_ << " Exception: " << ex.what() << endl;
	}
}

Timer	timer(TimerDeletesSingleShots);
unsigned long MsgCnt = 0;

void DaInfo::work()
{
	timer.start ();
	for (;;) {
		//StartFunc ();
		List *anslist;
		
		//errno = 0;

		// autosends empfangen
		anslist = recvAs (zdf, (ulong) timer.getNextDelay ());
		MsgCnt++;
		
		// timer aktualisieren
		timer.update ();
		// ggfs modifizierte Daten -> ZDF
		sendAll();

		if ( anslist == ( List * ) Nil ) {
			if ( errno == EINTR ) continue;
			fprintf ( stderr,
				"%s Fehler beim Autsend Empfangen\n", shorttime ( hb_time ( 0L ) ) );
			perror ( "recvAs" );
			break; /* de facto prgramm ende */
		}
		
		if ( anslist != (List*) 1 ) { /* nicht timeout */
			WITHREGLIST ( anslist, slt ) {
				DAREAD *ans = (DAREAD *) GetNodeData( slt );
//AktuelleZeit.preSet (time (0L));

				if ( ans->pans->Media == ANS_Q ) {
					if ( !daArray[ans->datatype] ) {
						fprintf ( stderr,
							"%s > Unangeforderte DA=%ld(Msg) erhalten. Flags=%d\n",
							shorttime ( hb_time ( 0L ) ), ans->datatype, ans->pans->Flags );
					}
					else {
						daArray[ans->datatype]->receive( ans );
						daArray[ans->datatype]->takeNewData( ans );
					}
				}
				else {
					fprintf ( stderr,
						"%s > Unangeforderte DA=%ld(File) erhalten. Flags=%d\n",
						shorttime ( hb_time ( 0L ) ), ans->datatype, ans->pans->Flags );
				}
				// daten der teilantwort freigeben
				free ( (char *) ans->pans );
				if ( ans->resnum > 0 ) free ( (char *) ans->resdata );
				if ( ans->idnum > 0 ) free ( (char *) ans->id );
			} ENDWITH;
		}
		// antwortliste freigeben
		if ( anslist && anslist != (List*) 1 )
			Destroy( ( Node * ) anslist );

		// ggfs modifizierte Daten -> ZDF
		sendAll();
	}
}

