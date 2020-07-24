package com.sap.banking.termdeposit.odata;

import java.io.InputStream;

import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.ODataServiceFactory;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.apache.olingo.odata2.api.processor.ODataContext;
import org.apache.olingo.odata2.api.rt.RuntimeDelegate;

public class TermDepositServiceFactory extends ODataServiceFactory {

	@Override
	public ODataService createService(ODataContext ctx) throws ODataException {

		return createODataSingleProcessorService(getTermDepositEdmProvider(), new TermDepositSingleProcessor());
	}

	/**
	 * Get TermDeposit EDM from file
	 * 
	 * @return
	 */
	private EdmProvider getTermDepositEdmProvider() {
		try {
			InputStream fis = this.getClass().getClassLoader()
					.getResourceAsStream("com/sap/banking/odata/termdeposit/edm/TermDepositEdm.xml");
			EdmProvider edmProvider = RuntimeDelegate.createEdmProvider(fis, true);
			return edmProvider;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
