package com.example.demo.config;

import com.example.demo.domain.Coffee;
import com.example.demo.helper.BlankLineRecordSeparatorPolicy;
import com.example.demo.logger.LoggerJob;
import com.example.demo.repository.CoffeeRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Value("${file.input}")
    private String fileInput;
    //Autowire InvoiceRepository
    @Autowired
    CoffeeRepository repository;

    @Bean
    public FlatFileItemReader<Coffee> reader() {

        FlatFileItemReader<Coffee> reader= new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource(fileInput));
        // Reader. setResource(new FileSystemResource("D:/mydata/invoices.csv"));
        // reader.setResource(new UrlResource("https://xyz.com/files/invoices.csv"));
        // reader.setLinesToSkip(1);

        reader.setLineMapper(new DefaultLineMapper<>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setDelimiter(DELIMITER_COMMA);
                setNames("brand" , "origin" , "characteristics");
            }});

            setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(Coffee.class);
            }});
        }});

        reader.setRecordSeparatorPolicy(new BlankLineRecordSeparatorPolicy());

        return reader;
    }



    //Writer class Object
    @Bean
    public ItemWriter<Coffee> writer(){
        // return new InvoiceItemWriter(); // Using lambda expression code instead of a separate implementation
        return invoices -> {
            System.out.println("Saving Invoice Records: " +invoices);
        };
    }

    //Processor class Object
    @Bean
    public ItemProcessor<Coffee, Coffee> processor(){
        // return new InvoiceProcessor(); // Using lambda expression code instead of a separate implementation
        return coffee -> {
            var newCoffee = new Coffee();
            String brand = coffee.getBrand().toUpperCase();
            newCoffee.setBrand(brand);
            String origin = coffee.getOrigin().toUpperCase();
            newCoffee.setOrigin(origin);
            String characteristics = coffee.getCharacteristics().toUpperCase();
            newCoffee.setCharacteristics(characteristics);
            return   repository.save(newCoffee);
        };
    }

    //Listener class Object
    @Bean
    public JobExecutionListener listener() {
        return new LoggerJob();
    }

    //Autowire StepBuilderFactory
    @Autowired
    private StepBuilderFactory sbf;

    //Step Object
    @Bean
    public Step stepA() {
        return sbf.get("stepA")
                .<Coffee,Coffee>chunk(2)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build()
                ;
    }

    //Autowire JobBuilderFactory
    @Autowired
    private JobBuilderFactory jbf;

    //Job Object
    @Bean
    public Job jobA(){
        return jbf.get("jobA")
                .incrementer(new RunIdIncrementer())
                .listener(listener())
                .start(stepA())
                // .next(stepB()) 
                // .next(stepC())
                .build()
                ;
    }
}
