package cir.nus.edu;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;

public class Configuration {
		
	public String config_file = "acm.cfg";
	public String id = "acm";
	public int apNum =8; 
	public int Gap = 600; //gap to merge two stay periods
	public String path = "/home/honghande/Research/SocialProbe/ProbeBeacon/PiLab/FigurePlot/";//image output folder
	public int size= 900; //canvas size
	public int timeGap = 60;//crowd change time unit *minute used for time table
	public int scale = 15;
	public String HomeDir= "/home/honghande/Research/SocialProbe/ProbeBeacon/PiLab/";
	public String src = "";
	
	public Configuration()
	{
		File c_file = new File(config_file);
//		System.out.println("read configuration");
		if(c_file.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(c_file));
				String cfg_line = null;
				while((cfg_line = reader.readLine()) != null)
				{
					cfg_line = cfg_line.trim();
					if(cfg_line.startsWith("#")) //commented
						continue;
					String[] cfg_line_split = cfg_line.split(":");
					if(cfg_line_split.length == 2)
					{
						String key = cfg_line_split[0].trim();
						String value = cfg_line_split[1].trim();
						parseLine(key, value);
					}
				}
				reader.close();
			}
			catch(Exception ex)
			{
				System.out.println("error: reading configuration file");
				System.out.println(ex.getMessage());
			}
		}
	}
	
	private void parseLine(String key, String value)
	{
		if(key.equals("apNum"))
		{
			apNum = Integer.parseInt(value);
		}
		else if(key.equals("Gap"))
		{
			Gap = Integer.parseInt(value);
		}
		else if(key.equals("path"))
		{
			path = value;
		}
		else if(key.equals("id"))
		{
			id = value;
		}
		else if(key.equals("HomeDir"))
		{
			HomeDir = value;
		}	
		else if(key.equals("src"))
		{
			src = value;
		}
		else if(key.equals("size"))
		{
			size = Integer.parseInt(value);
		}
		else if(key.equals("timeGap"))
		{
			timeGap = Integer.parseInt(value);
		}
		else if(key.equals("scale"))
		{
			scale = Integer.parseInt(value);
		}
		else
		{
			System.out.println("warning: no such config: " + key);
		}
	}
	
}
