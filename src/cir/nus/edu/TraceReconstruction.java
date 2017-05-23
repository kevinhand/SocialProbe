package cir.nus.edu;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import cir.nus.edu.ProbeRequestStructure.FingerPrint;
import cir.nus.edu.ProbeRequestStructure.FingerPrintWrapper;
import cir.nus.edu.ProbeRequestStructure.ProbeRequestBrief;
import cir.nus.edu.ProbeRequestStructure.ProbeRequestBriefWrapper;
import cir.nus.util.FunctionUtility;
import cir.nus.util.FunctionUtility.Coord;

public class TraceReconstruction {
	ArrayList<ArrayList<ProbeRequestBrief>> mMovementTrace = new ArrayList<ArrayList<ProbeRequestBrief>>();
	ArrayList<ProbeRequestBrief> mProbeVector = new ArrayList<ProbeRequestBrief>();
	BufferedImage bg ;
	ArrayList<Coord> APs;
	int Apnum;
	int Scale;
	
	public TraceReconstruction(ArrayList<ProbeRequestBrief> MPV, BufferedImage bgimage, ArrayList<Coord> anchors, int apnum, int scale){
		this.Apnum = apnum;
		this.bg = bgimage;
		this.mProbeVector = MPV;
		this.APs = anchors;
		this.Scale = scale;
	}
	
	public ArrayList<ArrayList<ProbeRequestBrief>> generateIndependentTrace() {
		CompressTrace();
		mMovementTrace = new ArrayList<ArrayList<ProbeRequestBrief>>();
		ArrayList<ProbeRequestBrief> temptrace = new ArrayList<ProbeRequestBrief>();
		for (int i = 0; i < mProbeVector.size();i++) {
			if (temptrace.isEmpty()) {
				temptrace.add(mProbeVector.get(i));
//				i++;
			} else {
				if (GetTimeDiffInSecond(temptrace.get(temptrace.size() - 1).getEndTime(),
						mProbeVector.get(i).getStartTime()) < 10 * 60) {
					temptrace.add(mProbeVector.get(i));
//					i++;
				} else {
					if(temptrace.size()>5){
						mMovementTrace.add(temptrace);
						System.out.println(CalendarToString(temptrace.get(0).getStartTime()) + " "
								+ CalendarToString(temptrace.get(temptrace.size() - 1).getEndTime()) + " "
								+ GetTimeDiffInSecond(temptrace.get(0).getStartTime(),
										temptrace.get(temptrace.size() - 1).getEndTime()) / 60
								+ " min  "+ temptrace.size());
					}
					temptrace = new ArrayList<ProbeRequestBrief>();
					temptrace.add(mProbeVector.get(i));
					
				}
			}
		}
		if(temptrace.size()>5){
			mMovementTrace.add(temptrace);
			System.out.println(CalendarToString(temptrace.get(0).getStartTime()) + " "
					+ CalendarToString(temptrace.get(temptrace.size() - 1).getEndTime()) + " "
					+ GetTimeDiffInSecond(temptrace.get(0).getStartTime(),
							temptrace.get(temptrace.size() - 1).getEndTime()) / 60
					+ " min  "+ temptrace.size());
		}
		return mMovementTrace;

	}
	
	public ArrayList<ProbeRequestBrief> CompressTrace() {
			
			ArrayList<ArrayList<Double>> rssis = new ArrayList<ArrayList<Double>>();
			for(int j=0;j<Apnum;j++){
				ArrayList<Double> rssi = new ArrayList<Double>();
				if(mProbeVector.get(0).getRssiVec()[j]!=-99)
					rssi.add(mProbeVector.get(0).getRssiVec()[j]);
				rssis.add(rssi);
			}
				
			int i = 0;
			while (i < mProbeVector.size() - 1) {

				if (CanMerge(mProbeVector.get(i), mProbeVector.get(i + 1))
						&& GetDistance(mProbeVector.get(i).getRssiVec(),mProbeVector.get(i + 1).getRssiVec()) < 5) {
					mProbeVector.get(i).setEndTime(mProbeVector.get(i + 1).getEndTime());
					for (int j = 0; j < Apnum; j++){
						if(mProbeVector.get(i + 1).getRssiVec()[j]!=-99)
							rssis.get(j).add(mProbeVector.get(i + 1).getRssiVec()[j]);
					}
					mProbeVector.remove(i + 1);
				} else {
					for (int j = 0; j < Apnum; j++){
							mProbeVector.get(i).getRssiVec()[j] = getAverage(rssis.get(j));						
							if(i+1 < mProbeVector.size()){
								rssis.get(j).clear();
								if(mProbeVector.get(i+1).getRssiVec()[j]!=-99)
									rssis.get(j).add(mProbeVector.get(i+1).getRssiVec()[j]);
							}
							
					}
					i++;
				}
			}
		
		return mProbeVector;
	}
	

	

	
	double getSimilarityOfFingerprint(double[] a, double[] b) {
		double result = (getCrossPorduct(a, b))
				/ (getCrossPorduct(a, a) + getCrossPorduct(b, b) - getCrossPorduct(a, b));
		result = result - Math.abs(a[a.length - 1] - b[b.length - 1]) / 100.0;
		return result;
	}
	
