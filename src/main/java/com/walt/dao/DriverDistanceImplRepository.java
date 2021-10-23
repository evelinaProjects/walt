package com.walt.dao;

import com.walt.model.City;
import com.walt.model.Driver;
import com.walt.model.DriverDistance;
import com.walt.model.DriverDistanceImpl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverDistanceImplRepository extends CrudRepository<DriverDistanceImpl, Long> {
    DriverDistanceImpl findByDriver(Driver driver);

    List<DriverDistance> findByOrderByDistanceDesc();

    List<DriverDistance> findAllByDriverCityOrderByDistanceDesc(City city);
}
