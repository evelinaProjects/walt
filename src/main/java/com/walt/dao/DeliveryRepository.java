package com.walt.dao;

import com.walt.model.Driver;
import com.walt.model.Delivery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeliveryRepository extends CrudRepository<Delivery, Long> {
    List<Delivery> findAllByDriver(Driver driver);
    List<Delivery> findAllByDriverAndDeliveryTime(Driver driver2,Date date);
}


