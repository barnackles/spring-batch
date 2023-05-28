package com.kodilla.csvconverter;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

//    private final JobBuilder jobBuilder;
//    private final StepBuilder stepBuilder;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;


    public BatchConfiguration(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
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
    @Bean
    ProductProcessor processor() {
        return new ProductProcessor();
    }
    @Bean
    FlatFileItemWriter<Product> writer() {
        //extractor
        BeanWrapperFieldExtractor<Product> extractor = new BeanWrapperFieldExtractor<>();
        //set field names
        extractor.setNames(new String[] {"id", "quantity", "price"});

        //set line aggregator to write item to a line
        DelimitedLineAggregator<Product> aggregator = new DelimitedLineAggregator<>();
        aggregator.setDelimiter(",");
        aggregator.setFieldExtractor(extractor);


        FlatFileItemWriter<Product> writer = new FlatFileItemWriter<>();
        //writer target
        writer.setResource(new FileSystemResource("output.csv"));
        writer.setShouldDeleteIfExists(true);
        writer.setLineAggregator(aggregator);

        return writer;
    }

    @Bean
    Step priceChange(
            ItemReader<Product> reader,
            ItemProcessor<Product, Product> processor,
            ItemWriter<Product> writer) {

        return new StepBuilder("priceChange", jobRepository)
                .<Product, Product>chunk(50, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();

    }

    @Bean
    public Job changePriceJob(JobRepository jobRepository, Step step) {
        return new JobBuilder("changePriceJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .build();
    }

}
