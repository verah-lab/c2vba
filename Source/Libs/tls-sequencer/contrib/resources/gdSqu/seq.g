#header <<
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
>>

<<
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
>>

#token LCURLYBRACE 				"\{"
#token RCURLYBRACE 				"\}"
#token LSQUAREBRACKET 			"\["
#token RSQUAREBRACKET 			"\]"
#token LPARENTHESIS 				"\("
#token RPARENTHESIS 				"\)"
#token COMMA 						","
#token LOGOR 						"\|\|"
#token LOGAND 						"&&"
#token BITWISEOR 					"\|"
#token BITWISEXOR 				"^"
#token AMPERSAND 					"&"
#token EQUAL 						"=="
#token NOTEQUAL 					"!="
#token LESSTHAN 					"<"
#token GREATERTHAN 				"\>"
#token LESSTHANOREQUALTO 		"\<="
#token GREATERTHANOREQUALTO	"\>="
#token SHIFTLEFT 					"\<\<"
#token SHIFTRIGHT 				"\>\>"
#token PLUS 						"\+"
#token MINUS 						"\-"
#token STAR 						"\*"
#token DIVIDE 						"/"
#token MOD 							"\%"
#token ONESCOMPLEMENT 			"\~"
#token LOGNOT 						"!"
#token DOT 							"."
#token COLON 						":"
#token POINTERTO 					"\-\>"

/* currently unused */
#token ASSIGNEQUAL 				"="
#token BITWISEANDEQUAL 			"&="
#token BITWISEOREQUAL 			"\|\="
#token BITWISEXOREQUAL			"\^\="
#token DIVIDEEQUAL 				"/="
#token ELLIPSIS 					"..."
#token MINUSEQUAL 				"\-="
#token MINUSMINUS 				"\-\-"
#token MODEQUAL 					"\%="
#token PLUSEQUAL 					"\+="
#token PLUSPLUS 					"\+\+"
#token QUESTIONMARK 				"?"
//#token SEMICOLON 					";"
#token SHIFTLEFTEQUAL 			"\<\<="
#token SHIFTRIGHTEQUAL 			"\>\>="
#token TIMESEQUAL 				"\*="
	
#token Bei 							"Bei"
#token Break 						"Break"
#token Case 						"Case"
#token Clusters 					"Clusters"
#token DecLoop 					"DecLoop"
#token Default 					"Default"
#token Eingang 					"Eingang"
#token Else 						"Else"
#token ErrorMessage 				"ErrorMessage"
#token Exists 						"Exists"
#token Global 						"Global"
#token If 							"If"
#token IncLoop 					"IncLoop"
#token Knotens 					"Knotens"
#token New 							"New"
#token Old 							"Old"
#token RdsLoc 						"RdsLoc"
#token Write 						"Write"
#token Sequence 					"Sequence"
#token Switch 						"Switch"
#token Temporaer 					"Temporaer"
#token Von 							"[Vv]on"
#token Des 							"des"
#token Do							"Do"
#token While						"While"
#token Continue					"Continue"
#token DES							"des"
#token De							"De"
#token DEs							"DEs"
#token Ea							"Ea"
#token DaNew						"\-New"
#token DaOld						"\-Old"
#token Cluster						"Clusters"
#token Cluster						"Cluster"
#token Knoten						"Knotens"
#token Knoten						"Knoten"
#token Monoflop					"Monoflop"
#token Retriggerbarer			"retriggerbarer"
#token Eaweiser					"eaweiser"
#token Einmaliger					"einmaliger"
#token Knotenweiser				"knotenweiser"
#token Clusterweiser				"clusterweiser"
#token In							"in"
#token System						"System"
#token NumEas						"AnzahlEas"
#token ";" << skip(); >>

#token Assign						"\:\="
#token SeqState					"\=\=\>"
#token Dollar						"\$"

/* from ansi.g -------------------------------------------------------------- */

#token OCTALINT 					"0[0-7]*"
#token DECIMALINT 				"[1-9][0-9]*"
#token RDSINT		 				"[0-9][0-9]*\-[0-9][0-9]*{\-[0-9][0-9]*}"
#token HEXADECIMALINT			"(0x|0X)[0-9a-fA-F]+"
#token 								"'" << mode(CHARACTERS); more (); >>
#token 								"\"" << mode(STRINGS); more (); >>
#token IDENTIFIER					"[a-zA-Z_][a-zA-Z0-9_]*"

#token "\/\/(~[\n])*\n"				<< newline(); skip(); >>
#token "/\*" << mode (COMMENT); skip (); >>

#token "[\t\ ]+"						<< skip(); >>
#token "\\\n"							<< newline(); skip(); >>
#token "\\\r"							<< newline(); skip(); >>
#token "[\n\r]"						<< newline(); skip(); >>

// line number and file stuff from preprocessor
#token "#[\ \t]*{line}[\ \t]* [0-9]+[ ]*{[\ \t]* \"(~[\"])*\"{(~[\n])*}}\n"
									<<
										extern char *inputFileName;
										extern int lastLine;
										char *numstart = strpbrk( begexpr(), "0123456789" );
										_line = atoi( numstart );
										char *fnameStart = strchr( begexpr(), '"' );
										if (fnameStart) {
											free( inputFileName );
											inputFileName = strdup( fnameStart+1 );
											char *se = strchr( inputFileName, '"' );
											*se = 0;
											if (!strlen( inputFileName )) {
												free( inputFileName );
												inputFileName = strdup( "stdin" );
											}
										} else {
											free( inputFileName );
											inputFileName = strdup( "unknown filename" );
										}
										lastLine = _line;
										//printf( "At line %d of file '%s'\n",
										//	_line, inputFileName );
										skip();
									>>

