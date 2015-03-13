package emu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import server.*;

public class Runner {
	
	public static ArrayList<ArrayList<Server>> zones;
	public static int tickPerDay;
	public static int days;
	public static int currentTick;
	public static int currentTickLoop;
	public static int currentDay;
	public static int currentDayLoop;
	public static int ticksPast;
	public static int tick;
	public static int tickloop;
	public static String tickString = "000";
	public static String tickStringLoop = "000";
	public static String tickStringFormat = "";
	public static int dayMultiplier;
	public static File dataFile = null;
	public static File outFile = null;
	public static ObjectMapper mapper;
	public static JsonNode configData;
	public static HashMap<String,VirtualMachine> vmList;
	public static Server serverPlaceHolder;
	public static Server serverTooAdd;
	public static LinkedList<VirtualMachine> toAdd;
	public static int method;
	public static BufferedWriter writer;
	public static boolean verbose = false;
	public static boolean addServers = false;
	public static double addServerRamPercent;
	public static double addServerCPUPercent;
	public static int addServerWhen;
	public static int loopAt;
	public static boolean debug = false;
	
	
	
	public static void init() throws IOException {
		zones = new ArrayList<ArrayList<Server>>();
		vmList = new HashMap<String, VirtualMachine>();
		tickPerDay = configData.path("config").path("ticks_day").asInt();
  	dayMultiplier = (int)Math.log10(tickPerDay)+1;
		days = configData.path("config").path("days").asInt();
		tickStringFormat = "%d%0"+dayMultiplier+".0f";
		dayMultiplier = (int)Math.pow(10, dayMultiplier);
		toAdd = new LinkedList<VirtualMachine>();
		loopAt = configData.get("loop").asInt(0);
		method = configData.path("config").path("algorithm").asInt();
		if(method > 2)
			method = 2;
		writer = new BufferedWriter(new FileWriter(outFile));
		if(configData.path("config").has("newservers")) {
			addServers = true;
			addServerCPUPercent = configData.path("config").path("newservers").get("percentcpu").asDouble();
			addServerRamPercent = configData.path("config").path("newservers").get("percentram").asDouble();
			addServerWhen = configData.path("config").path("newservers").get("when").asInt();
			serverTooAdd = new Server(configData.path("config").path("newservers").path("server").get("cores").asInt(),
					configData.path("config").path("newservers").path("server").get("ram").asInt(),
					configData.path("config").path("newservers").path("server").get("maxvms").asInt());

		}
		
	}
	
	public static void end() throws IOException {
		writer.close();
	}
	
	public static boolean increaceTick() {
		
		currentDay=ticksPast/tickPerDay;
		currentTick = ticksPast%tickPerDay;
		tick = currentDay*dayMultiplier+currentTick;
		ticksPast++; //this is last so the ticks can start on 000
		tickString = String.format(tickStringFormat, currentDay,currentTick + 0.0);
		
		//looping
		if(loopAt>0 && tick >= loopAt) {
			tickloop = tick - loopAt*(tick/loopAt);
			currentDayLoop=tickloop/100;
			currentTickLoop = tickloop%100;
			tickStringLoop = String.format(tickStringFormat, currentDayLoop,currentTickLoop + 0.0);
			
		}
		else {
			tickStringLoop = tickString;
		}
		
		return (currentDay<days);
		
	}
	public static void createRack() {
		try {
			JsonNode rack = configData.path("config").path("zones");
			for(int i = 0; i<rack.size(); i++) {
				zones.add(new ArrayList<Server>());
				//System.out.println(i + " 1");
				for(int j = 0; j<rack.get(i).path("servers").asInt(); j++) {
					//System.out.println(j + "2");
					zones.get(i).add(new Server(rack.get(i).path("cores").asInt(), rack.get(i).path("ram").asInt(),rack.get(i).path("maxvms").asInt()));
				}
				System.out.println("Rack " + i + " created with " + zones.get(i).size() + " servers...");
			}
		}
		catch(Exception e) {
			System.out.println("Error in Json at config.zones");
			e.printStackTrace();
		}
		if(addServers)
			zones.add(new ArrayList<Server>());
	}
	
