/*
 * A n t l r  T r a n s l a t i o n  H e a d e r
 *
 * Terence Parr, Will Cohen, and Hank Dietz: 1989-1999
 * Purdue University Electrical Engineering
 * With AHPCRC, University of Minnesota
 * ANTLR Version 1.33MR22
 *
 *   ANTLR -CC -k 2 -ge -k 4 -mrhoist on -prc on v:\rn_mL-8-2\linkVobW\vmis\src\gdSqu\seq.g
 *
 */

#define ANTLR_VERSION	13322
#include "pcctscfg.h"
#include "pccts_stdio.h"
#include "tokens.h"

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
#include "AParser.h"
#include "Sequencer.h"
#include "DLexerBase.h"
#include "ATokPtr.h"
#ifndef PURIFY
#define PURIFY(r,s) memset((char *) &(r),'\0',(s));
#endif

#include "ValueList.h"
#include "CommonInfo.h"
#include <dkinfshm.h>
char *inputFileName = strdup( "No File" );
extern int lastLine;
#include <Property.h>

#if 0 // ndef WIN32
template class Stack<TransOpSwitch*>;
template class Stack<Context*>;
template class Stack<TransOpBreakableBlock*>;
template class Stack<TransOpCLoop*>;
template class StackElem<TransOpSwitch*>;
template class StackElem<Context*>;
template class StackElem<TransOpBreakableBlock*>;
template class StackElem<TransOpCLoop*>;
#endif

int cntBadValue = 1;

void
Sequencer::pushContext(void)
{
	zzRULE;
	;
	
	Context *lastCont = currentContext;
	contextStack.push( currentContext );
	currentContext = new Context(lastCont );
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x1);
}

void
Sequencer::popContext(void)
{
	zzRULE;
	;
	
	delete currentContext;
	currentContext = contextStack.pop();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x2);
}

void
Sequencer::pushSwitch(void)
{
	zzRULE;
	;
	switchStack.push( currentSwitch ); currentSwitch = new TransOpSwitch;
	pushBlock( currentSwitch );
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x4);
}

void
Sequencer::popSwitch(void)
{
	zzRULE;
	;
	currentSwitch = switchStack.pop();
	popBlock();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x8);
}

void
Sequencer::pushBreakableBlock( TransOpBreakableBlock *newBreakableBlock )
{
	zzRULE;
	;
	
	breakableBlockStack.push( currentBreakableBlock );
	currentBreakableBlock = newBreakableBlock;
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x10);
}

void
Sequencer::popBreakableBlock(void)
{
	zzRULE;
	;
	currentBreakableBlock = breakableBlockStack.pop();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x20);
}

void
Sequencer::pushContinueableBlock( TransOpCLoop *newContinueableBlock )
{
	zzRULE;
	;
	
	continueableBlockStack.push( currentContinueableBlock );
	currentContinueableBlock = newContinueableBlock;
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x40);
}

void
Sequencer::popContinueableBlock(void)
{
	zzRULE;
	;
	currentContinueableBlock = continueableBlockStack.pop();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd1, 0x80);
}

void
Sequencer::pushBlock( TransOpBreakableBlock *newBreakableBlock )
{
	zzRULE;
	;
	blockStack.push( currentBlock ); currentBlock = newBreakableBlock;
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x1);
}

void
Sequencer::popBlock(void)
{
	zzRULE;
	;
	currentBlock = blockStack.pop();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x2);
}

Sequencer::_rv11
Sequencer::bitor_operator(void)
{
	struct _rv11 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv11))
	_retv.res = 0; _retv.line = 0;
	zzmatch(BITWISEOR); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	_retv.res = new BitOr; _retv.line = _t11->getLine();
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x4);
	return _retv;
}

Sequencer::_rv12
Sequencer::bitexor_operator(void)
{
	struct _rv12 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv12))
	_retv.res = 0; _retv.line = 0;
	zzmatch(BITWISEXOR); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	_retv.res = new BitXor; _retv.line = _t11->getLine();
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x8);
	return _retv;
}

Sequencer::_rv13
Sequencer::bitand_operator(void)
{
	struct _rv13 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv13))
	_retv.res = 0; _retv.line = 0;
	zzmatch(AMPERSAND); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	_retv.res = new BitAnd; _retv.line = _t11->getLine();
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x10);
	return _retv;
}

Sequencer::_rv14
Sequencer::and_operator(void)
{
	struct _rv14 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv14))
	_retv.res = 0; _retv.line = 0;
	zzmatch(LOGAND); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	_retv.res = new LogAnd; _retv.line = _t11->getLine();
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x20);
	return _retv;
}

Sequencer::_rv15
Sequencer::or_operator(void)
{
	struct _rv15 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv15))
	_retv.res = 0; _retv.line = 0;
	zzmatch(LOGOR); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	_retv.res = new  LogOr; _retv.line = _t11->getLine();
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x40);
	return _retv;
}

Sequencer::_rv16
Sequencer::equality_operator(void)
{
	struct _rv16 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv16))
	_retv.res = 0; _retv.line = 0;
	if ( (LA(1)==NOTEQUAL) ) {
		zzmatch(NOTEQUAL); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		
		_retv.res = new RelNotEq; _retv.line = _t11->getLine();
 consume();
	}
	else {
		if ( (LA(1)==EQUAL) ) {
			zzmatch(EQUAL); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			_retv.res = new RelEq; _retv.line = _t11->getLine();
 consume();
		}
		else {FAIL(1,err1,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd2, 0x80);
	return _retv;
}

Sequencer::_rv17
Sequencer::relational_operator(void)
{
	struct _rv17 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv17))
	_retv.res = 0; _retv.line = 0;
	if ( (LA(1)==LESSTHAN) ) {
		zzmatch(LESSTHAN); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		
		_retv.res = new RelLT; _retv.line = _t11->getLine();
 consume();
	}
	else {
		if ( (LA(1)==GREATERTHAN) ) {
			zzmatch(GREATERTHAN); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			_retv.res = new RelGT; _retv.line = _t11->getLine();
 consume();
		}
		else {
			if ( (LA(1)==LESSTHANOREQUALTO)
 ) {
				zzmatch(LESSTHANOREQUALTO); _t11 = (ANTLRTokenPtr)LT(1); labase++;
				
				_retv.res = new RelLE; _retv.line = _t11->getLine();
 consume();
			}
			else {
				if ( (LA(1)==GREATERTHANOREQUALTO) ) {
					zzmatch(GREATERTHANOREQUALTO); _t11 = (ANTLRTokenPtr)LT(1); labase++;
					
					_retv.res = new RelGE; _retv.line = _t11->getLine();
 consume();
				}
				else {FAIL(1,err2,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x1);
	return _retv;
}

Sequencer::_rv18
Sequencer::shift_operator(void)
{
	struct _rv18 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv18))
	_retv.res = 0; _retv.line = 0;
	if ( (LA(1)==SHIFTLEFT) ) {
		zzmatch(SHIFTLEFT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		
		_retv.res = new ShiftLeft; _retv.line = _t11->getLine();
 consume();
	}
	else {
		if ( (LA(1)==SHIFTRIGHT) ) {
			zzmatch(SHIFTRIGHT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			_retv.res = new ShiftRight; _retv.line = _t11->getLine();
 consume();
		}
		else {FAIL(1,err3,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x2);
	return _retv;
}

Sequencer::_rv19
Sequencer::multiplicative_operator(void)
{
	struct _rv19 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv19))
	_retv.res = 0; _retv.line = 0;
	if ( (LA(1)==STAR) ) {
		zzmatch(STAR); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		
		_retv.res = new Mult; _retv.line = _t11->getLine();
 consume();
	}
	else {
		if ( (LA(1)==DIVIDE)
 ) {
			zzmatch(DIVIDE); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			_retv.res = new Div; _retv.line = _t11->getLine();
 consume();
		}
		else {
			if ( (LA(1)==MOD) ) {
				zzmatch(MOD); _t11 = (ANTLRTokenPtr)LT(1); labase++;
				
				_retv.res = new Mod; _retv.line = _t11->getLine();
 consume();
			}
			else {FAIL(1,err4,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x4);
	return _retv;
}

Sequencer::_rv20
Sequencer::additive_operator(void)
{
	struct _rv20 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv20))
	_retv.res = 0;_retv.line = 0;
	if ( (LA(1)==PLUS) ) {
		zzmatch(PLUS); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		
		_retv.res = new Plus; _retv.line = _t11->getLine();
 consume();
	}
	else {
		if ( (LA(1)==MINUS) ) {
			zzmatch(MINUS); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			_retv.res = new BinMinus; _retv.line = _t11->getLine();
 consume();
		}
		else {FAIL(1,err5,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x8);
	return _retv;
}

Sequencer::_rv21
Sequencer::unary_operator(void)
{
	struct _rv21 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv21))
	_retv.res = 0; _retv.line = 0;
	if ( (LA(1)==MINUS) ) {
		zzmatch(MINUS); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		
		_retv.res = new UnMinus; _retv.line = _t11->getLine();
 consume();
	}
	else {
		if ( (LA(1)==ONESCOMPLEMENT)
 ) {
			zzmatch(ONESCOMPLEMENT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			_retv.res = new BitNot; _retv.line = _t11->getLine();
 consume();
		}
		else {
			if ( (LA(1)==LOGNOT) ) {
				zzmatch(LOGNOT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
				
				_retv.res = new LogNot; _retv.line = _t11->getLine();
 consume();
			}
			else {FAIL(1,err6,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x10);
	return _retv;
}

Sequencer::_rv22
Sequencer::expression(void)
{
	struct _rv22 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv22))
	{ struct _rv23 _trv; _trv = assignment_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x20);
	return _retv;
}

Sequencer::_rv23
Sequencer::assignment_expression(void)
{
	struct _rv23 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv23))
	{ struct _rv24 _trv; _trv = conditional_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x40);
	return _retv;
}

Sequencer::_rv24
Sequencer::conditional_expression(void)
{
	struct _rv24 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv24))
	{ struct _rv29 _trv; _trv = logical_or_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd3, 0x80);
	return _retv;
}

Sequencer::_rv25
Sequencer::constant_expression(void)
{
	struct _rv25 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv25))
	{ struct _rv24 _trv; _trv = conditional_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd4, 0x1);
	return _retv;
}

Sequencer::_rv26
Sequencer::cast_expression(void)
{
	struct _rv26 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv26))
	{ struct _rv39 _trv; _trv = unary_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd4, 0x2);
	return _retv;
}

