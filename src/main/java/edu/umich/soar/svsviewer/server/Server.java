package edu.umich.soar.svsviewer.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javafx.concurrent.Task;

public class Server extends Task<Void> {

  private final int portNumber;

  private final Consumer<String> inputProcessor;

  @FunctionalInterface
  public interface ConnectionListener {
    void run();
  }

  private final List<ConnectionListener> onConnectedListeners = new ArrayList<>();

  public Server(int portNumber, Consumer<String> inputProcessor) {
    this.portNumber = portNumber;
    this.inputProcessor = inputProcessor;
  }

  public static void main(String[] args) {
    final Server server = new Server(12122, System.out::println);
    server.run();
  }

  /**
   * Continually waits for a connection to {@link #portNumber}.
   *
   * @return
   */
  @Override
  protected Void call() {
    while (true) {
      System.out.println("Listening on port " + portNumber + " for a connection...");
      try (ServerSocket serverSocket = new ServerSocket(portNumber);
          Socket clientSocket = serverSocket.accept();
          // TODO: use BufferedWriter instead so that exceptions aren't swallowed
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          BufferedReader in =
              new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); ) {
        System.out.println("Connection established");
        for (ConnectionListener l : onConnectedListeners) {
          l.run();
        }
        String inputLine;

        while (!isCancelled() && (inputLine = in.readLine()) != null) {
          inputProcessor.accept(inputLine);
          out.println("Received: " + inputLine);
        }
      } catch (IOException e) {
        System.out.println(e.getMessage());
      } finally {
        System.out.println("Server connection ended");
      }
    }
  }

  public void onConnected(ConnectionListener listener) {
    onConnectedListeners.add(listener);
  }
}
