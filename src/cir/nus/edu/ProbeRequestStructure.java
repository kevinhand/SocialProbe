package cir.nus.edu;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.commons.math3.ml.clustering.Clusterable;

/**
 *
 * @author Hande
 */

public class ProbeRequestStructure{
	
//	static int Apnum = 8;
	public static class ProbeRequest {
	    private  Calendar stime;
	    private  Calendar etime;
	    private String sourceMAC;
	    private ArrayList<Integer> rssiList =null;
	    
	    public ProbeRequest(Calendar s,Calendar e, String sourceMAC, int rssi)
	    {
	        this.stime = s;
	        this.etime = e;
	        this.sourceMAC = sourceMAC;
	        rssiList = new ArrayList<Integer>();
	        rssiList.add(rssi);   	
	    }
	    
	    
	    public Calendar getStartTime() {return this.stime;}
	    public Calendar getEndTime() {return this.etime;}
	    public void setStartTime(Calendar s) {this.stime = s;}
	    public void setEndTime(Calendar e) {this.etime = e;}
	    public String getSourceMAC() {return this.sourceMAC;}
	    public ArrayList<Integer> getRssi() {return this.rssiList;}
	    
	    
	    public int getAveragerssi(){
	    	if(rssiList.size()>0){
	    		int sum =0;
	    		for(int i=0;i<rssiList.size();i++){
	    			sum += rssiList.get(i);
	    		}
	    		return sum/rssiList.size();
	    	}
	    	else
	    		return 0;   	
	    }
	    
	    public int getAveragerssiWithoutOutliar(){
	    	int avg = getAveragerssi();
	    	double dev = getDeviationrssi();
//	    	System.out.println(rssiList.size()+" "+avg+" "+dev+" "+rssiList.toString()+" "+sourceMAC+" "+FunctionUtility.CalendarToString(stime));
	    	if(rssiList.size()>0){
	    		int sum =0,cc=0;
	    		for(int i=0;i<rssiList.size();i++){
	    			if(Math.abs(rssiList.get(i)-avg)<=dev){
	    				sum += rssiList.get(i);
	    				cc++;
	    			}
	    				
	    		}
	    		return sum/cc;
	    	}
	    	else
	    		return 0;   	
	    }
	    	    
	    public double getDeviationrssi(){
	    	if(rssiList.size()>0){
	    		int avg = getAveragerssi();
	    		
	    		double var =0;
	    		for(int i=0;i<rssiList.size();i++){
	    			var +=(rssiList.get(i)-avg)*(rssiList.get(i)-avg);
	    		}
	    		return Math.sqrt((var/rssiList.size()));   		
	    	}
	    	else
	    		return 0;   	
	    }
	    
	    public double getDeviationrssiWithoutOutliar(){
	    	if(rssiList.size()>0){	    
	    		int avg = getAveragerssiWithoutOutliar();
	    		double dev = getDeviationrssi();
	    		double var =0;
	    		int cc=0;
	    		for(int i=0;i<rssiList.size();i++){
	    			if(Math.abs(rssiList.get(i)-avg)<=dev){
	    				var +=(rssiList.get(i)-avg)*(rssiList.get(i)-avg);
	    				cc++;
	    			}
	    		}
	    		return Math.sqrt((var/cc));   		
	    	}
	    	else
	    		return 0;   	
	    }
	    
	    public boolean IsInSameBurst(ProbeRequest probe)
	    {        
	        if (probe == null)
	            return false;
	        else if (probe == this)
	            return true;
	        
	        if ( probe.IsInSameWindow(this)&&
	        		probe.getSourceMAC().equals(this.sourceMAC))
	        {

	        	return true;
	        }
	           
	        else
	            return false;
	    }
	    
	    public boolean IsInSameWindow(ProbeRequest probe)
	    {        
	        if (probe == null)
	            return false;
	        else if (probe == this)
	            return true;
	        
	        int diff = Math.abs(GetTimediffInSecond(probe));
	        
	        if (diff>=0 && diff <=2){//regard as same burst within 2 second
//	        	if(sourceMAC.equalsIgnoreCase("cc:fa:00:c7:c9:a2"))
//	        		System.out.println("same window"+' '+diff+FunctionUtility.CalendarToString(this.getStartTime())+" "+FunctionUtility.CalendarToString(probe.getEndTime()));
	        	return true;
	        }
	        else
	            return false;
	    }
	    public boolean IsInSameStaying(ProbeRequest probe, int second)
	    {        
	        if (probe == null)
	            return false;
	        else if (probe == this)
	            return true;
	        
	        int diff = Math.abs(GetTimediffInSecond(probe));
	        if (diff>=0 && diff <second)
	            return true;
	        else
	            return false;
	    }
	    
	    
	    public int  GetTimediffInSecond(ProbeRequest probe)
	    {        
	       if (probe == this)
	            return 0;
	        return (int) ((probe.getStartTime().getTimeInMillis()-this.getStartTime().getTimeInMillis())/1000);
	  
	    }
	    
