#include <hbtimewrap.h>
#ifdef DB_IS_ZDF
#include "SEArrayT.hc"
#else
#include "SEArrayT.h"
#endif
#include "SeqOp.h"
#include <Property.h>
#include "ValueList.h"
#include "Timer.h"
#include "CurEnv.h"
#include <avlTree.h>
#include <RefDblLink.h>
#include "IdList.h"

extern FILE *dbg;

#define CLFUN(type,cl,code) type cl :: code
#define CLFUN2(type,cl,code,Value) type cl :: code( #Value, 1 );

#define SPECIES Species() const { return ((unsigned long)&SpeciesDum); }

CLFUN(unsigned long,	TransOpBlock,				SPECIES)
CLFUN(unsigned long,	TransOpErrMsg,				SPECIES)
CLFUN(unsigned long,	TransOpSysCmd,				SPECIES)
CLFUN(unsigned long,	TransOpDoAssign,				SPECIES)
CLFUN(unsigned long,	TransOpBreakableBlock,	SPECIES)
CLFUN(unsigned long,	TransOpSwitch,				SPECIES)
CLFUN(unsigned long,	TransOpBreak,		   	SPECIES)
CLFUN(unsigned long,	TransOpNop,			   	SPECIES)
CLFUN(unsigned long,	TransOpWrite,			   SPECIES)
CLFUN(unsigned long,	TransOpCLoop,			   SPECIES)
CLFUN(unsigned long,	TransOpCLoopWhile,		SPECIES)
CLFUN(unsigned long,	TransOpCLoopDoWhile,		SPECIES)
CLFUN(unsigned long,	TransOpContinue,			SPECIES)
CLFUN(unsigned long,	TransOpMonoflop,			SPECIES)
CLFUN(unsigned long,	TransOpRtrMonoflop,		SPECIES)

/*
#define CLSPECIES ClSpecies() { return ((unsigned long)&SpeciesDum); }

CLFUN(unsigned long,	TransOpIncLoop,	CLSPECIES)
CLFUN(unsigned long,	TransOpDecLoop,	CLSPECIES)
CLFUN(unsigned long,	TransOpIf,			CLSPECIES)
CLFUN(unsigned long,	TransOpIfElse,		CLSPECIES)
*/

#define SPECIESDUM SpeciesDum = new Property

CLFUN2(Property *,	TransOpBlock,				SPECIESDUM, Op_Block)
CLFUN2(Property *,	TransOpErrMsg,				SPECIESDUM, Op_ErrMsg)
CLFUN2(Property *,	TransOpSysCmd,				SPECIESDUM, Op_SysCmd)
CLFUN2(Property *,	TransOpDoAssign,				SPECIESDUM, Op_DoAssign)
CLFUN2(Property *,	TransOpBreakableBlock,	SPECIESDUM, Op_BrkBlock)
CLFUN2(Property *,	TransOpSwitch,				SPECIESDUM, Op_Switch)
CLFUN2(Property *,	TransOpBreak,			   SPECIESDUM, Op_Break)
CLFUN2(Property *,	TransOpNop,			 	  	SPECIESDUM, Op_Nop)
CLFUN2(Property *,	TransOpWrite,			 	SPECIESDUM, Op_Write)
CLFUN2(Property *,	TransOpCLoop,			 	SPECIESDUM, Op_CLoop)
CLFUN2(Property *,	TransOpCLoopWhile,		SPECIESDUM, Op_CLWhile)
CLFUN2(Property *,	TransOpCLoopDoWhile,		SPECIESDUM, Op_CLDoWhile)
CLFUN2(Property *,	TransOpContinue,			SPECIESDUM, Op_Continue)
CLFUN2(Property *,	TransOpMonoflop,			SPECIESDUM, Op_Monoflop)
CLFUN2(Property *,	TransOpRtrMonoflop,		SPECIESDUM, Op_RtrMonoflop)

#define GETPROPBASIC GetProp() const { return (*SpeciesDum); }
#define GETPROPINHER(b) GetProp() const \
	{ return (b::GetProp().Add(*SpeciesDum)); }

