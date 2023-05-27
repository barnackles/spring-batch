package com.kodilla.csvconverter;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private final JobBuilder jobBuilder;
    private final StepBuilder stepBuilder;

    BatchConfiguration(JobBuilder jobBuilder, StepBuilder stepBuilder) {
        this.jobBuilder = jobBuilder;
        this.stepBuilder = stepBuilder;
    }

    @Bean
    FlatFileItemReader<Product> reader() {
        FlatFileItemReader<Product> reader = new FlatFileItemReader<>();
        //indicate source
        reader.setResource(new ClassPathResource("input.csv"));

        //indicate how to divide lines with delimiter
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        //set field names
        tokenizer.setNames("id", "quantity", "price");

        //indicate how to map values onto object fields
        BeanWrapperFieldSetMapper<Product> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Product.class);

        //setup lineMapper
        DefaultLineMapper<Product> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);


        reader.setLineMapper(lineMapper);
        return reader;
    }


}
