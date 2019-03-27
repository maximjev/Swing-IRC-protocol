import IRC.IrcClient;
import IRC.controller.MainFrameController;
import IRC.gui.MainFrame;

import javax.swing.*;
import java.io.IOException;

public class Program {

    public static void main(String[] args) {

        MainFrame frame = new MainFrame(new MainFrameController(new IrcClient("irc.freenode.net","simple_bot")));
        frame.setVisible(true);
    }
}
