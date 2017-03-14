package com.hortonworks.iot.simulator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.hortonworks.iot.rest.TechnicianService;
import com.hortonworks.iot.simulator.events.ProgramGuide;
import com.hortonworks.iot.simulator.events.Station;
import com.hortonworks.iot.simulator.types.BioReactorSimulator;
import com.hortonworks.iot.simulator.types.FiltrationSystemSimulator;
import com.hortonworks.iot.simulator.types.RetailStoreSimulator;
import com.hortonworks.iot.simulator.types.RetailStoreSimulator2;
import com.hortonworks.iot.simulator.types.STBSimulator;
import com.hortonworks.iot.simulator.types.SocialMediaEmulator;
import com.hortonworks.iot.simulator.types.TechnicianSimulator;
import com.hortonworks.iot.simulator.types.X1TunerSimulator;

import net.sf.ehcache.CacheManager;

public class Simulator {
    // Base URI the Grizzly HTTP server will listen on
    public static String ipaddress;
    public static String port;
	public static HttpServer startServer(String simType, String deviceId) {
    	//Map<String,String> deviceDetailsMap = new HashMap<String, String>();
    	Map<String,String> deviceNetworkInfoMap = new HashMap<String, String>();
    	ResourceConfig config = null;
    	URI baseUri = null;
    	deviceNetworkInfoMap = getNetworkInfo(deviceId, simType);
    	baseUri = UriBuilder.fromUri("http://"+ deviceNetworkInfoMap.get("ipaddress") + "/server/").port(Integer.parseInt(deviceNetworkInfoMap.get("port"))).build();
    	//deviceDetailsMap = getSimulationDetails(simType, deviceId);
		//baseUri = UriBuilder.fromUri("http://" + deviceDetailsMap.get("ipaddress") + "/server/").port(Integer.parseInt(deviceDetailsMap.get("port"))).build();
	
    	if(simType.equalsIgnoreCase("STB")){
    		config = new ResourceConfig(TechnicianService.class);
    	}
    	else if(simType.equalsIgnoreCase("Technician")){
    		config = new ResourceConfig(TechnicianService.class);
    	}
    	else if(simType.equalsIgnoreCase("BioReactor")){
    		config = new ResourceConfig(TechnicianService.class);
    	}
    	else if(simType.equalsIgnoreCase("FiltrationSystem")){
    		config = new ResourceConfig(TechnicianService.class);
    	}
    	else if(simType.equalsIgnoreCase("RetailStore")){
    		config = new ResourceConfig(RetailStoreSimulator.class);
    	}
    	else{
    		System.exit(1);
    	}
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, config);
		return server;
    }
    public static Map<String,String> getNetworkInfo(String deviceId, String simType){
    	Map<String,String> deviceNetworkInfoMap = new HashMap<String, String>();
        String ipaddress;
    	String hostname;
        String ipScratch[];
        try {
            ipScratch = InetAddress.getLocalHost().toString().replace("/", ":").split(":"); 
            ipaddress = InetAddress.getLocalHost().getHostAddress();
            hostname = InetAddress.getLocalHost().getHostName();
            deviceNetworkInfoMap.put("ipaddress", ipaddress);
            deviceNetworkInfoMap.put("hostname", hostname);
            System.out.println("Current IP address : " + ipaddress);
            System.out.println("Current Hostname : " + hostname);         
        } catch (UnknownHostException e) {
 
            e.printStackTrace();
        }
        
        switch(simType + " " + deviceId){
		case "Technician 1000":
			deviceNetworkInfoMap.put("port", "8070");
			break;
		case "Technician 2000":
			deviceNetworkInfoMap.put("port", "8071");
			break;
		case "Technician 3000":
			deviceNetworkInfoMap.put("port", "8072");
			break;
		case "STB 1000":
			deviceNetworkInfoMap.put("port", "8073");
			break;
		case "STB 2000":
			deviceNetworkInfoMap.put("port", "8074");
			break;
		case "STB 3000":
			deviceNetworkInfoMap.put("port", "8075");
			break;
		case "BioReactor 1000":
			deviceNetworkInfoMap.put("port", "8076");
			break;
		case "BioReactor 2000":
			deviceNetworkInfoMap.put("port", "8077");
			break;
		case "BioReactor 3000":
			deviceNetworkInfoMap.put("port", "8078");
			break;
		case "FiltrationSystem 1000":
			deviceNetworkInfoMap.put("port", "8060");
			break;
		case "FiltrationSystem 2000":
			deviceNetworkInfoMap.put("port", "8061");
			break;
		case "FiltrationSystem 3000":
			deviceNetworkInfoMap.put("port", "8062");
		 	break;
		case "RetailStore 1000":
			deviceNetworkInfoMap.put("port", "8063");
			break;
		case "RetailStore2 1000":
			deviceNetworkInfoMap.put("port", "8063");
			break;	
		case "RetailStore 2000":
			deviceNetworkInfoMap.put("port", "8064");
			break;
		case "RetailStore 3000":
			deviceNetworkInfoMap.put("port", "8065");
			break;
		case "SocialMedia 1000":
			break;	
		default:
			System.out.println("There is no record of " + simType + " " + deviceId + ". Cannot start device simulation");
			System.exit(1);
			break;
		}
        
        return deviceNetworkInfoMap;
    }
    
	public static void main(String[] args) throws IOException {
		Thread deviceThread;
		Thread techThread;
		String targetIP;
		String simType = args[0];
		String serialNumber = args[1];
		String mode = args[2];
		if(args.length == 4){	
			targetIP = args[3];
		}else if(args.length == 5){	
				targetIP = args[4];
		}else{
			targetIP = "sandbox.hortonworks.com";
		}
		System.out.println("Starting Cache...");
		CacheManager.create();
		CacheManager.getInstance().addCache("TechnicianRouteRequest");
		
		if(simType.equalsIgnoreCase("STB")){
			System.out.println("Starting Webservice...");
			final HttpServer server = startServer(simType, serialNumber);
			server.start();
			System.out.println("Starting Set Top Box...");
			Map networkInfo = getNetworkInfo(serialNumber, simType);
			STBSimulator stb = new STBSimulator(serialNumber, mode, targetIP);
            deviceThread = new Thread(stb);
            deviceThread.setName("Device: " + serialNumber);
            deviceThread.start();
		}
		else if(simType.equalsIgnoreCase("Technician")){			
			System.out.println("Starting Webservice...");
			final HttpServer server = startServer(simType, serialNumber);
			server.start();
			System.out.println("Starting Technician Route");
			Map networkInfo = getNetworkInfo(serialNumber, simType);
			ipaddress =  (String)networkInfo.get("ipaddress");
			port =  (String)networkInfo.get("port");
			TechnicianSimulator tech = new TechnicianSimulator(serialNumber, targetIP, ipaddress, port);
            techThread = new Thread(tech);
            techThread.setName("Technician: " + serialNumber);
            techThread.start();
        }
		else if(simType.equalsIgnoreCase("X1Tuner")){			
			System.out.println("Starting X1 Channel Tune Event Simualtion");
			int numDevices = Integer.valueOf(args[3]) * 1000;
			int lastSerialNumber = numDevices;
			int[] zipCodes = {19101, 33301, 80123, 94105, 97201};
			//ipaddress =  (String)networkInfo.get("ipaddress");
			//port =  (String)networkInfo.get("port");
			Map<Integer,String> market = new HashMap<Integer, String>(); 
			market.put(19101, "Philadelphia, PA");
			market.put(33301, "Ft. Lauderdale, FL");
			market.put(80123, "Denver, CO");
			market.put(94105, "San Francisco, CA");
			market.put(97201, "Portland, OR");
			for(int zipCode: zipCodes){
				List<Station> guide = new ArrayList<Station>();
				guide = ProgramGuide.getProgramGuide(zipCode);
				for(int currentSerialNumber=1000; currentSerialNumber<=lastSerialNumber; currentSerialNumber+=1000){
					X1TunerSimulator xOneTune = new X1TunerSimulator(guide, currentSerialNumber+""+zipCode, mode, market.get(zipCode), zipCode);
					techThread = new Thread(xOneTune);
					techThread.setName("Device: " + serialNumber + zipCode);
					techThread.start();
				}
			}
        }
		else if(simType.equalsIgnoreCase("BioReactor")){			
			System.out.println("Starting Webservice...");
			final HttpServer server = startServer(simType, serialNumber);
			server.start();
			System.out.println("Starting BioReactor Fermentation Process...");
			Map networkInfo = getNetworkInfo(serialNumber, simType);
			ipaddress =  (String)networkInfo.get("ipaddress");
			port =  (String)networkInfo.get("port");
			//BioReactorSimulator bioReactor = new BioReactorSimulator(serialNumber, ipaddress, port);
			BioReactorSimulator bioReactor = new BioReactorSimulator(serialNumber, mode, targetIP);
			deviceThread = new Thread(bioReactor);
			deviceThread.setName("BioReactor: " + serialNumber);
			deviceThread.start();
        }else if(simType.equalsIgnoreCase("FiltrationSystem")){			
			System.out.println("Starting Webservice...");
			final HttpServer server = startServer(simType, serialNumber);
			server.start();
			System.out.println("Starting Filtration Process...");
			Map networkInfo = getNetworkInfo(serialNumber, simType);
			ipaddress =  (String)networkInfo.get("ipaddress");
			port =  (String)networkInfo.get("port");
			//BioReactorSimulator bioReactor = new BioReactorSimulator(serialNumber, ipaddress, port);
			FiltrationSystemSimulator filtartionSystem = new FiltrationSystemSimulator(serialNumber, mode, targetIP);
			deviceThread = new Thread(filtartionSystem);
			deviceThread.setName("Filtration System: " + serialNumber);
			deviceThread.start();
        }else if(simType.equalsIgnoreCase("RetailStore")){			
			System.out.println("Starting Webservice...");
			final HttpServer server = startServer(simType, serialNumber);
			server.start();
			System.out.println("Starting Retail Store Simulation...");
			Map networkInfo = getNetworkInfo(serialNumber, simType);
			ipaddress =  (String)networkInfo.get("ipaddress");
			port =  (String)networkInfo.get("port");
			
			RetailStoreSimulator retailStore = new RetailStoreSimulator(serialNumber, mode, targetIP);
			deviceThread = new Thread(retailStore);
			deviceThread.setName("Retail Store: " + serialNumber);
			deviceThread.start();
        }else if(simType.equalsIgnoreCase("RetailStore2")){			
			System.out.println("Starting Webservice...");
			final HttpServer server = startServer(simType, serialNumber);
			server.start();
			System.out.println("Starting Retail Store Simulation...");
			Map networkInfo = getNetworkInfo(serialNumber, simType);
			ipaddress =  (String)networkInfo.get("ipaddress");
			port =  (String)networkInfo.get("port");
			
			RetailStoreSimulator2 retailStore = new RetailStoreSimulator2(serialNumber, mode, targetIP);
			deviceThread = new Thread(retailStore);
			deviceThread.setName("Retail Store: " + serialNumber);
			deviceThread.start();
        }else if(simType.equalsIgnoreCase("SocialMedia")){			
			System.out.println("Starting Webservice...");
			//final HttpServer server = startServer(simType, serialNumber);
			//server.start();
			System.out.println("Starting Social Media Emulator...");
			Map networkInfo = getNetworkInfo(serialNumber, simType);
			ipaddress =  (String)networkInfo.get("ipaddress");
			port =  (String)networkInfo.get("port");
			
			SocialMediaEmulator socialMedia = new SocialMediaEmulator(serialNumber, mode, targetIP);
			deviceThread = new Thread(socialMedia);
			deviceThread.setName("Social Media Emulator: " + serialNumber);
			deviceThread.start();
        }
    }
}