	    public void PrintProbeRequest(){
	    	System.out.println(this.getStartTime().get(Calendar.YEAR)+"-"
	    						+this.getStartTime().get(Calendar.MONTH)+"-"
	    						+this.getStartTime().get(Calendar.DAY_OF_MONTH)+" "
	    						+this.getStartTime().get(Calendar.HOUR_OF_DAY)+":"
	    						+this.getStartTime().get(Calendar.MINUTE)+":"
	    						+this.getStartTime().get(Calendar.SECOND)+" "
	    						+(this.getEndTime().getTimeInMillis()-this.getStartTime().getTimeInMillis())+" "
	    						+ this.getRssi()+" "+this.sourceMAC);
	    }
	}
		
	public static class ProbeRequestBrief {
	    private  Calendar startTime;
	    private  Calendar endTime;
	    private String sourceMAC;
	    private double[] rssiVec;
	    
	    public ProbeRequestBrief(Calendar s, Calendar e,String sourceMAC, double[] al, int Apnum)
	    {
	        this.setStartTime(s);
	        this.setEndTime(e);
	        this.sourceMAC = sourceMAC;
	        rssiVec = new double[Apnum];
	        setRssiVec(al); 	
	    }	    	    
	    public String getSourceMAC() {return this.sourceMAC;}	 

		public double[] getRssiVec() {
			return rssiVec;
		}
		public void setRssiVec(double[] rssiVec) {
			this.rssiVec = rssiVec;
		}
		public Calendar getStartTime() {
			return startTime;
		}
		public void setStartTime(Calendar startTime) {
			this.startTime = startTime;
		}
		public Calendar getEndTime() {
			return endTime;
		}
		public void setEndTime(Calendar endTime) {
			this.endTime = endTime;
		}
	}
		
	public static class ProbeRequestBriefWrapper implements Clusterable {
	    private ProbeRequestBrief tProbeRequestBrief;
	    private double [] points ;
	    
	    public ProbeRequestBriefWrapper(ProbeRequestBrief t, int Apnum)
	    {
	    	this.tProbeRequestBrief=t;	    		    	
	    	double max =-100;
	    	double min =-20;
	    	points = new double[Apnum+1];
	    	
	    	for(int i=0;i<Apnum;i++){
	    		if(t.getRssiVec()[i]>max && t.getRssiVec()[i]>-99)
	    			max =t.getRssiVec()[i];	 
	    		if(t.getRssiVec()[i]<min && t.getRssiVec()[i]>-99)
	    			min =t.getRssiVec()[i];	 
	    	}
	    	
	    	for(int i=0;i<Apnum;i++){
	    		if(t.getRssiVec()[i]>-90){
	    			this.points[i]=t.getRssiVec()[i]-max;
	    		}
	    		else
	    			this.points[i]=t.getRssiVec()[i];
	    	}
	    	this.points[Apnum] = max;
	    }

		public ProbeRequestBrief gettProbeRequestBrief() {
			return tProbeRequestBrief;
		}

		@Override
		public double[] getPoint() {
			// TODO Auto-generated method stub
			return points;
		}
	}
	
	
	public static class FingerPrint {
	    private String sourceMAC;
	    private double[] rssiVec;
	    
	    public FingerPrint(String sourceMAC, double[] al, int Apnum)
	    {
	        this.sourceMAC = sourceMAC;
	        rssiVec = new double[Apnum];
	        setRssiVec(al); 	
	    }	
	    
	    public FingerPrint(String sourceMAC, int[] al, int Apnum)
	    {
	        this.sourceMAC = sourceMAC;
	        rssiVec = new double[Apnum];
	        for(int i=0;i<Apnum;i++)
	        	rssiVec[i] = al[i];
	    }	
	    
	    public String getSourceMAC() {return this.sourceMAC;}	 

		public double[] getRssiVec() {
			return rssiVec;
		}
		public void setRssiVec(double[] rssiVec) {
			this.rssiVec = rssiVec;
		}
	}
		
	public static class FingerPrintWrapper implements Clusterable {
	    private FingerPrint tFingerPrint;
	    private double [] points;
	    
	    public FingerPrintWrapper(FingerPrint t, int Apnum)
	    {
	    	this.tFingerPrint=t;
	    	points = new double[Apnum];
//	    	double max =-100;
//	    	double min =-20;
//	    	for(int i=0;i<Apnum;i++){
//	    		if(t.getRssiVec()[i]>max && t.getRssiVec()[i]>-99)
//	    			max =t.getRssiVec()[i];	 
//	    		if(t.getRssiVec()[i]<min && t.getRssiVec()[i]>-99)
//	    			min =t.getRssiVec()[i];	 
//	    	}
	    	
	    	for(int i=0;i<Apnum;i++){
//	    		if(t.getRssiVec()[i]>-99){
//	    			this.points[i]=t.getRssiVec()[i]-max;//*((t.getRssiVec()[i]-min)/(max-min));
//	    		}
//	    		else
	    			this.points[i]=t.getRssiVec()[i];
	    	}
//	    	this.points[Apnum] = max;
	    }

		public FingerPrint gettFingerPrint() {
			return tFingerPrint;
		}

		@Override
		public double[] getPoint() {
			// TODO Auto-generated method stub
			return points;
		}
	}
	
}