	public static String printServers() {
		String out = "";
		int totalVMS = 0;
		for(int i = 0; i<zones.size(); i++) {
			for(int j = 0; j<zones.get(i).size();j++) {
				out = out + zones.get(i).get(j).toString() + '\n';
				totalVMS += zones.get(i).get(j).getNumberVM();
			}
			out =totalVMS + " Virtual Machines, "+ toAdd.size() + " in queue... \n" + out + "\n---\n";
		}
		return out;		
	}
	public static String scrapeServers() {
		String out = "";
		int totalVMS = 0;
		for(int i = 0; i<zones.size(); i++) {
			for(int j = 0; j<zones.get(i).size();j++) {
				out = out + zones.get(i).get(j).scrape() + '\n';
				totalVMS += zones.get(i).get(j).getNumberVM();
			}
			
			out =totalVMS + " Virtual Machines, "+ toAdd.size() + " in queue... \n" + out + "\n---\n";
		}
		return out;		
	}
	
	public static void createVMList() {
		try {
			String name = "";
			JsonNode jsonList = configData.path("config").path("vm_types");
			Iterator<String> vmIterator = jsonList.fieldNames();
			while(vmIterator.hasNext()) {
				name = vmIterator.next();
				vmList.put(name, new VirtualMachine(jsonList.path(name).path("cpu").asInt(),
						jsonList.path(name).path("cpu_range").asInt(), 
						jsonList.path(name).path("ram").asInt(), 
						jsonList.path(name).path("cpu_noise").asInt(),
						configData.path("config").get("mayflylife").asInt()));
						//System.out.println(vmList.get(name).toString());
			}
			System.out.println("Created " + vmList.size() + " initial Virtual Machines...");
		}
		catch (Exception e) {
			System.out.println("Error in json at config.vm_types");
			e.printStackTrace();
		}
	}
	
	public static String printVMList() {
		String out = "";
		Iterable<String> i = vmList.keySet();
		Iterator<String> ii = i.iterator();
		while(ii.hasNext()) {
			String ne = ii.next();
			out = out + vmList.get(ne).toString() + "---" + ne + "\n --- \n";
		}
		return out;
	}
	
	public static int[] calcStandardDeviation(ArrayList<Server> zone) {
		int[] out = new int[5];
		int averageRam = 0;
		int averageCPU = 0;
		int sumRam = 0;
		int sumCPU = 0;
		int i = 0;
		int vmCount = 0;
		for(i = 0; i<zone.size(); i++) {
			averageRam += zone.get(i).getUsedRam();
			averageCPU += zone.get(i).getUsedCore();
			vmCount += zone.get(i).getNumberVM();
		}
		if(zone.size() != 0) {
			averageRam /= zone.size();
			averageCPU /= zone.size();
		}
		
		for(i=0;i<zone.size();i++) {
			sumCPU += (zone.get(i).getUsedCore()-averageCPU)*(zone.get(i).getUsedCore()-averageCPU);
			sumRam += (zone.get(i).getUsedRam()-averageRam)*(zone.get(i).getUsedRam()-averageRam);
		}
		out[0] = (zone.size() == 0 ? 0 : (int)Math.sqrt(sumRam/(zone.size())));
		out[2] = (zone.size() == 0 ? 0 : (int)Math.sqrt(sumCPU/(zone.size())));
		out[1] = averageRam;
		out[3] = averageCPU;
		out[4] = vmCount;
		return out;
	}
	public static LinkedList<VirtualMachine> createVMSToAdd(int tick, String tickString) {
		LinkedList<VirtualMachine> out = new LinkedList<VirtualMachine>();
		JsonNode working = configData.path("ticks").path(tickString);
		for(int i = 0; i<working.size(); i++) {
			JsonNode req = working.get(i);
			for(int j = 0; j<req.path("count").asInt(); j++) {
				
				
				//System.out.println(req.toString());
				//String vmType = req.path("vm").asText();
				//System.out.println(vmType);
				
				
				out.add((vmList.get(req.path("vm").asText()).clone()));
				out.get(out.size()-1).setTickStart(tick);
				if(req.has("endtick"))
						out.get(out.size()-1).setTickEnd(req.path("endtick").asInt());
				if(req.has("cpu_range"))
					out.get(out.size()-1).setCpu_range(req.path("cpu_range").asInt());
				if(req.has("cpu"))
					out.get(out.size()-1).setCpu(req.path("cpu").asInt());
				if(req.has("cpu_noise"))
					out.get(out.size()-1).setSpike(req.path("cpu_noise").asInt());
				if(req.has("maxlife"))
					out.get(out.size()-1).setMaxLife(req.path("maxlife").asInt());
				if(req.has("ram"))
					out.get(out.size()-1).setRam(req.path("ram").asInt());
				if(req.has("persistent"))
					out.get(out.size()-1).setPersistent(req.path("persistent").asBoolean(false));
				if(req.has("mayfly"))
					out.get(out.size()-1).setMayfly(req.path("mayfly").asBoolean(false));
				
				//System.out.println(out.get(out.size()-1).toString());
			}
		}
		
		return out;
	}
	
