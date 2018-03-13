import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private final int PORT = 2018;
    private ServerSocket serverSocket;
    private boolean running;
    private Runnable background;

    public void start() {
        new Thread(background).start();
    }

    public void stop() {
        throw new NotImplementedException();
    }

    public void sendMessage(String message) {
        //serverSocket
    }

    public Server() {
        running = true;
        Runnable background = () -> startServer();
    }

    private void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            Socket socket = serverSocket.accept(); //wait for client

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


            while (running) {

            }
        } catch (IOException exception) {
            running = false;
        }
    }

}
