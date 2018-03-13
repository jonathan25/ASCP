import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GUI {

    private JRadioButton serverRadioButton;
    private JRadioButton clientRadioButton;
    private JCheckBox encryptWithKeyCheckBox;
    private JTextField hostnameTextField;
    private JTextField portTextField;
    private JPasswordField passwordField;
    private JButton startButton;
    private JTextField messageTextField;
    JPanel mainPanel;
    private JPanel optionsPanel;
    private JPanel chatPanel;
    private JButton button2;
    private JTextArea chatTextArea;

    private Chat chat;

    public GUI() {


        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    int port = Integer.parseInt(portTextField.getText());
                    if (serverRadioButton.isSelected()) {
                        if (encryptWithKeyCheckBox.isSelected()) {
                            chat = new Chat(port, new String(passwordField.getPassword()), chatTextArea); //ignoring some security things
                            new Thread(chat).start();
                        } else {
                            chat = new Chat(port, chatTextArea);
                            new Thread(chat).start();
                        }
                    } else {
                        String hostname = hostnameTextField.getText();
                        if (encryptWithKeyCheckBox.isSelected()) {
                            chat = new Chat(hostname, port, new String(passwordField.getPassword()), chatTextArea);  //ignoring some security things
                            new Thread(chat).start();
                        } else {
                            chat = new Chat(hostname, port, chatTextArea);
                            new Thread(chat).start();
                        }
                    }
                    chatPanel.setVisible(true);


                } catch (Exception ex) {
                    //show message
                    ex.printStackTrace();
                }
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String message = messageTextField.getText();
                try {
                    chat.sendMessage(message);
                    chatTextArea.append("You: " + message + "\n");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
