package com.dmovchinnikov.graphQLCassandraApi.repository;

import com.dmovchinnikov.graphQLCassandraApi.dto.Customer;
import org.springframework.data.cassandra.repository.AllowFiltering;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import reactor.core.publisher.Mono;
public interface CustomerRepository extends ReactiveCassandraRepository<Customer, Long>{
    @AllowFiltering
    public Mono<Customer> findByCustomerName(String customerName);

}