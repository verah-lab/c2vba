#include <stdio.h>
//#include <iostream.h>
//#define RNZDBGFLUSH cdbg.flush()
//extern ostream cdbg;
#include <DbgAreas.h>
#define DEBUGAREA NSEQ_AREA
#define RNZDBGFLUSH fflush(dbg)
#include "rnzdebug.h"

extern FILE *dbg;

#define DbgCtor       DebugA1
#define DbgCtorFine   DebugA2
#define DbgCtorFinest DebugA3
#define DbgDtor       DebugB1
#define DbgDtorFine   DebugB2
#define DbgDtorFinest DebugB3
#define DbgMemf       DebugC1
#define DbgMemfFine   DebugC2
#define DbgMemfFinest DebugC3

#include "DbgAnseq.h"
