grammar Receiver;

receiver : statement+ EOF ;

statement : deblockdef | locationList | targetTypeList;

locationList : 'LOCATIONS' STRING locList ;

locList : location ( ',' location )* ;

location : INT '-' INT ;

targetTypeList : 'TARGET_TYPES_ALLOWED' '(' typeList ')';

typeList : type ( ',' type )* ;

type : string ;

deblockdef : 'Datatype' string 'Fg' zahlenliste 'Id' zahlenliste 'Typ' zahl (header| atLocations | notAtLocations )? getter+ ;

atLocations : '@' STRING ;

notAtLocations : '-@' STRING ;

header : 'HEADER' ;

getter :
		  skipGetter 		// a getter for skipping bytes
		| singleValueGetter optTargetType?

		| stringGetter optTargetType? // a getter for strings
		| blockGetter optTargetType? // a getter for byte arrays (block)
		| arrayGetter optTargetType? // a getter for arrays of any kind
		| arrayGetter2 optTargetType? // a getter for arrays of any kind
		| ifGetter          // a getter checking a condition
		| optionalGetter	// a getter checking if there is more data available
		;

singleValueGetter	:
		  intGetter			// a get for integer values
        | bcdGetter         // a getter for BCD coded values
		| timeGetter 		// a getter for time stamps
		| floatGetter 		// a getter for floating point numbers
		| setGetter         // a getter setting a value from an expression without consuming bytes
					;

intGetter : optEndian? optSigned? intGettertype (expression|function)? string;

intGettertype : // dies füllt automatisch eine variable $?
             'BYTE'  |		// get a single byte
             'SHORT' | 		// get two bytes
             'NODE'  |		// get 3 bytes (usually used for node numbers)
             'LONG'  		// get 4 bytes
             ;

bcdGetter : 'BCD' zahl (expression|function)? string ;

floatGetter : 'FLOAT' (expression|function)? string;

timeGetter  : 'TIME' string string ;

optTargetType : 'AS' '(' string ')';

// === Arrays and Blocks =================================================================================

arrayGetter : 'ARRAY' '[' getter+ ']' string;

arrayGetter2 : 'ARRAY' '(' string ')' '[' getter+ ']' string;

stringGetter: (stringVarSize | stringFixSize | stringToEnd | stringVarSizeWithSizeCol) string;

stringVarSize: 'STRING' withSize ;
stringVarSizeWithSizeCol: 'STRING' withSize2 ;
stringFixSize: 'STRING' fixedSize ;
stringToEnd: 'STRING' toEnd ;

blockGetter: (blockVarSize | blockFixSize | blockToEnd | blockWithSizeCol ) string | blockToEndSkip;

blockVarSize: 'BLOCK' withSize ;
blockFixSize: 'BLOCK' fixedSize ;
blockToEnd: 'BLOCK' toEnd ;
blockToEndSkip: 'BLOCK' toEndSkip ;
blockWithSizeCol: 'BLOCK' withSize2 ;

withSize : 'with' 'size' ;
withSize2 : 'with' 'size' '(' string ')';
fixedSize : 'fixed' 'size' zahl ;
toEnd : 'to' 'end' ;
toEndSkip : 'to' 'end' 'skip' ;

// === Functions ======================================================================================================

function : functionname args ;

functionname : 'inval'
             ;

args : '(' arglist ')' ;

arglist : argument ( ',' argument )* ;

argument : expression ;

// === Specials =======================================================================================================

skipGetter : 'SKIP' ;

setGetter : 'SET' string ':=' ( expression | function) ;

optionalGetter : 'OPT' '{' getter+ '}' ;

ifGetter : 'IF' condition '{' getter+ '}' optElse? ;

optElse : 'ELSE' '{' getter+ '}' ;

condition : '(' expression ')' ;

// === Expression ======================================================================================================

expression : string | zahl | zahlenlisteExpr | expression operator expression | operationWithParenthesis ;

operationWithParenthesis : '(' expression operator expression ')' ;

operator : '==' | '!=' | '/' | '*' | '+' | '-' | '&&' | '||' | '&' | '|' | '>>' | '<<' | '>' | '>=' | '<' | '<=';

// in TLS this defaults to little endian
optEndian : ('little' | 'big') 'endian' ;

// in TLS this defaults to unsigned
optSigned : 'signed' | 'unsigned' ;

optDiv : 'DIV' zahl;

zahlenlisteExpr : '[' zahlenliste ']' ;

zahlenliste : zahl | wertebereich | zahlenliste ',' zahlenliste ;

wertebereich : zahl '-' zahl ;

zahl : INT | HEXNUM ;

string : STRING | QUOTE ;

//COMMENT :  '//' ~( '\r' | '\n' )* -> skip ;

BlockComment
    :   '/*' .*? '*/'
        -> skip
    ;

LineComment
    :   '//' ~[\r\n]*
        -> skip
    ;

STRING	: ('a'..'z' | 'A'..'Z' | '_' | '$' | '#' ) ('a'..'z' | 'A'..'Z' | '_' | '-' | '0'..'9')*
        | '$' '?'
        ;

//NAME	: ('a'..'z' | 'A'..'Z' | '_' ) ('a'..'z' | 'A'..'Z' | '_' | '-' | '0'..'9')* ;

QUOTE :  '"' (ESC | ~["\\])* '"' ;
fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

INT :   '0' | [1-9] [0-9]* ;

HEXNUM : '0x' [0-9a-fA-F] [0-9a-fA-F]* ;

WS  :   [ \t\n\r]+ -> skip ;
