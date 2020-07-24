package com.sap.banking.termdeposit.odata;

import java.net.URI;
import java.util.Map;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.EntityProviderWriteProperties;
import org.apache.olingo.odata2.api.ep.callback.OnWriteEntryContent;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackContext;
import org.apache.olingo.odata2.api.ep.callback.WriteEntryCallbackResult;
import org.apache.olingo.odata2.api.exception.ODataApplicationException;

import com.sap.banking.termdeposit.beans.Account;
import com.sap.banking.termdeposit.beans.TermDeposit;

public class EntityExpandCallback implements OnWriteEntryContent {

	private TermDepositStore store;
	private URI serviceRoot;

	public EntityExpandCallback(TermDepositStore dbStore, URI serviceRoot) {
		this.store = dbStore;
		this.serviceRoot = serviceRoot;
	}

	@Override
	public WriteEntryCallbackResult retrieveEntryResult(WriteEntryCallbackContext context) throws ODataApplicationException {

		WriteEntryCallbackResult result = new WriteEntryCallbackResult();

		if (isNavigationFrom(context, "TermDeposits", "FromAccount") || isNavigationFrom(context, "TermDeposits", "ToAccount")) {

			try {
				EntityProviderWriteProperties inlineProperties = EntityProviderWriteProperties.serviceRoot(serviceRoot)
						.expandSelectTree(context.getCurrentExpandSelectTreeNode()).build();

				// Get Source Entity Id
				String id = context.extractKeyFromEntryData().get("Id").toString();

				// Get Entity from backend
				TermDeposit dbTermDeposit = store.getTermDeposit(id);
				Account account = null;
				if (context.getNavigationProperty().getName().equals("FromAccount")) {
					account = dbTermDeposit.getFromAccount();
				} else if (context.getNavigationProperty().getName().equals("ToAccount")) {
					account = dbTermDeposit.getToAccount();
				}

				// Get Properties from object
				Map<String, Object> termDepProperties = TermDepositSingleProcessor.getPropertiesFromObject(account);

				result.setEntryData(termDepProperties);
				result.setInlineProperties(inlineProperties);

			} catch (EntityProviderException | EdmException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	private boolean isNavigationFrom(WriteEntryCallbackContext context, String entitySetName, String navigationPropertyName) {
		try {
			return (entitySetName != null && navigationPropertyName != null) && entitySetName.equals(context.getSourceEntitySet().getName())
					&& navigationPropertyName.equals(context.getNavigationProperty().getName());
		} catch (EdmException e) {
			e.printStackTrace();
		}
		return false;
	}

}
