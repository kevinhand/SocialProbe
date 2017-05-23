package cir.nus.edu;

import java.util.ArrayList;
import java.util.Calendar;

import cir.nus.edu.ProbeRequestStructure.ProbeRequestBrief;

/**
 *
 * @author Hande
 */
public class StayPeriod {
    private Calendar starttime;
    private Calendar endtime;
    private ArrayList<Integer> rssiList = new ArrayList<Integer>();

    
    public StayPeriod(Calendar s, Calendar e)
    {
        this.starttime = s;
        this.endtime = e;   
        rssiList = new ArrayList<Integer>();
    }
    
    public Calendar getStartTime() {return this.starttime;}
    public Calendar getEndTime() {return this.endtime;}
    public void setStartTime(Calendar s) {this.starttime = s;}
    public void setEndTime(Calendar e) {this.endtime = e;}
    
    
    public boolean IsInThisPeriod(Calendar t){
    	if(t==null)
    		return false;
    	if(t.before(starttime)||t.after(endtime))
    		return false;
    	else 
    		return true;
    }
    
    public boolean IsInThisPeriod(StayPeriod t){
    	if(t==null){
//    		System.out.println("null");
    		return false;
    	}
    	if(this.GetOverlapInSecond(t)<1){ // less than 1 second
//    		System.out.println("overlap: "+this.GetOverlapInSecond(t));
    		return false;
    	}
    	else 
    		return true;
    }
    
    public int GetOverlapInSecond(StayPeriod in){
    	if(in.starttime.before(this.starttime)){
    		if(in.endtime.before(this.starttime))
    			return 0;
    		else if(in.endtime.after(this.endtime))
    			return GetTimediffInSecond(this.starttime,this.endtime)+1;
    		else
    			return GetTimediffInSecond(this.starttime,in.endtime)+1;
    	}
    	else if(in.starttime.after(this.endtime) || in.starttime.equals(this.endtime)){
    		return 0;
    	}
    	else{
    		if(in.endtime.before(this.endtime) || in.endtime.equals(this.endtime) )
    			return GetTimediffInSecond(in.starttime,in.endtime)+1;
    		else 
    			return GetTimediffInSecond(in.starttime,this.endtime)+1;  			
    	}    	    	
    }
    
	public boolean CanMerge(StayPeriod in) {

		if ((this.getEndTime().before(in.getStartTime())) || (this.getStartTime().after(in.getEndTime()))){
			if(GetGap(this,in)>300)//5 min				
				return false;
			else
				return true;
		}
		else 
			return true;
	}
	
	public int GetGap(StayPeriod s1, StayPeriod s2) {
		if (s1.getEndTime().before(s2.getStartTime())) {
			return GetTimediffInSecond(s1.getEndTime(), s2.getStartTime());
		} else {
			return GetTimediffInSecond(s2.getEndTime(), s1.getStartTime());
		}
	}
    
    
    public int  GetTimediffInSecond(Calendar c1,Calendar c2)
    {        
        return Math.abs((int) ((c2.getTimeInMillis()-c1.getTimeInMillis())/1000)); 
    }

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
    			if(Math.abs(rssiList.get(i)-avg)<=1.5*dev){
    				var +=(rssiList.get(i)-avg)*(rssiList.get(i)-avg);
    				cc++;
    			}
    		}
    		return Math.sqrt((var/cc));   		
    	}
    	else
    		return 0;   	
    }
    
    
	public ArrayList<Integer> getRssiVec() {
		return rssiList;
	}

	public void setRssiVec(ArrayList<Integer> rssiVec) {
		this.rssiList = rssiVec;
	}

}


