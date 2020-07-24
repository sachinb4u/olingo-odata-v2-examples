package com.sap.banking.termdeposit.odata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.banking.termdeposit.beans.DepositRate;
import com.sap.banking.termdeposit.beans.TermDeposit;

public class TermDepositStore {
	
	
	private Map<String, TermDeposit> termsDb = new ConcurrentHashMap<String, TermDeposit>();
	private static TermDepositStore instance = new TermDepositStore();
	
	private TermDepositStore() {
		setup();
	}
	
	public static TermDepositStore getInstance() {
		return instance;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<TermDeposit> getTermdeposits(){
		return new ArrayList(termsDb.values());
	}
	
	
	public TermDeposit getTermDeposit(String id) {
		return termsDb.get(id);
	}

	private void setup() {
		TermDeposit dep1 = new TermDeposit();
		
		dep1.setCloseDate(new Date());
		dep1.setCustomerId("1234");
		dep1.setFromAccountId("Savings-Account1");
		dep1.setId("32432");
		dep1.setInterestAmount(BigDecimal.valueOf(231));
		dep1.setMaturityDate(new Date());
		dep1.setOpenDate(new Date());
		dep1.setPrincipalAmount(BigDecimal.valueOf(10234));
		dep1.setTenure("5 years");
		dep1.setToAccountId("23235");
		
		DepositRate interestRate = new DepositRate();
		interestRate.setDate(new Date());
		interestRate.setRate(7.75);
		interestRate.setTerm("5 Years");
		
		dep1.setInterestRate(interestRate);
		
		termsDb.put(dep1.getId(), dep1);
	}

	public TermDeposit addTermDeposit(TermDeposit deposit) {
		if (deposit.getId() != null && termsDb.containsKey(deposit.getId())) {
			return termsDb.put(deposit.getId(), deposit);
		} else {
			deposit.setId(UUID.randomUUID().toString());
			termsDb.put(deposit.getId(), deposit);
			return termsDb.get(deposit.getId());
		}
	}

	public boolean delete(String id) {

		return termsDb.remove(id) != null;
	}
}
