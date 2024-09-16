package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
	private static List<ClientHandler> clients = new ArrayList<>();
	private final int port;
	private ServerSocket s;
	
	public Server(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		try {
			s = new ServerSocket(port);
			System.out.println("Room opened at " + InetAddress.getLocalHost().getHostAddress() + ":" + port + ".");
			
			while (true) {
				Socket client = s.accept();
				System.out.println("User joined at " + s.getInetAddress().getHostAddress());
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
					System.out.println(message);
					
					for (ClientHandler handler : clients) {
						if(handler!=this) {
							handler.send(message);
						}
					}
				}
			} catch (IOException e) {
				//e.printStackTrace();
				System.out.println(name+" disconnected.");
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
