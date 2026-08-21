#include <hbtimewrap.h>
#include "SeqValue.h"
#include <Property.h>
#include "CommonInfo.h"
#include <nipd.h>
#include <nkomm.h>
#include "IdList.h"
#include "CurEnv.h"

extern CurrentEnv currentEnv;
//extern long currentEaid;
//extern long currentDa;
//extern long currentEaIndx;
//extern short currentBlock;
//extern unsigned char *currentData;
extern TypKommRoot *OSI7Shm;

extern unsigned char *getBase(long);
extern void setWritten( long DA, long indx );


extern FILE *dbg;

#define CLFUN(type,cl,code) type cl :: code
#define CLFUN2(type,cl,code,Value) type cl :: code ( #Value, 1 );
#define SPECIES Species() const { return ((unsigned long)&SpeciesDum); }
#define CLSPECIES ClSpecies() { return ((unsigned long)&SpeciesDum); }
#define SPECIESDUM SpeciesDum = new Property
#define GETPROPBASIC  GetProp () const { return (*SpeciesDum); }
#define GETPROPINH(baseclass) GetProp () const \
{ Property t(*SpeciesDum); Property b(baseclass::GetProp()); b.Add(t); return b; }

CLFUN( Property, 			InternValueRO,				GETPROPINH(InternValue) )
CLFUN( Property, 			InternValueRW,				GETPROPINH(InternValue) )
CLFUN( Property, 			ValueAssign,				GETPROPINH(BinExpr) )
CLFUN( Property, 			ValueExists,				GETPROPINH(UnExpr) )
CLFUN( Property, 			ValueLocalArray,			GETPROPBASIC)
CLFUN( Property, 			ValueLocalArrayIndex,	GETPROPBASIC)
CLFUN( Property, 			ValueRdsLoc,				GETPROPINH(BinExpr) )
CLFUN( unsigned long,	InternValue,				SPECIES )
CLFUN( unsigned long,	InternValueRO,				SPECIES )
CLFUN( unsigned long,	InternValueRW,				SPECIES )
CLFUN( unsigned long, 	ValueAssign,				SPECIES )
CLFUN( unsigned long, 	ValueExists,				SPECIES )
CLFUN( unsigned long, 	ValueLocalArray,			SPECIES )
CLFUN( unsigned long, 	ValueLocalArrayIndex,	SPECIES )
CLFUN( unsigned long, 	ValueRdsLoc,				SPECIES )
CLFUN( unsigned long, 	ValueSeqCommon,			SPECIES )
CLFUN2( Property *, 		InternValue,				SPECIESDUM, Val_Intern )
CLFUN2( Property *, 		InternValueRO,				SPECIESDUM, Val_RO )
CLFUN2( Property *, 		InternValueRW,				SPECIESDUM, Val_RW )
CLFUN2( Property *, 		ValueAssign,				SPECIESDUM, Val_Assign )
CLFUN2( Property *, 		ValueExists,				SPECIESDUM, Val_Exists )
CLFUN2( Property *, 		ValueLocalArray,			SPECIESDUM, Val_LocArr )
CLFUN2( Property *, 		ValueLocalArrayIndex,	SPECIESDUM, Val_LocArrIndex )
CLFUN2( Property *, 		ValueRdsLoc,				SPECIESDUM, Val_RdsLoc )
CLFUN2( Property *, 		ValueSeqCommon,			SPECIESDUM, Val_SeqCommon )

ValueSeqCommon::~ValueSeqCommon() 
{
	if (realValue) delete realValue;
	if (info) delete info;
}

Property ValueSeqCommon::GetProp () const
{
	Property t(*SpeciesDum);
	Property b(realValue->GetProp());
	t.Add (b);
	return t;
}

bool execError = false;

