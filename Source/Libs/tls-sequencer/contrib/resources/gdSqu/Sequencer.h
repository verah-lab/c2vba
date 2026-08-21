/*
 * Sequencer: P a r s e r  H e a d e r 
 *
 * Generated from: v:\rn_mL-8-2\linkVobW\vmis\src\gdSqu\seq.g
 *
 * Terence Parr, Russell Quong, Will Cohen, and Hank Dietz: 1989-1999
 * Parr Research Corporation
 * with Purdue University Electrical Engineering
 * with AHPCRC, University of Minnesota
 * ANTLR Version 1.33MR22
 */

#ifndef Sequencer_h
#define Sequencer_h

#ifndef ANTLR_VERSION
#define ANTLR_VERSION 13322
#endif

#include "AParser.h"


#ifdef WIN32
#pragma warning ( disable : 4102 )
#endif
#include <hbtimewrap.h>
#include "SeqValue.h"
#include "SeqOp.h"
#include "StackTmpl.h"
#include <rn_strtoul.h>
#include <strtonum.h>
#include "ParseIntern.h"
#include <hbString.h>
#include <db2c.h>
#include <nkomm.h>
#include <stdarg.h>

extern ValueSeqInternal internalKnotenNummer;
extern ValueSeqInternal internalDeNummer;
extern ValueSeqInternal internalEaid;
extern ValueSeqInternal internalEaindex;
extern ValueSeqInternal internalAktuelleZeit;
extern ValueSeqInternal internalAktuelleDa;
extern ValueSeqInternal internalAutosendZeit;
extern ValueSeqInternal internalAusloesezeitpunkt;
extern ValueSeqInternal internalErsterAusloesezeitpunkt;
extern unsigned short pDbgLevel;
extern bool catchDataType( long );
extern bool catchTriggerDataType( long, TransOp* );
extern unsigned long getNumEa( long da );
class Sequencer : public ANTLRParser {
public:
	static  const ANTLRChar *tokenName(int tk);
	enum { SET_SIZE = 224 };
protected:
	static  const ANTLRChar *_token_tbl[];
private:

private:
TransOpSwitch *currentSwitch;
Stack<TransOpSwitch*> switchStack;

		Context *currentContext;
Context *globalContext;
Stack<Context*> contextStack;

		TransOpBreakableBlock *currentBlock;
Stack<TransOpBreakableBlock*> blockStack;

		TransOpBreakableBlock *currentBreakableBlock;
Stack<TransOpBreakableBlock*> breakableBlockStack;

		TransOpCLoop *currentContinueableBlock;
Stack<TransOpCLoop*> continueableBlockStack;

