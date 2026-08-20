package de.heuboe.asfinag.vmis2.synchronize.vd.schedule;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
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
 * Quartz Job implementation to trigger interval end.
 * 
 * @author David Hermanns, Heusch/Boesefeldt GmbH, david.hermanns@heuboe.de; 29.07.2019
 *
 */
@Slf4j
public class IntervalEndJob implements Job  {

    public static final String ACTORREF                 = "actorref";
    public static final String IL_CRON_EXPRESSION_MAP   = "il_cron_exp";

    private static final Logger LOG = LoggerFactory.getLogger(IntervalEndJob.class);
    private static final DateTimeFormatter f = DateTimeFormatter
                                                    .ofLocalizedDateTime( FormatStyle.LONG )
                                                    .withLocale( Locale.GERMAN )
                                                    .withZone( ZoneId.systemDefault() );

    @SuppressWarnings("unchecked")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobMap = context.getJobDetail().getJobDataMap();

        Object actorRef             = jobMap.get(ACTORREF);
        Object ilCronExpsMap   = jobMap.get(IL_CRON_EXPRESSION_MAP);
        
        if(actorRef instanceof ActorRef &&
                ilCronExpsMap != null && ilCronExpsMap instanceof Map) {
            Date schedTime = context.getScheduledFireTime();
            Map<Integer, String> ilCronExps = (Map<Integer, String>) ilCronExpsMap;
            
            //find out, which intervallengths are hit by current time and cron expression
            Iterator<Entry<Integer, String>> entries = ilCronExps.entrySet().iterator();
            List<Integer> ils = new ArrayList<>();
            while(entries.hasNext()) {
                Entry<Integer, String> entry = entries.next();
                try {
                    if(new CronExpression(entry.getValue()).isSatisfiedBy(schedTime)) {
                        ils.add(entry.getKey());
                    }
                } catch (ParseException e) {
                    log.warn("Cron-Expression '{}' is no valid cronexpression. Please review code!", entry.getValue());
                }
            }
            if(!ils.isEmpty()) {
                Instant intervalEnd = schedTime.toInstant();
                String intervalEndString = f.format(intervalEnd);
                LOG.debug("trigger for interval end for IL: {} intervalend: {}",
                        ScheduleUtils.getListString(ils), intervalEndString);
                ActorRef algoActor = (ActorRef) actorRef;
                algoActor.tell(new IntervalEndTriggerData(intervalEnd, ils), ActorRef.noSender());
            }
        }
    }

}