CLFUN(Property, TransOpBreak,				GETPROPBASIC)
CLFUN(Property, TransOpErrMsg,			GETPROPBASIC)
CLFUN(Property, TransOpSysCmd,			GETPROPBASIC)
CLFUN(Property, TransOpDoAssign,			GETPROPBASIC)
CLFUN(Property, TransOpNop,				GETPROPBASIC)
CLFUN(Property, TransOpWrite,				GETPROPBASIC)
CLFUN(Property, TransOpContinue,			GETPROPBASIC)
CLFUN(Property, TransOpMonoflop,			GETPROPBASIC)
CLFUN(Property, TransOpBlock,				GETPROPINHER(TransBase))
CLFUN(Property, TransOpBreakableBlock,	GETPROPINHER(TransOpBlock))
CLFUN(Property, TransOpSwitch,			GETPROPINHER(TransOpBreakableBlock))
CLFUN(Property, TransOpCLoop,				GETPROPINHER(TransOpBreakableBlock))
CLFUN(Property, TransOpCLoopWhile,		GETPROPINHER(TransOpCLoop))
CLFUN(Property, TransOpCLoopDoWhile,	GETPROPINHER(TransOpCLoop))
CLFUN(Property, TransOpRtrMonoflop,		GETPROPINHER(TransOpMonoflop))


// -----------------------------------------------------------------------------
// TransOpBlock
// -----------------------------------------------------------------------------

Bool TransOpBlock::OpDo( DEData2 &src, DEData2 &dest, unsigned long &anz_bytes )
{
	unsigned long i, limit;
	
	limit = Inits.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Inits[i]->OpInit(limit, i, src, dest) ) return (False);
	}
	limit = Ops.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Ops[i]->OpDo(src, dest, anz_bytes) ) return (False);
	}
	limit = Exits.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Exits[i]->OpExit(limit, i, src, dest, anz_bytes) ) return (False);
	}

	return True;
}

// -----------------------------------------------------------------------------
// TransOpSwitch
// -----------------------------------------------------------------------------

static int cmp_CaseVal2Inst( const void *p1, const void *p2 )
{
	register CaseVal2Inst	*cv1 =(CaseVal2Inst*) p1,
									*cv2 =(CaseVal2Inst*) p2;
	return cv1->constVal - cv2->constVal;
}

TransOpSwitch::TransOpSwitch()
{
	defaultIndex.constVal = -1;
	jmpTable.SetCompareFunc( cmp_CaseVal2Inst );
	checkExpression = 0;
}

TransOpSwitch::~TransOpSwitch()
{
	if (checkExpression) delete checkExpression;
}

// take over others instructions
void TransOpSwitch::addOps( TransOpBlock *ins )
{
	while (ins->Inits.GetNum()) {
		AddInit( ins->Inits[0] );
		ins->Inits.Rem( 0 );
	}
	while (ins->Exits.GetNum()) {
		AddExit( ins->Exits[0] );
		ins->Exits.Rem( 0 );
	}
	while (ins->Ops.GetNum()) {
		Add( ins->Ops[0] );
		ins->Ops.Rem( 0 );
	}
}

bool TransOpSwitch::addCase( long p_caseVal, TransOpBlock *ins )
{

	CaseVal2Inst cv2i;
	cv2i.constVal = p_caseVal;
	cv2i.instructionIndex = Ops.GetNum();
	//fprintf( stderr, "Case %ld at index %lu\n", cv2i.constVal, cv2i.instructionIndex );
	if (jmpTable.Find( cv2i )) return (false);
	jmpTable.Add( cv2i );
	jmpTable.Sort();
	addOps( ins );
	return (true);
}

void TransOpSwitch::addDefault( TransOpBlock *ins )
{
	defaultIndex.constVal = 1;
	defaultIndex.instructionIndex = Ops.GetNum();
	//fprintf( stderr, "Default at index %lu\n", defaultIndex.instructionIndex );
	addOps( ins );
}

