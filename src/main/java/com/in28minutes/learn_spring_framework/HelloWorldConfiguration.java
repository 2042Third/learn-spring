package com.in28minutes.learn_spring_framework;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

record Person(String name, int age, Address addr) {};
record Address(String city, String country) {};

@Configuration
public class HelloWorldConfiguration {
    @Bean
    public String name() {
        return "Mike";
    }

    @Bean
    public int age() {
        return 25;
    }

    @Bean
    public Person person() {
        return new Person ("Jack", 20, address());
    }

    @Bean
    public Person person2method() {
        return new Person (name(), age(), address());
    }

    @Bean
    @Primary
    public Person person3Parameters(String name, int age, Address address) { // address will be the Primary Address
        return new Person (name, age, address);
    }

    @Bean
    public Person person4Parameters(String name, int age, @Qualifier("address4qualifier") Address address) { // Use qualifier
        return new Person (name, age, address);
    }

    @Bean(name="address1")
    @Primary
    public Address address() {
        return new Address("New York", "USA");
    }

    @Bean(name="address4")
    @Qualifier("address4qualifier")
    public Address address4() {
        return new Address("London", "UK");
    }



}
