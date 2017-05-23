package cir.nus.edu;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JTextArea;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.clustering.MultiKMeansPlusPlusClusterer;

import cir.nus.edu.ProbeRequestStructure.ProbeRequestBrief;
import cir.nus.edu.ProbeRequestStructure.ProbeRequestBriefWrapper;
import cir.nus.util.FunctionUtility.Coord;

public class ProbeRequestCluster {
	
	private  ArrayList<Coord> Anchor = new ArrayList<Coord>();
	
	private ArrayList<ProbeRequestBrief> ListOfProbeVector = null;
	private JTextArea textArea;
	public Configuration config;
	
	public ProbeRequestCluster( JTextArea textArea,ArrayList<ProbeRequestBrief> ListOfProbeVector, Graphics FloorMapWindow){
		config = new Configuration();		
		this.ListOfProbeVector=ListOfProbeVector;
		this.textArea=textArea;	
		BuildAnchorMap();
		
	}
	
	void BuildAnchorMap(){
		if(config.id.equals("biz")){
			BuildAnchorMapforBizCanteen();
		}else if(config.id.equals("lab")){
			BuildAnchorMapforLab();
		}
	}
	void BuildAnchorMapforBizCanteen() {
		
		Coord ap0 = new Coord(145 * config.scale / 20, 405 * config.scale / 20);
		Coord ap1 = new Coord(290 * config.scale / 20, 986 * config.scale / 20);
		Coord ap2 = new Coord(290 * config.scale / 20, 696 * config.scale / 20);
		Coord ap3 = new Coord(145 * config.scale / 20, 114 * config.scale / 20);
		Coord ap4 = new Coord(290 * config.scale / 20, 550 * config.scale / 20);
		Coord ap5 = new Coord(145 * config.scale / 20, 696 * config.scale / 20);// canteen
		Coord ap7 = new Coord(580 * config.scale / 20, 841 * config.scale / 20);
		Coord ap6 = new Coord(435 * config.scale / 20, 260 * config.scale / 20);
		
		Anchor.clear();
		Anchor.add(ap0);
		Anchor.add(ap1);
		Anchor.add(ap2);
		Anchor.add(ap3);
		Anchor.add(ap4);
		Anchor.add(ap5);
		Anchor.add(ap6);
		Anchor.add(ap7);
	}
	void BuildAnchorMapforLab() {
		
		Coord ap0 = new Coord(72*config.scale/10,53*config.scale/10);//lab pi unit	 10cm
		Coord ap1 = new Coord(57*config.scale/10,0*config.scale/10);
		Coord ap2 = new Coord(105*config.scale/10,105*config.scale/10);
		Coord ap3 = new Coord(31*config.scale/10,105*config.scale/10);
		Coord ap4 = new Coord(66*config.scale/10,105*config.scale/10);
		
		
		Anchor.clear();
		Anchor.add(ap0);
		Anchor.add(ap1);
		Anchor.add(ap2);
		Anchor.add(ap3);
		Anchor.add(ap4);
	}

	
	void DBSCANClusterProbeRequest(Calendar s, Calendar e, int scale, double eps, int MinPts,DrawingPanel centerPanel){
		BuildAnchorMap();
		List<ProbeRequestBriefWrapper> clusterInput = new ArrayList<ProbeRequestBriefWrapper>(ListOfProbeVector.size());
		for(ProbeRequestBrief b : ListOfProbeVector)
			if(b.getEndTime().before(e)&&b.getStartTime().after(s))
				clusterInput.add(new ProbeRequestBriefWrapper(b,config.apNum));
				
	
		DBSCANClusterer<ProbeRequestBriefWrapper> clusterer = new DBSCANClusterer<ProbeRequestBriefWrapper>(eps, MinPts);
		List<Cluster<ProbeRequestBriefWrapper>> clusterResults = clusterer.cluster(clusterInput);
		
		// output the clusters
		ArrayList<Double> index = new ArrayList<Double>();
		for(int j=0;j<config.apNum;j++){
			index.add(0.0);
		}
		
		for (int i=0; i<clusterResults.size(); i++) {
		    for (ProbeRequestBriefWrapper ProbeRequestBriefWrapper : clusterResults.get(i).getPoints()){
		    	ProbeRequestBrief p = ProbeRequestBriefWrapper.gettProbeRequestBrief();
		    	for(int j=0;j<config.apNum;j++){
		    		index.set(j, index.get(j)+p.getRssiVec()[j]);
		    	}	    		    	         
		    }
		    
		    for(int j=0;j<config.apNum;j++){
	    		index.set(j, index.get(j)/(double)clusterResults.get(i).getPoints().size());
	    	}	
		    
//		    Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor,index, mScale);		    
		    UpdateddbsText(index, i, clusterResults);
		}
		centerPanel.setIsIsKmeans(false);
		centerPanel.ClusterResultForDBS = clusterResults;
		centerPanel.revalidate();
		centerPanel.repaint();
	}

