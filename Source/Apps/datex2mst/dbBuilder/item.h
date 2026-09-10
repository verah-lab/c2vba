#ifndef _ITEM_H_
#define _ITEM_H_

#include "hbValue.h"
#include "math.h"



////////////////////////////////////////////////////////////////////////////////////////////////////
// locKey
////////////////////////////////////////////////////////////////////////////////////////////////////

class item
{
public:
	const hbValue& operator[] ( const hbString& code ) const
	{
		map< hbString, hbValue >::const_iterator it = m_values.find( code );
		if( it != m_values.end() )
			return it->second;

		static hbValue _V;
		return _V;
	}

	hbValue& operator[]( const hbString& code )
	{
		return m_values[ code ];
	}

	friend ostream& operator<<( ostream& os, const item& I );

private:
	map< hbString, hbValue > m_values;
};

typedef list< item > itemLst;


static double epsilon = 0.0001;



////////////////////////////////////////////////////////////////////////////////////////////////////
// locKey
////////////////////////////////////////////////////////////////////////////////////////////////////

class locKey
{
public:
	locKey( const item& I )
	{
		m_locName			= I["ms_name"];				
		m_equipment			= I["ms_equipment"];

   		m_rdsLocTblRef		= I["rds_loc_tbl_ref"];
   		m_rdsLocTblVer		= I["rds_loc_tbl_ver"];

   		m_rdsPrimLocCode	= I["rds_loc_code"];
   		m_rdsSecLocCode		= 0;

   		m_rdsDirection		= I["rds_direction"].asChar();
		m_rdsPrimLocDist	= I["rds_pt_distance"];
		m_rdsSecLocDist		= 0;

   		m_startCoordX		= I["coord_x"].asDouble();
   		m_startCoordY		= I["coord_y"].asDouble();

   		m_endCoordX			= -1;
   		m_endCoordY			= -1;
	}

	bool operator<( const locKey& key ) const
	{
		if( m_locName < key.m_locName )		
			return true;
		if( m_locName > key.m_locName )		
			return false;

		if( m_equipment < key.m_equipment )		
			return true;
		if( m_equipment > key.m_equipment )		
			return false;

		if( m_rdsLocTblRef < key.m_rdsLocTblRef )		
			return true;
		if( m_rdsLocTblRef > key.m_rdsLocTblRef )		
			return false;

		if( m_rdsLocTblVer < key.m_rdsLocTblVer )		
			return true;
		if( m_rdsLocTblVer > key.m_rdsLocTblVer )		
			return false;

		if( m_rdsPrimLocCode < key.m_rdsPrimLocCode )		
			return true;
		if( m_rdsPrimLocCode > key.m_rdsPrimLocCode )		
			return false;

		if( m_rdsSecLocCode < key.m_rdsSecLocCode )		
			return true;
		if( m_rdsSecLocCode > key.m_rdsSecLocCode )		
			return false;

		if( m_rdsDirection < key.m_rdsDirection )		
			return true;
		if( m_rdsDirection > key.m_rdsDirection )		
			return false;

		if( m_rdsPrimLocDist < key.m_rdsPrimLocDist )		
			return true;
		if( m_rdsPrimLocDist > key.m_rdsPrimLocDist )		
			return false;

		if( m_rdsSecLocDist < key.m_rdsSecLocDist )		
			return true;
		if( m_rdsSecLocDist > key.m_rdsSecLocDist )		
			return false;

		if( ( m_startCoordX < key.m_startCoordX ) && ( fabs( m_startCoordX - key.m_startCoordX ) > epsilon ) )		
			return true;
		if( ( m_startCoordX > key.m_startCoordX ) && ( fabs( m_startCoordX - key.m_startCoordX ) > epsilon ) )		
			return false;

		if( ( m_startCoordY < key.m_startCoordY ) && ( fabs( m_startCoordY - key.m_startCoordY ) > epsilon ) )		
			return true;
		if( ( m_startCoordY > key.m_startCoordY ) && ( fabs( m_startCoordY - key.m_startCoordY ) > epsilon ) )		
			return false;

		if( ( m_endCoordX < key.m_endCoordX ) && ( fabs( m_endCoordX - key.m_endCoordX ) > epsilon ) )		
			return true;
		if( ( m_endCoordX > key.m_endCoordX ) && ( fabs( m_endCoordX - key.m_endCoordX ) > epsilon ) )		
			return false;

		if( ( m_endCoordY < key.m_endCoordY ) && ( fabs( m_endCoordY - key.m_endCoordY ) > epsilon ) )		
			return true;
		if( ( m_endCoordY > key.m_endCoordY ) && ( fabs( m_endCoordY - key.m_endCoordY ) > epsilon ) )		
			return false;

		return false;
	}

private:
	hbString	m_locName;
	hbString	m_equipment;
   	hbString	m_rdsLocTblRef;
   	hbString	m_rdsLocTblVer;
   	long		m_rdsPrimLocCode;
   	long		m_rdsSecLocCode;
	char		m_rdsDirection;
	long		m_rdsPrimLocDist;
	long		m_rdsSecLocDist;
	double		m_startCoordX;
   	double		m_startCoordY;
	double		m_endCoordX;
   	double		m_endCoordY;
};



#endif
