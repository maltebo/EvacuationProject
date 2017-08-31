package model.helper;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Malte on 28.08.2017.
 */
public class TestShow extends JPanel {

    Shape shape;

    public TestShow(Shape shape) {
        this.shape = shape;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.draw(shape);
    }


}
