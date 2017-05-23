package cir.nus.util;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JFrame;

import cir.nus.edu.ProbeRequestStructure;
import cir.nus.edu.StayPeriod;
import cir.nus.edu.ProbeRequestStructure.ProbeRequest;
import cir.nus.edu.ProbeRequestStructure.ProbeRequestBrief;

public class FunctionUtility {

	public static class Coord {
		public int x;
		public int y;

		public Coord(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	public static void showOnScreen(int screen, JFrame frame) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gd = ge.getScreenDevices();
		if (screen > -1 && screen < gd.length) {
			frame.setLocation(gd[screen].getDefaultConfiguration().getBounds().x + 80, frame.getY() + 10);
		} else if (gd.length > 0) {
			frame.setLocation(gd[0].getDefaultConfiguration().getBounds().x + 80, frame.getY() + 10);
		} else {
			throw new RuntimeException("No Screens Found");
		}
	}

	public static void PrintInfo(ArrayList<ProbeRequest> mProbeRequestList) {
		for (int i = 0; i < mProbeRequestList.size(); i++) {
			// if(i>0 &&
			// mProbeRequestList.get(i-1).GetTimediffInSecond(mProbeRequestList.get(i))>gap)
			// System.out.println(mProbeRequestList.get(i-1).GetTimediffInSecond(mProbeRequestList.get(i)));
			// mProbeRequestList.get(i).PrintProbeRequest();
			if (mProbeRequestList.get(i).getRssi().size() > 3) {
				// System.out.println("size:"+mProbeRequestList.get(i).getRssi().size()+"
				// avg:"+mProbeRequestList.get(i).getAveragerssi()+" dev:
				// "+mProbeRequestList.get(i).getDeviationrssi());
				System.out.println(CalendarToString(mProbeRequestList.get(i).getStartTime()) + " "
				// +CalendarToString(mProbeRequestList.get(i).getEndTime())+" "
						+ mProbeRequestList.get(i).getRssi().size() + " "
						+ mProbeRequestList.get(i).getAveragerssiWithoutOutliar());// +"
																					// "
				// +mProbeRequestList.get(i).getDeviationrssiWithoutOutliar());
			}
		}
	}

