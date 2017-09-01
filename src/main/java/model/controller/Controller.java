package model.controller;

import model.graph.building.Building;
import model.graph.building.DIR;
import model.graph.building.Grid;
import model.graph.evacuation.ShortestPathsEvacuation;
import model.graph.useragent.Person;
import model.representation.BuildingRepresentation;
import model.representation.Menu;
import model.representation.Window;

import javax.swing.*;
import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class Controller implements Runnable {

    private Building building = new Building();
    private BuildingRepresentation buildingRepresentation = new BuildingRepresentation(building);

    private Window window;
    private Menu menu;

    private int milliSecondsPerStep = 200;

    private Thread thread;
    private boolean running = false;
    public boolean emergency = false;

    private float percentage;

    long lastTime;
    long deltaTime;
    int numberOfPeople;


    public STATE state;

    public enum STATE {
        BUILDINGMOVING,
        PAUSE,
        EMPTY
    }

    private Controller() {

        state = STATE.EMPTY;

    }


    public synchronized void start() {
        thread = new Thread(this);
        running = true;
        thread.start();
    }

    public synchronized void pause() {

        state = STATE.PAUSE;
        menu.stateChanged(STATE.PAUSE);

    }

    public synchronized void go() {

        state = STATE.BUILDINGMOVING;
        menu.stateChanged(STATE.BUILDINGMOVING);
    }

    public synchronized void stop() {
        try {
            thread.join();
            running = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adjustVelocity(int mps) {
        if (mps > 50) {
            milliSecondsPerStep = mps;
        } else
            throw new IllegalArgumentException("Too quick!");
    }

    public void adjustNumberOfPeople(int number) {
        if (number >= 0 && number < 500) {
            this.numberOfPeople = number;
            building.setAverageCapacity(number);
        } else {
            throw new IllegalArgumentException();
        }


    }

    public void run() {

        while (running) {

            synchronized (this) {
                if (state != STATE.EMPTY) {
                    boolean b = true;
                }
            }


            if (state != STATE.EMPTY) {
                if (state == STATE.PAUSE) {

                    buildingRepresentation.render(percentage);
                    window.repaint();
                    lastTime = System.currentTimeMillis() - deltaTime;

                } else if (state == STATE.BUILDINGMOVING) {

                    deltaTime = System.currentTimeMillis() - lastTime;

                    if (emergency && (building).getState() != Building.STATE.EVACUATION) {
                        building.startEvacuation(new ShortestPathsEvacuation(building));
                    }
                    percentage = ((float) deltaTime / (float) milliSecondsPerStep);
                    if (percentage >= 1) {
                        lastTime = System.currentTimeMillis();
                        buildingRepresentation.render(1.0f);
                        window.repaint();
                        building.tick();

                    } else {
                        if (deltaTime > (1000 / 30)) {
                            buildingRepresentation.render(percentage);
                            window.repaint();
                        }
                    }
                }

            }

        }

    }

    public synchronized void startEmergency(String strategy) {
        emergency = true;
        switch (strategy) {
            case "ShortestPath":
                building.startEvacuation(new ShortestPathsEvacuation(building));
                break;
            default:
                System.err.println("Unknown EvacuationStrategy, chose default one");
                building.startEvacuation(new ShortestPathsEvacuation(building));
                break;
        }
    }

    public synchronized void openFile(File file) {

        emergency = false;
        building = Building.fromJSON(file);
        building.setAverageCapacity(numberOfPeople);
        buildingRepresentation = new BuildingRepresentation(building);
        window.changeBuilding(buildingRepresentation);
        state = STATE.PAUSE;
        menu.stateChanged(STATE.PAUSE);

    }

    public synchronized void stopBuilding() {
        building = new Building();
        buildingRepresentation = new BuildingRepresentation(building);
        state = STATE.EMPTY;
        menu.stateChanged(STATE.EMPTY);
    }


    public Building createBuildingOne() {

        building = new Building("Building One", 20, 8, 12);
        createRoomsBuildingOne();
        createPassagesBuildingOne();
        createExitsBuildingOne();
        /*HashSet<Person> people = createInitialPeople();
        building.addPersons(people);*/
        return building;

    }


    private void createRoomsBuildingOne() {

        building.addRoom(building.getCell(0, 0), building.getCell(4, 3));
        building.addRoom(building.getCell(0, 4), building.getCell(4, 7));
        building.addRoom(building.getCell(5, 0), building.getCell(9, 7));
        building.addRoom(building.getCell(10, 0), building.getCell(14, 3));
        building.addRoom(building.getCell(10, 4), building.getCell(14, 7));
        building.addRoom(building.getCell(15, 0), building.getCell(19, 7));

    }

    private void createPassagesBuildingOne() {

        HashSet<Grid.CellPair> connectedCells = new HashSet<>();

        connectedCells.add(building.getCellPair(2, 3, 2, 4));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

        connectedCells.add(building.getCellPair(4, 1, 5, 1));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

        connectedCells.add(building.getCellPair(4, 5, 5, 5));
        connectedCells.add(building.getCellPair(4, 6, 5, 6));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

        connectedCells.add(building.getCellPair(9, 5, 10, 5));
        connectedCells.add(building.getCellPair(9, 6, 10, 6));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

        connectedCells.add(building.getCellPair(12, 3, 12, 4));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

        connectedCells.add(building.getCellPair(14, 1, 15, 1));
        connectedCells.add(building.getCellPair(14, 2, 15, 2));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

        connectedCells.add(building.getCellPair(14, 5, 15, 5));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

    }

    private void createExitsBuildingOne() {

        HashSet<Grid.CellPair> connectedCells = new HashSet<>();
        LinkedList<Grid.Cell> stairCells = new LinkedList<>();

        stairCells.add(building.getCell(-1, 5));
        stairCells.add(building.getCell(-1, 6));
        building.addStair(new LinkedList<>(stairCells), DIR.LEFT);
        stairCells.clear();

        connectedCells.add(building.getCellPair(2, 0, 2, -1));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

        stairCells.add(building.getCell(17, -1));
        building.addStair(new LinkedList<>(stairCells), DIR.UP);
        stairCells.clear();

        connectedCells.add(building.getCellPair(17, 7, 17, 8));
        connectedCells.add(building.getCellPair(18, 7, 18, 8));
        building.addDoor(new HashSet<>(connectedCells));
        connectedCells.clear();

    }

    private HashSet<Person> createInitialPeople() {

        HashSet<Person> people = new HashSet<>();

        /*
        code to get a random room to go to
        new LinkedList<Room>(building.getRooms()).get(r.nextInt(building.getRooms().size()))
         */
        Random r = new Random();
        for (int i = 0; i < 80; i++) {
            Grid.Cell startCell = building.getCell(r.nextInt(building.gridSizeX), r.nextInt(building.gridSizeY));
            while (startCell.isOccupied()) {
                startCell = building.getCell(r.nextInt(building.gridSizeX), r.nextInt(building.gridSizeY));
            }
            Person p = new Person("Eberhard " + (i + 1), startCell, false, Person.STATE.GOTOROOM,
                    null);
            people.add(p);
        }
        for (int i = 0; i < 80; i++) {
            Grid.Cell startCell = building.getCell(r.nextInt(building.gridSizeX), r.nextInt(building.gridSizeY));
            while (startCell.isOccupied() || startCell.isStair()) {
                startCell = building.getCell(r.nextInt(building.gridSizeX), r.nextInt(building.gridSizeY));
            }
            Person p = new Person("Eberhard " + (i + 11), startCell, true, Person.STATE.GOTOROOM,
                    null);
            people.add(p);
        }
        return people;

    }


    public static void main(String[] args) {

        Controller controller = new Controller();

        controller.building = new Building();
        controller.buildingRepresentation = new BuildingRepresentation(controller.building);

        controller.menu = new Menu(controller);

        controller.window = new Window(640,
                400,
                controller.buildingRepresentation,
                controller.menu,
                new JPanel());

        controller.start();


    }

}
