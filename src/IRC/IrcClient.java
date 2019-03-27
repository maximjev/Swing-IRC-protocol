package IRC;

import IRC.controller.MainFrameController;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcClient {
    private String server;
    private String nick;
    private String prevNick;
    private String login;
    private String channel;
    private boolean isConnected = false;
    private boolean switchedChannel = false;

    private BufferedReader reader;
    private BufferedWriter writer;

    private Socket socket;
    private MainFrameController controller;


    public IrcClient(String server, String login) {
        this.server = server;
        this.login = login;
    }

    public void setController(MainFrameController controller) {
        this.controller = controller;
    }


    public int openSocket(int port) throws IOException{
        socket = new Socket(server, port);

        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        writer.write("NICK " + nick + "\r\n");
        writer.write("USER " + login + " 8 * : Java IRC Hacks Bot\r\n");
        writer.flush();

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("004")) {
                break;
            } else if (line.contains("433")) {
                controller.passMessage("nick in use");
                return -1;
            }
        }
        isConnected = true;
        return 0;
    }

    public int connect(String nick) throws IOException {
        try {
            this.nick = nick;
            int error = 0;
            controller.clearUI();
            if(!isConnected) error = openSocket(6667);
            if(error < 0) return error;

            writer.write("JOIN " + channel + "\r\n");
            writer.flush();
            switchedChannel = true;

            new Thread(() -> inputStream()).start();
            return 0;

        } catch (Exception ex) {
            throw ex;
        }
    }

    public void inputStream() {
        String line;
        Pattern pattern;
        Matcher m;
        int code;
        switchedChannel = false;
        try {
            while ((line = reader.readLine()) != null) {
                if(!isConnected || switchedChannel) break;

                if(line.contains(nick) && Pattern.matches(".+[ ]\\d{3}[ ].+",line)) {
                    //System.out.println("matched\n");
                    pattern = Pattern.compile("\\d{3}");
                    m = pattern.matcher(line);
                    m.find();
                    code = Integer.parseInt(m.group());
                    //System.out.println("code " +code + "\n");

                    switch (code) {
                        case 5:
                        case 333:
                            break;
                        case 250:
                        case 251:
                        case 252:
                        case 253:
                        case 254:
                        case 255:
                        case 265:
                        case 266:
                        case 332:
                            controller.passMessage(cropLine(line));
                            break;
                        case 353:
                            parseUsers(line);
                            break;
                        case 375:
                            parseMotDLines(line);
                            break;
                        case 433:
                            nickInUse(line);
                            break;
                        default:
                            //System.out.println("not known code\n");
                            break;
                    }
                } else {
                    if (line.contains("PRIVMSG " + channel)) {  // handling messages to channel
                        parseUserMessage(line);
                    }
                    if(line.contains("PRIVMSG " + nick)) {
                        parsePrivateMessage(line);
                    }
                    if (line.contains("PING ")) { // pinging
                        ping(line);
                    }
                    if (line.contains("JOIN " + channel)) { // handling join
                        parseJoinedUser(line);
                    }
                    if (line.contains("QUIT ")) {  // handling quiting
                        parseQuitUser(line);
                    }
                    if(line.contains("NICK ")) {  //handling nick chang
                        parseNickChange(line);
                    }
                    if(line.contains("KICK " + channel)) {
                        parseKickedUser(line);
                    }
                }
                //System.out.println(line);
                //controller.passMessage(line);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void parsePrivateMessage(String line) {
        String nick = line.substring(1,line.indexOf("!"));
        String message = line.substring(line.indexOf(this.nick) + this.nick.length() +2);
        controller.passPrivateMessage(nick,message);

    }
    private void parseKickedUser(String line) {
        String name = line.substring(line.indexOf(channel)+ channel.length() +1,line.indexOf(":")-1);
        controller.passUser(name,false);
    }

    private void parseNickChange(String line) {
        String oldName = line.substring(1,line.indexOf("!"));
        String newName = line.substring(line.indexOf("NICK") + 5);
        System.out.println(oldName + " --- " + newName);
        controller.passUser(oldName,false);
        controller.passUser(newName,true);
    }
    private void parseMotDLines(String line) throws IOException {
        do {
            controller.passMessage(cropLine(line));
            line = reader.readLine();
        } while(line.contains("372"));
    }

    private String cropLine(String line) {
        line = line.replaceAll(":.*:","");
        return line;

    }

    public void sendToChannel(String message) {
        try {
            writer.write("PRIVMSG " + channel + " :" + message + "\r\n");
            writer.flush();
            controller.passMessage("<" + nick + "> :" + message);
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    private void ping(String line) throws IOException {
        writer.write("PONG " + line.substring(5) + "\r\n");
        writer.flush();

    }

    private void parseUserMessage(String line) {
        String name = line.substring(1,line.indexOf("!"));
        String message = line.substring(line.indexOf(channel) + channel.length() + 2);
        controller.passMessage("<" + name + "> : " + message);
    }

    private void parseJoinedUser(String line) {
        if(line.contains(nick)) return;
        String name = line.substring(1,line.indexOf("!"));
        controller.passMessage("<" + name + "> " + "has joined " + channel);
        controller.passUser(name,true);
    }
    private void parseQuitUser(String line)  {
        String name = line.substring(1,line.indexOf("!"));
        controller.passMessage("<" + name + "> " + "has quit " + channel);
        controller.passUser(name,false);
    }
    private void parseUsers(String line) throws IOException{
        String[] nameArray;
        List<String> nameList = new ArrayList<>();
        do {
            String names = line.substring(line.indexOf(channel) + channel.length() + 2);
            nameArray = names.split(" ");
            for (int i = 0; i < nameArray.length; i++) {
                nameList.add(nameArray[i]);
            }
            line = reader.readLine();
        } while (line.contains(" 353 ")); // end of names
        controller.passUsers(nameList);
    }

    public void sendToUser(String name, String message) throws IOException {
        writer.write("PRIVMSG " + name + " :" + message + "\r\n");
        writer.flush();
    }
    public void disconnect() throws IOException {
        writer.write("QUIT " + "\r\n");
        writer.flush();
        isConnected = false;
    }
    public void isConnected(boolean flag) {
        isConnected = flag;
    }
    public boolean isConnected() {
        return isConnected;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) throws  IOException {
        writer.write("NICK " + nick + "\r\n");
        writer.flush();
        controller.passMessage("You have changed your name to <" + nick + ">");
        prevNick = this.nick;
        this.nick = nick;
    }

    private void nickInUse(String line) {
        this.nick = prevNick;
    }
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
