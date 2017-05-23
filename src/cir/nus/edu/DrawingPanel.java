package cir.nus.edu;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;

import cir.nus.edu.ProbeRequestStructure.ProbeRequestBrief;
import cir.nus.edu.ProbeRequestStructure.ProbeRequestBriefWrapper;
import cir.nus.util.FunctionUtility;
import cir.nus.util.FunctionUtility.Coord;

public class DrawingPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8523735924227939986L;
	/**
	 * 
	 */
	private boolean IsDrawNode;
	private boolean isDrawTrace = true;
	private boolean IsLoadImage;
	private boolean IsKmeans;
	// private int mScale =50;//real distance to pixel for lab
//	private int Apnum = 8;
//	private int mScale = 15;// real distance to pixel for canteen

	// private Coord ap1 = new Coord(33*mScale,105*mScale); //lab unit 10cm
	// private Coord ap2 = new Coord(96*mScale,123*mScale);
	// private Coord ap3 = new Coord(52*mScale,0*mScale);


	public ArrayList<Coord> Anchor = new ArrayList<Coord>();
	static ArrayList<Calendar> startTimeList = new ArrayList<Calendar>();
	static ArrayList<Coord> CoordList = new ArrayList<Coord>();

	public HashMap<String, ArrayList<ProbeRequestBrief>> UpdateMap = new HashMap<String, ArrayList<ProbeRequestBrief>>();
	public HashMap<String, Color> MacToColor = new HashMap<String, Color>();
	// public ArrayList<ClusterPlotList> ClusterResult = new
	// ArrayList<ClusterPlotList>();
	List<Cluster<ProbeRequestBriefWrapper>> ClusterResultForDBS;
	List<CentroidCluster<ProbeRequestBriefWrapper>> clusterResultsForKmeans;
	public HashMap<Integer, Color> ClusterToColor = new HashMap<Integer, Color>();
	public BufferedImage bgimage = null;
	private double alfa = 0.5;
	public HashMap<String, ArrayList<ProbeRequestBrief>> mMacToDiningTrace = new HashMap<String, ArrayList<ProbeRequestBrief>>();
	public ArrayList<ArrayList<ProbeRequestBrief>> mMovementTrace = new ArrayList<ArrayList<ProbeRequestBrief>>();
	public ArrayList<BufferedImage> image = new ArrayList<BufferedImage>();
	public Configuration config;

	
	FileOutputStream fos = null; 

	public void paintComponent(Graphics g) {
		config = new Configuration();		

		super.paintComponent(g); 
		if(CoordList.isEmpty())
			SetTimeSequence();
//		if(fos ==null){
//			try {
//				fos = new FileOutputStream(new File("/home/honghande/Research/SocialProbe/ProbeBeacon/FigurePlotReady/" + "errorData.txt"));
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		
		g.clearRect(0, 0, 1000, 10000);
		BuildAnchorMap();
		if (IsDrawNode && !IsLoadImage && isDrawTrace) {
			drawAP(g);
			if (UpdateMap.size() > 0) {
				for (String m : UpdateMap.keySet()) {
					
					for (int i = 0; i < mMovementTrace.size(); i++) {
						Coord pre = null;
						for (int j = 0; j < mMovementTrace.get(i).size(); j++) {
							// draw each trace
							ArrayList<Double> index = new ArrayList<Double>();
							for (int k = 0; k < config.apNum; k++) {
								index.add((double) mMovementTrace.get(i).get(j).getRssiVec()[k]);
							}

							Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor, index, config.scale,bgimage);
							if (tmp != null) {
								if (pre != null) {
									Coord ajustPoint = new Coord((int) (tmp.x * alfa + pre.x * (1 - alfa)),
											(int) (tmp.y * alfa + pre.y * (1 - alfa)));
//									ajustPoint = FunctionUtility.getGeoPoint(ajustPoint, bgimage);
									if (ajustPoint != null) {
										drawPointWithLabel(g, ajustPoint.x, ajustPoint.y, "", 1.5, MacToColor.get(m),
												30, 40);
										drawLine(g, pre.x, pre.y, ajustPoint.x, ajustPoint.y);
										pre = ajustPoint;
									}
								} else {
									drawPointWithLabel(g, tmp.x, tmp.y, "", 3, MacToColor.get(m), 30, 40);
									pre = tmp;
								}
							}
						}
					}

			
					
					
					
//					if (mMacToDiningTrace.containsKey(m)) {
////						System.out.println(mMacToDiningTrace.get(m).size());
//						for (int i = 0; i < mMacToDiningTrace.get(m).size(); i++) {
//							ArrayList<Double> index = new ArrayList<Double>();
//							for (int j = 0; j < config.apNum; j++) {
//								index.add((double) mMacToDiningTrace.get(m).get(i).getRssiVec()[j]);
//							}
//							
//							Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor, index, config.scale, bgimage);
//							if (tmp != null) {
//								if (pre != null) {
//									Coord ajustPoint = new Coord((int) (tmp.x * alfa + pre.x * (1 - alfa)),
//											(int) (tmp.y * alfa + pre.y * (1 - alfa)));
//									ajustPoint = FunctionUtility.getGeoPoint(ajustPoint, bgimage);
//									if (ajustPoint != null) {
//										drawPointWithLabel(g, ajustPoint.x, ajustPoint.y, "", 1.5, MacToColor.get(m),
//												30, 40);
//										drawLine(g, pre.x, pre.y, ajustPoint.x, ajustPoint.y);
//										pre = ajustPoint;
//									}
//								} else {
//									drawPointWithLabel(g, tmp.x, tmp.y, "", 3, MacToColor.get(m), 30, 40);
//									pre = tmp;
//								}
//							}
//						}
//
//					}
				}
			}
		} else if (IsDrawNode && !IsLoadImage && !isDrawTrace) {
			drawAP(g);
			if (UpdateMap.size() > 0) {
				for (String m : UpdateMap.keySet()) {
					for (int i = 0; i < UpdateMap.get(m).size(); i++) {

						ArrayList<Double> index = new ArrayList<Double>();
						for (int j = 0; j < config.apNum; j++) {
							index.add((double) UpdateMap.get(m).get(i).getRssiVec()[j]);
						}
						
						Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor, index, config.scale, bgimage);
						
//						int position = findGroundtruth(UpdateMap.get(m).get(i));
//						if(position != -1){
//							double error = Math.round(Math.sqrt((tmp.x-CoordList.get(position).x)*(tmp.x-CoordList.get(position).x)
//									+(tmp.y-CoordList.get(position).y)*(tmp.y-CoordList.get(position).y)));
//							try {
//								fos.write((error/50.0+"\n").getBytes());
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
////							System.out.println(error/50.0);
//						}
						
						if (tmp != null) {
							drawPointWithLabel(g, tmp.x, tmp.y, "", 2, MacToColor.get(m), 30, 40);
						}

					}
				}
			}
		} else if (!IsDrawNode && IsLoadImage) {
			for (int i = 0; i < image.size(); i++)
				g.drawImage(image.get(i), 0, 500 * i, null);
		} else {
			drawAP(g);
			if (IsKmeans) {
				if (clusterResultsForKmeans != null && clusterResultsForKmeans.size() > 0) {
					ColorMaping(clusterResultsForKmeans.size());
					for (int i = 0; i < clusterResultsForKmeans.size(); i++) {
						double[] center = clusterResultsForKmeans.get(i).getCenter().getPoint();

						ArrayList<Double> index = new ArrayList<Double>();
						for (int j = 0; j < config.apNum; j++) {
							index.add(center[j]);
						}

						Coord centerCoodinate = FunctionUtility.generateCoordinateFromDistance(Anchor, index, config.scale,
								bgimage);
						drawPointWithLabel(g, centerCoodinate.x, centerCoodinate.y, "", 1.5, ClusterToColor.get(i), 30,
								40);
						for (int j = 0; j < clusterResultsForKmeans.get(i).getPoints().size(); j++) {
							ArrayList<Double> tindex = new ArrayList<Double>();
							for (int k = 0; k < config.apNum; k++) {
								tindex.add((double) clusterResultsForKmeans.get(i).getPoints().get(j)
										.gettProbeRequestBrief().getRssiVec()[k]);
							}
							Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor, tindex, config.scale, bgimage);
							drawPointWithLabel(g, tmp.x, tmp.y, "", 0.8, ClusterToColor.get(i), 30, 40);
						}
					}
				}
			} else {
				if (ClusterResultForDBS != null && ClusterResultForDBS.size() > 0) {
					ColorMaping(ClusterResultForDBS.size());
					for (int i = 0; i < ClusterResultForDBS.size(); i++) {
						for (int j = 0; j < ClusterResultForDBS.get(i).getPoints().size(); j++) {
							ArrayList<Double> tindex = new ArrayList<Double>();
							for (int k = 0; k < config.apNum; k++) {
								tindex.add((double) ClusterResultForDBS.get(i).getPoints().get(j)
										.gettProbeRequestBrief().getRssiVec()[k]);
							}

							Coord tmp = FunctionUtility.generateCoordinateFromDistance(Anchor, tindex, config.scale, bgimage);
							drawPointWithLabel(g, tmp.x, tmp.y, "", 0.8, ClusterToColor.get(i), 30, 40);
						}
					}
				}
			}
		}
	}
	
	public  void SetTimeSequence() {
		
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
		
		CoordList.add(new Coord(110,612));
		CoordList.add(new Coord(60,533));
		CoordList.add(new Coord(60,457));
		CoordList.add(new Coord(159,375));
		CoordList.add(new Coord(280,378));
		CoordList.add(new Coord(271,534));
		CoordList.add(new Coord(266,619));
		CoordList.add(new Coord(279,307));
		CoordList.add(new Coord(351,197));
		CoordList.add(new Coord(444,368));
		CoordList.add(new Coord(443,577));
		CoordList.add(new Coord(448,526));
		CoordList.add(new Coord(228,146));
		CoordList.add(new Coord(245,46));
		CoordList.add(new Coord(448,147));
		
//		Calendar start = Calendar.getInstance();
//		start.set(2015, 11, 16, 11, 20, 0);
//		startTimeList.add(start);
//		
//		start.set(2015, 11, 17, 11, 20, 0);
//		startTimeList.add(start);
//
//		start.set(2015, 11, 18, 11, 48, 0);
//		startTimeList.add(start);
//		
//		start.set(2015, 11, 19, 11, 40, 0);
//		startTimeList.add(start);
//
//		
//		CoordList.add(new Coord(63,403));
//		CoordList.add(new Coord(251,369));
//		CoordList.add(new Coord(161,506));
//		CoordList.add(new Coord(265,330));
		
		
		
		// for(int i=0;i<startTimeList.size();i++)
		// System.out.println(FunctionUtility.CalendarToString(startTimeList.get(i)));
	}


	int findGroundtruth(ProbeRequestBrief prb){
			
		for(int i=0;i<startTimeList.size();i++){
			Calendar end = (Calendar) startTimeList.get(i).clone();
			end.add(Calendar.MINUTE, 3);
			if(!prb.getStartTime().before(startTimeList.get(i)) && !prb.getEndTime().after(end)){
				return i;
			}
		}
		return -1;

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

	public boolean IsInOrderingFoodArea(Coord point) {
		if (bgimage.getRGB(point.x, point.y) == -6694422) {
			// System.out.println("yes");
			return true;
		} else {
			// System.out.println("no"+bgimage.getRGB(point.x,point.y));
			return false;
		}
	}

	public void ColorMaping(int size) {
		// int[] values = new int[]{0, 32, 64, 96, 128, 160, 192, 224, 255};
		int[] values = new int[] { 0, 64, 160, 255 };
		SortedMap<Long, Color> colours = new TreeMap<>(Collections.reverseOrder());
		long k = 0;
		for (int r : values) {
			for (int g : values) {
				for (int b : values) {
					colours.put(k++, new Color(r, g, b));
				}
			}
		}
		k = 0;

		for (int i = 0; i < size; i++) {
			ClusterToColor.put(i, colours.get((k++) % 63));
		}
	}

	public void drawAP(Graphics g) {
		// draw AP place
		if(config.id.equals("biz")){
			drawOutlineCanteen(g);
		}else if(config.id.equals("lab")){
			drawOutlineLab(g);
		}

		for (int i = 0; i < Anchor.size(); i++) {
			drawPointWithLabel(g, Anchor.get(i).x, Anchor.get(i).y, "ap" + i, 2, Color.RED, 30, 20);
		}
	}

	public void drawOutlineLab(Graphics g) {
		// drawLine(g,37*mScale/10,0,107*mScale/10,0);
		// drawLine(g,107*mScale/10,128*mScale/10,107*mScale/10,0);
		// drawLine(g,107*mScale/10,128*mScale/10,0*mScale/10,128*mScale/10);
		// drawLine(g,0*mScale/10,61*mScale/10,0*mScale/10,128*mScale/10);
		// drawLine(g,0*mScale/10,61*mScale/10,37*mScale/10,61*mScale/10);
		// drawLine(g,37*mScale/10,0*mScale/10,37*mScale/10,61*mScale/10);
		//
		// drawLine(g,37*mScale/10,73*mScale/10,37*mScale/10,128*mScale/10);
		// drawLine(g,72*mScale/10,53*mScale/10,72*mScale/10,128*mScale/10);
		// drawLine(g,53*mScale/10,34*mScale/10,80*mScale/10,34*mScale/10);
		try {
			bgimage = ImageIO.read(new File("/home/honghande/Research/SocialProbe/ProbeBeacon/lab_pi.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		g.drawImage(bgimage, 100, 50, null);

	}

	public void drawOutlineCanteen(Graphics g) {
		// drawLine(g,0,0,0,986*mScale/20);
		// drawLine(g,0,0,581*mScale/20,0);
		// drawLine(g,581*mScale/20,0,581*mScale/20,986*mScale/20);
		// drawLine(g,0,986*mScale/20,581*mScale/20,986*mScale/20);
		//
		// drawLine(g,0,260*mScale/20,581*mScale/20,260*mScale/20);
		// drawLine(g,291*mScale/20,260*mScale/20,291*mScale/20,841*mScale/20);
		// drawLine(g,291*mScale/20,841*mScale/20,436*mScale/20,841*mScale/20);
		// drawLine(g,436*mScale/20,986*mScale/20,436*mScale/20,841*mScale/20);
		// drawLine(g,436*mScale/20,260*mScale/20,436*mScale/20,696*mScale/20);
		// drawLine(g,581*mScale/20,696*mScale/20,436*mScale/20,696*mScale/20);

		try {
			bgimage = ImageIO.read(new File("/home/honghande/Research/SocialProbe/ProbeBeacon/canteen.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		g.drawImage(bgimage, 100, 50, null);

	}

	// Draw a point at (x,y)
	public void drawPointWithLabel(Graphics g, int x, int y, String label, double scale, Color color, int xOffset,
			int yOffset) {
		x += 100;
		y += 50;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(color);
		int length = (int) (5 * scale);

		// Assume x, y, and diameter are instance variables.
		Ellipse2D.Double circle = new Ellipse2D.Double(x, y, length, length);
		g2d.fill(circle);

		g2d.setColor(Color.red);
		Font font = new Font("Arial", Font.BOLD, 13);
		g2d.setFont(font);
		g2d.drawString(label, x + xOffset, y + yOffset);

	}

	public static void drawCircleWithLabel(Graphics g, int x, int y, String label, double scale, Color color,
			int xOffset, int yOffset) {
		x += 100;
		y += 50;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(color);
		int length = (int) (5 * scale);

		// Assume x, y, and diameter are instance variables.
		// Ellipse2D.Double circle = new Ellipse2D.Double(x, y, length, length);
		// g2d.fill(circle);
		g2d.drawOval(x, y, length, length);

		g2d.setColor(Color.red);
		Font font = new Font("Arial", Font.BOLD, 13);
		g2d.setFont(font);
		g2d.drawString(label, x + xOffset, y + yOffset);
	}

	public void drawLine(Graphics g, int x1, int y1, int x2, int y2) {
		x1 += 100;
		y1 += 50;
		x2 += 100;
		y2 += 50;
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.blue);
		g2d.setStroke(new BasicStroke(2));
		g2d.drawLine((int) (x1), (int) (y1), (int) (x2), (int) (y2));
	}

	public void drawLine(Graphics g, int x1, int y1, int x2, int y2, int width) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.blue);
		width = width + 1;
		width = width > 7 ? 7 : width;
		g2d.setStroke(new BasicStroke(width));
		g2d.drawLine((int) (x1), (int) (y1), (int) (x2), (int) (y2));
	}

	public boolean GetIsDrawNode() {
		return IsDrawNode;
	}

	public void setIsDrawNode(boolean isDrawNode) {
		IsDrawNode = isDrawNode;
	}

	public void setIsLoadImage(boolean isLoadImage) {
		IsLoadImage = isLoadImage;
	}

	public void setIsIsKmeans(boolean isKmeans) {
		IsKmeans = isKmeans;
	}



}