	public static String CalendarToString(Calendar c) {
		return "\"" + c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ ((c.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY))
				+ ":" + ((c.get(Calendar.MINUTE) < 10) ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)) + ":"
				+ ((c.get(Calendar.SECOND) < 10) ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND)) + "\" ";

	}

	public static int GetEarliestTimeIndex(ProbeRequest a, ProbeRequest b, ProbeRequest c) {
		if (a.getStartTime().before(b.getStartTime())) {
			if (a.getStartTime().before(c.getStartTime()))
				return 0;
			else
				return 2;
		} else {
			if (b.getStartTime().before(c.getStartTime()))
				return 1;
			else
				return 2;
		}
	}

	public static int GetEarliestTimeIndex(StayPeriod a, StayPeriod b, StayPeriod c) {
		if (a.getStartTime().before(b.getStartTime())) {
			if (a.getStartTime().before(c.getStartTime()))
				return 0;
			else
				return 2;
		} else {
			if (b.getStartTime().before(c.getStartTime()))
				return 1;
			else
				return 2;
		}
	}

	public static int GetEarliestTimeIndex(ProbeRequest a, ProbeRequest b) {
		if (a.getStartTime().before(b.getStartTime())) {
			return 1;
		} else {
			return 2;
		}

	}

	public static int GetEarliestTimeIndex(StayPeriod a, StayPeriod b) {
		if (a.getStartTime().before(b.getStartTime())) {
			return 0;
		} else {
			return 1;
		}

	}
	public static void GetAverageForPRB(ArrayList<ProbeRequestBrief> prb){
		
		int Apnum = prb.get(0).getRssiVec().length;
		for(int i=0;i<Apnum;i++){
			double sum =0;
			for(int j=0;j<prb.size();j++){
				sum += prb.get(j).getRssiVec()[i];				
			}
			double avg = sum/prb.size();
			
			double dev = 0;
			for(int j=0;j<prb.size();j++){
				dev += (prb.get(j).getRssiVec()[i]-avg)*(prb.get(j).getRssiVec()[i]-avg);
			}
			dev = Math.sqrt(dev/prb.size());
			System.out.println(prb.size()+" "+avg+" "+dev);
			
		}
		System.out.println("==========================");
	}
	
	static double reference2 =	-43.95; //1.7850262629124214 -49.94871328548005
	static double n2 = 1.98;
	static double reference1 =	-38.41; //1.0511160862516797 -38.4122616923553
	static double n1 =1.95;//1.19278670585941+1;
	static double referencepi =	-47.26; //pi 2.8330966739557746 -47.26465078049144
	static double npi =2.8;//
	
	public static double generateDistanceFromRssi(double rssi, int apindex) {
		// ArrayList<RssiToDistanceCon> conf = new ArrayList<RssiToDistanceCon>();
		if(apindex ==6||apindex ==7){			
			double dist = Math.pow(10.0, (double) (reference1 - rssi) / 10.0/n1);
			return  Math.round(dist * 1000.0) / 1000.0;
		}else if(apindex ==5){
			double dist = Math.pow(10.0, (double) (reference2 - rssi) / 10.0/n2);
			return  Math.round(dist * 1000.0) / 1000.0;
		}
//		else if(apindex ==3) {
//			double dist = Math.pow(10.0, (double) (referencepi - rssi) / 10.0/(npi));
//			return  Math.round(dist * 1000.0) / 1000.0;
//		}
		else{
			double dist = Math.pow(10.0, (double) (referencepi - rssi) / 10.0/npi);
			return  Math.round(dist * 1000.0) / 1000.0;
		}
//		double dist = Math.pow(10.0, (double) (referencepi - rssi) / 10.0/npi);
//		if(apindex ==3 || apindex==4||apindex==0){
//			if(dist<1)
//				dist = dist/2;
//			else
//				dist = Math.sqrt(dist*dist-1);
//		}
			
//		return  Math.round(dist * 1000.0) / 1000.0;
	}

	public static Coord generateCoordinateFromDistance3D(Coord a, Coord b, Coord c, double da, double db, double dc) {
//		da=da*10;
//		db=db*10;
//		dc=dc*10;
		double va = ((db * db - dc * dc) - (b.x * b.x - c.x * c.x) - (b.y * b.y - c.y * c.y)) / 2;
		double vb = ((db * db - da * da) - (b.x * b.x - a.x * a.x) - (b.y * b.y - a.y * a.y)) / 2;

		int y = (int) ((vb * (c.x - b.x) - va * (a.x - b.x)) / ((a.y - b.y) * (c.x - b.x) - (c.y - b.y) * (a.x - b.x)));
		int x = (int) ((va - y * (c.y - b.y)) / (c.x - b.x));

		return new Coord(x, y);
	}
