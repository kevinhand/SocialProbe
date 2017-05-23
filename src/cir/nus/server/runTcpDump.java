package cir.nus.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class runTcpDump {

	private static boolean IsSendToServer = true;
	private static HashMap<String, String> ouiDB = new HashMap<String, String>();
	private static Socket socket = null;
	private static String macPi = "";

	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("sudo java runTcpDump IP_address Port wlanX");
		} else {
			macPi = getEtherMac();
			ouiDB = ReadOuiFromFile("/home/honghande/ProbeBeacon/smart_device.txt");
			runTcpDumpClient(args[0], Integer.parseInt(args[1]), args[2]);
		}
	}

	public static void runTcpDumpClient(String host, int port, String wlanInterface) {
		try {
			String Cmd = "/usr/sbin/tcpdump -tttt -i " + wlanInterface + " -e -s 256";
			ProcessBuilder mProcessBuilder = new ProcessBuilder("/bin/bash", "-c", Cmd);
			Process mprocess = mProcessBuilder.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(mprocess.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(mprocess.getErrorStream()));

			socket = new Socket(host, port);
			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);

			String strLine;
			while ((strLine = stdInput.readLine()) != null) {
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

					for (int i = 0; i < eProbeRequestString.length; i++) {
						if (eProbeRequestString[i].contains("SA:"))
							msourceMAC = eProbeRequestString[i].substring(3);
						if (eProbeRequestString[i].contains("dB"))
							mRssi = -1 * Integer.parseInt(eProbeRequestString[i].replaceAll("[\\D]", ""));
					}
					String info = macPi + " " + CalendarToString(cal) + " " + msourceMAC + " " + mRssi + "\n";
					String prefix = msourceMAC.substring(0, 8);

					if (ouiDB.containsKey(prefix.toUpperCase())) {
						if (IsSendToServer) {
							bw.write(info);
							bw.flush();
							System.out.println(info);
						}
					}
				}
			}

			while ((strLine = stdError.readLine()) != null) {
				System.out.println(strLine);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			// Closing the socket
			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String getEtherMac() {
		String command = "/sbin/ifconfig";

		String sOsName = System.getProperty("os.name");
		if (sOsName.startsWith("Windows")) {
			command = "ipconfig";
		} else {

			if ((sOsName.startsWith("Linux")) || (sOsName.startsWith("Mac")) || (sOsName.startsWith("HP-UX"))) {
				command = "/sbin/ifconfig";
			} else {
				System.out.println("The current operating system '" + sOsName + "' is not supported.");
			}
		}

		Pattern p = Pattern.compile("([a-fA-F0-9]{1,2}(-|:)){5}[a-fA-F0-9]{1,2}");
		String mac = null;
		try {
			Process pa = Runtime.getRuntime().exec(command);
			pa.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(pa.getInputStream()));

			String line;
			Matcher m;
			while ((line = reader.readLine()) != null) {

				m = p.matcher(line);

				if (!m.find())
					continue;
				line = m.group();
				break;

			}
			mac = line;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mac;
	}

	public static String CalendarToString(Calendar c) {
		return c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH) + " "
				+ ((c.get(Calendar.HOUR_OF_DAY) < 10) ? "0" + c.get(Calendar.HOUR_OF_DAY) : c.get(Calendar.HOUR_OF_DAY))
				+ ":" + ((c.get(Calendar.MINUTE) < 10) ? "0" + c.get(Calendar.MINUTE) : c.get(Calendar.MINUTE)) + ":"
				+ ((c.get(Calendar.SECOND) < 10) ? "0" + c.get(Calendar.SECOND) : c.get(Calendar.SECOND));
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
				// System.out.println(tokens[0]+" "+tokens[1]);
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

}
