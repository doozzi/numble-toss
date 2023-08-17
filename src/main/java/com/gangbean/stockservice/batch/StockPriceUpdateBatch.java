package com.gangbean.stockservice.batch;

import com.gangbean.stockservice.domain.Stock;
import com.gangbean.stockservice.domain.StockRandomPrice;
import com.gangbean.stockservice.repository.StockRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
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

@EnableBatchProcessing
@Configuration
public class StockPriceUpdateBatch {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final EntityManagerFactory entityManagerFactory;

    private final StockRepository stockRepository;

    public StockPriceUpdateBatch(JobBuilderFactory jobBuilderFactory
            , StepBuilderFactory stepBuilderFactory
            , EntityManagerFactory entityManagerFactory
            , StockRepository stockRepository) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.entityManagerFactory = entityManagerFactory;
        this.stockRepository = stockRepository;
    }

    @Bean
    public Job stockUpdate(@Qualifier("stockUpdateStep1") Step step) {
        return jobBuilderFactory.get("stockUpdate")
                .start(step)
                .build();
    }

    @Bean
    public Step stockUpdateStep1(ItemReader<Stock> reader
            , ItemProcessor<Stock, Stock> processor
            , ItemWriter<Stock> writer) {
        return stepBuilderFactory.get("stockUpdateStep1")
                .<Stock, Stock>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public JpaCursorItemReader<Stock> jpaCursorItemReader() {
        return new JpaCursorItemReaderBuilder<Stock>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT s FROM Stock s")
                .build();
    }

    @Bean
    public ItemProcessor<Stock, Stock> stockPriceUpdate() {
        return stock -> {
            stock.updatePrice(new StockRandomPrice(), LocalDateTime.now());
            return stock;
        };
    }

    @Bean
    public ItemWriter<Stock> jpaCursorItemWriter() {
        return stockRepository::saveAll;
    }

}
