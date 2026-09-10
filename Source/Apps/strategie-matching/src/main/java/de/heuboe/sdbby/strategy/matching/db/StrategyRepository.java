package de.heuboe.sdbby.strategy.matching.db;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import de.heuboe.sdbby.service.data.db.Strategy;


/**
 * 
 * Strategie repository
 * 
 * @author peters
 *
 */
@Repository
public interface StrategyRepository extends MongoRepository<Strategy, String> {
}
