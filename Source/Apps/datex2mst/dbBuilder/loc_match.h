#ifndef _LOC_MATCH_H_
#define _LOC_MATCH_H_

#include <string>
#include <list>
using namespace std;


#include "d2Conf.h"




class locLocation;

class loc_match

{
public:
	bool init( const d2RoadFilterLst& roadFilter, const d2ObjNameFilterLst& objNameFilter );
	
	bool match( long id, const hbString& locName );

	const d2ObjNameFilterLst& getObjNameFilterLst() const
	{
		return m_objNameFilter;
	}
	
	static loc_match* get_loc_match()
	{
		if( !_instance )
			_instance = new loc_match();
		return _instance;
	} 
private:
	loc_match() {}
	static loc_match* _instance;
	
	list<locLocation*>	m_locList;

	d2ObjNameFilterLst	m_objNameFilter;
	d2ObjNameSet		m_objNameSet;
};


#define LOC_MATCH (*(loc_match::get_loc_match()))


#endif
