package com.in28minutes.learn_spring_framework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class DemoController {

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/list")
    public Iterable<Customer> getCustomers() {
        return customerRepository.findAll();
    }

    @GetMapping("/find/{id}")
    public Customer findCustomerById(@PathVariable Integer id) {
        return customerRepository.findCustomerById(id);
    }
    @GetMapping("/findfirstname/{first_name}")
    public Customer findCustomerByFirstName(@PathVariable String first_name) {
        return customerRepository.findCustomerByFirstName(first_name);
    }
}