bool seekSetup(
	CommonInfo		*info,					// description for target and start of search
	bool 				mayHaveMultiValues,	// are searches allowed, that possibly result in more than one ea
	unsigned char	*base,					// basic data to operate on

	bool 				&index_valid,			// current index of ea valid?
	long 				&fromDa,					// search starts from this da
	long 				&fromEaId,				// search starts from this eaid
	int 				&range					// requested range to search for
) {
	index_valid = true;
	fromDa = currentEnv.currentDa;
	if (info->dk->DataId != fromDa) {
		fromDa = info->dk->DataId;
		index_valid = false;
	}
	if (info->explInDa) {
		fromDa = info->theExplicitDa->dk->DataId;
		index_valid = false;
	}
	
	fromEaId = currentEnv.currentEaid;
	if (info->explicitEaid) {
		fromEaId = info->theExplicitEaid->GetVal( base );
		index_valid = false;
	}

	range = 0;
	switch (info->range) {
		case rng_ea: 			
			range = GUELTIG_DE; 									
			break;
		case rng_de2cluster:	
			range = GUELTIG_NORMALE_DES_AN_CLUSTER_DE;	
			break;
		case rng_node: 		
			if (!mayHaveMultiValues) {
				fprintf( stderr,
					"%s> Shouldn't happen: Multivalued range for get data."
					" Affected range: node\n",
					shorttime( hb_time( 0L ) ) );
				execError = true;
				return false;
			}
			range = GUELTIG_KNOTEN; 							
			break;
		case rng_cluster2de:	
			if (!mayHaveMultiValues) {
				fprintf( stderr,
					"%s> Shouldn't happen: Multivalued range for get data."
					" Affected range: DEs of cluster\n",
					shorttime( hb_time( 0L ) ) );
				execError = true;
				return false;
			}
			range = GUELTIG_CLUSTER_DE_AN_NORMALE_DES;	
			break;
	}
	return true;
}

long ValueSeqCommon::GetVal (unsigned char *base)
{
	//dump(); fflush( dbg );
	long res = 0;
	if (info->old) {
		bool index_valid = false;
		long dataTypeTest = 0;
		long testEaid = 0;
		int  destspec = 0;
		if (!seekSetup(
					info,
					false,
					base,
					index_valid,
					dataTypeTest,
					testEaid,
					destspec )){
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			fprintf( stderr, "in %s\n", __PRETTY_FUNCTION__ );
			return (0);
		}
		
		IdList tl;
		bool bres = GetIdList (	testEaid,
											currentEnv.currentEaIndx,
											destspec,
											dataTypeTest,
											info->dk->DataId,
											index_valid,
											EAINDX_GET,
											tl );
		
		if(!bres || tl.GetNum() != 1) {
			fprintf( stderr, "%s> No/too many items found to get data from in %s.\n",
				shorttime( hb_time ( 0L ) ), info->dk->DkName );
			fprintf( stderr, "Upon entry of DA %ld starting with Eaid %ld(%ld)\n",
				currentEnv.currentDa, testEaid, currentEnv.currentEaid );
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			execError = true;
			//if (tl) delete tl;
			return (0);
		}
		long indx = tl[0];
		//delete tl;

		unsigned char *dataBaseOfDA = getBase( info->dk->DataId );
		if (!dataBaseOfDA) {
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			execError = true;
			return (0);
		}
		res = realValue->GetVal( dataBaseOfDA + indx * info->dk->DkSize );
		//fprintf( stderr, "idx %ld -> Val = %ld(0x%lx) ee %d\n", indx, res, res, execError );
		//realValue->dump(); fprintf( stdout, "\n" ); fflush( stdout );
		//fprintf( stderr, "%ld(%p) <- DA %ld Idx %ld\n", res, dataBaseOfDA + indx * info->dk->DkSize, info->dk->DataId, indx );
	} else {
		res = realValue->GetVal( currentEnv.currentData );
		//fprintf( stderr, "act id %ld -> Val = %ld(0x%lx) ee %d\n", currentEnv.currentEaid, res, res, execError );
	}
	//dump(); fprintf( stdout, "Val = %ld(0x%lx)\n", res, res );
	return res;
}

long ValueSeqCommon::PutVal (unsigned char *base, long val)
{
	long res = val;

	if (info->old) {
		bool index_valid = false;
		long dataTypeTest = 0;
		long testEaid = 0;
		int  destspec = 0;
		if (!seekSetup(
					info,
					true,
					base,
					index_valid,
					dataTypeTest,
					testEaid,
					destspec )){
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			fprintf( stderr, "in %s\n", __PRETTY_FUNCTION__ );
			return (val);
		}
	
		IdList tl;
		bool res = GetIdList (	testEaid,
								currentEnv.currentEaIndx,
								destspec,
								dataTypeTest,
								info->dk->DataId,
								index_valid,
								EAINDX_GET,
								tl );
		
		if(!res || !tl.GetNum()) {
			fprintf( stderr, "%s> No target found to write to. Trying to assign to datatype %s.\n",
				shorttime( hb_time ( 0L ) ), info->dk->DkName );
			fprintf( stderr, "Upon entry of DA %ld starting with Eaid %ld(%ld)\n",
				currentEnv.currentDa, testEaid, currentEnv.currentEaid );
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			execError = true;
			//if (tl) delete tl;
			return (0);
		}
	
		unsigned char *dataBaseOfDA = getBase( info->dk->DataId );
	
		register unsigned long i;
		for ( i = 0; i < tl.GetNum(); i++ ) {
			realValue->PutVal( dataBaseOfDA + tl[i] * info->dk->DkSize, val );
			setWritten( info->dk->DataId, tl[i] );
		}
		//delete tl;
	} else {
		realValue->PutVal( currentEnv.currentData, val );
		setWritten( info->dk->DataId, currentEnv.currentEaIndx );
	}
	return res;
}

