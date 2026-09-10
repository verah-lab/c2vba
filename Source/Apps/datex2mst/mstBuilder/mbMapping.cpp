#ifdef WIN32
#pragma warning (disable : 4786)
#endif


#include "mstBuilderLib.h"
using namespace mstBuilder;


#include "mbMapping.h"
#include "ddp.h"



#define MB_ERR_UNKNOWN_DK   2001


void mbDatakindMappings::check( ddpConnection* conn )
{
	list<mbDatakindMapping>::const_iterator itm;

	for( itm = m_dkMappings.begin(); itm != m_dkMappings.end(); ++itm )
	{
		const mbDatakindMapping& dkm = *itm;
		hbString dk = dkm.m_datakind.m_name;
		if( !conn->hasDatakind( dkm.m_datakind.m_name ) )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_UNKNOWN_DK;
			ex.description = hbString( "Unknown GeoDyn2 datakind " + dkm.m_datakind.m_name ).c_str();
			ex.source = hbString("mbDatakindMappings::check()").c_str();

			throw ex;
		}

		if( !conn->datakind( dk ).hasColumn( dkm.m_datakind.m_idColumn ) )
		{
			mstBuilderException ex;
			ex.code = MB_ERR_UNKNOWN_DK;
			ex.description = hbString( "Unknown GeoDyn2 datakind column " + dk + "::" + dkm.m_datakind.m_idColumn ).c_str();
			ex.source = hbString("mbDatakindMappings::check()").c_str();
		}

		const list<mbColumn>& cs = dkm.m_datakind.m_columns;
		list<mbColumn>::const_iterator itc;
		for( itc = cs.begin(); itc != cs.end(); ++itc )
		{
			const mbColumn& C = *itc;
			if( !conn->datakind( dk ).hasColumn( C.m_valColumn ) )
			{
				mstBuilderException ex;
				ex.code = MB_ERR_UNKNOWN_DK;
				ex.description = hbString( "Unknown GeoDyn2 datakind column " + dk + "::" + C.m_valColumn ).c_str();
				ex.source = hbString("mbDatakindMappings::check()").c_str();
			}
			if( !conn->datakind( dk ).hasColumn( C.m_validColumn ) )
			{
				mstBuilderException ex;
				ex.code = MB_ERR_UNKNOWN_DK;
				ex.description = hbString( "Unknown GeoDyn2 datakind column " + dk + "::" + C.m_validColumn ).c_str();
				ex.source = hbString("mbDatakindMappings::check()").c_str();
			}
		}

		const list<mbMapping>& ms = dkm.getMappings();
		list<mbMapping>::const_iterator itcm;
		for( itcm = ms.begin(); itcm != ms.end(); ++itcm )
		{
			const mbMapping& m = *itcm;
			if( !getColumn( dk, m.m_src.m_valColumn, m.m_src.m_valColumnCharacteristic ).valid() )
			{
				mstBuilderException ex;
				ex.code = MB_ERR_UNKNOWN_DK;
				ex.description = hbString( "Unknown mapping column" + dk + "::" + m.m_src.m_valColumn + ":" + m.m_src.m_valColumnCharacteristic ).c_str();
				ex.source = hbString("mbDatakindMappings::check()").c_str();
			}
		}
	}
}


bool mbDatakindMappings::hasMapping( const hbString& datakind,
								     const hbString& column, 
									 const hbString& columnCharacteristic ) const
{
	list<mbDatakindMapping>::const_iterator itm;

	for( itm = m_dkMappings.begin(); itm != m_dkMappings.end(); ++itm )
	{
		const mbDatakindMapping& dkm = *itm;
		if( dkm.m_datakind.m_name == datakind )
		{
			const list<mbMapping>& ms = dkm.getMappings();
			list<mbMapping>::const_iterator itc;
			for( itc = ms.begin(); itc != ms.end(); ++itc )
			{
				const mbMapping& m = *itc;
				if( ( m.m_src.m_valColumn == column ) && ( m.m_src.m_valColumnCharacteristic == columnCharacteristic ) )
					return true;
			}


			return false;
		}
	}

	return false;
}