//	
	public static Coord generateCoordinateFromDistanceSolo(Coord a, Coord b, Coord c, double da, double db, double dc) {
//			da=da*10;
//			db=db*10;
//			dc=dc*10;
			int granularity =3600;
			double MinimumError =Double.POSITIVE_INFINITY;
			Coord candidate = new Coord(0,0);
//			int index =0;
			for(int i=0;i<granularity;i++){
				double xe = a.x+da*Math.cos(i*2*Math.PI/granularity);
				double ye = a.y+da*Math.sin(i*2*Math.PI/granularity);
				double dbi=Math.sqrt((b.x-xe)*(b.x-xe)+(b.y-ye)*(b.y-ye));
				double dci=Math.sqrt((c.x-xe)*(c.x-xe)+(c.y-ye)*(c.y-ye));
				
				if((dbi-db)*(dbi-db)+(dci-dc)*(dci-dc)<MinimumError){
					MinimumError = (dbi-db)*(dbi-db)+(dci-dc)*(dci-dc);
					candidate = new Coord((int)xe, (int) ye);	
//					index = i;
				}
					
			}
			
//			System.out.println(candidate.x+" "+candidate.y+" "+index);
			return candidate;		
	}
	
	public static Coord generateCoordinateFromDistanceBalanced(Coord a, Coord b, Coord c, double da, double db, double dc) {

		Coord candidateA = generateCoordinateFromDistanceSolo(a,b,c,da,db,dc);
		Coord candidateB = generateCoordinateFromDistanceSolo(b,a,c,db,da,dc);
		
		double ratioA, ratioB;
		double sum = 1/da+1/db;
		ratioA = (1/da)/sum;
		ratioB = (1/db)/sum;
		
//		System.out.println(candidate.x+" "+candidate.y+" "+index);
		return new Coord((int)(candidateA.x*ratioA+candidateB.x*ratioB),(int)(candidateA.y*ratioA+candidateB.y*ratioB));		
}
	
	public static Coord generateCoordinateFromDistanceRatio(Coord a, Coord b, Coord c, double da, double db, double dc) {

		Coord candidate = new Coord(0,0);
		
		double ratioA, ratioB, ratioC;
		double sum = 1/da+1/db+1/dc;
		ratioA = (1/da)/sum;
		ratioB = (1/db)/sum;
		ratioC = (1/dc)/sum;
		candidate.x = (int)(a.x*ratioA+b.x*ratioB+c.x*ratioC);
		candidate.y = (int)(a.y*ratioA+b.y*ratioB+c.y*ratioC);
		
//		System.out.println(candidate.x+" "+candidate.y+" "+index);
		return candidate;		
}
	
	
	public static Coord generateCoordinateFromDistance(ArrayList<Coord> node,  ArrayList<Double> Rssis, int scale,BufferedImage bgimage) {
		ArrayList<Double> distance = new ArrayList<Double>();
		
		for(int i=0;i<Rssis.size();i++){
			distance.add(generateDistanceFromRssi(Rssis.get(i),i)*scale);
		}
		
		ArrayList<Double> copy = new ArrayList<Double>();
		copy.addAll(distance);
		
		Collections.sort(copy);//need to solve duplicate problem
		int a = distance.indexOf(copy.get(0));
		int b=-1;
		for(int i=0;i<distance.size();i++){
			if(Double.compare(distance.get(i),copy.get(1))==0&&i!=a){
					b=i;
					break;		
			}
		}
		int c=-1;
		for(int i=0;i<distance.size();i++){
			if(Double.compare(distance.get(i),copy.get(2))==0&&i!=a&&i!=b){
				c=i;	
				break;
			}						
		}
//		System.out.println(a+" "+b+" "+c);
		Coord result = null;
		if(distance.get(b)-distance.get(a)<2){
			result = generateCoordinateFromDistanceBalanced(node.get(a),node.get(b),node.get(c),
					distance.get(a),distance.get(b),distance.get(c));
		}		
		else{
			result = generateCoordinateFromDistanceSolo(node.get(a),node.get(b),node.get(c),
					distance.get(a),distance.get(b),distance.get(c));
		}		
//		result = generateCoordinateFromDistanceRatio(node.get(a),node.get(b),node.get(c),
//				distance.get(a),distance.get(b),distance.get(c));
		result = getGeoPoint(result, bgimage);
//		result = generateCoordinateFromDistance3D(node.get(a),node.get(b),node.get(c),
//				distance.get(a),distance.get(b),distance.get(c));
		return result;
	}
	
	
	public static boolean IsInMap(int x,int y, BufferedImage floorplan){
		if(x>0&&y>0&&x<floorplan.getWidth()&&y<floorplan.getHeight()){
			return true;
		}
		else
			return false;
	}
	
	public static Coord getGeoPoint(Coord point, BufferedImage floorplan)
	{
		boolean found =false;
		int width = floorplan.getWidth();
		int height = floorplan.getHeight();

		int x0 = point.x;
		int y0 = point.y;
		int a=-1,b=-1;
		int stepLength =5;
		
		for(int i=0; !found; i++)
		{
			int length = i*stepLength;
			int rUpper = x0-length;
			int rLower = x0+length;
			int cLeft = y0-length;
			int cRight = y0+length;
			
			if(rUpper<-50 || rLower>=width+50 || cLeft<-50 || cRight>=height+50)
			{
				break;
			}
			
			int m=rUpper;
			for(int n=cLeft; n<cRight; n++)
			{
				if(IsInMap(m,n,floorplan)){
					if(floorplan.getRGB(m,n) ==-1 || floorplan.getRGB(m,n) == -6694422){
	    				a = m;
	    				b = n;
	    				found =true;
	        			break;
	        		}	
				}			
			}
			if(found)
				break;
			
			m=rLower;
			for(int n=cLeft; n<cRight; n++)
			{
				if(IsInMap(m,n,floorplan)){
					if(floorplan.getRGB(m,n) ==-1 || floorplan.getRGB(m,n) == -6694422){
	    				a = m;
	    				b = n;
	    				found =true;
	        			break;
	        		}	
				}		
			}
			if(found)
				break;
			
			int n=cLeft;
			for(m=rUpper; m<rLower; m++)
			{
				if(IsInMap(m,n,floorplan)){
					if(floorplan.getRGB(m,n) ==-1 || floorplan.getRGB(m,n) == -6694422){
	    				a = m;
	    				b = n;
	    				found =true;
	        			break;
	        		}	
				}		
			}
			if(found)
				break;
			
			n=cRight;
			for(m=rUpper; m<rLower; m++)
			{
				if(IsInMap(m,n,floorplan)){
					if(floorplan.getRGB(m,n) ==-1 || floorplan.getRGB(m,n) == -6694422){
	    				a = m;
	    				b = n;
	    				found =true;
	        			break;
	        		}	
				}		
			}
		}
		
		if(found)
    	{
			return new Coord(a,b);
    	}else
    	{
    		return null;
    	}
	}
	
	
