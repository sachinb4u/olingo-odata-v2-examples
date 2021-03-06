package com.sap.banking.termdeposit.odata;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.olingo.odata2.api.ODataCallback;
import org.apache.olingo.odata2.api.batch.BatchHandler;
import org.apache.olingo.odata2.api.batch.BatchRequestPart;
import org.apache.olingo.odata2.api.batch.BatchResponsePart;
import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.edm.EdmEntitySet;
import org.apache.olingo.odata2.api.edm.EdmEntityType;
import org.apache.olingo.odata2.api.edm.EdmProperty;
import org.apache.olingo.odata2.api.ep.EntityProvider;
import org.apache.olingo.odata2.api.ep.EntityProviderBatchProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderReadProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties.ODataEntityProviderPropertiesBuilder;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.exception.ODataNotFoundException;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.api.uri.ExpandSelectTreeNode;
import org.apache.olingo.odata2.api.uri.KeyPredicate;
import org.apache.olingo.odata2.api.uri.NavigationSegment;
import org.apache.olingo.odata2.api.uri.PathInfo;
import org.apache.olingo.odata2.api.uri.UriParser;
import org.apache.olingo.odata2.api.uri.info.DeleteUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetComplexPropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntitySetUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetEntityUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetMediaResourceUriInfo;
import org.apache.olingo.odata2.api.uri.info.GetSimplePropertyUriInfo;
import org.apache.olingo.odata2.api.uri.info.PostUriInfo;
import org.apache.olingo.odata2.api.uri.info.PutMergePatchUriInfo;

import com.sap.banking.termdeposit.beans.Account;
import com.sap.banking.termdeposit.beans.DepositRate;
import com.sap.banking.termdeposit.beans.TermDeposit;
import com.sap.banking.termdeposit.service.TermDepositStore;

public class TermDepositSingleProcessor extends ODataSingleProcessor {

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

