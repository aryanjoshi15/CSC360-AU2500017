import javax.swing.*;
import java.awt.*;

public class SimpleRectangle extends JPanel {

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);

        // Corners of the rectangle
        int x = 300;       // left edge
        int y = 300;       // top edge
        int width = 300;
        int height = 300;

        // Top line: from top-left to top-right
        g.drawLine(x, y, x + width, y);

        // Right line: from top-right to bottom-right
        g.drawLine(x + width, y, x + width, y + height);

        // Bottom line: from bottom-right to bottom-left
        g.drawLine(x + width, y + height, x, y + height);

        // Left line: from bottom-left to top-left
        g.drawLine(x, y + height, x, y);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("My Rectangle");
        frame.add(new SimpleRectangle());
        frame.setSize(1000, 1000);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}


