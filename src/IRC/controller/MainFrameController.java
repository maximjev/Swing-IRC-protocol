package IRC.controller;

import IRC.IrcClient;
import IRC.gui.MainFrame;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrameController {
    private IrcClient client;
    private MainFrame frame;

    private String username;

    private Map<String,PrivateMessageController> openedChats;

    public MainFrameController(IrcClient client) {
        openedChats = new HashMap<>();
        this.client = client;
        client.setController(this);
    }

    public void setFrame(MainFrame frame) {
        this.frame = frame;
    }

    public void passMessage(String line) {
        frame.updateChat(line);
    }
    public void passUsers(List<String> users) {
        users.sort(Comparator.naturalOrder());
        frame.initUsersList(users);
    }
    public void passUser(String user,boolean insert) {
        frame.updateUsersList(user,insert);
    }

    public void connect(String nick,String channel) {
        if(!verifyUserName(nick)) return;
        this.username = nick;

        client.setChannel(channel);
            Thread thread = new Thread(() ->{
                try {
                    client.connect(nick);
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
    }

    public void removeChat(PrivateMessageController controller) {
        openedChats.remove(controller.getUser());
    }

    public void passPrivateMessage(String user, String message) {
        if(openedChats.containsKey(user)) {
            openedChats.get(user).updateChat("<" + user + "> : " + message);
            return;
        }

        PrivateMessageController controller = new PrivateMessageController(user,this);
        controller.updateChat("<" + user + "> : " + message);
        openedChats.put(user,controller);

    }

    public void createPrivateFrame(String name) {
        PrivateMessageController controller = new PrivateMessageController(name,this);
        openedChats.put(name,controller);
        controller.getPrivateMessageFrame().setVisible(true);
    }
    public void sendToUser(PrivateMessageController controller,String name, String message) {
        if(message.isEmpty()) {
            controller.updateChat("Your message is empty");
        }
        if(!client.isConnected()) {
            controller.updateChat("You are not connected");
        }
        new Thread(() -> {
            try {
                client.sendToUser(name,message);
                controller.updateChat("<" +username + "> : " + message);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("sending to user failed");
            }
        }).start();

    }


    public void sendToChannel(String message) {
        if(message.isEmpty()) {
            frame.updateChat("Your message is empty");
            return;
        }
        if(!client.isConnected()) {
            frame.updateChat("You are not connected");
        }
        new Thread(() ->{ client.sendToChannel(message);}).start();
    }
    public void disconnect() {
        new Thread(() -> {
            try {
                client.disconnect();
                frame.clearUI();
                frame.updateChat("Disconnected from the server");

            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void clearUI() {
        new Thread(() -> {
                frame.clearUI();
        }).start();
    }
    private boolean verifyUserName(String name) {
        username = client.getNick();
        if(name.isEmpty()) {
            frame.updateChat("name is empty");
            return false;
        }
        if(name.length() > 15 ) {
            frame.updateChat("name can't be longer than 15 symbols");
            return false;
        }
        if(username == name) {
            frame.updateChat("You own this name already");
            return false;
        }
        return true;
    }

    public void changeUsername(String name) {
        if(!verifyUserName(name)) return;

        username = name;
        if(client.isConnected()) {
            Thread thread = new Thread(() -> {
                try {
                    client.setNick(name);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            thread.start();
        }
    }
}
