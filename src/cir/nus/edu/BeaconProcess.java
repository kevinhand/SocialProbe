package cir.nus.edu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import cir.nus.edu.ProbeRequestStructure.FingerPrint;
import cir.nus.edu.ProbeRequestStructure.FingerPrintWrapper;
import cir.nus.edu.ProbeRequestStructure.ProbeRequest;
import cir.nus.edu.ProbeRequestStructure.ProbeRequestBrief;
import cir.nus.edu.ProbeRequestStructure.ProbeRequestBriefWrapper;
import cir.nus.util.FunctionUtility;
import cir.nus.util.IOUtility;

public class BeaconProcess {

	// private static ArrayList<ProbeRequest> mProbeRequestList = new
	// ArrayList<ProbeRequest>();
	private static HashMap<String, ArrayList<ArrayList<ProbeRequest>>> mMacToProbeData = new HashMap<String, ArrayList<ArrayList<ProbeRequest>>>();

	// static String path = "/home/honghande/ProbeBeacon/PiLab/FigurePlot/";
	// static String path =
	// "/home/honghande/ProbeBeacon/BizCanteenData/TimeTablePlot/";
	// static String path ="/home/honghande/ProbeBeacon/labdata/TimeTablePlot/";
	static HashMap<Integer, Vector<Double>> signalDB = new HashMap<Integer, Vector<Double>>();
	static HashMap<String, String> ouiDB = new HashMap<String, String>();
	static ArrayList<ProbeRequestBrief> ListOfProbeVector = null;
	static HashMap<String, ArrayList<ProbeRequestBrief>> MacToProbeVector = new HashMap<String, ArrayList<ProbeRequestBrief>>();
	static HashMap<String, Color> MacToColor = new HashMap<String, Color>();
	static HashMap<String, ArrayList<Double>> MacToProfile = new HashMap<String, ArrayList<Double>>();
	static HashMap<String, Integer> MacTomostFrequencyGap = new HashMap<String, Integer>();
	static Graphics FloorMapWindow;
	static ArrayList<String> CheckedMac = new ArrayList<String>();
	static ArrayList<String> LongStayList = new ArrayList<String>();
	static HashMap<String, ArrayList<ProbeRequestBrief>> mMacToDiningTrace = new HashMap<String, ArrayList<ProbeRequestBrief>>();
	static DiningPatternDetect DPD;
	static ArrayList<Calendar> startTimeList = new ArrayList<Calendar>();
	static ArrayList<Coord> CoordList = new ArrayList<Coord>();

	private static ProbeRequestCluster mProbeRequestCluster = null;
	private static boolean IsShowCountingResult = true;
	private static boolean IsShowRegularity = true;

	private static JTextArea textArea;
	private static Box box;
	private static JTabbedPane leftPanel;
	private static DrawingPanel centerPanel;
	private static JTextArea eps = new JTextArea(1, 10);
	private static JTextArea MinPts = new JTextArea(1, 10);
	private static JTextArea kmean = new JTextArea(1, 10);
	private static JTextArea IterNum = new JTextArea(1, 10);
	private static JTextArea numTrials = new JTextArea(1, 10);

	private static JTextArea syear = new JTextArea(1, 4);
	private static JTextArea smonth = new JTextArea(1, 2);
	private static JTextArea sday = new JTextArea(1, 2);
	private static JTextArea shour = new JTextArea(1, 2);
	private static JTextArea smin = new JTextArea(1, 2);
	private static JTextArea ssecond = new JTextArea(1, 2);

	private static JTextArea eyear = new JTextArea(1, 4);
	private static JTextArea emonth = new JTextArea(1, 2);
	private static JTextArea eday = new JTextArea(1, 2);
	private static JTextArea ehour = new JTextArea(1, 2);
	private static JTextArea emin = new JTextArea(1, 2);
	private static JTextArea esecond = new JTextArea(1, 2);

	private static JTextArea Scale = new JTextArea(1, 3);
	private static JTextArea Gap = new JTextArea(1, 3);

	private static JButton TimeBtn;
	private static JButton CrowdBtn;
	private static JButton TimeTableBtn;
	private static JButton SignalBtn;
	private static JButton ErrorBarBtn;
	private static JButton ClearBtn;
	private static JButton KMeansPlusPlusClusterBtn;
	private static JButton MultiKMeansClusterBtn;
	private static JButton DBSCANClusterBtn;
	private static JButton ShowColocationBtn;
	private static JButton ShowLocalizationError;
	private static JButton ShowSimilarityTrend;
	public static Configuration config;

	public static void main(String[] args) {

		config = new Configuration();
		ListOfProbeVector = new ArrayList<ProbeRequestBrief>();
		FloorMapWindow = createUI(config.size, config.size, "Passive Scanning");
		initialParameter();
		addListener();
		Calendar start = Calendar.getInstance();
		start.clear();
		start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
				Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()), Integer.parseInt(smin.getText()),
				Integer.parseInt(ssecond.getText()));

		Calendar end = Calendar.getInstance();
		end.clear();
		end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()), Integer.parseInt(eday.getText()),
				Integer.parseInt(ehour.getText()), Integer.parseInt(emin.getText()),
				Integer.parseInt(esecond.getText()));
		// BuildDatabase();
		// signalDB = IOUtility.ReadDBFromFile("WiFiDB.txt");
		ouiDB =IOUtility.ReadOuiFromFile("/home/honghande/Research/SocialProbe/ProbeBeacon/smart_device.txt");

		SetTimeSequence();

		// Crowd counting
		// MergeProbeRequestList(mProbeRequestList);
		// PrintGap();
		// ProcessPersonalActivity();

		// visualization of probe beacon
		mMacToProbeData = IOUtility.ReadFromNewFormatFile(config.src, start, end, config.apNum, config.HomeDir);
		// mMacToProbeData = IOUtility.ReadFromNewFormatFile("DataBase", ouiDB);
		// System.out.println(mMacToProbeData.size());// +" "+ouiDB.size());
		// mMTPD = IOUtility.ReadFromFile(ouiDB );

		filterProbeData();
		// buildProfilePerMac();
		ProcessVectorFromMap();
		ProcessCheckBox();
		

		
//		DPD = new DiningPatternDetect(MacToProbeVector,	centerPanel.bgimage,centerPanel.Anchor, config.apNum, config.scale);
//		centerPanel.mMacToDiningTrace = DPD.generatecandidateDiningTrace();//get trace that longer than 10 min and start from order area
//		DPD.ConvergeDiningTrace();
//		mMacToDiningTrace = DPD.ConvergeDiningTrace();
//		mMacToDiningTrace = centerPanel.mMacToDiningTrace;
//		DPD.ConvergeDiningTrace();
		
