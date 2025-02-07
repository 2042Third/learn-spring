package com.in28minutes.learn_spring_framework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DirectedDemoController {

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/add")
    public String addCustomer(@RequestParam String first, @RequestParam String last) {
        Customer customer = new Customer();
        customer.setFirstName(first);
        customer.setLastName(last);
        customerRepository.save(customer);
        // Redirect to index.html
        return "redirect:/";
    }

    @GetMapping("/form")
    public String showForm(
            @RequestParam(name = "firstname", defaultValue = "") String firstname,
            @RequestParam(name = "lastname", defaultValue = "") String lastname,
            Model model
    ) {
        model.addAttribute("firstname", firstname);
        model.addAttribute("lastname", lastname);
        return "form";
    }
}
