package de.heuboe.datex2.mst.builder;

public class D2MSTPubXMLMan
{
    public static String getD2xsdNSPref()
    {
    	return "xsi:";
    }
    
    public static void setLanguage( String lang )
    {
    	language = lang;
    }
    
	public static String getLanguage() 
	{
		return language;
	}
	
	private static String language = "de";
}