	public static void addRoundRobin() {
		boolean again = false;
		do {
			again = addRoundRobin(!again); //trickery to make sure I can use firstcheck as true the first time while only using one variable
		}//this works on the principle that addRoundRobin will return true if it needs to run again, so again will be passed as false second run
		while(again);
	}
	public static boolean addRoundRobin(boolean firstCheck) {
		int listSize = toAdd.size();
		Server lowestNumber = zones.get(0).get(0);
		// finding the virtual machine with the lowest number of VMS.
		if(firstCheck) {
			int lowest = Integer.MAX_VALUE;
			for(int i = 0; i<zones.size(); i++) {
				for(int j = 0; j<zones.get(i).size(); j++) {
					if(zones.get(i).get(j).getNumberVM() < lowest) { //if the number is less than the previous lowest number 
						lowest = zones.get(i).get(j).getNumberVM(); //replace lowest with new lowest
						lowestNumber = zones.get(i).get(j); //save a pointer to the new lowest position
					}
				}
			}
		}
		for(int i = 0; i<zones.size(); i++) {
			for(int j = 0; j<zones.get(i).size(); j++) {
				if(firstCheck) {
					if(lowestNumber == zones.get(i).get(j)) { //if the pointers match, then it is no longer the first loop through
						firstCheck = false;
					}
				}
				if(!firstCheck && !toAdd.isEmpty()) { //only add if not empty and not first check, not necessary to check firstCheck
					if(zones.get(i).get(j).addVM(toAdd.get(0)))
						toAdd.remove(0);					
				}
			}
		}
		if(toAdd.size()>0) {
			if(listSize == toAdd.size()) { //if unable to add anything leave loop.
				return false;
			}
			return true;
		}
		return false;
		
	}
	
	public static void addLoadLevelingPercent() {
		if(toAdd.isEmpty())
			return;
		double minLoad = Double.MIN_VALUE;
		int initSize = 0; 
		serverPlaceHolder = null;
		do {
			initSize = toAdd.size();
			minLoad = 0;
			for(int i = 0; i<zones.size(); i++) {
				for(int j = 0; j<zones.get(i).size(); j++) {
					if(zones.get(i).get(j).getLoad()>minLoad && toAdd.size() > 0 && zones.get(i).get(j).canAddVM(toAdd.get(0))) {
						minLoad = zones.get(i).get(j).getLoad();
						serverPlaceHolder = zones.get(i).get(j);
					}
				}
			}
			if(serverPlaceHolder != null && toAdd.size() > 0) {
				if(serverPlaceHolder.addVM(toAdd.get(0))) {
					toAdd.remove(0);
					serverPlaceHolder.updateLoad();
				}
			}
		}
		while(initSize != toAdd.size());
	}

	public static void addLoadLevelingAvailable() {
		if(toAdd.isEmpty())
			return;
		int maxAvailable = 0;
		int initSize = 0; 
		serverPlaceHolder = null;
		do {
			initSize = toAdd.size();
			maxAvailable = 0;
			for(int i = 0; i<zones.size(); i++) {
				for(int j = 0; j<zones.get(i).size(); j++) {
					if((zones.get(i).get(j).getMaxRam()-zones.get(i).get(j).getUsedRam())>maxAvailable && toAdd.size() > 0 && zones.get(i).get(j).canAddVM(toAdd.get(0))) {
						maxAvailable = zones.get(i).get(j).getMaxRam()-zones.get(i).get(j).getUsedRam();
						serverPlaceHolder = zones.get(i).get(j);
					}
				}
			}
			if(serverPlaceHolder != null && toAdd.size() > 0) {
				if(serverPlaceHolder.addVM(toAdd.get(0))) {
					toAdd.remove(0);
					serverPlaceHolder.update();
				}
			}
		}
		while(initSize != toAdd.size());
	}
	
