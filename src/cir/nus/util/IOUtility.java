package cir.nus.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import cir.nus.edu.DrawingPanel;
import cir.nus.edu.ProbeRequestStructure;

/**
*
* @author Hande
*/

import cir.nus.edu.ProbeRequestStructure.ProbeRequest;

public class IOUtility {

//	private static int Apnum = 8;
	private static int regionnum = 4;
//	static String path = "/home/honghande/ProbeBeacon/BizCanteenData/Plot/";
//	static String path = "/home/honghande/ProbeBeacon/labdata/TimeTablePlot/";
//	static String path = "/home/honghande/ProbeBeacon/PiLab/FigurePlot/";
	private static ArrayList<ArrayList<ProbeRequest>> mProbeRequestQueueOfAPS = new ArrayList<ArrayList<ProbeRequest>>();
	static HashMap<Integer, Vector<Double>> signalDB = new HashMap<Integer, Vector<Double>>();

	static public void WriteTimeTable(ArrayList<Integer> activityTable, Calendar s, String mac, int gap, String path) {
		Calendar cal = (Calendar) s.clone();
		File TimeWriter = new File(path + mac + ".txt");
		TimeWriter.setReadable(true);
		TimeWriter.setWritable(true);
		try {
			FileOutputStream fos = new FileOutputStream(TimeWriter);
			for (int i = 0; i < activityTable.size(); i++) {
				if (activityTable.get(i) == 1)
					fos.write((FunctionUtility.CalendarToString(cal) + ", " + activityTable.get(i) + "\n").getBytes());
				cal.add(Calendar.MINUTE, gap);
			}
			fos.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static public void WriteSignalData(ArrayList<ArrayList<ProbeRequest>> data, Calendar s, Calendar e, String mac, String path) {
		for (int i = 0; i < data.size(); i++) {
			File SignalWriter = new File(path + mac + "_ap" + i + ".txt");
			SignalWriter.setReadable(true);
			SignalWriter.setWritable(true);
			try {
				FileOutputStream fos = new FileOutputStream(SignalWriter);
				for (int j = 0; j < data.get(i).size(); j++) {
					if (data.get(i).get(j).getStartTime().after(s) && data.get(i).get(j).getEndTime().before(e))
						fos.write((FunctionUtility.CalendarToString(data.get(i).get(j).getStartTime()) + ", "
								+ data.get(i).get(j).getAveragerssiWithoutOutliar() + "\n").getBytes());
				}
				fos.close();

			} catch (Exception ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}

	}
	
	static public void WriteErrorBarData(ArrayList<String> MacList, HashMap<String,ArrayList<ArrayList<ProbeRequest>>> mMacToProbeData, Calendar s, Calendar e,int Apnum,String path) {
		File SignalWriter = new File(path + "errorBars.txt");
		SignalWriter.setReadable(true);
		SignalWriter.setWritable(true);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(SignalWriter);
			for (int i = 0; i < Apnum; i++) {
				String DataString="AP"+i;
				for(int j=0;j<MacList.size();j++){
					DataString += GetInfoForProbeRequestList(MacList.get(j),mMacToProbeData.get(MacList.get(j)).get(i),s,e);				
				}
				DataString +="\n";
				fos.write(DataString.getBytes());
			}
			fos.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	static  public String GetInfoForProbeRequestList(String mac, ArrayList<ProbeRequest> data, Calendar s, Calendar e){
		double sum = 0;
		double var = 0;
		double avg =0;
		int count =0;
		if(data.size()==0)
			return " -99 0";
		for(int i=0;i<data.size();i++){
			if(data.get(i).getStartTime().after(s)&&data.get(i).getEndTime().before(e)){
				sum+=data.get(i).getAveragerssiWithoutOutliar();
				count++;
			}
				
		}
		avg = sum/count;
		
		for(int i=0;i<data.size();i++){
			if(data.get(i).getStartTime().after(s)&&data.get(i).getEndTime().before(e)){
				var+=Math.pow((data.get(i).getAveragerssiWithoutOutliar()-avg),2);
			}
			
		}
		var /= count;
		return " "+avg+" "+Math.sqrt(var);
	}
	
	static public void GenerateErrorBarPlot(ArrayList<String> macList, DrawingPanel dp, String path) throws IOException {

			File FWriter = new File(path + "errorBar.plt");
			try {
				FileOutputStream fos = new FileOutputStream(FWriter);

				fos.write(("reset\n").getBytes());
				fos.write("set term png\n".getBytes());
				fos.write(("set out '" + path + "Siganl_ErrorBars.png'\n").getBytes());			
				fos.write("set style fill solid 0.3 noborder\n".getBytes());
				fos.write("set key horizontal Left reverse noenhanced autotitles nobox\n".getBytes());
				fos.write("set style histogram errorbars linewidth 2\n".getBytes());
				fos.write("set bars front\n".getBytes());
				fos.write("set style data histograms\n".getBytes());
				fos.write("set xlabel \" \"\n".getBytes());
				fos.write("set xtics rotate by -45\n".getBytes());
				fos.write(("set xlabel offset character 0, -1, 0\n").getBytes());
				fos.write(("set yrange [0:80]\n").getBytes());
				fos.write("set ylabel \"Signal Strength(dB)\" \n".getBytes());
				fos.write("set ytics ('-100' 0)\n".getBytes());
				fos.write("set for [i=-100:0:20] ytics add (sprintf('%d', i) i + 100)\n".getBytes());
				fos.write(("plot \\\n").getBytes());
				
				fos.write(("'"+path+"errorBars.txt' using ($2+100):3:xtic(1) t \""+macList.get(0)+"\", \\\n").getBytes());
				
				for (int i = 2; i < macList.size(); i++) {
					fos.write(("'' using ($"+i*2+"+100):"+(i*2+1)+":xtic(1) t \""+macList.get(i-1)+"\", \\\n").getBytes());
				}

				fos.write(("'' using ($"+macList.size()*2+"+100):"+(macList.size()*2+1)+":xtic(1) t \""+macList.get(macList.size()-1)+"\" \n").getBytes());
				fos.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		dp.image.clear();
		Process pr = Runtime.getRuntime().exec("gnuplot " + path + "errorBar.plt");
		System.out.println("gnuplot " + path + "errorBar.plt");

		try {
			Thread.sleep(1000);
			dp.image.add(ImageIO.read(new File(path + "Siganl_ErrorBars.png")));
		} catch (IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		dp.revalidate();
		dp.repaint();
	}

	static public void GenerateSignalPlot(ArrayList<String> macList, DrawingPanel dp, int Apnum, String path) throws IOException {
		for (int m = 0; m < macList.size(); m++) {
			File FWriter = new File(path + "plot_signal_" + macList.get(m) + ".plt");
			try {
				FileOutputStream fos = new FileOutputStream(FWriter);

				fos.write(("set border 3\n").getBytes());
				fos.write("set term png\n".getBytes());
				fos.write(("set out '" + path + "signal_" + macList.get(m) + ".png'\n").getBytes());
				fos.write("set xdata time\n".getBytes());
				fos.write("set key font \",18\"\n".getBytes());
				fos.write("set key spacing 4\n".getBytes());
				fos.write("set key above\n".getBytes());
				fos.write("set datafile separator \",\"\n".getBytes());
				fos.write("set timefmt \"%Y-%m-%d %H:%M:%S\"\n".getBytes());
				fos.write("set format x \"%m-%d\\n%H:%M\"\n".getBytes());
				fos.write(("set title \"" + macList.get(m) + "\"\n").getBytes());
				fos.write(("set yrange[-99:-20]\n").getBytes());
				fos.write("set ylabel \"RSSI(db)\" font \",25\"\n".getBytes());
				fos.write("set xlabel \"time\" font \",25\"\n".getBytes());
				fos.write(("plot ").getBytes());
				for (int i = 0; i < Apnum - 1; i++) {
					fos.write(("'" + path + macList.get(m) + "_ap" + i + ".txt'  using 1:2 with points t 'AP-" + i
							+ "',\\\n").getBytes());
				}

				fos.write(("'" + path + macList.get(m) + "_ap" + (Apnum - 1) + ".txt'  using 1:2 with points t 'AP-"
						+ (Apnum - 1) + "'\n").getBytes());
				fos.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dp.image.clear();
		for (int i = 0; i < macList.size(); i++) {
			Process pr = Runtime.getRuntime().exec("gnuplot " + path + "plot_signal_" + macList.get(i) + ".plt");
			System.out.println("gnuplot " + path + "signal_" + macList.get(i) + ".plt");

			try {
				Thread.sleep(1000);
				dp.image.add(ImageIO.read(new File(path + "signal_" + macList.get(i) + ".png")));
			} catch (IOException | InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		dp.revalidate();
		dp.repaint();
	}

	static public void GeneratTimePlot(ArrayList<String> mac, DrawingPanel dp, String path) throws IOException {

		File TimeWriter = new File(path + "plot.plt");
		// File bash = new File(path+"run.sh");

		try {
			FileOutputStream fos = new FileOutputStream(TimeWriter);
			// FileOutputStream bos = new FileOutputStream(bash);
			fos.write(("set border 3\n").getBytes());
			fos.write("set term png \n".getBytes());
			fos.write(("set out '" + path + "timetable_hande.png'\n").getBytes());
			fos.write("set xdata time\n".getBytes());
			fos.write("set key font \",18\"\n".getBytes());
			fos.write("set key spacing 4\n".getBytes());
			fos.write("set key above\n".getBytes());
			fos.write("set datafile separator \",\"\n".getBytes());
			fos.write("set timefmt \"%Y-%m-%d %H:%M:%S\"\n".getBytes());
			fos.write("set format x \"%m-%d\\n%H:%M\"\n".getBytes());
			// fos.write("set xrange [\"08:00\":\"22:00\"]".getBytes());
			fos.write(("set yrange[0:" + (mac.size() + 3) + "]\n").getBytes());
			fos.write("set ylabel \"status\" font \",25\"\n".getBytes());
			fos.write("set xlabel \"time\" font \",25\"\n".getBytes());
			fos.write(("plot ").getBytes());
			if (mac.size() > 1) {
				for (int i = 0; i < mac.size() - 1; i++) {
					fos.write(("'" + path + mac.get(i) + ".txt'  using 1:($2+" + (i + 1) + ") with points t '"
							+ mac.get(i) + "'pt 5,\\\n").getBytes());
				}
			}
			fos.write(("'" + path + mac.get(mac.size() - 1) + ".txt'  using 1:($2+" + mac.size() + ") with points t '"
					+ mac.get(mac.size() - 1) + "' pt 5 \n").getBytes());

			// fos.write("".getBytes());
			// fos.write("".getBytes());

			fos.close();
			// bos.write("gnuplot plot.plt".getBytes());
			// bos.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// bash.setExecutable(true);
		// String[] cmd = new String[]{"/bin/sh", path+"run.sh"};
		// System.out.println(cmd[0]+" "+cmd[1]);
		Process pr = Runtime.getRuntime().exec("gnuplot " + path + "plot.plt");
		// new ProcessBuilder(path+"run.sh").start();
		// runScript mrunscript = new runScript();
		// mrunscript.runTheScript("gnuplot "+path+"plot.plt");

		try {
			Thread.sleep(1000);
			dp.image.clear();
			dp.image.add(ImageIO.read(new File(path + "timetable_hande.png")));
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		dp.revalidate();
		dp.repaint();
	}

	static public void ProcessLocationFromFile(int Apnum, String path) {
		for (int j = 1; j <= Apnum; j++) {
			String filename = "ap" + j + ".txt";
			ArrayList<ProbeRequest> mProbeRequestList = new ArrayList<ProbeRequest>();
			mProbeRequestList = IOUtility.ReadDataFromSingleFile(path + filename);
			mProbeRequestQueueOfAPS.add(mProbeRequestList);
		}

		int[] spottedRssi = { 0, 0, 0 };
		while (mProbeRequestQueueOfAPS.get(0).size() > 0 && mProbeRequestQueueOfAPS.get(1).size() > 0
				&& mProbeRequestQueueOfAPS.get(2).size() > 0) {
			int index = FunctionUtility.GetEarliestTimeIndex(mProbeRequestQueueOfAPS.get(0).get(0),
					mProbeRequestQueueOfAPS.get(1).get(0), mProbeRequestQueueOfAPS.get(2).get(0));
			// int index
			// =GetEarliestTimeIndex(mProbeRequestQueueOfAPS.get(0).get(0),
			// mProbeRequestQueueOfAPS.get(1).get(0))-1;
			spottedRssi[index] = mProbeRequestQueueOfAPS.get(index).get(0).getAveragerssiWithoutOutliar();
			for (int i = 0; i < Apnum && i != index; i++) {
				if (mProbeRequestQueueOfAPS.get(index).get(0).IsInSameStaying(mProbeRequestQueueOfAPS.get(i).get(0),10)) {
					spottedRssi[i] = mProbeRequestQueueOfAPS.get(i).get(0).getAveragerssiWithoutOutliar();
					mProbeRequestQueueOfAPS.get(i).remove(0);
				}
			}
			Calendar tc = mProbeRequestQueueOfAPS.get(index).get(0).getStartTime();
			mProbeRequestQueueOfAPS.get(index).remove(0);
			Vector<Integer> spotted = new Vector<Integer>();
			for (int j = 0; j < Apnum; j++) {
				spotted.add(spottedRssi[j]);
			}

			System.out.println(spotted.toString() + " " + FunctionUtility.CalendarToString(tc) + ", "
					+ FunctionUtility.GetLocation(spotted, signalDB));
			// System.out.println(CalendarToString(tc)+",
			// "+GetLocation(spotted));
		}
	}

	static public void BuildDatabase(int Apnum, String path) {

		File wifiDBWriter = new File(path + "wifiDBWithoutOutliar.txt");
		try {
			FileOutputStream fos = new FileOutputStream(wifiDBWriter);

			for (int i = 1; i <= regionnum; i++) {
				for (int j = 1; j <= Apnum; j++) {
					String filename = "ap" + j + "-" + "r" + i + ".txt";
					System.out.println("===========" + filename + "==========");
					ArrayList<ProbeRequest> mProbeRequestList = new ArrayList<ProbeRequest>();
					mProbeRequestList = IOUtility.ReadDataFromSingleFile(path + filename);

					int n = 0, sum = 0;
					double dev = 0;
					for (int k = 0; k < mProbeRequestList.size(); k++) {
						n += mProbeRequestList.get(k).getRssi().size();
						sum += mProbeRequestList.get(k).getAveragerssiWithoutOutliar()
								* mProbeRequestList.get(k).getRssi().size();
						dev += Math.pow(mProbeRequestList.get(k).getDeviationrssiWithoutOutliar(), 2)
								* mProbeRequestList.get(k).getRssi().size();
					}
					fos.write((n + " " + sum / n + " " + Math.sqrt(dev / n) + "\n").getBytes());
					FunctionUtility.PrintInfo(mProbeRequestList);
				}

				fos.write("==========================\n".getBytes());
			}
			fos.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HashMap<Integer, Vector<Double>> ReadDBFromFile(String filename) {
		// Open the file
		HashMap<Integer, Vector<Double>> signalDB = new HashMap<Integer, Vector<Double>>();
		FileInputStream fstream;
		try {
			fstream = new FileInputStream("/home/honghande/ProbeBeacon/PiLab/" + filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;
			Vector<Double> aps = new Vector<Double>();
			int regionnum = 1;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				if (strLine.startsWith("=")) {
					if (signalDB == null) {
						signalDB = new HashMap<Integer, Vector<Double>>();
					}
					signalDB.put(regionnum, aps);
					aps = new Vector<Double>();
					regionnum++;
				} else {
					String[] tokens = strLine.split(" ");
					double rssi = Double.parseDouble(tokens[1]);
					aps.add(rssi);
				}
			}
			// Close the input stream
			br.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println(signalDB.toString());
		return signalDB;

	}

	public static HashMap<String, String> ReadOuiFromFile(String filename) {
		// Open the file
		HashMap<String, String> ouiDB = new HashMap<String, String>();
		FileInputStream fstream;
		ouiDB = new HashMap<String, String>();
		try {
			fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// System.out.println(strLine);
				String[] tokens = strLine.split(" ");
//				System.out.println(tokens[0]+" "+tokens[1]);
				ouiDB.put(tokens[0], tokens[1]);
			}

			// System.out.println(ouiDB.size());

			// Close the input stream
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ouiDB;
		// System.out.println(signalDB.toString());
	}

	public static HashMap<String, ArrayList<ArrayList<ProbeRequest>>> ReadFromFile(String folderName,HashMap<String, String> ouiDB, int Apnum) {
		HashMap<String, ArrayList<ArrayList<ProbeRequest>>> mMacToProbeData = new HashMap<String, ArrayList<ArrayList<ProbeRequest>>>();

		for (int index = 0; index < Apnum; index++) {
			String filename = "Ap" + (index) + ".txt";
			FileInputStream fstream;
			try {
				fstream = new FileInputStream("/home/honghande/ProbeBeacon/labdata/"+folderName+"/" + filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

				String strLine;

				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					if (strLine.contains("SA:") && !strLine.contains(" short ")) {

						// Print the content on the console
						String[] eProbeRequestString = strLine.split(" ");
						String msourceMAC = null;
						ProbeRequest tProbeRequest = null;
						int mRssi = 0;
						Calendar cal = Calendar.getInstance();

						if (eProbeRequestString[4].equalsIgnoreCase("bad-fcs")) {
							continue;
						} else if (eProbeRequestString[2].equalsIgnoreCase("short")) {
							msourceMAC = eProbeRequestString[17].substring(3);
							// construct ProbeRequest
							String[] edate = eProbeRequestString[0].split("-");
							String eTime = eProbeRequestString[1].substring(0, 8);
							String[] eTimeCut = eTime.split(":");
							cal.clear();
							cal.set(Integer.parseInt(edate[0]), Integer.parseInt(edate[1]), Integer.parseInt(edate[2]),
									Integer.parseInt(eTimeCut[0]), Integer.parseInt(eTimeCut[1]),
									Integer.parseInt(eTimeCut[2]));
							if (index == 0)
								cal.add(Calendar.SECOND, -82);
							else if (index == 1)
								cal.add(Calendar.MINUTE, -1);
							else {
								cal.add(Calendar.HOUR, 7);
								cal.add(Calendar.SECOND, 30);
							}

							mRssi = -1 * Integer.parseInt(eProbeRequestString[11].replaceAll("[\\D]", ""));
							// System.out.println(index+"
							// "+CalendarToString(cal)+" "+msourceMAC+"
							// "+mRssi);
							tProbeRequest = new ProbeRequest(cal, cal, msourceMAC, mRssi);
						} else if (eProbeRequestString.length > 17 && eProbeRequestString[17].equalsIgnoreCase("lon")) {
							msourceMAC = eProbeRequestString[25].substring(3);
							// construct ProbeRequest
							String[] edate = eProbeRequestString[0].split("-");
							String eTime = eProbeRequestString[1].substring(0, 8);
							String[] eTimeCut = eTime.split(":");
							cal.clear();
							cal.set(Integer.parseInt(edate[0]), Integer.parseInt(edate[1]), Integer.parseInt(edate[2]),
									Integer.parseInt(eTimeCut[0]), Integer.parseInt(eTimeCut[1]),
									Integer.parseInt(eTimeCut[2]));

							if (index == 0)
								cal.add(Calendar.SECOND, -82);
							else if (index == 1)
								cal.add(Calendar.MINUTE, -1);
							else {
								cal.add(Calendar.HOUR, 7);
								cal.add(Calendar.SECOND, 30);
							}

							mRssi = -1 * Integer.parseInt(eProbeRequestString[7].replaceAll("[\\D]", ""));
							// System.out.println(index+"
							// "+CalendarToString(cal)+" "+msourceMAC+"
							// "+mRssi);
							tProbeRequest = new ProbeRequest(cal, cal, msourceMAC, mRssi);
						} else {
							if (eProbeRequestString[13].equalsIgnoreCase("BSSID:Broadcast")
									&& eProbeRequestString[15].contains("SA:")) {
								msourceMAC = eProbeRequestString[15].substring(3);
								// System.out.println((index+1)+" "+msourceMAC);
							} else {

								for (int i = 8; i < eProbeRequestString.length; i++) {
									if (eProbeRequestString[i].contains("SA:")) {
										msourceMAC = eProbeRequestString[i].substring(3);
									}
								}

							}
							// construct ProbeRequest
							String[] edate = eProbeRequestString[0].split("-");
							String eTime = eProbeRequestString[1].substring(0, 8);
							String[] eTimeCut = eTime.split(":");
							cal.clear();
							cal.set(Integer.parseInt(edate[0]), Integer.parseInt(edate[1]), Integer.parseInt(edate[2]),
									Integer.parseInt(eTimeCut[0]), Integer.parseInt(eTimeCut[1]),
									Integer.parseInt(eTimeCut[2]));

							if (index == 0)
								cal.add(Calendar.SECOND, -82);
							else if (index == 1)
								cal.add(Calendar.MINUTE, -1);
							else {
								cal.add(Calendar.HOUR, 7);
								cal.add(Calendar.SECOND, 30);
							}
							// System.out.println(index+"
							// "+CalendarToString(cal)+msourceMAC);
							mRssi = -1 * Integer.parseInt(eProbeRequestString[9].replaceAll("[\\D]", ""));
							// System.out.println(index+"
							// "+CalendarToString(cal)+msourceMAC+" "+mRssi );
							tProbeRequest = new ProbeRequest(cal, cal, msourceMAC, mRssi);
						}

						String prefix = msourceMAC.substring(0, 8);

						if (ouiDB.containsKey(prefix.toUpperCase())) {
							// check existence add ProbeRequest to map
							if (!mMacToProbeData.containsKey(msourceMAC)) {
								if (index > 0) {
									ArrayList<ArrayList<ProbeRequest>> tProbeRequestQueueOfAPS = new ArrayList<ArrayList<ProbeRequest>>();
									ArrayList<ProbeRequest> tProbeRequestList = new ArrayList<ProbeRequest>();
									tProbeRequestQueueOfAPS.add(tProbeRequestList);
									mMacToProbeData.put(msourceMAC, tProbeRequestQueueOfAPS);

									while (mMacToProbeData.get(msourceMAC).size() < index) {
										ArrayList<ProbeRequest> kProbeRequestList = new ArrayList<ProbeRequest>();
										mMacToProbeData.get(msourceMAC).add(kProbeRequestList);
									}
									ArrayList<ProbeRequest> kProbeRequestList = new ArrayList<ProbeRequest>();
									kProbeRequestList.add(tProbeRequest);
									mMacToProbeData.get(msourceMAC).add(kProbeRequestList);

								} else {
									ArrayList<ArrayList<ProbeRequest>> tProbeRequestQueueOfAPS = new ArrayList<ArrayList<ProbeRequest>>();
									ArrayList<ProbeRequest> tProbeRequestList = new ArrayList<ProbeRequest>();
									tProbeRequestList.add(tProbeRequest);
									tProbeRequestQueueOfAPS.add(tProbeRequestList);
									mMacToProbeData.put(msourceMAC, tProbeRequestQueueOfAPS);
								}

							} else {
								// new ap data
								if (mMacToProbeData.get(msourceMAC).size() < index + 1) {
									while (mMacToProbeData.get(msourceMAC).size() < index) {
										ArrayList<ProbeRequest> tProbeRequestList = new ArrayList<ProbeRequest>();
										mMacToProbeData.get(msourceMAC).add(tProbeRequestList);
									}
									ArrayList<ProbeRequest> tProbeRequestList = new ArrayList<ProbeRequest>();
									tProbeRequestList.add(tProbeRequest);
									mMacToProbeData.get(msourceMAC).add(tProbeRequestList);
								} else {
									if (!tProbeRequest.IsInSameBurst(mMacToProbeData.get(msourceMAC).get(index)
											.get(mMacToProbeData.get(msourceMAC).get(index).size() - 1))) {
										mMacToProbeData.get(msourceMAC).get(index).add(tProbeRequest);
									} else {
										mMacToProbeData.get(msourceMAC).get(index)
												.get(mMacToProbeData.get(msourceMAC).get(index).size() - 1).getRssi()
												.add(mRssi);
										if (mMacToProbeData.get(msourceMAC).get(index)
												.get(mMacToProbeData.get(msourceMAC).get(index).size() - 1).getEndTime()
												.before(cal)) {
											mMacToProbeData.get(msourceMAC).get(index)
													.get(mMacToProbeData.get(msourceMAC).get(index).size() - 1)
													.setEndTime(cal);
										}
									}

								}

							}
						}

					}

				} // end while

				// Close the input stream
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mMacToProbeData;
	}
	
	public static HashMap<String, ArrayList<ArrayList<ProbeRequest>>> ReadFromNewFormatFile(String folderName, Calendar start,Calendar end,int Apnum, String HomeDir) {
		
		HashMap<String, ArrayList<ArrayList<ProbeRequest>>> mMacToProbeData = new HashMap<String, ArrayList<ArrayList<ProbeRequest>>>();

		for (int index = 0; index < Apnum; index++) {//ap0.txt, ap1.txt. ap2.txt....
			String filename = "Ap" + index + ".txt";
			FileInputStream fstream;
			try {
				fstream = new FileInputStream(HomeDir+folderName+"/" + filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

				String strLine;

				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
						// Print the content on the console
						String[] eProbeRequestString = strLine.split(" ");
						String msourceMAC = null;
						ProbeRequest tProbeRequest = null;
						int mRssi = 0;
						Calendar cal = Calendar.getInstance();
						//parse data from line
						String[] edate = eProbeRequestString[1].split("-");
						String eTime = eProbeRequestString[2].substring(0, 8);
						String[] eTimeCut = eTime.split(":");
						cal.clear();
						cal.set(Integer.parseInt(edate[0]), Integer.parseInt(edate[1]), Integer.parseInt(edate[2]),
								Integer.parseInt(eTimeCut[0]), Integer.parseInt(eTimeCut[1]),
								Integer.parseInt(eTimeCut[2]));
						
						msourceMAC = eProbeRequestString[3];
						mRssi = Integer.parseInt(eProbeRequestString[4]);
						tProbeRequest = new ProbeRequest(cal, cal, msourceMAC, mRssi);

						if(mRssi>-100&&mRssi<-20&&cal.after(start)&&cal.before(end)){
							// new data
							if (!mMacToProbeData.containsKey(msourceMAC)) {//not contain mac address
								
								ArrayList<ArrayList<ProbeRequest>> tProbeRequestQueueOfAPS = new ArrayList<ArrayList<ProbeRequest>>();
								ArrayList<ProbeRequest> tProbeRequestList = new ArrayList<ProbeRequest>();
								
								if (index > 0) { //index is AP number
									//add mac to list 								
									tProbeRequestQueueOfAPS.add(tProbeRequestList);
									mMacToProbeData.put(msourceMAC, tProbeRequestQueueOfAPS);
									
									//begin from size =1
									while (mMacToProbeData.get(msourceMAC).size() < index) { //handle case  Ap3, Ap4.... the missing Ap1 and Ap2
										ArrayList<ProbeRequest> kProbeRequestList = new ArrayList<ProbeRequest>();
										mMacToProbeData.get(msourceMAC).add(kProbeRequestList);
									}
									ArrayList<ProbeRequest> kProbeRequestList = new ArrayList<ProbeRequest>();
									kProbeRequestList.add(tProbeRequest);
									mMacToProbeData.get(msourceMAC).add(kProbeRequestList);

								} else {// index =0 first time, we create the data							
									tProbeRequestList.add(tProbeRequest);
									tProbeRequestQueueOfAPS.add(tProbeRequestList);
									mMacToProbeData.put(msourceMAC, tProbeRequestQueueOfAPS);
								}

							} else {
								// check existence add ProbeRequest to map
								//check whether there are some ap miss the data
								if (mMacToProbeData.get(msourceMAC).size() < index + 1) {//first data from this AP
									ArrayList<ProbeRequest> tProbeRequestList = new ArrayList<ProbeRequest>();
									tProbeRequestList.add(tProbeRequest);
									mMacToProbeData.get(msourceMAC).add(tProbeRequestList);
								} else {//not the first data from this AP
									int lastIndex = mMacToProbeData.get(msourceMAC).get(index).size() - 1;//index of last element
									if (!tProbeRequest.IsInSameBurst(mMacToProbeData.get(msourceMAC).get(index).get(lastIndex))) {
										//compare with last element within 2 second period
										mMacToProbeData.get(msourceMAC).get(index).add(tProbeRequest);
									} else {//within 2 second period
										mMacToProbeData.get(msourceMAC).get(index).get(lastIndex).getRssi().add(mRssi); //add to rssi vector
										if (mMacToProbeData.get(msourceMAC).get(index).get(lastIndex).getEndTime().before(cal)) {
											mMacToProbeData.get(msourceMAC).get(index).get(lastIndex).setEndTime(cal);
										}
									}
								}
							}
						} // in reasonable rssi range
				} // end while

				// Close the input stream
				br.close();
				fstream.close();
				
				//make up for the missing one
				for(String m: mMacToProbeData.keySet()){
					if(mMacToProbeData.get(m).size()<index+1){
						ArrayList<ProbeRequest> tProbeRequestList = new ArrayList<ProbeRequest>();
						mMacToProbeData.get(m).add(tProbeRequestList);
					}
				}
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return mMacToProbeData;
	}

	public static ArrayList<ProbeRequest> ReadDataFromSingleFile(String filename) {
		// Open the file
		ArrayList<ProbeRequest> mProbeRequestList = new ArrayList<ProbeRequest>();
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				String[] eProbeRequestString = strLine.split(" ");
				String msourceMAC = eProbeRequestString[13].substring(3);
				if (msourceMAC.compareTo("cc:fa:00:c7:c9:a2") == 0) {
					String[] edate = eProbeRequestString[0].split("-");
					String eTime = eProbeRequestString[1].substring(0, 8);
					String[] eTimeCut = eTime.split(":");
					Calendar cal = Calendar.getInstance();
					cal.clear();
					cal.set(Integer.parseInt(edate[0]), Integer.parseInt(edate[1]), Integer.parseInt(edate[2]),
							Integer.parseInt(eTimeCut[0]), Integer.parseInt(eTimeCut[1]),
							Integer.parseInt(eTimeCut[2]));

					int mRssi = -1 * Integer.parseInt(eProbeRequestString[7].replaceAll("[\\D]", ""));
					ProbeRequest tProbeRequest = new ProbeRequest(cal, cal, msourceMAC, mRssi);
					if (mProbeRequestList.size() == 0) {
						mProbeRequestList.add(tProbeRequest);
						// System.out.println (mProbeRequestList.size()+"
						// "+strLine);
					} else if (!tProbeRequest.IsInSameBurst(mProbeRequestList.get(mProbeRequestList.size() - 1))) {
						mProbeRequestList.add(tProbeRequest);
						// System.out.println (mProbeRequestList.size()+"
						// "+strLine);
					} else {
						mProbeRequestList.get(mProbeRequestList.size() - 1).getRssi().add(mRssi);
						if (mProbeRequestList.get(mProbeRequestList.size() - 1).getEndTime().before(cal)) {
							mProbeRequestList.get(mProbeRequestList.size() - 1).setEndTime(cal);
						}
					}
				}

			}

			// Close the input stream
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mProbeRequestList;

	}

}