const mbMapping& mbDatakindMappings::getMapping( const hbString& datakind,
												 const hbString& column, 
												 const hbString& columnCharacteristic )
{
	list<mbDatakindMapping>::const_iterator itm;

	for( itm = m_dkMappings.begin(); itm != m_dkMappings.end(); ++itm )
	{
		const mbDatakindMapping& dkm = *itm;
		if( dkm.m_datakind.m_name == datakind )
		{
			const list<mbMapping>& ms = dkm.getMappings();
			list<mbMapping>::const_iterator itc;
			for( itc = ms.begin(); itc != ms.end(); ++itc )
			{
				const mbMapping& m = *itc;
				if( ( m.m_src.m_valColumn == column ) && ( m.m_src.m_valColumnCharacteristic == columnCharacteristic ) )
					return m;
			}


			static mbMapping _M1;
			return _M1;
		}
	}

	static mbMapping _M2;
	return _M2;
}



mbReferenceTypeEnum mbDatakindMappings::getRefType( const hbString& datakind ) const
{
	list<mbDatakindMapping>::const_iterator itm;

	for( itm = m_dkMappings.begin(); itm != m_dkMappings.end(); ++itm )
	{
		const mbDatakindMapping& dkm = *itm;
		if( dkm.m_datakind.m_name == datakind )
			return dkm.m_datakind.m_referenceType;
	}

	return mbNone;
}

hbString mbDatakindMappings::getObjType( const hbString& datakind ) const
{
	list<mbDatakindMapping>::const_iterator itm;

	for( itm = m_dkMappings.begin(); itm != m_dkMappings.end(); ++itm )
	{
		const mbDatakindMapping& dkm = *itm;
		if( dkm.m_datakind.m_name == datakind )
			return dkm.m_datakind.m_objectType;
	}

	return "";
}

const mbDatakind& mbDatakindMappings::getDatakind( const hbString& dkName )
{
	list<mbDatakindMapping>::const_iterator itm;

	for( itm = m_dkMappings.begin(); itm != m_dkMappings.end(); ++itm )
	{
		const mbDatakindMapping& dkm = *itm;
		if( dkm.m_datakind.m_name == dkName )
			return dkm.m_datakind;
	}

	static mbDatakind dk;
	return dk;
}

const mbColumn& mbDatakindMappings::getColumn( const hbString& datakind,
											   const hbString& column, 
											   const hbString& columnCharacteristic ) const
{
	list<mbDatakindMapping>::const_iterator itm;

	for( itm = m_dkMappings.begin(); itm != m_dkMappings.end(); ++itm )
	{
		const mbDatakindMapping& dkm = *itm;
		if( dkm.m_datakind.m_name == datakind )
		{
			const list<mbColumn>& cs = dkm.m_datakind.m_columns;
			list<mbColumn>::const_iterator itc;
			for( itc = cs.begin(); itc != cs.end(); ++itc )
			{
				const mbColumn& C = *itc;
				if( ( C.m_valColumn == column ) && ( C.m_valColumnCharacteristic == columnCharacteristic ) )
					return C;
			}


			static mbColumn _c1;
			return _c1;
		}
	}

	static mbColumn _c2;
	return _c2;
}



mbMappingTypeEnum toMappingTypeEnum( const hbString& mmStr )
{
	if( mmStr == "copy" )
		return mbCopy;
	if( mmStr == "constMultiplication" )
		return mbConstMultiplication;
	if( mmStr == "period2HourWeighted" )
		return mbPeriod2HourWeighted;
	if( mmStr == "function" )
		return mbFunction;
	else
		return mbCopy;
}

hbString toString( mbMappingTypeEnum mt )
{
	switch( mt )
	{
	case mbConstMultiplication:
		return "constMultiplication";
	case mbPeriod2HourWeighted:
		return "period2HourWeighted";
	default:
		return "copy";
	}
}

