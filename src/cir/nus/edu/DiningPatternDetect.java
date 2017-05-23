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

public class DiningPatternDetect {
	HashMap<String, ArrayList<ProbeRequestBrief>> mMacToDiningTrace = new HashMap<String, ArrayList<ProbeRequestBrief>>();
	HashMap<String, ArrayList<ProbeRequestBrief>> MacToProbeVector = new HashMap<String, ArrayList<ProbeRequestBrief>>();
	BufferedImage bg ;
	ArrayList<Coord> APs;
	int Apnum;
	int Scale;
	
	public DiningPatternDetect(HashMap<String, ArrayList<ProbeRequestBrief>> MPV, BufferedImage bgimage, ArrayList<Coord> anchors, int apnum, int scale){
		this.Apnum = apnum;
		this.bg = bgimage;
		this.MacToProbeVector = MPV;
		this.APs = anchors;
		this.Scale = scale;
	}
	
	public HashMap<String, ArrayList<ProbeRequestBrief>> generatecandidateDiningTrace(){
		for(String m:MacToProbeVector.keySet()){
			ArrayList<ProbeRequestBrief> temptrace = new ArrayList<ProbeRequestBrief>();
			for(int i=0;i<MacToProbeVector.get(m).size();){ // find the first trace begin with order area
				if(temptrace.isEmpty()){
					ArrayList<Double> index = new ArrayList<Double>();
					for(int j=0;j<Apnum;j++)
						index.add( (double)MacToProbeVector.get(m).get(i).getRssiVec()[j]);
					//in order area
					Coord tmp = FunctionUtility.generateCoordinateFromDistance(APs,index,Scale,bg);
//					if(IsInOrderingFoodArea(tmp))
						temptrace.add(MacToProbeVector.get(m).get(i));
					i++;
				}
				else{
//					if(GetTimeDiffInSecond(temptrace.get(temptrace.size()-1).getEndTime(),MacToProbeVector.get(m).get(i).getStartTime())<10*60){
						temptrace.add(MacToProbeVector.get(m).get(i));
						i++;
//					}
//					else{
//						temptrace.clear();						
//					}
				}
			}
			if(!temptrace.isEmpty()&&GetTimeDiffInSecond(temptrace.get(0).getStartTime(),temptrace.get(temptrace.size()-1).getEndTime())>10*60){
				mMacToDiningTrace.put(m, temptrace);
//				System.out.println(m+" "+CalendarToString(temptrace.get(0).getStartTime())+" "+CalendarToString(temptrace.get(temptrace.size()-1).getEndTime())+" "+GetTimeDiffInSecond(temptrace.get(0).getStartTime(),temptrace.get(temptrace.size()-1).getEndTime())/60+" min");
			}
		}
		return mMacToDiningTrace;
		
	}
	
	public HashMap<String, ArrayList<ProbeRequestBrief>> ConvergeDiningTrace() {
		for (String m : mMacToDiningTrace.keySet()) {
			int i = 0;
			ArrayList<ArrayList<Double>> rssis = new ArrayList<ArrayList<Double>>();
			for(int j=0;j<Apnum;j++){
				ArrayList<Double> rssi = new ArrayList<Double>();
				if(mMacToDiningTrace.get(m).get(0).getRssiVec()[j]!=-99)
					rssi.add(mMacToDiningTrace.get(m).get(0).getRssiVec()[j]);
				rssis.add(rssi);
			}
				
			
			while (i < mMacToDiningTrace.get(m).size() - 1) {

				if (CanMerge(mMacToDiningTrace.get(m).get(i), mMacToDiningTrace.get(m).get(i + 1))
						&& GetDistance(mMacToDiningTrace.get(m).get(i).getRssiVec(),mMacToDiningTrace.get(m).get(i + 1).getRssiVec()) < 5) {
					mMacToDiningTrace.get(m).get(i).setEndTime(mMacToDiningTrace.get(m).get(i + 1).getEndTime());
					for (int j = 0; j < Apnum; j++){
						if(mMacToDiningTrace.get(m).get(i + 1).getRssiVec()[j]!=-99)
							rssis.get(j).add(mMacToDiningTrace.get(m).get(i + 1).getRssiVec()[j]);
					}
					mMacToDiningTrace.get(m).remove(i + 1);
				} else {
					for (int j = 0; j < Apnum; j++){
							mMacToDiningTrace.get(m).get(i).getRssiVec()[j] = getAverage(rssis.get(j));						
							if(i+1 < mMacToDiningTrace.get(m).size()){
								rssis.get(j).clear();
								if(mMacToDiningTrace.get(m).get(i+1).getRssiVec()[j]!=-99)
									rssis.get(j).add(mMacToDiningTrace.get(m).get(i+1).getRssiVec()[j]);
							}
							
					}
					i++;				
				}
			}
		}
		return mMacToDiningTrace;
	}
	
