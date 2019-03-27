package IRC.gui;

import IRC.controller.MainFrameController;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainFrame extends JFrame{
    private JLabel usernameLabel;
    private JButton connectButton;
    private JButton disconnectButton;
    private JList userslistArea;
    private JTextArea chatArea;
    private JButton sendButton;
    private JTextField messageArea;
    private JTextField usernameTextField;
    private JPanel mainPanel;
    private JLabel usersonlineLabel;
    private JButton changeUsernameButton;
    private JLabel channelLabel;
    private JTextField channelTextField;

    private DefaultListModel<String> userListModel;


    private MainFrameController controller;
    public MainFrame(MainFrameController controller) {


        this.controller = controller;
        controller.setFrame(this);
        setTitle("SwIrc client");
        setSize(1200,600);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        initComponents();
        setContentPane(mainPanel);
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
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

        userslistArea.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userslistArea.setLayoutOrientation(JList.VERTICAL);
        userListModel = new DefaultListModel<>();
        userslistArea.setModel(userListModel);
    }
    private void initListeners() {
        connectButton.addActionListener(e -> { controller.connect(usernameTextField.getText(),channelTextField.getText());});
        disconnectButton.addActionListener(e ->{controller.disconnect();});
        changeUsernameButton.addActionListener(e -> {controller.changeUsername(usernameTextField.getText());});
        sendButton.addActionListener(e -> {
            controller.sendToChannel(messageArea.getText());
            messageArea.setText(null);
            messageArea.setCaretPosition(0);
        });

        messageArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();
                if(key == KeyEvent.VK_ENTER) {
                    controller.sendToChannel(messageArea.getText());
                    messageArea.setText(null);
                    messageArea.setCaretPosition(0);
                }
            }
        });

        MouseListener mouseListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JList jlist = (JList)e.getSource();
                if(e.getClickCount() == 2) {
                    int index = jlist.locationToIndex(e.getPoint());
                    if(index >= 0) {
                        String name = (String)jlist.getModel().getElementAt(index);
                        controller.createPrivateFrame(name);
                    }
                }
            }
        };
        userslistArea.addMouseListener(mouseListener);
    }

    public void initUsersList(List<String> usersList) {

        for (int i = 0; i < usersList.size(); i++) {
            userListModel.addElement(usersList.get(i));
        }

        SwingUtilities.invokeLater(() -> {
            userslistArea.setModel(userListModel);
            userslistArea.updateUI();
        });



    }
    public void updateChat(String line) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String time = sdf.format(date);
        chatArea.append("[" + time + "] " + line + "\n");
        chatArea.setCaretPosition(chatArea.getText().length());
    }

    public void updateUsersList(String user,boolean insert) {
        SwingUtilities.invokeLater(() -> {
            if(insert) {
                userListModel.addElement(user);
            } else {
                userListModel.removeElement(user);
            }
        });
    }
    public void clearUI() {
        userListModel.clear();
        chatArea.setText("");
        messageArea.setText("");
    }
}