package com.in28minutes.learn_spring_framework;

import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, Integer> {

    Customer findCustomerById(Integer id);
    Customer findCustomerByFirstName(String name);
}
