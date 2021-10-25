package com.walt;

import com.walt.dao.CustomerRepository;
import com.walt.dao.DeliveryRepository;
import com.walt.dao.DriverRepository;
import com.walt.dao.DriverDistanceImplRepository;
import com.walt.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class WaltServiceImpl implements WaltService {
    private static final Logger log = LoggerFactory.getLogger(WaltServiceImpl.class);
    public  static final double MAX_DISTANCE = 20.0;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @Autowired
    DriverDistanceImplRepository driverDistanceImplRepository;


    //find least busy available driver.
    //return null if there is no available driver in city.
    private Driver findDriver(Date deliveryTime, City city){
        int minDeliveryHistoryCount = Integer.MAX_VALUE;
        Driver selectedDriver = null;

        for (Driver driver: driverRepository.findAllDriversByCity(city)) {
            if(deliveryRepository.findAllByDriverAndDeliveryTime(driver, deliveryTime).size() == 0) {
                int deliveriesCount = deliveryRepository.findAllByDriver(driver).size();
                if( deliveriesCount < minDeliveryHistoryCount){
                    minDeliveryHistoryCount = deliveriesCount;
                    selectedDriver = driver;
                }
            }
        }

        return selectedDriver;
    }

    //update total distance in driver distance repository
    private void updateDriverDistance(Driver driver, double distance){
        DriverDistanceImpl driverDistance = driverDistanceImplRepository.findByDriver(driver);
        driverDistance.addTotalDistance(distance);
        driverDistanceImplRepository.save(driverDistance);
    }


    @Override
    public Delivery createOrderAndAssignDriver(Customer customer, Restaurant restaurant, Date deliveryTime) {
        if (customerRepository.findByName(customer.getName()) == null) {
            customerRepository.save(customer);
        }

        if(customer.getCity().getName() != restaurant.getCity().getName()) {
            log.error("Customer city " + customer.getCity().getName() +
                      " is not matching the restaurant city " + restaurant.getCity().getName());
            return null;
        }

        Driver driver = findDriver(deliveryTime,customer.getCity());
        if (driver == null) {
            log.error("No available driver in city " + restaurant.getCity().getName() +
                      " for delivery to customer " + customer.getName());
            return null;
        }

        Delivery delivery = new Delivery(driver, restaurant ,customer ,deliveryTime);
        double distance = Math.random() * MAX_DISTANCE;
        delivery.setDistance(distance);
        updateDriverDistance(driver,distance);
        return deliveryRepository.save(delivery);
    }

    @Override
    public List<DriverDistance> getDriverRankReport() {
        return driverDistanceImplRepository.findByOrderByDistanceDesc();
    }

    @Override
    public List<DriverDistance> getDriverRankReportByCity(City city) {
        return driverDistanceImplRepository.findAllByDriverCityOrderByDistanceDesc(city);

    }
}
