package de.heuboe.datex2.location.service;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService
public interface LocationService 
{
	@WebMethod
	boolean ping( String clientId );
	
	@WebMethod
	List<D2MeasurementSite> getDatex2Locations( SiteType type,
			                                    LocationFilter filter )
		throws ServiceException;	                                    
}
