package com.hortonworks.iot.simulator.types;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.hortonworks.iot.simulator.events.HistorianEvent;
import com.hortonworks.iot.simulator.events.STBStatus;

public class HistorianSimulator implements Runnable {
    private String serialNumber;
    private String targetIP;
    private String state; 
    private String status;
    private String mode;
    private long timestamp;
    private String function;
    private double value;
    private Map<Integer,String> functionType = new HashMap<Integer,String>();
    
    private String externalCommand;
    private Integer cyclesCompleted = 0;
    
    Random random = new Random();
    
    public HistorianSimulator(String deviceSerialNumber, String mode, String targetIP){
        initialize(deviceSerialNumber, mode, targetIP);    
    }
    
    public void run() {
 
    	try {
			powerOn();
			normalCycle();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
      
    	while(state.equalsIgnoreCase("on")){
    		Integer incident = random.nextInt(6-1) + 1;
    		try {
    			System.out.println("Cycle Randomizer: " + incident);
    			
    			if(mode.equalsIgnoreCase("training"))
    				runTrainingCycle(incident);
    			else
    				runSimulationCycle(incident);
    			
    		} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	System.exit(0);
    }
    public void runSimulationCycle(Integer incident) throws InterruptedException{
    	if(incident > 3 && cyclesCompleted >= 1 && 0==1 ){
			tempFailCycle();
			powerOff();
		}else{
			normalCycle();
			cyclesCompleted++;
		}
    }
    
    public void runTrainingCycle(Integer incident) throws InterruptedException{
		if(incident > 3){
			tempFailCycle();
			//cyclesCompleted++;
		}
		else{
			normalCycle();
			//cyclesCompleted++;
		}
    }
    
    public void initialize(String deviceSerialNumber, String mode, String targetIP){
        this.serialNumber = deviceSerialNumber; //deviceSpout.getSerialNumber();
        this.targetIP = targetIP;
        state = "off";
        status = "normal";
        functionType.put(1,"rpm_truck_a");
        functionType.put(2,"rpm_truck_b");
        functionType.put(3,"mpg_truck_a");
        functionType.put(4,"mpg_truck_b");
        externalCommand = "none";
        if(mode.equalsIgnoreCase("training")){	
        	this.mode = mode;
        	System.out.print("******************** Training Mode");
        }
        else{
        	this.mode = "simulation";
        	System.out.print("******************** Simulation Mode");
        }	
    }
    
    public void sendStatus(){
        HistorianEvent historianEvent = new HistorianEvent();
        historianEvent.setSerialNumber(serialNumber);
        historianEvent.setState(state);
        historianEvent.setStatus(status);
        historianEvent.setTimestamp(timestamp);
        historianEvent.setFunction(function);
        historianEvent.setValue(value);
        
        try{
        	URL url = new URL("http://" + targetIP + ":8084/contentListener");
    		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		conn.setDoOutput(true);
    		conn.setRequestMethod("POST");
    		conn.setRequestProperty("Content-Type", "application/json");
            
            System.out.println("To String: " + convertPOJOToJSON(historianEvent));
            
            OutputStream os = conn.getOutputStream();
    		os.write(convertPOJOToJSON(historianEvent).getBytes());
    		os.flush();
            
            if (conn.getResponseCode() != 200) {
    			throw new RuntimeException("Failed : HTTP error code : "
    				+ conn.getResponseCode());
    		}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void normalCycle() throws InterruptedException{
    	System.out.println("Starting New Normal Cycle");
    	for(int i=0; i<30; i++){
    		timestamp = System.currentTimeMillis();
    		function = functionType.get(random.nextInt(5-1)+1);
    		value = (random.nextDouble());
        	sendStatus();
            Thread.sleep(500);
    	}    	  
    }
    
    public void tempFailCycle() throws InterruptedException{
    	System.out.println("Starting New TempFail Cycle");
    	for(int i=0; i<30; i++){
    		if(i==0){
    			setValue(random.nextDouble());
    		}
    		else{
    			setValue(random.nextDouble());
    		}
    		
    		sendStatus();
            Thread.sleep(500);
    	}
    }
    
    /*
    public void generateStatus() throws InterruptedException{
    	Integer incident = random.nextInt(21-1) + 1;
    	System.out.println("Incident Randomizer: " + incident);
    	if(incident == 20 && eventsSent >= 30){
    		signalStrength = random.nextInt(70-20) + 20;
        	internalTemp = random.nextInt(110-100) + 100;
    	}
    	else{
    		signalStrength = random.nextInt(95-85) + 85;
        	internalTemp = random.nextInt(95-85) + 85;	
    	}
    	
    	sendStatus();
    	eventsSent++;
        Thread.sleep(1000);
    }
    */
    
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

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
