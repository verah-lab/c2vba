package de.heuboe.sdbby.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import de.heuboe.log.Logger;
import de.heuboe.sdbby.service.config.db.StrategyRepository;
import de.heuboe.sdbby.service.data.StrategyItem;
import de.heuboe.sdbby.service.data.StrategySet;
import de.heuboe.sdbby.service.data.db.Strategy;

public class StrategyMan {

	private static final Logger LOGGER = Logger.getLogger(StrategyMan.class);

    @Autowired
    StrategyRepository repo;                                                                            
		
	public StrategyMan() {
		super();
		
		LOGGER.info( "StrategyMan initialised" );
	}

	public StrategySet readStrategies() {
		
		StrategySet strategySet = new StrategySet();
		
		List<Strategy> strategies = repo.findAll(); 
		strategySet.getStrategySet().addAll( strategies.stream().map( s -> new StrategyItem( s.getId(), s.isActive() ) ).collect( Collectors.toList() ) );

		return strategySet;
	}


}
