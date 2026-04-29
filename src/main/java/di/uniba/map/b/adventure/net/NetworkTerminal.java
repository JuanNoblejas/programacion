package di.uniba.map.b.adventure.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkTerminal {

    private static final int PORT = 5555;
    private static Thread serverThread;

    public static void startServer() {
        if (serverThread != null && serverThread.isAlive()) return;
        
        serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Network server started on port " + PORT);
                while (!Thread.currentThread().isInterrupted()) {
                    try (Socket clientSocket = serverSocket.accept();
                         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                        out.println("TRANSMISSION RECEIVED — This is rescue fleet Artemis. We received your signal on frequency OMEGA-7. The unlock sequence for the Escape Module is: OMEGA-2847-ESCAPE. Repeat: OMEGA-2847-ESCAPE. Good luck.");
                    }
                }
            } catch (IOException e) {
                // Silently ignore server errors in the game context
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    public static String getMessageFromServer() {
        try (Socket socket = new Socket("localhost", PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            return in.readLine();
        } catch (IOException e) {
            return "Network error: Could not connect to the communications server.";
        }
    }
}
