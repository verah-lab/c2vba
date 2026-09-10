package de.heuboe.datex2.location.service.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.heuboe.datex2.location.service.D2MeasurementSite;
import de.heuboe.mst.config.lib.MstContent;

/**
 * 
 * The internal representation of complete MST data
 * 
 * @author peters
 *
 */
public class MstContentExt extends MstContent
{
	private List<D2MeasurementSite> sites = new ArrayList<>();
	
	public MstContentExt( String d2Version, String mst, String defVersion, String version )
	{
		super( d2Version, mst, defVersion, version );
	}

	public List<D2MeasurementSite> getSites() {
		return sites;
	}

	public void addSites( Collection<D2MeasurementSite> sites) {
		this.sites.addAll( sites );
	}
}

