/****************************************************************************/
/*
   Project      TOSCA 

   File:        tr_err.h   
   Author:      Peter Schmitz                  
   Comment:     TRAILS out

   Date        |   Comment
   ----------------------------------------------------
   25.04.1999  |   initial version
               |      

*/ 
/****************************************************************************/

#ifndef _TR_ERR_H_
#define _TR_ERR_H_



#ifndef DEBUG
#define DEBUG
#endif

#define LOGGING
#define LOGGINGAREA 1



#include "hbLog.h"
#include "hbError.h"
#include "hbJournal.h"

#include "psError.h"

#define NumErrExts 3



class TR_Exception

{
public:
	enum errCode
	{
		ERR_UNKNOWN_VALUE_TYPE = 10001,
		ERR_VALUE_TYPE_MISMATCH = 10002,
		ERR_MAX_VALUE_DIFF_TYPE = 10003
	};
	
	static psErrorExplText ErrExts[NumErrExts];
	
	static void init()
	{
		psErrorT::extendErrorSet( NumErrExts , &(ErrExts[0]) );
	}
};



#endif


