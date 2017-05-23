package cir.nus.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

public class BuildData {

//	private static String path = "/home/honghande/ProbeBeacon/BizCanteenData/DistanceMeasure/Ap4/cc:fa:00:c7:c9:a2/";
//	private static String path = "/home/honghande/ProbeBeacon/BizCanteenData/DistanceMeasure/Ap4/f4:09:d8:9c:4d:c6/";
	private static String path = "/home/honghande/ProbeBeacon/BizCanteenData/DistanceMeasure/Ap0/";
//	private static String path = "/home/honghande/ProbeBeacon/BizCanteenData/DistanceMeasure/Ap4/04:f7:e4:cd:1d:bf/";
//	private static String path = "/home/honghande/ProbeBeacon/BizCanteenData/DistanceMeasure/Ap4/f8:cf:c5:db:34:dd/";
	
	private static double[] DistanceList= {2.72,3.92,5.31,6.71,8.12,9.53,10.95,12.36,15.18,16.58,18.0,19.4,21.76,22.86,24.28,25.69};
//	private static double[] DistanceList={1.2,2.2,4.8,6.2,7.8,9.2,13.9,15.2,16.7,20.7,20.9,21.3,22,22.9,23.8,25.2,29.2};
	private static double[] RssiList =new double[DistanceList.length];
	private static String[] MacList = {"cc:fa:00:c7:c9:a2"};//,"f4:09:d8:9c:4d:c6","cc:fa:00:c7:c9:0c","f8:cf:c5:db:34:dd"};
	private static int SampleNum =14;
	public static void main(String[] args) {
		PrecessDatas();
	}

