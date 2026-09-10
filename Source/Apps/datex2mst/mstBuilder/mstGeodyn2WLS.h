#ifndef _MSTGEODYN2WLS_H_
#define _MSTGEODYN2WLS_H_

#include "hbString.h"
#include <list>
using namespace std;

#include "geodyn2WLSService.h"
using namespace geodyn2WLSService;

#include "CORBAObjectHandle.h"
#include "CORBAObjectFactory.h"
#include "loc_man.h"


class RoadElementPosition
{
public:
	long m_reId;
	long m_offset;
};

class mstGeodyn2WLS
{
public:
	void connect( ddpConnection* connection,
				  locConnection* locConnection,
				  const hbString& geodyn2WLSAddress );

	bool connected();

	static mstGeodyn2WLS* get_mstGeodyn2WLS()
	{
		if( !_instance )
		{
			_instance = new mstGeodyn2WLS();
		}
		return _instance;
	}

	void getRdsInfo( const hbString& objType, 
					 const list<long> locIds,
					 map<long,locRDSInfo>& rdsInfos );
private:
	void  getMatchingREPositions( const hbString& objType, 
							      const hbString& matchingType,
							      const list<long>& IDs,
								  map<long,RoadElementPosition>& positions );


private:
	mstGeodyn2WLS() { m_serviceHandle = 0; }
	static mstGeodyn2WLS* _instance;

	ddpConnection*									m_connection;
	locConnection*									m_locConnection;
	CORBAObjectHandle<geodyn2WLSService::Service>	m_serviceHandle;
};

#define MST_GEODYN2WLS (*(mstGeodyn2WLS::get_mstGeodyn2WLS()))


#endif
