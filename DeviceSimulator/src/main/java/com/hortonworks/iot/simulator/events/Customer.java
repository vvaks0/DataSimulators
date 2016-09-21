package com.hortonworks.iot.simulator.events;

public class Customer {
	private String accountNumber;
	private String accountType;	
	
	public Customer(){}
	
	public Customer(String accountNumber, String accountType){
		this.accountNumber = accountNumber;
		this.accountType = accountType;
	}
	
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
}
