#ifndef _D2CONF_H_
#define _D2CONF_H_

#include <set>
using namespace std;

#include "hbString.h"


//////////////////////////////////////////////////////////////////////////////////////////////
// d2MST
//////////////////////////////////////////////////////////////////////////////////////////////

class d2MST
{
public:
	d2MST( const hbString& name, const hbString& descr, 
           unsigned version, const hbString& def = hbString() ) :
		m_name( name ),
		m_descr( descr ),
		m_version( version ),
		m_def( def )
	{
	}

	d2MST() {}

	hbString getName() const { return m_name; }
	hbString getDescr() const { return m_descr; }
	unsigned getVersion() const { return m_version; }
	hbString getDef() const { return m_def; }

	void setDef( const hbString& def )
	{
		m_def = def;
	}
private:
	hbString m_name;
	hbString m_descr;
	unsigned m_version;
	hbString m_def;
};



//////////////////////////////////////////////////////////////////////////////////////////////
// d2RoadFilter
//////////////////////////////////////////////////////////////////////////////////////////////

class d2RoadFilter
{
public:
	d2RoadFilter( const hbString& name, char dir, 
				  long startOffs, long endOffs ) :
		m_name( name ),
		m_dir( dir ),
		m_startOffset( startOffs ),
		m_endOffset ( endOffs )
	{
	}

	d2RoadFilter() {}

	hbString getName() const { return m_name; }
	char getDirection() const { return m_dir; }
	long getStartOffset() const { return m_startOffset; }
	long getEndOffset() const { return m_endOffset; }

private:
	hbString m_name;
	char m_dir;
	long m_startOffset;
	long m_endOffset;
};

typedef list< d2RoadFilter > d2RoadFilterLst;



//////////////////////////////////////////////////////////////////////////////////////////////
// d2ObjNameFilter
//////////////////////////////////////////////////////////////////////////////////////////////

typedef list< hbString > hbStringLst;

class d2ObjNameFilter
{
public:
	d2ObjNameFilter( const hbString& name, const hbStringLst& laneIds ) :
		m_name( name ),
		m_laneIds( laneIds )
	{

	}

	d2ObjNameFilter() {}


	hbString getName() const { return m_name; }
	hbStringLst getLaneIds() const { return m_laneIds; }
private:
	hbString		m_name;
	hbStringLst		m_laneIds;
};

typedef list< d2ObjNameFilter >		d2ObjNameFilterLst;
typedef set< hbString >				d2ObjNameSet;	


//////////////////////////////////////////////////////////////////////////////////////////////
// d2DkDef
//////////////////////////////////////////////////////////////////////////////////////////////

class d2DkAttrDef
{
public:
	d2DkAttrDef( 
				const hbString& datakindName,
				const hbString& datakindIdCol,
				const hbString& datakindValueCol,
				const hbString& datakindStateCol,
				const hbStringLst& datakindInvalidVals,
				const hbString& vehicleClass,
				const hbString& valueUnit, 
				long validInterval
			   ) :
		m_datakindName( datakindName ),
		m_datakindIdCol( datakindIdCol ),
		m_datakindValueCol( datakindValueCol ),
		m_datakindStateCol( datakindStateCol ),
		m_datakindInvalidVals( datakindInvalidVals ),
		m_vehicleClass( vehicleClass ),
		m_valueUnit( valueUnit ),
		m_validInterval( validInterval )
	{
	}

	d2DkAttrDef() {}

	hbString	getDatakindName() const { return m_datakindName; }
	hbString	getDatakindValueCol() const { return m_datakindValueCol; }
	hbString	getDatakindIdCol() const { return m_datakindIdCol; }
	hbString	getDatakindStateCol() const { return m_datakindStateCol; }
	hbStringLst getDatakindInvalidVals() const { return m_datakindInvalidVals; }
	hbString	getVehicleClass() const { return m_vehicleClass; }
	hbString	getValueUnit() const { return m_valueUnit; }
	long		getValidInterval() const { return m_validInterval; };

private:
	hbString	m_datakindName;
	hbString	m_datakindIdCol;
	hbString	m_datakindValueCol;
	hbString	m_datakindStateCol;
	hbStringLst m_datakindInvalidVals;
	hbString	m_vehicleClass;
	hbString	m_valueUnit;
	long		m_validInterval;
};

typedef list< d2DkAttrDef > d2DkAttrDefLst;



#endif