d2MeasuredOrDerivedDataTypeEnum toMeasuredOrDerivedDataTypeEnum( const hbString& dtStr )
{
	if( dtStr == "humidityInformation" )
		return mbHumidityInformation;
	else if( dtStr == "individualVehicleMeasurements" )
		return mbIndividualVehicleMeasurements;
	else if( dtStr == "pollutionInformation" )
		return mbPollutionInformation;
	else if( dtStr == "precipitationInformation" )
		return mbPrecipitationInformation;
	else if( dtStr == "pressureInformation" )
		return mbPressureInformation;
	else if( dtStr == "radiationInformation" )
		return mbRadiationInformation;
	else if( dtStr == "roadSurfaceConditionInformation" )
		return mbRoadSurfaceConditionInformation;
	else if( dtStr == "temperatureInformation" )
		return mbTemperatureInformation;
	else if( dtStr == "trafficConcentration" )
		return mbTrafficConcentration;
	else if( dtStr == "trafficFlow" )
		return mbTrafficFlow;
	else if( dtStr == "trafficHeadway" )
		return mbTrafficHeadway;
	else if( dtStr == "trafficSpeed" )
		return mbTrafficSpeed;
	else if( dtStr == "trafficStatusInformation" )
		return mbTrafficStatusInformation;
	else if( dtStr == "travelTimeInformation" )
		return mbTravelTimeInformation;
	else if( dtStr == "visibilityInformation" )
		return mbVisibilityInformation;
	else if( dtStr == "windInformation" )
		return mbWindInformation;
	else
		return mbDTUnknown;
}

mbReferenceTypeEnum toReferenceTypeEnum( const hbString& dtStr )
{
	if( dtStr == "TLS" )
		return mbTLS;
	else if( dtStr == "GEODYN2_REF" )
		return mbGEODYN2_REF;
	else if( dtStr == "GEODYN2_REF_NON_Q" )
		return mbGEODYN2_REF_NON_Q;
	else if( dtStr == "TLS_Q_COORD_SUPPLY" )
		return mbTLS_Q_COORD_SUPPLY;
	else
		return mbNone;
}

hbString toString( d2MeasuredOrDerivedDataTypeEnum moddt )
{
	switch( moddt )
	{
	case mbHumidityInformation:
		return "humidityInformation";
	case mbIndividualVehicleMeasurements:
		return "individualVehicleMeasurements";
	case mbPollutionInformation:
		return "pollutionInformation";
	case mbPrecipitationInformation:
		return "precipitationInformation";
	case mbPressureInformation:
		return "pressureInformation";
	case mbRadiationInformation:
		return "radiationInformation";
	case mbRoadSurfaceConditionInformation:
		return "roadSurfaceConditionInformation";
	case mbTemperatureInformation:
		return "temperatureInformation";
	case mbTrafficConcentration:
		return "trafficConcentration";
	case mbTrafficFlow:
		return "trafficFlow";
	case mbTrafficHeadway:
		return "trafficHeadway";
	case mbTrafficSpeed:
		return "trafficSpeed";
	case mbTrafficStatusInformation:
		return "trafficStatusInformation";
	case mbTravelTimeInformation:
		return "travelTimeInformation";
	case mbVisibilityInformation:
		return "visibilityInformation";
	case mbWindInformation:
		return "windInformation";
	default:
		return "unknown";
	}
}

