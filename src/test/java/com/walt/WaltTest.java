package com.walt;

import com.walt.dao.*;
import com.walt.model.*;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;

import javax.annotation.Resource;
import java.util.*;
import static org.junit.Assert.*;


@SpringBootTest()
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class WaltTest {

    public static final long HOUR = 3600*1000;

    @TestConfiguration
    static class WaltServiceImplTestContextConfiguration {

        @Bean
        public WaltService waltService() {
            return new WaltServiceImpl();
        }
    }


    @Autowired
    WaltService waltService;

    @Resource
    CityRepository cityRepository;

    @Resource
    CustomerRepository customerRepository;

    @Resource
    DriverRepository driverRepository;

    @Resource
    DeliveryRepository deliveryRepository;

    @Resource
    RestaurantRepository restaurantRepository;

    @Resource
    DriverDistanceImplRepository driverDistanceImplRepository;


    @BeforeEach()
    public void prepareData() {

        City jerusalem = new City("Jerusalem");
        City tlv = new City("Tel-Aviv");
        City bash = new City("Beer-Sheva");
        City haifa = new City("Haifa");

        cityRepository.save(jerusalem);
        cityRepository.save(tlv);
        cityRepository.save(bash);
        cityRepository.save(haifa);


        createDrivers(jerusalem, tlv, bash, haifa);

        createCustomers(jerusalem, tlv, haifa);

        createRestaurant(jerusalem, tlv);

        createDriverDistance();
    }

    @AfterEach
    public void clearData() {
        driverDistanceImplRepository.deleteAll();
        deliveryRepository.deleteAll();
        restaurantRepository.deleteAll();
        customerRepository.deleteAll();
        driverRepository.deleteAll();
        cityRepository.deleteAll();
    }

    private void createRestaurant(City jerusalem, City tlv) {
        Restaurant meat = new Restaurant("meat", jerusalem, "All meat restaurant");
        Restaurant vegan = new Restaurant("vegan", tlv, "Only vegan");
        Restaurant cafe = new Restaurant("cafe", tlv, "Coffee shop");
        Restaurant chinese = new Restaurant("chinese", tlv, "chinese restaurant");
        Restaurant mexican = new Restaurant("restaurant", jerusalem, "mexican restaurant ");

        restaurantRepository.saveAll(Lists.newArrayList(meat, vegan, cafe, chinese, mexican));
    }

    private void createCustomers(City jerusalem, City tlv, City haifa) {
        Customer beethoven = new Customer("Beethoven", tlv, "Ludwig van Beethoven");
        Customer mozart = new Customer("Mozart", jerusalem, "Wolfgang Amadeus Mozart");
        Customer chopin = new Customer("Chopin", haifa, "Frédéric François Chopin");
        Customer rachmaninoff = new Customer("Rachmaninoff", tlv, "Sergei Rachmaninoff");
        Customer bach = new Customer("Bach", tlv, "Sebastian Bach. Johann");


        customerRepository.saveAll(Lists.newArrayList(beethoven, mozart, chopin, rachmaninoff,bach ));
    }

    private void createDrivers(City jerusalem, City tlv, City bash, City haifa) {
        Driver mary = new Driver("Mary", tlv);
        Driver patricia = new Driver("Patricia", tlv);
        Driver jennifer = new Driver("Jennifer", haifa);
        Driver james = new Driver("James", bash);
        Driver john = new Driver("John", bash);
        Driver robert = new Driver("Robert", jerusalem);
        Driver david = new Driver("David", jerusalem);
        Driver daniel = new Driver("Daniel", tlv);
        Driver noa = new Driver("Noa", haifa);
        Driver ofri = new Driver("Ofri", haifa);
        Driver nata = new Driver("Neta", jerusalem);


        driverRepository.saveAll(Lists.newArrayList(mary, patricia, jennifer, james, john, robert, david, daniel, noa, ofri, nata));
    }

    private  void createDriverDistance(){
        ArrayList<DriverDistanceImpl> list = new ArrayList<>();
        for (Driver driver: driverRepository.findAll()) {
            DriverDistanceImpl imp = new DriverDistanceImpl(driver, 0L);
            list.add(imp);
        }
        driverDistanceImplRepository.saveAll(list);
    }


    @Test
    public void testBasics() {

        assertEquals(((List<City>) cityRepository.findAll()).size(),4);
        assertEquals((driverRepository.findAllDriversByCity(cityRepository.findByName("Beer-Sheva")).size()), 2);
    }

    @Test
    public void testSameCity() {
        //customer live in tlv
        Customer customer1 = customerRepository.findByName("Beethoven");
        //customer live in jerusalem
        Customer customer2 = customerRepository.findByName("Mozart");
        //restaurant in jerusalem
        Restaurant restaurant = restaurantRepository.findByName("meat");
        Date deliveryTime = new Date("10/23/2021 10:00:00");

        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer1, restaurant, deliveryTime);
        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer2, restaurant, deliveryTime);

        assertNull(delivery1);
        assertNotNull(delivery2);
        assertEquals(delivery2.getDriver().getCity().getName(), delivery2.getRestaurant().getCity().getName());
    }

    @Test
    public void testCustomerNotExist() {
        Customer customer = new Customer("Joseph", cityRepository.findByName("Jerusalem"), "franz joseph haydn");
        Restaurant restaurant = restaurantRepository.findByName("restaurant");
        Date deliveryTime = new Date("10/23/2021 13:00:00");

        assertEquals(customerRepository.count(),5);

        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);

        assertNotNull(delivery);

        assertEquals(customerRepository.count(),6);
    }

    @Test
    public void testDriver() {
        Customer customer = customerRepository.findByName("Bach");
        Restaurant restaurant = restaurantRepository.findByName("cafe");
        Date deliveryTime = new Date("10/23/2021 15:00:00");

        Delivery delivery1 = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
        assertNotNull(delivery1);

        Delivery delivery2 = waltService.createOrderAndAssignDriver(customer, restaurant,deliveryTime);
        assertNotNull(delivery2);
        assertNotEquals(delivery1.getDriver().getName(), delivery2.getDriver().getName());

        Delivery delivery3 = waltService.createOrderAndAssignDriver(customer, restaurant,deliveryTime);
        assertNotNull(delivery3);
        assertTrue(!Objects.equals(delivery3.getDriver().getName(), delivery1.getDriver().getName()) &&
                            !Objects.equals(delivery3.getDriver().getName(), delivery2.getDriver().getName()));

        Delivery delivery4 = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);
        assertNull(delivery4);

        Date newDeliveryTime = new Date(deliveryTime.getTime() - HOUR);
        Delivery delivery5 = waltService.createOrderAndAssignDriver(customer, restaurant, newDeliveryTime);
        assertNotNull(delivery5);

        newDeliveryTime = new Date(deliveryTime.getTime() + HOUR);
        Delivery delivery6 = waltService.createOrderAndAssignDriver(customer, restaurant, newDeliveryTime);
        assertNotNull(delivery6);

    }

    @Test
    public void testDistance() {
        Random random = new Random();

        for (Customer customer: customerRepository.findAll()) {
            for (Restaurant restaurant : restaurantRepository.findAllByCity(customer.getCity())) {
                Date deliveryTime = new Date("10/23/2021 " + Integer.toString(random.nextInt(13)) + ":00:00");
                Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);

                if(delivery != null) {
                    assertTrue(0 <= delivery.getDistance() && delivery.getDistance() <= 20.0);
                }
            }
        }
    }

    @Test
    public void testWaltService() {
        Customer customer = customerRepository.findByName("Beethoven");
        Restaurant restaurant = restaurantRepository.findByName("cafe");
        Date deliveryTime = new Date("10/23/2021 11:00:00");
        long totalDeliveriesCount = deliveryRepository.count();

        Delivery delivery = waltService.createOrderAndAssignDriver(customer, restaurant, deliveryTime);

        assertNotNull(delivery);
        assertEquals(deliveryRepository.count(),totalDeliveriesCount + 1);
        assertEquals(delivery.getCustomer(), customer);
        assertEquals(delivery.getRestaurant(), restaurant);
        assertEquals(delivery.getDeliveryTime(), deliveryTime);

        assertTrue(Objects.equals(delivery.getDriver().getName(), "Mary") ||
                            Objects.equals(delivery.getDriver().getName(), "Patricia") ||
                            Objects.equals(delivery.getDriver().getName(), "Daniel"));

        assertTrue(0 <= delivery.getDistance() && delivery.getDistance() <= 20.0);
    }

   @Test
    public void testRankReport() {
       Random random = new Random();

       for (Driver driver : driverRepository.findAll()) {
           List<Restaurant> restaurants  = restaurantRepository.findAllByCity(driver.getCity());
           if(restaurants.isEmpty()) continue;
           Restaurant firstRestaurant = restaurants.get(0);
           for (Customer customer : customerRepository.findAllByCity(driver.getCity())) {
               Date deliveryTime = new Date("10/23/2021 " + Integer.toString(random.nextInt(13)) + ":00:00");
               Delivery delivery = waltService.createOrderAndAssignDriver(customer, firstRestaurant, deliveryTime);
           }
       }

       List<DriverDistance> list = waltService.getDriverRankReport();
       assertEquals(list.size(), driverRepository.count());


       double prevDriverTotalDistance = Integer.MAX_VALUE;
       for (DriverDistance driverRank: list) {
           assertTrue(driverRank.getTotalDistance() <= prevDriverTotalDistance);
           prevDriverTotalDistance = driverRank.getTotalDistance();

           double currentDriverTotalDistance = 0;
           for (Delivery delivery: deliveryRepository.findAllByDriver(driverRank.getDriver())) {
               currentDriverTotalDistance += delivery.getDistance();
           }
           assertTrue(currentDriverTotalDistance == driverRank.getTotalDistance());
       }

   }

    @Test
    public void testRankReportByCity() {
        Customer customer1 = customerRepository.findByName("Bach");
        Customer customer2 = customerRepository.findByName("Mozart");

        Restaurant restaurant1 = restaurantRepository.findByName("cafe");
        Restaurant restaurant2 = restaurantRepository.findByName("meat");

        City city = cityRepository.findByName("Tel-Aviv");

        Date deliveryTime = new Date("10/23/2021 15:00:00");

        waltService.createOrderAndAssignDriver(customer1, restaurant1, deliveryTime);
        waltService.createOrderAndAssignDriver(customer1, restaurant1, deliveryTime);
        waltService.createOrderAndAssignDriver(customer2, restaurant2, deliveryTime);

        List<DriverDistance> list = waltService.getDriverRankReportByCity(city);

        assertEquals(list.size(), driverRepository.findAllDriversByCity(city).size());

        double prevDriverTotalDistance = Integer.MAX_VALUE;
        for (DriverDistance driverRank: list) {
            assertTrue(driverRank.getTotalDistance() <= prevDriverTotalDistance);
            prevDriverTotalDistance = driverRank.getTotalDistance();
        }
    }

}
