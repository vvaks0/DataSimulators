package com.hortonworks.iot.simulator.types;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.hortonworks.iot.simulator.events.Customer;
import com.hortonworks.iot.simulator.events.InventoryUpdate;
import com.hortonworks.iot.simulator.events.Product;
import com.hortonworks.iot.simulator.events.STBStatus;
import com.hortonworks.iot.simulator.events.StoreTransaction;
import com.hortonworks.iot.simulator.events.StoreTransaction2;

public class RetailStoreSimulator2 implements Runnable {
    private String serialNumber;
    private String targetIP;
    private String state; 
    private String status;
    private String mode;
    private String shipToState;
    
    private String externalCommand;
    private Integer cyclesCompleted = 0;
    
    private Map<String, String> productDirectory = new HashMap<String, String>();
    private Map<String, Product> inventory = new HashMap<String, Product>();
    private Map<String, Customer> customers = new HashMap<String, Customer>();
    private Map<String, String> shippingInfo = new HashMap<String, String>();
    
    Random random = new Random();
    
    public RetailStoreSimulator2(String deviceSerialNumber, String mode, String targetIP){
        initialize(deviceSerialNumber, mode, targetIP);    
    }
    
    public void run() {
    	try {
			powerOn();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
      
    	while(state.equalsIgnoreCase("on")){
    		Integer incident = random.nextInt(6-1) + 1;
    		try {
    			System.out.println("Dice Roll: " + incident);
    			
    			if(mode.equalsIgnoreCase("training"))
    				runTrainingCycle(incident);
    			else
    				runSimulationCycle(incident);  			
    		} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    }
    
    public void runSimulationCycle(Integer incident) throws InterruptedException{
    	if(incident > 3 ){
			inventoryTheft();
			cyclesCompleted++;
		}
		else{
			normalTransaction();
			cyclesCompleted++;
		}
    }
    
    public void initialize(String deviceSerialNumber, String mode, String targetIP){
        this.serialNumber = deviceSerialNumber; //deviceSpout.getSerialNumber();
        this.targetIP = targetIP;
        state = "off";
        status = "normal";
        externalCommand = "none";
        
        this.inventory.put("11",new Product("11","Electronics","TV","Samsung","X101","1000.00"));
        this.inventory.put("12",new Product("12","Electronics","DVD-Player","LG","J202","250.00"));
        this.inventory.put("13",new Product("13","Electronics","Sound System","Sony","C303","500.00"));
        this.inventory.put("21",new Product("21","Movie","Action","NA","Gladiator", "20.00"));
        this.inventory.put("22",new Product("22","Movie","Comedy","NA","Wedding Crashers","22.00"));
        this.inventory.put("23",new Product("23","Movie","Drama","NA","Peeky Blinders","23.00"));
        this.inventory.put("31",new Product("31","Game","Software","Sony","God of War X", "50.00"));
        this.inventory.put("32",new Product("32","Game","Console","Sony","PlayStation 4","200.00"));
        this.inventory.put("33",new Product("33","Game","Accessory","Microsoft","XBox Controller","65.00"));
        this.inventory.put("41",new Product("41","Music","Hip-Hop","NA","JZ","15.00"));
        this.inventory.put("42",new Product("42","Music","Classic Rock","NA","Guns and Roses","19.00"));
        this.inventory.put("43",new Product("43","Music","Country","NA","Billy Ray Cyris", "14.00"));
        this.inventory.put("51",new Product("51","Software","Game","Activision","X2: Wolverine's Revenge", "45.00"));
        this.inventory.put("52",new Product("52","Software","Eduction","Knowledge Adventure","PlayZone! 4th - 6th Grade - Windows","20.00"));
        this.inventory.put("53",new Product("53","Software","Productivity","Microsoft","Office 360","150.00"));
        
        this.customers.put("1",new Customer("12345","VISA"));
        this.customers.put("2",new Customer("54321","MasterCard"));
        this.customers.put("3",new Customer("67890","AMEX"));
        this.customers.put("4",new Customer("09876","VISA"));
        this.customers.put("5",new Customer("11111","AMEX"));
        
        this.shippingInfo.put("1", "US");
        this.shippingInfo.put("2", "GB");
        this.shippingInfo.put("3", "CN");
        this.shippingInfo.put("4", "RU");
        this.shippingInfo.put("5", "BR");
        
        if(mode.equalsIgnoreCase("training")){	
        	this.mode = mode;
        	System.out.print("******************** Training Mode");
        }
        else{
        	this.mode = "simulation";
        	System.out.print("******************** Simulation Mode");
        }	
    }
    
    public void sendTransaction(Customer currentCustomer, Product product){
    	String transactionTimeStamp = ((Long)Calendar.getInstance().getTimeInMillis()).toString();
    	String transactionId = serialNumber + transactionTimeStamp;
    	
    	StoreTransaction2 storeTransaction = new StoreTransaction2();
    	storeTransaction.setSerialNumber(serialNumber);
    	storeTransaction.setState(state);
    	storeTransaction.setStatus(status);
    	storeTransaction.setAccountNumber(currentCustomer.getAccountNumber());
    	storeTransaction.setAccountType(currentCustomer.getAccountType());
    	storeTransaction.setTransactionId(transactionId);
    	storeTransaction.setCurrency("dollars");
    	storeTransaction.setIsCardPresent("true");
    	storeTransaction.setIpAddress("n/a");
    	storeTransaction.setTransactionTimeStamp(transactionTimeStamp);
    	storeTransaction.setProductId(product.getProductId());
    	storeTransaction.setItemName(product.getItemName());
    	storeTransaction.setItemCategory(product.getItemCategory());
    	storeTransaction.setSubClass(product.getSubClass());
    	storeTransaction.setManufacturer(product.getManufacturer());
    	storeTransaction.setPrice(Double.valueOf(product.getPrice()));
    	storeTransaction.setShipToState(shipToState);
    	
        try{
        	URL url = new URL("http://" + targetIP + ":8082/contentListener");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setDoOutput(true);
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("Content-Type", "application/json");
            System.out.println("To String: " + convertPOJOToJSON(storeTransaction));
            
            OutputStream os = conn.getOutputStream();
    		os.write(convertPOJOToJSON(storeTransaction).getBytes());
    		os.flush();
            
            if (conn.getResponseCode() != 200) {
    			throw new RuntimeException("Failed : HTTP error code : "+ conn.getResponseCode());
    		}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendInventoryUpdate(Product currentProduct){
    	InventoryUpdate inventoryUpdate = new InventoryUpdate();
    	inventoryUpdate.setProductId(currentProduct.getProductId());
    	inventoryUpdate.setLocationId(serialNumber);
    	try{
        	URL url = new URL("http://" + targetIP + ":8082/contentListener");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setDoOutput(true);
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("Content-Type", "application/json");
            System.out.println("To String: " + convertPOJOToJSON(inventoryUpdate));
            
            OutputStream os = conn.getOutputStream();
    		os.write(convertPOJOToJSON(inventoryUpdate).getBytes());
    		os.flush();
            
            if (conn.getResponseCode() != 200) {
    			throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
    		}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void normalTransaction() throws InterruptedException{
    	String inventorySelection = null;
    	String customerSelection = null;
    	Double totalAmount = 0.0;
    	Product currentProduct = new Product();
    	Customer currentCustomer = new Customer();
    	List<String> productList = new ArrayList<String>();
    	int basketSize = random.nextInt(4-1) + 1;
    	System.out.println("Report Inventory Deduction");
    	
    	inventorySelection = ((Integer)(random.nextInt(6-1) + 1)).toString() + ((Integer)(random.nextInt(4-1) + 1)).toString() ;
    	currentProduct = inventory.get(inventorySelection);
    	sendInventoryUpdate(currentProduct);
    	Thread.sleep(1000);
    	
    	System.out.println("Report Financial Transaction");
    	customerSelection = ((Integer)(random.nextInt(6-1) + 1)).toString();
    	currentCustomer = customers.get(customerSelection);
        shipToState = shippingInfo.get(((Integer)(random.nextInt(6-1) + 1)).toString());
    	sendTransaction(currentCustomer, currentProduct);
        Thread.sleep(1000);
    }
    
    public void inventoryTheft() throws InterruptedException{
    	String inventorySelection = null;
    	Product currentProduct = new Product();

    	System.out.println("Report Inventory Deduction");
    	inventorySelection = ((Integer)(random.nextInt(6-1) + 1)).toString() + ((Integer)(random.nextInt(4-1) + 1)).toString() ;
    	currentProduct = inventory.get(inventorySelection);
    	sendInventoryUpdate(currentProduct);
    	Thread.sleep(1000);
    }
    
    public void runTrainingCycle(Integer incident) throws InterruptedException{
    	/*
    	if(incident > 3){
			tempFailCycle();
			//cyclesCompleted++;
		}
		else{
			normalCycle();
			//cyclesCompleted++;
		}
    	 */
    }
    
    public String convertPOJOToJSON(Object pojo) {
    	String jsonString = "";
    	ObjectMapper mapper = new ObjectMapper();

    	try {
    		jsonString = mapper.writeValueAsString(pojo);
    	} catch (JsonGenerationException e) {
    		e.printStackTrace();
    	} catch (JsonMappingException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    	return jsonString;
    }
    
    public void powerOn() throws InterruptedException {
    	state = "on";
    }
    
    public void powerOff() throws InterruptedException{
        state = "off";
    }
    
    public void setSerialNumber(String value){
        serialNumber = value;
    }
    public void setState(String value){
        state = value;
    }
    public void setStatus(String value){
        status = value;
    }
    public void setExternalCommand(String value){
        externalCommand = value;
    }
    
    public String getSerialNumber(){
        return serialNumber;
    }
    public String getState(){
        return state;
    }
    public String getStatus(){
        return status;
    }
    public String getExternalCommand(){
        return externalCommand;
    }
}

