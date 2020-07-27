package com.sap.banking.termdeposit.service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.banking.termdeposit.beans.Account;
import com.sap.banking.termdeposit.beans.DepositRate;
import com.sap.banking.termdeposit.beans.TermDeposit;

public class TermDepositStore {
	
	private Map<String, Account> accountDb = new ConcurrentHashMap<String, Account>();
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
		dep1.setId("32432");
		dep1.setInterestAmount(BigDecimal.valueOf(231));
		dep1.setMaturityDate(new Date());
		dep1.setOpenDate(new Date());
		dep1.setPrincipalAmount(BigDecimal.valueOf(10234));
		dep1.setTenure("5 years");
		dep1.setStatus("Pending");
		
		DepositRate interestRate = new DepositRate();
		interestRate.setDate(new Date());
		interestRate.setRate(7.75);
		interestRate.setTerm("5 Years");
		
		dep1.setInterestRate(interestRate);
		
		Account fromAccount = new Account();
		fromAccount.setId("343434");
		fromAccount.setAccountNumber("from-12334");
		fromAccount.setBankName("ICICI Bank");
		fromAccount.setType("Savings");
		fromAccount.setCurrencyCode("INR");
		fromAccount.setCurrentBalance(BigDecimal.valueOf(4232424.23));
		fromAccount.setStatus("Active");

		dep1.setFromAccountId(fromAccount.getId());
		accountDb.put(fromAccount.getId(), fromAccount);

		Account toAccount = new Account();
		toAccount.setId("32432");
		toAccount.setAccountNumber("Deposit-4543");
		toAccount.setBankName("ICICI Bank");
		toAccount.setCurrencyCode("INR");
		toAccount.setCurrentBalance(BigDecimal.valueOf(100000));
		toAccount.setStatus("Pending");
		toAccount.setType("Deposit");

		dep1.setToAccountId(toAccount.getId());
		accountDb.put(toAccount.getId(), toAccount);

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

	public List<Account> getAccounts() {
		return new ArrayList<Account>(accountDb.values());
	}

	public Account getAccount(String id) {
		return accountDb.get(id);
	}

	public byte[] getTermDepositCertificate(String id) {
		String certFileName = "certificates/term-deposit-certificate-" + id + ".pdf";
		final int CAPACITY = 1024 * 1000;

		try {
			InputStream fis = this.getClass().getClassLoader().getResourceAsStream(certFileName);
			if (fis == null) {
				return null;
			}
			ByteBuffer buffer = ByteBuffer.allocate(CAPACITY);
			byte[] bytesArr = new byte[CAPACITY];

			int read = fis.read(bytesArr);
			while (read >= 0) {
				if (buffer.remaining() < read) {
					ByteBuffer tmpBuffer = ByteBuffer.allocate(buffer.capacity() + CAPACITY);
					tmpBuffer.put(buffer);
					buffer = tmpBuffer;
				}
				buffer.put(bytesArr, 0, read);
				read = fis.read(bytesArr);
			}
			return Arrays.copyOf(buffer.array(), buffer.position());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
}
