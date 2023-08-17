package com.gangbean.stockservice.batch;

import com.gangbean.stockservice.util.BatchExecutionTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@EnableScheduling
@Configuration
public class BatchSchedule {

    private final Logger LOGGER = LoggerFactory.getLogger(BatchSchedule.class);

    private final ApplicationContext context;

    private final JobLauncher jobLauncher;

    public BatchSchedule(ApplicationContext context, JobLauncher jobLauncher) {
        this.context = context;
        this.jobLauncher = jobLauncher;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void runStockUpdateBatch() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        LOGGER.info(">>>>>>>>>>>>> START Scheduled Stock Job <<<<<<<<<<<<<<<<<<");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        Job stockUpdate = (Job) context.getBean("stockUpdate");
        JobExecution execution = jobLauncher.run(stockUpdate, jobParameters);
        BatchStatus status = execution.getStatus();
        LOGGER.info(status.toString());
        LOGGER.info(">>>>>>>>>>>>> FINISHED Scheduled Stock Job <<<<<<<<<<<<<<<<<<");
    }

    @Scheduled(cron = "0 0 * * * *")
    public void runExecuteReservedTradeBatch() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        LOGGER.info(">>>>>>>>>>>>> START Scheduled Reservation Job <<<<<<<<<<<<<<<<<<");

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        Job reservedPayment = (Job) context.getBean("executeReservedPayment");
        JobExecution execution = jobLauncher.run(reservedPayment, jobParameters);
        BatchStatus status = execution.getStatus();
        LOGGER.info(status.toString());

        LocalDateTime nextExecution = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.HOURS);
        BatchExecutionTime.write("Reservation", nextExecution);
        LOGGER.info(">>>>>>>>>>>>> FINISHED Scheduled Reservation Job <<<<<<<<<<<<<<<<<<");
    }
}