	void UpdatekmeanText(double[] center,int i, List<CentroidCluster<ProbeRequestBriefWrapper>> clusterResults){
		textArea.append("C"+i+"\n");
		String rssi ="RSSI(db): {";
		for(int k=0;k<center.length;k++)
			rssi +=Math.round(center[k])+" ";
		
		rssi +="}"+"\n";
		textArea.append(rssi);
		textArea.append("total number of nodes: "+clusterResults.get(i).getPoints().size()+"\n");
        textArea.append("\n");
	}
	
	String ArrayListToSting (ArrayList<Double> t){
		String info="";
		for(int i=0;i<t.size();i++)
			info +=Math.round(t.get(i))+" ";
		return info;
		
	}
	void UpdateddbsText(ArrayList<Double> center,int i, List<Cluster<ProbeRequestBriefWrapper>> clusterResults){
		textArea.append("C"+i+"\n");
		textArea.append("RSSI(db): {"+ArrayListToSting(center)+"}"+"\n");
		textArea.append("total number of nodes: "+clusterResults.get(i).getPoints().size()+"\n");
        textArea.append("\n");
	}
	
	void KMeansPlusPlusClusterProbeRequest(Calendar s, Calendar e, int scale, int k, int iterationNum,DrawingPanel centerPanel){
		BuildAnchorMap();
//		ArrayList<ClusterPlotList> cr = new ArrayList<ClusterPlotList>();
		List<ProbeRequestBriefWrapper> clusterInput = new ArrayList<ProbeRequestBriefWrapper>(ListOfProbeVector.size());
//		 System.out.println(ListOfProbeVector.size());
		for(ProbeRequestBrief b : ListOfProbeVector)
			if(b.getEndTime().before(e)&&b.getStartTime().after(s))
				clusterInput.add(new ProbeRequestBriefWrapper(b,config.apNum));
					
		KMeansPlusPlusClusterer<ProbeRequestBriefWrapper> clusterer = new KMeansPlusPlusClusterer<ProbeRequestBriefWrapper>(k, iterationNum);
		List<CentroidCluster<ProbeRequestBriefWrapper>> clusterResults = clusterer.cluster(clusterInput);
		
		// output the clusters
		for (int i=0; i<clusterResults.size(); i++) {
		    double[] center = clusterResults.get(i).getCenter().getPoint();
		    ArrayList<Double> index = new ArrayList<Double>();
		    for(int j=0;j<config.apNum;j++){
		    	index.add(center[j]);
		    }
//		    Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor,index,mScale);		 
		    UpdatekmeanText(center, i, clusterResults);
		}
		centerPanel.setIsIsKmeans(true);
		centerPanel.clusterResultsForKmeans = clusterResults;
		centerPanel.revalidate();
		centerPanel.repaint();
	}
	
	void MultiKMeansPlusPlusClusterProbeRequest(Calendar s, Calendar e,int scale, int k, int iterationNum, int numTrials,DrawingPanel centerPanel){
		BuildAnchorMap();
		List<ProbeRequestBriefWrapper> clusterInput = new ArrayList<ProbeRequestBriefWrapper>(ListOfProbeVector.size());
		for(ProbeRequestBrief b : ListOfProbeVector)
			if(b.getStartTime().before(e)&&b.getEndTime().after(s))
				clusterInput.add(new ProbeRequestBriefWrapper(b,config.apNum));
					
		KMeansPlusPlusClusterer<ProbeRequestBriefWrapper> clusterer = new KMeansPlusPlusClusterer<ProbeRequestBriefWrapper>(k, iterationNum);
		MultiKMeansPlusPlusClusterer<ProbeRequestBriefWrapper> Multiclusterer = new MultiKMeansPlusPlusClusterer<ProbeRequestBriefWrapper>(clusterer, numTrials);
		List<CentroidCluster<ProbeRequestBriefWrapper>> clusterResults = Multiclusterer.cluster(clusterInput);
		
		// output the clusters
		for (int i=0; i<clusterResults.size(); i++) {
//		    System.out.println("Cluster " + i);
		    double[] center = clusterResults.get(i).getCenter().getPoint();
		    
		    ArrayList<Double> index = new ArrayList<Double>();
		    for(int j=0;j<config.apNum;j++){
		    	index.add(center[j]);
		    }

//		    Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor,index,mScale);
		    UpdatekmeanText(center, i, clusterResults);
		}

		centerPanel.setIsIsKmeans(true);
		centerPanel.clusterResultsForKmeans = clusterResults;
		centerPanel.revalidate();
		centerPanel.repaint();
	}

}
