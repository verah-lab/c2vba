#include <hbtimewrap.h>
#ifndef I_DBGADLCK_H
#define I_DBGADLCK_H

#define dinfo (char *[])

#define DebugInfo(x) DMSetDebugText(x, TXT_##x);
#define DebugAnswer(x) DMSetAnswer( ANS_CODE_##x, ANS_FUNC_##x, ANS_DESC_##x );

/* -  1 - AREA_Global ------------------------------------------------------- */
#define AREA_DaInfo 1
#define TXT_AREA_DaInfo dinfo { \
   "Global area/AREA_Global",   \
   "  A: Action execution",     \
   "     1 Print DA",           \
   "     3 Dump Action",        \
   0 }

/* -  2 - AREA_ZdfIn -------------------------------------------------------- */
#define AREA_ZdfIn 2
#define TXT_AREA_ZdfIn dinfo {                                  \
   "ZDF input area/AREA_ZdfIn",                                 \
   "  Values are interpreted as level and not as subareacodes", \
   "  0: Debug off",                                            \
   "  1: Merely the time an ZDF answer got in",                 \
   "  2: 1 plus display the number of each datatype,",          \
   "     the number of received EAs, the flags and wether",     \
   "     the data was reveived in msg or in file",              \
   "  3: 2 plus a hexdump for each id",                         \
   "     or the information for the file",                      \
   0 }

/* -  1 - AREA_Distr -------------------------------------------------------- */
#define AREA_Distr 3
#define TXT_AREA_Distr dinfo {                                 \
   "Distribution area/AREA_Distr",                             \
   "  A: Timeout for nodes(as result of a start of a server)", \
   "  B: Timeout for allows",                                  \
   "  C: Writing of SchaltOption/internal queueing",           \
   "  D: Birth of a node(possible target for data)",           \
   "  E: Death of a node",                                     \
   "  G: Status changes of nodes/DaSYSFehlerDUE",              \
   "  H: Receiving commands Start Server/End Server",          \
   0 }

// timer/timeout
// Ziel up/down

/* - Debuganswer startmtrace/MTRACE ----------------------------------------- */
#define ANS_CODE_MTRACE dinfo { \
   "StartMtrace",               \
   "startmtrace",               \
   0 }
#define ANS_DESC_MTRACE dinfo { \
   "dynlock: start mtrace",     \
   0 }
#define ANS_FUNC_MTRACE StartMtrace

/* - Debuganswer distribution/DISTR ----------------------------------------- */
#define ANS_CODE_DISTR dinfo { \
   "Distribution",             \
   "distribution",             \
   0 }
#define ANS_DESC_DISTR dinfo {                    \
   "dynlock: Show current distribution settings", \
   0 }
#define ANS_FUNC_DISTR infoDistr

/* - Debuganswer ForceGC/forcegc -------------------------------------------- */
#define ANS_CODE_FORCEGC dinfo { \
   "ForceGC",                    \
   "forcegc",                    \
   0 }
#define ANS_DESC_FORCEGC dinfo {                     \
   "dynlock: Force garbage collection of Pools", \
   0 }
#define ANS_FUNC_FORCEGC forceLPGarbageCollection

#define CCNOW shorttime( hb_time( 0L ) ) << " "

#endif

