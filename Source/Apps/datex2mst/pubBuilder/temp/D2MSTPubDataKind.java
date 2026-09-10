package de.heuboe.datex2.mst.builder;


public enum D2MSTPubDataKind
{
    humidityInformation(0),
    pollutionInformation(1),
    precipitationInformation(2),
    pressureInformation(3),
    radiationInformation(4),
    roadSurfaceConditionInformation(5),
    temperatureInformation(6),
    visibilityInformation(7),
    windInformation(8),
    individualVehicleMeasurements(9),
    trafficConcentration(10),
    trafficFlow(11),
    trafficHeadway(12),
    trafficSpeed(13),
    travelTimeInformation(14),
    unknownn(15);
    
    
    D2MSTPubDataKind( int v )
	{
		this.value = v;
	}
	
	static public D2MSTPubDataKind toDataKind( int v )
	{
		if( v == 0 )
			return humidityInformation;
		if( v == 1 )
			return pollutionInformation;
		if( v == 2 )
			return precipitationInformation;
		if( v == 3 )
			return pressureInformation;
		if( v == 4 )
			return radiationInformation;
		if( v == 5 )
			return roadSurfaceConditionInformation;
		if( v == 6 )
			return temperatureInformation;
		if( v == 7 )
			return visibilityInformation;
		if( v == 8 )
			return windInformation;
		if( v == 9 )
			return individualVehicleMeasurements;
		if( v == 10)
			return trafficConcentration;
		if( v == 11)
			return trafficFlow;
		if( v == 12)
			return trafficHeadway;
		if( v == 13)
			return trafficSpeed;
		if( v == 14)
			return travelTimeInformation;
		else
			return unknownn;
	}
	
	public String toString()
	{
		if( value == 0 )
			return "humidityInformation";
		if( value == 1 )
			return "pollutionInformation";
		if( value == 2 )
			return "precipitationInformation";
		if( value == 3 )
			return "pressureInformation";
		if( value == 4 )
			return "radiationInformation";
		if( value == 5 )
			return "roadSurfaceConditionInformation";
		if( value == 6 )
			return "temperatureInformation";
		if( value == 7 )
			return "visibilityInformation";
		if( value == 8 )
			return "windInformation";
		if( value == 9 )
			return "individualVehicleMeasurements";
		if( value == 10)
			return "trafficConcentration";
		if( value == 11)
			return "trafficFlow";
		if( value == 12)
			return "trafficHeadway";
		if( value == 13)
			return "trafficSpeed";
		if( value == 14)
			return "travelTimeInformation";
		else
			return "unknownn";
	}
	
	public int toInteger()
	{
		return value;
	}
				
	private int value;
   
}
