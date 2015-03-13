package configGen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Generator {
	public ArrayList<Integer> lifeExpectancy;
	public JSONObject root;
	public int mayflyLife;
	
	@SuppressWarnings("unchecked")
	public Generator(File csvFile) throws FileNotFoundException {
		
		
		//needed because one of the details needs life expectancy
		//plus it is configurable in config.dat (really csv)
		
		
		//config start
		Scanner config = new Scanner(new File("configCreation\\config.dat"));
		
		String[] temp = config.nextLine().split(",");
		lifeExpectancy = new ArrayList<Integer>(temp.length);
		for(int i = 0; i<temp.length; i++) {
			lifeExpectancy.add(new Integer(temp[i]));
		}
		if(config.hasNextLine()) {
			mayflyLife = new Integer(config.nextLine());
		}
		config.close();
		//config done
		
		Scanner sc = new Scanner(csvFile);
		
		root = new JSONObject();
		
		String[] vmType = sc.nextLine().split(",");
		
		while (sc.hasNext()) {
			String[] workingData = sc.nextLine().split(",");
			double currentTick = new Integer(workingData[0]);
			String tick = String.format("%03.0f", currentTick); //format tick correctly (0 is 000, 1 is 001)
			JSONArray data = new JSONArray();
			for(int place = 1; place<workingData.length; place++) { //add lots to array
				int number = new Integer(workingData[place]); //creating new int because this number is only used for iteration and type
				for(int i = 0; i<number; i++) {
					data.add(details(vmType[place]));
				}
			}
			root.put(tick, data);
		}
		sc.close();
	}
	public JSONObject details(String type) {
		int life = 0;
		Collections.shuffle(lifeExpectancy);
		Collections.shuffle(lifeExpectancy);
		life = lifeExpectancy.get((int)(Math.random()*lifeExpectancy.size())); //random number [0,sizeOfList)
		return details(type, life, 1);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject details(String type, int life, int count) {
		JSONObject data = new JSONObject();
		data.put("vm",type);
		data.put("maxlife", life);
		data.put("count", count);
		if(life>mayflyLife && mayflyLife > 0) {
			data.put("mayfly", true);
		}
		return data;
	}
	
	public static void main(String[] args) throws IOException {
		//File home = new File("configCreation\\config.dat");
		//System.out.println(home.exists());
		
 		Generator gen = new Generator(new File(args[0]));
		
		System.out.println(gen.root.toJSONString());
		BufferedWriter write = new BufferedWriter(new FileWriter("Generated.json"));
		write.write(gen.root.toJSONString());
		write.close();
		
		
	}

}
