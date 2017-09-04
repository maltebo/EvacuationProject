package model.representation;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Malte on 11.08.2017.
 */
public class Window extends Canvas {

    private JFrame frame;
    private JPanel upperSide;
    private Canvas canvas;
    private Menu menu;
    private Component statistics;

    public Window(int width, int height, BuildingRepresentation building, Menu menu, Component statistics) {

        this.canvas = building;
        this.menu = menu;
        this.statistics = statistics;

        frame = new JFrame();

        frame.setSize(width, height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new GridLayout2(2,1, 6,6));
        upperSide = new JPanel(new GridLayout2(1,2,6,6));
        upperSide.setPreferredSize(new Dimension(1000, 700));
        frame.add(upperSide);


        this.canvas.setPreferredSize(new Dimension(700, 700));

        upperSide.add(this.canvas);

        menu.setPreferredSize(new Dimension(300,700));
        upperSide.add(menu);

        statistics.setPreferredSize(new Dimension(1000, 300));
        statistics.setBackground(Color.green);
        frame.add(statistics);

        frame.requestFocus();
        frame.setVisible(true);

    }

    public Window(int width, int height, Canvas canvas, Menu menu, Component c3) {

        this.canvas = canvas;
        this.menu = menu;
        this.statistics = statistics;

        frame = new JFrame();

        frame.setSize(width, height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new GridLayout2(2,1, 6,6));
        upperSide = new JPanel(new GridLayout2(1,2,6,6));
        upperSide.setPreferredSize(new Dimension(1000, 700));
        frame.add(upperSide);


        this.canvas.setPreferredSize(new Dimension(700, 700));

        upperSide.add(this.canvas);

        menu.setPreferredSize(new Dimension(300,700));
        upperSide.add(menu);

        statistics = new JPanel();
        statistics.setPreferredSize(new Dimension(1000, 300));
        statistics.setBackground(Color.green);
        frame.add(statistics);

        frame.requestFocus();
        frame.setVisible(true);

    }

    public void changeBuilding(Canvas canvas) {

        this.canvas = canvas;
        this.canvas.setPreferredSize(new Dimension(700, 700));
        upperSide.remove(0);
        upperSide.add(canvas, 0);

    }

}