#token Eof "@"

#lexclass COMMENT

#token "[\n\r]"						<< skip(); newline(); >>
#token "\*/"							<< mode(START); skip(); >>
#token "\*~[/]"						<< skip(); >>
#token "~[\*\n\r]+"					<< skip(); >>

#lexclass STRINGS

#token STRING "\"" 					<< mode(START); >>
//#token "\\\n"							<< /*replchar((char) 0x0A); more(); newline();*/ >>
//#token "\\\r"							<< /*replchar((char) 0x0D); more(); newline();*/ >>
//#token "\\\n"							<< advance(); more(); >>
//#token "\\\r"							<< advance(); more(); >>
#token "\\\n"							<< replstr(""); more(); >>
#token "\\\r"							<< replstr(""); more(); >>
#token "\\n" 							<< replchar((char) 0x0A); more(); >>
#token "\\t" 							<< replchar((char) 0x09); more(); >>
#token "\\v" 							<< replchar((char) 0x0B); more(); >>
#token "\\b" 							<< replchar((char) 0x08); more(); >>
#token "\\r" 							<< replchar((char) 0x0D); more(); >>
#token "\\f" 							<< replchar((char) 0x0C); more(); >>
#token "\\a" 							<< replchar((char) 0x07); more(); >>
#token "\\\\" 							<< replchar((char) 0x5C); more(); >>
#token "\\?" 							<< replchar((char) 0x3F); more(); >>
#token "\\'" 							<< replchar((char) 0x27); more(); >>
#token "\\\"" 							<< replchar((char) 0x22); more(); >>
#token "\\0[0-7]*"					<< replchar((char) strtol (begexpr(), NULL, 8)); more(); >>
#token "\\[1-9][0-9]*"				<< replchar((char) strtol (begexpr(), NULL, 10)); more(); >>
#token "\\(0x|0X)[0-9a-fA-F]+"	<< replchar((char) strtol (begexpr(), NULL, 16)); more(); >>
#token "[\n\r]" 						<< newline(); more(); >>
#token "~[\"\n\r\\]+" 				<< more(); >>

#lexclass CHARACTERS 

#token CHARACTER "'" 				<<mode(START); >>
#token "\\n" 							<< replchar((char) 0x0A); more();mode(DONE); >>
#token "\\t" 							<< replchar((char) 0x09); more();mode(DONE); >>
#token "\\v" 							<< replchar((char) 0x0B); more();mode(DONE); >>
#token "\\b" 							<< replchar((char) 0x08); more();mode(DONE); >>
#token "\\r" 							<< replchar((char) 0x0D); more();mode(DONE); >>
#token "\\f" 							<< replchar((char) 0x0C); more();mode(DONE); >>
#token "\\a" 							<< replchar((char) 0x07); more();mode(DONE); >>
#token "\\\\" 							<< replchar((char) 0x5C); more();mode(DONE); >>
#token "\\?" 							<< replchar((char) 0x3F); more();mode(DONE); >>
#token "\\'" 							<< replchar((char) 0x27); more();mode(DONE); >>
#token "\\\"" 							<< replchar((char) 0x22); more();mode(DONE); >>
#token "\\0[0-7]*" 					<< replchar((char) strtol (begexpr(), NULL, 8)); more();mode(DONE); >>
#token "\\[1-9][0-9]*" 				<< replchar((char) strtol (begexpr(), NULL, 10)); more();mode(DONE); >>
#token "\\(0x|0X)[0-9a-fA-F]+"	<< replchar((char) strtol (begexpr(), NULL, 16)); more();mode(DONE); >>
#token "[\n\r]" 						<< newline(); more(); >>
#token "~['\n\r\\]" 					<< more(); mode(DONE); >>

#lexclass DONE

#token CHARACTER "'" 				<< mode(START); >>

#lexclass START

