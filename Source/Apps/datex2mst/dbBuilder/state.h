#ifndef _STATE_H_
#define _STATE_H_


#include "hbString.h"

enum EBuildState
{
	buildQueued,
	buildStarted,
	configurationParsed,
	savingRecords,
	recordBuildingFinished,
	buildFailed,
	buildAborted,
	finished
};


void initialUpdateStates();


void writeState( const hbString& mstName, unsigned version, EBuildState state, 
				 const hbString& result, time_t = -1  );



#endif