		CommonInfo *currentInputDatatype;
public:
bool parseError;
bool parseWarns;
void init() {
	ANTLRParser::init();
	
			//fprintf( stderr, "Sequencer Parser inited\n" );
	globalContext = currentContext = new Context( 0 ); //global context
	if (!currentContext->insert( "KnotenNummer", &internalKnotenNummer )) {
		panic ("Can't internally declare KnotenNummer\n" );
	}
	if (!currentContext->insert( "DeNummer", &internalDeNummer )) {
		panic ("Can't internally declare DeNummer\n" );
	}
	if (!currentContext->insert( "Eaid", &internalEaid )) {
		panic ("Can't internally declare Eaid\n" );
	}
	if (!currentContext->insert( "EaIndex", &internalEaindex )) {
		panic ("Can't internally declare EaIndex\n" );
	}
	if (!currentContext->insert( "AktuelleZeit", &internalAktuelleZeit )) {
		panic ("Can't internally declare AktuelleZeit\n" );
	}
	if (!currentContext->insert( "Eingangsdatenart", &internalAktuelleDa )) {
		panic ("Can't internally declare Eingangsdatenart\n" );
	}
	if (!currentContext->insert( "AutosendZeit", &internalAutosendZeit )) {
		panic ("Can't internally declare AutosendZeit\n" );
	}
	currentInputDatatype = 0;
	currentSwitch = 0;
	currentBreakableBlock = 0;
	currentContinueableBlock = 0;
	currentBlock = 0;
	parseError = false;
	parseWarns = false;
}
void ParseError( int line, char *fmt, ... );
void ParseWarn( int line, char *fmt, ... );
static Value *badValue(bool &isArray)
{
	extern int cntBadValue;
	char tmp[40]; isArray = false;
	ValueLocVar *res = new ValueLocVar;
	sprintf( tmp, "badValue%d", cntBadValue++ );
	res->name = strdup( tmp );
	return (res);
}
protected:
	static SetWordType setwd1[224];
	static SetWordType err1[32];
	static SetWordType err2[32];
	static SetWordType setwd2[224];
	static SetWordType err3[32];
	static SetWordType err4[32];
	static SetWordType err5[32];
	static SetWordType err6[32];
	static SetWordType setwd3[224];
	static SetWordType err7[32];
	static SetWordType setwd4[224];
	static SetWordType setwd5[224];
	static SetWordType setwd6[224];
	static SetWordType setwd7[224];
	static SetWordType setwd8[224];
	static SetWordType setwd9[224];
	static SetWordType err8[32];
	static SetWordType err9[32];
	static SetWordType setwd10[224];
	static SetWordType err10[32];
	static SetWordType err11[32];
	static SetWordType setwd11[224];
	static SetWordType err12[32];
	static SetWordType err13[32];
	static SetWordType setwd12[224];
	static SetWordType err14[32];
	static SetWordType err15[32];
	static SetWordType err16[32];
	static SetWordType err17[32];
	static SetWordType err18[32];
	static SetWordType err19[32];
	static SetWordType setwd13[224];
	static SetWordType err20[32];
	static SetWordType err21[32];
	static SetWordType err22[32];
	static SetWordType err23[32];
	static SetWordType err24[32];
	static SetWordType setwd14[224];
	static SetWordType err25[32];
	static SetWordType err26[32];
	static SetWordType err27[32];
	static SetWordType err28[32];
	static SetWordType err29[32];
	static SetWordType setwd15[224];
	static SetWordType err30[32];
	static SetWordType err31[32];
	static SetWordType err32[32];
	static SetWordType err33[32];
	static SetWordType err34[32];
	static SetWordType err35[32];
	static SetWordType err36[32];
	static SetWordType err37[32];
	static SetWordType err38[32];
	static SetWordType setwd16[224];
	static SetWordType err39[32];
	static SetWordType err40[32];
	static SetWordType err41[32];
	static SetWordType setwd17[224];
	static SetWordType err42[32];
	static SetWordType err43[32];
	static SetWordType err44[32];
	static SetWordType err45[32];
	static SetWordType setwd18[224];
	static SetWordType err46[32];
	static SetWordType err47[32];
	static SetWordType err48[32];
	static SetWordType err49[32];
	static SetWordType setwd19[224];
	static SetWordType setwd20[224];
private:
	void zzdflthandlers( int _signal, int *_retsignal );

public:

struct _rv11 {
	BinExpr *res;
	int line ;
};

struct _rv12 {
	BinExpr *res;
	int line ;
};

struct _rv13 {
	BinExpr *res;
	int line ;
};

struct _rv14 {
	BinExpr *res;
	int line ;
};

struct _rv15 {
	BinExpr *res;
	int line ;
};

struct _rv16 {
	BinExpr *res;
	int line ;
};

struct _rv17 {
	BinExpr *res;
	int line ;
};

struct _rv18 {
	BinExpr *res;
	int line ;
};

struct _rv19 {
	BinExpr *res;
	int line ;
};

struct _rv20 {
	BinExpr *res;
	int line ;
};

struct _rv21 {
	UnExpr *res;
	int line ;
};

struct _rv22 {
	Value *res;
	bool isArray ;
};

struct _rv23 {
	Value *res;
	bool isArray ;
};

struct _rv24 {
	Value *res;
	bool isArray ;
};

struct _rv25 {
	Value *res;
	bool isArray ;
};

struct _rv26 {
	Value *res;
	bool isArray ;
};

struct _rv27 {
	Value *res;
	bool isArray ;
};

struct _rv28 {
	Value *res;
	bool isArray ;
};

struct _rv29 {
	Value *res;
	bool isArray ;
};

struct _rv30 {
	Value *res;
	bool isArray ;
};

struct _rv31 {
	Value *res;
	bool isArray ;
};

struct _rv32 {
	Value *res;
	bool isArray ;
};

struct _rv33 {
	Value *res;
	bool isArray ;
};

struct _rv34 {
	Value *res;
	bool isArray ;
};

struct _rv35 {
	Value *res;
	bool isArray ;
};

struct _rv36 {
	Value *res;
	bool isArray ;
};

struct _rv37 {
	Value *res;
	bool isArray ;
};

struct _rv38 {
	Value *res;
	bool isArray ;
};

struct _rv39 {
	Value *res;
	bool isArray ;
};

struct _rv40 {
	Value *res;
	bool isArray ;
};

struct _rv42 {
	Value *res;
	bool isArray ;
};

struct _rv44 {
	Value *res;
	bool isArray ;
};

struct _rv58 {
	Value *res;
	bool isArray ;
};

struct _rv59 {
	CommonInfo *res;
	int line ;
};

struct _rv63 {
	Value *res;
	bool isArray ;
};

struct _rv65 {
	CommonInfo *res;
	int line ;
};

struct _rv67 {
	Value *res;
	bool isArray ;
};
	Sequencer(ANTLRTokenBuffer *input);
	void pushContext(void);
	void popContext(void);
	void pushSwitch(void);
	void popSwitch(void);
	void pushBreakableBlock( TransOpBreakableBlock *newBreakableBlock );
	void popBreakableBlock(void);
	void pushContinueableBlock( TransOpCLoop *newContinueableBlock );
	void popContinueableBlock(void);
	void pushBlock( TransOpBreakableBlock *newBreakableBlock );
	void popBlock(void);
	struct _rv11 bitor_operator(void);
	struct _rv12 bitexor_operator(void);
	struct _rv13 bitand_operator(void);
	struct _rv14 and_operator(void);
	struct _rv15 or_operator(void);
	struct _rv16 equality_operator(void);
	struct _rv17 relational_operator(void);
	struct _rv18 shift_operator(void);
	struct _rv19 multiplicative_operator(void);
	struct _rv20 additive_operator(void);
	struct _rv21 unary_operator(void);
	struct _rv22 expression(void);
	struct _rv23 assignment_expression(void);
	struct _rv24 conditional_expression(void);
	struct _rv25 constant_expression(void);
	struct _rv26 cast_expression(void);
	struct _rv27 postfix_expression(void);
	struct _rv28 primary_expression(void);
	struct _rv29 logical_or_expression(void);
	struct _rv30 logical_and_expression(void);
	struct _rv31 inclusive_or_expression(void);
	struct _rv32 exclusive_or_expression(void);
	struct _rv33 and_expression(void);
	struct _rv34 equality_expression(void);
	struct _rv35 relational_expression(void);
	struct _rv36 shift_expression(void);
	struct _rv37 additive_expression(void);
	struct _rv38 multiplicative_expression(void);
	struct _rv39 unary_expression(void);
	struct _rv40 argument_expression_list(void);
	 unsigned long   constant(void);
	struct _rv42 primary_value(void);
	void checkAssignment( ValueAssign *asgn, bool &lhsAr, bool rhsAr, int line, Value *&res );
	struct _rv44 atomare_spalte(void);
	void seq(void);
	void eingang_oder_variable(void);
	void anweisungsliste(void);
	 TransOp *  anweisung(void);
	void lokale_variable_deklarieren(void);
	void globale_variable_deklarieren(void);
	 TransOp *  fehlernachricht_oder_syscmd(void);
	 char *  string(void);
	 ValueList *  opt_arglist(void);
	 ValueList *  arglist(void);
	 TransOpMonoflop *  monoflop_decl(void);
	 int   monofloptype(void);
	void sequenz_festlegung(void);
	struct _rv58 linke_seite(void);
	struct _rv59 datenartelement(void);
	void atomicEa( CommonInfo *inf, bool realExpl );
	void nonAtomicEa( CommonInfo *inf );
	void deepenNonAtomic( CommonInfo *inf );
	struct _rv63 spalte( CommonInfo *infIn, bool have_da );
	 Value *  opt_array_elem(void);
	struct _rv65 datenart( CommonInfo *resIn );
	void identifier( hbString& res, int& line );
	struct _rv67 variable(void);
	 TransOp *  zusammengesetzte_anweisung(void);
	 TransOp *  schleife(void);
	 TransOpLoop *  loop_type(void);
	 TransOp *  schleife2(void);
	 TransOp *  if_else(void);
	 TransOp *  switch_anweisung(void);
	void switchanweisungen(void);
	void caseliste(void);
	void case_anweisung(void);
	void default_anweisung(void);
	 Value *  spezial_funktion(void);
	 unsigned long   konstante(void);
};

#endif /* Sequencer_h */
