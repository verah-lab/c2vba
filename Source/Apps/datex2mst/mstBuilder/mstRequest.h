#ifndef _MSTREQUEST_H_
#define _MSTREQUEST_H_

#include "hbString.h"
using namespace std;


class mstRequest
{
public:
	mstRequest( const hbString& mstId, const hbString& mstVersion, 
		        const hbString& defVersion, const hbString& refVersion ) :
			m_mstId( mstId ), m_mstVersion( mstVersion ), 
			m_defVersion( defVersion ), m_refVersion( refVersion )
	{
	}

	mstRequest()
	{
	}

public:
	const hbString& getMstId() const { return m_mstId; }
	const hbString& getMstVersion() const { return m_mstVersion; }
	const hbString& getDefVersion() const { return m_defVersion; }
	const hbString& getRefVersion() const { return m_refVersion; }

private:
    hbString		m_mstId;
	hbString		m_mstVersion;

	hbString		m_defVersion;
	hbString		m_refVersion;
};


#endif