	public static void PrecessFiles() {
		try {

			for (int i = 0; i < 21; i++) {
				String filename = "table" + (i + 1) + ".txt";
				FileInputStream fstream = new FileInputStream(path + filename);
				BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
				File FileWriter = new File(path + "table" + (i + 1) + ".txt");
				FileWriter.setReadable(true);
				FileWriter.setWritable(true);
				FileOutputStream fos = new FileOutputStream(FileWriter);

				String strLine;
				while ((strLine = br.readLine()) != null) {
					// System.out.println(strLine);
					if (strLine.contains("SA:") && strLine.contains("dB ")) {
						// Print the content on the console
						String[] eProbeRequestString = strLine.split(" ");
						String msourceMAC = null;
						int mRssi = 0;
						Calendar cal = Calendar.getInstance();
						String[] edate = eProbeRequestString[0].split("-");
						String eTime = eProbeRequestString[1].substring(0, 8);
						String[] eTimeCut = eTime.split(":");
						cal.clear();
						cal.set(Integer.parseInt(edate[0]), Integer.parseInt(edate[1]), Integer.parseInt(edate[2]),
								Integer.parseInt(eTimeCut[0]), Integer.parseInt(eTimeCut[1]),
								Integer.parseInt(eTimeCut[2]));

						for (int j = 0; j < eProbeRequestString.length; j++) {
							if (eProbeRequestString[j].contains("SA:"))
								msourceMAC = eProbeRequestString[j].substring(3);
							if (eProbeRequestString[j].contains("dB"))
								mRssi = -1 * Integer.parseInt(eProbeRequestString[j].replaceAll("[\\D]", ""));
						}
						String info = CalendarToString(cal) + " " + msourceMAC + " " + mRssi + "\n";
						fos.write((info).getBytes());
						System.out.println(info);
					}
				}

				fos.close();
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static void PrecessDatas() {
		try {
			for(int str = 0;str<MacList.length;str++){
				for (int i = 0; i < SampleNum; i++) {
					String filename = "table" + (i + 1) + ".dat";
					FileInputStream fstream = new FileInputStream(path +MacList[str] +"/"+ filename);
					BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

					String strLine;
					ArrayList<Integer> RssiVec = new ArrayList<Integer>();
					while ((strLine = br.readLine()) != null) {
						// System.out.println(strLine);
						String[] eProbeRequestString = strLine.split(" ");
						Calendar cal = Calendar.getInstance();

						String[] edate = eProbeRequestString[0].split("-");
						String eTime = eProbeRequestString[1].substring(0, 8);
						String[] eTimeCut = eTime.split(":");
						cal.clear();
						cal.set(Integer.parseInt(edate[0]), Integer.parseInt(edate[1]), Integer.parseInt(edate[2]),
								Integer.parseInt(eTimeCut[0]), Integer.parseInt(eTimeCut[1]),
								Integer.parseInt(eTimeCut[2]));

						String msourceMAC = eProbeRequestString[2];
						int mRssi = Integer.parseInt(eProbeRequestString[3]);
						RssiVec.add(mRssi);

					}
					double avg = 0;
					double dev = 0;

					avg = getAveragerssi(RssiVec);

					for (int k = 0; k < RssiVec.size(); k++) {
						dev += (RssiVec.get(k) - avg) * (RssiVec.get(k) - avg);
					}
					dev = Math.sqrt(dev / RssiVec.size());
//					System.out.println(RssiVec.size() + " " +DistanceList[i]+" "+ getAveragerssi(RssiVec) + " " + getDeviationrssi(RssiVec)
//							+ " " + getAveragerssiWithoutOutliar(RssiVec) + " " + getDeviationrssiWithoutOutliar(RssiVec));
					RssiList[i] += getAveragerssiWithoutOutliar(RssiVec);
				}
			}

			
			
			double Denominator=0;
			double Numerator =0;
			double reference = 0;
			for(int j=1;j<SampleNum;j++){
				Denominator += 10*Math.log10(DistanceList[j]/DistanceList[0]);
				Numerator +=(RssiList[j]-RssiList[0])/MacList.length;
			}
			
			double n = -1.0*Numerator/Denominator;
			for(int i=0;i<SampleNum;i++)
				reference+=10*n*Math.log10(DistanceList[i])+RssiList[i]/MacList.length;
				System.out.println(n+" "+reference/SampleNum);
			
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public static double getAveragerssi(ArrayList<Integer> rssiList) {
		if (rssiList.size() > 0) {
			double sum = 0;
			for (int i = 0; i < rssiList.size(); i++) {
				sum += rssiList.get(i);
			}
			return sum / rssiList.size();
		} else
			return 0;
	}

	public static double getAveragerssiWithoutOutliar(ArrayList<Integer> rssiList) {
		double avg = getAveragerssi(rssiList);
		double dev = getDeviationrssi(rssiList);
		// System.out.println(rssiList.size()+" "+avg+" "+dev+"
		// "+rssiList.toString()+" "+sourceMAC+"
		// "+FunctionUtility.CalendarToString(stime));
		if (rssiList.size() > 0) {
			double sum = 0, cc = 0;
			for (int i = 0; i < rssiList.size(); i++) {
				if (Math.abs(rssiList.get(i) - avg) <= dev) {
					sum += rssiList.get(i);
					cc++;
				}

			}
			return sum / cc;
		} else
			return 0;
	}

	public static double getDeviationrssi(ArrayList<Integer> rssiList) {
		if (rssiList.size() > 0) {
			double avg = getAveragerssi(rssiList);

			double var = 0;
			for (int i = 0; i < rssiList.size(); i++) {
				var += (rssiList.get(i) - avg) * (rssiList.get(i) - avg);
			}
			return Math.sqrt((var / rssiList.size()));
		} else
			return 0;
	}

	public static double getDeviationrssiWithoutOutliar(ArrayList<Integer> rssiList) {
		if (rssiList.size() > 0) {
			double avg = getAveragerssiWithoutOutliar(rssiList);
			double dev = getDeviationrssi(rssiList);
			double var = 0;
			int cc = 0;
			for (int i = 0; i < rssiList.size(); i++) {
				if (Math.abs(rssiList.get(i) - avg) <= dev) {
					var += (rssiList.get(i) - avg) * (rssiList.get(i) - avg);
					cc++;
				}
			}
			return Math.sqrt((var / cc));
		} else
			return 0;
	}

	public static String CalendarToString(Calendar c) {
		return c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ ((c.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY))
				+ ":" + ((c.get(Calendar.MINUTE) < 10) ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)) + ":"
				+ ((c.get(Calendar.SECOND) < 10) ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND));
	}

	public static int GeneratePathLossExponent(){
		
		return 0;
	}
}
