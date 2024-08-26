package com.example.spring_batch.config;

import com.example.spring_batch.entity.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer item) throws Exception {
        //If u want to write any logic to process the data.U need to write here.

        return item;
    }
}
