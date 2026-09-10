#ifndef _MSTLOCMATCH_H_
#define _MSTLOCMATCH_H_

#include "hbString.h"
#include <list>
using namespace std;

#include "mstBuilderLib.h"
using namespace mstBuilder;

#include "loc_man.h"


class locLocation;

class mstLocMatch
{
public:
	bool init( const mstObjFilters& objFilters, 
			   const mstRoadFilters& roadFilters,
			   hbString& errTxt  );
	
	bool initLocClass( const hbString& locClass,
					   hbString& errTxt );

	bool match( long id, const hbString& locName );
	bool matchName( const hbString& locName );

	bool matchExcluded( long id, const hbString& locName );

	static mstLocMatch* get_mstLocMatch()
	{
		if( !_instance )
			_instance = new mstLocMatch();
		return _instance;
	} 

	static void setLocConnection( locConnection* locConnection )
	{
		m_locConnection = locConnection;
	}
private:
	mstLocMatch() {}
	static mstLocMatch* _instance;

	static locConnection* m_locConnection;
	
	list<locLocation>	m_locList;
	locLocation			m_roadLoc;
	mstObjFilters		m_objFilters;
	set<hbString>		m_objNameSet;

	set<long>			m_notMatchingLocIds;

	hbString			m_locClass;
	set<long>			m_matchingLocIds;
};


#define MST_LOC_MATCH (*(mstLocMatch::get_mstLocMatch()))


#endif