	public static void addRaked() {
		boolean add = false;
		double percent = 0.0;
		int number = 0;
		if(addServers) {
			if(ticksPast%addServerWhen == 0) {
				for(int i = 0; i<zones.size(); i++) { //ram
					for(int j = 0; j<zones.get(i).size(); j++) {
						percent += zones.get(i).get(j).getRamPercentUtilization();
						number++;
					}
				}
				percent /= number;
				if(percent >= addServerRamPercent)
					add = true;
				percent = 0.0;
				number = 0;
				for(int i = 0; i<zones.size(); i++) { //CPU
					for(int j = 0; j<zones.get(i).size(); j++) {
						percent += zones.get(i).get(j).getCPUPercentUtilization();
						number++;
					}
				}
				percent /= number;
				if(percent >= addServerCPUPercent)
					add = true;
			}
		}
		
		if(add) {
			zones.get(zones.size()-1).add(serverTooAdd.basicClone());
		}
			
	}

	public static void tick() {
		
		//tick and gather machines being re-added due to mayflies.
		
		for(int i = 0; i<zones.size(); i++) {
			for(int j = 0; j<zones.get(i).size(); j++) {
				toAdd.addAll(zones.get(i).get(j).tick(tick));
			}
		}
		
		//creating the new virtual machines
		toAdd.addAll(createVMSToAdd(tick, tickStringLoop));
		
		//servers need to update, it is easy and should be done more often.
		for(int i = 0; i<zones.size(); i++) {
			for(int j = 0; j<zones.get(i).size(); j++) {
				zones.get(i).get(j).update();
			}
		}
	
		if(debug)
			System.out.println(toAdd.size() + " vm in queue");
		
		//add Virtual Machines to the servers
		switch(method) {
			case 0: 
				addRoundRobin();
				break;
			case 1:
				addLoadLevelingPercent();
				break;
			case 2:
				addLoadLevelingAvailable();
			default:
				addRoundRobin();
				break;
					
			
		}
		for(int i = 0; i<zones.size(); i++) { //that is why we are updating again!
			for(int j = 0; j<zones.get(i).size(); j++) {
				zones.get(i).get(j).update();
			}
		}
		//servers need to tick
		
		//adding servers if necessary
		addRaked();
		
	}
	public static void printSTD() {
		for(int i = 0; i<zones.size(); i++) { //Write Standard Deviation
			int[] std = calcStandardDeviation(zones.get(i));
			System.out.printf(" :: Zone %d | Avg,Std | Ram,CPU | #VMS,#Servers || %d,%d | %d,%d | %d,%d ",i,std[1],std[0],std[3],std[2],std[4], zones.get(i).size());
			try {
				writer.write(String.format(" :: Zone %d | Avg,Std | Ram,CPU | #VMS,#Servers || %d,%d | %d,%d | %d,%d ",i,std[1],std[0],std[3],std[2],std[4], zones.get(i).size()));
			} catch (IOException e) {
				System.out.println("Error: failed to write to " + outFile.getPath());
			}
		}
		System.out.println("");
		try {
			writer.write("\n");
		} catch (IOException e) {
			System.out.println("Error: failed to write to " + outFile.getPath());
		}
	}
	