void TransOpSwitch::setExpr( Value *expr )
{
	checkExpression = expr;
}

Bool TransOpSwitch::OpDo( DEData2 &src, DEData2 &dest, unsigned long &anz_bytes )
{
	unsigned char *basedata;
	if ( dir == Send ) basedata = src.DE;
	else               basedata = dest.DE;

	//dump( "OpDo" );
	long exprVal = checkExpression->GetVal( basedata );
	//fprintf( stderr, "Switch(%ld)\n", exprVal );
	CaseVal2Inst seek, *res = 0;
	seek.constVal = exprVal;
	if (!(res = jmpTable.Find( seek ))) {
		//fprintf( stderr, "No Case\n" );
		if (defaultIndex.constVal > -1) { // default ausfuehren
			seek = defaultIndex;
			//fprintf( stderr, "Use Default at index %lu\n", seek.instructionIndex );
		} else {
			//fprintf( stderr, "No Default\n" );
			seek.constVal = -1;
		}
	} else {
		seek = *res;
		//fprintf( stderr, "Use Case at index %lu\n", seek.instructionIndex );
	}
	unBroken();
	if (seek.constVal > -1) {
		unsigned long i, limit;
		limit = Inits.GetNum();
		for ( i = 0; i < limit; i++ ) {
			if ( ! Inits[i]->OpInit(limit, i, src, dest) ) return (False);
		}
		limit = Ops.GetNum();
		for ( i = seek.instructionIndex; i < limit; i++ ) {
			if ( ! Ops[i]->OpDo(src, dest, anz_bytes) ) return (False);
			if (isBroken()) break;
		}
		limit = Exits.GetNum();
		for ( i = 0; i < limit; i++ ) {
			if ( ! Exits[i]->OpExit(limit, i, src, dest, anz_bytes) ) return (False);
		}
		if (isBroken() == this) unBroken();
	}
	return True;
}

void TransOpSwitch::dump(char *s)
{
	fprintf( dbg, "switch(%s)( ", /*)*/ s );
	checkExpression->dump();
	fprintf( dbg, /*(*/ " ) {\n" /*}*/ );
	TransOpBreakableBlock::dump( "" );
	fprintf( dbg, /*{*/ "\n}\n" );
}

int TransOpSwitch::operator ==(const TransOp& oth) const
{
	if ( Species() != oth.Species() ) return (0);
	TransOpSwitch &other =(TransOpSwitch &) oth;
	if (!(*checkExpression == *other.checkExpression)) return (0);
	return ( TransOpBreakableBlock::operator ==(other) );
}

unsigned long TransOpSwitch::OpSize()
{
	return (VARIABLE_SIZE);
}

void TransOpSwitch::Compact()
{
	unsigned long i, limit;
	limit = Ops.GetNum();
	for ( i = 0; i < limit; i++ ) {
		Ops[i]->Compact();
	}
}

// -----------------------------------------------------------------------------
// TransOpCLoop
// -----------------------------------------------------------------------------

TransOpCLoop *TransOpCLoop::continued = 0;

Bool TransOpCLoop::OpDo( DEData2 &src, DEData2 &dest, unsigned long &anz_bytes )
{
	unsigned char *basedata;
	if ( dir == Send ) basedata = src.DE;
	else               basedata = dest.DE;

	unsigned long i, limit;
	limit = Inits.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Inits[i]->OpInit(limit, i, src, dest) ) return (False);
	}
	unContinued();
	limit = Ops.GetNum();
	for (;;) {
		unBroken();
		if (preCheckExpression() && !checkExpression->GetVal( basedata ))
			break;
		for ( i = 0; i < limit; i++ ) {
			if ( ! Ops[i]->OpDo(src, dest, anz_bytes) ) return (False);
			if (isBroken()) break;
		}
		if (i<limit && !isContinued()) break; // break alone
		if (isContinued() && isContinued() != this) break;
		unContinued();
		if (!preCheckExpression() && !checkExpression->GetVal( basedata ))
			break;
	}
	limit = Exits.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Exits[i]->OpExit(limit, i, src, dest, anz_bytes) ) return (False);
	}
	if (isBroken() == this) unBroken();
	return True;
}