Bool ValueSeqCommon::APutVal (unsigned char *base, unsigned char *data, unsigned size)
{
	if (atomic) {
		fprintf( stderr, "%s> Internal error. %s[%d] %s\n",
			shorttime( hb_time( 0L ) ), __FILE__, __LINE__, __PRETTY_FUNCTION__ );
		return (False);
	}
	if (info->old) {
		bool index_valid = false;
		long dataTypeTest = 0;
		long testEaid = 0;
		int  destspec = 0;
		if (!seekSetup(
					info,
					true,
					base,
					index_valid,
					dataTypeTest,
					testEaid,
					destspec )){
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			fprintf( stderr, "in %s\n", __PRETTY_FUNCTION__ );
			return (0);
		}
	
		IdList tl;
		bool res = GetIdList (	testEaid,
											currentEnv.currentEaIndx,
											destspec,
											dataTypeTest,
											info->dk->DataId,
											index_valid,
											EAINDX_GET,
											tl );
		
		if(!res || !tl.GetNum()) {
			fprintf( stderr, "%s> No target found to write to. Trying to assign to datatype %s.\n",
				shorttime( hb_time ( 0L ) ), info->dk->DkName );
			fprintf( stderr, "Upon entry of DA %ld starting with Eaid %ld(%ld)\n",
				currentEnv.currentDa, testEaid, currentEnv.currentEaid );
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			execError = true;
			//if (tl) delete tl;
			return (0);
		}
	
		unsigned char *dataBaseOfDA = getBase( info->dk->DataId );
	
		register unsigned long i;
		for ( i = 0; i < tl.GetNum(); i++ ) {
			if (!((ZdfColumnArray*)realValue)->APutVal( dataBaseOfDA + tl[i] * info->dk->DkSize, data, size))
			{	execError = true; /*delete tl;*/ return (False); }
			setWritten( info->dk->DataId, tl[i] );
		}
		//delete tl;
	} else {
		if (!((ZdfColumnArray*)realValue)->APutVal( currentEnv.currentData, data, size))
		{	execError = true; return (False); }
		setWritten( info->dk->DataId, currentEnv.currentEaIndx );
	}
	return True;
}

unsigned char *ValueSeqCommon::GetAdr (unsigned char *base)
{
	unsigned char *res = 0;
	if (info->old) {
		bool index_valid = false;
		long dataTypeTest = 0;
		long testEaid = 0;
		int  destspec = 0;
		if (!seekSetup(
					info,
					false,
					base,
					index_valid,
					dataTypeTest,
					testEaid,
					destspec )){
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			fprintf( stderr, "in %s\n", __PRETTY_FUNCTION__ );
			return (0);
		}
	
		IdList tl;
		bool bres = GetIdList (	testEaid,
											currentEnv.currentEaIndx,
											destspec,
											dataTypeTest,
											info->dk->DataId,
											index_valid,
											EAINDX_GET,
											tl );
		
		if(!bres || tl.GetNum() != 1) {
			fprintf( stderr, "%s> No/too many items found to get data from in %s.\n",
				shorttime( hb_time ( 0L ) ), info->dk->DkName );
			fprintf( stderr, "Upon entry of DA %ld starting with Eaid %ld(%ld)\n",
				currentEnv.currentDa, testEaid, currentEnv.currentEaid );
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			execError = true;
			//if (tl) delete tl;
			return (0);
		}
		long indx = tl[0];
		//delete tl;

		unsigned char *dataBaseOfDA = getBase( info->dk->DataId );
		if (!dataBaseOfDA) {
			FILE *bak = dbg;
			dbg = stderr;
			dump();
			dbg = bak;
			execError = true;
			return (0);
		}
		res = realValue->GetAdr( dataBaseOfDA + indx * info->dk->DkSize );
	} else {
		res = realValue->GetAdr( currentEnv.currentData );
	}
//dump(); fprintf( stdout, "Val = %ld(0x%lx)\n", res, res );
	return res;
}