	public static void run() {
		createRack();
		createVMList();
		System.out.println("\n");
 		while (increaceTick()) {
			tick();
			System.out.print("Tick " + tickString + ", Queue size "+toAdd.size()); // write tick String
			try {
				writer.write("Tick "+tickString + ", Queue size "+toAdd.size());
			} catch (IOException e) {
				System.out.println("Error: failed to write to " + outFile.getPath());
			}
			printSTD();
			if(currentTick == 0 && verbose) {
				System.out.println(scrapeServers());
				try {
					writer.write(scrapeServers() + "\n");
				} catch (IOException e) {
					System.out.println("Error: failed to write to " + outFile.getPath());
				}
			}
			
		}
 		System.out.print("Tick " + tickString + ", Queue size "+toAdd.size()); // write tick String
		try {
			writer.write("Tick " + tickString + ", Queue size "+toAdd.size());
		} catch (IOException e) {
			System.out.println("Error: failed to write to " + outFile.getPath());
		}
		printSTD();
 		if(verbose) {
	 		System.out.println("\n"+ scrapeServers());
	 		try {
				writer.write("\n"+ scrapeServers() + "\n");
			} catch (IOException e) {
				System.out.println("Error: failed to write to " + outFile.getPath());
			}
 		}
	}
	
	public static void debugLoop() throws IOException {
		//Iterator<String> debugs = configData.path("ticks").path("000").path("req001").get("count").asInt();
		//System.out.println(configData.path("ticks").path("000").get(0).path("count").toString());
		
		System.out.println("init");
		System.out.println("-------------------------------------------------");
		writer.write("init \n -------------------------------------------------\n");
		
		createRack();
		
		System.out.println("Create Rack");
		System.out.println(printServers());
		System.out.println("-------------------------------------------------");
		writer.write("Create Rack" + printServers() + "-------------------------------------------------\n");
		
		createVMList();
		
		System.out.println("createVMList");
		System.out.println(printVMList());
		System.out.println("-------------------------------------------------");
		writer.write("createVMList"+printServers()+"-------------------------------------------------\n");
		
		
		increaceTick();
		
		System.out.printf("%d %d %d",tickPerDay, tick, dayMultiplier);
		System.out.println(tickStringFormat);
		System.out.println(tickString);
		System.out.println("-------------------------------------------------");
		writer.write(String.format("%d %d %d\n%s\n%s\n-------------------------------------------------\n", tickPerDay, tick, dayMultiplier, tickStringFormat, tickString));
		
		tick();
		System.out.println(tickString +"\n"+ printServers());
		
		System.out.println("-------------------------------------------------");
		writer.write(String.format("%s\n%s\n-------------------------------------------------\n", tickString, printServers()));
		
		
		System.out.println("Enter to continue...");
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
		sc.close();
		
		while (increaceTick()) {
			tick();
			printSTD();
			System.out.println(tickString +"\n"+ printServers());
			writer.write(tickString + "\n" + printServers());
		}
		printSTD();
		System.out.println(tickString +"\n"+ printServers() + "\n\n");
		//System.out.println(printServers());
		writer.write(tickString + "\n" + printServers());
	}
	
	public static void main(String[] args) throws IOException {
		/*
		if(args.length == 1) {
		
			dataFile = new File(args[0]);
		}
		if(args.length == 2) {
			outFile = new File(args[1]);
		}
		if(dataFile == null) {
			throw(new Exception("No Input File"));
		}
		else if(outFile == null) {
			outFile = new File(dataFile.getParentFile().toString() + "/outfile.txt"); 
		}
		
		*/
		for(int i = 0; i<args.length; i++) {
			switch(args[i]) { //read in arguments
				case "-i":
					dataFile = new File(args[++i]);
					break;
				case "-o":
					outFile = new File(args[++i]);
					break;
				case "-v":
					verbose = true;
					break;
				case "-d":
					debug = true;
					break;
				default:
					System.out.println("Unexpected Argument: " + args[i]);
					System.out.println("Expected Arguments: ");
					System.out.println("-i config_fle [-o output_file -d{debug} -v{verbose}");
					System.exit(1);
					break;
			}
		}
		if(dataFile == null) {
			System.out.println("Specify the input file\nThe commands are -i config file, -o out file, -v verbose, -d debug");
			System.exit(1);
		}
		if(outFile == null) { //check to make sure outfile is specified
			outFile = new File(dataFile.getParentFile().toString() + "/outfile.txt");
		}
		
		try {
			mapper = new ObjectMapper();
			configData = mapper.readTree(dataFile);
		}
		catch (Exception e) {
			System.out.println("Could not load Json file");
			e.printStackTrace();
		}
		
		init();
		if(debug)
			debugLoop();
		else
			run();
		
		end();
		
	}

}