// -----------------------------------------------------------------------------
// TransOpCLoopWhile
// -----------------------------------------------------------------------------

void TransOpCLoopWhile::dump (char *s)
{
	fprintf( dbg, "%s: OpWhile %p expr (\n", s, this ); fflush(dbg);
	checkExpression->dump();
	fprintf( dbg, " ) {\n" );
	TransOpCLoop::dump( "" );
	fprintf( dbg, "\n}\n" );
}

// -----------------------------------------------------------------------------
// TransOpCLoopDoWhile
// -----------------------------------------------------------------------------

void TransOpCLoopDoWhile::dump (char *s)
{
	fprintf( dbg, "%s: OpDoWhile %p {\n", s, this ); fflush(dbg);
	TransOpCLoop::dump( "" );
	fprintf( dbg, "\n} while expr (\n" );
	checkExpression->dump();
	fprintf( dbg, " )\n" );
}

// -----------------------------------------------------------------------------
// TransOpBreakableBlock
// -----------------------------------------------------------------------------
TransOpBreakableBlock *TransOpBreakableBlock::broken = 0;

Bool TransOpBreakableBlock::OpDo( DEData2 &src, DEData2 &dest, unsigned long &anz_bytes )
{
	unsigned long i, limit;
	
	limit = Inits.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Inits[i]->OpInit(limit, i, src, dest) ) return (False);
	}
	limit = Ops.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Ops[i]->OpDo(src, dest, anz_bytes) ) return (False);
		if (isBroken()) break;
	}
	limit = Exits.GetNum();
	for ( i = 0; i < limit; i++ ) {
		if ( ! Exits[i]->OpExit(limit, i, src, dest, anz_bytes) ) return (False);
	}
	if (isBroken() == this) unBroken();

	return True;
}

// -----------------------------------------------------------------------------
// TransOpBreak
// -----------------------------------------------------------------------------

Bool TransOpBreak::OpDo( DEData2 &, DEData2 &, unsigned long & )
{
	breakBlock->doBreak();
	return True;
}

void TransOpBreak::dump(char *s)
{
	fprintf( dbg, "%s: OpBreak %p", s, this ); fflush(dbg);
}

int TransOpBreak::operator ==(const TransOp& other) const
{
	if ( Species() != other.Species() ) return (0);
	TransOpBreak &oth =(TransOpBreak &) other;
	if ( !( *breakBlock == *oth.breakBlock ) ) return (0);
	return (1);
}

unsigned long TransOpBreak::OpSize()
{
	return (0);
}

// -----------------------------------------------------------------------------
// TransOpContinue
// -----------------------------------------------------------------------------

Bool TransOpContinue::OpDo( DEData2 &, DEData2 &, unsigned long & )
{
	continueBlock->doBreak(); continueBlock->doContinue();
	return True;
}

void TransOpContinue::dump(char *s)
{
	fprintf( dbg, "%s: OpContinue %p", s, this ); fflush(dbg);
}

int TransOpContinue::operator ==(const TransOp& other) const
{
	if ( Species() != other.Species() ) return (0);
	TransOpContinue &oth =(TransOpContinue &) other;
	if ( !( *continueBlock == *oth.continueBlock ) ) return (0);
	return (1);
}

unsigned long TransOpContinue::OpSize()
{
	return (0);
}

// -----------------------------------------------------------------------------
// TransOpNop
// -----------------------------------------------------------------------------

Bool TransOpNop::OpDo( DEData2 &, DEData2 &, unsigned long & )
{
	return True;
}

void TransOpNop::dump(char *s)
{
	fprintf( dbg, "%s: OpNop %p", s, this ); fflush(dbg);
}

int TransOpNop::operator ==(const TransOp& other) const
{
	if ( Species() != other.Species() ) return (0);
	return (1);
}

unsigned long TransOpNop::OpSize()
{
	return (0);
}

