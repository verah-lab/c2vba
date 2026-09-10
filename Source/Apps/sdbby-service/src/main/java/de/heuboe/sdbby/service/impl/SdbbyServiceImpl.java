package de.heuboe.sdbby.service.impl;

import de.heuboe.log.Logger;
import de.heuboe.sdbby.service.data.CommStatItem;
import de.heuboe.sdbby.service.data.CommStatRequest;
import de.heuboe.sdbby.service.data.CommStatSet;
import de.heuboe.sdbby.service.data.StrategyRequest;
import de.heuboe.sdbby.service.data.StrategySet;
import de.heuboe.sdbby.service.iface.SdbbyService;

public class SdbbyServiceImpl implements SdbbyService {

	private static final Logger LOGGER = Logger.getLogger(SdbbyServiceImpl.class);
	
	private StrategyMan strategyMan;
	private CommStatMan commStatMan;
	
	public SdbbyServiceImpl( CommStatMan commStatMan, StrategyMan strategyMan ) {
		this.strategyMan = strategyMan;
		this.commStatMan = commStatMan;
	}

	@Override
	public StrategySet queryStrategies(StrategyRequest request) {
		LOGGER.info("queryStrategies() called");
		return strategyMan.readStrategies();
	}

	@Override
	public CommStatSet queryCommStates(CommStatRequest request) {
		LOGGER.info("queryCommStates() called");
		return commStatMan.getCommStates();
	}

	@Override
	public void setCommState(CommStatItem item) {
		LOGGER.info("setCommState(" + item.getId() + ") called");
		commStatMan.setCommState(item);
	}

}
