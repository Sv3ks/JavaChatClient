package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
	private static List<ClientHandler> clients = new ArrayList<>();
	private final int port;
	private ServerSocket s;
	private boolean logging = false;
	private FileWriter logger;
	
	public Server(int port) {
		this.port = port;
	}

	public void configureLogging(boolean logging, String logPath) {
		this.logging = logging;
		if(logging) {
			try {
				new File(logPath).createNewFile();
				logger = new FileWriter(logPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String log(String message) {
		if(logging) {
			try {
				logger.write("["+LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))+"] "+message+"\n");
				logger.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return message;
	}
	
	@Override
	public void run() {
		try {
			s = new ServerSocket(port);
			System.out.println(log("Room opened at " + InetAddress.getLocalHost().getHostAddress() + ":" + port + "."));
			
			while (true) {
				Socket client = s.accept();
				System.out.println(log("User joined at " + s.getInetAddress().getHostAddress()));
				for (ClientHandler cHandler : clients) {
					cHandler.send(s.getInetAddress().getHostAddress()+" joined the room.");
				}
				
				ClientHandler handler = new ClientHandler(client);
				new Thread(handler).start();
				clients.add(handler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class ClientHandler implements Runnable {
		private PrintWriter writer;
		private BufferedReader reader;
		private String name;
		
		public ClientHandler(Socket s) {
			try {
				writer = new PrintWriter(s.getOutputStream(),true);
				reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			name = s.getInetAddress().getHostAddress()+":"+s.getPort();
		}

		@Override
		public void run() {
			try {
				while(true) {
					String input = reader.readLine();
					String message = "";
					if (input.startsWith("/")) {
						String[] command = input.split(" ");
						command[0] = command[0].replace("/", "");
						if (command[0].equalsIgnoreCase("name")) {
							message = name + " changed name to " + command[1];
							name = command[1];
							this.send(message);
						} else {
							this.send("Unknown command.");
							continue;
						}
					} else {
						message = "[" + name + "]: " + input;
					}
					System.out.println(log(message));
					
					for (ClientHandler handler : clients) {
						if(handler!=this) {
							handler.send(message);
						}
					}
				}
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println(log(name+" disconnected."));
				clients.remove(this);
				for (ClientHandler handler : clients) {
					handler.send(name+" disconnected.");
				}
			}
		}
		
		public void send(String message) {
			writer.println(message);
		}
	}
}
