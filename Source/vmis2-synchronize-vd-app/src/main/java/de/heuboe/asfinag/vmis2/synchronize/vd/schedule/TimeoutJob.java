package de.heuboe.asfinag.vmis2.synchronize.vd.schedule;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import lombok.extern.slf4j.Slf4j;

/**
 * Quartz Job implementation to trigger interval timeout.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 29.07.2019
 *
 */
@Slf4j
public class TimeoutJob implements Job  {
    
    private static final Logger LOG = LoggerFactory.getLogger(TimeoutJob.class);
    private static final DateTimeFormatter f = DateTimeFormatter
            .ofLocalizedDateTime( FormatStyle.LONG )
            .withLocale( Locale.GERMAN )
            .withZone( ZoneId.systemDefault() );

    public static final String ACTORREF                 = "actorref";
    public static final String IL_TIMEOUT_MAP           = "il_timeout_map";
    public static final String IL_CRON_EXPRESSION_MAP   = "il_cron_exp_map";
    
    @SuppressWarnings("unchecked")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobMap = context.getJobDetail().getJobDataMap();

        Object actorRef             = jobMap.get(ACTORREF);
        Object ilTimeoutMap         = jobMap.get(IL_TIMEOUT_MAP);
        Object ilCronExpsMap        = jobMap.get(IL_CRON_EXPRESSION_MAP);
        
        if(actorRef instanceof ActorRef
                && ilTimeoutMap != null && ilTimeoutMap instanceof Map
                && ilCronExpsMap != null && ilCronExpsMap instanceof Map) {
            
            Date timeoutTime = context.getScheduledFireTime();
            Instant timeoutTimeInst = timeoutTime.toInstant();
            Map<Integer, String> ilCronExps = (Map<Integer, String>) ilCronExpsMap;
            Map<Integer, Integer> timeoutSeconds = (Map<Integer, Integer>) ilTimeoutMap;

            // Find all intervals, whose timeout expires now
            List<Integer> ils = findTimeoutIntervals(ilCronExps, timeoutTime);
            if(ils.isEmpty()) {
                return;
            }
            
            // find all interval lengths (which triggered this current timeout) with the same interval end:
            Map<Instant, List<Integer>> intervalEndIlMap = new HashMap<>();
            for(Integer il : ils) {
                Integer timeoutDelay = timeoutSeconds.get(il);
                Instant intervalEnd = timeoutTimeInst.minusSeconds(timeoutDelay);
                intervalEndIlMap.computeIfAbsent(intervalEnd, ilen -> new ArrayList<>()).add(il);
            }
            
            // trigger all timeout / intervalend constellations
            Iterator<Entry<Instant, List<Integer>>> ents = intervalEndIlMap.entrySet().iterator();
            while(ents.hasNext()) {
                Entry<Instant, List<Integer>> entry = ents.next();
                List<Integer> curILs = entry.getValue();
                Instant curIntervalEnd = entry.getKey();
                
                if(curILs != null && !curILs.isEmpty()) {
                    String timeoutString = f.format(timeoutTimeInst);
                    String intervalEndString = f.format(curIntervalEnd);
                    LOG.info("trigger timeout for IL: {}, timeout: {}, intervalend: {}",
                            ScheduleUtils.getListString(curILs), timeoutString, intervalEndString);
                    
                    ActorRef algoActor = (ActorRef) actorRef;
                    algoActor.tell(new IntervalTimeoutTriggerData(timeoutTimeInst, curIntervalEnd, curILs), ActorRef.noSender());
                }
            }
            
        }
    }
    
    private List<Integer> findTimeoutIntervals(Map<Integer, String> ilCronExps, Date timeoutTime) {
        List<Integer> ils = new ArrayList<>();
        Iterator<Entry<Integer, String>> entries = ilCronExps.entrySet().iterator();
        while(entries.hasNext()) {
            Entry<Integer, String> entry = entries.next();
            try {
                if(new CronExpression(entry.getValue()).isSatisfiedBy(timeoutTime)) {
                    ils.add(entry.getKey());
                }
            } catch (ParseException e) {
                log.warn("Cron-Expression '{}' is no valid cronexpression. Please review code!", entry.getValue());
            }
        }
        return ils;
    }

}
