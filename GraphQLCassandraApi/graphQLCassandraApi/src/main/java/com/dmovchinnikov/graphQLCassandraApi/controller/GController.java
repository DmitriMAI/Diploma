package com.dmovchinnikov.graphQLCassandraApi.controller;

import com.dmovchinnikov.graphQLCassandraApi.dto.Customer;
import com.dmovchinnikov.graphQLCassandraApi.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class GController {

    @Autowired
    private final CustomerRepository repository;

    @QueryMapping
    public Flux<Customer> getAllStudents(){
        return repository.findAll();
    }
}

record Company(@Id Long id, String name) {

}
