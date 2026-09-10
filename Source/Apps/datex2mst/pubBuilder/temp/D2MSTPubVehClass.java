package de.heuboe.datex2.mst.builder;

public enum D2MSTPubVehClass
{
	anyVehicle(0),
	articulatedVehicle(1),
	bus(2),
	car(3),
	carOrLightVehicle(4),
	carWithCaravan(5),
	carWithTrailer(6),
	fourWheelDrive(7),
	goodsVehicle(8),
	heavyLorry(9),
	heavyVehicle(10),
	highSidedVehicle(11),
	lightVehicle(12),
	lorry(13),
	motorcycle(14),
	twoWheeledVehicle(15),
	van(16),
	vehicleWithCatalyticConverter(17),
	vehicleWithoutCatalyticConverter(18),
	vehicleWithCaravan(19),
	vehicleWithTrailer(20),
	withEvenNumberedRegistrationPlates(21),
	withOddNumberedRegistrationPlates(22),
	other(23);
	
	D2MSTPubVehClass( int v )
	{
		this.value = v;
	}
	
	static public D2MSTPubVehClass toVehClass( int v )
	{
		if( v == 0 )
			return anyVehicle;
		if( v == 1 )
			return articulatedVehicle;
		if( v == 2 )
			return bus;
		if( v == 3 )
			return car;
		if( v == 4 )
			return carOrLightVehicle;
		if( v == 5 )
			return carWithCaravan;
		if( v == 6 )
			return carWithTrailer;
		if( v == 7 )
			return fourWheelDrive;
		if( v == 8 )
			return goodsVehicle;
		if( v == 9 )
			return heavyLorry;
		if( v == 10)
			return heavyVehicle;
		if( v == 11)
			return highSidedVehicle;
		if( v == 12)
			return lightVehicle;
		if( v == 13)
			return lorry;
		if( v == 14)
			return motorcycle;
		if( v == 15)
			return twoWheeledVehicle;
		if( v == 16)
			return van;
		if( v == 17)
			return vehicleWithCatalyticConverter;
		if( v == 18)
			return vehicleWithoutCatalyticConverter;
		if( v == 19)
			return vehicleWithCaravan;
		if( v == 20)
			return vehicleWithTrailer;
		if( v == 21)
			return withEvenNumberedRegistrationPlates;
		if( v == 22)
			return withOddNumberedRegistrationPlates;
		else
			return other;
	}
	
	public String toString()
	{
		if( value == 0 )
			return "anyVehicle";
		if( value == 1 )
			return "articulatedVehicle";
		if( value == 2 )
			return "bus";
		if( value == 3 )
			return "car";
		if( value == 4 )
			return "carOrLightVehicle";
		if( value == 5 )
			return "carWithCaravan";
		if( value == 6 )
			return "carWithTrailer";
		if( value == 7 )
			return "fourWheelDrive";
		if( value == 8 )
			return "goodsVehicle";
		if( value == 9 )
			return "heavyLorry";
		if( value == 10)
			return "heavyVehicle";
		if( value == 11)
			return "highSidedVehicle";
		if( value == 12)
			return "lightVehicle";
		if( value == 13)
			return "lorry";
		if( value == 14)
			return "motorcycle";
		if( value == 15)
			return "twoWheeledVehicle";
		if( value == 16)
			return "van";
		if( value == 17)
			return "vehicleWithCatalyticConverter";
		if( value == 18)
			return "vehicleWithoutCatalyticConverter";
		if( value == 19)
			return "vehicleWithCaravan";
		if( value == 20)
			return "vehicleWithTrailer";
		if( value == 21)
			return "withEvenNumberedRegistrationPlates";
		if( value == 22)
			return "withOddNumberedRegistrationPlates";
		else
			return "other";
	}
	
	private int value;
}