	double getCrossPorduct(double[] a, double[] b) {
		if (a.length != b.length)
			return 0;
		else {
			double result = 0;
			for (int i = 0; i < a.length; i++) {
				result += a[i] * b[i];
			}

			return result;
		}
	}


	String ArrayListToString (ArrayList<Double> t){
		String info="";
		for(int i=0;i<t.size();i++)
			info +=t.get(i)+" ";
		return info;
		
	}
	
	
	public double getAverage(ArrayList<Double> rssi){
		if(rssi.isEmpty())
			return -99.0;
		else{
			double sum =0;
			for(int i=0;i<rssi.size();i++){
				sum+=rssi.get(i);
			}
			return sum/(int)rssi.size();		
		}
	}
	
	public  boolean CanMerge(ProbeRequestBrief s1, ProbeRequestBrief s2) {

		if ((s1.getEndTime().before(s2.getStartTime())) || (s1.getStartTime().after(s2.getEndTime()))){
			if(GetGap(s1,s2)>600)//10 min				
				return false;
			else
				return true;
		}
		else 
			return true;
	}
	
	public int GetGap(ProbeRequestBrief s1, ProbeRequestBrief s2) {
		if (s1.getEndTime().before(s2.getStartTime())) {
			return GetTimeDiffInSecond(s1.getEndTime(), s2.getStartTime());
		} else {
			return GetTimeDiffInSecond(s2.getEndTime(), s1.getStartTime());
		}
	}
	
	double GetDistance(double[] s, double[] t) {
		double distance = 0;
		int size = 0;
		for (int i = 0; i < t.length; i++) {
			if (t[i] >-99&&t[i]<-20&&s[i] >-99&&s[i]<-20) {
				distance += (t[i] - s[i]) * (t[i] - s[i]);
				size++;
			}
		}
		if(size>=4)
			return Math.sqrt(distance / (int)size);
		else
			return 20;
	}
	
	public boolean IsInOrderingFoodArea(ProbeRequestBrief pb){ 
		ArrayList<Double> index = new ArrayList<Double>();
		for (int j = 0; j < Apnum; j++) {
			index.add((double) pb.getRssiVec()[j]);
		}

		Coord tmp = FunctionUtility.generateCoordinateFromDistance(APs, index, Scale, bg);
		if(tmp ==null)
			return false;
		if(bg.getRGB(tmp.x,tmp.y) == -6694422)
			return true;		
		else
			return false;	
	}
	
	public boolean IsInOrderingFoodArea(Coord point ){ 
		if(point ==null)
			return false;
		if(bg.getRGB(point.x,point.y) == -6694422)
			return true;		
		else
			return false;	
	}
	
	public boolean IsInMap(Coord point){
		if(point.x>100&&point.y>50&&point.x<(10.5*50+100)&&point.y<(12.8*50+50)){
			return true;
		}
		else
			return false;
	}
	
	public String CalendarToString(Calendar c) {
		return "\"" + c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ ((c.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY))
				+ ":" + ((c.get(Calendar.MINUTE) < 10) ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)) + ":"
				+ ((c.get(Calendar.SECOND) < 10) ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND)) + "\" ";

	}
	
	public int GetTimeDiffInSecond(Calendar a, Calendar b){
		if(a==null||b ==null)
			return 0;
		else
			return (int) Math.abs((b.getTimeInMillis()-a.getTimeInMillis())/1000);
	}
	
	
}
