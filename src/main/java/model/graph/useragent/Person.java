package model.graph.useragent;

import model.graph.building.Building;
import model.graph.building.DIR;
import model.graph.building.Grid;
import model.graph.building.Grid.*;
import model.graph.Path;
import model.graph.building.Building.Room;

import java.awt.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**
 * This class represents a person walking inside a specified building.
 * It saves its location and has an id, a name, a color and a size.
 * It can also have a goal-location and/or a path it wants to go along.
 *
 * @author Malte Bossert
 * @version 3.2
 */
public class Person {

    /**
     * the color of this person, might be changed if the person changes its state (TODO)
     */
    public transient Color COLOR;
    /**
     * the size of the person (used to represent it)
     */
    public transient final int SIZE;
    /**
     * the person's name
     */
    private final String name;
    /**
     * the building this person is currently in
     */
    private transient Building building;
    /**
     * the room this person is in
     */
    private transient Room isInRoom;
    /**
     * defines whether the person is disabled or not - standard
     * is no
     */
    private boolean isDisabled = false;
    /**
     * the last cell this person was on
     */
    private transient Cell wasOnCell;
    /**
     * the cell the person is on in this step
     */
    private Cell isOnCell;
    /**
     * used to give each person a unique id
     */
    private transient static int idResource = 0;
    /**
     * the person's unique id
     */
    private final int id;
    /**
     * the state the person is in
     */
    private transient STATE state;
    /**
     * the path the person wants to go (this is null in the beginning)
     */
    private transient Path path;
    /**
     * the person's goal room, in the beginning null
     */
    private transient Room goalRoom;
    /**
     * a boolean that states whether this person is being evacuated right now
     */

    private transient MovementModule movementModule;
    /**
     * all states a person can be in
     */
    public enum STATE {
        GETINTOBUILDING,
        STANDSTILL,
        STAYINROOM,
        STAYINBUILDING,
        WANDERRANDOMLY,
        GOTOROOM,
        EVACUATION
    }


    /**
     * creates a new "standard" - person with a unique id
     *
     * @param isDisabled if this person is disabled
     */
    public Person(boolean isDisabled, Building building) {

        // unique id
        this.id = idResource++;
        this.name = "RandomPerson" + id;
        this.movementModule = MovementModule.getMovementModule(building);
        SIZE = isDisabled ? 15 : 10;
        this.building = building;
        LinkedList<Grid.Cell> startCells = new LinkedList<>(building.getEntryCells());
        Collections.shuffle(startCells);

        // finds a valid startCell
        for (Grid.Cell cell : startCells) {
            if (!isDisabled) {
                if (!cell.isOccupied()) {
                    cell.setOccupied(true);

                    this.isOnCell = cell;
                    this.wasOnCell = cell;
                    break;
                }
            } else {
                if (!cell.isOccupied() && !cell.isStair()) {
                    cell.setOccupied(true);

                    this.isOnCell = cell;
                    this.wasOnCell = cell;
                    break;
                }
            }
        }
        if (isOnCell == null) {
            throw new IllegalStateException("No valid cell was found!");
        }
        this.isInRoom = isOnCell.getRoom();
        this.isDisabled = isDisabled;
        this.state = STATE.GETINTOBUILDING;
        COLOR = Color.CYAN;
    }


    /**
     * see other constructor
     *
     * @param name       see other constructor
     * @param startCell  see other constructor
     * @param isDisabled see other constructor
     * @param state      see other constructor
     */
    public Person(String name, Cell startCell, boolean isDisabled, STATE state, Building building) {

        this(name, startCell, isDisabled, state, null, building);

    }

    /**
     * constructor that builds a specified person from scratch.
     *
     * @param name       the name of this person
     * @param startCell  the cell on which this person is starting
     * @param isDisabled whether this person is disabled
     * @param state      the state of this person
     * @param goalRoom   the goal room; null if none is necessary
     */
    public Person(String name, Grid.Cell startCell, boolean isDisabled, STATE state, Room goalRoom, Building building) {

        this.name = name;
        this.movementModule = MovementModule.getMovementModule(building);
        this.isOnCell = startCell;
        this.wasOnCell = startCell;
        if (!startCell.isOccupied()) {

            startCell.setOccupied(true);
        } else {
            throw new IllegalStateException("StartCell was already occupied");
        }
        this.isInRoom = startCell.getRoom();
        this.goalRoom = goalRoom;
        this.building = building;
        this.isDisabled = isDisabled;
        this.id = idResource++;
        this.state = state;
        /*COLOR = new Color((float) Math.random(), (float) Math.random(),
                (float) Math.random(), (float) (Math.random() / 2 + 0.5));
        */
        COLOR = Color.CYAN;
        SIZE = isDisabled ? 15 : 10;

    }

    /**
     * @return the name of the person
     */
    public String getName() {
        return name;
    }

    /**
     * @return the room in which the person is standing in this moment
     */
    public Room getIsInRoom() {
        return isInRoom;
    }

