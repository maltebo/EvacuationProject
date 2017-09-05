package model.representation;

import model.graph.building.Building;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by Malte on 11.08.2017.
 */
public class Window extends Canvas {

    private JFrame frame;
    private JPanel upperSide;
    private Building building;
    private Menu menu;
    private Component statistics;
    private JTabbedPane pane;

    public Window(int width, int height, Building building, Menu menu, Component statistics) {

        pane = new JTabbedPane();
        makeBuilding(building, pane);


        this.menu = menu;
        this.statistics = statistics;

        frame = new JFrame();

        frame.setSize(width, height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new GridLayout2(2, 1, 6, 6));
        upperSide = new JPanel(new GridLayout2(1, 2, 6, 6));
        upperSide.setPreferredSize(new Dimension(1000, 700));
        frame.add(upperSide);


        pane.setPreferredSize(new Dimension(700, 700));

        upperSide.add(pane);

        menu.setPreferredSize(new Dimension(300, 700));
        upperSide.add(menu);

        statistics.setPreferredSize(new Dimension(1000, 300));
        statistics.setBackground(Color.green);
        frame.add(statistics);

        frame.requestFocus();
        frame.setVisible(true);

    }

    public void changeBuilding(Building newBuilding) {

        this.building = newBuilding;

        pane = new JTabbedPane();
        pane.setPreferredSize(new Dimension(700, 700));
        makeBuilding(newBuilding, pane);

        upperSide.remove(0);
        upperSide.add(pane, 0);

    }

    private void makeBuilding(Building building, JTabbedPane pane) {

        int key = KeyEvent.VK_1;

        for (int floor = 0; floor < building.floors; floor++) {

            BuildingRepresentation buildingRepresentation = new BuildingRepresentation(building, floor);

            pane.addTab("Floor " + (floor + 1), buildingRepresentation);

            pane.setMnemonicAt(floor, key++);

        }

    }

    public void render() {
        ((BuildingRepresentation)pane.getSelectedComponent()).render();
    }

    public void render(float percentage) {
        ((BuildingRepresentation)pane.getSelectedComponent()).render(percentage);
    }

}
