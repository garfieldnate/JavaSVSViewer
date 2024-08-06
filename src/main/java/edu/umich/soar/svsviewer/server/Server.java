package edu.umich.soar.svsviewer.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class Server extends Task<Void> {

  private final int portNumber;

  private final Consumer<String> inputProcessor;
  private final Consumer<String> showMessage;

  @FunctionalInterface
  public interface ConnectionListener {
    void run();
  }

  private final List<ConnectionListener> onConnectedListeners = new ArrayList<>();

  public Server(int portNumber, Consumer<String> inputProcessor, Consumer<String> showMessage) {
    this.portNumber = portNumber;
    this.inputProcessor = inputProcessor;
    // we run on a different thread, so messages need to be directed back to main thread
    this.showMessage = (s) -> Platform.runLater(() -> showMessage.accept(s));
  }

  public static void main(String[] args) {
    final Server server = new Server(12122, System.out::println, System.out::println);
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
      showMessage.accept("Listening on port " + portNumber + " for a connection...");
      try (ServerSocket serverSocket = new ServerSocket(portNumber);
          Socket clientSocket = serverSocket.accept();
          // TODO: use BufferedWriter instead so that exceptions aren't swallowed
          PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
          BufferedReader in =
              new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); ) {
        System.out.println("Connection established");
        showMessage.accept("Client connected");
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
        //  TODO: would prefer that this stay shown in the bottom corner of the pane, in case
        // the user forgets what to do next.
        //        showMessage.accept("Client disconnected. Reconnect with svs connect_viewer
        // 12122.");
        System.out.println("Server connection ended");
      }
    }
  }

  public void onConnected(ConnectionListener listener) {
    onConnectedListeners.add(listener);
  }
}