hbString toString( d2VehicleTypeEnum vt )
{
	switch( vt )
	{
	case mbAgriculturalVehicle:
		return "agriculturalVehicle";
	case mbAnyVehicle:
		return "anyVehicle";
	case mbArticulatedVehicle:
		return "articulatedVehiclembAgriculturalVehicle";
	case mbBicycle:
		return "bicycle";
	case mbBus:
		return "bus";
	case mbCar:
		return "car";
	case mbCaravan:
		return "caravan";
	case mbCarOrLightVehicle:
		return "carOrLightVehicle";
	case mbCarWithCaravan:
		return "carWithCaravan";
	case mbCarWithTrailer:
		return "carWithTrailer";
	case mbConstructionOrMaintenanceVehicle:
		return "constructionOrMaintenanceVehicle";
	case mbFourWheelDrive:
		return "fourWheelDrive";
	case mbHighSidedVehicle:
		return "highSidedVehicle";
	case mbLorry:
		return "lorry";
	case mbMoped:
		return "moped";
	case mbMotorcycle:
		return "motorcycle";
	case mbMotorcycleWithSideCar:
		return "motorcycleWithSideCar";
	case mbMotorscooter:
		return "motorscooter";
	case mbTanker:
		return "tanker";
	case mbThreeWheeledVehicle:
		return "threeWheeledVehicle";
	case mbTrailer:
		return "trailer";
	case mbTram:
		return "tram";
	case mbTwoWheeledVehicle:
		return "twoWheeledVehicle";
	case mbVan:
		return "van";
	case mbVehicleWithCatalyticConverter:
		return "vehicleWithCatalyticConverter";
	case mbVehicleWithoutCatalyticConverter:
		return "vehicleWithoutCatalyticConverter";
	case mbVehicleWithCaravan:
		return "vehicleWithCaravan";
	case mbVehicleWithTrailer:
		return "vehicleWithTrailer";
	case mbWithEvenNumberedRegistrationPlates:
		return "withEvenNumberedRegistrationPlates";
	case mbWithOddNumberedRegistrationPlates:
		return "withOddNumberedRegistrationPlates";
	default:
		return "";
	}
} 


d2VehicleTypeEnum toVehicleTypeEnum( const hbString& vtStr )
{
	if( vtStr == "anyVehicle" )
		return mbAnyVehicle;
	else if( vtStr == "agriculturalVehicle" )
		return mbAgriculturalVehicle;
	else if( vtStr == "ArticulatedVehicle" )
		return mbArticulatedVehicle;
	else if( vtStr == "bicycle" )
		return mbBicycle;
	else if( vtStr == "bus" )
		return mbBus;
	else if( vtStr == "car" )
		return mbCar;
	else if( vtStr == "caravan" )
		return mbCaravan;
	else if( vtStr == "carOrLightVehicle" )
		return mbCarOrLightVehicle;
	else if( vtStr == "carWithCaravan" )
		return mbCarWithCaravan;
	else if( vtStr == "carWithTrailer" )
		return mbCarWithTrailer;
	else if( vtStr == "constructionOrMaintenanceVehicle" )
		return mbConstructionOrMaintenanceVehicle;
	else if( vtStr == "fourWheelDrive" )
		return mbFourWheelDrive;
	else if( vtStr == "highSidedVehicle" )
		return mbHighSidedVehicle;
	else if( vtStr == "lorry" )
		return mbLorry;
	else if( vtStr == "moped" )
		return mbMoped;
	else if( vtStr == "motorcycle" )
		return mbMotorcycle;
	else if( vtStr == "motorcycleWithSideCar" )
		return mbMotorcycleWithSideCar;
	else if( vtStr == "motorscooter" )
		return mbMotorscooter;
	else if( vtStr == "tanker" )
		return mbTanker;
	else if( vtStr == "threeWheeledVehicle" )
		return mbThreeWheeledVehicle;
	else if( vtStr == "trailer" )
		return mbTrailer;
	else if( vtStr == "tram" )
		return mbTram;
	else if( vtStr == "twoWheeledVehicle" )
		return mbTwoWheeledVehicle;
	else if( vtStr == "van" )
		return mbVan;
	else if( vtStr == "vehicleWithCatalyticConverter" )
		return mbVehicleWithCatalyticConverter;
	else if( vtStr == "vehicleWithoutCatalyticConverter" )
		return mbVehicleWithoutCatalyticConverter;
	else if( vtStr == "vehicleWithCaravan" )
		return mbVehicleWithCaravan;
	else if( vtStr == "vehicleWithTrailer" )
		return mbVehicleWithTrailer;
	else if( vtStr == "withEvenNumberedRegistrationPlates" )
		return mbWithEvenNumberedRegistrationPlates;
	else if( vtStr == "withOddNumberedRegistrationPlates" )
		return mbWithOddNumberedRegistrationPlates;
	else
		return mbOther;
}



