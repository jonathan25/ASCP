import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Chat implements Runnable {
    private int port;
    private String hostname;
    private boolean encrypted;
    private String key;
    private boolean server;

    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;
    private ServerSocket serverSocket;

    private JTextArea textArea;

    private boolean close = false;

    public Chat(int port, JTextArea textArea) throws Exception {
        this.port = port;
        this.server = true;
        encrypted = false;
        this.textArea = textArea;
        serverSocket = new ServerSocket(port);

    }

    public Chat(int port, String key, JTextArea textArea) throws Exception {
        this.port = port;
        this.server = true;
        encrypted = true;
        this.textArea = textArea;
        this.key = key;
        serverSocket = new ServerSocket(port);
    }

    public Chat(String hostname, int port, JTextArea textArea) throws Exception {
        encrypted = false;
        this.port = port;
        this.server = false;
        this.textArea = textArea;
        socket = new Socket(hostname, port);
    }

    public Chat(String hostname, int port, String key, JTextArea textArea) throws Exception {
        encrypted = true;
        this.port = port;
        this.server = false;
        this.textArea = textArea;
        this.key = key;
        socket = new Socket(hostname, port);
    }

    public void sendMessage(String message) throws Exception {
        byte[] data = DataStructure.getPlainMessage(message);
        if (encrypted) {
            outputStream.write(DataStructure.encryptData(data, key));
        } else {
            outputStream.write(data);
        }
    }

    public void stop() {
        close = true;
        try {
            if (server) {
                serverSocket.close();
            }
        } catch (Exception ex) {
            System.out.println("WARNING: Exception while trying to stop server socket.");
        }

        try {
            socket.close();
        } catch (Exception ex) {
            System.out.println("WARNING: Exception while trying to stop socket.");
        }
    }

    public void run() {
        if (server) {
            try {
                socket = serverSocket.accept();
            } catch (SocketException ex) {
                if (close) {
                    System.out.println("Server stopped correctly.");
                } else {
                    System.out.println("Error while accepting the connection");
                    ex.printStackTrace();
                    System.out.println("Thread stopped.");
                }
                return;
            } catch (Exception ex) {
                System.out.println("Error while accepting the connection");
                ex.printStackTrace();
                System.out.println("Thread stopped.");
                return;
            }
        }

        String address = null;

        try {
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            address = socket.getInetAddress().getHostAddress();
            textArea.append("Connected to: " + address + "\n");
        } catch (Exception ex) {
            System.out.println("Error getting streams");
            ex.printStackTrace();
            return;
        }

        while (true) {
            try {
                byte[] data = new byte[256];
                inputStream.read(data);

                String message;
                if (encrypted) {
                    message = DataStructure.getMessage(DataStructure.decryptData(data, key));
                } else {
                    message = DataStructure.getMessage(data);
                }

                textArea.append(address + ": " + message + "\n");

            } catch (SocketException ex) {
                if (close) {
                    System.out.println("Socket stopped correctly.");
                } else {
                    System.out.println("Error while receiving data.");
                    System.out.println("Thread stopped.");
                }
                return;
            } catch (Exception ex) {
                System.out.println("Error while receiving data.");
                System.out.println("Thread stopped.");
                return;
            }
        }
    }


}
