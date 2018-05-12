import javax.swing.*;
import javax.xml.crypto.Data;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Chat implements Runnable {
    private int port;
    private String hostname;
    private boolean encrypted;
    private char[] key;
    private boolean server;
    private Variables internalVariables = new Variables(new BigInteger("2426697107"), null, new BigInteger("17123207"));
    private BigInteger q = new BigInteger("2426697107");
    private BigInteger a = new BigInteger("17123207");
    private BigInteger x;


    private InputStream inputStream;
    private OutputStream outputStream;
    private Socket socket;
    private ServerSocket serverSocket;

    private JTextField keyField;
    private JTextArea textArea;

    private boolean close = false;

    public Chat(int port, JTextArea textArea) throws Exception {
        this.port = port;
        this.server = true;
        encrypted = false; //10.34.47.35, 10.34.8.146
        this.textArea = textArea;
        serverSocket = new ServerSocket(port);

    }

    public Chat(int port, JTextField keyField, JTextArea textArea) throws Exception {
        this.port = port;
        this.server = true;
        encrypted = true;
        this.textArea = textArea;
        this.keyField = keyField;
        serverSocket = new ServerSocket(port);
    }

    public Chat(String hostname, int port, JTextArea textArea) throws Exception {
        encrypted = false;
        this.port = port;
        this.server = false;
        this.textArea = textArea;
        socket = new Socket(hostname, port);
    }

    public Chat(String hostname, int port, JTextField keyField, JTextArea textArea) throws Exception {
        encrypted = true;
        this.port = port;
        this.server = false;
        this.textArea = textArea;
        this.keyField = keyField;
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

        boolean isInitialization = true;
        while (true) {
            try {
                byte[] data = new byte[256];

                String message;
                if (isInitialization) {
                    x = DataStructure.randomBigInteger(q);
                    BigInteger publicY = DataStructure.fastExp(a, x, q);

                    data = DataStructure.getPlainMessage(DataStructure.createInitialization(q, a, publicY));
                    outputStream.write(data); //send initialization

                    inputStream.read(data); //wait for response
                    message = DataStructure.getMessage(data);
                    Variables receivedVariables = DataStructure.getVariables(message);

                    BigInteger k = DataStructure.fastExp(receivedVariables.y, x, q);

                    key = DataStructure.validateKey(k);
                    System.out.println(publicY);
                    System.out.println(key);

                    isInitialization = false;
                    if (encrypted) {
                        keyField.setText(new String(key));
                    }

                } else {
                    inputStream.read(data);
                    if (encrypted) {
                        System.out.println("key: " + new String(key));
                        message = DataStructure.getMessage(DataStructure.decryptData(data, key));
                    } else {
                        message = DataStructure.getMessage(data);
                    }
                    textArea.append(address + ": " + message + "\n");
                }


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
