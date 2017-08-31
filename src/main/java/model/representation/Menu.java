package model.representation;

import model.controller.Controller;
import model.graph.evacuation.EvacuationStrategy;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Created by Malte on 11.08.2017.
 */
public class Menu extends JPanel {

    private Controller controller;

    private JPanel mpsSliderPanel;
    private JSlider mpsSlider;

    private JPanel peopleSliderPanel;
    private JSlider peopleSlider;

    private JButton pauseButton;

    private JComboBox emergencyStrategies;
    private JButton emergencyButton;

    private JButton stopButton;

    private JFileChooser fileChooser;

    private JButton fileOpenButton;

    public Menu(Controller controller) {

        this.controller = controller;

        setLayout(new GridLayout2(30, 1));

        mpsSliderPanel = fpsSlider();
        mpsSliderPanel.setPreferredSize(new Dimension(100, 150));

        pauseButton = pauseButton();
        pauseButton.setPreferredSize(new Dimension(100, 100));

        stopButton = stopButton();
        stopButton.setPreferredSize(new Dimension(100, 100));

        fileOpenButton = fileOpen();
        fileOpenButton.setPreferredSize(new Dimension(100, 100));

        peopleSliderPanel = peopleSlider();
        peopleSliderPanel.setPreferredSize(new Dimension(100, 150));


        JPanel emergency = new JPanel(new GridLayout2(1,2));
        emergency.setPreferredSize(new Dimension(100,100));
        emergencyButton = emergencyButton();
        emergencyButton.setPreferredSize(new Dimension(30, 100));
        emergencyStrategies = emergencyStrategies();
        emergencyStrategies.setPreferredSize(new Dimension(70, 100));
        emergency.add(emergencyStrategies);
        emergency.add(emergencyButton);


        JPanel stopPause = new JPanel(new GridLayout2(1, 2));
        stopPause.setPreferredSize(new Dimension(100, 100));
        stopPause.add(pauseButton);
        stopPause.add(stopButton);

        add(stopPause);
        add(emergency);
        add(mpsSliderPanel);
        add(peopleSliderPanel);
        add(fileOpenButton);

    }