//	public static Coord generateCoordinateFromDistance(Coord a, Coord b, Coord c, double da, double db, double dc) {
//			da=da*10;
//			db=db*10;
//			dc=dc*10;
//			int granularity =3600;
//			double MinimumError =Double.POSITIVE_INFINITY;;
//			Coord candidate1 = new Coord(0,0);
//			int index =0;
//			for(int i=0;i<granularity;i++){
//				double xe = a.x+da*Math.cos(i*2*Math.PI/granularity);
//				double ye = a.y+da*Math.sin(i*2*Math.PI/granularity);
//				double dbi=Math.sqrt((b.x-xe)*(b.x-xe)+(b.y-ye)*(b.y-ye));
//				double dci=Math.sqrt((c.x-xe)*(c.x-xe)+(c.y-ye)*(c.y-ye));
//				
//				if((dbi-db)*(dbi-db)+(dci-dc)*(dci-dc)<MinimumError){
//					MinimumError = (dbi-db)*(dbi-db)+(dci-dc)*(dci-dc);
//					candidate1 = new Coord((int)xe, (int) ye);	
//					index = i;
//				}
//					
//			}
//			
//			System.out.println(candidate1.x+" "+candidate1.y+" "+index);
//			Coord candidate2 = generateSingleCoordinateFromDistance(b,a,c,db,da,dc);
//			Coord candidate3 = generateSingleCoordinateFromDistance(c,a,b,dc,da,db);
//			double sum = 1.0/da+1.0/db+1.0/dc;
//			double x = candidate1.x/sum/da+candidate2.x/sum/db+candidate3.x/sum/dc;
//			double y = candidate1.y/sum/da+candidate2.y/sum/db+candidate3.y/sum/dc;
//			Coord candidate = new Coord((int)x,(int)y);
//			return candidate;
//		
//}
	
	public static Coord generateSingleCoordinateFromDistance(Coord a, Coord b, Coord c, double da, double db, double dc) {
		
		int granularity =3600;
		double MinimumError =Double.POSITIVE_INFINITY;;
		Coord candidate = new Coord(0,0);
		int index =0;
		for(int i=0;i<granularity;i++){
			double xe = a.x+da*Math.cos(i*2*Math.PI/granularity);
			double ye = a.y+da*Math.sin(i*2*Math.PI/granularity);
			double dbi=Math.sqrt((b.x-xe)*(b.x-xe)+(b.y-ye)*(b.y-ye));
			double dci=Math.sqrt((c.x-xe)*(c.x-xe)+(c.y-ye)*(c.y-ye));
			
			if((dbi-db)*(dbi-db)+(dci-dc)*(dci-dc)<MinimumError){
				MinimumError = (dbi-db)*(dbi-db)+(dci-dc)*(dci-dc);
				candidate = new Coord((int)xe, (int) ye);	
				index = i;
			}
				
		}
//		System.out.println(candidate.x+" "+candidate.y+" "+index);
		
		return candidate;
	
}

	public static int GetLocation(Vector<Integer> spotted, HashMap<Integer, Vector<Double>> signalDB) {
		double distance = 50000;
		int label = 0;
		for (int key : signalDB.keySet()) {
			Vector<Double> temp = signalDB.get(key);
			if (distance > GetDistance(temp, spotted)) {
				distance = GetDistance(temp, spotted);
				label = key;
			}
		}
		return label;
	}

	public static int GetLocation(int[] spotted, HashMap<Integer, Vector<Double>> signalDB) {
		double distance = 50000;
		int label = 0;
		for (int key : signalDB.keySet()) {
			Vector<Double> temp = signalDB.get(key);
			if (distance > GetDistance(temp, spotted)) {
				distance = GetDistance(temp, spotted);
				label = key;
			}
		}
		return label;
	}

	public static double GetDistance(Vector<Double> s, Vector<Integer> t) {
		int distance = 0;
		for (int i = 0; i < t.size(); i++) {
			if (t.get(i) != 0) {
				distance += (t.get(i) - s.get(i)) * (t.get(i) - s.get(i));
			}
		}
		return Math.sqrt(distance / t.size());
	}

	public static double GetDistance(Vector<Double> s, int[] t) {
		int distance = 0;
		for (int i = 0; i < t.length; i++) {
			if (t[i] != 0) {
				distance += (t[i] - s.get(i)) * (t[i] - s.get(i));
			}
		}
		return Math.sqrt(distance / t.length);
	}
	
	public static double GetDistance(int[] s, int[] t) {
		int distance = 0;
		for (int i = 0; i < t.length; i++) {
			if (t[i] != 0) {
				distance += (t[i] - s[i]) * (t[i] - s[i]);
			}
		}
		return Math.sqrt(distance / t.length);
	}
	
	public static double GetDistance(double[] s, double[] t) {
		double distance = 0;
		for (int i = 0; i < t.length; i++) {
			if (t[i] != 0) {
				distance += (t[i] - s[i]) * (t[i] - s[i]);
			}
		}
		return Math.sqrt(distance / t.length);
	}


	public static int GetTimediffInSecond(Calendar c1, Calendar c2) {
		return (int) ((c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000);

	}

	public static int GetGap(StayPeriod s1, StayPeriod s2) {
		if (s1.getStartTime().before(s2.getStartTime())) {
			return GetTimediffInSecond(s1.getEndTime(), s2.getStartTime());
		} else {
			return GetTimediffInSecond(s2.getEndTime(), s1.getStartTime());
		}

	}

	public static int GetOverlapInSecond(StayPeriod s1, StayPeriod s2) {
		if (s1.getStartTime().before(s2.getStartTime())) {
			if (s1.getEndTime().before(s2.getStartTime()))
				return 0;
			else if (s1.getEndTime().after(s2.getEndTime()))
				return GetTimediffInSecond(s2.getStartTime(), s2.getEndTime()) + 1;
			else
				return GetTimediffInSecond(s2.getStartTime(), s1.getEndTime()) + 1;
		} else if (s1.getStartTime().after(s2.getEndTime()) || s1.getStartTime().equals(s2.getEndTime())) {
			return 0;
		} else {
			if (s1.getEndTime().before(s2.getEndTime()) || s1.getEndTime().equals(s2.getEndTime()))
				return GetTimediffInSecond(s1.getStartTime(), s1.getEndTime()) + 1;
			else
				return GetTimediffInSecond(s1.getStartTime(), s2.getEndTime()) + 1;
		}
	}
	
	public static int GetOverlapInSecond(ProbeRequestBrief s1, ProbeRequestBrief s2) {
		if (s1.getStartTime().before(s2.getStartTime())) {
			if (s1.getEndTime().before(s2.getStartTime()))
				return 0;
			else if (s1.getEndTime().after(s2.getEndTime()))
				return GetTimediffInSecond(s2.getStartTime(), s2.getEndTime()) + 1;
			else
				return GetTimediffInSecond(s2.getStartTime(), s1.getEndTime()) + 1;
		} else if (s1.getStartTime().after(s2.getEndTime()) || s1.getStartTime().equals(s2.getEndTime())) {
			return 0;
		} else {
			if (s1.getEndTime().before(s2.getEndTime()) || s1.getEndTime().equals(s2.getEndTime()))
				return GetTimediffInSecond(s1.getStartTime(), s1.getEndTime()) + 1;
			else
				return GetTimediffInSecond(s1.getStartTime(), s2.getEndTime()) + 1;
		}
	}

	public static boolean CanMerge(ProbeRequestBrief s1, ProbeRequestBrief s2) {

		if ((s1.getEndTime().before(s2.getStartTime())) || (s1.getStartTime().after(s2.getEndTime()))){
			if(GetGap(s1,s2)>300)//10 min				
				return false;
			else
				return true;
		}
		else 
			return true;
	}

	public static int GetGap(ProbeRequestBrief s1, ProbeRequestBrief s2) {
		if (s1.getEndTime().before(s2.getStartTime())) {
			return GetTimediffInSecond(s1.getEndTime(), s2.getStartTime());
		} else {
			return GetTimediffInSecond(s2.getEndTime(), s1.getStartTime());
		}
	}

}
