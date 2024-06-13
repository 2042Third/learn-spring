package com.in28minutes.learn_spring_framework;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;


public class App02HelloWorldSpring {
    public static void main(String[] args) {
        // 1: Launch a Spring Context

        var context = new AnnotationConfigApplicationContext(HelloWorldConfiguration.class);

        // 2: Configure things that we want Spring to manage


        // 3: Get the bean from the Spring Context
        System.out.println(context.getBean("name"));
        System.out.println(context.getBean("person"));

//        System.out.println(context.getBean("address")); // Renamed to address1
        System.out.println(context.getBean("address1"));
        System.out.println(context.getBean(Address.class)); // Use the type to get the bean

        System.out.println(context.getBean("person3Parameters"));
        System.out.println(context.getBean("person4Parameters"));

        // See all the beans that are available
        System.out.printf("All beans\n");
        Arrays.stream(context.getBeanDefinitionNames())
                .forEach(System.out::println);

    }
}