// -----------------------------------------------------------------------------
// TransOpWrite
// -----------------------------------------------------------------------------

extern bool SendActualData2ZDF();
Bool TransOpWrite::OpDo( DEData2 &, DEData2 &, unsigned long & )
{
	if (!SendActualData2ZDF()) return (False);
	return True;
}

void TransOpWrite::dump(char *s)
{
	fprintf( dbg, "%s: OpWrite %p", s, this ); fflush(dbg);
}

int TransOpWrite::operator ==(const TransOp& other) const
{
	if ( Species() != other.Species() ) return (0);
	return (1);
}

unsigned long TransOpWrite::OpSize()
{
	return (0);
}

// -----------------------------------------------------------------------------
// TransOpErrMsg
// -----------------------------------------------------------------------------

extern bool SendActualData2ZDF();
Bool TransOpErrMsg::OpDo( DEData2 &s, DEData2 &d, unsigned long & )
{
	unsigned char *basedata;
	if ( dir == Send ) basedata = s.DE;
	else               basedata = d.DE;

	time_t now = hb_time( 0L );
	fprintf( stderr, "gmtime=%s", hb_asctime( hb_gmtime( &now ) ) );
	register unsigned long i = 0;
	char *prc = 0;
	prc = strchr( fmtString, '%' );
	char *start = fmtString;
	while (prc) {
		if (*(prc+1) && *(prc+1) == '%') {
			*(prc+1) = 0;
			fprintf( stderr, "%s", start );
			*(prc+1) = '%';
			start = prc+2;
			prc++; // no arg: %%
		} else {
			*prc = 0;
			fprintf( stderr, "%s%ld", start, ((*valueList)[i++])->GetVal( basedata ) );
			*prc = '%';
			start = prc + 1;
		}
		prc = strchr( prc+1, '%' );
	}
	if (*start) fprintf( stderr, "%s", start );
	fprintf( stderr, "\n" );
	return True;
}

void TransOpErrMsg::dump(char *s)
{
	fprintf( dbg, "%s: OpErrMsg %p\n  (\"%s\" ...\n", s, this, fmtString );
	fflush(dbg);
}

int TransOpErrMsg::operator ==(const TransOp& oth) const
{
	if ( Species() != oth.Species() ) return (0);
	TransOpErrMsg &other =(TransOpErrMsg &) oth;
	if (strcmp( fmtString, other.fmtString )) return (0);
	if (valueList->GetNum() != other.valueList->GetNum()) return (0);
	register unsigned long i, limit = valueList->GetNum();
	for ( i = 0; i < limit; i++ ) {
		if (!(*((*valueList)[i]) == *((*other.valueList)[i]))) return (0);
	}
	return (1);
}

unsigned long TransOpErrMsg::OpSize()
{
	return (0);
}

// -----------------------------------------------------------------------------
// TransOpSysCmd
// -----------------------------------------------------------------------------

Bool TransOpSysCmd::OpDo( DEData2 &s, DEData2 &d, unsigned long & )
{
	unsigned char *basedata;
	if ( dir == Send ) basedata = s.DE;
	else               basedata = d.DE;

	char execStr[16*1024];
	unsigned short execStrPtr = 0;
	register unsigned long i = 0;
	char *prc = 0;
	execStrPtr += sprintf( execStr + execStrPtr, "exec ksh -c \"" );
	prc = strchr( fmtString, '%' );
	char *start = fmtString;
	while (prc) {
		if (*(prc+1) && *(prc+1) == '%') {
			*(prc+1) = 0;
			execStrPtr += sprintf( execStr + execStrPtr, "%s", start );
			*(prc+1) = '%';
			start = prc+2;
			prc++; // no arg: %%
		} else {
			*prc = 0;
			execStrPtr += sprintf( execStr + execStrPtr, "%s%ld", start, ((*valueList)[i++])->GetVal( basedata ) );
			*prc = '%';
			start = prc + 1;
		}
		prc = strchr( prc+1, '%' );
	}
	if (*start) execStrPtr += sprintf( execStr + execStrPtr, "%s", start );
	execStrPtr += sprintf( execStr + execStrPtr, "\"&" );
	//fprintf( stderr, "Executing: '%s'\n", execStr );
	system( execStr );
	return True;
}

