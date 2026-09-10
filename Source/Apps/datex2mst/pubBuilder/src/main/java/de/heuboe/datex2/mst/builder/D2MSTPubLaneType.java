package de.heuboe.datex2.mst.builder;

public enum D2MSTPubLaneType
{
	allLanesCompleteCarriageway(0),
	busLane(1),
	busStop(2),
	carPoolLane(3),
	centralReservation(4),
	crawlerLane(5),
	emergencyLane(6),
	escapeLane(7),
	expressLane(8),
	hardShoulder(9),
	heavyVehicleLane(10),
	lane1(11),
	lane2(12),
	lane3(13),
	lane4(14),
	lane5(15),
	lane6(16),
	lane7(17),
	lane8(18),
	lane9(19),
	layBy(20),
	leftHandTurningLane(21),
	leftLane(22),
	localTrafficLane(23),
	middleLane(24),
	opposingLanes(25),
	overtakingLane(26),
	rightHandTurningLane(27),
	rightLane(28),
	rushHourLane(29),
	setDownArea(30),
	slowVehicleLane(31),
	throughTrafficLane(32),
	tidalFlowLane(33),
	turningLane(34),
	verge(35),
	unknown(36);

	D2MSTPubLaneType( int v )
	{
		this.value = v;
	}
	
	static public D2MSTPubLaneType toLaneType( int v )
	{
		if( v == 0 )
			return allLanesCompleteCarriageway;
		if( v == 1 )
			return busLane;
		if( v == 2 )
			return busStop;
		if( v == 3 )
			return 	carPoolLane;
		if( v == 4 )
			return centralReservation;
		if( v == 5 )
			return crawlerLane;
		if( v == 6 )
			return emergencyLane;
		if( v == 7 )
			return escapeLane;
		if( v == 8 )
			return expressLane;
		if( v == 9 )
			return hardShoulder;
		if( v == 10 )
			return heavyVehicleLane;
		if( v == 11 )
			return lane1;
		if( v == 12 )
			return lane2;
		if( v == 13 )
			return lane3;
		if( v == 14 )
			return lane4;
		if( v == 15 )
			return lane5;
		if( v == 16 )
			return lane6;
		if( v == 17 )
			return lane7;
		if( v == 18 )
			return lane8;
		if( v == 19 )
			return lane9;
		if( v == 20 )
			return layBy;
		if( v == 21 )
			return leftHandTurningLane;
		if( v == 22 )
			return leftLane;
		if( v == 23 )
			return localTrafficLane;
		if( v == 24 )
			return middleLane;
		if( v == 25 )
			return opposingLanes;
		if( v == 26 )
			return overtakingLane;
		if( v == 27 )
			return rightHandTurningLane;
		if( v == 28 )
			return rightLane;
		if( v == 29 )
			return rushHourLane;
		if( v == 30 )
			return setDownArea;
		if( v == 31 )
			return slowVehicleLane;
		if( v == 32 )
			return throughTrafficLane;
		if( v == 33 )
			return tidalFlowLane;
		if( v == 34 )
			return turningLane;
		if( v == 35 )
			return verge;
		else
			return unknown;
	}
	
	public String toString()
	{
		if( value == 0 )
			return "allLanesCompleteCarriageway";
		if( value == 1 )
			return "busLane";
		if( value == 2 )
			return "busStop";
		if( value == 3 )
			return "	carPoolLane";
		if( value == 4 )
			return "centralReservation";
		if( value == 5 )
			return "crawlerLane";
		if( value == 6 )
			return "emergencyLane";
		if( value == 7 )
			return "escapeLane";
		if( value == 8 )
			return "expressLane";
		if( value == 9 )
			return "hardShoulder";
		if( value == 10 )
			return "heavyVehicleLane";
		if( value == 11 )
			return "lane1";
		if( value == 12 )
			return "lane2";
		if( value == 13 )
			return "lane3";
		if( value == 14 )
			return "lane4";
		if( value == 15 )
			return "lane5";
		if( value == 16 )
			return "lane6";
		if( value == 17 )
			return "lane7";
		if( value == 18 )
			return "lane8";
		if( value == 19 )
			return "lane9";
		if( value == 20 )
			return "layBy";
		if( value == 21 )
			return "leftHandTurningLane";
		if( value == 22 )
			return "leftLane";
		if( value == 23 )
			return "localTrafficLane";
		if( value == 24 )
			return "middleLane";
		if( value == 25 )
			return "opposingLanes";
		if( value == 26 )
			return "overtakingLane";
		if( value == 27 )
			return "rightHandTurningLane";
		if( value == 28 )
			return "rightLane";
		if( value == 29 )
			return "rushHourLane";
		if( value == 30 )
			return "setDownArea";
		if( value == 31 )
			return "slowVehicleLane";
		if( value == 32 )
			return "throughTrafficLane";
		if( value == 33 )
			return "tidalFlowLane";
		if( value == 34 )
			return "turningLane";
		if( value == 35 )
			return "verge";
		else
			return "unknown";
	}

	private int value;
}
