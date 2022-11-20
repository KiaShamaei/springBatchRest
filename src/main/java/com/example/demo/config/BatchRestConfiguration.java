package com.example.demo.config;


import com.example.demo.domain.Coffee;
import com.example.demo.domain.Users;
import com.example.demo.logger.LoggingItemWriter;
import com.example.demo.repository.UserRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableBatchProcessing
public class BatchRestConfiguration {

    @Autowired
    UserRepository userRepository;
    @Autowired
    RestTemplate restTemplateA;
    private static final String PROPERTY_REST_API_URL = "user.url";
    @Bean
    public ItemReader<Users> itemReader(Environment environment,RestTemplate  restTemplateA) {
        return new RESTUserReader(environment.getRequiredProperty(PROPERTY_REST_API_URL), restTemplateA);
//        return new RESTUserReader(PROPERTY_REST_API_URL, restTemplateA);
    }

    @Bean
    public ItemWriter<Users> itemWriter() {
        return new LoggingItemWriter();
    }
    @Bean
    public Step restStep(ItemReader<Users> reader,
                         ItemWriter<Users> writer,
                         StepBuilderFactory stepBuilderFactory) {
        return stepBuilderFactory.get("restStep")
                .<Users, Users>chunk(10)
                .reader(reader)
                .processor(processorRest())
                .writer(writer)
                .build();
    }

    @Bean
    public Job restJob(Step restStep,
                          JobBuilderFactory jobBuilderFactory) {
        return jobBuilderFactory.get("restJob")
                .incrementer(new RunIdIncrementer())
                .flow(restStep)
                .end()
                .build();
    }
    @Bean
    public ItemProcessor<Users, Users> processorRest(){
        // return new InvoiceProcessor(); // Using lambda expression code instead of a separate implementation
        return user -> {
            var newUsers = new Users();
            String name = user.getName();;
            newUsers.setName(name);
            String email = user.getEmail();
            newUsers.setEmail(email);
            String username = user.getUsername();
            newUsers.setUsername(username);
            return   userRepository.save(newUsers);
        };
    }
}