int ValueSeqCommon::operator == (const ValueCarrier &oth) //const
{
	if (Species() != oth.Species()) return (0);
	ValueSeqCommon &other = (ValueSeqCommon&) oth;
	if (info->dk != other.info->dk) return (0);
	if (!(info->theExplicitEaid == other.info->theExplicitEaid)) return (0);
	if (info->range != other.info->range) return (0);
	if (info->old != other.info->old) return (0);
	if (info->explicitEaid != other.info->explicitEaid) return (0);
	return (realValue == other.realValue);
}

void ValueSeqCommon::dump()
{
	char tmp[256];
	tmp[0] = 0;
	fprintf( dbg, "VCCom ( %s%s ", info->dk->DkName, (info->old)?"-Old":"-New" ); fflush( dbg );
	if (info->explicitEaid) {
		fprintf( dbg, "-EA(" ); fflush( dbg );
		info->theExplicitEaid->dump(); fflush( dbg );
		fprintf( dbg, ")" ); fflush( dbg );
	}
	if (info->explInDa) {
		fprintf( dbg, "( DA %ld )", info->theExplicitDa->dk->DataId ); fflush( dbg );
	}
	fprintf( dbg, " " ); fflush( dbg );
	realValue->dump(); fflush( dbg );
	fprintf( dbg, ")\n" ); fflush( dbg );
//abort ();
}

bool easExist( ValueCarrier *operand, unsigned char *base )
{
	//fprintf( dbg, "%s of %p\n", __PRETTY_FUNCTION__, operand ); fflush( dbg );
	//operand->dump();
	Property p = operand->GetProp();
	//p.Dump();
	if (p.Car().Includes( "Val_CanGetAdr" ))
		return (true); //local variable
	// Zdf-Column
	if (!p.Car().Includes( "Val_SeqCommon" )) {
		fprintf( stderr, "Shouldn't happen: Illegal VarType. %s[%d] %s\n",
			__FILE__, __LINE__, __PRETTY_FUNCTION__ );
		FILE *bak = dbg;
		dbg = stderr;
		operand->dump();
		dbg = bak;
		return (false);
	}
	register ValueSeqCommon *vs = (ValueSeqCommon*) operand;

	bool index_valid = false;
	long dataTypeTest = 0;
	long testEaid = 0;
	int  destspec = 0;
	if (!seekSetup(
				vs->info,
				true,
				base,
				index_valid,
				dataTypeTest,
				testEaid,
				destspec )){
		fprintf( stderr, "in %s\n", __PRETTY_FUNCTION__ );
		return (0);
	}

	IdList tl;
	bool res = GetIdList (	testEaid,
										currentEnv.currentEaIndx,
										destspec,
										dataTypeTest,
										vs->info->dk->DataId,
										index_valid,
										EA_TEST,
										tl );

	if (res) return (true);
	return (false);
}

long ValueExists::GetVal(unsigned char *base)
{
	if (easExist( operand, base )) return (1);
	return (0);
}

