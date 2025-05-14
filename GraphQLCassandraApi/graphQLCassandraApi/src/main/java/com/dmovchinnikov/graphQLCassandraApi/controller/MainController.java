package com.dmovchinnikov.graphQLCassandraApi.controller;

import com.dmovchinnikov.graphQLCassandraApi.dto.Customer;
import com.dmovchinnikov.graphQLCassandraApi.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@RestController
public class MainController {
    @Autowired
    CustomerRepository customerRepository;
    @GetMapping(value = "/customer/{customerName}")
    public Mono<Customer> findTheCustomer(@PathVariable String customerName) {
        return customerRepository.findByCustomerName(customerName);
    }
    @GetMapping(value = "/customer")
    public Flux<Customer> findAllTheCustomers() {
        return customerRepository.findAll();
    }
    @PostMapping(value = "/customer")
    public Mono<Customer> addCustomer(@RequestBody Customer customerRequest) {
        return customerRepository.save(customerRequest);
    }
}