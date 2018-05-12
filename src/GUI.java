import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI {
    private JFrame parent;
    private JRadioButton serverRadioButton;
    private JRadioButton clientRadioButton;
    private JCheckBox encryptWithKeyCheckBox;
    private JTextField hostnameTextField;
    private JTextField portTextField;
    private JTextField passwordField;
    private JButton startButton;
    JPanel mainPanel;
    private JPanel optionsPanel;
    private JPanel chatPanel;
    private JButton button2;
    private JTextArea chatTextArea;
    private JTextField messageTextField;

    private Chat chat;

    private Thread current;

    public GUI(JFrame parent) {
        this.parent = parent;

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (current != null) {
                    startButton.setText("Start");
                    chatPanel.setVisible(false);
                    chat.stop();
                    current = null;
                    parent.pack();
                    return;
                }
                try {
                    int port = Integer.parseInt(portTextField.getText());
                    if (serverRadioButton.isSelected()) {
                        if (encryptWithKeyCheckBox.isSelected()) {
                            chat = new Chat(port, passwordField, chatTextArea);
                        } else {
                            chat = new Chat(port, chatTextArea);
                        }
                    } else {
                        String hostname = hostnameTextField.getText();
                        if (encryptWithKeyCheckBox.isSelected()) {
                            chat = new Chat(hostname, port, passwordField, chatTextArea);
                        } else {
                            chat = new Chat(hostname, port, chatTextArea);
                        }
                    }
                    current = new Thread(chat);
                    current.start();

                    chatPanel.setVisible(true);
                    startButton.setText("Stop");
                    parent.pack();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "There was an error while trying to connect.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
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
