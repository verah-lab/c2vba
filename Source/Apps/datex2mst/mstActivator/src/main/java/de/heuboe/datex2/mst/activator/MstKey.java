package de.heuboe.datex2.mst.activator;

import de.heuboe.util.PairC;

public class MstKey extends PairC<String,String>
{
	public MstKey( String mstId, String mstVersion )
	{
		super( mstId, mstVersion );
	}

	public String getMstId()
	{
		return getFirst();
	}


	public String getMstVersion()
	{
		return getSecond();
	}
}
