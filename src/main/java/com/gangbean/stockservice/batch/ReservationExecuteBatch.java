package com.gangbean.stockservice.batch;

import com.gangbean.stockservice.domain.ReservationStatus;
import com.gangbean.stockservice.domain.TradeReservation;
import com.gangbean.stockservice.repository.TradeReservationRepository;
import com.gangbean.stockservice.util.BatchExecutionTime;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

@EnableBatchProcessing
@Configuration
public class ReservationExecuteBatch {

    private final Logger LOGGER = LoggerFactory.getLogger(ReservationExecuteBatch.class);

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory entityManagerFactory;

    private final TradeReservationRepository tradeReservationRepository;

    public ReservationExecuteBatch(JobBuilderFactory jobBuilderFactory
            , StepBuilderFactory stepBuilderFactory
            , EntityManagerFactory entityManagerFactory
            , TradeReservationRepository tradeReservationRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.tradeReservationRepository = tradeReservationRepository;
    }

    @Bean
    public Job executeReservedPayment(@Qualifier("executePayment") Step step) {
        return jobBuilderFactory.get("executeReservedPayment")
                .start(step)
                .build();
    }

    @Bean
    public Step executePayment(ItemReader<TradeReservation> reader
            , ItemProcessor<TradeReservation, TradeReservation> processor
            , ItemWriter<TradeReservation> writer) {
        return stepBuilderFactory.get("executePayment")
                .<TradeReservation, TradeReservation>chunk(1)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public JpaCursorItemReader<TradeReservation> reservationReader() {
        LocalDateTime reservation = BatchExecutionTime.nextExecutionTime("Reservation");
        return new JpaCursorItemReaderBuilder<TradeReservation>()
                .name("reservationReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT r FROM TradeReservation r WHERE r.tradeAt = :tradeAt and r.status = :status")
                .parameterValues(Map.of("tradeAt", reservation, "status", ReservationStatus.READY))
                .build();
    }

    @Bean
    public ItemProcessor<TradeReservation, TradeReservation> executeTrade() {
        return reservation -> {
            LOGGER.info("Data >>>>>>>>>>>> " + reservation);
            reservation.execute();
            tradeReservationRepository.save(reservation);
            return reservation;
        };
    }

    @Bean
    public ItemWriter<TradeReservation> reservationWriter() {
        return items -> Objects.equals(items, null);
    }

}