void TransOpSysCmd::dump(char *s)
{
	fprintf( dbg, "%s: OpErrMsg %p\n  (\"%s\" ...\n", s, this, fmtString );
	fflush(dbg);
}

int TransOpSysCmd::operator ==(const TransOp& oth) const
{
	if ( Species() != oth.Species() ) return (0);
	TransOpSysCmd &other =(TransOpSysCmd &) oth;
	if (strcmp( fmtString, other.fmtString )) return (0);
	if (valueList->GetNum() != other.valueList->GetNum()) return (0);
	register unsigned long i, limit = valueList->GetNum();
	for ( i = 0; i < limit; i++ ) {
		if (!(*((*valueList)[i]) == *((*other.valueList)[i]))) return (0);
	}
	return (1);
}

unsigned long TransOpSysCmd::OpSize()
{
	return (0);
}

// -----------------------------------------------------------------------------
// TransOpDoAssign
// -----------------------------------------------------------------------------

extern bool SendActualData2ZDF();
Bool TransOpDoAssign::OpDo( DEData2 &s, DEData2 &d, unsigned long & )
{
	unsigned char *basedata;
	if ( dir == Send ) basedata = s.DE;
	else               basedata = d.DE;

	value->GetVal( basedata );
	return True;
}

void TransOpDoAssign::dump(char *s)
{
	fprintf( dbg, "%s: OpAssign %p\n   ", s, this );
	value->dump();
	fflush(dbg);
}

int TransOpDoAssign::operator ==(const TransOp& oth) const
{
	if ( Species() != oth.Species() ) return (0);
	TransOpDoAssign &other =(TransOpDoAssign &) oth;
	if (!(*value == *other.value)) return (0);
	return (1);
}

unsigned long TransOpDoAssign::OpSize()
{
	return (0);
}

// -----------------------------------------------------------------------------
// TransOpMonoflop
// -----------------------------------------------------------------------------

extern unsigned long MsgCnt;
extern CurrentEnv		currentEnv;
extern Timer			timer;

class FlopContainer;

class MonoflopDone : public TimerDrain, public DblLink {
	friend class TransOpMonoflop;
	friend class TransOpRtrMonoflop;
	private:
		//unsigned long	entryTime;
		unsigned long	entryCnt;
		CurrentEnv		mdCurrentEnv;
		TransOp			*action;
		FlopContainer	**beingIn;
		long				ownKey;
	public: 
		void TimerExpired( long now, unsigned long uval);
		DblPtrRetFuncs(MonoflopDone);
};

class FlopContainer : public avlTree<long, MonoflopDone*> {
	//public: 
	//	avlTree<long, MonoflopDone*> flops;
};

void MonoflopDone::TimerExpired( long now, unsigned long /*uval*/)
{
	//fprintf( stderr, "%s -> %p\n", __PRETTY_FUNCTION__, this );
	CurrentEnv save = currentEnv;
	currentEnv = mdCurrentEnv;
	currentEnv.ersterAusloeseZeitpunt = currentEnv.currentTime;
	currentEnv.currentTime = now;
	if (isolated()) {
		(*beingIn)->remove( ownKey );
		if (!(*beingIn)->getCardinal()) {
			delete (*beingIn);
			*beingIn = 0;
		}
	} else {
		//fprintf( stderr, "%s[%d]\n", __FILE__, __LINE__ );
		MonoflopDone *tst;
		//fprintf( stderr, "%s[%d]\n", __FILE__, __LINE__ );
		//(*beingIn)->find( ownKey, tst);
		if (first() == this) {
			// dieser ist der erste in der liste
			(*beingIn)->replace( ownKey, next(), tst);
		}
		detach();
	}
	DEData2 s, d;
	unsigned long anz = 0;
	d.size = s.size = 0;
	d.DE = s.DE = 0;
	//fprintf( stderr, "%s[%d] act -> %p\n", __FILE__, __LINE__, action );
	action->OpDo( s, d, anz );
	currentEnv = save;
}

