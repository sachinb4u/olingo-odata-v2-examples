package com.sap.banking.termdeposit.odata;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotImplementedException;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.info.GetComplexPropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;

import com.sap.banking.termdeposit.beans.DepositRate;
import com.sap.banking.termdeposit.beans.TermDeposit;

public class TermDepositSingleProcessor extends ODataSingleProcessor {

	private  List<Class<?>> validClasses = Arrays.asList(Boolean.class, Date.class, String.class,
			BigDecimal.class);

	private TermDepositStore dbStore = TermDepositStore.getInstance();
	
	/**
	 * Read EntitySet = getAll()
	 */
	@Override
	public ODataResponse readEntitySet(GetEntitySetUriInfo uriInfo, String contentType) throws ODataException {
		EdmEntitySet entitySet = uriInfo.getStartEntitySet();

		// Get records from backend
		List<TermDeposit> termDeposits = dbStore.getTermdeposits();

		// Get properties for EntitySet
		List<Map<String, Object>> props = getPropertiesForList(termDeposits);

		return EntityProvider.writeFeed(contentType, entitySet, props,
				EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
	}
	
	/**
	 * Read Entity = get(id)
	 */
	@Override
	public ODataResponse readEntity(GetEntityUriInfo uriInfo, String contentType) throws ODataException {
		EdmEntitySet entitySet = uriInfo.getStartEntitySet();

		// get id for Entity
		String id = uriInfo.getKeyPredicates().get(0).getLiteral();

		// Get record from Backend
		TermDeposit result = dbStore.getTermDeposit(id);

		// Get Properties for Entity
		Map<String, Object> data = getPropertiesFromObject(result);

		return EntityProvider.writeEntry(contentType, entitySet, data,
				EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
	}
	
	
	/**
	 * Read Entity SimpleProperty = get(id)/Name
	 * 
	 */
	@Override
	public ODataResponse readEntitySimpleProperty(GetSimplePropertyUriInfo uriInfo, String contentType) throws ODataException {

//		// get id for Entity
//		String id = uriInfo.getKeyPredicates().get(0).getLiteral();
//
//		// Get record from Backend
//		TermDeposit result = dbStore.getTermDeposit(id);
//
//		// Get Properties for Entity
//		Map<String, Object> entityProps = getPropertiesFromObject(result);
//
//		// initialize propValue with Parent props
//		Object propValue = entityProps;
//		EdmProperty finalEdmProperty = null;
//		for (EdmProperty edmProperty : uriInfo.getPropertyPath()) {
//			// if properties map present i.e. if ComplexProperty, set new properties as propValue
//			if (propValue instanceof Map) {
//				propValue = ((Map<String, Object>) propValue).get(edmProperty.getName());
//			}
//			finalEdmProperty = edmProperty;
//		}

		Map<EdmProperty, Object> propValMap = readEntityProperty(uriInfo.getKeyPredicates().get(0), uriInfo.getPropertyPath());

		return EntityProvider.writeProperty(contentType, (EdmProperty) propValMap.keySet().toArray()[0], propValMap.values().toArray()[0]);
	}

	
	@Override
	public ODataResponse readEntityComplexProperty(GetComplexPropertyUriInfo uriInfo, String contentType) throws ODataException {
//		String id = uriInfo.getKeyPredicates().get(0).getLiteral();
//		TermDeposit result = dbStore.getTermDeposit(id);
//		Map<String, Object> data = getPropertiesFromObject(result);
//
//		Object propValue = data;
//		EdmProperty finalEdmProperty = null;
//		for (EdmProperty edmProperty : uriInfo.getPropertyPath()) {
//			if (propValue instanceof Map) {
//				propValue = ((Map<String, Object>) propValue).get(edmProperty.getName());
//			}
//			finalEdmProperty = edmProperty;
//		}

		Map<EdmProperty, Object> propValMap = readEntityProperty(uriInfo.getKeyPredicates().get(0), uriInfo.getPropertyPath());

		return EntityProvider.writeProperty(contentType, (EdmProperty) propValMap.keySet().toArray()[0], propValMap.values().toArray()[0]);
	}
	
	

	@Override
	public ODataResponse readEntitySimplePropertyValue(GetSimplePropertyUriInfo uriInfo, String contentType)
			throws ODataException {
//		String id = uriInfo.getKeyPredicates().get(0).getLiteral();
//		TermDeposit result = dbStore.getTermDeposit(id);
//		Map<String,Object> data = getPropertiesFromObject(result);
//	
//		Object propValue = data;
//		EdmProperty finalEdmProperty = null;
//		for(EdmProperty edmProperty: uriInfo.getPropertyPath()) {
//			if(propValue instanceof Map) {
//				propValue = ((Map<String, Object>)propValue).get(edmProperty.getName());
//			}
//			finalEdmProperty = edmProperty;
//		}
		
		Map<EdmProperty, Object> propValMap = readEntityProperty(uriInfo.getKeyPredicates().get(0), uriInfo.getPropertyPath());
		
		return EntityProvider.writePropertyValue((EdmProperty) propValMap.keySet().toArray()[0], propValMap.values().toArray()[0]);
	}
	
	private Map<EdmProperty, Object> readEntityProperty(KeyPredicate key, List<EdmProperty> propertyPaths) throws ODataException {
		// get id for Entity
		String id = key.getLiteral();

		// Get record from Backend
		TermDeposit result = dbStore.getTermDeposit(id);

		// Get Properties for Entity
		Map<String, Object> entityProps = getPropertiesFromObject(result);

		// initialize propValue with Parent props
		Object propValue = entityProps;
		EdmProperty finalEdmProperty = null;
		for (EdmProperty edmProperty : propertyPaths) {
			// if properties map present i.e. if ComplexProperty, set new properties as
			// propValue
			if (propValue instanceof Map) {
				propValue = ((Map<String, Object>) propValue).get(edmProperty.getName());
			}
			finalEdmProperty = edmProperty;
		}

		Map<EdmProperty, Object> edmPropertyValueMap = new HashMap<>();
		edmPropertyValueMap.put(finalEdmProperty, propValue);

		return edmPropertyValueMap;
	}
	
	@Override
	public ODataResponse createEntity(PostUriInfo uriInfo, InputStream content, String requestContentType,
			String contentType) throws ODataException {
		
		// No support for creating and linking new entry
		if(uriInfo.getNavigationSegments().size()>0) {
			throw new ODataNotImplementedException();
		}
		
		// No support for media resources
		if(uriInfo.getStartEntitySet().getEntityType().hasStream()) {
			throw new ODataNotImplementedException();
		}
		
		EntityProviderReadProperties properties = EntityProviderReadProperties.init().mergeSemantic(false).build();
		
		ODataEntry entry = EntityProvider.readEntry(requestContentType, uriInfo.getStartEntitySet(), content, properties);
		//if something	 goes wrong in deserialization this is managed via the ExceptionMapper
        //no need for an application to do exception handling here an convert the exceptions in HTTP exceptions
		
		Map<String, Object> requestProperties = entry.getProperties();
		
		Map<String, Object> result = null;
		// create Object from
		try {
			EdmEntityType entityType = uriInfo.getStartEntitySet().getEntityType();
			Class<?> entitClass = Class.forName(entityType.toString());
			
			Object object = entitClass.getConstructors()[0].newInstance(new Object[] {});
			setPropertiesToObject(object, requestProperties);
			
			TermDeposit newTermDeposit = dbStore.addTermDeposit((TermDeposit)object);
			
			result = getPropertiesFromObject(newTermDeposit);
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return EntityProvider.writeEntry(contentType, uriInfo.getStartEntitySet(), result, EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
	}
	
	private String propertyName(Method m ) {
		return m.getName().substring(3);
	}
	
	
	/**
	 * Get List of Map
	 * 
	 * @param objects
	 * @return
	 */
	private List<Map<String,Object>> getPropertiesForList(List<?> objects){
		return objects.stream()
			.map(this::getPropertiesFromObject)
			.collect(toList());
	}
	
	private Object setPropertiesToObject(Object source, Map<String, Object> props) {
		
		Stream.of(source.getClass().getMethods())
			.filter(this::isSetterMethod)
			.filter(m -> props.keySet().contains(propertyName(m)))
			.forEach(m -> invokeSetMethod(m, source, props.get(propertyName(m))));
		return source;
	}

	private Object invokeSetMethod(Method m, Object obj, Object arg) {
		try {
			if(arg instanceof Map) {
				Class<?> complexTypeClass = m.getParameterTypes()[0];
				Object childObj = complexTypeClass.newInstance();
				arg = setPropertiesToObject(childObj, (Map<String, Object>) arg);
			}
			m.invoke(obj, arg);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		return obj;
	}
	
	
	/**
	 * Get Map of Property Name and Value from Object
	 * 
	 * @param object
	 * @return
	 */
	private  Map<String, Object> getPropertiesFromObject(Object object) {

		Map<String, Object> propertiesMap = Stream.of(object.getClass().getMethods())
				.filter(this::isGetMethod)
				.filter(m -> invokeGetMethod(m, object) != null)
				.collect(toMap(this::getPropertyNameFromMethod, m -> invokeGetMethod(m, object)));

		return propertiesMap;
	}

	
	/**
	 * Invoke methods of Entity
	 * 
	 * @param m
	 * @param obj
	 * @return
	 */
	private  Object invokeGetMethod(Method m, Object obj) {
		Object result = null;
		try {
			result = m.invoke(obj, new Object[] {});
			if(result instanceof DepositRate) {
				result = getPropertiesFromObject(result);
			}
		} catch (Exception ex) {
		}
		return result;
	}

	

	/**
	 * Is valid supported return Type
	 * 
	 * @param m
	 * @return
	 */
	private  boolean isValidReturnType(Method m) {
		return validClasses.contains(m.getReturnType());
	}

	
	/**
	 * Get Property name from method e.g. getOpenDate => OpenDate
	 * @param m
	 * @return
	 */
	private  String getPropertyNameFromMethod(Method m) {
		return m.getName().substring(3);
	}

	
	/**
	 * Check if method is get or is method
	 * @param m
	 * @return
	 */
	private  boolean isGetMethod(Method m) {
		return !"getClass".equals(m.getName()) && m.getName().startsWith("get") || m.getName().startsWith("is");
	}
	
	/**
	 * Check if method is setter
	 * @param m
	 * @return
	 */
	private boolean isSetterMethod(Method m) {
		return m.getName().startsWith("set");
	}
}
