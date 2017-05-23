package cir.nus.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class BufferedSocketServer {

	private static ServerSocket serverSocket;

	public static void main(String args[]) {

		if (args.length != 1) {
			System.out.println("sudo java Server port");
		} else {
			String path = "/home/honghande/CodeToAccuFind/";

			try {
				int port = Integer.parseInt(args[1]);
				File FileWriter = new File(path + "ServerRevData.txt");
				FileWriter.setReadable(true);
				FileWriter.setWritable(true);
				FileOutputStream fos = new FileOutputStream(FileWriter);

				serverSocket = new ServerSocket(port);
				System.out.println("Waiting for a connection on " + port);
				while (true) {
					Socket socket = serverSocket.accept();
					Thread rthread = new RevThread(socket, fos);
					rthread.start();
				}

			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				try {
					serverSocket.close();
				} catch (Exception e) {
				}
			}
		}
	}

}

class RevThread extends Thread {
	protected Socket socket;
	protected FileOutputStream fos;

	public RevThread(Socket clientSocket, FileOutputStream f) {
		this.socket = clientSocket;
		this.fos = f;
	}

	public void run() {
		String str;
		InputStream is;
		try {
			is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);

			while ((str = br.readLine()) != null) {
				System.out.println(str);
				fos.write((str + "\n").getBytes());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
