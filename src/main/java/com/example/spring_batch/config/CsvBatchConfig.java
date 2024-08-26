package com.example.spring_batch.config;


import com.example.spring_batch.Repository.CustomerRepository;
import com.example.spring_batch.entity.Customer;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Configuration
@EnableBatchProcessing
public class CsvBatchConfig {
   
    @Autowired
    private CustomerRepository customerRepo;

    //It is predefined one and is used to create the step in spring batch.
   @Autowired
    private StepBuilderFactory sBuilder;

    //It is predefined one and is used to create the Job in spring batch.
    @Autowired
    private JobBuilderFactory jBuilder;

    //create Reader
    //Read the data from source
    @Bean
     public FlatFileItemReader<Customer> customerReader(){
        FlatFileItemReader<Customer> itemReader=new FlatFileItemReader<Customer>();
        //It is used to read the resource from given file path.\
        //new FileSystemResource ("src/main/resources/customers.csv")
        itemReader.setResource( new ClassPathResource("customers.csv"));

        itemReader.setName("csv-Reader");
       //It will skip the 1st line in csv file because it is header and those headers are present in Entity class
        itemReader.setLinesToSkip(1);
        //linemapper is used to tell that read one line and represent as One customer object in a table
        itemReader.setLineMapper(lineMapper());
        return  itemReader;

    }

    private LineMapper<Customer> lineMapper() {
        //pre-defined class
        DefaultLineMapper<Customer> lineMapper=new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
     //We are reading csv file thats why we are using ,.
        lineTokenizer.setDelimiter(",");
        //If data is not present in row on csv file.It will fill as null because every row will not contain the data.
        lineTokenizer.setStrict(false);
        //Order of data present in .csv file
        lineTokenizer.setNames("id","firstName","lastName","email","gender","contactNo","country","dob");
        //Take the data from lineMapper and convert into Bean object.
        //Used to convert the Java object.
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper=new BeanWrapperFieldSetMapper<Customer>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return  lineMapper;
    }
    //create processor
     @Bean
    public CustomerProcessor customerProcessor(){
        return new  CustomerProcessor();
    }

    //create writer
    //It is predefined bean which is used to write the data in database
    @Bean
    public RepositoryItemWriter<Customer> customerWriter(){
        RepositoryItemWriter<Customer> repositoryWriter=new RepositoryItemWriter<>();
        repositoryWriter.setRepository(customerRepo);
        repositoryWriter.setMethodName("save");
        return  repositoryWriter;

    }

    //create step
    @Bean
    public Step step(){
        //"step-1" is name of the step and chunk(10) means at a time 10 records will process the input and output is customer only.
        return sBuilder.get("step-1").<Customer,Customer>chunk(10)
                .reader(customerReader())
                .processor(customerProcessor())
                .writer(customerWriter()).build();

    }
    //create job
    @Bean
    public Job job(){
        //In flow() it will call the Step using the step().If u have multiple steps u need create multiple flows.
        return jBuilder.get("customer-job").flow(step()).end().build();

    }


}
