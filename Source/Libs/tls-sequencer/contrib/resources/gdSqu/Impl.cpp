#ifdef DB_IS_ZDF
#include "SEArrayT.hc"
#else
#include "SEArrayT.h"
#endif
#include <avlTree.hc>
#include "ZD.h"
#include "SeqOp.h"
#include "SeqValue.h"
#include "DaInfo.h"
#include "Timer.h"
#include "CurEnv.h"

struct DaInfo;
class MonoflopDone;

CurrentEnv currentEnv; 

//long currentEaid;
//long currentDa;
//long currentDe;
//long currentNode;
//long currentTime;
//long currentASTime;

//long currentEaIndx;
//short currentBlock;
//unsigned char *currentData;

ValueSeqInternal internalKnotenNummer(
	&currentEnv.currentNode, 					"Node", 			0);
ValueSeqInternal internalDeNummer(
	&currentEnv.currentDe, 						"De", 			0);
ValueSeqInternal internalEaid(			
	&currentEnv.currentEaid, 					"Eaid", 			0);
ValueSeqInternal internalEaindex(			
	&currentEnv.currentEaIndx, 				"Eaindex", 		0);
ValueSeqInternal internalAktuelleZeit(	
	&currentEnv.currentTime,					"Time", 			0);
ValueSeqInternal internalAktuelleDa(	
	&currentEnv.currentDa, 						"Datatype",		0);
ValueSeqInternal internalAutosendZeit(	
	&currentEnv.currentASTime,					"ASTime",		0);
ValueSeqInternal internalAusloesezeitpunkt(	
	&currentEnv.letzterAusloeseZeitpunkt,	"LastShot",		0);
ValueSeqInternal internalErsterAusloesezeitpunkt(	
	&currentEnv.ersterAusloeseZeitpunt,		"FirstShot",	0);

//TmplSEArray (TransOp*, 10 );
//TmplSEArray (TransOpInit*, 5 );
//TmplSEArray (TransOpExit*, 5 );
#if 0 // ndef WIN32
TmplSEArray (ValueCarrier*, 5 );
TmplSEArray (DaInfo*, 5 );
TmplSEArrayS (CaseVal2Inst, 10 );
TmplAvlTree (ZDString, ValueCarrier*);
TmplAvlTree (long, MonoflopDone*);
TmplAvlIter (long, DaInfo*);
TmplSEArray  (TimerRequest_inTimer*, 20);
#endif
