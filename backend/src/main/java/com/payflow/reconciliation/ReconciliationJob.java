package com.payflow.reconciliation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.scheduling.annotation.Scheduled;import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="spring.task.scheduling.enabled",havingValue="true")
public class ReconciliationJob {private final ReconciliationService service;public ReconciliationJob(ReconciliationService s){service=s;}@Scheduled(cron="${RECONCILIATION_CRON:0 0 2 * * *}",zone="UTC") public void run(){service.run();}}
