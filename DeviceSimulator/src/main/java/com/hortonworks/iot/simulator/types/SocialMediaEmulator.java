package com.hortonworks.iot.simulator.types;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.hortonworks.iot.simulator.events.SocialMediaStatement;

public class SocialMediaEmulator implements Runnable {
    private String serialNumber;
    private String targetIP;
    private String state; 
    private String status;
    private String mode;
    private Map <String, StatementConstructor> statement = new HashMap<String, StatementConstructor>();
    private Map <String, String> brand = new HashMap<String, String>();
    private Map <String, String> category = new HashMap<String, String>();
    private Map <String, String> ipAddressMenu = new HashMap<String, String>();
    
    Random random = new Random();
    
    public SocialMediaEmulator(String deviceSerialNumber, String mode, String targetIP){
    	System.out.print("******************** Initializing Social Media Simulator");
    	initialize(deviceSerialNumber, mode, targetIP);    
    }
    
    public void run() {
    	try {
			powerOn();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
      
    	while(state.equalsIgnoreCase("on")){
    		Integer incident = random.nextInt(11-1) + 1;
    		try {
    			//System.out.println("Cycle Randomizer: " + incident);
    			
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
    	int orderIndex = 0;
    	int selectedStatement = random.nextInt(11-1) + 1;
    	int selectedBrand = random.nextInt(3-1) + 1;
    	int selectedCategory = random.nextInt(6-1) + 1;
    	int selectedSourceIndexIP = random.nextInt(6-1) + 1;
    	
    	System.out.print("******************** Selecting Statement: " + selectedStatement);
    	StatementConstructor socialStatement = statement.get(String.valueOf(selectedStatement));
    	String[] currentStatement = socialStatement.getStatement();
    	String[] gramaticalOrder = socialStatement.getOrder();
    	
    	for(int i=0; i < currentStatement.length; i++){
    		if(currentStatement[i] == null){
    			if(gramaticalOrder[orderIndex].equalsIgnoreCase("brand")){
    				currentStatement[i] = brand.get(String.valueOf(selectedBrand));
    				orderIndex++;
    			}else if(gramaticalOrder[orderIndex].equalsIgnoreCase("category")){
    				currentStatement[i] = category.get(String.valueOf(selectedCategory));
    				orderIndex++;
    			}
    		}
    	}
    	
    	socialStatement.setStatement(currentStatement);
    	
    	System.out.print("******************** Selecting Source Region: " + selectedSourceIndexIP);
    	String statementSourceIP = ipAddressMenu.get(String.valueOf(selectedSourceIndexIP));
    	
    	System.out.println("******************** Generated new social media statement... sending");
    	sendStatus(String.join(" ", currentStatement), statementSourceIP);
    	Thread.sleep(1000);
    }
    
    public void runTrainingCycle(Integer incident) throws InterruptedException{

    }
    
    public void initialize(String deviceSerialNumber, String mode, String targetIP){        
    	this.serialNumber = deviceSerialNumber; //deviceSpout.getSerialNumber();
        this.targetIP = targetIP;
        this.state = "off";
        this.status = "normal";
        
        this.category.put("1","Electronics");
        this.category.put("2","Movies");
        this.category.put("3","Games");
        this.category.put("4","Music");
        this.category.put("5","Software");
        
        this.brand.put("1", "Rays");
        this.brand.put("2", "Best-Stuff");
        
        this.statement.put("1", new StatementConstructor(
        		new String[]{null,"has the best",null},
        		new String[]{"brand","category"}));
        this.statement.put("2", new StatementConstructor(
        		new String[]{"Always seem to find the best",null,"at",null}, 
        		new String[]{"category","brand"}));
        this.statement.put("3", new StatementConstructor(
        		new String[]{"I am really happy with my new",null,", thanks", null}, 
        		new String[]{"category","brand"}));
        this.statement.put("4", new StatementConstructor(
        		new String[]{"I can always count on",null,"for awesome",null}, 
        		new String[]{"brand","category"}));
        this.statement.put("5", new StatementConstructor(
        		new String[]{null,"always has the best prices on", null}, 
        		new String[]{"brand","category"}));
        this.statement.put("6", new StatementConstructor(
        		new String[]{null,"has the worst",null}, 
        		new String[]{"brand","category"}));
        this.statement.put("7", new StatementConstructor(
        		new String[]{"I can never find any",null,"that I am looking for at",null}, 
        		new String[]{"category","brand"}));
        this.statement.put("8", new StatementConstructor(
        		new String[]{"I absolutely hate the",null,"I just got from", null}, 
        		new String[]{"category","brand"}));
        this.statement.put("9", new StatementConstructor(
        		new String[]{"I can always count on",null,"for disappointing",null}, 
        		new String[]{"brand","category"}));
        this.statement.put("10", new StatementConstructor(
        		new String[]{null,"always seems to be trying to rip me off when I shop for", null}, 
        		new String[]{"brand","category"}));
        
        this.ipAddressMenu.put("1", "24.103.0.35");     //New York, NY
        this.ipAddressMenu.put("2", "24.104.63.78");    //Philadelphia, PA
        this.ipAddressMenu.put("3", "67.175.140.166");  //Chicago, IL
        this.ipAddressMenu.put("4", "67.180.8.157");  	//San Francisco, CA
        this.ipAddressMenu.put("5", "67.200.144.44");  	//Houston, TX
        
        if(mode.equalsIgnoreCase("training")){	
        	this.mode = mode;
        	System.out.print("******************** Training Mode");
        }
        else{
        	this.mode = "simulation";
        	System.out.print("******************** Simulation Mode");
        }	
    }
    
    public void sendStatus(String statementBody, String statementSourceIP){
    	Date date = new Date();
    	SocialMediaStatement socialMediaStatment = new SocialMediaStatement();
    	socialMediaStatment.setEventTimeStamp(String.valueOf(date.getTime()));
    	socialMediaStatment.setStatement(statementBody);
    	socialMediaStatment.setIpAddress(statementSourceIP);
    	socialMediaStatment.setLatitude("");
    	socialMediaStatment.setLongitude("");
    	
        try{
        	URL url = new URL("http://" + targetIP +":8082/contentListener");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setDoOutput(true);
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("Content-Type", "application/json");
            
            System.out.println("To String: " + convertPOJOToJSON(socialMediaStatment));
            
            OutputStream os = conn.getOutputStream();
    		os.write(convertPOJOToJSON(socialMediaStatment).getBytes());
    		os.flush();
            
            if (conn.getResponseCode() != 200) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ conn.getResponseCode());
    		}

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    
    private class StatementConstructor {
    	private String [] order;
    	private String [] statement;
    	
    	public StatementConstructor(String[] statement, String [] order){
    		this.statement = statement;
    		this.order = order;
    	}
    	
		public String[] getOrder() {
			return order;
		}
		public void setOrder(String[] order) {
			this.order = order;
		}
		public String[] getStatement() {
			return statement;
		}
		public void setStatement(String[] statement) {
			this.statement = statement;
		}	
    }
}