//		System.out.println(mMacToProbeData.size()+" "+MacToProbeVector.size());
	}

	public static void SetTimeSequence() {

		Calendar start = Calendar.getInstance();
		start.set(2015, 11, 2, 19, 56, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 20, 3, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 20, 17, 0);
		startTimeList.add(start);
		start.set(2015, 11, 2, 20, 24, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 20, 31, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 20, 40, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 20, 48, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 20, 55, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 21, 2, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 21, 10, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 21, 18, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 21, 27, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 21, 34, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 21, 42, 0);
		startTimeList.add(start);

		start = Calendar.getInstance();
		start.set(2015, 11, 2, 21, 50, 0);
		startTimeList.add(start);

		CoordList.add(new Coord(110, 612));
		CoordList.add(new Coord(60, 533));
		CoordList.add(new Coord(60, 457));
		CoordList.add(new Coord(159, 375));
		CoordList.add(new Coord(280, 378));
		CoordList.add(new Coord(271, 534));
		CoordList.add(new Coord(266, 619));
		CoordList.add(new Coord(279, 307));
		CoordList.add(new Coord(351, 197));
		CoordList.add(new Coord(444, 368));
		CoordList.add(new Coord(443, 577));
		CoordList.add(new Coord(448, 526));
		CoordList.add(new Coord(228, 146));
		CoordList.add(new Coord(245, 46));
		CoordList.add(new Coord(448, 147));

		// for(int i=0;i<startTimeList.size();i++)
		// System.out.println(FunctionUtility.CalendarToString(startTimeList.get(i)));
	}

	// build UI
	public static Graphics createUI(int width, int height, String title) {
		JFrame f = new JFrame("Cluster result");
		f.setSize(width + 300, height);
		f.setVisible(true);
		f.setTitle(title);
		f.setResizable(false);// private static int[] activityTable = new
								// int[24*6];

		JPanel rightPanel = new JPanel();
		f.getContentPane().add(rightPanel, "East");

		leftPanel = new JTabbedPane();
		leftPanel.setPreferredSize(new Dimension(220, 900));
		leftPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		Border border = BorderFactory.createTitledBorder("Filter Condition");
		leftPanel.setBorder(border);
		f.getContentPane().add(leftPanel, "West");

		centerPanel = new DrawingPanel();
		// centerPanel.setBackground(Color.gray);
		centerPanel.setPreferredSize(new Dimension(1000, 10000));
		JScrollPane centerscroll = new JScrollPane(centerPanel);
		centerscroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		centerscroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		f.getContentPane().add(centerscroll, "Center");

		// TEXT AREA
		textArea = new JTextArea(59, 20);
		textArea.setOpaque(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setVisible(true);

		box = Box.createVerticalBox();

		JScrollPane scroll = new JScrollPane(textArea);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		rightPanel.add(scroll);

		JScrollPane jscrlpBox = new JScrollPane(box);
		jscrlpBox.setPreferredSize(new Dimension(200, 700));
		leftPanel.addTab("Mac List", jscrlpBox);

		JPanel clusterPanel = new JPanel(false);
		// clusterPanel.setLayout(new GridLayout(40,4));
		leftPanel.addTab("Clustering", clusterPanel);

		addClusterPanel(clusterPanel, eps, MinPts, "eps", "MinPts", "15", "1");

		DBSCANClusterBtn = new JButton("DBSCAN Cluster");
		clusterPanel.add(DBSCANClusterBtn);

		addClusterPanel(clusterPanel, kmean, IterNum, "k", "IterNum", "10", "100");

		KMeansPlusPlusClusterBtn = new JButton("KMeansPlusPlus");
		clusterPanel.add(KMeansPlusPlusClusterBtn);

		JTextArea numTrialshint = new JTextArea(1, 5);
		numTrialshint.setOpaque(false);
		numTrialshint.setVisible(true);
		numTrialshint.setEditable(false);
		numTrialshint.setText("numTrials:");

		numTrials.setOpaque(true);
		numTrials.setVisible(true);
		numTrials.setEditable(true);

		clusterPanel.add(numTrialshint);
		clusterPanel.add(numTrials);

		MultiKMeansClusterBtn = new JButton("Multi-KMeansCluster");
		clusterPanel.add(MultiKMeansClusterBtn);

		clusterPanel.add(syear);
		clusterPanel.add(smonth);
		clusterPanel.add(sday);
		clusterPanel.add(shour);
		clusterPanel.add(smin);
		clusterPanel.add(ssecond);

		clusterPanel.add(eyear);
		clusterPanel.add(emonth);
		clusterPanel.add(eday);
		clusterPanel.add(ehour);
		clusterPanel.add(emin);
		clusterPanel.add(esecond);

		JTextArea scalehint = new JTextArea(1, 5);
		scalehint.setOpaque(false);
		scalehint.setVisible(true);
		scalehint.setEditable(false);
		scalehint.setText("Scale and Gap:");

		Scale.setOpaque(true);
		Scale.setVisible(true);
		Scale.setEditable(true);

		clusterPanel.add(scalehint);
		clusterPanel.add(Scale);

		Gap.setOpaque(true);
		Gap.setVisible(true);
		Gap.setEditable(true);

		clusterPanel.add(Gap);

		TimeBtn = new JButton("GetProbeRequestByTime");
		clusterPanel.add(TimeBtn);
		CrowdBtn = new JButton("GetCrowndChangeByTime");
		clusterPanel.add(CrowdBtn);
		TimeTableBtn = new JButton("Get Time Table Graph");
		clusterPanel.add(TimeTableBtn);
		SignalBtn = new JButton("Get RSSI Trend Graph");
		clusterPanel.add(SignalBtn);
		ErrorBarBtn = new JButton("Get Error Bar Graph");
		clusterPanel.add(ErrorBarBtn);
		ShowColocationBtn = new JButton("Show Colocation");
		clusterPanel.add(ShowColocationBtn);
		ShowLocalizationError = new JButton("Show localization error");
		clusterPanel.add(ShowLocalizationError);
		ShowSimilarityTrend = new JButton("Show Similarity Trend");
		clusterPanel.add(ShowSimilarityTrend);
		ClearBtn = new JButton("Clear");
		clusterPanel.add(ClearBtn);

		Graphics g = centerPanel.getGraphics();
		centerPanel.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// f.pack();
		FunctionUtility.showOnScreen(1, f);

		return g;
	}

	public static void initialParameter() {
		syear.setText("2015");
		smonth.setText("10");
		sday.setText("25");
		shour.setText("0");
		smin.setText("1");
		ssecond.setText("0");

		eyear.setText("2015");
		emonth.setText("11");
		eday.setText("2");
		ehour.setText("23");
		emin.setText("0");
		esecond.setText("0");

		numTrials.setText("10");

		Scale.setText("15");
		Gap.setText("600");
	}

	// add listener for all the button
	public static void addListener() {
		DBSCANClusterBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				textArea.setText("");
				centerPanel.setIsDrawNode(false);
				centerPanel.setIsLoadImage(false);
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				int mscale = Integer.parseInt(Scale.getText());
				if (CheckedMac.size() == 0) {
					mProbeRequestCluster = new ProbeRequestCluster(textArea, ListOfProbeVector, FloorMapWindow);
				} else {
					ArrayList<ProbeRequestBrief> ListFiltered = new ArrayList<ProbeRequestBrief>();
					for (int i = 0; i < CheckedMac.size(); i++) {
						ListFiltered.addAll(MacToProbeVector.get(CheckedMac.get(i)));
					}
					mProbeRequestCluster = new ProbeRequestCluster(textArea, ListFiltered, FloorMapWindow);
				}
				mProbeRequestCluster.DBSCANClusterProbeRequest(start, end, mscale, Double.parseDouble(eps.getText()),
						Integer.parseInt(MinPts.getText()), centerPanel);
			}
		});

		KMeansPlusPlusClusterBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				textArea.setText("");
				centerPanel.setIsDrawNode(false);
				centerPanel.setIsLoadImage(false);
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				int mscale = Integer.parseInt(Scale.getText());
				if (CheckedMac.size() == 0) {
					mProbeRequestCluster = new ProbeRequestCluster(textArea, ListOfProbeVector, FloorMapWindow);
				} else {
					ArrayList<ProbeRequestBrief> ListFiltered = new ArrayList<ProbeRequestBrief>();
					for (int i = 0; i < CheckedMac.size(); i++) {
						ListFiltered.addAll(MacToProbeVector.get(CheckedMac.get(i)));
					}
					mProbeRequestCluster = new ProbeRequestCluster(textArea, ListFiltered, FloorMapWindow);
				}
				mProbeRequestCluster.KMeansPlusPlusClusterProbeRequest(start, end, mscale,
						Integer.parseInt(kmean.getText()), Integer.parseInt(IterNum.getText()), centerPanel);

			}
		});

		MultiKMeansClusterBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				textArea.setText("");
				centerPanel.setIsDrawNode(false);
				centerPanel.setIsLoadImage(false);
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				int mscale = Integer.parseInt(Scale.getText());
				if (CheckedMac.size() == 0) {
					mProbeRequestCluster = new ProbeRequestCluster(textArea, ListOfProbeVector, FloorMapWindow);
				} else {
					ArrayList<ProbeRequestBrief> ListFiltered = new ArrayList<ProbeRequestBrief>();
					for (int i = 0; i < CheckedMac.size(); i++) {
						ListFiltered.addAll(MacToProbeVector.get(CheckedMac.get(i)));
					}
					mProbeRequestCluster = new ProbeRequestCluster(textArea, ListFiltered, FloorMapWindow);
				}
				mProbeRequestCluster.MultiKMeansPlusPlusClusterProbeRequest(start, end, mscale,
						Integer.parseInt(kmean.getText()), Integer.parseInt(IterNum.getText()),
						Integer.parseInt(numTrials.getText()), centerPanel);
			}
		});

		ShowLocalizationError.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				int mscale = Integer.parseInt(Scale.getText());
				centerPanel.setIsDrawNode(true);
				centerPanel.setIsLoadImage(false);
				GetLocalizationError(MacToProbeVector);
			}

		});

		ShowColocationBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				int mscale = Integer.parseInt(Scale.getText());
				centerPanel.setIsDrawNode(true);
				centerPanel.setIsLoadImage(false);
				GetColocation(start, end, MacToProbeVector);
			}

		});

		TimeBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				int mscale = Integer.parseInt(Scale.getText());
				centerPanel.setIsDrawNode(true);
				centerPanel.setIsLoadImage(false);
				GetProbeRequestByTime(start, end, mscale, MacToProbeVector);
			}
		});

		CrowdBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				centerPanel.setIsDrawNode(true);
				centerPanel.setIsLoadImage(false);
				int mscale = Integer.parseInt(Scale.getText());
				GetCrowdChangeByTime(start, end, mscale, MacToProbeVector);
			}
		});

		TimeTableBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				centerPanel.setIsDrawNode(false);
				centerPanel.setIsLoadImage(true);
				config.Gap = Integer.parseInt(Gap.getText());
				GenerateTimeTable(start, end);
			}
		});

		SignalBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				centerPanel.setIsDrawNode(false);
				centerPanel.setIsLoadImage(true);
				GenerateSignalGraph(start, end);
			}
		});

		ErrorBarBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				centerPanel.setIsDrawNode(false);
				centerPanel.setIsLoadImage(true);
				GenerateErrorBarGraph(start, end);
			}
		});

		ClearBtn.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				ClearGraphic();
				ClearCheckBox();
				CheckedMac.clear();
				centerPanel.UpdateMap.clear();
				LongStayList.clear();
			}
		});

		ShowSimilarityTrend.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Execute when button is pressed
				Calendar start = Calendar.getInstance();
				start.clear();
				start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
						Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
						Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

				Calendar end = Calendar.getInstance();
				end.clear();
				end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
						Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
						Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
				centerPanel.setIsDrawNode(true);
				centerPanel.setIsLoadImage(false);
				GetSimilarityTrend(start, end, MacToProbeVector);
			}
		});
		
	}

	public static void addClusterPanel(JPanel clusterPanel, JTextArea p1, JTextArea p2, String h1, String h2, String d1,
			String d2) {
		JTextArea hint1 = new JTextArea(1, 5);
		hint1.setOpaque(false);
		hint1.setVisible(true);
		hint1.setEditable(false);
		hint1.setText(h1 + ": ");

		p1.setOpaque(true);
		p1.setVisible(true);
		p1.setEditable(true);
		p1.setText(d1);

		JTextArea hint2 = new JTextArea(1, 5);
		hint2.setOpaque(false);
		hint2.setVisible(true);
		hint2.setEditable(false);
		hint2.setText(h2 + ":");

		p2.setOpaque(true);
		p2.setVisible(true);
		p2.setEditable(true);
		p2.setText(d2);

		clusterPanel.add(hint1);
		clusterPanel.add(p1);
		clusterPanel.add(hint2);
		clusterPanel.add(p2);
	}

	static ArrayList<StayPeriod> ProbeRequestListToStayPeriod(Calendar s, Calendar e, ArrayList<ProbeRequest> PRL) {
		ArrayList<StayPeriod> msp = new ArrayList<StayPeriod>();

		for (int i = 0; i < PRL.size(); i++) {
			if (PRL.get(i).getStartTime().after(s) && PRL.get(i).getEndTime().before(e)) {
				StayPeriod temp = new StayPeriod(PRL.get(i).getStartTime(), PRL.get(i).getEndTime());
				temp.getRssiVec().add(PRL.get(i).getAveragerssiWithoutOutliar());
				if (msp.size() == 0) {
					msp.add(temp);
				} else if (FunctionUtility.GetGap(msp.get(msp.size() - 1), temp) <= config.Gap) {
					if (Math.abs(msp.get(msp.size() - 1).getAveragerssiWithoutOutliar()
							- (PRL.get(i).getAveragerssiWithoutOutliar())) < 10) {// rssi
																					// fluctuate
																					// within
																					// 10db
						msp.get(msp.size() - 1).setEndTime(temp.getEndTime());
						msp.get(msp.size() - 1).getRssiVec().add(PRL.get(i).getAveragerssiWithoutOutliar());
					} else
						msp.add(temp);
				} else {
					msp.add(temp);
				}
			}
		}

		// filter out those short instance
		int id = 0;

		id = 0;
		while (id < msp.size() - 1) {
			if (FunctionUtility.GetTimediffInSecond(msp.get(id).getEndTime(),
					msp.get(id + 1).getStartTime()) < config.Gap) {
				msp.get(id).setEndTime(msp.get(id + 1).getEndTime());
				msp.get(id).getRssiVec().addAll(msp.get(id + 1).getRssiVec());
				msp.remove(id + 1);
			} else
				id++;
		}

		return msp;
	}

	static ArrayList<StayPeriod> MergeStayPeriod(ArrayList<ArrayList<StayPeriod>> stayPeriodForAllAP) {
		// for case n=3
		ArrayList<StayPeriod> tmp = MergeTwoStayPeriodList(stayPeriodForAllAP.get(0), stayPeriodForAllAP.get(1));
		ArrayList<StayPeriod> mergedStayPeriod = MergeTwoStayPeriodList(tmp, stayPeriodForAllAP.get(2));
		// System.out.println("mergedStayPeriod"+":"+mergedStayPeriod.size());

		// for(int i=0;i<mergedStayPeriod.size();i++){
		// System.out.println(FunctionUtility.CalendarToString(mergedStayPeriod.get(i).getStartTime())+"
		// "+
		// FunctionUtility.CalendarToString(mergedStayPeriod.get(i).getEndTime()));
		// }
		// System.out.println();
		return mergedStayPeriod;
	}

	static ArrayList<StayPeriod> MergeTwoStayPeriodList(ArrayList<StayPeriod> a, ArrayList<StayPeriod> b) {
		// initialize
		if (a.size() != 0 && b.size() != 0) {
			ArrayList<ArrayList<StayPeriod>> list = new ArrayList<ArrayList<StayPeriod>>();
			list.add(a);
			list.add(b);
			int index;
			ArrayList<StayPeriod> result = new ArrayList<StayPeriod>();
			while (list.get(0).size() > 0 && list.get(1).size() > 0) {
				index = FunctionUtility.GetEarliestTimeIndex(list.get(0).get(0), list.get(1).get(0));
				if (result.size() == 0)
					result.add(list.get(index).get(0));
				else {
					if (result.get(result.size() - 1).getEndTime().before(list.get(index).get(0).getStartTime())) {
						if (FunctionUtility.GetGap(result.get(result.size() - 1), list.get(index).get(0)) > 600)
							result.add(list.get(index).get(0));
						else
							result.get(result.size() - 1).setEndTime(list.get(index).get(0).getEndTime());
					} else if (result.get(result.size() - 1).getEndTime().before(list.get(index).get(0).getEndTime())) {
						result.get(result.size() - 1).setEndTime(list.get(index).get(0).getEndTime());
					} else
						;

				}
				list.get(index).remove(0);
			}

			if (list.get(0).size() == 0)
				index = 1;
			else
				index = 0;

			while (list.get(index).size() > 0) {
				if (result.get(result.size() - 1).getEndTime().before(list.get(index).get(0).getStartTime())) {
					result.add(list.get(index).get(0));
				} else if (result.get(result.size() - 1).getEndTime().before(list.get(index).get(0).getEndTime())) {
					result.get(result.size() - 1).setEndTime(list.get(index).get(0).getEndTime());
				} else
					;
				list.get(index).remove(0);
			}

			return result;
		} else {
			if (a.size() == 0)
				return b;
			else
				return a;
		}

	}

	// process personal time table
	static ArrayList<Integer> ProcessPerApPersonalActivity(Calendar s, Calendar e, ArrayList<StayPeriod> sp, int gap) {
		Calendar cal1 = (Calendar) s.clone();
		Calendar cal2 = (Calendar) s.clone();
		cal2.add(Calendar.MINUTE, gap);
		ArrayList<Integer> activityTable = new ArrayList<Integer>();

		boolean IsSet = false;
		while (cal1.before(e)) {
			IsSet = false;
			StayPeriod s1 = new StayPeriod(cal1, cal2);
			for (int i = 0; i < sp.size(); i++) {
				if (sp.get(i).IsInThisPeriod(s1)) {
					activityTable.add(1);
					IsSet = true;
					break;
				}
			}
			if (!IsSet)
				activityTable.add(0);
			cal1.add(Calendar.MINUTE, gap);
			cal2.add(Calendar.MINUTE, gap);
		}

		return activityTable;
	}

	// process personal time table
	static ArrayList<Integer> ProcessPersonalActivity(Calendar s, Calendar e,
			HashMap<Integer, ArrayList<StayPeriod>> sp, int gap) {

		ArrayList<Integer> activityTable = new ArrayList<Integer>();
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int APIndex : sp.keySet()) {
			if (activityTable.size() == 0)
				activityTable = ProcessPerApPersonalActivity(s, e, sp.get(APIndex), gap);
			else {
				temp = ProcessPerApPersonalActivity(s, e, sp.get(APIndex), gap);
				for (int i = 0; i < activityTable.size(); i++) {
					if (activityTable.get(i) == 1 || temp.get(i) == 1)
						activityTable.set(i, 1);
				}
			}

		}

		return activityTable;
	}

	static void GetColocation(Calendar start, Calendar end,
			HashMap<String, ArrayList<ProbeRequestBrief>> macToProbeVector) {
		if (CheckedMac.size() > 1) {
			HashMap<String, ArrayList<ProbeRequestBrief>> FilterMacToProbeVector = new HashMap<String, ArrayList<ProbeRequestBrief>>();
			for (int i = 0; i < CheckedMac.size(); i++) {
				ArrayList<ProbeRequestBrief> tmp = new ArrayList<ProbeRequestBrief>();
				for (int j = 0; j < macToProbeVector.get(CheckedMac.get(i)).size(); j++) {
					if (macToProbeVector.get(CheckedMac.get(i)).get(j).getStartTime().after(start)
							&& (macToProbeVector.get(CheckedMac.get(i)).get(j).getEndTime().before(end))) {
						tmp.add(macToProbeVector.get(CheckedMac.get(i)).get(j));
					}
				}
				FilterMacToProbeVector.put(CheckedMac.get(i), tmp);
			}

			for (int i = 0; i < CheckedMac.size(); i++) {
				for (int j = i + 1; j < CheckedMac.size(); j++) {
					double time = GetColocationForTwoPeople(FilterMacToProbeVector.get(CheckedMac.get(i)),
							FilterMacToProbeVector.get(CheckedMac.get(j)));
					// if (time > 10)
					System.out.println(CheckedMac.get(i) + " " + CheckedMac.get(j) + " " + time + "min");
				}
			}
		} else if (CheckedMac.size() == 1) {
			String master = CheckedMac.get(0);
			//filter data based on time stamp
			HashMap<String, ArrayList<ProbeRequestBrief>> FilterMacToProbeVector = new HashMap<String, ArrayList<ProbeRequestBrief>>();
			for (String slave : macToProbeVector.keySet()) {
				ArrayList<ProbeRequestBrief> tmp = new ArrayList<ProbeRequestBrief>();
				for (int j = 0; j < macToProbeVector.get(slave).size(); j++) {
					if (macToProbeVector.get(slave).get(j).getStartTime().after(start)
							&& (macToProbeVector.get(slave).get(j).getEndTime().before(end))) {
						tmp.add(macToProbeVector.get(slave).get(j));
					}
				}
				FilterMacToProbeVector.put(slave, tmp);
			}
			//get co-location time
			for (String slave : FilterMacToProbeVector.keySet()) {
								
				if(FilterMacToProbeVector.get(slave).size()>0)
				GetColocationForTwoPeople(FilterMacToProbeVector.get(master),
						FilterMacToProbeVector.get(slave));
			}
		}

	}
	
	private static double GetColocationForTwoPeople(ArrayList<ProbeRequestBrief> a, ArrayList<ProbeRequestBrief> b) {
		// TODO Auto-generated method stub
		a = PerIntraMergeVector(a);
		b = PerIntraMergeVector(b);

		ArrayList<StayPeriod> coSession = new ArrayList<StayPeriod>();
		ArrayList<Double> SimilaritySession = new ArrayList<Double>();
		int i = 0;
		int j = 0;
		boolean isFound = false;
		while (i < a.size()) {
			isFound = false;
			StayPeriod sp = null;
			j = 0;
			while (j < b.size()) {
				if (FunctionUtility.GetOverlapInSecond(a.get(i), b.get(j)) > 0) {
					isFound = true;
					double sim = getSimilarityOfFingerprint(a.get(i), b.get(j));
//					if (sim > 0.8) {
						Calendar s, e;
						if (a.get(i).getStartTime().before(b.get(j).getStartTime())) {
							s = b.get(j).getStartTime();
						} else {
							s = a.get(i).getStartTime();
						}

						if (a.get(i).getEndTime().before(b.get(j).getEndTime())) {
							e = a.get(i).getEndTime();
						} else {
							e = b.get(j).getEndTime();
						}
						// if(sp==null)
						sp = new StayPeriod(s, e);
						coSession.add(sp);
						SimilaritySession.add(sim);
//					}
					j++;
				} else {
					if (isFound) {
						break;
					} else
						j++;
				}

			}

			i++;
		}

		double TotalTime = 0;
		double S_Dining = 0;

		
		for (i = 0; i < coSession.size(); i++) {

			double TimeInmin = (coSession.get(i).getEndTime().getTimeInMillis()
					- coSession.get(i).getStartTime().getTimeInMillis()) / 1000.0 / 60;
			
			S_Dining += SimilaritySession.get(i)*TimeInmin;
			TotalTime += TimeInmin;
		}
		double ratio =0;
		if((int)TotalTime>0){
			ratio = S_Dining/TotalTime;
		System.out.println(a.get(0).getSourceMAC() + "\t" + b.get(0).getSourceMAC() + "\t" + (int)TotalTime+"\t"+(int)S_Dining+"\t"+ratio );}
		return TotalTime;

	}


	static ArrayList<ProbeRequestBrief> PerIntraMergeVector(ArrayList<ProbeRequestBrief> seq) {
		int i = 0;
		int size = 1;
		while (i < seq.size() - 1) {
			if (FunctionUtility.CanMerge(seq.get(i), seq.get(i + 1))
					&& getSimilarityOfFingerprint(seq.get(i), seq.get(i + 1)) > 0.4) {
				seq.get(i).setEndTime(seq.get(i + 1).getEndTime());
				for (int j = 0; j < config.apNum; j++)
					seq.get(i).getRssiVec()[j] = (seq.get(i).getRssiVec()[j] * size + seq.get(i + 1).getRssiVec()[j])
							/ (size + 1);
				size++;
				seq.remove(i + 1);
			} else {
				// System.out.println(getSimilarityOfFingerprint(seq.get(i),
				// seq.get(i + 1)));
				i++;
				size = 1;
			}
		}
		return seq;
	}

	static void GenerateTimeTable(Calendar s, Calendar e) {
		HashMap<String, HashMap<Integer, ArrayList<StayPeriod>>> MacToStayPeriod = new HashMap<String, HashMap<Integer, ArrayList<StayPeriod>>>();
		HashMap<String, Integer> MacToStayTimeLength = new HashMap<String, Integer>();
		LongStayList = new ArrayList<String>();
		if (CheckedMac.size() > 0) { // show time table for device
			for (int i = 0; i < CheckedMac.size(); i++) {
				ArrayList<Integer> activityTable = new ArrayList<Integer>();
				String mac = CheckedMac.get(i);
				ArrayList<ArrayList<ProbeRequest>> ProbeData = mMacToProbeData.get(mac);
				HashMap<Integer, ArrayList<StayPeriod>> stayPeriodForAllAP = new HashMap<Integer, ArrayList<StayPeriod>>();

				for (int j = 0; j < config.apNum; j++) {
					stayPeriodForAllAP.put(j, ProbeRequestListToStayPeriod(s, e, ProbeData.get(j)));
				}
				MacToStayPeriod.put(mac, stayPeriodForAllAP);

				// MacToStayPeriod hash map ready
				activityTable = ProcessPersonalActivity(s, e, MacToStayPeriod.get(mac), 60);

				if (IsShowRegularity) {
					int SizeofDay = 24 * 60 / 60;
					double regularity = 0.0;
					int numOfDay = activityTable.size() / SizeofDay;
					if (activityTable.size() % SizeofDay == 0) {
						for (int m = 0; m < SizeofDay; m++) {
							for (int d1 = 0; d1 < numOfDay; d1++) {
								for (int d2 = d1 + 1; d2 < numOfDay; d2++) {
									if (activityTable.get(SizeofDay * d1 + m)
											+ activityTable.get(SizeofDay * d2 + m) != 1)
										regularity++;
									else if (activityTable.get(SizeofDay * d1 + m)
											+ activityTable.get(SizeofDay * d2 + m) == 1)
										regularity--;
								}
							}

						}
						regularity /= (SizeofDay * numOfDay * (numOfDay - 1) / 2);
						System.out.println(CheckedMac.get(i) + " " + regularity + " " + numOfDay);
					}
				}

				IOUtility.WriteTimeTable(activityTable, s, mac, 60, config.path);
			}
			try {
				IOUtility.GeneratTimePlot(CheckedMac, centerPanel, config.path);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			// sort the map
			Map<String, ArrayList<ProbeRequestBrief>> sortedmap = new TreeMap<String, ArrayList<ProbeRequestBrief>>(
					MacToProbeVector);
			System.out.println(sortedmap.size());
			for (String m : sortedmap.keySet()) {
				MacToStayTimeLength.put(m, CalculateStayPeriod(MacToProbeVector.get(m), s, e));
			}
			// calculate stay time
			for (String m : sortedmap.keySet()) {
//				System.out.println(MacToStayTimeLength.get(m) / (double) FunctionUtility.GetTimediffInSecond(s, e));
				double Stayrate = MacToStayTimeLength.get(m) / (double) FunctionUtility.GetTimediffInSecond(s, e);
				if (Stayrate < 0.6 && Stayrate > 0.02) {
					System.out.println(m + "\t"
							+ Math.round(Stayrate* 100) / 100.0
							+ "\t" + MacToStayTimeLength.get(m) / 60 + "min\t" + MacToStayTimeLength.get(m) / 3600
							+ "hour\t" + MacToProbeVector.get(m).size() + "\t"
							+ ouiDB.get(m.substring(0, 8).toUpperCase()));

					LongStayList.add(m);
					CheckedMac.add(m);
				}
			}

			System.out.println(LongStayList.size());
			// remove those periodically send data 1 for each three hour, show
			// some frequency patterns
			int index = 0;
			while (index < LongStayList.size()) {
				ArrayList<Integer> activityTable = new ArrayList<Integer>();
				HashMap<Integer, ArrayList<StayPeriod>> stayPeriodForAllAP = new HashMap<Integer, ArrayList<StayPeriod>>();

				for (int j = 0; j < config.apNum; j++) {// should not assume all
														// AP can
					// capture
					stayPeriodForAllAP.put(j,
							ProbeRequestListToStayPeriod(s, e, mMacToProbeData.get(LongStayList.get(index)).get(j)));
				}
				MacToStayPeriod.put(LongStayList.get(index), stayPeriodForAllAP);
				activityTable = ProcessPersonalActivity(s, e, MacToStayPeriod.get(LongStayList.get(index)), 120);
				double counter = 0;
				ArrayList<Integer> ari = new ArrayList<Integer>();
				for (int i = 0; i < activityTable.size(); i++) {
					counter += activityTable.get(i);
					if (activityTable.get(i) == 1)
						ari.add(i);
				}
				System.out.println(LongStayList.get(index) + " percentage " + counter / activityTable.size());

				if (counter / activityTable.size() >= 0.8) { // threshold 0.8
					LongStayList.remove(index);
					CheckedMac.remove(index);
				} else
					index++;

			}
			System.out.println(LongStayList.size());
			
			
			// cluster relationship based on their fingerprint
			ArrayList<FingerPrintWrapper> FPList = new ArrayList<FingerPrintWrapper>();
			for (int i = 0; i < LongStayList.size(); i++) {
				// return the representative fingerprint
				double[] fingerprint = GetLongStayFingerprintByKMeansPlusPlusCluster(s, e, 3, 100,
						MacToProbeVector.get(LongStayList.get(i)));
				FingerPrintWrapper fp = new FingerPrintWrapper(new FingerPrint(LongStayList.get(i), fingerprint, config.apNum+1),config.apNum+1);
				FPList.add(fp);
			}
			System.out.println();
			for (int i = 0; i < FPList.size(); i++) {
				// show fingerprint similarity of different device
				System.out.print(LongStayList.get(i));
				for (int j = 0; j < FPList.size(); j++) {
					System.out.print("\t" + Math.round(
							100 * getSimilarityOfFingerprint(FPList.get(i).getPoint(), FPList.get(j).getPoint()))
							/ 100.0);
				}
//				System.out.print("\t" + ArrayToString(FPList.get(i).getPoint()));
				System.out.println();
			}

			 DBSCANClusterFingerPrint(Double.parseDouble(eps.getText()), Integer.parseInt(MinPts.getText()), FPList);
			
			 for (int i = 0; i < LongStayList.size(); i++) {
				 for (int j = i + 1; j < LongStayList.size(); j++) {
					 double TimeLength = CalculateTogertherTime(MacToProbeVector.get(LongStayList.get(i)),
							 	MacToProbeVector.get(LongStayList.get(j)));
					 System.out.println(MacToProbeVector.get(LongStayList.get(i)).get(0).getSourceMAC()+ 
							 "\t"+MacToProbeVector.get(LongStayList.get(j)).get(0).getSourceMAC()+
							 "\t"+TimeLength/60.0);
				 }
			 }

		}
	}

	static double GetFingerPrintDistance(FingerPrint a, FingerPrint b) {
		double distance = 0;
		for (int i = 0; i < a.getRssiVec().length; i++) {
			distance += (a.getRssiVec()[i] - b.getRssiVec()[i]) * (a.getRssiVec()[i] - b.getRssiVec()[i]);
		}
		return Math.sqrt(distance / a.getRssiVec().length);
	}

	static double[] GetLongStayFingerprintByKMeansPlusPlusCluster(Calendar s, Calendar e, int k, int iterationNum,
			ArrayList<ProbeRequestBrief> ProbeVector) {

		List<ProbeRequestBriefWrapper> clusterInput = new ArrayList<ProbeRequestBriefWrapper>(ProbeVector.size());
		// System.out.println(ProbeVector.size());
		for (ProbeRequestBrief b : ProbeVector)
			if (b.getEndTime().before(e) && b.getStartTime().after(s))
				clusterInput.add(new ProbeRequestBriefWrapper(b, config.apNum));

		KMeansPlusPlusClusterer<ProbeRequestBriefWrapper> clusterer = new KMeansPlusPlusClusterer<ProbeRequestBriefWrapper>(
				k, iterationNum);
		List<CentroidCluster<ProbeRequestBriefWrapper>> clusterResults = clusterer.cluster(clusterInput);
		// output the clusters
		int Clustrsize = 0;
		double[] center = new double[config.apNum + 1];
		for (int i = 0; i < clusterResults.size(); i++) {
			if (clusterResults.get(i).getPoints().size() > Clustrsize) {
				Clustrsize = clusterResults.get(i).getPoints().size();
				center = clusterResults.get(i).getCenter().getPoint();
			}
		}

		double rawCenter[] = new double[config.apNum];
		for (int i = 0; i < center.length - 1; i++) {
			rawCenter[i] = center[i] + center[center.length - 1];
		}

		String info = ProbeVector.size() + "\t" + Clustrsize + "\t"
				+ clusterResults.get(0).getPoints().get(0).gettProbeRequestBrief().getSourceMAC() + "\t";
		for (int i = 0; i < rawCenter.length; i++) {
			info += Math.round(rawCenter[i] * 100.0) / 100.0 + "\t";
		}
		System.out.println(info);
		return center;

	}

	static void DBSCANClusterFingerPrint(double eps, int MinPts, ArrayList<FingerPrintWrapper> ProbeVector) {
		
		List<FingerPrintWrapper> clusterInput = new ArrayList<FingerPrintWrapper>(ProbeVector.size());
		for (FingerPrintWrapper b : ProbeVector) {
			clusterInput.add(b);
//			System.out.println("\t" + ArrayToString(b.getPoint()));
		}

		DBSCANClusterer<FingerPrintWrapper> clusterer = new DBSCANClusterer<FingerPrintWrapper>(eps, MinPts);
		List<Cluster<FingerPrintWrapper>> clusterResults = clusterer.cluster(clusterInput);
		System.out.println(clusterResults.size());
		// output the clusters

		for (int i = 0; i < clusterResults.size(); i++) {
			ArrayList<Double> index = new ArrayList<Double>();
			for (int j = 0; j < config.apNum + 1; j++) {
				index.add(0.0);
			}
			for (FingerPrintWrapper Fp : clusterResults.get(i).getPoints()) {
//				 System.out.println(Fp.gettFingerPrint().getSourceMAC()+" "+ArrayToString(Fp.getPoint()));
				for (int j = 0; j < config.apNum + 1; j++) {
					index.set(j, index.get(j) + Fp.getPoint()[j]);
				}
			}

			for (int j = 0; j < config.apNum + 1; j++) {
				index.set(j, index.get(j) / (double) clusterResults.get(i).getPoints().size());
			}

			System.out.println("C" + i);
			System.out.println("RSSI(db): {" + ArrayListToString(index) + "}");
			System.out.println("total number of nodes: " + clusterResults.get(i).getPoints().size());
			String member = "";
			for (FingerPrintWrapper Fp : clusterResults.get(i).getPoints()) {
				member += Fp.gettFingerPrint().getSourceMAC() + " ";
			}
			System.out.print("Member:" + member + "\n");

		}

	}

	static String ArrayListToString(ArrayList<Double> t) {
		String info = "";
		for (int i = 0; i < t.size(); i++)
			info += t.get(i) + " ";
		return info;

	}

	static String ArrayToString(double[] t) {
		String info = "";
		for (int i = 0; i < t.length; i++)
			info += t[i] + " ";
		return info;

	}

	static int CalculateTogertherTime(ArrayList<ProbeRequestBrief> a, ArrayList<ProbeRequestBrief> b) {
		int stayTime = 0;
		for (int i = 0; i < a.size(); i++) {
			for (int j = 0; j < b.size(); j++) {
				int tmp = FunctionUtility.GetOverlapInSecond(a.get(i), b.get(j));
				if (tmp > 0 && getSTESimilarityOfFingerprint(a.get(i).getRssiVec(), b.get(j).getRssiVec()) > 0.8) {
					stayTime += (tmp+1);
				}
			}
		}
		return stayTime;
	}

	static int CalculateStayPeriod(ArrayList<ProbeRequestBrief> sp, Calendar s, Calendar e) {
		int total = 0;
		for (int i = 0; i < sp.size(); i++) {
			if (!sp.get(i).getStartTime().before(s) && !sp.get(i).getEndTime().after(e)) {
				int tmp = FunctionUtility.GetTimediffInSecond(sp.get(i).getStartTime(), sp.get(i).getEndTime());
				if (tmp < 60)
					total += 60;
				else
					total += tmp;
			} else if (sp.get(i).getStartTime().before(s) && sp.get(i).getEndTime().before(e)
					&& sp.get(i).getEndTime().after(s)) {
				int tmp = FunctionUtility.GetTimediffInSecond(s, sp.get(i).getEndTime());
				if (tmp < 60)
					total += 60;
				else
					total += tmp;
			} else if (sp.get(i).getEndTime().after(e) && sp.get(i).getStartTime().before(e)
					&& sp.get(i).getStartTime().after(s)) {
				int tmp = FunctionUtility.GetTimediffInSecond(sp.get(i).getStartTime(), e);
				if (tmp < 60)
					total += 60;
				else
					total += tmp;
			} else {
			}

		}
		return total;
	}

	static void GenerateSignalGraph(Calendar s, Calendar e) {
		for (int i = 0; i < CheckedMac.size(); i++) {
			String mac = CheckedMac.get(i);
			IOUtility.WriteSignalData(mMacToProbeData.get(mac), s, e, mac, config.path);
		}
		try {
			IOUtility.GenerateSignalPlot(CheckedMac, centerPanel, config.apNum, config.path);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	static void GenerateErrorBarGraph(Calendar s, Calendar e) {
		if (CheckedMac.size() > 0) {
			IOUtility.WriteErrorBarData(CheckedMac, mMacToProbeData, s, e, config.apNum, config.path);

			try {
				IOUtility.GenerateErrorBarPlot(CheckedMac, centerPanel, config.path);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}
	// process raw data to build mapping meta-data
	static void filterProbeData() {
		Iterator<Map.Entry<String, ArrayList<ArrayList<ProbeRequest>>>> iter = mMacToProbeData.entrySet().iterator();
		while (iter.hasNext()) {
			// need at least 20 probe request to process
			ArrayList<ArrayList<ProbeRequest>> entry = iter.next().getValue();
			int probeSize = 0;
			for (int i = 0; i < entry.size(); i++) {
				probeSize += entry.get(i).size();
			}
			if (probeSize < 20) {
				iter.remove();
			}
		}
	}

	static void buildProfilePerMac() {

		File GapWriter = new File("/home/honghande/ProbeBeacon/BizCanteenData/" + "gapData.txt");
		int a1 = 0, a2 = 0, a3 = 0, a4 = 0, a5 = 0, a6 = 0, a7 = 0, a8 = 0, a9 = 0, a10 = 0;

		try {
			FileOutputStream fos = new FileOutputStream(GapWriter);

			for (String m : mMacToProbeData.keySet()) {
				ArrayList<Double> MProfile = new ArrayList<Double>();
				ArrayList<Integer> GapList = new ArrayList<Integer>();
				String info = m;
				for (int i = 0; i < config.apNum; i++) {
					// double max = -100;
					// double min = -20;
					// for (int k = 0; k < mMacToProbeData.get(m).get(i).size();
					// k++) {
					// double rssi =
					// mMacToProbeData.get(m).get(i).get(k).getAveragerssiWithoutOutliar();
					// if (rssi > max)
					// max = rssi;
					// if (rssi < min)
					// min = rssi;
					// }
					// info += "\t" + max + "\t " + min;
					// MProfile.add(max);
					// MProfile.add(min);

					for (int k = 0; k < mMacToProbeData.get(m).get(i).size() - 1; k++) {
						int gaptime = FunctionUtility.GetTimediffInSecond(
								mMacToProbeData.get(m).get(i).get(k).getStartTime(),
								mMacToProbeData.get(m).get(i).get(k + 1).getStartTime());
						// if (gaptime > 0){
						// GapList.add(gaptime);
						// fos.write((gaptime+"\n").getBytes());
						//
						// if(gaptime<=5)
						// a1++;
						// else if(gaptime<=10)
						// a2++;
						// else if(gaptime<=20)
						// a3++;
						// else if(gaptime<=30)
						// a4++;
						// else if(gaptime<=60)
						// a5++;
						// else if(gaptime<=120)
						// a6++;
						// else if(gaptime<=180)
						// a7++;
						// else if(gaptime<=300)
						// a8++;
						// else if(gaptime<=600)
						// a9++;
						// else if(gaptime>600)
						// a10++;
						//
						// }
						if (gaptime <= 60 * 10)
							fos.write((gaptime + "\n").getBytes());
					}

				}
				// MacToProfile.put(m, MProfile);
				// // System.out.println(m+" "+MProfile.toString());
				//
				// // Creating a Frequency Map.
				// Map<Integer, Integer> frequencyMap = new HashMap<Integer,
				// Integer>();
				//
				// // defining a set of map entry that sorts based on value
				// Set<Map.Entry<Integer, Integer>> sortedFrequencySet = new
				// TreeSet<Map.Entry<Integer, Integer>>(
				// new Comparator<Map.Entry<Integer, Integer>>() {
				// @Override
				// public int compare(Map.Entry<Integer, Integer> obj1,
				// Map.Entry<Integer, Integer> obj2) {
				// int vcomp = obj1.getValue().compareTo(obj2.getValue());
				// return -vcomp;
				// }
				// });
				//
				// // populating frequency Map.
				// for (Integer integer : GapList) {
				// if (frequencyMap.containsKey(integer)) {
				// Integer val = frequencyMap.get(integer);
				// frequencyMap.put(integer, ++val);
				// } else {
				// frequencyMap.put(integer, 1);
				// }
				// }
				//
				// sortedFrequencySet.addAll(frequencyMap.entrySet());
				// Map.Entry<Integer, Integer> firstKey =
				// sortedFrequencySet.iterator().next();
				// if (firstKey.getKey() > 10)
				// // System.out.println(m+"\t" + firstKey.getKey() +"\t"+
				// //
				// firstKey.getValue()+"\t"+(double)firstKey.getValue()/GapList.size());
				// MacTomostFrequencyGap.put(m, firstKey.getKey());

			}

			// System.out.println(a1 + " " + a2 + " " + a3 + " " + a4 + " " + a5
			// + " " + a6 + " " + a7 + " " + a8 + " "
			// + a9 + " " + a10);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static SortedMap<Long, Color> getColorMap() {
		// int[] values = new int[]{0, 32, 64, 96, 128, 160, 192, 224, 255};
		int[] values = new int[] { 0, 64, 128, 192, 255 };
		SortedMap<Long, Color> colors = new TreeMap<>(Collections.reverseOrder());// 125
																					// colors
		long k = 0;
		for (int r : values) {
			for (int g : values) {
				for (int b : values) {
					colors.put(k++, new Color(r, g, b));
				}
			}
		}
		return colors;
	}

	static void ProcessVectorFromMap() {
		SortedMap<Long, Color> colorsMap = getColorMap();
		long colorIndex = 0;

		// generate RSSI vector with time stamp and MAC address
		ListOfProbeVector = new ArrayList<ProbeRequestBrief>();
		for (String m : mMacToProbeData.keySet()) {

			ArrayList<ProbeRequestBrief> ProbeVector = new ArrayList<ProbeRequestBrief>();
			ProbeRequest[] HistoryProbe = new ProbeRequest[config.apNum];
			// initialize as 0
			int[] count = new int[config.apNum];
			for (int i = 0; i < config.apNum; i++) {
				count[i] = 0;
				HistoryProbe[i] = null;
			}

			boolean isGoingOn = true;
			ArrayList<ArrayList<ProbeRequest>> PD = mMacToProbeData.get(m);

			while (isGoingOn) {
				isGoingOn = false;
				// initialize as all -99
				double[] spottedRssi = new double[config.apNum];
				for (int i = 0; i < config.apNum; i++)
					spottedRssi[i] = -99;

				// bypass null data from first several AP
				int index = -1; // earliest AP data index
				for (int i = 0; i < config.apNum; i++) {
					if (count[i] < PD.get(i).size()) {
						index = i;
						break;
					}
				}

				// finding the earliest index from rest AP data
				for (int i = index + 1; i < config.apNum && index > -1; i++) {
					if (count[i] < PD.get(i).size()) {
						if (PD.get(i).get(count[i]).getStartTime()
								.before(PD.get(index).get(count[index]).getStartTime()))
							index = i;
					}
				}

				spottedRssi[index] = PD.get(index).get(count[index]).getAveragerssiWithoutOutliar();
				HistoryProbe[index] = PD.get(index).get(count[index]);

				Calendar ts = PD.get(index).get(count[index]).getStartTime();
				Calendar te = PD.get(index).get(count[index]).getEndTime();

				for (int i = 0; i < config.apNum; i++) {
					if (i != index) {
						if (count[i] < PD.get(i).size()) {
							if (HistoryProbe[i] == null)
								HistoryProbe[i] = PD.get(i).get(count[i]);
							if (PD.get(index).get(count[index]).IsInSameStaying(PD.get(i).get(count[i]), 20)) {
								// within 20 second, because not well
								// synchronize
								if (Math.abs(PD.get(index).get(count[index])
										.GetTimediffInSecond(PD.get(i).get(count[i]))) < Math.abs(
												PD.get(index).get(count[index]).GetTimediffInSecond(HistoryProbe[i]))) {
									// check the next data and history data who
									// is closer
									spottedRssi[i] = PD.get(i).get(count[i]).getAveragerssiWithoutOutliar();
									HistoryProbe[i] = PD.get(i).get(count[i]);
									if (PD.get(i).get(count[i]).getEndTime().after(te))
										te = PD.get(i).get(count[i]).getEndTime();
									count[i]++;
								} else {
									spottedRssi[i] = HistoryProbe[i].getAveragerssiWithoutOutliar();
									if (HistoryProbe[i].getEndTime().after(te))
										te = HistoryProbe[i].getEndTime();
								}

							} else if (PD.get(index).get(count[index]).IsInSameStaying(HistoryProbe[i], 120)) {
								spottedRssi[i] = HistoryProbe[i].getAveragerssiWithoutOutliar();
								if (HistoryProbe[i].getEndTime().after(te))
									te = HistoryProbe[i].getEndTime();
							}
						} else if (HistoryProbe[i] != null) {
							if (PD.get(index).get(count[index]).IsInSameStaying(HistoryProbe[i], 300)) {
								spottedRssi[i] = HistoryProbe[i].getAveragerssiWithoutOutliar();
								if (HistoryProbe[i].getEndTime().after(te))
									te = HistoryProbe[i].getEndTime();
							}
						}
					}

				}

				count[index]++;
				int valid = 0;
				int goodSignal = 0;
				for (int c = 0; c < spottedRssi.length; c++) {
					if (spottedRssi[c] != -99) {
						valid++;
					}
					if (spottedRssi[c] > -70.1) {
						goodSignal++;
					}

				}

				if (valid > 4 && goodSignal > 0) {
					ProbeRequestBrief tp = new ProbeRequestBrief(ts, te, m, spottedRssi, config.apNum);
					ListOfProbeVector.add(tp);
					ProbeVector.add(tp);
				}

				// go on if still there are some data
				for (int i = 0; i < config.apNum; i++) {
					if (count[i] < PD.get(i).size()) {
						isGoingOn = true;
						break;
					}
				}
				// System.out.println(count[0] + " " + count[1] + " " + count[2]
				// + " " + count[3] + " " + count[4]);
			}

			if (ProbeVector.size() > 10) {
				// System.out.println(m+" vector size: "+ProbeVector.size());
				MacToProbeVector.put(m, ProbeVector);
				MacToColor.put(m, colorsMap.get((colorIndex++) % 124));
				centerPanel.MacToColor.put(m, colorsMap.get((colorIndex++) % 124));
			}

		}
		// IntraMergeVector();
		// IntraMergeVector();
	}

	static void IntraMergeVector() {
		for (String m : MacToProbeVector.keySet()) {
			int i = 0;
			int size = 1;
			while (i < MacToProbeVector.get(m).size() - 1) {

				if (FunctionUtility.CanMerge(MacToProbeVector.get(m).get(i), MacToProbeVector.get(m).get(i + 1))
						&& getSimilarityOfFingerprint(MacToProbeVector.get(m).get(i),
								MacToProbeVector.get(m).get(i + 1)) > 0.8) {
					MacToProbeVector.get(m).get(i).setEndTime(MacToProbeVector.get(m).get(i + 1).getEndTime());
					for (int j = 0; j < config.apNum; j++)
						MacToProbeVector.get(m).get(i)
								.getRssiVec()[j] = (MacToProbeVector.get(m).get(i).getRssiVec()[j] * size
										+ MacToProbeVector.get(m).get(i + 1).getRssiVec()[j]) / (size + 1);
					size++;
					MacToProbeVector.get(m).remove(i + 1);
				} else {
					i++;
					size = 1;
				}
			}
		}
	}

	static void ProcessCheckBox() {
		// add check box
		Map<String, ArrayList<ProbeRequestBrief>> sortedmap = new TreeMap<String, ArrayList<ProbeRequestBrief>>(
				MacToProbeVector);
		// System.out.println("in the check box");
		for (final String m : sortedmap.keySet()) {
			JCheckBox check = new JCheckBox(m + " " + sortedmap.get(m).size());
			box.add(check);

			check.setSelected(false);

			check.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent ie) {
					centerPanel.setIsDrawNode(true);
					centerPanel.setIsLoadImage(false);
					JCheckBox b = (JCheckBox) ie.getItem();
					if (b.isSelected()) {
						if (!CheckedMac.contains(m))
							CheckedMac.add(m);
						// commented for time saving

						Calendar start = Calendar.getInstance();
						start.clear();
						start.set(Integer.parseInt(syear.getText()), Integer.parseInt(smonth.getText()),
								Integer.parseInt(sday.getText()), Integer.parseInt(shour.getText()),
								Integer.parseInt(smin.getText()), Integer.parseInt(ssecond.getText()));

						Calendar end = Calendar.getInstance();
						end.clear();
						end.set(Integer.parseInt(eyear.getText()), Integer.parseInt(emonth.getText()),
								Integer.parseInt(eday.getText()), Integer.parseInt(ehour.getText()),
								Integer.parseInt(emin.getText()), Integer.parseInt(esecond.getText()));
						// System.out.println(FunctionUtility.CalendarToString(start)+"
						// "+FunctionUtility.CalendarToString(end) );
						ArrayList<ProbeRequestBrief> prb = new ArrayList<ProbeRequestBrief>();
						for (int i = 0; i < MacToProbeVector.get(m).size(); i++) {
							if (MacToProbeVector.get(m).get(i).getStartTime().after(start)
									&& MacToProbeVector.get(m).get(i).getEndTime().before(end))
								prb.add(MacToProbeVector.get(m).get(i));
						}

						centerPanel.UpdateMap.put(m, prb);

						if (prb.size() > 0) { // for normal node drawing
							textArea.append("---" + m + "---" + prb.size() + "nodes\n\n");
							for (int i = 0; i < prb.size(); i++) {
								UpdateText(prb.get(i));

							}
						}
						
						TraceReconstruction trc = new TraceReconstruction(MacToProbeVector.get(m), centerPanel.bgimage,
								centerPanel.Anchor, config.apNum, config.scale);
						
						centerPanel.mMovementTrace = trc.generateIndependentTrace();
						
											

//						if (mMacToDiningTrace.containsKey(m)) {
//							if (mMacToDiningTrace.get(m).size() > 0) { 
//								textArea.append("---" + m + "---" + mMacToDiningTrace.get(m).size() + "nodes\n\n");
//								for (int i = 0; i < mMacToDiningTrace.get(m).size(); i++) {
//									UpdateText(mMacToDiningTrace.get(m).get(i));
//								}
////								if (DPD != null) {
////									DPD.findPeopleWithSimilarDiningTime(m, Double.parseDouble(eps.getText()),
////											Integer.parseInt(MinPts.getText()));
////								}
//							}
//						}

					} else {
						if (CheckedMac.contains(m))
							CheckedMac.remove(m);
						centerPanel.UpdateMap.remove(m);
					}
					centerPanel.revalidate();
					centerPanel.repaint();
				}

			});

			box.revalidate();
			box.repaint();
		}
	}

	static void GetLocalizationError(HashMap<String, ArrayList<ProbeRequestBrief>> macToProbeVector) {
		File GapWriter = new File("/home/honghande/ProbeBeacon/piLab/" + "errorData.txt");
		for (int i = 0; i < startTimeList.size(); i++) {

			ArrayList<FingerPrint> tmp = new ArrayList<FingerPrint>();
			ArrayList<FingerPrintWrapper> tmpplus = new ArrayList<FingerPrintWrapper>();
			Calendar start = startTimeList.get(i);
			Calendar end = (Calendar) startTimeList.get(i).clone();
			end.add(Calendar.MINUTE, 3);

		}

	}

	// button trigger event
	static void GetProbeRequestByTime(Calendar s, Calendar e, int scale,
			HashMap<String, ArrayList<ProbeRequestBrief>> mMacToProbeVector) {

		HashMap<String, ArrayList<ProbeRequestBrief>> ListOfMacToProbeVectorByTime = new HashMap<String, ArrayList<ProbeRequestBrief>>();
		if (CheckedMac.size() == 0) {
			for (String m : mMacToProbeVector.keySet()) {
				ArrayList<ProbeRequestBrief> t = new ArrayList<ProbeRequestBrief>();
				for (ProbeRequestBrief b : mMacToProbeVector.get(m)) {
					if (b.getStartTime().after(s) && b.getEndTime().before(e)) {
						t.add(b);
					}
				}
				if (t.size() > 0) {
					ListOfMacToProbeVectorByTime.put(m, t);
				}
			}
			UpdateUIFromMacToList(ListOfMacToProbeVectorByTime, scale);
		} else {

			for (int i = 0; i < CheckedMac.size(); i++) {
				ArrayList<ProbeRequestBrief> t = new ArrayList<ProbeRequestBrief>();
				// check for time restriction
				if (mMacToProbeVector.containsKey(CheckedMac.get(i))) {
					for (ProbeRequestBrief b : mMacToProbeVector.get(CheckedMac.get(i))) {
						if (b.getStartTime().after(s) && b.getEndTime().before(e)) {
							t.add(b);
						}
					}
					if (t.size() > 0) {
						ListOfMacToProbeVectorByTime.put(CheckedMac.get(i), t);
					}
				}
//				cmpFingeprint(mMacToProbeVector.get(CheckedMac.get(i)));
			}
			UpdateUIFromMacToList(ListOfMacToProbeVectorByTime, scale); // udpate																// UI
		}
	}

	// button trigger event
	static void GetSimilarityTrend(Calendar s, Calendar e,
			HashMap<String, ArrayList<ProbeRequestBrief>> mMacToProbeVector) {

		HashMap<String, ArrayList<ProbeRequestBrief>> ListOfMacToProbeVectorByTime = new HashMap<String, ArrayList<ProbeRequestBrief>>();
		System.out.println(CheckedMac.size());	
		if (CheckedMac.size() == 2) {
			//do a filter of data first 
			for (String m : mMacToProbeVector.keySet()) {
				ArrayList<ProbeRequestBrief> t = new ArrayList<ProbeRequestBrief>();
				for (ProbeRequestBrief b : mMacToProbeVector.get(m)) {
					if (b.getStartTime().after(s) && b.getEndTime().before(e)) {
						t.add(b);
					}
				}
				if (t.size() > 0) {
					ListOfMacToProbeVectorByTime.put(m, t);
				}
			}
			System.out.println("data prepare done "+ListOfMacToProbeVectorByTime.get(CheckedMac.get(0)).size()+" "+ListOfMacToProbeVectorByTime.get(CheckedMac.get(1)).size());
			//track the similarity trend of device
			double lastData=0;
			double alfa = 0.5;
			for(ProbeRequestBrief pb : ListOfMacToProbeVectorByTime.get(CheckedMac.get(0))){
				int index =-1;
				int timedifference = 10000;
				for(int i =0;i<ListOfMacToProbeVectorByTime.get(CheckedMac.get(1)).size();i++){
					int temp= Math.abs(FunctionUtility.GetTimediffInSecond(pb.getStartTime(), 
							ListOfMacToProbeVectorByTime.get(CheckedMac.get(1)).get(i).getStartTime()));
					if(temp < timedifference){
						index =i;
						timedifference = temp;
					}
						
				}
					
				if(lastData==0)
					lastData = getSimilarityOfFingerprint(pb, ListOfMacToProbeVectorByTime.get(CheckedMac.get(1)).get(index));
				else{
					if(timedifference<300 && index >=0){
						double similarity = (1-alfa)*lastData+alfa*getSimilarityOfFingerprint(pb, ListOfMacToProbeVectorByTime.get(CheckedMac.get(1)).get(index));
						System.out.println(FunctionUtility.CalendarToString(pb.getStartTime())
								+"\t"+FunctionUtility.CalendarToString(ListOfMacToProbeVectorByTime.get(CheckedMac.get(1)).get(index).getStartTime())
								+"\t"+(similarity));
						lastData = similarity;
					}
				}
				

			}
		}
	}
	
	static void cmpFingeprint(ArrayList<ProbeRequestBrief> ProbeVector) {
		HashMap<Integer, ArrayList<FingerPrint>> validset = new HashMap<Integer, ArrayList<FingerPrint>>(); //standard fingerprint
		HashMap<Integer, ArrayList<FingerPrint>> validset2 = new HashMap<Integer, ArrayList<FingerPrint>>(); //standard fingerprint
		HashMap<Integer, ArrayList<FingerPrintWrapper>> validsetplus = new HashMap<Integer, ArrayList<FingerPrintWrapper>>(); //STE fingerprint

		//build up data map for comparison experiment
		for (int i = 0; i < startTimeList.size(); i++) {

			ArrayList<FingerPrint> tmp = new ArrayList<FingerPrint>();
			ArrayList<FingerPrint> tmp2 = new ArrayList<FingerPrint>();
			ArrayList<FingerPrintWrapper> tmpplus = new ArrayList<FingerPrintWrapper>();
			Calendar start = startTimeList.get(i);
			Calendar end = (Calendar) startTimeList.get(i).clone();
			end.add(Calendar.MINUTE, 3);

			for (ProbeRequestBrief b : ProbeVector) {
				if (b.getStartTime().after(start) && b.getEndTime().before(end)) {
					tmp.add(new FingerPrint(b.getSourceMAC(), b.getRssiVec(), config.apNum));
					tmp2.add(new FingerPrint(b.getSourceMAC(), b.getRssiVec(), config.apNum));
					tmpplus.add(new FingerPrintWrapper(new FingerPrint(b.getSourceMAC(), b.getRssiVec(), config.apNum),
							config.apNum));
				}
			}
			validset.put(i, tmp);
			validset2.put(i, tmp2);
			validsetplus.put(i, tmpplus);
		}
		double Slopeall =0;
		double Slopeallmean =0;
		double Slopeplusall = 0;
		for (int i = 0; i < startTimeList.size(); i++) {
			double[] SimilarityArray= new double[startTimeList.size()];
			double[] SimilarityArrayMean= new double[startTimeList.size()];
			double[] SimilarityArrayplus =new double[startTimeList.size()];
			double Slope =0;
			double SlopeMean =0;
			double Slopeplus = 0;
			for (int j = 0; j < startTimeList.size(); j++) {
				double similarity = 0;
				double similarityMean = 0;
				double similarityPlus = 0;
				for (int a = 0; a < validset.get(i).size(); a++) {
					for (int b = 0; b < validset.get(j).size(); b++) {
						similarity += getSimilarityOfFingerprint(validset.get(i).get(a).getRssiVec(),
								validset.get(j).get(b).getRssiVec());
						similarityMean += getSimilarityOfFingerprintSubtractMean(validset2.get(i).get(a).getRssiVec(),
								validset2.get(j).get(b).getRssiVec());
						similarityPlus += getSimilarityOfFingerprint(validsetplus.get(i).get(a).getPoint(),
								validsetplus.get(j).get(b).getPoint());
					}
				}
				SimilarityArray[j] =  similarity / validset.get(i).size() / validset.get(j).size();
				SimilarityArrayMean[j] =  similarityMean / validset.get(i).size() / validset.get(j).size();
				SimilarityArrayplus[j] =  similarityPlus / validset.get(i).size() / validset.get(j).size();
				System.out.println((i + 1) + "\t" + (j + 1) + "\t" + GetGroundTruthDistance(i, j) + "\t"
								+ Math.round(100.0 * similarity / validset.get(i).size() / validset.get(j).size())
								/ 100.0
								+ "\t"
								+ Math.round(100.0 * SimilarityArrayMean[j])
								/ 100.0
								+ "\t"
								+ Math.round(100.0 * similarityPlus / validset.get(i).size() / validset.get(j).size())
										/ 100.0);

			}
			for(int k=0;k< startTimeList.size();k++){
				if(k!=i){
					Slope +=(SimilarityArray[i]-SimilarityArray[k])/GetGroundTruthDistance(i, k);
					SlopeMean +=(SimilarityArrayMean[i]-SimilarityArrayMean[k])/GetGroundTruthDistance(i, k);
					Slopeplus +=(SimilarityArrayplus[i]-SimilarityArrayplus[k])/GetGroundTruthDistance(i, k);
				}
			}
			Slope /=startTimeList.size()-1;
			SlopeMean /=startTimeList.size()-1;
			Slopeplus /=startTimeList.size()-1;
			
			Slopeall += Slope;
			Slopeallmean +=SlopeMean;
			Slopeplusall +=Slopeplus;
			System.out.println(Math.round(1000 *Slopeall/(i+1))/1000.0+"\t"
					 +Math.round(1000 *Slopeallmean/(i+1))/1000.0+"\t"
					 +Math.round(1000 *Slopeplusall/(i+1))/1000.0);
			System.out.println();			
			
//			System.out.println(Math.round(100 *Slope)/100.0+"\t"+Math.round(100 *Slopeplus)/100.0);
		}
		 System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
		 System.out.println(Math.round(1000 *Slopeall/startTimeList.size())/1000.0+"\t"
				 +Math.round(1000 *Slopeallmean/startTimeList.size())/1000.0+"\t"
				 +Math.round(1000 *Slopeplusall/startTimeList.size())/1000.0);
	}

	static double GetGroundTruthDistance(int i, int j) {
		return Math
				.round(100.0
						* Math.sqrt((CoordList.get(i).x - CoordList.get(j).x)
								* (CoordList.get(i).x - CoordList.get(j).x)
								+ (CoordList.get(i).y - CoordList.get(j).y) * (CoordList.get(i).y - CoordList.get(j).y))
				/ 50.0) / 100.0;
	}

	static double getSimilarityOfFingerprint(ProbeRequestBrief a1, ProbeRequestBrief a2) {
//		ProbeRequestBriefWrapper pbwa = new ProbeRequestBriefWrapper(a1, config.apNum);
//		ProbeRequestBriefWrapper pbwb = new ProbeRequestBriefWrapper(a2, config.apNum);
		double[] aa = a1.getRssiVec();
		double[] bb = a2.getRssiVec();
			
		ArrayList<Double> a = new ArrayList<Double>();
		ArrayList<Double> b = new ArrayList<Double>();
		
		for(int i=0;i<aa.length;i++){
			if(aa[i]!=-99 && bb[i]!=-99){
				a.add(aa[i]);
				b.add(bb[i]);
			}
		}
		
		
		double[] ste_a = getSTEFingerprint(a);
		double[] ste_b = getSTEFingerprint(b);
		
//		ArrayList<Double> ste_a = a;
//		ArrayList<Double> ste_b = b;
		
		double result;
		if(ste_b.length>3){
			result = (getCrossPorduct(ste_a,ste_b))
					/ (getCrossPorduct(ste_a,ste_a) + getCrossPorduct(ste_b, ste_b) - getCrossPorduct(ste_a, ste_b));
		}else{
			result = 0.2;
		}
		
		result = (result-0.5)*2;//result - Math.abs(ste_a[ste_a.length - 1] - ste_b[ste_b.length - 1])/100.0;
		return result;
	}
	
	static double getSTESimilarityOfFingerprint(double[] a, double[] b) {
		
		double[] ste_a = getSTEFingerprint(a);
		double[] ste_b = getSTEFingerprint(b);
		
		double result;
		if(ste_b.length>3){
			result = (getCrossPorduct(ste_a,ste_b))
					/ (getCrossPorduct(ste_a,ste_a) + getCrossPorduct(ste_b, ste_b) - getCrossPorduct(ste_a, ste_b));
		}else{
			result = 0.2;
		}
		
		return result;
	}
	
	static double[] getSTEFingerprint(ArrayList<Double> in){
		
		double[] out = new double[in.size()+1];
		
		double max =-100;
    	double min =-20;
    	
    	for(int i=0;i<in.size();i++){
    		if(in.get(i)>max && in.get(i)>-99)
    			max =in.get(i);	 
    		if(in.get(i)<min && in.get(i)>-99)
    			min =in.get(i);	 
    	}
    	
    	for(int i=0;i<in.size();i++){
    			out[i]=in.get(i)-max;
    	}
    	out[in.size()-1] = max;
		
		
		return out;
	}
	
	static double[] getSTEFingerprint(double[] in){
		
		double[] out = new double[in.length+1];
		
		double max =-100;
    	double min =-20;
    	
    	for(int i=0;i<in.length;i++){
    		if(in[i]>max && in[i]>-99)
    			max =in[i];	 
    		if(in[i]<min && in[i]>-99)
    			min =in[i];	 
    	}
    	
    	for(int i=0;i<in.length;i++){
    			out[i]=in[i]-max;
    	}
    	out[in.length-1] = max;
		
		
		return out;
	}

	static double getSimilarityOfFingerprint(double[] a, double[] b) {
		// cosine similarity

		// pearson correlation
		double result = (getCrossPorduct(a, b))
				/ (getCrossPorduct(a, a) + getCrossPorduct(b, b) - getCrossPorduct(a, b));
		// result = result - Math.abs(a[a.length - 1] - b[b.length - 1]) /
		// 100.0;

		return result;
	}
	
	static double getSimilarityOfFingerprintSubtractMean(double[] a, double[] b) {
		// cosine similarity

		// pearson correlation
		 double a_avg = 0;
		 double b_avg = 0;
		 double[] aa = a.clone();
		 double[] bb = b.clone();
		
		 for(int i=0;i<a.length;i++){
			 a_avg +=a[i];
			 b_avg +=b[i];
		 }
		 a_avg /=a.length;
		 b_avg /=b.length;
		
		 for(int i=0;i<a.length;i++){
			 aa[i]-= a_avg;
			 bb[i]-=b_avg;
		 }
		double result = (getCrossPorduct(aa, bb))
				/ (getCrossPorduct(aa, aa) + getCrossPorduct(bb, bb) - getCrossPorduct(aa, bb));
		result = (result+1.0)/2.0;
		// result = result - Math.abs(a[a.length - 1] - b[b.length - 1]) /
		// 100.0;

		return result;
	}

	static double getCrossPorduct(double[] a, double[] b) {
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
	
	static double getCrossPorduct(ArrayList<Double> a, ArrayList<Double> b) {
		if (a.size() != b.size())
			return 0;
		else {
			double result = 0;
			for (int i = 0; i < a.size(); i++) {
				result += a.get(i) * b.get(i);
			}

			return result;
		}
	}

	static double GetMoranRatioEnhanced(ArrayList<FingerPrint> data) {
		double MR = 0;
		double sum = 0;

		List<FingerPrintWrapper> clusterInput = new ArrayList<FingerPrintWrapper>(data.size());
		for (FingerPrint b : data)
			clusterInput.add(new FingerPrintWrapper(b, config.apNum));

		int N = data.size();
		double[] meansRssiVec = new double[config.apNum + 1];
		for (int i = 0; i < config.apNum + 1; i++) {
			sum = 0;
			for (int j = 0; j < N; j++)
				sum += clusterInput.get(j).getPoint()[i];
			meansRssiVec[i] = sum / N;
		}

		System.out.println(N + " " + meansRssiVec[0] + ", " + meansRssiVec[1] + ", " + meansRssiVec[2] + ", "
				+ meansRssiVec[3] + ", " + meansRssiVec[4] + ", " + meansRssiVec[5]);

		for (int i = 0; i < N; i++) {
			for (int j = i; j < N; j++) {
				MR += GetDistance(clusterInput.get(i).getPoint(), clusterInput.get(j).getPoint());
			}
		}

		// MR = UpIndex/(DownIndex*N);
		return MR / (N * (N - 1) / 2);

	}

	static double GetMoranRatio(ArrayList<ProbeRequestBrief> data) {
		double MR = 0;
		double sum = 0;
		double UpIndex = 0;
		double DownIndex = 0;
		int N = data.size();
		double[] meansRssiVec = new double[config.apNum];
		for (int i = 0; i < config.apNum; i++) {
			sum = 0;
			for (int j = 0; j < N; j++)
				sum += data.get(j).getRssiVec()[i];
			meansRssiVec[i] = sum / N;
		}

		// System.out.println(N+" "+meansRssiVec[0]+", "+meansRssiVec[1]+",
		// "+meansRssiVec[2]+", "+meansRssiVec[3]+", "+meansRssiVec[4]);
		for (int i = 0; i < N; i++) {
			for (int j = i; j < N; j++) {
				MR += GetDistance(data.get(i).getRssiVec(), data.get(j).getRssiVec());
				UpIndex += GetDistance(data.get(i).getRssiVec(), meansRssiVec)
						* GetDistance(data.get(j).getRssiVec(), meansRssiVec);
			}
			DownIndex += GetDistance(data.get(i).getRssiVec(), meansRssiVec)
					* GetDistance(data.get(i).getRssiVec(), meansRssiVec);
		}

		// MR = UpIndex/(DownIndex*N);
		return MR / (N * (N - 1) / 2);

	}

	static double GetDistance(int[] s, double[] t) {
		double distance = 0;
		for (int i = 0; i < t.length; i++) {
			distance += (t[i] - s[i]) * (t[i] - s[i]);
		}
		return Math.sqrt(distance / t.length);
	}

	static double GetDistance(int[] s, int[] t) {
		double distance = 0;
		for (int i = 0; i < t.length; i++) {
			distance += (t[i] - s[i]) * (t[i] - s[i]);
		}
		return Math.sqrt(distance / t.length);
	}

	static double GetDistance(double[] s, double[] t) {
		double distance = 0;
		for (int i = 0; i < t.length; i++) {
			distance += (t[i] - s[i]) * (t[i] - s[i]);
		}
		return Math.sqrt(distance / t.length);
	}

	static void GetCrowdChangeByTime(Calendar s, Calendar e, int scale,
			HashMap<String, ArrayList<ProbeRequestBrief>> mMacToProbeVector) {
		if (IsShowCountingResult) {
			ArrayList<ArrayList<Vector<String>>> CountingTableForAllAP = new ArrayList<ArrayList<Vector<String>>>();
			for (int i = 0; i < config.apNum; i++) {
				Calendar sc = (Calendar) s.clone();
				Calendar ec = (Calendar) s.clone();
				ec.add(Calendar.MINUTE, config.timeGap);

				ArrayList<Vector<String>> CountingTable = new ArrayList<Vector<String>>();

				while (sc.before(e)) {
					Vector<String> macs = new Vector<String>();
					for (String m : mMacToProbeData.keySet()) {
						if (IsMacShow(sc, ec, mMacToProbeData.get(m).get(i))) {// &&LongStayList.contains(m)){
							macs.add(m);
						}
					}
					// if(macs.size()>0)
					CountingTable.add(macs);
					sc.add(Calendar.MINUTE, config.timeGap);
					ec.add(Calendar.MINUTE, config.timeGap);
				}
				CountingTableForAllAP.add(CountingTable);
			}

			Calendar sc = (Calendar) s.clone();
			Calendar ec = (Calendar) s.clone();
			ec.add(Calendar.MINUTE, config.timeGap);
			for (int j = 0; j < CountingTableForAllAP.get(0).size(); j++) {
				// String info = "";
				int NumofPeople = 0;
				for (int i = 0; i < config.apNum; i++) {
					if (CountingTableForAllAP.get(i).get(j).size() > NumofPeople)
						NumofPeople = CountingTableForAllAP.get(i).get(j).size();
					// info += CountingTableForAllAP.get(i).get(j).size() + " ,
					// ";
				}
				// info += CountingTableForAllAP.get(Apnum - 1).get(j).size();
				System.out.println(FunctionUtility.CalendarToString(sc) + " , " + FunctionUtility.CalendarToString(ec)
						+ " , " + NumofPeople);
				sc.add(Calendar.MINUTE, config.timeGap);
				ec.add(Calendar.MINUTE, config.timeGap);
			}

		} else {
			HashMap<String, ArrayList<ProbeRequestBrief>> ListOfMacToProbeVectorByTime = new HashMap<String, ArrayList<ProbeRequestBrief>>();
			for (String m : mMacToProbeVector.keySet()) {
				ArrayList<ProbeRequestBrief> t = new ArrayList<ProbeRequestBrief>();
				for (ProbeRequestBrief b : mMacToProbeVector.get(m)) {
					if (b.getStartTime().after(s) && b.getEndTime().before(e)) {
						t.add(b);
					}
				}
				if (t.size() > 0) {
					ListOfMacToProbeVectorByTime.put(m, t);
				}

			}
			Calendar mid = (Calendar) s.clone();
			mid.add(Calendar.MINUTE, config.timeGap);
			while (s.before(e)) {
				// System.out.println(CalendarToString(s)+"
				// "+CalendarToString(mid));
				GetProbeRequestByTime(s, mid, scale, ListOfMacToProbeVectorByTime);
				s.add(Calendar.MINUTE, config.timeGap);
				mid.add(Calendar.MINUTE, config.timeGap);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	static boolean IsMacShow(Calendar s, Calendar e, ArrayList<ProbeRequest> pr) {
		boolean IsPresented = false;
		for (int i = 0; i < pr.size(); i++) {
			if (pr.get(i).getStartTime().before(e) && pr.get(i).getStartTime().after(s)) {
				IsPresented = true;
				break;
			}
		}
		return IsPresented;
	}

	static void UpdateUIFromMacToList(HashMap<String, ArrayList<ProbeRequestBrief>> p, int scale) {
		textArea.setText(null);
		centerPanel.UpdateMap = p;
		for (String m : p.keySet()) {
			for (int i = 0; i < p.get(m).size(); i++) {
				UpdateText(p.get(m).get(i));
			}
		}
		// System.out.println(p.size()+" "+centerPanel.UpdateMap.size());
		centerPanel.revalidate();
		centerPanel.paintImmediately(0, 0, 1000, 1000);
	}

	// basic utility
	static void UpdateText(ProbeRequestBrief pb) {
		textArea.append("Mac: " + pb.getSourceMAC() + "\n");
		textArea.append("Start Time" + FunctionUtility.CalendarToString(pb.getStartTime()) + "\n");
		textArea.append("End Time" + FunctionUtility.CalendarToString(pb.getEndTime()) + "\n");
		String rssiInfo = "";
		for (int i = 0; i < pb.getRssiVec().length; i++) {
			rssiInfo += Math.round(pb.getRssiVec()[i] * 10.0) / 10.0 + " ";
		}
		textArea.append("{" + rssiInfo + "}" + "\n");
		String distanceInfo = "";
		for (int i = 0; i < pb.getRssiVec().length; i++) {
			distanceInfo += FunctionUtility.generateDistanceFromRssi(pb.getRssiVec()[i], i) + "m ";
		}
		textArea.append("Distance:{" + distanceInfo + "} \n\n");

	}

	static void ClearGraphic() {
		textArea.setText(null);
		FloorMapWindow.clearRect(0, 0, 1000, 10000);
		// drawAP(FloorMapWindow);
	}

	static void ClearCheckBox() {
		for (Component c : box.getComponents()) {
			if (((JCheckBox) c).isSelected()) {
				((JCheckBox) c).setSelected(false);
			}
		}
	}

	public static class Coord {
		public int x;
		public int y;

		Coord(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

}