long ValueAssign::GetVal(unsigned char *base)
{
	if (atomic) {
		long res = RightOperand->GetVal( base );
		LeftOperand->PutVal( base, res );
		return (res);
	} else {
		ZdfColumnArray *lhs = ((ZdfColumnArray *)((ValueSeqCommon*)LeftOperand)->realValue);
		unsigned short col_dim = lhs->col_dim;
		//fprintf( stderr, "%s col_dim %hu\n", __PRETTY_FUNCTION__, col_dim );
		ZdfColumn *abase = lhs->array_base;
		if(!execError) {
			if (!elemByElem) {
				unsigned char *data = (unsigned char *) RightOperand->GetAdr( base );
				((ValueSeqCommon*)LeftOperand)->APutVal(	base,
																		data,
																		col_dim * abase->unit () );
			} else {
				//fprintf( stderr, "%s elemByElem\n", __PRETTY_FUNCTION__ );
				//fprintf( stderr, "%s col_dim %hu\n", __PRETTY_FUNCTION__, col_dim );
				ZdfColumnArray *rhs = ((ZdfColumnArray *)((ValueSeqCommon*)RightOperand)->realValue);
				ValueSeqCommon vslhs, vsrhs;
				ValueLocVar indx; indx.name = strdup( "index" );

				ZdfColumnArrayElem lhsael;
				lhsael.index = &indx;
				lhsael.col_dim = col_dim;
				lhsael.col_offset = lhs->col_offset;
				lhsael.array_base = abase;
				vslhs.realValue = &lhsael;
				vslhs.info = ((ValueSeqCommon*)LeftOperand)->info;

				ZdfColumnArrayElem rhsael;
				rhsael.index = &indx;
				rhsael.col_dim = col_dim;
				rhsael.col_offset = rhs->col_offset;
				rhsael.array_base = rhs->array_base;
				vsrhs.realValue = &rhsael;
				vsrhs.info = ((ValueSeqCommon*)RightOperand)->info;
				unsigned short i;
				for ( i = 0; i < col_dim; i++ ) {
					indx.value = i;
					vslhs.PutVal( base, vsrhs.GetVal( base ) );
				}
				lhsael.index = 0;
				lhsael.col_dim = 0;
				lhsael.col_offset = 0;
				lhsael.array_base = 0;
				vslhs.realValue = 0;
				vslhs.info = 0;

				rhsael.index = 0;
				rhsael.col_dim = 0;
				rhsael.col_offset = 0;
				rhsael.array_base = 0;
				vsrhs.realValue = 0;
				vsrhs.info = 0;
			}
		}
		return (0);
	}
}

long Fun_RdsLoc   (long left, long right)
{
	return (left*256 + right);
}

long Fun_mayNotBeUsed   (long , long )
{
	fprintf( stderr, "%s should not be used\n", __PRETTY_FUNCTION__ );
	abort();
	return (0);
}

long Fun_mayNotBeUsed   (long )
{
	fprintf( stderr, "%s should not be used\n", __PRETTY_FUNCTION__ );
	abort();
	return (0);
}

void ValueExists::dump() { fprintf ( dbg, " Exists(" /*)*/  ); UnExpr::dump (); fprintf (/*(*/ dbg, ")\n"); }

void ValueRdsLoc::dump() { fprintf ( dbg, " RdsLoc(" /*)*/  ); BinExpr::dump (); fprintf (/*(*/ dbg, ")\n"); }
void ValueAssign::dump() { fprintf ( dbg, " :=(" /*)*/  );     BinExpr::dump (); fprintf (/*(*/ dbg, ")\n"); }

#define RETFUNCUN UnOp () { return
#define RETFUNCBI BinOp () { return
#define CLFUN3(type,cl,code,Value) type cl :: code ( Value ); }


CLFUN3 (UnOpFunc,  ValueExists,             RETFUNCUN, Fun_mayNotBeUsed     )

CLFUN3 (BinOpFunc, ValueRdsLoc,             RETFUNCBI, Fun_RdsLoc   )
CLFUN3 (BinOpFunc, ValueAssign,             RETFUNCBI, Fun_mayNotBeUsed     )

//-----------------------------------------------------------------------------
// ValueLocalArray
//-----------------------------------------------------------------------------

void ValueLocalArray::dump()
{
	fprintf ( dbg, " LocArray %s (Dim %lu)", name, size  );
	fprintf ( dbg, "\n");
}

int ValueLocalArray::operator == (const ValueCarrier& other)
{
	if ( Species () != other.Species () ) return (0);
	ValueLocalArray &oth = (ValueLocalArray &) other;
	if (strcmp( name, oth.name )) return (0);
	if (size != oth.size) return (0);
	return (1);
}

long ValueLocalArray::GetVal (unsigned char *)
{
	fprintf( stderr, "%s: Can't get value from. Is an array.\n", name );
	abort();
	return (0);
}

long ValueLocalArray::PutVal (unsigned char *, long)
{
	fprintf( stderr, "%s: Can't set value to. Is an array.\n", name );
	abort();
	return (0);
}

//-----------------------------------------------------------------------------
// ValueLocalArrayIndex
//-----------------------------------------------------------------------------

void ValueLocalArrayIndex::dump()
{
	fprintf ( dbg, " LocArrayElem (" /*)*/  );
	base->dump();
	fprintf( /*(*/ dbg, "[ " ); indexExpression->dump(); fprintf( dbg, " ]\n)\n" );
}

