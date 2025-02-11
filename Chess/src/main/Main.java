package main;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Chess");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);

        GamePanel gp = new GamePanel();
        window.add(gp);
        window.pack();  // adjusts to the frame size

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gp.launchGame();
    }
}