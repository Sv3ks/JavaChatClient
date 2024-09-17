package main;

public class Main {
	public static void main(String[] args) {
		String input;
		while (true) {
			System.out.println("JavaChatClient");
			System.out.println("A - Join room");
			System.out.println("B - Create room");
			input = System.console().readLine();
			
			for(int i=0;i<10;i++) {
				System.out.println();
			}
			
			if (input.equalsIgnoreCase("A")) {
				System.out.println("Enter Room Address:");
				String[] address = System.console().readLine().split(":");
				String ip = address[0];
				int port = Integer.parseInt(address[1]);
				Client client = new Client(ip,port);
				client.run();
			}
			
			if (input.equalsIgnoreCase("B")) {
				System.out.println("Enter room port:");
				int port = Integer.parseInt(System.console().readLine());
				Server server = new Server(port);
				server.configureLogging(true, "log.txt");
				server.run();
			}
			
			for(int i=0;i<10;i++) {
				System.out.println();
			}
		}
	}
}