	public HashMap<String, ArrayList<ProbeRequestBrief>> FormaliseDiningTrace() {
		for (String m : mMacToDiningTrace.keySet()) {
			int i = 0;
			ArrayList<ArrayList<Double>> rssis = new ArrayList<ArrayList<Double>>();
			for(int j=0;j<Apnum;j++){
				ArrayList<Double> rssi = new ArrayList<Double>();
//				if(mMacToDiningTrace.get(m).get(0).getRssiVec()[j]!=-99)
					rssi.add(mMacToDiningTrace.get(m).get(0).getRssiVec()[j]);
				rssis.add(rssi);
			}
				
			
			while (i < mMacToDiningTrace.get(m).size() - 1) {

				if (CanMerge(mMacToDiningTrace.get(m).get(i), mMacToDiningTrace.get(m).get(i + 1))
						&&( IsInOrderingFoodArea(mMacToDiningTrace.get(m).get(i)) == IsInOrderingFoodArea(mMacToDiningTrace.get(m).get(i + 1)))) {
					mMacToDiningTrace.get(m).get(i).setEndTime(mMacToDiningTrace.get(m).get(i + 1).getEndTime());
					for (int j = 0; j < Apnum; j++){
//						if(mMacToDiningTrace.get(m).get(i + 1).getRssiVec()[j]!=-99)
							rssis.get(j).add(mMacToDiningTrace.get(m).get(i + 1).getRssiVec()[j]);
					}
					mMacToDiningTrace.get(m).remove(i + 1);
				} else {
					for (int j = 0; j < Apnum; j++){
							mMacToDiningTrace.get(m).get(i).getRssiVec()[j] = getAverage(rssis.get(j));						
							if(i+1 < mMacToDiningTrace.get(m).size()){
								rssis.get(j).clear();
//								if(mMacToDiningTrace.get(m).get(i+1).getRssiVec()[j]!=-99)
									rssis.get(j).add(mMacToDiningTrace.get(m).get(i+1).getRssiVec()[j]);
							}
							
					}
					i++;				
				}
			}
		}
		return mMacToDiningTrace;
	}
	