int ValueLocalArrayIndex::operator == (const ValueCarrier& other)
{
	if ( Species () != other.Species () ) return (0);
	ValueLocalArrayIndex &oth = (ValueLocalArrayIndex &) other;
	if (!(*base == *(oth.base))) return (0);
	if (!(*indexExpression == *(oth.indexExpression))) return (0);
	return (1);
}

long ValueLocalArrayIndex::GetVal (unsigned char *dbase)
{
	long indx = indexExpression->GetVal( dbase );
	if (indx < 0 || (unsigned long) indx >= base->size) {
		FILE *bk = dbg;
		dbg = stderr;
		fprintf( stderr, "Index out of bounds - zero returned):\n" );
		dump();
		dbg = bk;
		return (0);
	}
	//fprintf( stderr, "ValueLocalArrayIndex return %ld from index %ld\n", base->value[indx], indx );
	return (base->value[indx]);
}

long ValueLocalArrayIndex::PutVal (unsigned char *dbase, long val)
{
	long indx = indexExpression->GetVal( dbase );
	if (indx < 0 || (unsigned long) indx >= base->size) {
		FILE *bk = dbg;
		dbg = stderr;
		fprintf( stderr, "Index out of bounds - zero returned):\n" );
		dump();
		dbg = bk;
		return (0);
	}
	//fprintf( stderr, "ValueLocalArrayIndex assign %ld to index %ld\n", val, indx );
	return (base->value[indx] = val);
}

unsigned char *ValueLocalArrayIndex::GetAdr(unsigned char *dbase)
{
	long indx = indexExpression->GetVal( dbase );
	if (indx < 0 || (unsigned long) indx >= base->size) {
		FILE *bk = dbg;
		dbg = stderr;
		fprintf( stderr, "Index out of bounds - zero returned):\n" );
		dump();
		dbg = bk;
		return (0);
	}
	return ((unsigned char *) (base->value + indx));
}

//-----------------------------------------------------------------------------
// InternValue
//-----------------------------------------------------------------------------

Property InternValue::GetProp () const
{
	Property b (ValueCanGetAdr::GetProp ());
	b.Add (Property("Val_Long"));
	Property t (*SpeciesDum);
	b.Add (t);
	return (b);
}

int InternValue::operator == (const ValueCarrier& other)
{
	if ( Species () != other.Species () ) return (0);
	InternValue &oth = (InternValue &) other;
	if ( VarPtr != oth.VarPtr ) return (0);
	if ( strcmp (name, oth.name) ) return (0);
	if ( ResetVal != oth.ResetVal ) return (0);
	return (1);
}

void InternValue::dump ()
{
	fprintf ( dbg, "%s($%s ", GetProp().Cdr ().Cdr ().Cdr ().GetPropVal (), name ); fflush (dbg);
	fprintf ( dbg, "%ld{rst=%ld;})", *VarPtr, ResetVal ); fflush (dbg);
}

void InternValue::Reset ()
{
	*VarPtr = ResetVal;
}

long InternValue::GetVal (unsigned char *)
{
	return (*VarPtr);
}

long InternValueRO::PutVal (unsigned char *, long)
{
	fprintf ( stderr, "ReadOnly: InternValue %s\n", name );
	abort ();
	return (0);
}

long InternValueRW::PutVal (unsigned char *, long val)
{
	return (*VarPtr=val);
}

// InternValues must be global and not allocated via new
InternValueRO	*InternValueRO::	backup;
InternValueRW	*InternValueRW::	backup;

// die erfintern-werte sollen nicht geloescht werden koennen
InternValueRO::~InternValueRO ()
{
	if (!backup) backup = new InternValueRO( VarPtr, name, ResetVal );
	*backup = *this;
	//hd_string (this, sizeof (*this), stdout);
	//hd_string (backup, sizeof (*backup), stdout);
}

InternValueRW::~InternValueRW ()
{
	//hd_string (this, sizeof (*this), stdout);
	if (!backup) backup = new InternValueRW;
	*backup = *this;
	//hd_string (backup, sizeof (*backup), stdout);
}

void InternValueRO::operator delete (void *mem)
{

	memcpy (mem, backup, sizeof (InternValueRO));
	//hd_string (mem, sizeof (InternValueRO), stdout);
}

void InternValueRW::operator delete (void *mem)
{
	//hd_string (backup, sizeof (InternValueRW), stdout);
	//((InternValueRW*) mem) = *backup;
	memcpy (mem, backup, sizeof (InternValueRW));
	//hd_string (mem, sizeof (InternValueRW), stdout);
}

