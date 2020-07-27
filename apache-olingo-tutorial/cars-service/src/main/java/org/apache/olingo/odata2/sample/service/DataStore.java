package org.apache.olingo.odata2.sample.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;

public class DataStore {

  //Data accessors
  public Map<String, Object> getCar(int id) {
    Map<String, Object> data = null;

    Calendar updated = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
    switch (id) {
    case 1:
      updated.set(2012, 11, 11, 11, 11, 11);
      data = createCar(1, "F1 W03", 1, 189189.43, "EUR", "2012", updated, "file://imagePath/w03");
      break;

    case 2:
      updated.set(2013, 11, 11, 11, 11, 11);
      data = createCar(2, "F1 W04", 1, 199999.99, "EUR", "2013", updated, "file://imagePath/w04");
      break;

    case 3:
      updated.set(2012, 12, 12, 12, 12, 12);
      data = createCar(3, "F2012", 2, 137285.33, "EUR", "2012", updated, "http://pathToImage/f2012");
      break;

    case 4:
      updated.set(2013, 12, 12, 12, 12, 12);
      data = createCar(4, "F2013", 2, 145285.00, "EUR", "2013", updated, "http://pathToImage/f2013");
      break;

    case 5:
      updated.set(2011, 11, 11, 11, 11, 11);
      data = createCar(5, "F1 W02", 1, 167189.00, "EUR", "2011", updated, "file://imagePath/wXX");
      break;

    default:
      break;
    }
    
    return data;
  }

  
  private Map<String, Object> createCar(int carId, String model, int manufacturerId, double price, String currency, String modelYear, Calendar updated, String imagePath) {
    Map<String, Object> data = new HashMap<String, Object>();
    
    data.put("Id", carId);
    data.put("Model", model);
    data.put("ManufacturerId", manufacturerId);
    data.put("Price", price);
    data.put("Currency", currency);
    data.put("ModelYear", modelYear);
    data.put("Updated", updated);
    data.put("ImagePath", imagePath);
    
    return data;
  }
  
  public Map<String, Object> getManufacturer(int id) {
    Map<String, Object> data = null;
    Calendar date = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
    switch (id) {
    case 1:
      Map<String, Object> addressStar = createAddress("Star Street 137", "Stuttgart", "70173", "Germany");
      date.set(1954, 7, 4);
      data = createManufacturer(1, "Star Powered Racing", addressStar, date);
      break;
      
    case 2:
      Map<String, Object> addressHorse = createAddress("Horse Street 1", "Maranello", "41053", "Italy");
      date.set(1929, 11, 16);
      data = createManufacturer(2, "Horse Powered Racing", addressHorse, date);
      break;
      
    default:
      break;
    }
    
    return data;
  }

  private Map<String, Object> createManufacturer(int id, String name, Map<String, Object> address, Calendar updated) {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("Id", id);
    data.put("Name", name);
    data.put("Address", address);
    data.put("Updated", updated);
    return data;
  }
  
  private Map<String, Object> createAddress(String street, String city, String zipCode, String country) {
    Map<String, Object> address = new HashMap<String, Object>();
    address.put("Street", street);
    address.put("City", city);
    address.put("ZipCode", zipCode);
    address.put("Country", country);
    return address;
  }

  public Map<String, Object> getDriver(int id) {
    Map<String, Object> data = null;
    Calendar updated = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    
    switch (id) {
    case 1:
      updated.set(2012, 12, 19);
      data = createDriver(id, "Michael", "Shoemaker", "The One", null, updated);
      break;
      
    case 2:
      updated.set(2012, 12, 18);
      data = createDriver(id, "Nico", "Horsehill", null, 2, updated);
      break;

    case 3:
      updated.set(2011, 11, 11);
      data = createDriver(id, "Kimi", "Heikkinen", "Iceman", 3, updated);
      break;

    default:
      break;
    }
    
    return data;
  }

  private Map<String, Object> createDriver(int id, String name, String surname, String nickname, Integer carId, Calendar updated) {
    Map<String, Object> data = new HashMap<String, Object>();
    data.put("Id", id);
    data.put("Name", name);
    data.put("Surname", surname);
    data.put("Nickname", nickname);
    data.put("CarId", carId);
    data.put("Updated", updated);
    return data;
  }

  public List<Map<String, Object>> getDrivers() {
    List<Map<String, Object>> drivers = new ArrayList<Map<String, Object>>();
    drivers.add(getDriver(1));
    drivers.add(getDriver(2));
    drivers.add(getDriver(3));
    return drivers;
  }

  public List<Map<String, Object>> getCars() {
    List<Map<String, Object>> cars = new ArrayList<Map<String, Object>>();
    cars.add(getCar(1));
    cars.add(getCar(2));
    cars.add(getCar(3));
    cars.add(getCar(4));
    cars.add(getCar(5));
    return cars;
  }
  
  public List<Map<String, Object>> getManufacturers() {
    List<Map<String, Object>> manufacturers = new ArrayList<Map<String, Object>>();
    manufacturers.add(getManufacturer(1));
    manufacturers.add(getManufacturer(2));
    return manufacturers;
  }


  public List<Map<String, Object>> getCarsFor(int manufacturerId) {
    List<Map<String, Object>> cars = getCars();
    List<Map<String, Object>> carsForManufacturer = new ArrayList<Map<String,Object>>();
    
    for (Map<String,Object> car: cars) {
      if(Integer.valueOf(manufacturerId).equals(car.get("ManufacturerId"))) {
        carsForManufacturer.add(car);
      }
    }
    
    return carsForManufacturer;
  }
  
  public Map<String, Object> getManufacturerFor(int carId) {
    Map<String, Object> car = getCar(carId);
    if(car != null) {
      Object manufacturerId = car.get("ManufacturerId");
      if(manufacturerId != null) {
        return getManufacturer((Integer) manufacturerId);
      }
    }
    return null;
  }

  public Map<String, Object> getDriverFor(int carId) {
    List<Map<String, Object>> drivers = getDrivers();
    
    for (Map<String,Object> driver: drivers) {
      if(Integer.valueOf(carId).equals(driver.get("CarId"))) {
        return driver;
      }
    }
    return null;
  }

  public Map<String, Object> getCarFor(int driverKey) {
    Map<String, Object> driver = getDriver(driverKey);
    if(driver != null) {
      Object carId = driver.get("CarId");
      if(carId != null) {
        return getCar((Integer) carId);
      }
    }
    return null;
  }

  public byte[] readDriverImage(EdmEntitySet entitySet, int id) {
    try {
      if(MyEdmProvider.ENTITY_SET_NAME_DRIVERS.equals(entitySet.getName())) {
        int capacity = 1024 * 1000;
        byte[] temp = new byte[capacity];
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("Driver_" + id + ".png");
        if(is == null) {
          return null;
        }

        ByteBuffer result = ByteBuffer.allocate(capacity);
        int read = is.read(temp);
        while (read >= 0) {
          if(result.remaining() < read) {
            ByteBuffer tmpResult = ByteBuffer.allocate(result.capacity() + capacity);
            tmpResult.put(result);
            result = tmpResult;
          }
          result.put(temp, 0, read);
          read = is.read(temp);
        }
        
        return Arrays.copyOf(result.array(), result.position());
      }
    } catch (EdmException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
