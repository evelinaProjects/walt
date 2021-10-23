package com.walt.model;

import javax.persistence.*;

@Entity
public class DriverDistanceImpl implements  DriverDistance {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    Driver driver;

    double distance;

    public DriverDistanceImpl(){}

    public DriverDistanceImpl(Driver driver, double distance) {
        this.driver = driver;
        this.distance = distance;
    }
    public void addTotalDistance(double distance) {
        this.distance += distance;
    }

    @Override
    public Driver getDriver() {
        return driver;
    }

    @Override
    public double getTotalDistance() {
        return distance;
    }

}
