package model.graph.building;

import com.google.gson.*;
import model.graph.evacuation.EvacuationStrategy;
import model.graph.useragent.Person;
import model.helper.Pair;
import model.graph.building.Grid.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Represents a complete building with rooms, passages and people.
 * Can be in two states: either in normal running state or in evacuation-mode,
 * where all people are evacuated according to the current strategy in <code>MovementModule</code>,
 * <code>PathOntology</code> and <code>EmergencyOntology</code> (TODO)
 *
 * @author Malte Bossert
 * @version 3.2
 */
public class Building {


    /**
     * the grid of all cells the building consists of.
     */
    transient Grid grid;
    /**
     * the name of the building, which can not be changed.
     */
    public final String name;
    /**
     * the grid size of the building in X-direction.
     */
    public final int gridSizeX;
    /**
     * the grid size of the building in Y-direction.
     */
    public final int gridSizeY;

    private transient int roomNumber = 0;

    private transient int passageNumber = 0;

    /**
     * the states in which the building can be in, namely normal and evacuation mode.
     */
    public enum STATE {
        NORMAL,
        EVACUATION
    }

    /**
     * the average number of people in the building.
     */
    public int averageCapacity;

    /**
     * a <code>HashSet</code> of all persons that are currently in the building.
     *
     * @see Person
     */
    private transient HashSet<Person> personsInBuilding;

    /**
     * the actual state of the building.
     */
    private transient STATE state;


    /**
     * a <code>HashSet</code> of all Rooms in the building.
     *
     * @see Room
     */
    private HashSet<Room> rooms;
    /**
     * a <code>HashSet</code> of all Passages in the building.
     *
     * @see Passage
     * @see Door
     * @see Stair
     */
    private HashSet<Door> doors;
    //TODO
    private HashSet<Stair> stairs;
    /**
     * a <code>HashSet</code> of all Exits in the building.
     *
     * @see Passage
     */
    private transient HashSet<Passage> exits;

    /**
     * a <code>HashSet</code> of all cells that are outside of the building
     * and belong to an entrance.
     *
     * @see Grid.Cell
     */
    private transient HashSet<Grid.Cell> entryCells;

    /**
     * the <code>Singleton</code> instance of this building.
     */

    public Building() {
        this("empty", 1, 1, 0);
    }


    public Building (Building building) {

        this (building.name, building.gridSizeX, building.gridSizeY);
        for (Room room : building.rooms) {
            addRoom(grid.getCell(room.beginning), grid.getCell(room.end), room.id);
        }
        for (Door door : building.doors) {
            HashSet<CellPair> tempCells = new HashSet<>();
            for (CellPair cellPair : door.connectedCells) {
                tempCells.add(grid.getCellPair(grid.getCell(cellPair.getCell1()),
                        grid.getCell(cellPair.getCell2())));
            }
            addDoor(tempCells, door.id);
        }

        for (Stair stair : building.stairs) {
            LinkedList<Cell> tempCells = new LinkedList<>();
            for (Cell cell : stair.stairCells) {
                tempCells.add(grid.getCell(cell));
            }

            addStair(tempCells, stair.direction, stair.id);
        }

        updateNumbers();

        this.averageCapacity = 0;
        this.personsInBuilding = new HashSet<>();
        this.state = STATE.NORMAL;

    }


    /**
     * the private constructor that creates a new building.
     *
     * @param name            Name of the building.
     * @param gridSizeX       grid size in x-direction.
     * @param gridSizeY       grid size in y-direction.
     * @param averageCapacity average Capacity of people in this building.
     */
    public Building(String name, int gridSizeX, int gridSizeY, int averageCapacity) {
        this.name = name;
        this.gridSizeX = gridSizeX;
        this.gridSizeY = gridSizeY;
        createGrid();
        this.rooms = new HashSet<>();
        this.doors = new HashSet<>();
        this.stairs = new HashSet<>();
        this.exits = new HashSet<>();
        this.entryCells = new HashSet<>();
        this.averageCapacity = averageCapacity;
        this.personsInBuilding = new HashSet<>();
        this.state = STATE.NORMAL;
    }

    /**
     * the constructor that creates a new building.
     *
     * @param name      Name of the building.
     * @param gridSizeX grid size in x-direction.
     * @param gridSizeY grid size in y-direction.
     */
    public Building(String name, int gridSizeX, int gridSizeY) {
        this(name, gridSizeX, gridSizeY, 0);
    }

    /**
     * creates the grid with all cells that could be part of the building (according to
     * specified grid-Size).
     *
     * @see Grid.Cell
     */
    private void createGrid() {
        grid = new Grid(this);
    }

