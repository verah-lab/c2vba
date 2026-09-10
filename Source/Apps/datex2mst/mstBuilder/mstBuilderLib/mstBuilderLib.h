// ******************************************************************************************
//
//	Author   : P. Schmitz/Heusch-Boesefeldt GmbH
//	Kommentar: 
//			   
//
// ******************************************************************************************
#ifndef _MSTBUILDERLIB_H_
#define _MSTBUILDERLIB_H_


#ifdef WIN32

#ifdef _DEBUG
#		pragma comment(lib,"mstBuilderLib_d.lib")
#else
#		pragma comment(lib,"mstBuilderLib.lib")
#endif

#endif



#include "mstBuilderC.h"
#include "mstBuilderS.h"
#include "mstBuilderS_T.h"


#endif