Sequencer::_rv27
Sequencer::postfix_expression(void)
{
	struct _rv27 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv27))
	{ struct _rv28 _trv; _trv = primary_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd4, 0x4);
	return _retv;
}

Sequencer::_rv28
Sequencer::primary_expression(void)
{
	struct _rv28 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv28))
	_retv.res = 0; _retv.isArray = false;
	if ( (setwd4[LA(1)]&0x8) ) {
		{ struct _rv42 _trv; _trv = primary_value();

		_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	}
	else {
		if ( (LA(1)==LPARENTHESIS) ) {
			zzmatch(LPARENTHESIS); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
			zzmatch(RPARENTHESIS); labase++;
			 consume();
		}
		else {FAIL(1,err7,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd4, 0x10);
	return _retv;
}

Sequencer::_rv29
Sequencer::logical_or_expression(void)
{
	struct _rv29 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv29))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv30 _trv; _trv = logical_and_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (LA(1)==LOGOR) && (setwd4[LA(2)]&0x20) && 
(setwd4[LA(3)]&0x40) && (setwd4[LA(4)]&0x80) ) {
			{ struct _rv15 _trv; _trv = or_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv30 _trv; _trv = logical_and_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd5, 0x1);
	return _retv;
}

Sequencer::_rv30
Sequencer::logical_and_expression(void)
{
	struct _rv30 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv30))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv31 _trv; _trv = inclusive_or_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (LA(1)==LOGAND) && (setwd5[LA(2)]&0x2) && (setwd5[LA(3)]&0x4) && (setwd5[LA(4)]&0x8) ) {
			{ struct _rv14 _trv; _trv = and_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv31 _trv; _trv = inclusive_or_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd5, 0x10);
	return _retv;
}

Sequencer::_rv31
Sequencer::inclusive_or_expression(void)
{
	struct _rv31 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv31))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv32 _trv; _trv = exclusive_or_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (LA(1)==BITWISEOR) && 
(setwd5[LA(2)]&0x20) && (setwd5[LA(3)]&0x40) && (setwd5[LA(4)]&0x80) ) {
			{ struct _rv11 _trv; _trv = bitor_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv32 _trv; _trv = exclusive_or_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd6, 0x1);
	return _retv;
}

Sequencer::_rv32
Sequencer::exclusive_or_expression(void)
{
	struct _rv32 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv32))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv33 _trv; _trv = and_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (LA(1)==BITWISEXOR) && (setwd6[LA(2)]&0x2) && (setwd6[LA(3)]&0x4) && 
(setwd6[LA(4)]&0x8) ) {
			{ struct _rv12 _trv; _trv = bitexor_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv33 _trv; _trv = and_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd6, 0x10);
	return _retv;
}

Sequencer::_rv33
Sequencer::and_expression(void)
{
	struct _rv33 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv33))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv34 _trv; _trv = equality_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (LA(1)==AMPERSAND) && (setwd6[LA(2)]&0x20) && (setwd6[LA(3)]&0x40) && (setwd6[LA(4)]&0x80) ) {
			{ struct _rv13 _trv; _trv = bitand_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv34 _trv; _trv = equality_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd7, 0x1);
	return _retv;
}

Sequencer::_rv34
Sequencer::equality_expression(void)
{
	struct _rv34 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv34))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv35 _trv; _trv = relational_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (setwd7[LA(1)]&0x2) && (setwd7[LA(2)]&0x4) && 
(setwd7[LA(3)]&0x8) && (setwd7[LA(4)]&0x10) ) {
			{ struct _rv16 _trv; _trv = equality_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv35 _trv; _trv = relational_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd7, 0x20);
	return _retv;
}

Sequencer::_rv35
Sequencer::relational_expression(void)
{
	struct _rv35 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv35))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv36 _trv; _trv = shift_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (setwd7[LA(1)]&0x40) && (setwd7[LA(2)]&0x80) && (setwd8[LA(3)]&0x1) && (setwd8[LA(4)]&0x2) ) {
			{ struct _rv17 _trv; _trv = relational_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv36 _trv; _trv = shift_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd8, 0x4);
	return _retv;
}

Sequencer::_rv36
Sequencer::shift_expression(void)
{
	struct _rv36 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv36))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv37 _trv; _trv = additive_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (setwd8[LA(1)]&0x8) && 
(setwd8[LA(2)]&0x10) && (setwd8[LA(3)]&0x20) && (setwd8[LA(4)]&0x40) ) {
			{ struct _rv18 _trv; _trv = shift_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv37 _trv; _trv = additive_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd8, 0x80);
	return _retv;
}

Sequencer::_rv37
Sequencer::additive_expression(void)
{
	struct _rv37 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv37))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv38 _trv; _trv = multiplicative_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (setwd9[LA(1)]&0x1) && (setwd9[LA(2)]&0x2) && (setwd9[LA(3)]&0x4) && 
(setwd9[LA(4)]&0x8) ) {
			{ struct _rv20 _trv; _trv = additive_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv38 _trv; _trv = multiplicative_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd9, 0x10);
	return _retv;
}

Sequencer::_rv38
Sequencer::multiplicative_expression(void)
{
	struct _rv38 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv38))
	BinExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	{ struct _rv26 _trv; _trv = cast_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	{
		while ( (setwd9[LA(1)]&0x20) && (setwd9[LA(2)]&0x40) && (setwd9[LA(3)]&0x80) && (setwd10[LA(4)]&0x1) ) {
			{ struct _rv19 _trv; _trv = multiplicative_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv26 _trv; _trv = cast_expression();

			rhs = _trv.res; exprAr  = _trv.isArray; }
			
			op->LeftOperand = _retv.res; op->RightOperand = rhs; _retv.res = op;
			if (_retv.isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); _retv.res = badValue( _retv.isArray ); }
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd10, 0x2);
	return _retv;
}

Sequencer::_rv39
Sequencer::unary_expression(void)
{
	struct _rv39 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv39))
	UnExpr *op; Value *rhs; _retv.res = 0; _retv.isArray = false; bool exprAr = false; int line = 0;
	if ( (setwd10[LA(1)]&0x4) ) {
		{ struct _rv27 _trv; _trv = postfix_expression();

		_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	}
	else {
		if ( (setwd10[LA(1)]&0x8)
 ) {
			{ struct _rv21 _trv; _trv = unary_operator();

			op = _trv.res; line  = _trv.line; }
			{ struct _rv26 _trv; _trv = cast_expression();

			_retv.res = _trv.res; exprAr  = _trv.isArray; }
			
			if (exprAr) { ParseError( line, "Can't accecpt array with unary operator" ); _retv.res = badValue( _retv.isArray ); }
			op->operand = _retv.res; _retv.res = op;
		}
		else {FAIL(1,err8,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd10, 0x10);
	return _retv;
}

Sequencer::_rv40
Sequencer::argument_expression_list(void)
{
	struct _rv40 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv40))
	{ struct _rv23 _trv; _trv = assignment_expression();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd10, 0x20);
	return _retv;
}

 unsigned long  