class Sequencer {

<<
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
>>

pushContext: 
	<<;>>
	<<
		Context *lastCont = currentContext;
		contextStack.push( currentContext );
		currentContext = new Context(lastCont );
	>>
	;

popContext: 
	<<;>>
	<<
		delete currentContext;
		currentContext = contextStack.pop();
	>>
	;

pushSwitch: 	
	<<;>>
	<< switchStack.push( currentSwitch ); currentSwitch = new TransOpSwitch; >>
	pushBlock[currentSwitch]
	;

popSwitch: 	
	<<;>>
	<< currentSwitch = switchStack.pop(); >>
	popBlock
	;

pushBreakableBlock[TransOpBreakableBlock *newBreakableBlock]: 
	<<;>>
	<<
		breakableBlockStack.push( currentBreakableBlock );
		currentBreakableBlock = newBreakableBlock;
	>>
	;

popBreakableBlock: 
	<<;>>
	<< currentBreakableBlock = breakableBlockStack.pop(); >>
	;

pushContinueableBlock[TransOpCLoop *newContinueableBlock]: 
	<<;>>
	<<
		continueableBlockStack.push( currentContinueableBlock );
		currentContinueableBlock = newContinueableBlock;
	>>
	;

popContinueableBlock: 
	<<;>>
	<< currentContinueableBlock = continueableBlockStack.pop(); >>
	;

pushBlock[TransOpBreakableBlock *newBreakableBlock]: 
	<<;>>
	<< blockStack.push( currentBlock ); currentBlock = newBreakableBlock; >>
	;

popBlock: 
	<<;>>
	<< currentBlock = blockStack.pop(); >>
	;

bitor_operator > [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		BITWISEOR				<< $res = new BitOr; $line = $1->getLine(); >>
	;

bitexor_operator > [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		BITWISEXOR				<< $res = new BitXor; $line = $1->getLine(); >>
	;

bitand_operator > [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		AMPERSAND				<< $res = new BitAnd; $line = $1->getLine(); >>
	;

and_operator > [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		LOGAND					<< $res = new LogAnd; $line = $1->getLine(); >>
	;

or_operator > [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		LOGOR						<< $res = new  LogOr; $line = $1->getLine(); >>
	;

equality_operator > [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		NOTEQUAL					<< $res = new RelNotEq; $line = $1->getLine(); >>
	|	EQUAL						<< $res = new RelEq; $line = $1->getLine(); >>
	;

relational_operator 			> [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		LESSTHAN					<< $res = new RelLT; $line = $1->getLine(); >>
	|	GREATERTHAN				<< $res = new RelGT; $line = $1->getLine(); >>
	|	LESSTHANOREQUALTO		<< $res = new RelLE; $line = $1->getLine(); >>
	|	GREATERTHANOREQUALTO	<< $res = new RelGE; $line = $1->getLine(); >>
	;

shift_operator 				> [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		SHIFTLEFT				<< $res = new ShiftLeft; $line = $1->getLine(); >>
	|	SHIFTRIGHT				<< $res = new ShiftRight; $line = $1->getLine(); >>
	;

multiplicative_operator 	> [BinExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		STAR						<< $res = new Mult; $line = $1->getLine(); >>
	|	DIVIDE					<< $res = new Div; $line = $1->getLine(); >>
	|	MOD						<< $res = new Mod; $line = $1->getLine(); >>
	;

additive_operator 			> [BinExpr *res, int line]:
	<< $res = 0;$line = 0;  >>
		PLUS						<< $res = new Plus; $line = $1->getLine(); >>
	|	MINUS						<< $res = new BinMinus; $line = $1->getLine(); >>
	;

unary_operator 				> [UnExpr *res, int line]:
	<< $res = 0; $line = 0; >>
		MINUS						<< $res = new UnMinus; $line = $1->getLine(); >>
	|	ONESCOMPLEMENT			<< $res = new BitNot; $line = $1->getLine(); >>
	|	LOGNOT					<< $res = new LogNot; $line = $1->getLine(); >>
	;

expression							> [Value *res, bool isArray]
	:	assignment_expression>[$res, $isArray]
	;

assignment_expression			> [Value *res, bool isArray]
	:	conditional_expression>[$res, $isArray]
	;

conditional_expression			> [Value *res, bool isArray]
	:	logical_or_expression>[$res, $isArray]
	;

constant_expression				> [Value *res, bool isArray]
	:	conditional_expression>[$res, $isArray]
	;

cast_expression					> [Value *res, bool isArray]
	:	unary_expression>[$res, $isArray]
	;

postfix_expression				> [Value *res, bool isArray]
	:	primary_expression>[$res, $isArray]
	;

primary_expression				> [Value *res, bool isArray]:
	<< $res = 0; $isArray = false; >>
		primary_value>[$res, $isArray]
	|	LPARENTHESIS expression>[$res, $isArray] RPARENTHESIS
	;

logical_or_expression			> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		logical_and_expression>[$res, $isArray] #pragma approx
		(or_operator>[op, line] logical_and_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

logical_and_expression			> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		inclusive_or_expression>[$res, $isArray] #pragma approx
		(and_operator>[op, line] inclusive_or_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;
	
inclusive_or_expression			> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		exclusive_or_expression>[$res, $isArray] #pragma approx
		(bitor_operator>[op, line] exclusive_or_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

exclusive_or_expression			> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		and_expression>[$res, $isArray] #pragma approx
		(bitexor_operator>[op, line] and_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

and_expression						> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		equality_expression>[$res, $isArray] #pragma approx
		(bitand_operator>[op, line] equality_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

equality_expression				> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		relational_expression>[$res, $isArray] #pragma approx
		(equality_operator>[op, line] relational_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

relational_expression			> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		shift_expression>[$res, $isArray] #pragma approx
		( relational_operator>[op, line] shift_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

shift_expression					> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		additive_expression>[$res, $isArray] #pragma approx
		(shift_operator>[op, line] additive_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

/* See comment for multiplicative_expression regarding #pragma */
additive_expression				> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		multiplicative_expression>[$res, $isArray] #pragma approx
		(additive_operator>[op, line] multiplicative_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;
	
/* ANTLR has trouble dealing with the analysis of the confusing unary/binary
 * operators such as STAR, AMPERSAND, PLUS, etc...  With the #pragma
 * we simply tell ANTLR to use the "quick-to-analyze" approximate lookahead
 * as full LL(k) lookahead will not resolve the ambiguity anyway.  Might
 * as well not bother.  This has the side-benefit that ANTLR doesn't go
 * off to lunch here (take infinite hb_time to read grammar).
 */
multiplicative_expression		> [Value *res, bool isArray] : << BinExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		cast_expression>[$res, $isArray] #pragma approx
		(multiplicative_operator>[op, line] cast_expression>[rhs, exprAr]
			<<
				op->LeftOperand = $res; op->RightOperand = rhs; $res = op;
				if ($isArray || exprAr) { ParseError( line, "Can't accecpt array with binary operator" ); $res = badValue( $isArray ); }
			>>
		)*
	;

unary_expression					> [Value *res, bool isArray] : << UnExpr *op; Value *rhs; $res = 0; $isArray = false; bool exprAr = false; int line = 0; >>
		postfix_expression>[$res, $isArray]
	|	unary_operator>[op, line] cast_expression>[$res, exprAr]
			<< 
				if (exprAr) { ParseError( line, "Can't accecpt array with unary operator" ); $res = badValue( $isArray ); }
				op->operand = $res; $res = op;
			>>
	;

/*
primary_expression
	:	IDENTIFIER
	|	IDENTIFIER DOT IDENTIFIER
	|	constant
	|	STRING
	|	LPARENTHESIS expression RPARENTHESIS
	;
*/

argument_expression_list		> [Value *res, bool isArray]
	:	assignment_expression>[$res, $isArray] /* (COMMA assignment_expression)* */
	;

constant > [unsigned long res]:
	<< $res = 0; >>
		OCTALINT			<< $res = rn_strtoul( $1->getText(),0,0 ); >>
	|	DECIMALINT		<< $res = rn_strtoul( $1->getText(),0,0 ); >>
	|	RDSINT
			<<
				bool ok = true;
				$res = pa_getRds( $1->getText(), ok );
				//fprintf( stderr, "%s -> %lu\n", $1->getText(), $res );
				if (!ok) ParseError( $1->getLine(), "Bad RDS number" );
			>>
	|	HEXADECIMALINT	<< $res = rn_strtoul( $1->getText(),0,0 ); >>
	|	CHARACTER		<< $res = *($1->getText()+1); >>
	;

/* ---------------------------------------------------------------------- */

primary_value						>	[Value *res, bool isArray]: 
	<<
		$res = 0;
		$isArray = false;
		Value *expr = 0;
		unsigned long cnst = 0;
		ValueAssign *asgn = 0;
		bool rhsAr = false;
	>>
		konstante>[cnst]
			<<
				ValueConst *tres = new ValueConst;
				tres->value = cnst;
				$res = tres;
			>>
	|	linke_seite>[$res, $isArray]
		{
			Assign expression>[expr, rhsAr]
				<<
					asgn = new ValueAssign;
					asgn->LeftOperand = $res;
					asgn->RightOperand = expr;
					$res = asgn;
				>>
			checkAssignment[asgn, $isArray, rhsAr, $1->getLine(), $res]
				<<
					if (!parseError) {
						if ($isArray != rhsAr) {
							panic( "Should not get here\n" );
						}
					}
				>>
		}
	|	spezial_funktion>[$res]
	;

checkAssignment[ValueAssign *asgn, bool &lhsAr, bool rhsAr, int line, Value *&res]: 
	<<;>>
	<<
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
	>>
	;

/* REBNF: Keep standalone rule atomare_spalte */
atomare_spalte	> [Value *res, bool isArray]: 
	<< $res = 0; $isArray = false; >>
		linke_seite>[$res, $isArray]
	;

seq:
		( eingang_oder_variable )+ Eof
	;

eingang_oder_variable: 
	<< CommonInfo *inf = 0; int line = 0; >>
		Bei Eingang Von datenart[0]>[currentInputDatatype, line]
			<<
				if (pDbgLevel)
					fprintf( stdout, "Line %5d %s:\n",
						line, currentInputDatatype->dk->DkName );
				if (!currentInputDatatype)
					return;
			>>
			pushContext
				pushBlock[ new TransOpBreakableBlock ]
		(
				anweisungsliste
					<<
						if (pDbgLevel>8)
							currentBlock->dump( "" );
						if (!catchTriggerDataType( currentInputDatatype->dk->DataId, currentBlock )) {
							ParseError( line, "Can't keep trigger DA" );
						}
					>>
			| sequenz_festlegung
		)
				popBlock
			popContext
	|	globale_variable_deklarieren
	;

anweisungsliste: 
	<< TransOp *anw = 0; >>
		(
			anweisung>[anw]
				<<{
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
				}>>
		)+
	;

anweisung	> [TransOp *res]: 
	<<
		$res = 0;
		Value *expr = 0, *lhs = 0;
		unsigned long cnst = 0;
		TransOpDoAssign *tres = 0;
		bool lhsAr = false, rhsAr = false;
	>>
		lokale_variable_deklarieren
			<<
				//fprintf( stderr, "At line %d ==>\n", $1->getLine() );
				return (new TransOpNop);
			>>
	|	fehlernachricht_oder_syscmd>[$res]
	|	zusammengesetzte_anweisung>[$res]
	|	monoflop_decl>[$res]
	|	LCURLYBRACE
			pushContext
				pushBlock[new TransOpBreakableBlock]
		anweisungsliste << $res = currentBlock; >>
				popBlock
			popContext
		RCURLYBRACE
	|	SeqState konstante>[cnst]
			<<
				//fprintf( stderr, "At line %d ==>\n", $1->getLine() );
				return (new TransOpNop);
			>>
	|	linke_seite>[lhs, lhsAr] Assign expression>[expr, rhsAr]
			<<
				//fprintf( stderr, "At line %d :=\n", $2->getLine() );
				tres = new TransOpDoAssign;
				tres->value = new ValueAssign;
				tres->value->LeftOperand = lhs;
				tres->value->RightOperand = expr;
				$res = tres;
			>>
		checkAssignment[tres->value, lhsAr, rhsAr, $2->getLine(), ((Value*&)tres->value)]
	|	Break
			<<
				//fprintf( stderr, "At line %d Break\n", $1->getLine() );
				if (!currentBreakableBlock) {
					ParseError( $1->getLine(), "Break must be within switch/while/do-while" );
					$res = new TransOpNop;
				} else {
					TransOpBreak *bres = new TransOpBreak;
					bres->breakBlock = currentBreakableBlock;
					$res = bres;
				}
			>>
	|	Continue
			<<
				//fprintf( stderr, "At line %d Break\n", $1->getLine() );
				if (!currentContinueableBlock) {
					ParseError( $1->getLine(), "Continue must be within while/do-while" );
					$res = new TransOpNop;
				} else {
					TransOpContinue *cres = new TransOpContinue;
					cres->continueBlock = currentContinueableBlock;
					$res = cres;
				}
			>>
	|	Write
			<<
				$res = new TransOpWrite;
				//fprintf( stderr, "At line %d Write\n", $1->getLine() );
			>>
	;

lokale_variable_deklarieren: 
	<<
		hbString id;
		int line = 0;
		Value *expr = 0;
		bool exprAr = false;
		bool isArray = false;
		CommonInfo *inf;
	>>
		Temporaer identifier[id, line]
		{
				LSQUAREBRACKET expression>[expr, exprAr] RSQUAREBRACKET
					<< isArray = true; >>
		}
			<<
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
			>>
	|	datenart[0]>[inf, line] IDENTIFIER
			<<
				fprintf( stderr, "%s[%d]:\n", inputFileName, line );
				fprintf( stderr, "Not implemented: DATENART IDENTIFIER: %s[%d]",
					__FILE__, __LINE__ );
				abort();
			>>
	;

globale_variable_deklarieren: 
	<<
		hbString id;
		int line = 0;
		Value *expr = 0;
		bool exprAr = false;
		bool isArray = false;
	>>
		Global identifier[id, line]
		{
				LSQUAREBRACKET expression>[expr, exprAr] RSQUAREBRACKET
					<< isArray = true; >>
		}
			<<
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
			>>
	;

fehlernachricht_oder_syscmd	> [TransOp *res]: 
	<<
		$res = 0;
		ValueList *args;
		char *fmtStr = 0;
		TransOpWithValueList *tres = 0;
	>>
		(		ErrorMessage	<< tres = new TransOpErrMsg; >>
			|	System			<< tres = new TransOpSysCmd; >>
		)
		LPARENTHESIS string>[fmtStr] opt_arglist>[args] RPARENTHESIS
			<<
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
					ParseError( $2->getLine(),
						"Number of args in format string"
						" does not match number of args" );
					delete $res;
					$res = new TransOpNop;
				}
				else {
					tres->fmtString = fmtStr;
					tres->valueList = args;
					$res = tres;
				}
				}
			>>
	;

string > [char *res]: 
	<< $res = 0; >>
		STRING
			<<
				$res = strdup( $1->getText() + 1 ); $res[ strlen( $res ) - 1] = 0;
			>>
	;

opt_arglist > [ValueList *res]: 
	<< $res = 0; >>	

	|	arglist>[$res]
	;

arglist > [ValueList *res]: 
	<< $res = 0; Value *expr = 0; bool exprAr = false; >>
		COMMA expression>[expr, exprAr]
			<<
				$res = new ValueList; $res->Add( expr );
				if (exprAr)
					ParseError( $1->getLine(), "An array value can't be used as argument" );
			>>
		(
			COMMA expression>[expr, exprAr]
				<<
					$res->Add( expr );
					if (exprAr)
						ParseError( $1->getLine(), "An array value can't be used as argument" );
				>>
		)*
	;

monoflop_decl	> [TransOpMonoflop *res]: 
	<<
		$res = 0;
		bool canRetrigger = false, exprAr = false, exprAr2 = false;
		Value *expr = 0, *expr2 = 0;
		int mf_type = 0;
		TransOp *anwl;
	>>
		monofloptype>[mf_type]
		{ Retriggerbarer << canRetrigger = true; >> }
		Monoflop
		LPARENTHESIS expression>[expr, exprAr]
		{ COMMA expression>[expr2, exprAr2] } RPARENTHESIS
		LCURLYBRACE
			pushContext
				<<
					if (!currentContext->insert( "Ausloesezeitpunkt", &internalAusloesezeitpunkt )) {
						panic ("Can't internally declare Ausloesezeitpunkt\n" );
					}
					if (canRetrigger && !currentContext->insert( "ErsterAusloesezeitpunkt", &internalErsterAusloesezeitpunkt )) {
						panic ("Can't internally declare ErsterAusloesezeitpunkt\n" );
					}
				>>
				pushBlock[new TransOpBreakableBlock]
		anweisungsliste << anwl = currentBlock; >>
				popBlock
			popContext
		RCURLYBRACE
			<<
				TransOpMonoflop *tres;
				if (canRetrigger) {
					TransOpRtrMonoflop *ttres = new TransOpRtrMonoflop;
					ttres->timeout = expr;
					if (expr2) ttres->maxTimeout = expr2;
					$res = ttres;
				} else {
					TransOpMonoflop *ttres = new TransOpMonoflop;
					ttres->timeout = expr;
					if (expr2) {
						ParseError ( $3->getLine(),
							"MaxTimeout not available for non retriggerable monoflop" );
					}
					$res = ttres;
				}
				$res->mfType = mf_type;
				$res->entryDa = currentInputDatatype->dk->DataId;
				$res->action = anwl;
			>>
	;

monofloptype > [int res]: 
	<< $res = 0; >>
		{ Eaweiser }	<< $res = MF_T_EA; >>
	|	Einmaliger		<< $res = MF_T_ONCE; >>
	|	Knotenweiser	<< $res = MF_T_NODE; >>
	|	Clusterweiser	<< $res = MF_T_EA2CL; >>
	;

sequenz_festlegung: 
	<< Value *expr = 0, *spalte = 0; TransOpBreakableBlock *bdy = 0; bool exprAr = false, colAr = false; >>
		Sequence LPARENTHESIS expression>[expr, exprAr] COMMA atomare_spalte>[spalte, colAr] RPARENTHESIS
		LCURLYBRACE
			pushContext
				pushBlock[ new TransOpBreakableBlock ]
		anweisungsliste << bdy = currentBlock; >>
				popBlock
			popContext
		RCURLYBRACE
	;

// - ZdfDatenartSpalte -----------------------------------------------
linke_seite	> [Value *res, bool isArray]: 
	<<
		$res = 0;
		CommonInfo *inf;
		bool have_da = false;
		Value *col = 0; inf = 0;
		int line = 0;
		$isArray = false;
	>>
		variable>[$res, $isArray]
	|	{ datenartelement>[inf, line] DOT << have_da = true; >> }
		spalte[inf, have_da]>[col, $isArray] // nur spalte => eingangsdatenart
			<<
				CommonInfo *dkInf = 0;
				if (have_da&&inf)	dkInf = inf;
				else {
					dkInf = new CommonInfo( *currentInputDatatype );
					dkInf->old = false;
				}
				ValueSeqCommon *tres = new ValueSeqCommon;
				tres->info			= dkInf;
				tres->realValue	= col;
				tres->atomic = !$isArray;
				$res = tres;
				//fprintf( stderr, "OK lhs %d %s\n", line, tres->info->dk->DkName );
			>>
	;

datenartelement > [CommonInfo *res, int line]: 
	<< $res = new CommonInfo; bool realExpl = false; CommonInfo *dc = 0; >>
		(
				atomicEa[$res, realExpl]
			|	nonAtomicEa[$res] { deepenNonAtomic[$res] } In << realExpl = true; >>
		)
		datenart[$res]>[dc, $line]
		(
				DaNew
					<<
						$res->old = false;
						if (realExpl) {
							ParseError( $line, "Explicit addressing not allowd with '-New'" );
						}
					>>
			|	{ DaOld }
		)
	;

atomicEa[CommonInfo *inf, bool realExpl]: 
	<< Value *expr = 0; bool exprAr = false; $realExpl = false; >>
		{ De In <<  $realExpl = true; >> }
	|	Ea LPARENTHESIS expression>[expr, exprAr] RPARENTHESIS In
			<<
				if (inf) {
					inf->theExplicitEaid = expr;
					inf->explicitEaid = true;
					//fprintf( stderr, "explicit eaid(EA) at line %d val=%ld\n", $1->getLine(), inf->theExplicitEaid->GetVal(0) );
				}
				$realExpl = true;
				if (exprAr)
					ParseError( $1->getLine(), "An array value can't be used as explicit Eaid" );
			>>
	;

nonAtomicEa[CommonInfo *inf]: 
		DEs DES
		(
				Cluster << inf->range = rng_cluster2de; >>
			|	Knoten  << inf->range = rng_node; >>
		)
	|	Cluster DES DEs << inf->range = rng_de2cluster; >>
	;

deepenNonAtomic[CommonInfo *inf]: 
	<< CommonInfo *dkInf = 0; int line = 0; bool exprAr = false; Value *expr; >>
		LPARENTHESIS expression>[expr, exprAr]
			<<
				inf->explicitEaid = true;
				inf->theExplicitEaid = expr;
				//fprintf( stdout, "explicit eaid(NA) at line %d\n", $1->getLine() );
				if (exprAr) {
					ParseError( $1->getLine(), "Can't handle array as explicit eaid" );
				}
			>>
		{
			COMMA datenart[0]>[dkInf, line]
				<< inf->explInDa = true; inf->theExplicitDa = dkInf; >>
		}
		RPARENTHESIS
	;

spalte[CommonInfo *infIn, bool have_da] > [Value *res, bool isArray]: 
	<< Value *indx = 0; hbString id; $res = 0; int line = 0; $isArray = false; >>
		identifier[id, line] opt_array_elem>[indx]
			<<
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
						$res = badValue( $isArray );
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
							$res = badValue( $isArray );
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
							$isArray = true;
						}
						$res = tmp_res;
						if (indx && ! badType) {
							if (!colres) {
								ParseError( line, "[] applicable only on array columns");
								$res = badValue( $isArray );
							} else {
								ZdfColumnArrayElem *atmp_res = new ZdfColumnArrayElem;
								atmp_res->index 		= indx;
								atmp_res->col_offset	= colres->col_offset;
								atmp_res->array_base	= colres->array_base;
								atmp_res->col_dim		= colres->col_dim;
								colres->array_base = 0;
								delete tmp_res;
								$res = atmp_res;
								$isArray = false;
							}
						}
					}
				}
			>>
	;

opt_array_elem	> [Value *res]:
	<< $res = 0; bool exprAr = false; >>
	
	|	LSQUAREBRACKET expression>[$res, exprAr] RSQUAREBRACKET
			<<
				if (exprAr) {
					ParseError( $1->getLine(), "An array value can't be used as index" );
					$res = badValue( exprAr );
				}
			>>
	;

datenart[CommonInfo *resIn] > [CommonInfo *res, int line]: 
	<< $res = 0; hbString id; int line = 0;>>
		identifier[id,line]
			<<
				{
					datakind *dk = DkName2DkPtr( id );
					if ( ! dk ) {
						ParseError( line, "Unknown datatype %s", (const char*) id );
					} else {
						if (resIn)	$res = resIn;
						else 			$res = new CommonInfo;
						$res->dk  = dk;
						$line = line;
						if (!catchDataType( dk->DataId )) {
							ParseError( line, "Can't keep DA" );
						}
					}
				}
			>>
	;

identifier [hbString& res, int& line]: 
	<< $line = 0; >>
		IDENTIFIER << $res = $1->getText(); $line = $1->getLine(); >>
	;

variable	> [Value *res, bool isArray]: 
	<<
		$res = 0;
		$isArray = false;
		Value *indx = 0;
		hbString id;
		int line = 0;
		Value *expr = 0;
		bool exprAr = false;
		bool useArray = false;
	>>
		Dollar identifier[id, line]
		{
			LSQUAREBRACKET expression>[expr, exprAr] RSQUAREBRACKET
				<< useArray = true; >>
		}
			<<
				{
				Value *val = currentContext->find( (char*) id.getStr() );
				if (!val) {
					ParseError( $1->getLine(), "Unknown variable: %s", id.getStr() );
					bool egal = false;
					$res = badValue( egal );
				} else {
					if (useArray) {
						if (!val->GetProp().Includes( "Val_LocArr" )) {
							ParseError( $1->getLine(), "Declared variable is no array: %s", id.getStr() );
							bool egal = false;
							$res = badValue( egal );
						} else {
							if (exprAr) {
								ParseError( $1->getLine(), "Can't accept array value as index" );
								bool egal = false;
								$res = badValue( egal );
							}
							$res = new ValueLocalArrayIndex( (ValueLocalArray*) val, expr );
						}
					} else {
						$res = val;
						Property p = val->GetProp();
						if (p.Includes( "Val_LocArr" )) $isArray = true;
					}
				}
				}
			>>
		{ POINTERTO IDENTIFIER opt_array_elem>[indx]
			<< fprintf( stderr, "%s[%d]:\n", inputFileName, $1->getLine() );
				fprintf( stderr, "Not implemented ->identifier. Src: %s[%d]\n", __FILE__, __LINE__ );
				abort(); >>
		}
	;
// - ZdfDatenartSpalte -----------------------------------------------

zusammengesetzte_anweisung	> [TransOp *res]: 
	<< $res = 0; >>
		schleife>[$res]
	|	schleife2>[$res]
	|	if_else>[$res]
	|	switch_anweisung>[$res]
	;

schleife	> [TransOp *res]: 
	<<
		$res = 0;
		Value *expr1 = 0, *expr2 = 0;
		TransOpBreakableBlock *lpBody = 0;
		hbString id;
		int line = 0;
		bool exprAr1 = false, exprAr2 = false;
		TransOpLoop *tres = 0;
	>>
		loop_type>[tres] LPARENTHESIS	identifier[id, line] COMMA
												expression>[expr1, exprAr1] COMMA
												expression>[expr2, exprAr2] RPARENTHESIS
		LCURLYBRACE
			pushContext
				pushBlock[ new TransOpBreakableBlock ]
		anweisungsliste
			<<
				{
					if (exprAr1||exprAr2) {
						ParseError( line, "Loop: Can't accept array value" );
						$res = new TransOpNop;
					} else {
						Value *val = currentContext->find( (char*) id.getStr() );
						if (val) {
							ParseError( line, "Duplicate variable: %s", id.getStr() );
							$res = new TransOpNop;
						} else {
							ValueLocVar *newvar = new ValueLocVar;
							if (!currentContext->insert((char*)id.getStr(), newvar)) {
								ParseError( line, "Can't declare variable: %s", id.getStr() );
								$res = new TransOpNop;
							} else {
								newvar->name = strdup( id.getStr() );
								tres->LoopVar = newvar;
								tres->StartVal = expr1;
								tres->EndVal = expr2;
								tres->transformation = currentBlock;
								$res = tres;
							}
						}
					}
				}
			>>
				popBlock
			popContext
		RCURLYBRACE
	;

loop_type	> [TransOpLoop *res]: 
	<< $res = 0; >>
		IncLoop
			<<
				//fprintf( stderr, "At line %d IncLoop\n", $1->getLine() );
				$res = new TransOpIncLoop;
			>>
	|	DecLoop
			<<
				//fprintf( stderr, "At line %d DecLoop\n", $1->getLine() );
				$res = new TransOpDecLoop;
			>>
	;

schleife2	> [TransOp *res]: 
	<<
		$res = 0;
		Value *expr = 0;
		TransOpCLoopWhile *lpWhile = 0;
		TransOpCLoopDoWhile *lpDoWhile = 0;
		bool exprAr = false;
	>>
		While LPARENTHESIS expression>[expr, exprAr] RPARENTHESIS 
			<<
				lpWhile = new TransOpCLoopWhile;
				lpWhile->checkExpression = expr;
				if (exprAr)
					ParseError( $1->getLine(), "While: Can't accept array value" );
			>>
		LCURLYBRACE
			pushContext
				pushBlock[ lpWhile ]
					pushBreakableBlock[currentBlock]
						pushContinueableBlock[lpWhile]
		anweisungsliste
						popContinueableBlock
					popBreakableBlock
				popBlock
			popContext
		RCURLYBRACE << $res = lpWhile; >>
	|	Do << lpDoWhile = new TransOpCLoopDoWhile; >>
		LCURLYBRACE
			pushContext
				pushBlock[ lpDoWhile ]
					pushBreakableBlock[currentBlock]
						pushContinueableBlock[lpDoWhile]
		anweisungsliste
						popContinueableBlock
					popBreakableBlock
				popBlock
			popContext
		RCURLYBRACE
		While LPARENTHESIS expression>[expr, exprAr] RPARENTHESIS
			<<
				lpDoWhile->checkExpression = expr; $res = lpDoWhile;
				if (exprAr)
					ParseError( $1->getLine(), "Do-While: Can't accept array value" );
			>>
	;

if_else	> [TransOp *res]: 
	<<
		$res = 0;
		Value *expr = 0;
		TransOpBreakableBlock *bl1 = 0, *bl2 = 0;
		bool have_else = false;
		bool exprAr = false;
		//fprintf( stderr, "%s %d\n", __PRETTY_FUNCTION__, __LINE__ );
	>>
		If LPARENTHESIS expression>[expr, exprAr] RPARENTHESIS
			<<
				if (exprAr)
					ParseError( $1->getLine(), "If: Can't accept array value" );
			>>
		LCURLYBRACE
			pushContext
				pushBlock[ new TransOpBreakableBlock ]
		anweisungsliste << bl1 = currentBlock; >>
				popBlock
			popContext
		RCURLYBRACE
		{
			Else
			LCURLYBRACE
				pushContext
					pushBlock[ new TransOpBreakableBlock ]
			anweisungsliste  << bl2 = currentBlock; >>
					popBlock
				popContext
			RCURLYBRACE
				<<
					//fprintf( stderr, "At line %d Else bl1 %p bl2 %p\n", $1->getLine(), bl1, bl2 );
					TransOpIfElse *tres = new TransOpIfElse;
					tres->Condition = expr;
					tres->IfBlock = bl1;
					tres->ElseBlock = bl2;
					$res = tres;
					have_else = true;
				>>
		}
			<<
				if (!have_else) {
					//fprintf( stderr, "At line %d If bl1 %p\n", $1->getLine(), bl1 );
					TransOpIf *tres = new TransOpIf;
					tres->Condition = expr;
					tres->IfBlock = bl1;
					$res = tres;
				}
			>>
	;

switch_anweisung	> [TransOp *res]:  
	<< $res = 0; Value *expr = 0; bool exprAr = false; >>
		Switch
			<<
				//fprintf( stderr, "At line %d Switch\n", $1->getLine() );
			>>
			pushSwitch // makes a new switchblock
		LPARENTHESIS expression>[expr, exprAr] RPARENTHESIS
			<<
				if (exprAr) ParseError( $1->getLine(), "Switch: Can't accept array value" );
				currentSwitch->setExpr( expr); $res = currentSwitch;
			>>
		LCURLYBRACE
			pushContext
				pushBlock[ currentSwitch ]
					pushBreakableBlock[currentSwitch]
		switchanweisungen
					popBreakableBlock
				popBlock
			popContext
		RCURLYBRACE
			popSwitch
	;
	
switchanweisungen: 
		caseliste { default_anweisung }
	|	default_anweisung
	;

caseliste: 
		(
			case_anweisung
		)+
	;

case_anweisung: 
	<< TransOpBreakableBlock *anw = 0; long cnst = 0; >>
		Case konstante>[cnst]
			<<
				//fprintf( stderr, "At line %d Case\n", $1->getLine() );
			>>
		COLON
			pushBlock[new TransOpBreakableBlock]
		anweisungsliste
			<< currentSwitch->addCase( cnst, currentBlock ); >>
			popBlock
	;

default_anweisung: 
	<< TransOpBreakableBlock *anw = 0; >>
		Default COLON
			pushBlock[new TransOpBreakableBlock]
		anweisungsliste
			<<
				//fprintf( stderr, "At line %d Default\n", $1->getLine() );
				currentSwitch->addDefault(currentBlock);
			>>
			popBlock
	;

spezial_funktion > [Value *res]: 
	<< Value *expr1 = 0, *expr2 = 0; $res = 0; bool exprAr1 = false, exprAr2 = false; >>
		RdsLoc LPARENTHESIS
		expression>[expr1, exprAr1] COMMA expression>[expr2, exprAr2]
		RPARENTHESIS
			<<
				if (exprAr1 || exprAr2)
					ParseError( $1->getLine(), "RdsLoc: Can't accept array values" );
				else {
					ValueRdsLoc *tres = new ValueRdsLoc;
					tres->LeftOperand = expr1;
					tres->RightOperand = expr2;
					$res = tres;
				}
			>>
	|	Exists LPARENTHESIS linke_seite>[expr1, exprAr1] RPARENTHESIS
		<<
			ValueExists *tres = new ValueExists;
			tres->operand = expr1;
			$res = tres;
			if (pDbgLevel>3) {
				fprintf( stdout, "EXISTS arg %p:\n", expr1 );
				expr1->GetProp()./*Cdr().*/Dump(); fflush( stdout );
			}
		>>
	;

/* REBNF: Keep standalone rule konstante */
konstante > [unsigned long res]: 
	<< $res = 0; CommonInfo *daInfo = 0; int line = 0; >>
			constant>[$res]
		|	NumEas LPARENTHESIS datenart[0] > [daInfo, line] RPARENTHESIS
				<< $res = getNumEa( daInfo->dk->DataId ); >>
	;
}
