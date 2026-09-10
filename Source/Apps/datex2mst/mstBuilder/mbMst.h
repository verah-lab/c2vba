#ifndef _MBMST_H_
#define _MBMST_H_

#include "hbString.h"
#include <list>
using namespace std;


class mbMSTLocation
{
public:
    hbString		m_mst;
	long			m_id;

	hbString		m_d2Id;
	long			m_locId;

	hbString		m_carriageway;

	hbString		m_locName;		
	hbString		m_equipment;

   	hbString		m_rdsLocTblRef;
   	hbString		m_rdsLocTblVer;

   	long			m_rdsPrimLocCode;
   	long			m_rdsSecLocCode;

   	hbString		m_rdsDirection;
   	long			m_rdsPrimLocDist;
   	long			m_rdsSecLocDist;

   	long			m_startCoordX;
   	long			m_startCoordY;

   	long			m_endCoordX;
   	long			m_endCoordY;
};

typedef map<long,mbMSTLocation> mbMSTLocations;


class mbMSTSource
{
public:
    hbString		m_mst;
   	long			m_id;	

    hbString		m_datakind;
    hbString		m_valueCol;
    hbString		m_valueColCharacteristic;
   	unsigned		m_validInterval;

	hbString		m_idColumn;
	hbString		m_validColumn;
	hbString		m_invalidValues;

   	hbString		m_d2VehType;
   	hbString		m_d2DataType;
	hbString		m_d2BasicDataType;
	hbString		m_d2ValueElement;
	hbString		m_d2Value;
	hbString		m_d2ValueInner;
	hbString		m_d2ValuePath;

	hbString		m_mappingType;
	double			m_mappingFactor;
	hbString		m_valueDataType;

	int				m_period;
	hbString		m_filterColumn;
	list<hbString>  m_filterValues;

	hbString		toStringShort() const
	{
		return "Datakind '" + m_datakind + "', Column '" + m_valueCol + "'";
	};
};

typedef list<mbMSTSource> mbMSTSources;



class mbMSTItem
{
public:
    hbString		m_mst;
   	long			m_id;	

   	long			m_index;

    hbString		m_name; 
	hbString		m_carriageway;
    hbString		m_lane;     
   	unsigned		m_period;       
   	
   	long			m_dkRef;        
   	long			m_dkId;         
   	long			m_locId;        
};

typedef list<mbMSTItem> mbMSTItems;


#endif

