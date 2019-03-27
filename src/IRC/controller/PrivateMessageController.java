package IRC.controller;

import IRC.gui.PrivateMessageFrame;

import javax.swing.*;

public class PrivateMessageController{
    private String user;
    private PrivateMessageFrame frame;
    private MainFrameController controller;

    public PrivateMessageController(String user, MainFrameController controller) {
        this.user = user;
        this.controller = controller;
        frame = new PrivateMessageFrame(user,this);
        frame.setVisible(true);
    }

    public String getUser() {
        return user;
    }
    public PrivateMessageFrame getPrivateMessageFrame() {
        return frame;
    }

    public void removeChat() {
        controller.removeChat(this);

    }
    public void sendToUser(String message) {
        controller.sendToUser(this,user,message);
    }
    public void updateChat(String message) {
        frame.updateChat(message);
    }
}
