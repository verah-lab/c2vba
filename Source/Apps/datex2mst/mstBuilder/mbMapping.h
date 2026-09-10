#ifndef _MBMAPPING_H_
#define _MBMAPPING_H_

#include "hbString.h"
#include <list>
using namespace std;


class mbColumn
{
public:
	bool valid() const
	{
		return !m_valColumn.empty();
	}

	hbString m_valColumn;
	hbString m_valColumnDescription;
	hbString m_valColumnCharacteristic;
	hbString m_valColumnCharacteristicDescription;
	hbString m_validColumn;
	list<hbString> m_invalidValues;
	unsigned m_defaultValidInterval;
};

enum mbReferenceTypeEnum
{
	mbNone,
	mbTLS,
	mbGEODYN2_REF,
	mbGEODYN2_REF_NON_Q,
	mbTLS_Q_COORD_SUPPLY
};

mbReferenceTypeEnum toReferenceTypeEnum( const hbString& dtStr );


class mbDatakind
{
public:
	bool valid() const
	{
		return !m_name.empty();
	}

	void addColumn( mbColumn C )
	{
		m_columns.push_back( C );
	}

	const mbColumn& getColumn( const hbString& colName ) const
	{
		list<mbColumn>::const_iterator itc;
		for( itc = m_columns.begin(); itc != m_columns.end(); ++itc )
		{
			if( itc->m_valColumn == colName )
				return *itc;
		}

		static mbColumn _mbc;

		return _mbc;
	}

	hbString			m_name;
	hbString			m_idColumn;
	hbString			m_description;
	hbString			m_equipment;
	mbReferenceTypeEnum	m_referenceType;
	hbString			m_objectType;
	list<hbString>		m_subTypes;
	list<mbColumn>		m_columns;
	unsigned			m_defaultValidInterval;
};


class mbMappingSource
{
public:
	hbString m_valColumn;
	hbString m_valColumnCharacteristic;
};


enum d2MeasuredOrDerivedDataTypeEnum
{
	mbDTUnknown,
	mbHumidityInformation,
	mbIndividualVehicleMeasurements,
	mbPollutionInformation,
	mbPrecipitationInformation,
	mbPressureInformation,
	mbRadiationInformation,
	mbRoadSurfaceConditionInformation,
	mbTemperatureInformation,
	mbTrafficConcentration,
	mbTrafficFlow,
	mbTrafficHeadway,
	mbTrafficSpeed,
	mbTrafficStatusInformation,
	mbTravelTimeInformation,
	mbVisibilityInformation,
	mbWindInformation
};

d2MeasuredOrDerivedDataTypeEnum toMeasuredOrDerivedDataTypeEnum( const hbString& dtStr );

hbString toString( d2MeasuredOrDerivedDataTypeEnum moddt );



enum d2VehicleTypeEnum
{
	mbOther,
	mbAgriculturalVehicle,
	mbAnyVehicle,
	mbArticulatedVehicle,
	mbBicycle,
	mbBus,
	mbCar,
	mbCaravan,
	mbCarOrLightVehicle,
	mbCarWithCaravan,
	mbCarWithTrailer,
	mbConstructionOrMaintenanceVehicle,
	mbFourWheelDrive,
	mbHighSidedVehicle,
	mbLorry,
	mbMoped,
	mbMotorcycle,
	mbMotorcycleWithSideCar,
	mbMotorscooter,
	mbTanker,
	mbThreeWheeledVehicle,
	mbTrailer,
	mbTram,
	mbTwoWheeledVehicle,
	mbVan,
	mbVehicleWithCatalyticConverter,
	mbVehicleWithoutCatalyticConverter,
	mbVehicleWithCaravan,
	mbVehicleWithTrailer,
	mbWithEvenNumberedRegistrationPlates,
	mbWithOddNumberedRegistrationPlates
};

hbString toString( d2VehicleTypeEnum vt );

d2VehicleTypeEnum toVehicleTypeEnum( const hbString& vtStr );


class mbMappingTarget
{
public:
	d2MeasuredOrDerivedDataTypeEnum m_valueType;
	d2VehicleTypeEnum				m_vehType;
	hbString						m_basicDataType;
	hbString						m_valueElement;
	hbString						m_value;
	hbString						m_valueInner;
	hbString						m_valuePath;
};



enum mbMappingTypeEnum
{
	mbCopy,
	mbConstMultiplication,
	mbPeriod2HourWeighted,
	mbFunction
};



class mbMapping
{
public:
	bool valid() const
	{
		return !m_src.m_valColumn.empty();
	}

	mbMappingSource		m_src;
	mbMappingTypeEnum	m_mappingType;
	hbString			m_mappingFunction;
	double				m_factor;
	hbString			m_valueDataType;
	mbMappingTarget		m_trg;
};

class mbDatakindMapping
{
public:
	void addMapping( mbMapping dkm )
	{
		m_mappings.push_back( dkm );
	}

	const list<mbMapping>& getMappings() const
	{
		return m_mappings;
	}

	mbDatakind			m_datakind;
	list<mbMapping>		m_mappings;
};


class ddpConnection;

class mbDatakindMappings
{
public:
	void check( ddpConnection* conn );

	void addDkMapping( mbDatakindMapping dkm )
	{
		m_dkMappings.push_back( dkm );
	}

	bool hasMapping( const hbString& datakind,
		             const hbString& column, 
					 const hbString& columnCharacteristic ) const;

	const mbMapping& getMapping( const hbString& datakind,
								 const hbString& column, 
								 const hbString& columnCharacteristic );

	mbReferenceTypeEnum getRefType( const hbString& datakind ) const;
	hbString getObjType( const hbString& datakind ) const;

	const mbDatakind& getDatakind( const hbString& dkName );
	const mbColumn& getColumn( const hbString& datakind,
						const hbString& column, 
						const hbString& columnCharacteristic ) const;


	const list<mbDatakindMapping>& getDkMappings() const
	{
		return m_dkMappings;
	}
private:
	list<mbDatakindMapping>	m_dkMappings;
};


mbMappingTypeEnum toMappingTypeEnum( const hbString& mmStr );
hbString toString( mbMappingTypeEnum mt );


#endif