	public void findPeopleWithSimilarDiningTime(String mac, double eps, int minpts){
		ArrayList<ProbeRequestBrief> master = mMacToDiningTrace.get(mac);
		ArrayList<FingerPrint> FPList = new ArrayList<FingerPrint>();
		
		Map<String, ArrayList<ProbeRequestBrief>> sortedmap = new TreeMap<String, ArrayList<ProbeRequestBrief>>(
				mMacToDiningTrace);
		
		for(String m: sortedmap.keySet()){
			ArrayList<ProbeRequestBrief> slave = mMacToDiningTrace.get(m);
			ArrayList<ProbeRequestBrief> seat = new ArrayList<ProbeRequestBrief> ();
			if(GetTimeDiffInSecond(master.get(0).getStartTime(),slave.get(0).getStartTime())<10*60 
					&& GetTimeDiffInSecond(master.get(master.size()-1).getEndTime(),slave.get(slave.size()-1).getEndTime())<10*60){
				System.out.print(m+" "+GetTimeDiffInSecond(slave.get(0).getStartTime(),slave.get(slave.size()-1).getEndTime())/60+"min ");
				
				
				if(!IsInOrderingFoodArea(slave.get(0))&&!IsInOrderingFoodArea(slave.get(1)))
					seat.add(slave.get(0));
				
				for(int i=1;i<slave.size()-1;i++){
					if(!IsInOrderingFoodArea(slave.get(i-1))&&!IsInOrderingFoodArea(slave.get(i))&&!IsInOrderingFoodArea(slave.get(i+1)))
						seat.add(slave.get(i));
				}
				if(!IsInOrderingFoodArea(slave.get(slave.size()-1))&&!IsInOrderingFoodArea(slave.get(slave.size()-2)))
					seat.add(slave.get(slave.size()-1));
				
				
				if(seat.size()>2){
					double[] fingerprint = GetLongStayFingerprintByKMeansPlusPlusCluster(2, 100,seat);
					FingerPrint fp = new FingerPrint(m, fingerprint,Apnum);
					FPList.add(fp);
				}
			}
		}
		System.out.println(FPList.size());
		
			
		
		
		DBSCANClusterFingerPrint(eps, minpts, FPList);
		
//		List<FingerPrintWrapper> clusterInput = new ArrayList<FingerPrintWrapper>(FPList.size());
//		for (FingerPrint b : FPList)
//			clusterInput.add(new FingerPrintWrapper(b));
//		KMeansPlusPlusClusterFingerPrint(2,100,clusterInput,mac);
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

	void DBSCANClusterFingerPrint(double eps, int MinPts, ArrayList<FingerPrint> ProbeVector) {	

		List<FingerPrintWrapper> clusterInput = new ArrayList<FingerPrintWrapper>(ProbeVector.size());
		for (FingerPrint b : ProbeVector)
			clusterInput.add(new FingerPrintWrapper(b,Apnum));
		
		DBSCANClusterer<FingerPrintWrapper> clusterer = new DBSCANClusterer<FingerPrintWrapper>(eps, MinPts);
		List<Cluster<FingerPrintWrapper>> clusterResults = clusterer.cluster(clusterInput);
		System.out.println(clusterResults.size());
		// output the clusters
 
		for (int i = 0; i < clusterResults.size(); i++) {
			ArrayList<Double> index = new ArrayList<Double>();
			for(int j=0;j<Apnum;j++){
				index.add(0.0);
			}
			for (FingerPrintWrapper Fp : clusterResults.get(i).getPoints()) {
				for(int j=0;j<Apnum;j++){
		    		index.set(j, index.get(j)+Fp.getPoint()[j]);
		    	}	
			}
			
		    for(int j=0;j<Apnum;j++){
	    		index.set(j, index.get(j)/(double)clusterResults.get(i).getPoints().size());
	    	}	

			System.out.println("C" + i);
			System.out.println("RSSI(db): {"+ArrayListToString(index)+"}");
			System.out.println("total number of nodes: " + clusterResults.get(i).getPoints().size());
			String member = "";
			for (FingerPrintWrapper Fp : clusterResults.get(i).getPoints()) {
				member += Fp.gettFingerPrint().getSourceMAC() + " ";
			}
			System.out.print("Member:" + member + "\n");

		}

	}
	
	String ArrayListToString (ArrayList<Double> t){
		String info="";
		for(int i=0;i<t.size();i++)
			info +=t.get(i)+" ";
		return info;
		
	}
	
	void KMeansPlusPlusClusterFingerPrint( int k, int iterationNum,List<FingerPrintWrapper> clusterInput, String mac) {
		KMeansPlusPlusClusterer<FingerPrintWrapper> clusterer = new KMeansPlusPlusClusterer<FingerPrintWrapper>(
				k, iterationNum);
		List<CentroidCluster<FingerPrintWrapper>> clusterResults = clusterer.cluster(clusterInput);
		// output the clusters
		
		int clusterindex =-1;

		for (int i = 0; i < clusterResults.size(); i++) {
			if(clusterindex == -1){
				for(int j=0;j<clusterResults.get(i).getPoints().size();j++){
					if(clusterResults.get(i).getPoints().get(j).gettFingerPrint().getSourceMAC().equals(mac)){
						clusterindex = i;
						break;
					}				
				}
			}		
		}
		double[] center = clusterResults.get(clusterindex).getCenter().getPoint();
		
		String rssi ="RSSI(db): {";
		for(int l=0;l<center.length;l++)
			rssi +=Math.round(center[l])+" ";
		
		rssi +="}";
		
		System.out.println(rssi);
		System.out.println("total number of nodes: " + clusterResults.get(clusterindex).getPoints().size());
		String member = "";
		for (FingerPrintWrapper Fp : clusterResults.get(clusterindex).getPoints()) {
			member += Fp.gettFingerPrint().getSourceMAC() + " ";
		}
		System.out.print("Member:" + member + "\n");
		
		if(clusterResults.get(clusterindex).getPoints().size()>10)
			KMeansPlusPlusClusterFingerPrint(k,iterationNum,clusterResults.get(clusterindex).getPoints(),mac);
	}
	
	double[] GetLongStayFingerprintByKMeansPlusPlusCluster( int k, int iterationNum,ArrayList<ProbeRequestBrief> ProbeVector) {

		List<ProbeRequestBriefWrapper> clusterInput = new ArrayList<ProbeRequestBriefWrapper>(ProbeVector.size());
		for (ProbeRequestBrief b : ProbeVector)
			clusterInput.add(new ProbeRequestBriefWrapper(b,Apnum));

		KMeansPlusPlusClusterer<ProbeRequestBriefWrapper> clusterer = new KMeansPlusPlusClusterer<ProbeRequestBriefWrapper>(
				k, iterationNum);
		List<CentroidCluster<ProbeRequestBriefWrapper>> clusterResults = clusterer.cluster(clusterInput);
		// output the clusters
		int Clustrsize = 0;
		double[] center = { 0 };
		for (int i = 0; i < clusterResults.size(); i++) {
			if (clusterResults.get(i).getPoints().size() > Clustrsize) {
				Clustrsize = clusterResults.get(i).getPoints().size();
				center = clusterResults.get(i).getCenter().getPoint();
			}
		}
		
		String info=ProbeVector.size() + " " + Clustrsize + " ";//+ clusterResults.get(0).getPoints().get(0).gettProbeRequestBrief().getSourceMAC() + " ";
		for(int i=0;i<center.length;i++){
			info += Math.round(center[i]*100.0)/100.0+ " ";
		}
		System.out.println(info);
		System.out.println();
		return center;

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
