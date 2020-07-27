package org.apache.olingo.odata2.sample.service;

import static org.apache.olingo.odata2.sample.service.MyEdmProvider.ENTITY_NAME_CAR;
import static org.apache.olingo.odata2.sample.service.MyEdmProvider.ENTITY_NAME_DRIVER;
import static org.apache.olingo.odata2.sample.service.MyEdmProvider.ENTITY_NAME_MANUFACTURER;
import static org.apache.olingo.odata2.sample.service.MyEdmProvider.ENTITY_SET_NAME_CARS;
import static org.apache.olingo.odata2.sample.service.MyEdmProvider.ENTITY_SET_NAME_DRIVERS;
import static org.apache.olingo.odata2.sample.service.MyEdmProvider.ENTITY_SET_NAME_MANUFACTURERS;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.edm.EdmLiteralKind;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.edm.EdmSimpleType;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties.ODataEntityProviderPropertiesBuilder;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMediaResourceUriInfo;


public class MyODataSingleProcessor extends ODataSingleProcessor {
  
  private final DataStore dataStore;
  
  public MyODataSingleProcessor() {
    dataStore = new DataStore();
  }

  @Override
  public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {

    EdmEntitySet entitySet;

    if (uriInfo.getNavigationSegments().size() == 0) {
      entitySet = uriInfo.getStartEntitySet();

      if (ENTITY_SET_NAME_CARS.equals(entitySet.getName())) {
        return EntityProvider.writeFeed(contentType, entitySet, dataStore.getCars(), EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      } else if (ENTITY_SET_NAME_MANUFACTURERS.equals(entitySet.getName())) {
        return EntityProvider.writeFeed(contentType, entitySet, dataStore.getManufacturers(), EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      } else if (ENTITY_SET_NAME_DRIVERS.equals(entitySet.getName())) {
        return EntityProvider.writeFeed(contentType, entitySet, dataStore.getDrivers(), EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (uriInfo.getNavigationSegments().size() == 1) {
      //navigation first level, simplified example for illustration purposes only
      entitySet = uriInfo.getTargetEntitySet();

      if (ENTITY_SET_NAME_CARS.equals(entitySet.getName())) {
        int manufacturerKey = getKeyValue(uriInfo.getKeyPredicates().get(0));

        List<Map<String, Object>> cars = new ArrayList<Map<String, Object>>();
        cars.addAll(dataStore.getCarsFor(manufacturerKey));

        return EntityProvider.writeFeed(contentType, entitySet, cars, EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
    }

    throw new ODataNotImplementedException();
  }

  @Override
  public ODataResponse readEntity(GetEntityUriInfo uriInfo, String contentType) throws ODataException {

    if (uriInfo.getNavigationSegments().size() == 0) {
      EdmEntitySet entitySet = uriInfo.getStartEntitySet();

      if (ENTITY_SET_NAME_CARS.equals(entitySet.getName())) {
        int id = getKeyValue(uriInfo.getKeyPredicates().get(0));
        Map<String, Object> data = dataStore.getCar(id);

        if (data != null) {
          URI serviceRoot = getContext().getPathInfo().getServiceRoot();
          ODataEntityProviderPropertiesBuilder propertiesBuilder = EntityProviderWriteProperties.serviceRoot(serviceRoot);

          // create and register callback
          Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
          callbacks.put(ENTITY_NAME_MANUFACTURER, new MyCallback(dataStore, serviceRoot));
          callbacks.put(ENTITY_NAME_DRIVER, new MyCallback(dataStore, serviceRoot));
          ExpandSelectTreeNode expandSelectTreeNode = UriParser.createExpandSelectTree(uriInfo.getSelect(), uriInfo.getExpand());
          //
          propertiesBuilder.expandSelectTree(expandSelectTreeNode).callbacks(callbacks);

          return EntityProvider.writeEntry(contentType, entitySet, data, propertiesBuilder.build());
        }
      } else if (ENTITY_SET_NAME_MANUFACTURERS.equals(entitySet.getName())) {
        int id = getKeyValue(uriInfo.getKeyPredicates().get(0));
        Map<String, Object> data = dataStore.getManufacturer(id);

        if (data != null) {
          URI serviceRoot = getContext().getPathInfo().getServiceRoot();
          ODataEntityProviderPropertiesBuilder propertiesBuilder = EntityProviderWriteProperties.serviceRoot(serviceRoot);

          // create and register callback
          Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
          callbacks.put(ENTITY_SET_NAME_CARS, new MyCallback(dataStore, serviceRoot));
          ExpandSelectTreeNode expandSelectTreeNode = UriParser.createExpandSelectTree(uriInfo.getSelect(), uriInfo.getExpand());
          //
          propertiesBuilder.expandSelectTree(expandSelectTreeNode).callbacks(callbacks);

          return EntityProvider.writeEntry(contentType, entitySet, data, propertiesBuilder.build());
        }
      } else if (ENTITY_SET_NAME_DRIVERS.equals(entitySet.getName())) {
        int id = getKeyValue(uriInfo.getKeyPredicates().get(0));
        Map<String, Object> data = dataStore.getDriver(id);

        if (data != null) {
          URI serviceRoot = getContext().getPathInfo().getServiceRoot();
          ODataEntityProviderPropertiesBuilder propertiesBuilder = EntityProviderWriteProperties.serviceRoot(serviceRoot);

          // create and register callback
          Map<String, ODataCallback> callbacks = new HashMap<String, ODataCallback>();
          callbacks.put(ENTITY_NAME_CAR, new MyCallback(dataStore, serviceRoot));
          ExpandSelectTreeNode expandSelectTreeNode = UriParser.createExpandSelectTree(uriInfo.getSelect(), uriInfo.getExpand());
          //
          propertiesBuilder.expandSelectTree(expandSelectTreeNode).callbacks(callbacks);

          return EntityProvider.writeEntry(contentType, entitySet, data, propertiesBuilder.build());
        }
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    } else if (uriInfo.getNavigationSegments().size() == 1) {
      //navigation first level, simplified example for illustration purposes only
      EdmEntitySet entitySet = uriInfo.getTargetEntitySet();
      
      Map<String, Object> data = null;
      
      if (ENTITY_SET_NAME_MANUFACTURERS.equals(entitySet.getName())) {
        int carKey = getKeyValue(uriInfo.getKeyPredicates().get(0));
        data = dataStore.getManufacturerFor(carKey);
      } else if(isAssociation(uriInfo, ENTITY_SET_NAME_CARS, ENTITY_SET_NAME_DRIVERS)) {
        int carKey = getKeyValue(uriInfo.getKeyPredicates().get(0));
        data = dataStore.getDriverFor(carKey);
      } else if(isAssociation(uriInfo, ENTITY_SET_NAME_DRIVERS, ENTITY_SET_NAME_CARS)) {
        int driverKey = getKeyValue(uriInfo.getKeyPredicates().get(0));
        data = dataStore.getCarFor(driverKey);
      }
      
      if(data != null) {
        return EntityProvider.writeEntry(contentType, uriInfo.getTargetEntitySet(), 
            data, EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
      }

      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
    }

    throw new ODataNotImplementedException();
  }
  
  private boolean isAssociation(GetEntityUriInfo uriInfo, String startName, String targetName) throws EdmException {
    if(startName == null || targetName == null) {
      return false;
    }
    EdmEntitySet startEntitySet = uriInfo.getStartEntitySet();
    EdmEntitySet targetEntitySet = uriInfo.getTargetEntitySet();
    
    return startName.equals(startEntitySet.getName()) && targetName.equals(targetEntitySet.getName());
    
  }
  
  @Override
  public ODataResponse readEntityMedia(final GetMediaResourceUriInfo uriInfo, final String contentType) throws ODataException {

    final EdmEntitySet entitySet = uriInfo.getTargetEntitySet();
    if(ENTITY_SET_NAME_DRIVERS.equals(entitySet.getName())) {
      int id = getKeyValue(uriInfo.getKeyPredicates().get(0));
      byte[] image = dataStore.readDriverImage(entitySet, id);
      if (image == null) {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      }
      
      String mimeType = "image/png";
      return ODataResponse.fromResponse(EntityProvider.writeBinary(mimeType, image)).build();
    }
    
    throw new ODataNotImplementedException();
  }


  private int getKeyValue(KeyPredicate key) throws ODataException {
    EdmProperty property = key.getProperty();
    EdmSimpleType type = (EdmSimpleType) property.getType();
    return type.valueOfString(key.getLiteral(), EdmLiteralKind.DEFAULT, property.getFacets(), Integer.class);
  }
}
