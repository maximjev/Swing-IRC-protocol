package IRC.gui;

import IRC.controller.MainFrameController;
import IRC.controller.PrivateMessageController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PrivateMessageFrame extends JFrame {
    private JTextArea chatArea;
    private JPanel mainPanel;
    private JButton sendButton;
    private JTextField messageArea;

    private String user;
    private PrivateMessageController controller;

    public String getUser() {
        return user;
    }

    public PrivateMessageFrame(String user,PrivateMessageController controller) {
        this.controller = controller;
        this.user = user;
        setSize(new Dimension(450,330));
        this.setTitle(user);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        initComponents();
        setContentPane(mainPanel);
        initListeners();
    }

    public void setVisible(boolean vis){
        super.setVisible(vis);
    }
    private void initComponents() {
        chatArea.setEditable(false);
        chatArea.setMargin(new Insets(3,5,3,5));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
    }
    private void initListeners() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.removeChat();
                dispose();
            }
        });
        sendButton.addActionListener(e -> {
            controller.sendToUser(messageArea.getText());
            messageArea.setText(null);
            messageArea.setCaretPosition(0);
        });
        messageArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if(key == KeyEvent.VK_ENTER) {
                    controller.sendToUser(messageArea.getText());
                    messageArea.setCaretPosition(0);
                    messageArea.setText("");
                }
            }
        });
    }

    public void updateChat(String line) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        Date date = new Date();
        String time = sdf.format(date);
        chatArea.append("[" + time + "] " + line + "\n");
        chatArea.setCaretPosition(chatArea.getText().length());
    }
}
