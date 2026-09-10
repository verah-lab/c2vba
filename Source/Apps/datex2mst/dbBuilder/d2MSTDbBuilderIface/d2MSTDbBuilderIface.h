// ******************************************************************************************
//
//	Author   : P. Schmitz/Heusch-Boesefeldt GmbH
//	Kommentar: 
//			   
//
// ******************************************************************************************
#ifndef _D2MSTDBBUILDERIFACE_H_
#define _D2MSTDBBUILDERIFACE_H_


#ifdef WIN32

#ifdef _DEBUG
#		ifdef USE_STLPORT
#			pragma comment(lib,"d2MSTDbBuilderIfaces_d.lib")
#		else
#			pragma comment(lib,"d2MSTDbBuilderIface_d.lib")
#		endif
#else
#		ifdef USE_STLPORT
#			pragma comment(lib,"d2MSTDbBuilderIfaces.lib")
#		else
#			pragma comment(lib,"d2MSTDbBuilderIface.lib")
#		endif
#endif

#endif



#include "d2MSTDbBuilderIfaceC.h"
#include "d2MSTDbBuilderIfaceS.h"
#include "d2MSTDbBuilderIfaceS_T.h"


#endif