    public void updateNumbers() {
        passageNumber = getPassages().size();
        roomNumber = rooms.size();
    }

    public Grid getGrid() {
        return grid;
    }

    //TODO
    public Grid.Cell getCell(int x, int y) {
        return grid.getCell(x, y);
    }

    //TODO
    public Grid.CellPair getCellPair(int x1, int y1, int x2, int y2) {

        return getCellPair(getCell(x1, y1), getCell(x2, y2));

    }

    public Grid.CellPair getCellPair(Grid.Cell c1, Grid.Cell c2) {

        return grid.getCellPair(c1, c2);

    }

    //TODO
    public float distance(Grid.Cell c1, Grid.Cell c2) {
        return grid.distance(c1, c2);
    }

    //TODO
    public float distance(Grid.Cell c1, HashSet<Grid.Cell> c2) {
        return grid.distance(c1, c2);
    }

    // TODO: Javadocs
    public void addRoom(Grid.Cell leftUp, Grid.Cell rightDown) {
        rooms.add(new Room(leftUp, rightDown));
    }

    // TODO: Javadocs
    private void addRoom(Grid.Cell leftUp, Grid.Cell rightDown, int id) {
        rooms.add(new Room(leftUp, rightDown, id));
    }

    //TODO
    public static Building fromJSON(File json) {

        try {
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(Building.class, new BuildingInstanceCreator());
            Gson gson = gb.create();
            return gson.fromJson(new FileReader(json), Building.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    //TODO
    public static Building fromJSON(String json) {

        try {
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(Building.class, new BuildingInstanceCreator());
            Gson gson = gb.create();
            return gson.fromJson(new FileReader(json), Building.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public void makeJSONfile() {
        String json = toJSONstring();
        String name = this.name.replaceAll("\\s+","");

        ClassLoader cl = getClass().getClassLoader();
        String resourcePath = cl.getResource("buildingsJson").getPath();


        File file = new File(resourcePath + "/" + name + ".json");
        if(!file.exists()) {
            try {
                file.createNewFile();
                file.setWritable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else{
            System.err.println("File already exists");
        }
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(json);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String toJSONstring() {

        String json = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            json = gson.toJson(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;

    }

    // TODO: Javadocs
    public void addDoor(HashSet<Grid.CellPair> connectedCells) {
        Door door = new Door(connectedCells);
        doors.add(door);

        if (door.isExit()) {
            Room room = door.getExitRoom();
            room.addExit(door);
            exits.add(door);
            entryCells.addAll(door.getRoomChangingCells(null));
        }
    }

    // TODO: Javadocs
    private void addDoor(HashSet<Grid.CellPair> connectedCells, int id) {
        Door door = new Door(connectedCells, id);
        doors.add(door);

        if (door.isExit()) {
            Room room = door.getExitRoom();
            room.addExit(door);
            exits.add(door);
            entryCells.addAll(door.getRoomChangingCells(null));
        }
    }

    public void addStair(LinkedList<Grid.Cell> stairCells, int direction) {

        Stair stair = new Stair(stairCells, direction);
        stairs.add(stair);

        if (stair.isExit()) {
            Room room = stair.getExitRoom();
            room.addExit(stair);
            exits.add(stair);
            entryCells.addAll(stair.getRoomChangingCells(null));
        }

    }

    private void addStair(LinkedList<Grid.Cell> stairCells, int direction, int id) {

        Stair stair = new Stair(stairCells, direction, id);
        stairs.add(stair);

        if (stair.isExit()) {
            Room room = stair.getExitRoom();
            room.addExit(stair);
            exits.add(stair);
            entryCells.addAll(stair.getRoomChangingCells(null));
        }

    }

    /**
     * @return all the rooms in the building
     */
    public HashSet<Room> getRooms() {
        return new HashSet<>(rooms);
    }


    /**
     * @return all the passages in the building, like doors or stairs
     */
    public HashSet<Passage> getPassages() {
        HashSet<Passage> passages = new HashSet<>(doors);
        passages.addAll(stairs);
        return passages;
    }

    /**
     * @return all the doors in the building.
     */
    public HashSet<Door> getDoors() {
        return new HashSet<>(doors);
    }

    /**
     * @return all the stairs in the building.
     */
    public HashSet<Stair> getStairs() {
        return new HashSet<>(stairs);
    }

    /**
     * @return all the exits in the building (might be doors or stairs
     */
    public HashSet<Passage> getExits() {
        return new HashSet<>(exits);
    }

    /**
     * @return all cells of the grid which allow entering the building, eg that are part
     * of an exit and are located outside the building.
     */
    public HashSet<Grid.Cell> getEntryCells() {
        return new HashSet<>(entryCells);
    }


    /**
     * adds a pre-specified person to the building.
     *
     * @param person person to be included in the building.
     * @see Person
     */
    public void addPerson(Person person) {
        personsInBuilding.add(person);
    }

    /**
     * removes a person from the building.
     *
     * @param person person to be removed
     * @see Person
     */
    public void removePerson(Person person) {
        personsInBuilding.remove(person);
    }


    /**
     * @return all the persons in the building
     */
    public HashSet<Person> getPersonsInBuilding() {
        return new HashSet<>(personsInBuilding);
    }

    public void updateNumberOfPeople(int number) {
        this.averageCapacity = number;
    }


    /**
     * sets the State of the building to evacuation-mode and tells every person to
     * move outside the building.
     */
    public void startEvacuation(EvacuationStrategy es) {
        this.state = STATE.EVACUATION;

        es.startEvacuation();

    }


    /**
     * @return the actual state of the building
     */
    public STATE getState() {
        return state;
    }


    /**
     * in every step, this method tells every person inside to update itself,
     * and randomly adds persons so that in average, the number of persons stays constant
     */
    public void tick() {

        float diminish = 0.3f;
        float pDeletePerson = 1;
        float pAddPerson = 0;
        if (averageCapacity != 0) {
            pDeletePerson = ((float) personsInBuilding.size() / (float) (averageCapacity * 2)) * diminish;
            pAddPerson = diminish - pDeletePerson;
        }
        // cells are blocked if people move diagonally - must be unblocked after every step
        grid.unblock();
        LinkedList<Person> personCopy = new LinkedList<>(personsInBuilding);
        // persons who are in the building the longest are first
        personCopy.sort(new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.getId() - o2.getId();
            }
        });
        // lets persons leave the building with some earlier defined probability
        // <code>pDeletePerson</code>
        while (true) {
            if (!personCopy.isEmpty()) {
                Person person = personCopy.poll();
                if (person.getState() != Person.STATE.GOTOROOM || person.getGoalRoom() != null) {
                    person.tick(Math.random() < pDeletePerson);
                    break;
                } else {
                    person.tick();
                }
            } else {
                break;
            }
        }
        // tells every other person to update as well
        for (Person person : personCopy) {
            person.tick();
        }
        //if the state is not evacuation-state, add a person
        if (state != STATE.EVACUATION) {
            addRandomPerson(pAddPerson);
        }
    }


    /**
     * adds a random Person; probability that this person will be disabled is given
     * by field <code>pDisabled</code>
     *
     * @param pAdd probability that a new person will actually be added; depends on the actual number
     *             of people in the building and the average number of people that should be in there
     */
    private void addRandomPerson(float pAdd) {

        float pDisabled = 0.2f;

        if (Math.random() < pAdd) {

            if (Math.random() < pDisabled) {
                for (Grid.Cell cell : getEntryCells()) {
                    if (!cell.isOccupied() && !cell.isStair()) {
                        addPerson(new Person(true, this));
                        break;
                    }
                }
            } else {


                for (Grid.Cell cell : getEntryCells()) {
                    if (!cell.isOccupied()) {
                        addPerson(new Person(false, this));
                        break;
                    }
                }
            }
        }
    }

    /**
     * is an abstract class for all types of passages, namely doors and stairs.
     * Assigns a unique id to each passage.
     *
     * @author Malte Bossert
     * @version 3.2
     * @see Door
     * @see Stair
     */
    public abstract class Passage {

        /**
         * the passage's unique id, which is a constant and can therefore
         * be public
         */
        public final int id;

        //TODO
        transient boolean exit;

        /**
         * constructor that only assigns the ID
         */
        public Passage() {

            this.id = passageNumber++;

        }

        /**
         * constructor that only assigns the ID
         */
        private Passage(int id) {

            this.id = id;

        }

        /**
         * gets the room that connects two passages.
         *
         * @param passage the passage that shall be compared; you check whether
         *                this passage and the that passage have a common room.
         * @return common room if found; else it returns null.
         * @see Pair
         */
        public Room getConnected(Passage passage) {

            return (Room) this.getConnectsInOut().getSameElement(
                    passage.getConnectsInOut());

        }

        /**
         * @return if the passage is an exit - returns default value false.
         */
        public boolean isExit() {
            return exit;
        }

        /**
         * @return if the passage is a stair - returns default value false.
         * @see Stair overwrites the method
         */
        public boolean isStair() {
            return false;
        }

        /**
         * @return the Pair of rooms that are connected by this Passage;
         * one of the Rooms may be null in case of this being an exit
         */
        public abstract Pair<Room, Room> getConnectsInOut();

        /**
         * @return all cells that can be used to change the room
         */
        public abstract HashSet<Grid.Cell> getRoomChangingCells();

        /**
         * @param room the room in which the cells should be
         * @return all cells that belong to this passage and are in the
         * specified room
         */
        public abstract HashSet<Grid.Cell> getRoomChangingCells(Room room);

        /**
         * @return null if the passage is no exit; else the room in which
         * this exit is situated
         */
        public abstract Room getExitRoom();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || !(o instanceof Passage)) return false;


            Passage passage = (Passage) o;

            return id == passage.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }


    /**
     * Represents a door in a building through its coordinates and cells.
     *
     * @author Malte Bossert
     * @version 3.2
     * @see Passage
     */
    public class Door extends Passage {

        /**
         * the rooms that are connected by this passage
         */
        transient Pair<Room, Room> connectsInOut;
        /**
         * all the cell-pairs that belong to the passage;
         * all cells that are connected.
         */
        HashSet<Grid.CellPair> connectedCells;

        /**
         * all cells that are on the outside.
         * outside is assigned randomly; in case this is an exit,
         * outside will be the cells outside the building.
         */
        transient Pair<Room, HashSet<Grid.Cell>> outsideCells;
        /**
         * all cells that are on the inside.
         * inside is assigned randomly; in case this is an exit,
         * inside will be the cells inside the building.
         */
        transient Pair<Room, HashSet<Grid.Cell>> insideCells;

        /**
         * all single cells that this door consists of. In difference to
         * connectedCells, this returns single cells, no cellPairs.
         */
        transient HashSet<Grid.Cell> roomChangingCells;

        /**
         * constructor that builds a door depending on the connectedCells,
         * which represent the door.
         *
         * @param connectedCells all the Cells on both sides of the Door.
         *                       they are given as pairs of cells that are
         *                       directly connected and that are on both sides
         *                       of the door.
         */
        Door(HashSet<Grid.CellPair> connectedCells) {

            // calls super method which assigns an id.
            super();
            this.connectedCells = connectedCells;

            // updates outside and inside Cells
            LinkedList<Grid.Cell> outside = new LinkedList<>();
            LinkedList<Grid.Cell> inside = new LinkedList<>();

            for (Grid.CellPair fp : connectedCells) {
                outside.add(fp.getCell1());
                inside.add(fp.getCell2());
            }

            // gets common Room of outside- and insideCells.
            // method throws an exception if not all cells are in the same room
            Room outsideRoom = grid.getRoom(outside);
            Room insideRoom = grid.getRoom(inside);

            // adds the Passage to the rooms.
            if (outsideRoom != null) outsideRoom.addPassage(this);
            if (insideRoom != null) insideRoom.addPassage(this);

            this.connectsInOut = new Pair<>(insideRoom, outsideRoom);

            this.outsideCells = new Pair<>(outsideRoom, new HashSet<>(outside));
            this.insideCells = new Pair<>(insideRoom, new HashSet<>(inside));

            roomChangingCells = new HashSet<>();
            roomChangingCells.addAll(outside);
            roomChangingCells.addAll(inside);

            if (outsideRoom == null ^ insideRoom == null) {

                exit = true;

                if (connectsInOut.getKey() == null) {
                    connectsInOut = new Pair<>(connectsInOut.getValue(), connectsInOut.getKey());
                    HashSet<Grid.Cell> temp = new HashSet<>(insideCells.getValue());
                    insideCells = outsideCells;
                    outsideCells = new Pair<>(null, temp);
                }
                if (connectsInOut.getValue() != null) {
                    throw new IllegalArgumentException("no Exit");
                }
            } else if (outsideRoom == null && insideRoom == null) {
                throw new IllegalStateException("This passage seems to go nowhere");
            } else {
                exit = false;
            }

        }

        /**
         * constructor that builds a door depending on the connectedCells,
         * which represent the door.
         *
         * @param connectedCells all the Cells on both sides of the Door.
         *                       they are given as pairs of cells that are
         *                       directly connected and that are on both sides
         *                       of the door.
         */
        private Door(HashSet<Grid.CellPair> connectedCells, int id) {

            // calls super method which assigns an id.
            super(id);
            this.connectedCells = connectedCells;

            // updates outside and inside Cells
            LinkedList<Grid.Cell> outside = new LinkedList<>();
            LinkedList<Grid.Cell> inside = new LinkedList<>();

            for (Grid.CellPair fp : connectedCells) {
                outside.add(fp.getCell1());
                inside.add(fp.getCell2());
            }

            // gets common Room of outside- and insideCells.
            // method throws an exception if not all cells are in the same room
            Room outsideRoom = grid.getRoom(outside);
            Room insideRoom = grid.getRoom(inside);

            // adds the Passage to the rooms.
            if (outsideRoom != null) outsideRoom.addPassage(this);
            if (insideRoom != null) insideRoom.addPassage(this);

            this.connectsInOut = new Pair<>(insideRoom, outsideRoom);

            this.outsideCells = new Pair<>(outsideRoom, new HashSet<>(outside));
            this.insideCells = new Pair<>(insideRoom, new HashSet<>(inside));

            roomChangingCells = new HashSet<>();
            roomChangingCells.addAll(outside);
            roomChangingCells.addAll(inside);

            if (outsideRoom == null ^ insideRoom == null) {

                exit = true;

                if (connectsInOut.getKey() == null) {
                    connectsInOut = new Pair<>(connectsInOut.getValue(), connectsInOut.getKey());
                    HashSet<Grid.Cell> temp = new HashSet<>(insideCells.getValue());
                    insideCells = outsideCells;
                    outsideCells = new Pair<>(null, temp);
                }
                if (connectsInOut.getValue() != null) {
                    throw new IllegalArgumentException("no Exit");
                }
            } else if (outsideRoom == null && insideRoom == null) {
                throw new IllegalStateException("This passage seems to go nowhere");
            } else {
                exit = false;
            }

        }

        @Override
        public Pair<Room, Room> getConnectsInOut() {
            return connectsInOut;
        }

        @Override
        public HashSet<Grid.Cell> getRoomChangingCells() {
            return new HashSet<>(roomChangingCells);
        }

        /**
         * @return all the connected Cells from the connectedCells HashSet
         */
        public HashSet<Grid.CellPair> getConnectedCells() {
            return new HashSet<>(connectedCells);
        }

        /**
         * @return null, since there is no exit-room for a door unless it's an exit
         */
        @Override
        public Room getExitRoom() {
            if (exit) {
                return connectsInOut.getKey();
            }
            return null;
        }

        /**
         * @param room the room in which the cells should be
         * @return the cells that belong to the specified Room; returns null if room
         * is not part of the door, returns the cells outside the building if room is null
         */
        @Override
        public HashSet<Grid.Cell> getRoomChangingCells(Room room) {
            if (room != null) {
                if (room.equals(insideCells.getKey())) {
                    return new HashSet<>(insideCells.getValue());
                }
                if (room.equals(outsideCells.getKey())) {
                    return new HashSet<>(outsideCells.getValue());
                }
            } else {
                if (isExit()) {
                    return new HashSet<>(outsideCells.getValue());
                }
            }
            return null;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Passage passage = (Passage) o;

            return id == passage.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

    }


    /**
     * Represents a stair in a building.
     *
     * @author Malte Bossert
     * @version 3.2
     */
    public class Stair extends Passage {

        /**
         * rooms that are connected by this stair; one might be null if it's an Exit
         */
        transient Pair<Room, Room> connectsInOut;


        //TODO
        LinkedList<Grid.Cell> stairCells;
        /**
         * the cells where the actual stair is
         */
        transient Pair<Room, HashSet<Grid.Cell>> stairCellsPair;
        /**
         * the cells inside (inside has only a meaning if this is an exit)
         */
        transient Pair<Room, HashSet<Grid.Cell>> insideCells;
        /**
         * the cells outside (outside has only a meaning if this is an exit, in this
         * case, the Key of this pair is null)
         */
        transient Pair<Room, HashSet<Grid.Cell>> outsideCells;
        /**
         * all cells from which you can change the room
         */
        transient HashSet<Grid.Cell> roomChangingCells;
        /**
         * the direction you walk to go up/down the street
         */
        int direction;

        /**
         * constructor that constructs a stair based on its cells and the direction.
         *
         * @param stairCellsPair
         * @param direction
         */
        private Stair(LinkedList<Grid.Cell> stairCellsPair, int direction, int id) {

            super(id);
            this.direction = direction;
            this.stairCells = new LinkedList<>(stairCellsPair);

            LinkedList<Grid.Cell> inside = new LinkedList<>();
            LinkedList<Grid.Cell> outside = new LinkedList<>();

            for (Grid.Cell cell : stairCellsPair) {
                if (!stairCellsPair.contains(cell.getNextCell(direction))) {
                    inside.add(cell.getNextCell(direction));
                }
                if (!stairCellsPair.contains(cell.getNextCell(DIR.getComplement(direction)))) {
                    outside.add(cell.getNextCell(DIR.getComplement(direction)));
                }
                cell.setStair();

            }
            Room outsideRoom = grid.getRoom(outside);
            Room insideRoom = grid.getRoom(inside);
            Room stairRoom = grid.getRoom(stairCellsPair);
            this.stairCellsPair = new Pair<>(stairRoom, new HashSet<>(stairCellsPair));
            roomChangingCells = new HashSet<>(stairCellsPair);
            exit = false;

            if (stairRoom == null) {
                exit = true;
                if (outsideRoom == null && insideRoom != null) {
                    roomChangingCells.addAll(inside);
                } else if (outsideRoom != null && insideRoom == null) {
                    roomChangingCells.addAll(outside);
                } else throw new IllegalArgumentException();
            } else if (outsideRoom == null || insideRoom == null) {
                exit = true;
                if (outsideRoom == null && insideRoom != null) {
                    roomChangingCells.addAll(outside);
                } else if (outsideRoom != null /* && insideRoom == null */) {
                    roomChangingCells.addAll(inside);
                } else throw new IllegalArgumentException();
            }

            if (outsideRoom != null) outsideRoom.addPassage(this);
            if (insideRoom != null) insideRoom.addPassage(this);

            if (exit && insideRoom == null) {
                this.connectsInOut = new Pair<>(outsideRoom, null);
                this.insideCells = new Pair<>(outsideRoom, new HashSet<>(outside));
                this.outsideCells = new Pair<>(null, new HashSet<>(inside));
            } else {
                this.connectsInOut = new Pair<>(insideRoom, outsideRoom);
                this.insideCells = new Pair<>(insideRoom, new HashSet<>(inside));
                this.outsideCells = new Pair<>(outsideRoom, new HashSet<>(outside));
            }

        }

        /**
         * constructor that constructs a stair based on its cells and the direction.
         *
         * @param stairCellsPair
         * @param direction
         */
        public Stair(LinkedList<Grid.Cell> stairCellsPair, int direction) {

            super();
            this.direction = direction;
            this.stairCells = new LinkedList<>(stairCellsPair);

            LinkedList<Grid.Cell> inside = new LinkedList<>();
            LinkedList<Grid.Cell> outside = new LinkedList<>();

            for (Grid.Cell cell : stairCellsPair) {
                if (!stairCellsPair.contains(cell.getNextCell(direction))) {
                    inside.add(cell.getNextCell(direction));
                }
                if (!stairCellsPair.contains(cell.getNextCell(DIR.getComplement(direction)))) {
                    outside.add(cell.getNextCell(DIR.getComplement(direction)));
                }
                cell.setStair();

            }
            Room outsideRoom = grid.getRoom(outside);
            Room insideRoom = grid.getRoom(inside);
            Room stairRoom = grid.getRoom(stairCellsPair);
            this.stairCellsPair = new Pair<>(stairRoom, new HashSet<>(stairCellsPair));
            roomChangingCells = new HashSet<>(stairCellsPair);
            exit = false;

            if (stairRoom == null) {
                exit = true;
                if (outsideRoom == null && insideRoom != null) {
                    roomChangingCells.addAll(inside);
                } else if (outsideRoom != null && insideRoom == null) {
                    roomChangingCells.addAll(outside);
                } else throw new IllegalArgumentException();
            } else if (outsideRoom == null || insideRoom == null) {
                exit = true;
                if (outsideRoom == null && insideRoom != null) {
                    roomChangingCells.addAll(outside);
                } else if (outsideRoom != null /* && insideRoom == null */) {
                    roomChangingCells.addAll(inside);
                } else throw new IllegalArgumentException();
            }

            if (outsideRoom != null) outsideRoom.addPassage(this);
            if (insideRoom != null) insideRoom.addPassage(this);

            if (exit && insideRoom == null) {
                this.connectsInOut = new Pair<>(outsideRoom, null);
                this.insideCells = new Pair<>(outsideRoom, new HashSet<>(outside));
                this.outsideCells = new Pair<>(null, new HashSet<>(inside));
            } else {
                this.connectsInOut = new Pair<>(insideRoom, outsideRoom);
                this.insideCells = new Pair<>(insideRoom, new HashSet<>(inside));
                this.outsideCells = new Pair<>(outsideRoom, new HashSet<>(outside));
            }

        }

        /**
         * @return the cells that belong to the stair
         */
        public Pair<Room, HashSet<Grid.Cell>> getStairCellsPair() {
            return new Pair<>(stairCellsPair.getKey(), new HashSet<>(stairCellsPair.getValue()));
        }

        /**
         * @return the cells that belong to the "inside", which has only a meaning if this is an exit
         * else: cells from a random side
         */
        public Pair<Room, HashSet<Grid.Cell>> getInsideCells() {
            return new Pair<>(insideCells.getKey(), new HashSet<>(insideCells.getValue()));
        }

        /**
         * @return the cells that belong to the "outside", which has only a meaning if this is an exit
         * else: cells from a random side
         */
        public Pair<Room, HashSet<Grid.Cell>> getOutsideCells() {
            return new Pair<>(outsideCells.getKey(), new HashSet<>(outsideCells.getValue()));
        }

        /**
         * @return direction on which one goes if one takes the stair
         */
        public int getDirection() {
            return direction;
        }

        /**
         * it's not an ExitStair
         *
         * @return null, because this is not an exit
         */
        @Override
        public Room getExitRoom() {
            if (exit) {
                return connectsInOut.getKey();
            }
            return null;
        }

        @Override
        public Pair<Room, Room> getConnectsInOut() {
            return connectsInOut;
        }

        @Override
        public boolean isStair() {
            return true;
        }

        @Override
        public HashSet<Grid.Cell> getRoomChangingCells() {
            return new HashSet<>(roomChangingCells);
        }

        @Override
        public HashSet<Grid.Cell> getRoomChangingCells(Room room) {
            if (room != null) {
                if (room.equals(insideCells.getKey())) {
                    if (insideCells.getKey().equals(stairCellsPair.getKey())) {
                        return stairCellsPair.getValue();
                    } else return insideCells.getValue();
                }
                if (!isExit()) {
                    if (room.equals(outsideCells.getKey())) {
                        if (outsideCells.getKey().equals(stairCellsPair.getKey())) {
                            return stairCellsPair.getValue();
                        } else return outsideCells.getValue();
                    }
                }
            } else {
                if (isExit()) {
                    if (stairCellsPair.getKey() == null) {
                        return stairCellsPair.getValue();
                    } else {
                        return outsideCells.getValue();
                    }
                }
            }
            return null;
        }
    }


    /**
     * represents a room in the building, specified by its cell in the left upper corner,
     * the cell in the right lower corner, its passages (in general) and its exits
     *
     * @author Malte Bossert
     * @version 3.2
     */
    public class Room {

        /**
         * cell in the left upper corner
         */
        private Grid.Cell beginning;
        /**
         * cell in the right lower corner
         */
        private Grid.Cell end;
        /**
         * the Room's unique id
         */
        public final int id;
        /**
         * a <code>HashSet</code> that contains all passages that belong to
         * this room and are no exits.
         */
        private transient HashSet<Passage> passages;
        /**
         * a <code>HashSet</code> that contains all exits.
         */
        private transient HashSet<Passage> exits;
        /**
         * a <code>HashSet</code> that contains every cell of this room.
         */
        private transient HashSet<Grid.Cell> cells;


        private Room(Grid.Cell beginning, Grid.Cell end, int id, HashSet<Passage> passages, HashSet<Passage> exits) {

            this.beginning = beginning;
            this.end = end;
            this.passages = passages;
            this.exits = exits;
            this.id = id;
            cells = calculateCells();

            for (Grid.Cell cell : cells) {
                cell.setRoom(this);
            }

        }

        /**
         * constructor that builds up a new Room. Also assigns room to each cell that belongs
         * to this room.
         *
         * @param beginning left upper grid-Cell
         * @param end       right lower grid-Cell
         * @param passages  all the Passages that lead to or from this room
         *                  except the exits.
         * @param exits     all the exits that leave from this room
         */
        public Room(Grid.Cell beginning, Grid.Cell end, HashSet<Passage> passages, HashSet<Passage> exits) {
            this(beginning, end, roomNumber++, passages, exits);
        }

        /**
         * specifies a room without any passages
         *
         * @param beginning left upper grid-Cell
         * @param end       right lower grid-Cell
         */
        public Room(Grid.Cell beginning, Grid.Cell end) {
            this(beginning, end, new HashSet<>(), new HashSet<>());
        }


        //TODO
        private Room(Grid.Cell beginning, Grid.Cell end, int id) {
            this(beginning, end, id, new HashSet<>(), new HashSet<>());
        }

        /**
         * calculates all Cells that belong to this Room
         *
         * @return the <code>HashSet</code> that contains all Cells in this room
         */
        private HashSet<Grid.Cell> calculateCells() {

            HashSet<Grid.Cell> tempCells = new HashSet<>();
            for (int i = beginning.getX(); i <= end.getX(); i++) {
                for (int j = beginning.getY(); j <= end.getY(); j++) {

                    tempCells.add(grid.getCell(i, j));

                }
            }
            return tempCells;
        }

        /**
         * adds a new Passage to the room.
         *
         * @param newPassage passage to be added to this room.
         */
        public void addPassage(Passage newPassage) {
            passages.add(newPassage);
        }

        /**
         * adds a new exit to this room.
         *
         * @param exit that will be added.
         */
        public void addExit(Passage exit) {
            exits.add(exit);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Room room = (Room) o;

            return id == room.id;
        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }

        /**
         * @return all the exits of this Room
         */
        public HashSet<Passage> getExits() {
            return new HashSet<>(exits);
        }

        /**
         * @return the left upper corner of this Room
         */
        public Grid.Cell getBeginning() {
            return beginning;
        }

        /**
         * @return the right lower corner of this Room
         */
        public Grid.Cell getEnd() {
            return end;
        }

        /**
         * @return all the passages that are connected to this room
         */
        public HashSet<Passage> getPassages() {
            return new HashSet<>(passages);
        }

        //TODO
        public boolean isPassage(Grid.Cell cell1, Grid.Cell cell2) {
            if (!cell1.getRoom().equals(cell2.getRoom())) {
                for (Passage passage : passages) {
                    if (passage.getRoomChangingCells().contains(cell1) &&
                            passage.getRoomChangingCells().contains(cell2)) {
                        return true;
                    }
                }
                for (Passage exit : exits) {
                    if (exit.getRoomChangingCells().contains(cell1) &&
                            exit.getRoomChangingCells().contains(cell2)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "Room{" +
                    "beginning=" + beginning +
                    ", end=" + end +
                    ", id=" + id +
                    '}';
        }
    }

    public static class BuildingInstanceCreator implements JsonDeserializer<Building> {

        @Override
        public Building deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject buildingObject = json.getAsJsonObject();
            String name = buildingObject.get("name").getAsString();
            int gridSizeX = buildingObject.get("gridSizeX").getAsInt();
            int gridSizeY = buildingObject.get("gridSizeY").getAsInt();
            Building building = new Building(name, gridSizeX, gridSizeY);

            JsonArray roomsObject = buildingObject.getAsJsonArray("rooms");
            for (JsonElement temp : roomsObject) {
                JsonObject tempRoom = temp.getAsJsonObject();
                deserializeRoom(tempRoom, building);
            }

            JsonArray doorsObject = buildingObject.getAsJsonArray("doors");
            for (JsonElement temp : doorsObject) {
                JsonObject tempDoor = temp.getAsJsonObject();
                deserializeDoor(tempDoor, building);
            }

            JsonArray stairsObject = buildingObject.getAsJsonArray("stairs");
            for (JsonElement temp : stairsObject) {
                JsonObject tempStair = temp.getAsJsonObject();
                deserializeStair(tempStair, building);
            }

            building.updateNumbers();

            return building;
        }

        private void deserializeRoom(JsonObject json, Building building) {
            int id = json.get("id").getAsInt();
            Grid.Cell beginning = deserializeCell(json.getAsJsonObject("beginning"), building);
            Grid.Cell end = deserializeCell(json.getAsJsonObject("end"), building);
            building.addRoom(beginning, end, id);
        }

        private void deserializeDoor(JsonObject json, Building building) {
            int id = json.get("id").getAsInt();

            HashSet<Grid.CellPair> connectedCells = new HashSet<>();
            JsonArray tempCellPairs = json.getAsJsonArray("connectedCells");
            for (JsonElement temp : tempCellPairs) {
                JsonObject tempCellPair = temp.getAsJsonObject();
                Grid.Cell cell1 = deserializeCell(tempCellPair.getAsJsonObject("cell1"), building);
                Grid.Cell cell2 = deserializeCell(tempCellPair.getAsJsonObject("cell2"), building);
                connectedCells.add(building.getGrid().getCellPair(cell1, cell2));
            }
            building.addDoor(connectedCells, id);

        }

        private void deserializeStair(JsonObject json, Building building) {
            int id = json.get("id").getAsInt();
            int direction = json.get("direction").getAsInt();

            LinkedList<Grid.Cell> stairCells = new LinkedList<>();
            JsonArray tempStairCells = json.getAsJsonArray("stairCells");
            for (JsonElement temp : tempStairCells) {
                JsonObject tempCell = temp.getAsJsonObject();
                stairCells.add(deserializeCell(tempCell, building));
            }
            building.addStair(stairCells, direction, id);
        }

        private Grid.Cell deserializeCell(JsonObject tempCell, Building building) {
            return building.getCell(tempCell.get("x").getAsInt(), tempCell.get("y").getAsInt());
        }
    }

}