Sequencer::constant(void)
{
	 unsigned long  	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( unsigned long  	))
	_retv = 0;
	if ( (LA(1)==OCTALINT) ) {
		zzmatch(OCTALINT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		
		_retv = rn_strtoul( _t11->getText(),0,0 );
 consume();
	}
	else {
		if ( (LA(1)==DECIMALINT) ) {
			zzmatch(DECIMALINT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			_retv = rn_strtoul( _t11->getText(),0,0 );
 consume();
		}
		else {
			if ( (LA(1)==RDSINT) ) {
				zzmatch(RDSINT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
				
				
				bool ok = true;
				_retv = pa_getRds( _t11->getText(), ok );
				//fprintf( stderr, "%s -> %lu\n", $1->getText(), $res );
				if (!ok) ParseError( _t11->getLine(), "Bad RDS number" );
 consume();
			}
			else {
				if ( (LA(1)==HEXADECIMALINT) ) {
					zzmatch(HEXADECIMALINT); _t11 = (ANTLRTokenPtr)LT(1); labase++;
					
					_retv = rn_strtoul( _t11->getText(),0,0 );
 consume();
				}
				else {
					if ( (LA(1)==CHARACTER)
 ) {
						zzmatch(CHARACTER); _t11 = (ANTLRTokenPtr)LT(1); labase++;
						
						_retv = *(_t11->getText()+1);
 consume();
					}
					else {FAIL(1,err9,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
				}
			}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd10, 0x40);
	return _retv;
}

Sequencer::_rv42
Sequencer::primary_value(void)
{
	struct _rv42 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv42))
	
	_retv.res = 0;
	_retv.isArray = false;
	Value *expr = 0;
	unsigned long cnst = 0;
	ValueAssign *asgn = 0;
	bool rhsAr = false;
	if ( (setwd10[LA(1)]&0x80) ) {
		 cnst  = konstante();

		
		ValueConst *tres = new ValueConst;
		tres->value = cnst;
		_retv.res = tres;
	}
	else {
		if ( (setwd11[LA(1)]&0x1) ) {
			{ struct _rv58 _trv; _trv = linke_seite();

			_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
			{
				ANTLRTokenPtr _t21=NULL;
				if ( (LA(1)==Assign) ) {
					zzmatch(Assign); _t21 = (ANTLRTokenPtr)LT(1); labase++;
					 consume();
					{ struct _rv22 _trv; _trv = expression();

					expr = _trv.res; rhsAr  = _trv.isArray; }
					
					asgn = new ValueAssign;
					asgn->LeftOperand = _retv.res;
					asgn->RightOperand = expr;
					_retv.res = asgn;
					checkAssignment( asgn, _retv.isArray, rhsAr, _t21->getLine(), _retv.res );
					
					if (!parseError) {
						if (_retv.isArray != rhsAr) {
							panic( "Should not get here\n" );
						}
					}
				}
				else {
					if ( (setwd11[LA(1)]&0x2) ) {
					}
					else {FAIL(1,err10,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
				}
			}
		}
		else {
			if ( (setwd11[LA(1)]&0x4)
 ) {
				 _retv.res  = spezial_funktion();

			}
			else {FAIL(1,err11,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd11, 0x8);
	return _retv;
}

void
Sequencer::checkAssignment( ValueAssign *asgn, bool &lhsAr, bool rhsAr, int line, Value *&res )
{
	zzRULE;
	;
	
	//Property lhsprop = asgn->LeftOperand->GetProp();
	//Property rhsprop = asgn->RightOperand->GetProp();
	// wenn die rechte seite ein array ist,
	// muss die linke seite auch ein array sein
	if (lhsAr != rhsAr) {
		ParseError( line, "Array/non_Array assignment mismatch" );
		res = badValue( lhsAr );
	} else {
		if (lhsAr) { // both sides are arrays
			ValueSeqCommon *lhs = (ValueSeqCommon *) asgn->LeftOperand;
			ValueSeqCommon *rhs = (ValueSeqCommon *) asgn->RightOperand;
			ZdfColumnArray *arrLhs = (ZdfColumnArray *) lhs->realValue;
			ZdfColumnArray *arrRhs = (ZdfColumnArray *) rhs->realValue;
			asgn->atomic = false;
			asgn->elemByElem = false;
			if (arrLhs->col_dim != arrRhs->col_dim) {
				ParseError( line, "Dimensions of arrays do not match" );
			}
			if (!(*(arrLhs->array_base) == *(arrRhs->array_base))) {
				asgn->elemByElem = true;
				ParseWarn( line, "Basic types of arrays do not match" );
			}
		} else {
			// both sides are not arrays. thats ok ok in any case
			asgn->atomic = true;
		}
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd11, 0x10);
}

Sequencer::_rv44
Sequencer::atomare_spalte(void)
{
	struct _rv44 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv44))
	_retv.res = 0; _retv.isArray = false;
	{ struct _rv58 _trv; _trv = linke_seite();

	_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd11, 0x20);
	return _retv;
}

void
Sequencer::seq(void)
{
	zzRULE;
	{
		int zzcnt=1;
		do {
			eingang_oder_variable();
		} while ( (setwd11[LA(1)]&0x40) );
	}
	zzmatch(Eof); labase++;
	 consume();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd11, 0x80);
}

void
Sequencer::eingang_oder_variable(void)
{
	zzRULE;
	CommonInfo *inf = 0; int line = 0;
	if ( (LA(1)==Bei) ) {
		zzmatch(Bei); labase++;
		 consume();
		zzmatch(Eingang); labase++;
		 consume();
		zzmatch(Von); labase++;
		 consume();
		{ struct _rv65 _trv; _trv = datenart( 0 );

		currentInputDatatype = _trv.res; line  = _trv.line; }
		
		if (pDbgLevel)
		fprintf( stdout, "Line %5d %s:\n",
		line, currentInputDatatype->dk->DkName );
		if (!currentInputDatatype)
		return;
		pushContext();
		pushBlock(  new TransOpBreakableBlock  );
		{
			if ( (setwd12[LA(1)]&0x1) ) {
				anweisungsliste();
				
				if (pDbgLevel>8)
				currentBlock->dump( "" );
				if (!catchTriggerDataType( currentInputDatatype->dk->DataId, currentBlock )) {
					ParseError( line, "Can't keep trigger DA" );
				}
			}
			else {
				if ( (LA(1)==Sequence) ) {
					sequenz_festlegung();
				}
				else {FAIL(1,err12,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
		popBlock();
		popContext();
	}
	else {
		if ( (LA(1)==Global)
 ) {
			globale_variable_deklarieren();
		}
		else {FAIL(1,err13,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd12, 0x2);
}

void
Sequencer::anweisungsliste(void)
{
	zzRULE;
	TransOp *anw = 0;
	{
		int zzcnt=1;
		do {
			 anw  = anweisung();

			{
				static TransOp *nop = 0;
				if (!nop) {
					nop = new TransOpNop;
				}
				if(!anw) {
					fprintf( stderr, "empty ins\n" );
					/*abort();*/
				}
				else {
					if (!(*nop == *anw))
					currentBlock->Add( anw );
					//fprintf( stderr, "anwl: anw %p\n", anw );
				}
			}
		} while ( (setwd12[LA(1)]&0x4) );
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd12, 0x8);
}

 TransOp * 
Sequencer::anweisung(void)
{
	 TransOp * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL,_t12=NULL;
	PURIFY(_retv,sizeof( TransOp * 	))
	
	_retv = 0;
	Value *expr = 0, *lhs = 0;
	unsigned long cnst = 0;
	TransOpDoAssign *tres = 0;
	bool lhsAr = false, rhsAr = false;
	if ( (setwd12[LA(1)]&0x10) && (LA(2)==IDENTIFIER) && (setwd12[LA(3)]&0x20) && (setwd12[LA(4)]&0x40) ) {
		lokale_variable_deklarieren();
		
		//fprintf( stderr, "At line %d ==>\n", $1->getLine() );
		return (new TransOpNop);
	}
	else {
		if ( (setwd12[LA(1)]&0x80)
 ) {
			 _retv  = fehlernachricht_oder_syscmd();

		}
		else {
			if ( (setwd13[LA(1)]&0x1) ) {
				 _retv  = zusammengesetzte_anweisung();

			}
			else {
				if ( (setwd13[LA(1)]&0x2) ) {
					 _retv  = monoflop_decl();

				}
				else {
					if ( (LA(1)==LCURLYBRACE) ) {
						zzmatch(LCURLYBRACE); _t11 = (ANTLRTokenPtr)LT(1); labase++;
						 consume();
						pushContext();
						pushBlock( new TransOpBreakableBlock );
						anweisungsliste();
						_retv = currentBlock;
						popBlock();
						popContext();
						zzmatch(RCURLYBRACE); labase++;
						 consume();
					}
					else {
						if ( (LA(1)==SeqState) ) {
							zzmatch(SeqState); _t11 = (ANTLRTokenPtr)LT(1); labase++;
							 consume();
							 cnst  = konstante();

							
							//fprintf( stderr, "At line %d ==>\n", $1->getLine() );
							return (new TransOpNop);
						}
						else {
							if ( (setwd13[LA(1)]&0x4) && 
(setwd13[LA(2)]&0x8) && (setwd13[LA(3)]&0x10) && (setwd13[LA(4)]&0x20) ) {
								{ struct _rv58 _trv; _trv = linke_seite();

								lhs = _trv.res; lhsAr  = _trv.isArray; }
								zzmatch(Assign); _t12 = (ANTLRTokenPtr)LT(1); labase++;
								 consume();
								{ struct _rv22 _trv; _trv = expression();

								expr = _trv.res; rhsAr  = _trv.isArray; }
								
								//fprintf( stderr, "At line %d :=\n", $2->getLine() );
								tres = new TransOpDoAssign;
								tres->value = new ValueAssign;
								tres->value->LeftOperand = lhs;
								tres->value->RightOperand = expr;
								_retv = tres;
								checkAssignment( tres->value, lhsAr, rhsAr, _t12->getLine(), ((Value*&)tres->value) );
							}
							else {
								if ( (LA(1)==Break) ) {
									zzmatch(Break); _t11 = (ANTLRTokenPtr)LT(1); labase++;
									
									
									//fprintf( stderr, "At line %d Break\n", $1->getLine() );
									if (!currentBreakableBlock) {
										ParseError( _t11->getLine(), "Break must be within switch/while/do-while" );
										_retv = new TransOpNop;
									} else {
										TransOpBreak *bres = new TransOpBreak;
										bres->breakBlock = currentBreakableBlock;
										_retv = bres;
									}
 consume();
								}
								else {
									if ( (LA(1)==Continue) ) {
										zzmatch(Continue); _t11 = (ANTLRTokenPtr)LT(1); labase++;
										
										
										//fprintf( stderr, "At line %d Break\n", $1->getLine() );
										if (!currentContinueableBlock) {
											ParseError( _t11->getLine(), "Continue must be within while/do-while" );
											_retv = new TransOpNop;
										} else {
											TransOpContinue *cres = new TransOpContinue;
											cres->continueBlock = currentContinueableBlock;
											_retv = cres;
										}
 consume();
									}
									else {
										if ( (LA(1)==Write)
 ) {
											zzmatch(Write); _t11 = (ANTLRTokenPtr)LT(1); labase++;
											
											
											_retv = new TransOpWrite;
											//fprintf( stderr, "At line %d Write\n", $1->getLine() );
 consume();
										}
										else {FAIL(4,err14,err15,err16,err17,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd13, 0x40);
	return _retv;
}

void
Sequencer::lokale_variable_deklarieren(void)
{
	zzRULE;
	
	hbString id;
	int line = 0;
	Value *expr = 0;
	bool exprAr = false;
	bool isArray = false;
	CommonInfo *inf;
	if ( (LA(1)==Temporaer) ) {
		zzmatch(Temporaer); labase++;
		 consume();
		identifier( id, line );
		{
			if ( (LA(1)==LSQUAREBRACKET) ) {
				zzmatch(LSQUAREBRACKET); labase++;
				 consume();
				{ struct _rv22 _trv; _trv = expression();

				expr = _trv.res; exprAr  = _trv.isArray; }
				zzmatch(RSQUAREBRACKET); labase++;
				
				isArray = true;
 consume();
			}
			else {
				if ( (setwd13[LA(1)]&0x80) ) {
				}
				else {FAIL(1,err18,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
		
		if (!isArray) {
			ValueLocVar *newvar = new ValueLocVar;
			if (!currentContext->insert((char*)id.getStr(), newvar)) {
				ParseError( line, "Can't declare variable: %s", id.getStr() );
			}
			newvar->name = strdup( id.getStr() );
		} else {
			// Dimensionierung muss ein konstanter ausdruck sein
			ValueLocalArray *newvar = new ValueLocalArray(expr->GetVal( 0 ));
			if (!currentContext->insert((char*)id.getStr(), newvar)) {
				ParseError( line, "Can't declare variable: %s", id.getStr() );
			}
			//fprintf( stderr, "Array dim %ld\n", expr->GetVal( 0 ) );
			newvar->name = strdup( id.getStr() );
		}
	}
	else {
		if ( (LA(1)==IDENTIFIER) ) {
			{ struct _rv65 _trv; _trv = datenart( 0 );

			inf = _trv.res; line  = _trv.line; }
			zzmatch(IDENTIFIER); labase++;
			
			
			fprintf( stderr, "%s[%d]:\n", inputFileName, line );
			fprintf( stderr, "Not implemented: DATENART IDENTIFIER: %s[%d]",
			__FILE__, __LINE__ );
			abort();
 consume();
		}
		else {FAIL(1,err19,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd14, 0x1);
}

void
Sequencer::globale_variable_deklarieren(void)
{
	zzRULE;
	
	hbString id;
	int line = 0;
	Value *expr = 0;
	bool exprAr = false;
	bool isArray = false;
	zzmatch(Global); labase++;
	 consume();
	identifier( id, line );
	{
		if ( (LA(1)==LSQUAREBRACKET)
 ) {
			zzmatch(LSQUAREBRACKET); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			expr = _trv.res; exprAr  = _trv.isArray; }
			zzmatch(RSQUAREBRACKET); labase++;
			
			isArray = true;
 consume();
		}
		else {
			if ( (setwd14[LA(1)]&0x2) ) {
			}
			else {FAIL(1,err20,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	
	if (!isArray) {
		ValueLocVar *newvar = new ValueLocVar;
		if (!globalContext->insert((char*)id.getStr(), newvar)) {
			ParseError( line, "Can't declare variable: %s", id.getStr() );
		}
		newvar->name = strdup( id.getStr() );
	} else {
		// Dimensionierung muss ein konstanter ausdruck sein
		ValueLocalArray *newvar = new ValueLocalArray(expr->GetVal( 0 ));
		if (!globalContext->insert((char*)id.getStr(), newvar)) {
			ParseError( line, "Can't declare variable: %s", id.getStr() );
		}
		newvar->name = strdup( id.getStr() );
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd14, 0x4);
}

 TransOp * 
Sequencer::fehlernachricht_oder_syscmd(void)
{
	 TransOp * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t12=NULL;
	PURIFY(_retv,sizeof( TransOp * 	))
	
	_retv = 0;
	ValueList *args;
	char *fmtStr = 0;
	TransOpWithValueList *tres = 0;
	{
		if ( (LA(1)==ErrorMessage) ) {
			zzmatch(ErrorMessage); labase++;
			
			tres = new TransOpErrMsg;
 consume();
		}
		else {
			if ( (LA(1)==System) ) {
				zzmatch(System); labase++;
				
				tres = new TransOpSysCmd;
 consume();
			}
			else {FAIL(1,err21,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	zzmatch(LPARENTHESIS); _t12 = (ANTLRTokenPtr)LT(1); labase++;
	 consume();
	 fmtStr  = string();

	 args  = opt_arglist();

	zzmatch(RPARENTHESIS); labase++;
	
	
	{
		char *prc = 0;
		prc = strchr( fmtStr, '%' );
		unsigned short cntAR = 0;
		while (prc) {
			if (*(prc+1) && *(prc+1) == '%') prc++; // no arg: %%
			else cntAR++;
			prc = strchr( prc+1, '%' );
		}
		unsigned short cntArgsScript = 0;
		if (args)
		cntArgsScript = args->GetNum();
		if (cntAR != cntArgsScript) {
			ParseError( _t12->getLine(),
			"Number of args in format string"
			" does not match number of args" );
			delete _retv;
			_retv = new TransOpNop;
		}
		else {
			tres->fmtString = fmtStr;
			tres->valueList = args;
			_retv = tres;
		}
	}
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd14, 0x8);
	return _retv;
}

 char * 
Sequencer::string(void)
{
	 char * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( char * 	))
	_retv = 0;
	zzmatch(STRING); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	
	_retv = strdup( _t11->getText() + 1 ); _retv[ strlen( _retv ) - 1] = 0;
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd14, 0x10);
	return _retv;
}

 ValueList * 
Sequencer::opt_arglist(void)
{
	 ValueList * 	 _retv;
	zzRULE;
	PURIFY(_retv,sizeof( ValueList * 	))
	_retv = 0;
	if ( (LA(1)==RPARENTHESIS) ) {
	}
	else {
		if ( (LA(1)==COMMA)
 ) {
			 _retv  = arglist();

		}
		else {FAIL(1,err22,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd14, 0x20);
	return _retv;
}

 ValueList * 
Sequencer::arglist(void)
{
	 ValueList * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( ValueList * 	))
	_retv = 0; Value *expr = 0; bool exprAr = false;
	zzmatch(COMMA); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr = _trv.res; exprAr  = _trv.isArray; }
	
	_retv = new ValueList; _retv->Add( expr );
	if (exprAr)
	ParseError( _t11->getLine(), "An array value can't be used as argument" );
	{
		ANTLRTokenPtr _t21=NULL;
		while ( (LA(1)==COMMA) ) {
			zzmatch(COMMA); _t21 = (ANTLRTokenPtr)LT(1); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			expr = _trv.res; exprAr  = _trv.isArray; }
			
			_retv->Add( expr );
			if (exprAr)
			ParseError( _t21->getLine(), "An array value can't be used as argument" );
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd14, 0x40);
	return _retv;
}

 TransOpMonoflop * 
Sequencer::monoflop_decl(void)
{
	 TransOpMonoflop * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t13=NULL;
	PURIFY(_retv,sizeof( TransOpMonoflop * 	))
	
	_retv = 0;
	bool canRetrigger = false, exprAr = false, exprAr2 = false;
	Value *expr = 0, *expr2 = 0;
	int mf_type = 0;
	TransOp *anwl;
	 mf_type  = monofloptype();

	{
		if ( (LA(1)==Retriggerbarer) ) {
			zzmatch(Retriggerbarer); labase++;
			
			canRetrigger = true;
 consume();
		}
		else {
			if ( (LA(1)==Monoflop) ) {
			}
			else {FAIL(1,err23,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	zzmatch(Monoflop); _t13 = (ANTLRTokenPtr)LT(1); labase++;
	 consume();
	zzmatch(LPARENTHESIS); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr = _trv.res; exprAr  = _trv.isArray; }
	{
		if ( (LA(1)==COMMA) ) {
			zzmatch(COMMA); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			expr2 = _trv.res; exprAr2  = _trv.isArray; }
		}
		else {
			if ( (LA(1)==RPARENTHESIS)
 ) {
			}
			else {FAIL(1,err24,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	zzmatch(RPARENTHESIS); labase++;
	 consume();
	zzmatch(LCURLYBRACE); labase++;
	 consume();
	pushContext();
	
	if (!currentContext->insert( "Ausloesezeitpunkt", &internalAusloesezeitpunkt )) {
		panic ("Can't internally declare Ausloesezeitpunkt\n" );
	}
	if (canRetrigger && !currentContext->insert( "ErsterAusloesezeitpunkt", &internalErsterAusloesezeitpunkt )) {
		panic ("Can't internally declare ErsterAusloesezeitpunkt\n" );
	}
	pushBlock( new TransOpBreakableBlock );
	anweisungsliste();
	anwl = currentBlock;
	popBlock();
	popContext();
	zzmatch(RCURLYBRACE); labase++;
	
	
	TransOpMonoflop *tres;
	if (canRetrigger) {
		TransOpRtrMonoflop *ttres = new TransOpRtrMonoflop;
		ttres->timeout = expr;
		if (expr2) ttres->maxTimeout = expr2;
		_retv = ttres;
	} else {
		TransOpMonoflop *ttres = new TransOpMonoflop;
		ttres->timeout = expr;
		if (expr2) {
			ParseError ( _t13->getLine(),
			"MaxTimeout not available for non retriggerable monoflop" );
		}
		_retv = ttres;
	}
	_retv->mfType = mf_type;
	_retv->entryDa = currentInputDatatype->dk->DataId;
	_retv->action = anwl;
 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd14, 0x80);
	return _retv;
}

 int  
Sequencer::monofloptype(void)
{
	 int  	 _retv;
	zzRULE;
	PURIFY(_retv,sizeof( int  	))
	_retv = 0;
	if ( (setwd15[LA(1)]&0x1) ) {
		{
			if ( (LA(1)==Eaweiser) ) {
				zzmatch(Eaweiser); labase++;
				 consume();
			}
			else {
				if ( (setwd15[LA(1)]&0x2) ) {
				}
				else {FAIL(1,err25,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
		_retv = MF_T_EA;
	}
	else {
		if ( (LA(1)==Einmaliger) ) {
			zzmatch(Einmaliger); labase++;
			
			_retv = MF_T_ONCE;
 consume();
		}
		else {
			if ( (LA(1)==Knotenweiser)
 ) {
				zzmatch(Knotenweiser); labase++;
				
				_retv = MF_T_NODE;
 consume();
			}
			else {
				if ( (LA(1)==Clusterweiser) ) {
					zzmatch(Clusterweiser); labase++;
					
					_retv = MF_T_EA2CL;
 consume();
				}
				else {FAIL(1,err26,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd15, 0x4);
	return _retv;
}

void
Sequencer::sequenz_festlegung(void)
{
	zzRULE;
	Value *expr = 0, *spalte = 0; TransOpBreakableBlock *bdy = 0; bool exprAr = false, colAr = false;
	zzmatch(Sequence); labase++;
	 consume();
	zzmatch(LPARENTHESIS); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr = _trv.res; exprAr  = _trv.isArray; }
	zzmatch(COMMA); labase++;
	 consume();
	{ struct _rv44 _trv; _trv = atomare_spalte();

	spalte = _trv.res; colAr  = _trv.isArray; }
	zzmatch(RPARENTHESIS); labase++;
	 consume();
	zzmatch(LCURLYBRACE); labase++;
	 consume();
	pushContext();
	pushBlock(  new TransOpBreakableBlock  );
	anweisungsliste();
	bdy = currentBlock;
	popBlock();
	popContext();
	zzmatch(RCURLYBRACE); labase++;
	 consume();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd15, 0x8);
}

Sequencer::_rv58
Sequencer::linke_seite(void)
{
	struct _rv58 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv58))
	
	_retv.res = 0;
	CommonInfo *inf;
	bool have_da = false;
	Value *col = 0; inf = 0;
	int line = 0;
	_retv.isArray = false;
	if ( (LA(1)==Dollar) ) {
		{ struct _rv67 _trv; _trv = variable();

		_retv.res = _trv.res; _retv.isArray  = _trv.isArray; }
	}
	else {
		if ( (setwd15[LA(1)]&0x10) ) {
			{
				if ( (setwd15[LA(1)]&0x20) && (setwd15[LA(2)]&0x40)
 ) {
					{ struct _rv59 _trv; _trv = datenartelement();

					inf = _trv.res; line  = _trv.line; }
					zzmatch(DOT); labase++;
					
					have_da = true;
 consume();
				}
				else {
					if ( (LA(1)==IDENTIFIER) && (setwd15[LA(2)]&0x80) ) {
					}
					else {FAIL(2,err27,err28,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
				}
			}
			{ struct _rv63 _trv; _trv = spalte( inf, have_da );

			col = _trv.res; _retv.isArray  = _trv.isArray; }
			
			CommonInfo *dkInf = 0;
			if (have_da&&inf)	dkInf = inf;
			else {
				dkInf = new CommonInfo( *currentInputDatatype );
				dkInf->old = false;
			}
			ValueSeqCommon *tres = new ValueSeqCommon;
			tres->info			= dkInf;
			tres->realValue	= col;
			tres->atomic = !_retv.isArray;
			_retv.res = tres;
			//fprintf( stderr, "OK lhs %d %s\n", line, tres->info->dk->DkName );
		}
		else {FAIL(1,err29,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd16, 0x1);
	return _retv;
}

Sequencer::_rv59
Sequencer::datenartelement(void)
{
	struct _rv59 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv59))
	_retv.res = new CommonInfo; bool realExpl = false; CommonInfo *dc = 0;
	{
		if ( (setwd16[LA(1)]&0x2) ) {
			atomicEa( _retv.res, realExpl );
		}
		else {
			if ( (setwd16[LA(1)]&0x4) ) {
				nonAtomicEa( _retv.res );
				{
					if ( (LA(1)==LPARENTHESIS)
 ) {
						deepenNonAtomic( _retv.res );
					}
					else {
						if ( (LA(1)==In) ) {
						}
						else {FAIL(1,err30,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
					}
				}
				zzmatch(In); labase++;
				
				realExpl = true;
 consume();
			}
			else {FAIL(1,err31,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	{ struct _rv65 _trv; _trv = datenart( _retv.res );

	dc = _trv.res; _retv.line  = _trv.line; }
	{
		if ( (LA(1)==DaNew) ) {
			zzmatch(DaNew); labase++;
			
			
			_retv.res->old = false;
			if (realExpl) {
				ParseError( _retv.line, "Explicit addressing not allowd with '-New'" );
			}
 consume();
		}
		else {
			if ( (setwd16[LA(1)]&0x8) ) {
				{
					if ( (LA(1)==DaOld) ) {
						zzmatch(DaOld); labase++;
						 consume();
					}
					else {
						if ( (LA(1)==DOT)
 ) {
						}
						else {FAIL(1,err32,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
					}
				}
			}
			else {FAIL(1,err33,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd16, 0x10);
	return _retv;
}

void
Sequencer::atomicEa( CommonInfo *inf, bool realExpl )
{
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	Value *expr = 0; bool exprAr = false;  realExpl = false;
	if ( (setwd16[LA(1)]&0x20) ) {
		{
			if ( (LA(1)==De) ) {
				zzmatch(De); labase++;
				 consume();
				zzmatch(In); labase++;
				
				realExpl = true;
 consume();
			}
			else {
				if ( (LA(1)==IDENTIFIER) ) {
				}
				else {FAIL(1,err34,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
	}
	else {
		if ( (LA(1)==Ea) ) {
			zzmatch(Ea); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			 consume();
			zzmatch(LPARENTHESIS); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			expr = _trv.res; exprAr  = _trv.isArray; }
			zzmatch(RPARENTHESIS); labase++;
			 consume();
			zzmatch(In); labase++;
			
			
			if (inf) {
				inf->theExplicitEaid = expr;
				inf->explicitEaid = true;
				//fprintf( stderr, "explicit eaid(EA) at line %d val=%ld\n", $1->getLine(), inf->theExplicitEaid->GetVal(0) );
			}
			realExpl = true;
			if (exprAr)
			ParseError( _t11->getLine(), "An array value can't be used as explicit Eaid" );
 consume();
		}
		else {FAIL(1,err35,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd16, 0x40);
}

void
Sequencer::nonAtomicEa( CommonInfo *inf )
{
	zzRULE;
	if ( (LA(1)==DEs)
 ) {
		zzmatch(DEs); labase++;
		 consume();
		zzmatch(DES); labase++;
		 consume();
		{
			if ( (LA(1)==Cluster) ) {
				zzmatch(Cluster); labase++;
				
				inf->range = rng_cluster2de;
 consume();
			}
			else {
				if ( (LA(1)==Knoten) ) {
					zzmatch(Knoten); labase++;
					
					inf->range = rng_node;
 consume();
				}
				else {FAIL(1,err36,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
	}
	else {
		if ( (LA(1)==Cluster) ) {
			zzmatch(Cluster); labase++;
			 consume();
			zzmatch(DES); labase++;
			 consume();
			zzmatch(DEs); labase++;
			
			inf->range = rng_de2cluster;
 consume();
		}
		else {FAIL(1,err37,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd16, 0x80);
}

void
Sequencer::deepenNonAtomic( CommonInfo *inf )
{
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	CommonInfo *dkInf = 0; int line = 0; bool exprAr = false; Value *expr;
	zzmatch(LPARENTHESIS); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr = _trv.res; exprAr  = _trv.isArray; }
	
	inf->explicitEaid = true;
	inf->theExplicitEaid = expr;
	//fprintf( stdout, "explicit eaid(NA) at line %d\n", $1->getLine() );
	if (exprAr) {
		ParseError( _t11->getLine(), "Can't handle array as explicit eaid" );
	}
	{
		if ( (LA(1)==COMMA) ) {
			zzmatch(COMMA); labase++;
			 consume();
			{ struct _rv65 _trv; _trv = datenart( 0 );

			dkInf = _trv.res; line  = _trv.line; }
			inf->explInDa = true; inf->theExplicitDa = dkInf;
		}
		else {
			if ( (LA(1)==RPARENTHESIS)
 ) {
			}
			else {FAIL(1,err38,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	zzmatch(RPARENTHESIS); labase++;
	 consume();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd17, 0x1);
}

Sequencer::_rv63
Sequencer::spalte( CommonInfo *infIn, bool have_da )
{
	struct _rv63 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv63))
	Value *indx = 0; hbString id; _retv.res = 0; int line = 0; _retv.isArray = false;
	identifier( id, line );
	 indx  = opt_array_elem();

	
	CommonInfo *dkInf;
	short col_offset, col_len, col_type;
	long col_dim;
	if (have_da)	dkInf = infIn;
	else 				dkInf = currentInputDatatype;
	if (!dkInf->dk)
	goto fail;
	if ( !ColLimitsType (	(char*)(const char*)id, dkInf->dk->DataId,
	&col_offset, &col_len,
	&col_type, &col_dim ) ) {
		ParseError( line,
		"DA %s has no column %s.",
		dkInf->dk->DkName, (const char*) id );
		_retv.res = badValue( _retv.isArray );
	} else {
	ZdfColumn *tmp_res = 0;
	bool badType = false;
	switch ( col_type ) {
	case CBYTETYPE : 		tmp_res = new ZdfColumnUByte; 	break;
	case CSTRINGTYPE :
	case CFIXCHARTYPE :	tmp_res = new ZdfColumnSByte;		break;
	case CSHORTTYPE :
	case CSHORT1TYPE :
	case CSHORT2TYPE :
	case CSHORT3TYPE :
	case CSHORT4TYPE : 	tmp_res = new ZdfColumnSShort;	break;
	case CBIT16TYPE : 	tmp_res = new ZdfColumnUShort; 	break;
	case CLONGTYPE :
	case CLONG1TYPE :
	case CLONG2TYPE :
	case CLONG3TYPE :
	case CLONG4TYPE : 	tmp_res = new ZdfColumnSLong; 	break;
	case CBIT32TYPE : 	tmp_res = new ZdfColumnULong; 	break;
	default:
	ParseError( line,
	"DA %s column %s has unsupported type %hd.",
	dkInf->dk->DkName, (const char*) id, col_type);
	badType = true;
	_retv.res = badValue( _retv.isArray );
	break;
}
if (!badType) {
	tmp_res->col_offset = col_offset;
	ZdfColumnArray *colres = 0;
	if ( col_dim ) {
		colres = new ZdfColumnArray;
		colres->col_offset  = col_offset;
		tmp_res->col_offset = 0;
		colres->array_base  = tmp_res;
		colres->col_dim     = col_dim;
		tmp_res = colres;
		_retv.isArray = true;
	}
	_retv.res = tmp_res;
	if (indx && ! badType) {
		if (!colres) {
			ParseError( line, "[] applicable only on array columns");
			_retv.res = badValue( _retv.isArray );
		} else {
			ZdfColumnArrayElem *atmp_res = new ZdfColumnArrayElem;
			atmp_res->index 		= indx;
			atmp_res->col_offset	= colres->col_offset;
			atmp_res->array_base	= colres->array_base;
			atmp_res->col_dim		= colres->col_dim;
			colres->array_base = 0;
			delete tmp_res;
			_retv.res = atmp_res;
			_retv.isArray = false;
		}
	}
}
}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd17, 0x2);
	return _retv;
}

 Value * 
Sequencer::opt_array_elem(void)
{
	 Value * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( Value * 	))
	_retv = 0; bool exprAr = false;
	if ( (setwd17[LA(1)]&0x4) ) {
	}
	else {
		if ( (LA(1)==LSQUAREBRACKET) ) {
			zzmatch(LSQUAREBRACKET); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			_retv = _trv.res; exprAr  = _trv.isArray; }
			zzmatch(RSQUAREBRACKET); labase++;
			
			
			if (exprAr) {
				ParseError( _t11->getLine(), "An array value can't be used as index" );
				_retv = badValue( exprAr );
			}
 consume();
		}
		else {FAIL(1,err39,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd17, 0x8);
	return _retv;
}

Sequencer::_rv65
Sequencer::datenart( CommonInfo *resIn )
{
	struct _rv65 _retv;
	zzRULE;
	PURIFY(_retv,sizeof(struct _rv65))
	_retv.res = 0; hbString id; int line = 0;
	identifier( id,line );
	
	{
		datakind *dk = DkName2DkPtr( id );
		if ( ! dk ) {
			ParseError( line, "Unknown datatype %s", (const char*) id );
		} else {
			if (resIn)	_retv.res = resIn;
			else 			_retv.res = new CommonInfo;
			_retv.res->dk  = dk;
			_retv.line = line;
			if (!catchDataType( dk->DataId )) {
				ParseError( line, "Can't keep DA" );
			}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd17, 0x10);
	return _retv;
}

void
Sequencer::identifier( hbString& res, int& line )
{
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	line = 0;
	zzmatch(IDENTIFIER); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	res = _t11->getText();  line = _t11->getLine();
 consume();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd17, 0x20);
}

Sequencer::_rv67
Sequencer::variable(void)
{
	struct _rv67 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof(struct _rv67))
	
	_retv.res = 0;
	_retv.isArray = false;
	Value *indx = 0;
	hbString id;
	int line = 0;
	Value *expr = 0;
	bool exprAr = false;
	bool useArray = false;
	zzmatch(Dollar); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	 consume();
	identifier( id, line );
	{
		if ( (LA(1)==LSQUAREBRACKET) ) {
			zzmatch(LSQUAREBRACKET); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			expr = _trv.res; exprAr  = _trv.isArray; }
			zzmatch(RSQUAREBRACKET); labase++;
			
			useArray = true;
 consume();
		}
		else {
			if ( (setwd17[LA(1)]&0x40) ) {
			}
			else {FAIL(1,err40,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	
	{
		Value *val = currentContext->find( (char*) id.getStr() );
		if (!val) {
			ParseError( _t11->getLine(), "Unknown variable: %s", id.getStr() );
			bool egal = false;
			_retv.res = badValue( egal );
		} else {
			if (useArray) {
				if (!val->GetProp().Includes( "Val_LocArr" )) {
					ParseError( _t11->getLine(), "Declared variable is no array: %s", id.getStr() );
					bool egal = false;
					_retv.res = badValue( egal );
				} else {
					if (exprAr) {
						ParseError( _t11->getLine(), "Can't accept array value as index" );
						bool egal = false;
						_retv.res = badValue( egal );
					}
					_retv.res = new ValueLocalArrayIndex( (ValueLocalArray*) val, expr );
				}
			} else {
				_retv.res = val;
				Property p = val->GetProp();
				if (p.Includes( "Val_LocArr" )) _retv.isArray = true;
			}
		}
	}
	{
		ANTLRTokenPtr _t21=NULL;
		if ( (LA(1)==POINTERTO)
 ) {
			zzmatch(POINTERTO); _t21 = (ANTLRTokenPtr)LT(1); labase++;
			 consume();
			zzmatch(IDENTIFIER); labase++;
			 consume();
			 indx  = opt_array_elem();

			fprintf( stderr, "%s[%d]:\n", inputFileName, _t21->getLine() );
			fprintf( stderr, "Not implemented ->identifier. Src: %s[%d]\n", __FILE__, __LINE__ );
			abort();
		}
		else {
			if ( (setwd17[LA(1)]&0x80) ) {
			}
			else {FAIL(1,err41,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd18, 0x1);
	return _retv;
}

 TransOp * 
Sequencer::zusammengesetzte_anweisung(void)
{
	 TransOp * 	 _retv;
	zzRULE;
	PURIFY(_retv,sizeof( TransOp * 	))
	_retv = 0;
	if ( (setwd18[LA(1)]&0x2) ) {
		 _retv  = schleife();

	}
	else {
		if ( (setwd18[LA(1)]&0x4) ) {
			 _retv  = schleife2();

		}
		else {
			if ( (LA(1)==If) ) {
				 _retv  = if_else();

			}
			else {
				if ( (LA(1)==Switch)
 ) {
					 _retv  = switch_anweisung();

				}
				else {FAIL(1,err42,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd18, 0x8);
	return _retv;
}

 TransOp * 
Sequencer::schleife(void)
{
	 TransOp * 	 _retv;
	zzRULE;
	PURIFY(_retv,sizeof( TransOp * 	))
	
	_retv = 0;
	Value *expr1 = 0, *expr2 = 0;
	TransOpBreakableBlock *lpBody = 0;
	hbString id;
	int line = 0;
	bool exprAr1 = false, exprAr2 = false;
	TransOpLoop *tres = 0;
	 tres  = loop_type();

	zzmatch(LPARENTHESIS); labase++;
	 consume();
	identifier( id, line );
	zzmatch(COMMA); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr1 = _trv.res; exprAr1  = _trv.isArray; }
	zzmatch(COMMA); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr2 = _trv.res; exprAr2  = _trv.isArray; }
	zzmatch(RPARENTHESIS); labase++;
	 consume();
	zzmatch(LCURLYBRACE); labase++;
	 consume();
	pushContext();
	pushBlock(  new TransOpBreakableBlock  );
	anweisungsliste();
	
	{
		if (exprAr1||exprAr2) {
			ParseError( line, "Loop: Can't accept array value" );
			_retv = new TransOpNop;
		} else {
			Value *val = currentContext->find( (char*) id.getStr() );
			if (val) {
				ParseError( line, "Duplicate variable: %s", id.getStr() );
				_retv = new TransOpNop;
			} else {
				ValueLocVar *newvar = new ValueLocVar;
				if (!currentContext->insert((char*)id.getStr(), newvar)) {
					ParseError( line, "Can't declare variable: %s", id.getStr() );
					_retv = new TransOpNop;
				} else {
					newvar->name = strdup( id.getStr() );
					tres->LoopVar = newvar;
					tres->StartVal = expr1;
					tres->EndVal = expr2;
					tres->transformation = currentBlock;
					_retv = tres;
				}
			}
		}
	}
	popBlock();
	popContext();
	zzmatch(RCURLYBRACE); labase++;
	 consume();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd18, 0x10);
	return _retv;
}

 TransOpLoop * 
Sequencer::loop_type(void)
{
	 TransOpLoop * 	 _retv;
	zzRULE;
	PURIFY(_retv,sizeof( TransOpLoop * 	))
	_retv = 0;
	if ( (LA(1)==IncLoop) ) {
		zzmatch(IncLoop); labase++;
		
		
		//fprintf( stderr, "At line %d IncLoop\n", $1->getLine() );
		_retv = new TransOpIncLoop;
 consume();
	}
	else {
		if ( (LA(1)==DecLoop) ) {
			zzmatch(DecLoop); labase++;
			
			
			//fprintf( stderr, "At line %d DecLoop\n", $1->getLine() );
			_retv = new TransOpDecLoop;
 consume();
		}
		else {FAIL(1,err43,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd18, 0x20);
	return _retv;
}

 TransOp * 
Sequencer::schleife2(void)
{
	 TransOp * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( TransOp * 	))
	
	_retv = 0;
	Value *expr = 0;
	TransOpCLoopWhile *lpWhile = 0;
	TransOpCLoopDoWhile *lpDoWhile = 0;
	bool exprAr = false;
	if ( (LA(1)==While) ) {
		zzmatch(While); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		 consume();
		zzmatch(LPARENTHESIS); labase++;
		 consume();
		{ struct _rv22 _trv; _trv = expression();

		expr = _trv.res; exprAr  = _trv.isArray; }
		zzmatch(RPARENTHESIS); labase++;
		
		
		lpWhile = new TransOpCLoopWhile;
		lpWhile->checkExpression = expr;
		if (exprAr)
		ParseError( _t11->getLine(), "While: Can't accept array value" );
 consume();
		zzmatch(LCURLYBRACE); labase++;
		 consume();
		pushContext();
		pushBlock(  lpWhile  );
		pushBreakableBlock( currentBlock );
		pushContinueableBlock( lpWhile );
		anweisungsliste();
		popContinueableBlock();
		popBreakableBlock();
		popBlock();
		popContext();
		zzmatch(RCURLYBRACE); labase++;
		
		_retv = lpWhile;
 consume();
	}
	else {
		if ( (LA(1)==Do) ) {
			zzmatch(Do); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			
			lpDoWhile = new TransOpCLoopDoWhile;
 consume();
			zzmatch(LCURLYBRACE); labase++;
			 consume();
			pushContext();
			pushBlock(  lpDoWhile  );
			pushBreakableBlock( currentBlock );
			pushContinueableBlock( lpDoWhile );
			anweisungsliste();
			popContinueableBlock();
			popBreakableBlock();
			popBlock();
			popContext();
			zzmatch(RCURLYBRACE); labase++;
			 consume();
			zzmatch(While); labase++;
			 consume();
			zzmatch(LPARENTHESIS); labase++;
			 consume();
			{ struct _rv22 _trv; _trv = expression();

			expr = _trv.res; exprAr  = _trv.isArray; }
			zzmatch(RPARENTHESIS); labase++;
			
			
			lpDoWhile->checkExpression = expr; _retv = lpDoWhile;
			if (exprAr)
			ParseError( _t11->getLine(), "Do-While: Can't accept array value" );
 consume();
		}
		else {FAIL(1,err44,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd18, 0x40);
	return _retv;
}

 TransOp * 
Sequencer::if_else(void)
{
	 TransOp * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( TransOp * 	))
	
	_retv = 0;
	Value *expr = 0;
	TransOpBreakableBlock *bl1 = 0, *bl2 = 0;
	bool have_else = false;
	bool exprAr = false;
	//fprintf( stderr, "%s %d\n", __PRETTY_FUNCTION__, __LINE__ );
	zzmatch(If); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	 consume();
	zzmatch(LPARENTHESIS); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr = _trv.res; exprAr  = _trv.isArray; }
	zzmatch(RPARENTHESIS); labase++;
	
	
	if (exprAr)
	ParseError( _t11->getLine(), "If: Can't accept array value" );
 consume();
	zzmatch(LCURLYBRACE); labase++;
	 consume();
	pushContext();
	pushBlock(  new TransOpBreakableBlock  );
	anweisungsliste();
	bl1 = currentBlock;
	popBlock();
	popContext();
	zzmatch(RCURLYBRACE); labase++;
	 consume();
	{
		if ( (LA(1)==Else)
 ) {
			zzmatch(Else); labase++;
			 consume();
			zzmatch(LCURLYBRACE); labase++;
			 consume();
			pushContext();
			pushBlock(  new TransOpBreakableBlock  );
			anweisungsliste();
			bl2 = currentBlock;
			popBlock();
			popContext();
			zzmatch(RCURLYBRACE); labase++;
			
			
			//fprintf( stderr, "At line %d Else bl1 %p bl2 %p\n", $1->getLine(), bl1, bl2 );
			TransOpIfElse *tres = new TransOpIfElse;
			tres->Condition = expr;
			tres->IfBlock = bl1;
			tres->ElseBlock = bl2;
			_retv = tres;
			have_else = true;
 consume();
		}
		else {
			if ( (setwd18[LA(1)]&0x80) ) {
			}
			else {FAIL(1,err45,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
		}
	}
	
	if (!have_else) {
		//fprintf( stderr, "At line %d If bl1 %p\n", $1->getLine(), bl1 );
		TransOpIf *tres = new TransOpIf;
		tres->Condition = expr;
		tres->IfBlock = bl1;
		_retv = tres;
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd19, 0x1);
	return _retv;
}

 TransOp * 
Sequencer::switch_anweisung(void)
{
	 TransOp * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( TransOp * 	))
	_retv = 0; Value *expr = 0; bool exprAr = false;
	zzmatch(Switch); _t11 = (ANTLRTokenPtr)LT(1); labase++;
	
	
	//fprintf( stderr, "At line %d Switch\n", $1->getLine() );
 consume();
	pushSwitch();
	zzmatch(LPARENTHESIS); labase++;
	 consume();
	{ struct _rv22 _trv; _trv = expression();

	expr = _trv.res; exprAr  = _trv.isArray; }
	zzmatch(RPARENTHESIS); labase++;
	
	
	if (exprAr) ParseError( _t11->getLine(), "Switch: Can't accept array value" );
	currentSwitch->setExpr( expr); _retv = currentSwitch;
 consume();
	zzmatch(LCURLYBRACE); labase++;
	 consume();
	pushContext();
	pushBlock(  currentSwitch  );
	pushBreakableBlock( currentSwitch );
	switchanweisungen();
	popBreakableBlock();
	popBlock();
	popContext();
	zzmatch(RCURLYBRACE); labase++;
	 consume();
	popSwitch();
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd19, 0x2);
	return _retv;
}

void
Sequencer::switchanweisungen(void)
{
	zzRULE;
	if ( (LA(1)==Case) ) {
		caseliste();
		{
			if ( (LA(1)==Default) ) {
				default_anweisung();
			}
			else {
				if ( (LA(1)==RCURLYBRACE) ) {
				}
				else {FAIL(1,err46,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
			}
		}
	}
	else {
		if ( (LA(1)==Default)
 ) {
			default_anweisung();
		}
		else {FAIL(1,err47,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd19, 0x4);
}

void
Sequencer::caseliste(void)
{
	zzRULE;
	{
		int zzcnt=1;
		do {
			case_anweisung();
		} while ( (LA(1)==Case) );
	}
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd19, 0x8);
}

void
Sequencer::case_anweisung(void)
{
	zzRULE;
	TransOpBreakableBlock *anw = 0; long cnst = 0;
	zzmatch(Case); labase++;
	 consume();
	 cnst  = konstante();

	
	//fprintf( stderr, "At line %d Case\n", $1->getLine() );
	zzmatch(COLON); labase++;
	 consume();
	pushBlock( new TransOpBreakableBlock );
	anweisungsliste();
	currentSwitch->addCase( cnst, currentBlock );
	popBlock();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd19, 0x10);
}

void
Sequencer::default_anweisung(void)
{
	zzRULE;
	TransOpBreakableBlock *anw = 0;
	zzmatch(Default); labase++;
	 consume();
	zzmatch(COLON); labase++;
	 consume();
	pushBlock( new TransOpBreakableBlock );
	anweisungsliste();
	
	//fprintf( stderr, "At line %d Default\n", $1->getLine() );
	currentSwitch->addDefault(currentBlock);
	popBlock();
	return;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd19, 0x20);
}

 Value * 
Sequencer::spezial_funktion(void)
{
	 Value * 	 _retv;
	zzRULE;
	ANTLRTokenPtr _t11=NULL;
	PURIFY(_retv,sizeof( Value * 	))
	Value *expr1 = 0, *expr2 = 0; _retv = 0; bool exprAr1 = false, exprAr2 = false;
	if ( (LA(1)==RdsLoc) ) {
		zzmatch(RdsLoc); _t11 = (ANTLRTokenPtr)LT(1); labase++;
		 consume();
		zzmatch(LPARENTHESIS); labase++;
		 consume();
		{ struct _rv22 _trv; _trv = expression();

		expr1 = _trv.res; exprAr1  = _trv.isArray; }
		zzmatch(COMMA); labase++;
		 consume();
		{ struct _rv22 _trv; _trv = expression();

		expr2 = _trv.res; exprAr2  = _trv.isArray; }
		zzmatch(RPARENTHESIS); labase++;
		
		
		if (exprAr1 || exprAr2)
		ParseError( _t11->getLine(), "RdsLoc: Can't accept array values" );
		else {
			ValueRdsLoc *tres = new ValueRdsLoc;
			tres->LeftOperand = expr1;
			tres->RightOperand = expr2;
			_retv = tres;
		}
 consume();
	}
	else {
		if ( (LA(1)==Exists) ) {
			zzmatch(Exists); _t11 = (ANTLRTokenPtr)LT(1); labase++;
			 consume();
			zzmatch(LPARENTHESIS); labase++;
			 consume();
			{ struct _rv58 _trv; _trv = linke_seite();

			expr1 = _trv.res; exprAr1  = _trv.isArray; }
			zzmatch(RPARENTHESIS); labase++;
			
			
			ValueExists *tres = new ValueExists;
			tres->operand = expr1;
			_retv = tres;
			if (pDbgLevel>3) {
				fprintf( stdout, "EXISTS arg %p:\n", expr1 );
				expr1->GetProp()./*Cdr().*/Dump(); fflush( stdout );
			}
 consume();
		}
		else {FAIL(1,err48,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd19, 0x40);
	return _retv;
}

 unsigned long  
Sequencer::konstante(void)
{
	 unsigned long  	 _retv;
	zzRULE;
	PURIFY(_retv,sizeof( unsigned long  	))
	_retv = 0; CommonInfo *daInfo = 0; int line = 0;
	if ( (setwd19[LA(1)]&0x80) ) {
		 _retv  = constant();

	}
	else {
		if ( (LA(1)==NumEas)
 ) {
			zzmatch(NumEas); labase++;
			 consume();
			zzmatch(LPARENTHESIS); labase++;
			 consume();
			{ struct _rv65 _trv; _trv = datenart( 0 );

			daInfo = _trv.res; line  = _trv.line; }
			zzmatch(RPARENTHESIS); labase++;
			
			_retv = getNumEa( daInfo->dk->DataId );
 consume();
		}
		else {FAIL(1,err49,&zzMissSet,&zzMissText,&zzBadTok,&zzBadText,&zzErrk); goto fail;}
	}
	return _retv;
fail:
	syn(zzBadTok, (ANTLRChar *)"", zzMissSet, zzMissTok, zzErrk);
	resynch(setwd20, 0x1);
	return _retv;
}