bool TransOpMonoflop::trigger( long key, unsigned char *basedata )
{
	MonoflopDone *mfd;
	bool mfExists = false;
	if (realFlops->find( key, mfd ) == 0)
		mfExists = true;
	if (mfExists) {
		if (exclusive) return (true); // Es laeuft schon ein Monflop
		MonoflopDone *walk = mfd->first();
		while (walk) {
			if (walk->entryCnt == MsgCnt)
				return (true); // Es laeuft schon ein Monflop
			walk = walk->next();
		}
	}
	MonoflopDone *nmfd = new MonoflopDone;
	TimerRequest tr;
	tr.sec_from_now = timeout->GetVal( basedata );
	timer.insert( tr, nmfd );
	//fprintf( stderr, "%p -> timer\n", nmfd );
	nmfd->beingIn = &realFlops;
	nmfd->action = action;
	nmfd->mdCurrentEnv = currentEnv;
	nmfd->mdCurrentEnv.letzterAusloeseZeitpunkt = currentEnv.currentTime;
	nmfd->entryCnt = MsgCnt;
	nmfd->ownKey = key;
	if (!mfExists) realFlops->insert( key, nmfd );
	else mfd->append( nmfd );
	return (true);
}

Bool TransOpMonoflop::OpDo (DEData2 &src, DEData2 &dest, unsigned long&)
{
	unsigned char *basedata;
	if ( dir == Send ) basedata = src.DE;
	else               basedata = dest.DE;

	if (!realFlops) realFlops = new FlopContainer;
	switch (mfType) {
		case MF_T_EA: 
			trigger( currentEnv.currentEaid, basedata );
			break;
		case MF_T_ONCE: 
			trigger( 0, basedata );
			break;
		case MF_T_NODE: 
			trigger( currentEnv.currentNode, basedata );
			break;
		case MF_T_EA2CL: 
			IdList tl;
			bool res = GetIdList(	currentEnv.currentEaid,
												currentEnv.currentEaIndx,
												GUELTIG_NORMALE_DES_AN_CLUSTER_DE,
												entryDa,
												entryDa,
												true,
												EAID_GET,
												tl );
			if (!res || tl.GetNum() != 1) {
				fprintf( stderr,
					"%s> Couldn't start Monoflop for cluster of %ld in DA %ld\n",
					shorttime( hb_time( 0L ) ), currentEnv.currentEaid, entryDa );
				//if (idl) delete idl;
				return (false);
			}
			trigger( tl[0], basedata );
			//delete idl;
			break;
	}
	return (True);
}

TransOpMonoflop::TransOpMonoflop()
{
	realFlops = 0;
	entryDa = 0;
	timeout = 0;
	action = 0;
	mfType = 0;
	exclusive = false;
}

void TransOpMonoflop::dump(char *s)
{
	fprintf( dbg, "%s: OpMonoflop:%hd %p {", s, mfType, this ); fflush(dbg);
	action->dump( "" );
	fprintf( dbg, "} ( " );
	timeout->dump();
	fprintf( dbg, ")\n" ); fflush(dbg);
}

int TransOpMonoflop::operator ==(const TransOp& oth) const
{
	if ( Species() != oth.Species() ) return (0);
	TransOpMonoflop &other =(TransOpMonoflop &) oth;
	if (!(*action == *(other.action))) return (0);
	if (!(*timeout == *(other.timeout))) return (0);
	if (mfType != other.mfType) return (0);
	return (1);
}

unsigned long TransOpMonoflop::OpSize()
{
	return (action->OpSize());
}

// -----------------------------------------------------------------------------
// TransOpRtrMonoflop
// -----------------------------------------------------------------------------

void TransOpRtrMonoflop::dump(char *s)
{
	TransOpRtrMonoflop::dump( s );
	fprintf( dbg, "\\-> Rtr" );
	if (maxTimeout) {
		fprintf( dbg, " ( " ); maxTimeout->dump(); fprintf( dbg, " )\n" );
	}
	fflush(dbg);
}

