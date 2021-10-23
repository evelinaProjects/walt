package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {
    Customer findByName(String name);

    List<Customer> findAllByCity(City city);
}