    /**
     * @return whether the person is disabled or not
     */
    public boolean isDisabled() {
        return isDisabled;
    }

    /**
     * @return the cell on which the person is standing
     */
    public Grid.Cell getIsOnCell() {
        return isOnCell;
    }

    /**
     * @return the cell on which the person was standing
     */
    public Grid.Cell getWasOnCell() {
        return wasOnCell;
    }

    /**
     * @return the person's unique id
     */
    public int getId() {
        return id;
    }

    /**
     * returns path as new path if this existed
     */
    public Path getPath() {
        if (path == null) return null;
        return new Path(path);
    }

    /**
     * @param path this path will be assigned to the person
     */
    public void setPath(Path path) {
        this.path = path;
    }

    /**
     * @return the room the person wants to reach
     */
    public Room getGoalRoom() {
        return goalRoom;
    }

    /**
     * @param goalRoom the room the person should go to next
     */
    public void setGoalRoom(Room goalRoom) {
        this.goalRoom = goalRoom;
    }

    /**
     * @return returns the actual <code>STATE</code> of the person
     * @see STATE
     */
    public STATE getState() {
        return state;
    }

    /**
     * @param state the <code>STATE</code> the person will have next
     * @see STATE
     */
    public void setState(STATE state) {
        if (this.state != STATE.EVACUATION) {
            this.state = state;
        }
    }

    /**
     * this person is removed from the building when calling this method
     */
    public void remove() {
        if (isOnCell != null) {

            if (wasOnCell.isOccupied()) {

            }
            isOnCell.setOccupied(false);
        }
        building.removePerson(this);
    }

    /**
     * tells the person to update itself.
     * Calls the <code>MovementModule</code> to get the
     * next direction, blocks surrounding cells and changes the person's state
     * in 10% of the cases
     *
     * @see MovementModule
     */
    public void tick() {

        // marks cell as unoccupied
        isOnCell.setOccupied(false);
        wasOnCell = isOnCell;
        if (building.getPersonsInBuilding().contains(this)) {
            int direction = movementModule.move(this);

            // block cells if you go diagonally
            if (direction > 40) blockCells(direction);
            isOnCell = isOnCell.getNextCell(direction);
            // marks new Cell as occupied
            isOnCell.setOccupied(true);
            if (wasOnCell.isOutside()) {
                wasOnCell.setOccupied(false);
            }
            // sets its Room
            isInRoom = isOnCell.getRoom();
            if (building.getState() != Building.STATE.EVACUATION) {
                if ((this.state != STATE.GOTOROOM || this.goalRoom != null) && this.state != STATE.STANDSTILL) {
                    if (Math.random() < 0.1) {

                        changeState();
                    }

                }
            }
        }
    }

    /**
     * changes a person's state, either to go to a special room or to stay in the building
     * but walk around randomly or to stay in the room it is in.
     */
    private void changeState() {

        if (!(isOnCell.isOutside())) {
            Random r = new Random();
            double p = r.nextDouble();
            if (p < 0.5) {
                state = STATE.GOTOROOM;
                goalRoom = new LinkedList<>(building.getRooms()).get(r.nextInt(building.getRooms().size()));

            } else if (p < 0.75) {
                state = STATE.STAYINBUILDING;
            } else {
                state = STATE.STAYINROOM;
            }
        }
    }

    /**
     * same as the tick method above, but depending on the boolean changing the state
     * to moving outside the building. This is called in the <code>Building</code>
     * and depends on the number of people currently in the building.
     *
     * @param exit whether the person should exit or not
     */
    public void tick(boolean exit) {

        if (!exit) {
            tick();
        } else {
            state = STATE.GOTOROOM;
            goalRoom = null;
            tick();
        }
    }

    /**
     * blocks cells if the person goes diagonally, so that another person
     * can't go diagonally as well, orthogonally to this person (if it could,
     * it would be possible for two people to cross paths)
     *
     * @param direction the direction the person is going in
     */
    private void blockCells(int direction) {
        if (direction % DIR.DOWN == 0) {
            if (isOnCell.getNextCell(DIR.DOWN) != null) {
                isOnCell.getNextCell(DIR.DOWN).block(true);
            }
        }

        else if (direction % DIR.UP == 0) {
            if (isOnCell.getNextCell(DIR.UP) != null) {
                isOnCell.getNextCell(DIR.UP).block(true);
            }
        }
        if (direction % DIR.LEFT == 0) {
            if (isOnCell.getNextCell(DIR.LEFT) != null) {
                isOnCell.getNextCell(DIR.LEFT).block(true);
            }
        }
        else if (direction % DIR.RIGHT == 0) {
            if (isOnCell.getNextCell(DIR.RIGHT) != null) {
                isOnCell.getNextCell(DIR.RIGHT).block(true);
            }
        }
    }

    public void startEvacuation() {
        setPath(null);
        state = STATE.EVACUATION;
    }

    void nextInPath() {

        path.remove(0);

    }


}