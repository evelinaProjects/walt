package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Restaurant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends CrudRepository<Restaurant, Long> {
    Restaurant findByName(String name);

    List<Restaurant> findAllByCity(City city);
}
