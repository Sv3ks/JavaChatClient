package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {
	private final String serverip;
	private final int serverport;
	private Socket s;
	private PrintWriter writer;
	private BufferedReader reader;
	private boolean closed = false;
	
	public static void main(String[] args) {
		while(true) {
			System.out.println("Enter Room Address:");
			String[] address = System.console().readLine().split(":");
			String ip = address[0];
			int port = Integer.parseInt(address[1]);
			
			for(int i=0;i<100;i++) {
				System.out.println();
			}
			
			Client client = new Client(ip,port);
			client.run();
			
			for(int i=0;i<100;i++) {
				System.out.println();
			}
		}
	}
	
	public Client(String serverip, int serverport) {
		this.serverip = serverip;
		this.serverport = serverport;
	}

	@Override
	public void run() {
		try {
			s = new Socket(serverip, serverport);
			System.out.println("Joined room.");
			writer = new PrintWriter(s.getOutputStream(),true);
			reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			
			new Thread(() -> {
				try {
					while(true) {
						System.out.println(reader.readLine());
					}
				} catch (IOException e) {
					//e.printStackTrace();
					System.out.println("Room closed. Press enter to continue.");
					closed = true;
					return;
				}
			}).start();
			
			while(!closed) {
				String input = System.console().readLine();
				writer.println(input);
				//System.out.println("[You]: "+input);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
