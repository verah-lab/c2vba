#ifndef _TRMISC_H_
#define _TRMISC_H_




#include <stdio.h>
#include <string>
using namespace std;


#include "dbg_area_def.h"


extern "C" {

#ifdef DEBUGAREA
#undef DEBUGAREA
#endif
#define DEBUGAREA 1

#ifdef LOGGINGAREA
#undef LOGGINGAREA
#endif
#define LOGGINGAREA 1


#ifdef DEBUG
#undef DEBUG
#endif
#define DEBUG
#include "rnzdebug.h"
}

#include "hbPrefix.h"
#include <time.h>
#include "hbLog.h"
#include "hbString.h"


void throwErrorMsg( const list<string>& errTexts );

#ifdef WIN32
inline hbLogStream& operator<<( hbLogStream& os, hbPrefixT& p )
{
	(ostream&)os << p;

	return os;
}
#endif

class d2MSTException : public exception
{
public:
	d2MSTException( long errCode, const hbString& errMsg ) :
		m_errCode( errCode ), m_errMsg( errMsg )
	{
	}

	virtual ~d2MSTException()
#if __GNUC__ >= 3
	throw ()
#endif
	{}

	long getErrCode() const { return m_errCode; }
	hbString getErrMsg() const { return m_errMsg; }

private:
	long		m_errCode;
	hbString	m_errMsg;
};

inline ostream& operator<<(ostream& os, const d2MSTException& ex )
{
	os << "d2MSTException: " << "Code: " << ex.getErrCode() << ", Msg: " << ex.getErrMsg();

	return os;
}


#define D2_MST_ERR_UNKNOWN_UNIT_CODE		1001
#define D2_MST_ERR_UNKNOWN_VEH_CLASS		1002
#define D2_MST_ERR_INVALID_MST_DEF   		1003
#define D2_MST_ERR_INVALID_LOC_FILTER		1004
#define D2_MST_ERR_CREATE_DB_MST			1005
#define D2_MST_ERR_HB_EXCEPTION  			1006


#endif