    private JButton fileOpen() {

        fileChooser = new JFileChooser();
        ClassLoader classLoader = getClass().getClassLoader();
        File file = null;
        try {
            file = new File(classLoader.getResource("buildingsJson").getFile());
        } catch (NullPointerException e) {
            System.err.println("buildingsJson does not seem to exist");
        }
        fileChooser.setCurrentDirectory(file);

        fileOpenButton = new JButton("Open existing Building");
        fileOpenButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(fileOpenButton);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    controller.openFile(file);

                }
            }
        });

        return fileOpenButton;

    }

    private JPanel fpsSlider() {

        mpsSlider = new JSlider(100, 2000, 500);
        mpsSlider.setName("Milliseconds per step");
        mpsSlider.setMajorTickSpacing(500);
        mpsSlider.setMinorTickSpacing(100);
        mpsSlider.setPaintTicks(true);
        mpsSlider.setPaintLabels(true);

        mpsSlider.setEnabled(false);

        if (controller.state == Controller.STATE.BUILDINGMOVING) {
            mpsSlider.setEnabled(true);
        }

        mpsSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                controller.adjustVelocity(source.getValue());
            }
        });

        JPanel mps = new JPanel(new GridLayout2(2, 1));
        JLabel mpsText = new JLabel("Milliseconds per step:");
        mpsText.setPreferredSize(new Dimension(100, 60));
        mps.add(mpsText, Component.CENTER_ALIGNMENT);
        mpsSlider.setPreferredSize(new Dimension(100, 140));
        mps.add(mpsSlider);

        return mps;

    }

    private JPanel peopleSlider() {

        peopleSlider = new JSlider(0, 200, 0);
        peopleSlider.setName("People in Building");
        peopleSlider.setMajorTickSpacing(40);
        peopleSlider.setMinorTickSpacing(10);
        peopleSlider.setPaintTicks(true);
        peopleSlider.setPaintLabels(true);

        peopleSlider.setEnabled(false);

        if (controller.state == Controller.STATE.BUILDINGMOVING) {
            peopleSlider.setEnabled(true);
        }

        peopleSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                controller.adjustNumberOfPeople(source.getValue());
            }
        });

        JPanel people = new JPanel(new GridLayout2(2, 1));
        JLabel peopleText = new JLabel("Average Nr. of people in Building:");
        peopleText.setPreferredSize(new Dimension(100, 60));
        people.add(peopleText, Component.CENTER_ALIGNMENT);
        peopleSlider.setPreferredSize(new Dimension(100, 140));
        people.add(peopleSlider);

        return people;

    }


    private JButton pauseButton() {

        JButton pause = new JButton();
        if (controller.state == Controller.STATE.PAUSE) {
            pause.setText("GO");
        } else if (controller.state == Controller.STATE.BUILDINGMOVING) {
            pause.setText("PAUSE");
        } else if (controller.state == Controller.STATE.EMPTY) {
            pause.setText("Pause-Button");
            pause.setEnabled(false);
        }

        pause.setMnemonic(KeyEvent.VK_SPACE);
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pause.getText().equals("PAUSE")) {
                    controller.pause();
                } else {
                    controller.go();
                }
            }
        });

        return pause;

    }

    private JButton stopButton() {
        JButton stop = new JButton();
        if (controller.state == Controller.STATE.PAUSE || controller.state == Controller.STATE.BUILDINGMOVING) {
            stop.setText("STOP");
        } else if (controller.state == Controller.STATE.EMPTY) {
            stop.setText("Stop-Button");
            stop.setEnabled(false);
        }

        stop.setMnemonic(KeyEvent.VK_SPACE);
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.stopBuilding();
            }
        });

        return stop;
    }

    private JButton emergencyButton() {

        JButton emergency = new JButton();
        emergency.setText("Evacuation!");
        if (controller.state == Controller.STATE.PAUSE || controller.state == Controller.STATE.EMPTY) {
            emergency.setEnabled(false);
        } else {
            emergency.setEnabled(true);
        }

        emergency.setMnemonic(KeyEvent.VK_E);
        emergency.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String strategy = (String) emergencyStrategies.getSelectedItem();
                controller.startEmergency(strategy);
                emergency.setEnabled(false);
            }
        });

        return emergency;
    }

    private JComboBox emergencyStrategies() {

        String[] knownStrategies = {"ShortestPath"};
        JComboBox<String> temp = new JComboBox<>(knownStrategies);
        temp.setSelectedIndex(0);

        temp.setEnabled(true);
        return temp;

    }

    public synchronized void stateChanged(Controller.STATE state) {

        switch (state) {

            case PAUSE:
                mpsSlider.setEnabled(false);

                peopleSlider.setEnabled(true);

                pauseButton.setText("GO");
                pauseButton.setEnabled(true);

                stopButton.setText("STOP");
                stopButton.setEnabled(true);

                emergencyButton.setEnabled(false);

                fileOpenButton.setEnabled(false);
                break;

            case EMPTY:
                mpsSlider.setEnabled(false);

                peopleSlider.setEnabled(false);

                pauseButton.setText("Pause-Button");
                pauseButton.setEnabled(false);

                stopButton.setText("Stop-Button");
                stopButton.setEnabled(false);

                emergencyButton.setEnabled(false);

                fileOpenButton.setEnabled(true);
                break;

            case BUILDINGMOVING:
                mpsSlider.setEnabled(true);

                peopleSlider.setEnabled(true);

                pauseButton.setText("PAUSE");
                pauseButton.setEnabled(true);

                stopButton.setText("STOP");
                stopButton.setEnabled(true);

                if (!controller.emergency) {
                    emergencyButton.setEnabled(true);
                }

                fileOpenButton.setEnabled(false);
                break;

            default:
                throw new IllegalStateException("Unknown state!");

        }

    }

}
