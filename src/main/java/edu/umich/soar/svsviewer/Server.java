package edu.umich.soar.svsviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private final int portNumber;

	public Server(int portNumber) {
		this.portNumber = portNumber;
	}

	public void run() {
		try (
			ServerSocket serverSocket = new ServerSocket(portNumber);
			Socket clientSocket = serverSocket.accept();
			PrintWriter out =
				new PrintWriter(clientSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(
				new InputStreamReader(clientSocket.getInputStream()));
		) {
			System.out.println("Connection established");
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				String outputLine =  "Received: " + inputLine;
				System.out.println(outputLine);
				out.println(outputLine);
			}
		} catch (IOException e) {
			System.out.println("Exception caught when trying to listen on port "
				+ portNumber + " or listening for a connection");
			System.out.println(e.getMessage());
		}
	}

	public static void main(String[] args) {
		final Server server = new Server(12122);
		server.run();
	}
}