bool TransOpRtrMonoflop::reTrigger( long key, unsigned char *basedata )
{
	MonoflopDone *mfd;
	long now = hb_time( 0L );
	if (realFlops->find( key, mfd ) == 0) {
		if (mfd->entryCnt == MsgCnt)
			return (true); // Monflop durch gleiche Message
		// retrigger it
		bool retriggered = false;
		if (maxTimeout) {
			long maxT = maxTimeout->GetVal( basedata );
			long in = now - mfd->mdCurrentEnv.currentTime;
			long extend = timeout->GetVal( basedata );
			if (in + extend > maxT) {
				extend = maxT - in;
			}
			if (extend > 0) {
				timer.remove( mfd );
				TimerRequest tr;
				tr.sec_from_now = extend;
				timer.insert( tr, mfd );
				//fprintf( stderr, "%p -> timer\n", mfd );
				retriggered = true;
			}
		} else {
			long extend = timeout->GetVal( basedata );
			timer.remove( mfd );
			TimerRequest tr;
			tr.sec_from_now = extend;
			timer.insert( tr, mfd );
			//fprintf( stderr, "%p -> timer\n", mfd );
			retriggered = true;
		}
		if (retriggered) {
			mfd->mdCurrentEnv.currentEaid = currentEnv.currentEaid;
			mfd->mdCurrentEnv.currentDa = currentEnv.currentDa;
			mfd->mdCurrentEnv.currentDe = currentEnv.currentDe;
			mfd->mdCurrentEnv.currentNode = currentEnv.currentNode;
			mfd->mdCurrentEnv.currentASTime = currentEnv.currentASTime;
			mfd->mdCurrentEnv.currentEaIndx = currentEnv.currentEaIndx;
			mfd->mdCurrentEnv.currentBlock = currentEnv.currentBlock;
			mfd->mdCurrentEnv.letzterAusloeseZeitpunkt = now;
		}
		return (true);
	}
	mfd = new MonoflopDone;
	TimerRequest tr;
	tr.sec_from_now = timeout->GetVal( basedata );
	timer.insert( tr, mfd );
	//fprintf( stderr, "%p -> timer\n", mfd );
	realFlops->insert( key, mfd );
	mfd->beingIn = &realFlops;
	mfd->action = action;
	mfd->mdCurrentEnv = currentEnv;
	mfd->mdCurrentEnv.letzterAusloeseZeitpunkt = now;
	mfd->entryCnt = MsgCnt;
	mfd->ownKey = key;
	return (true);
}

Bool TransOpRtrMonoflop::OpDo (DEData2 &src, DEData2 &dest, unsigned long&)
{
	unsigned char *basedata;
	if ( dir == Send ) basedata = src.DE;
	else               basedata = dest.DE;

	if (!realFlops) realFlops = new FlopContainer;
	switch (mfType) {
		case MF_T_EA: 
			reTrigger( currentEnv.currentEaid, basedata );
			break;
		case MF_T_ONCE: 
			reTrigger( 0, basedata );
			break;
		case MF_T_NODE: 
			reTrigger( currentEnv.currentNode, basedata );
			break;
		case MF_T_EA2CL: 
			IdList tl;
			bool res = GetIdList(	currentEnv.currentEaid,
												currentEnv.currentEaIndx,
												GUELTIG_NORMALE_DES_AN_CLUSTER_DE,
												entryDa,
												entryDa,
												true,
												EAID_GET,
												tl );
			if (!res || tl.GetNum() != 1) {
				fprintf( stderr,
					"%s> Couldn't start Rtr Monoflop for cluster of %ld in DA %ld\n",
					shorttime( hb_time( 0L ) ), currentEnv.currentEaid, entryDa );
				//if (tl) delete tl;
				return (false);
			}
			reTrigger( tl[0], basedata );
			//delete tl;
			break;
	}
	return (True);
}