		if (uriInfo.getNavigationSegments().size() == 0) {
			EdmEntitySet entitySet = uriInfo.getStartEntitySet();

			// get id for Entity
			String id = uriInfo.getKeyPredicates().get(0).getLiteral();

			// Get record from Backend
			TermDeposit result = dbStore.getTermDeposit(id);

			if (result == null) {
				return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
			}

			// register Callback for expand
			URI serviceRoot = getContext().getPathInfo().getServiceRoot();
			Map<String, ODataCallback> callbacks = new HashMap<>();
			callbacks.put("FromAccount", new EntityExpandCallback(dbStore, serviceRoot));
			callbacks.put("ToAccount", new EntityExpandCallback(dbStore, serviceRoot));
			
			ODataEntityProviderPropertiesBuilder propertiesBuilder = EntityProviderWriteProperties.serviceRoot(serviceRoot);
			ExpandSelectTreeNode expandSelectTreeNode = UriParser.createExpandSelectTree(uriInfo.getSelect(), uriInfo.getExpand());
			propertiesBuilder.expandSelectTree(expandSelectTreeNode).callbacks(callbacks);

			// Get Properties for Entity
			Map<String, Object> data = getPropertiesFromObject(result);

			return EntityProvider.writeEntry(contentType, entitySet, data, propertiesBuilder.build());
		} else if (uriInfo.getNavigationSegments().size() == 1) {
			EdmEntitySet entitySet = uriInfo.getStartEntitySet();
			// get id for Entity
			String id = uriInfo.getKeyPredicates().get(0).getLiteral();

			// Get record from Backend
			TermDeposit result = dbStore.getTermDeposit(id);

			if (result == null) {
				return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
			}
			NavigationSegment navSegment = uriInfo.getNavigationSegments().get(0);
			Account account = null;
			if (navSegment.getNavigationProperty().getName().equals("FromAccount")) {
				account = dbStore.getAccount(result.getFromAccountId());
			} else if (navSegment.getNavigationProperty().getName().equals("ToAccount")) {
				account = dbStore.getAccount(result.getToAccountId());
			}
			// Get Properties for Entity
			Map<String, Object> data = getPropertiesFromObject(account);

			return EntityProvider.writeEntry(contentType, entitySet.getRelatedEntitySet(navSegment.getNavigationProperty()), data,
					EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());

		}
		throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
	}

	/**
	 * Read Entity SimpleProperty = get(id)/Name
	 * 
	 */
	@Override
	public ODataResponse readEntitySimpleProperty(GetSimplePropertyUriInfo uriInfo, String contentType) throws ODataException {

		Map<EdmProperty, Object> propValMap = readEntityProperty(uriInfo.getKeyPredicates().get(0), uriInfo.getPropertyPath());

		return EntityProvider.writeProperty(contentType, (EdmProperty) propValMap.keySet().toArray()[0], propValMap.values().toArray()[0]);
	}

	@Override
	public ODataResponse readEntityComplexProperty(GetComplexPropertyUriInfo uriInfo, String contentType) throws ODataException {

		Map<EdmProperty, Object> propValMap = readEntityProperty(uriInfo.getKeyPredicates().get(0), uriInfo.getPropertyPath());

		return EntityProvider.writeProperty(contentType, (EdmProperty) propValMap.keySet().toArray()[0], propValMap.values().toArray()[0]);
	}

	@Override
	public ODataResponse readEntitySimplePropertyValue(GetSimplePropertyUriInfo uriInfo, String contentType) throws ODataException {

		Map<EdmProperty, Object> propValMap = readEntityProperty(uriInfo.getKeyPredicates().get(0), uriInfo.getPropertyPath());

		return EntityProvider.writePropertyValue((EdmProperty) propValMap.keySet().toArray()[0], propValMap.values().toArray()[0]);
	}

	/**
	 * Read Entity Property - Simple, Complex, Nested
	 * 
	 * @param key
	 * @param propertyPaths
	 * @return
	 * @throws ODataException
	 */
	@SuppressWarnings("unchecked")
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

	/**
	 * Create Entity = / + Post
	 */
	@Override
	public ODataResponse createEntity(PostUriInfo uriInfo, InputStream content, String requestContentType, String contentType) throws ODataException {

		ODataEntry entry = EntityProvider.readEntry(requestContentType, uriInfo.getStartEntitySet(), content,
				EntityProviderReadProperties.init().mergeSemantic(false).build());

		Map<String, Object> requestEntityProperties = entry.getProperties();

		Map<String, Object> createResult = null;

		try {
			EdmEntityType entityType = uriInfo.getStartEntitySet().getEntityType();

			// Fully Qualified Name = EntityType string representation i.e. Namespace + Name
			Class<?> entitClass = Class.forName(entityType.toString());

			// Create Entity Object
			Object entityObject = entitClass.getConstructors()[0].newInstance(new Object[] {});
			// set Properties from request
			setPropertiesToObject(entityObject, requestEntityProperties);

			// Persist entity - backend call - expected to set Id
			TermDeposit dbTermDeposit = dbStore.addTermDeposit((TermDeposit) entityObject);

			// get Properties for newly created Entity
			createResult = getPropertiesFromObject(dbTermDeposit);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return EntityProvider.writeEntry(contentType, uriInfo.getStartEntitySet(), createResult,
				EntityProviderWriteProperties.serviceRoot(getContext().getPathInfo().getServiceRoot()).build());
	}

	/**
	 * Update entity /(id) - PUT
	 * 
	 */
	@Override
	public ODataResponse updateEntity(PutMergePatchUriInfo uriInfo, InputStream content, String requestContentType, boolean merge, String contentType)
			throws ODataException {

		ODataEntry entry = EntityProvider.readEntry(requestContentType, uriInfo.getStartEntitySet(), content,
				EntityProviderReadProperties.init().mergeSemantic(false).build());

		// Get request Entity Properties
		Map<String, Object> requestEntityProperties = entry.getProperties();

		// Get Entity from DB
		String id = uriInfo.getKeyPredicates().get(0).getLiteral();
		TermDeposit dbTermDeposit = dbStore.getTermDeposit(id);

		if (id == null || dbTermDeposit == null) {
			return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
		}

		// update/merge request Properties to DB object
		setPropertiesToObject(dbTermDeposit, requestEntityProperties);

		// Store updated object to database
		dbStore.addTermDeposit(dbTermDeposit);

		return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
	}

	/**
	 * Delete entity /(id) - DELETE
	 * 
	 */
	@Override
	public ODataResponse deleteEntity(DeleteUriInfo uriInfo, String contentType) throws ODataException {
		if ("TermDeposits".equals(uriInfo.getTargetEntitySet().getName())) {
			String id = uriInfo.getKeyPredicates().get(0).getLiteral();

			boolean isDeleted = dbStore.delete(id);

			if (isDeleted) {
				return ODataResponse.status(HttpStatusCodes.NO_CONTENT).build();
			}
			return ODataResponse.status(HttpStatusCodes.NOT_FOUND).build();
		}
		return super.deleteEntity(uriInfo, contentType);
	}

	/**
	 * Execute batch
	 * 
	 */
	@Override
	public ODataResponse executeBatch(BatchHandler handler, String contentType, InputStream content) throws ODataException {
		PathInfo pathInfo = getContext().getPathInfo();
		List<BatchResponsePart> batchResponsesParts = new ArrayList<>();

		EntityProviderBatchProperties batchProperties = EntityProviderBatchProperties.init().pathInfo(pathInfo).build();
		List<BatchRequestPart> batchParts = EntityProvider.parseBatchRequest(contentType, content, batchProperties);

		for (BatchRequestPart batchRequestPart : batchParts) {
			batchResponsesParts.add(handler.handleBatchPart(batchRequestPart));
		}

		return EntityProvider.writeBatchResponse(batchResponsesParts);
	}

	/**
	 * Execute changeSet
	 * 
	 */
	@Override
	public BatchResponsePart executeChangeSet(BatchHandler handler, List<ODataRequest> requests) throws ODataException {

		List<ODataResponse> responses = new ArrayList<>();

		for (ODataRequest oDataRequest : requests) {
			ODataResponse oDataResponse = handler.handleRequest(oDataRequest);
			if (oDataResponse.getStatus().getStatusCode() >= HttpStatusCodes.BAD_REQUEST.getStatusCode()) {
				// Rollback
				List<ODataResponse> errorResponses = new ArrayList<>();
				errorResponses.add(oDataResponse);

				// If a request within a ChangeSet fails, a Batch Response Part contains only
				// the error response and the flag changeSet is set to false.
				return BatchResponsePart.responses(errorResponses).changeSet(false).build();
			}
			responses.add(oDataResponse);
		}

		return BatchResponsePart.responses(responses).changeSet(true).build();
	}

	/**
	 * Download Term Deposit Certificate
	 * 
	 */
	@Override
	public ODataResponse readEntityMedia(GetMediaResourceUriInfo uriInfo, String contentType) throws ODataException {
		final EdmEntitySet entitySet = uriInfo.getStartEntitySet();
		if ("TermDeposits".equals(entitySet.getName())) {
			String termDepositId = uriInfo.getKeyPredicates().get(0).getLiteral();
			byte[] depositCertificate = dbStore.getTermDepositCertificate(termDepositId);
			if (depositCertificate == null) {
				throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
			}
			String mimeType = "application/pdf";
			String contentDespositionHeader = "Content-disposition";
			String attachmentFileName = "attachment; filename=term-deposit-certificate-" + termDepositId + ".pdf";
			return ODataResponse.fromResponse(EntityProvider.writeBinary(mimeType, depositCertificate)).header(contentDespositionHeader, attachmentFileName)
					.build();

		}
		return super.readEntityMedia(uriInfo, contentType);
	}

	/**
	 * Set Properties from Request to Entity Object
	 * 
	 * @param source
	 * @param props
	 * @return
	 */
	private Object setPropertiesToObject(Object source, Map<String, Object> props) {

		Stream.of(source.getClass().getMethods())
				// only setter methods
				.filter(this::isSetterMethod)
				// only setter for which request Property present
				.filter(m -> props.keySet().contains(propertyName(m)))
				// invoke setter for every request property with value from request
				.forEach(m -> invokeSetMethod(m, source, props.get(propertyName(m))));

		return source;
	}

	/**
	 * Get Property Name from setter method name
	 * 
	 * @param m
	 * @return
	 */
	private String propertyName(Method m) {
		return m.getName().substring(3);
	}

	/**
	 * Invoke setter method on EntityObject with arg as values. This is called
	 * recursively to fill Complex Types
	 * 
	 * @param m
	 * @param obj
	 * @param methodArgument
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Object invokeSetMethod(Method m, Object obj, Object methodArgument) {
		try {
			// if prop value / arg is Map i.e. Property has ComplexType
			if (methodArgument instanceof Map) {
				// Create ComplexType object
				Class<?> complexTypeClass = m.getParameterTypes()[0];
				Object complexTypeObj = complexTypeClass.newInstance();

				// Get ComplexType Object from properties. Note - this is recursive call so it
				// will work for nested complex types
				methodArgument = setPropertiesToObject(complexTypeObj, (Map<String, Object>) methodArgument);
			}
			// call setter - for COmplex type above if block gets the Object
			if (methodArgument instanceof Calendar) {
				m.invoke(obj, ((Calendar) methodArgument).getTime());
			} else {
				m.invoke(obj, methodArgument);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return obj;
	}

	/**
	 * Get Properties for List of Objects
	 * 
	 * @param objects
	 * @return
	 */
	public static List<Map<String, Object>> getPropertiesForList(List<?> objects) {
		return objects.stream().map(TermDepositSingleProcessor::getPropertiesFromObject).collect(toList());
	}

	/**
	 * Get Map of Property Name and Value from Object
	 * 
	 * @param object
	 * @return
	 */
	public static Map<String, Object> getPropertiesFromObject(Object object) {

		Map<String, Object> propertiesMap = Stream.of(object.getClass().getMethods()).filter(TermDepositSingleProcessor::isGetMethod)
				.filter(m -> invokeGetMethod(m, object) != null)
				.collect(toMap(TermDepositSingleProcessor::getPropertyNameFromMethod, m -> invokeGetMethod(m, object)));

		return propertiesMap;
	}

	/**
	 * Invoke methods of Entity
	 * 
	 * @param m
	 * @param obj
	 * @return
	 */
	private static Object invokeGetMethod(Method m, Object obj) {
		Object result = null;
		try {
			result = m.invoke(obj, new Object[] {});
			if (result instanceof DepositRate || result instanceof Account) {
				result = getPropertiesFromObject(result);
			}
		} catch (Exception ex) {
		}
		return result;
	}

	/**
	 * Get Property name from method e.g. getOpenDate => OpenDate
	 * 
	 * @param m
	 * @return
	 */
	private static String getPropertyNameFromMethod(Method m) {
		return m.getName().substring(3);
	}

	/**
	 * Check if method is get or is method
	 * 
	 * @param m
	 * @return
	 */
	private static boolean isGetMethod(Method m) {
		return !"getClass".equals(m.getName()) && m.getName().startsWith("get") || m.getName().startsWith("is");
	}

	/**
	 * Check if method is setter
	 * 
	 * @param m
	 * @return
	 */
	private boolean isSetterMethod(Method m) {
		return m.getName().startsWith("set");
	}
}
