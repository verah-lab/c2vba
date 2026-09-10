package de.heuboe.datex2.vms.service;


public enum D2VMSOperationReason
{
    SITUATION("situation"),
    OPERATOR_CREATED("operatorCreated"),
    TRAFFIC_MANAGEMENT("trafficManagement"),
    TRAVEL_TIME("travelTime"),
    CAMPAIGN("campaign"),
    DEFAULT("default");
    private final String value;
    
    D2VMSOperationReason( String v ) 
    {
        value = v;
    }

    public String value() 
    {
        return value;
    }

    public static D2VMSOperationReason fromValue(String v) 
    {
        for (D2VMSOperationReason c: D2VMSOperationReason.values() ) 
        {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
