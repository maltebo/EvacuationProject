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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Represents a complete building with rooms, passages and people.
 * Can be in two states: either in normal running state or in evacuation-mode,
 * where all people are evacuated according to the current strategy in <code>MovementModule</code>,
 * <code>PathOntology</code> and <code>EmergencyStrategy</code>
 *
 * @author Malte Bossert
 * @version 3.2
 */
public class Building {


    /**
     * the grid of all cells the building consists of.
     */
    private transient Grid grid;
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

    /**
     * the number of floors in this Building
     */
    public final int floors;

    /**
     * the number of rooms in this building, is used for id creation
     */
    private transient int roomNumber = 0;

    /**
     * the number of passages in this building, is used for id creation
     */
    private transient int passageNumber = 0;

    /**
     * the evacuation strategy in case of evacuation. gets assigned when
     * an emergency is necessary.
     */
    private transient EvacuationStrategy evacuationStrategy;

    /**
     * the states in which the building can be in, namely normal and evacuation mode.
     */
    public enum STATE {
        NORMAL,
        STANDSTILL,
        EVACUATION
    }

    /**
     * the average number of people in the building.
     */
    private transient int averageCapacity;

    /**
     * a <code>HashSet</code> of all persons that are currently in the building.
     *
     * @see Person
     * @see Building#toJSONstringWithoutPeople()
     */
    private HashSet<Person> personsInBuilding;

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
     */
    private HashSet<Door> doors;

    /**
     * a <code>HashSet</code> of all Stairs in the building
     *
     * @see Passage
     * @see Stair
     */
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
     * @see Cell
     */
    private transient HashSet<Cell> entryCells;

    /**
     * the <code>Singleton</code> instance of this building.
     */

    public Building() {
        this("empty", 1, 1, 1, 0);
    }


    /**
     * copy Constructor, constructs the new Building by taking its basic components
     * and copying them without copying the actual state of the building.
     * Number and location of people, kind of emergency-management etc. will not be
     * copied.
     *
     * @param building the <code>Buidling</code> that will be copied
     */
    public Building(Building building) {

        this(building.name, building.gridSizeX, building.gridSizeY, building.floors);
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
            for (Cell cell : stair.lowerStairCells) {
                tempCells.add(grid.getCell(cell));
            }

            addStair(tempCells, stair.changeFloor, stair.direction, stair.id);
        }

        updateNumbers();

        this.averageCapacity = 0;
        this.personsInBuilding = new HashSet<>();
        this.state = STATE.NORMAL;

    }


    /**
     * the full constructor that creates a new building.
     *
     * @param name            Name of the building.
     * @param gridSizeX       grid size in x-direction.
     * @param gridSizeY       grid size in y-direction.
     * @param averageCapacity average Capacity of people in this building.
     * @param numberOfFloors  the number of Floors of this building.
     */
    public Building(String name, int gridSizeX, int gridSizeY, int numberOfFloors, int averageCapacity) {
        this.name = name;
        this.gridSizeX = gridSizeX;
        this.gridSizeY = gridSizeY;
        this.floors = numberOfFloors;
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
     * constructor that creates a new building and sets the number of people
     * inside to zero.
     *
     * @param name           Name of the building.
     * @param gridSizeX      grid size in x-direction.
     * @param gridSizeY      grid size in y-direction.
     * @param numberOfFloors the number of Floors of this building.
     */
    public Building(String name, int gridSizeX, int gridSizeY, int numberOfFloors) {
        this(name, gridSizeX, gridSizeY, numberOfFloors, 0);
    }

    /**
     * creates the grid with all cells that could be part of the building (according to
     * specified grid-Size).
     *
     * @see Cell
     */
    private void createGrid() {
        grid = new Grid(this);
    }

    /**
     * this Method updates the number of rooms and passages. Will be called after a building
     * got created from a JSON-file, because the ids are already assigned.
     * Usually, the parameters will be set automatically by the constructor of a new
     * <code>Passage</code> and a new <code>Room</code>
     */
    private void updateNumbers() {
        passageNumber = getPassages().size();
        roomNumber = rooms.size();
    }

    /**
     * @return the grid of the building
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * returns a Cell in this building. Does the same as
     * <code>[building].getGrid().getCell(x,y)</code>
     *
     * @param x the x value of the cell
     * @param y the y value of the cell
     * @param f floor of the cell
     * @return the fully specified cell (with all its parameters)
     */
    public Cell getCell(int x, int y, int f) {
        return grid.getCell(x, y, f);
    }


    public Cell getCell(int x, int y) {
        return grid.getCell(x, y, 0);
    }


    /**
     * facilityClass, does the same as
     * <code>#getCellPair(#getCell(x1,y1,floor1),#getCell(x2,y2,floor2))</code>
     *
     * @param x1     x Value of first Cell
     * @param y1     y Value of first Cell
     * @param floor1 floor of first cell
     * @param x2     x Value of second Cell
     * @param y2     y Value of second Cell
     * @param floor2 floor of second cell
     * @return the full <code>CellPair</code>
     */
    public CellPair getCellPair(int x1, int y1, int floor1, int x2, int y2, int floor2) {

        return getCellPair(getCell(x1, y1, floor1), getCell(x2, y2, floor2));

    }

    public CellPair getCellPair(int x1, int y1, int x2, int y2) {
        return getCellPair(x1, y1, 0, x2, y2, 0);
    }

    /**
     * returns a <code>CellPair</code> as specified by the two cells
     *
     * @param c1 first Cell
     * @param c2 second Cell
     * @return the CellPair consistent of the two above mentioned Cells
     */
    public CellPair getCellPair(Cell c1, Cell c2) {

        return grid.getCellPair(c1, c2);

    }

    /**
     * calls the method that calculates the distance between
     * two cells in <code>Grid</code>
     *
     * @param c1 Cell 1
     * @param c2 Cell 2
     * @return the distance in float
     * @see Grid#distance(Cell, Cell)
     */
    public float distance(Cell c1, Cell c2) {
        return grid.distance(c1, c2);
    }

    /**
     * calls the method that calculates the distance between a cell
     * and a Set of cells in <code>Grid</code>
     *
     * @param c1 Cell 1
     * @param c2 Cell 2
     * @return the distance in float
     * @see Grid#distance(Cell, HashSet)
     */
    public float distance(Cell c1, HashSet<Cell> c2) {
        return grid.distance(c1, c2);
    }

    /**
     * adds a new Room to the Building
     *
     * @param leftUp    the cell of the left upper part of the room
     * @param rightDown the cell of the right lower part of the room
     */
    public void addRoom(Cell leftUp, Cell rightDown) {
        rooms.add(new Room(leftUp, rightDown));
    }

    /**
     * adds a Room including its id. Can only be called from this class and will be
     * used if a building gets rebuilt from a JSON file.
     *
     * @param leftUp    the cell of the left upper part of the room
     * @param rightDown the cell of the right lower part of the room
     * @param id        the rooms unique id
     */
    private void addRoom(Cell leftUp, Cell rightDown, int id) {
        rooms.add(new Room(leftUp, rightDown, id));
    }

    /**
     * creates a Building from a JSON file by using a <code>Gson</code>
     *
     * @param json the JSON file
     * @return the created Building
     */
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

    /**
     * calculates a Building from a JSON String
     *
     * @param json the String that specifies the Building
     * @return the Building expressed by the String
     * @see Building#fromJSON(File)
     */
    public static Building fromJSON(String json) {

        try {
            GsonBuilder gb = new GsonBuilder();
            gb.registerTypeAdapter(Building.class, new BuildingInstanceCreator());
            Gson gson = gb.create();
            return gson.fromJson(json, Building.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * creates a new JSON-File from this Building
     */
    public void makeJSONfile(boolean people, File file) {
        String json;
        if (people) {
            json = toJSONstringWithPeople();
        } else {
            json = toJSONstringWithoutPeople();
        }

        if (!file.exists()) {
            try {
                file.createNewFile();
                file.setWritable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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


    /**
     * creates a new JSON-File from this Building
     */
    public void makeJSONfile(boolean people) {
        String json;
        if (people) {
            json = toJSONstringWithPeople();
        } else {
            json = toJSONstringWithoutPeople();
        }
        String name = this.name.replaceAll("\\s+", "");
        if (people) {
            name += "WithPeople";
        }

        ClassLoader cl = getClass().getClassLoader();
        String resourcePath = cl.getResource("buildingsJson").getPath();


        File file = new File(resourcePath + "/" + name + ".json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                file.setWritable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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

    /**
     * @return the String that specifies this Building as a JSON
     */
    private String toJSONstringWithPeople() {

        String json = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            json = gson.toJson(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;

    }

    private String toJSONstringWithoutPeople() {

        String json = null;
        try {
            GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
            gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                    return fieldAttributes.getName().equals("personsInBuilding");
                }

                @Override
                public boolean shouldSkipClass(Class<?> aClass) {
                    return false;
                }
            });
            Gson gson = gsonBuilder.create();
            json = gson.toJson(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return json;

    }

    /**
     * adds a Door to the Building using the connected cells of this Door.
     *
     * @param connectedCells a Set of CellPairs of Cells that are directly connected
     */
    public void addDoor(HashSet<CellPair> connectedCells) {
        Door door = new Door(connectedCells);
        doors.add(door);

        addExit(door);
    }

    /**
     * adds a Door to this Building using the connected Cells of this Door.
     * The id is already specified, so that this Method will only be used
     * when rebuilding an already existing Building.
     *
     * @param connectedCells Pairs of cells that are connected, are in different rooms
     *                       and belong to this Door
     * @param id             the id of this Door
     */
    private void addDoor(HashSet<CellPair> connectedCells, int id) {
        Door door = new Door(connectedCells, id);
        doors.add(door);

        addExit(door);
    }

    /**
     * adds a stair to this building. a Stair is specified by the cells belonging
     * to the stair and the direction of the stair.
     *
     * @param stairCells the cells that are occupied by this stair
     * @param direction  the direction in which you can use the stair
     */
    @Deprecated
    public void addStair(LinkedList<Cell> stairCells, int direction) {

        addStair(stairCells, false, direction);

    }

    /**
     * adds a stair to this building. a Stair is specified by the cells belonging
     * to the stair and the direction of the stair.
     *
     * @param stairCells  the cells that are occupied by this stair
     * @param changeFloor if the stair stays in the same floor or if it is used to change
     *                    the floor
     * @param direction   the direction in which you can use the stair
     */
    public void addStair(LinkedList<Cell> stairCells, boolean changeFloor, int direction) {

        Stair stair = new Stair(stairCells, changeFloor, direction);
        stairs.add(stair);

        addExit(stair);

    }

    /**
     * adds a stair to this building. a Stair is specified by the cells belonging
     * to the stair and the direction of the stair. This method is called when the
     * id is already known, when a building is rebuilt from a JSON file
     *
     * @param stairCells the cells that are occupied by this stair
     * @param direction  the direction in which you can use the stair
     * @param id         the id of this Stair
     */
    private void addStair(LinkedList<Cell> stairCells, boolean changeFloor, int direction, int id) {

        Stair stair = new Stair(stairCells, changeFloor, direction, id);
        stairs.add(stair);

        addExit(stair);

    }

    /**
     * checks whether a given passage is an exit, and if it is, adds this passage
     * to the list of exits
     *
     * @param passage the passage to be checked
     */
    private void addExit(Passage passage) {
        if (passage.isExit()) {
            Room room = passage.getExitRoom();
            room.addExit(passage);
            exits.add(passage);
            entryCells.addAll(passage.getRoomChangingCells(null));
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
    public HashSet<Cell> getEntryCells() {
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

    /**
     * @param number the number to which the average capacity of this building shall be set
     */
    public void setAverageCapacity(int number) {
        this.averageCapacity = number;
    }


    /**
     * sets the State of the building to evacuation-mode and tells every person to
     * move outside the building.
     *
     * @param es the used EvacuationStrategy, gets called in the Menu depending on user input
     */
    public void startEvacuation(EvacuationStrategy es) {

        this.evacuationStrategy = es;

        this.state = STATE.EVACUATION;

        evacuationStrategy.startEvacuation();

    }

    /**
     * @return the actual evacuation Strategy of the building
     */
    public EvacuationStrategy getEvacuationStrategy() {
        return evacuationStrategy;
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
        //personCopy.sort(Comparator.comparingInt(Person::getId));
        Collections.shuffle(personCopy);
        // lets persons leave the building with some earlier defined probability
        // <code>pDeletePerson</code>
        while (true) {
            if (!personCopy.isEmpty()) {
                Person person = personCopy.poll();
                if ((person.getState() != Person.STATE.GOTOROOM || person.getGoalRoom() != null)
                        && person.getState() != Person.STATE.STANDSTILL) {
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
        if (state != STATE.EVACUATION && state != STATE.STANDSTILL) {
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
                for (Cell cell : getEntryCells()) {
                    if (!cell.isOccupied() && !cell.isStair()) {
                        addPerson(new Person(true, this));
                        break;
                    }
                }
            } else {


                for (Cell cell : getEntryCells()) {
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

        /**
         * states whether this Passage is an exit
         */
        transient boolean exit;

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
        public abstract HashSet<Cell> getRoomChangingCells();

        /**
         * @param room the room in which the cells should be
         * @return all cells that belong to this passage and are in the
         * specified room
         */
        public abstract HashSet<Cell> getRoomChangingCells(Room room);

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
        HashSet<CellPair> connectedCells;

        /**
         * all cells that are on the outside.
         * outside is assigned randomly; in case this is an exit,
         * outside will be the cells outside the building.
         */
        transient Pair<Room, HashSet<Cell>> outsideCells;
        /**
         * all cells that are on the inside.
         * inside is assigned randomly; in case this is an exit,
         * inside will be the cells inside the building.
         */
        transient Pair<Room, HashSet<Cell>> insideCells;

        /**
         * all single cells that this door consists of. In difference to
         * connectedCells, this returns single cells, no cellPairs.
         */
        transient HashSet<Cell> roomChangingCells;

        /**
         * constructor that builds a door depending on the connectedCells,
         * which represent the door.
         *
         * @param connectedCells all the Cells on both sides of the Door.
         *                       they are given as pairs of cells that are
         *                       directly connected and that are on both sides
         *                       of the door.
         */
        Door(HashSet<CellPair> connectedCells) {

            this(connectedCells, passageNumber++);
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
        private Door(HashSet<CellPair> connectedCells, int id) {

            // calls super method which assigns an id.
            super(id);
            this.connectedCells = connectedCells;

            // updates outside and inside Cells
            LinkedList<Cell> outside = new LinkedList<>();
            LinkedList<Cell> inside = new LinkedList<>();

            for (CellPair fp : connectedCells) {
                outside.add(fp.getCell1());
                inside.add(fp.getCell2());
            }

            // gets common Room of outside- and lowerCells.
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
                    HashSet<Cell> temp = new HashSet<>(insideCells.getValue());
                    insideCells = outsideCells;
                    outsideCells = new Pair<>(null, temp);
                }
                if (connectsInOut.getValue() != null) {
                    throw new IllegalArgumentException("no Exit");
                }
            } else if (outsideRoom == null) {
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
        public HashSet<Cell> getRoomChangingCells() {
            return new HashSet<>(roomChangingCells);
        }

        /**
         * @return all the connected Cells from the connectedCells HashSet
         */
        public HashSet<CellPair> getConnectedCells() {
            return new HashSet<>(connectedCells);
        }

        /**
         * @return if this is an exit, return the room from which the exit is accessible
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
        public HashSet<Cell> getRoomChangingCells(Room room) {
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

        /**
         * the list of cells that are occupied by this stair
         */
        LinkedList<Cell> lowerStairCells;
        /**
         * the cells where the actual stair is
         */
        transient Pair<Room, HashSet<Cell>> stairCellsPair1;
        /**
         * the cells where the actual stair is
         */
        transient Pair<Room, HashSet<Cell>> stairCellsPair2;
        /**
         * the cells inside (inside has only a meaning if this is an exit)
         */
        transient Pair<Room, HashSet<Cell>> lowerCells;
        /**
         * the cells outside (outside has only a meaning if this is an exit, in this
         * case, the Key of this pair is null)
         */
        transient Pair<Room, HashSet<Cell>> higherCells;
        /**
         * all cells from which you can change the room
         */
        transient HashSet<Cell> roomChangingCells;
        /**
         * the direction you walk to go up/down the street
         */
        final int direction;

        final boolean changeFloor;

        /**
         * constructor that constructs a stair based on its cells and the direction, as
         * well as the id.
         *
         * @param lowerStairCells the cells that are occupied by this stair
         * @param direction       the direction of the stair
         * @param id              the id of this passage
         */
        private Stair(LinkedList<Cell> lowerStairCells, boolean changeFloor, int direction, int id) {

            super(id);
            this.direction = direction;
            this.changeFloor = changeFloor;
            this.lowerStairCells = new LinkedList<>(lowerStairCells);
            LinkedList<Cell> upperStairCells = new LinkedList<>();

            LinkedList<Cell> higher = new LinkedList<>();
            LinkedList<Cell> lower = new LinkedList<>();

            // normal, just calculating the cells
            if (!changeFloor) {
                for (Cell cell : lowerStairCells) {
                    if (!lowerStairCells.contains(cell.getNextCell(direction, false))) {
                        higher.add(cell.getNextCell(direction, false));
                    }
                    if (!lowerStairCells.contains(cell.getNextCell(DIR.getComplement(direction), false))) {
                        lower.add(cell.getNextCell(DIR.getComplement(direction), false));
                    }

                    upperStairCells.add(cell);
                    cell.setStair();

                }
            }
            // calculating the upper Room
            else {
                for (Cell cell : lowerStairCells) {

                    Cell upperStairCell = cell.getNextCell(DIR.STAY, 1);
                    upperStairCells.add(upperStairCell);
                    cell.setStair();
                    upperStairCell.setStair();

                    // if the next cell is not part of the staircells, we add it to the
                    // upper Cells
                    if (!lowerStairCells.contains(cell.getNextCell(direction, false))) {
                        higher.add(upperStairCell.getNextCell(direction, false));
                    }
                    // if the next cell in the wrong direction ("downstairs") is not part
                    // of the staircells, we add it to the lower Cells
                    if (!lowerStairCells.contains(cell.getNextCell(DIR.getComplement(direction), false))) {
                        lower.add(cell.getNextCell(DIR.getComplement(direction), false));
                    }

                }

            }
            Room lowerRoom = grid.getRoom(lower);

            Room higherRoom = grid.getRoom(higher);
            Room stairRoom1 = grid.getRoom(lowerStairCells);
            Room stairRoom2 = grid.getRoom(upperStairCells);
            this.stairCellsPair1 = new Pair<>(stairRoom1, new HashSet<>(lowerStairCells));
            this.stairCellsPair2 = new Pair<>(stairRoom2, new HashSet<>(upperStairCells));

            roomChangingCells = new HashSet<>(lowerStairCells);
            roomChangingCells.addAll(higher);

            exit = false;

            // if the stair is in the outside, we look for the end of the stairs that is not in the outside
            if (stairRoom1 == null && stairRoom2 == null) {
                exit = true;
                if (lowerRoom == null && higherRoom != null) {
                    roomChangingCells.addAll(higher);
                } else if (lowerRoom != null && higherRoom == null) {
                    roomChangingCells.addAll(lower);
                } else throw new IllegalArgumentException();
            }
            // if one of the other rooms is in the outside, we look for which one it is and add its fields to the
            // cells to change a room
            else if (lowerRoom == null || higherRoom == null) {
                exit = true;
                if (lowerRoom == null && higherRoom != null) {
                    roomChangingCells.addAll(lower);
                } else if (lowerRoom != null /* && higherRoom == null */) {
                    roomChangingCells.addAll(higher);
                } else throw new IllegalArgumentException();
            }

            if (lowerRoom != null) lowerRoom.addPassage(this);
            if (higherRoom != null) higherRoom.addPassage(this);

            this.connectsInOut = new Pair<>(lowerRoom, higherRoom);
            this.lowerCells = new Pair<>(lowerRoom, new HashSet<>(lower));
            this.higherCells = new Pair<>(higherRoom, new HashSet<>(higher));

        }

        /**
         * constructor that constructs a stair based on its cells and the direction.
         *
         * @param lowerStairCells the cells that are occupied by this stair
         * @param direction       the direction of this stair
         */
        Stair(LinkedList<Cell> lowerStairCells, boolean changeFloors, int direction) {

            this(lowerStairCells, changeFloors, direction, passageNumber++);

        }

        //TODO
        public HashSet<Cell> getStairCells() {
            HashSet<Cell> sCells = new HashSet<>();
            sCells.addAll(stairCellsPair1.getValue());
            sCells.addAll(stairCellsPair2.getValue());
            return sCells;
        }

        /**
         * @return the cells that belong to the stair
         */
        public Pair<Room, HashSet<Cell>> getStairCellsPair1() {
            return new Pair<>(stairCellsPair1.getKey(), new HashSet<>(stairCellsPair1.getValue()));
        }

        /**
         * @return the cells that belong to the stair
         */
        public Pair<Room, HashSet<Cell>> getStairCellsPair2() {
            return new Pair<>(stairCellsPair2.getKey(), new HashSet<>(stairCellsPair2.getValue()));
        }

        /**
         * @return the cells that belong to the "inside", which has only a meaning if this is an exit
         * else: cells from a random side
         */
        public HashSet<Cell> getLowerCells() {
            return lowerCells.getValue();
        }

        /**
         * @return the cells that belong to the "outside", which has only a meaning if this is an exit
         * else: cells from a random side
         */
        public HashSet<Cell> getHigherCells() {
            return new HashSet<>(higherCells.getValue());
        }

        /**
         * @return direction on which one goes if one takes the stair
         */
        public int getDirection() {
            return direction;
        }

        /**
         * if this is an Exit, return the room in from which the exit is accessible
         */
        @Override
        public Room getExitRoom() {
            if (exit) {
                if (connectsInOut.getKey() == null) {
                    return connectsInOut.getValue();
                } else {
                    return connectsInOut.getKey();
                }
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

        public boolean isInFloor(int floor) {

            if (connectsInOut.getKey() != null) {
                if (connectsInOut.getKey().getFloor() == floor) return true;
            }
            if (connectsInOut.getValue() != null) {
                if (connectsInOut.getValue().getFloor() == floor) return true;
            }
            return false;

        }

        public int getHigherFloor() {

            return higherCells.getKey().floor;

        }


        public int getLowerFloor() {

            if (lowerCells.getKey() != null) {
                return lowerCells.getKey().floor;
            } else return 0;

        }


        @Override
        public HashSet<Cell> getRoomChangingCells() {
            return new HashSet<>(roomChangingCells);
        }

        @Override
        public HashSet<Cell> getRoomChangingCells(Room room) {
            if (room != null) {
                HashSet<Cell> changingCells = new HashSet<>();

                for (Cell cell : getRoomChangingCells()) {
                    if (room.equals(cell.getRoom())) {
                        changingCells.add(cell);
                    }
                }

                return changingCells;
            } else {

                HashSet<Cell> changingCells = new HashSet<>();
                for (Cell cell : getRoomChangingCells()) {
                    if (cell.getRoom() == null) {
                        changingCells.add(cell);
                    }
                }
                return changingCells;
            }


            /*if (room != null) {
                if (changeFloor) {
                    if (room.equals(stairCellsPair1.getKey())) {
                        return stairCellsPair1.getValue();
                    } else if (room.equals(higherCells.getKey())) {
                        return higherCells.getValue();
                    }
                } else {
                    if (room.equals(lowerCells.getKey())) {
                        if (lowerCells.getKey().equals(stairCellsPair1.getKey())) {
                            return stairCellsPair1.getValue();
                        } else {
                            return lowerCells.getValue();
                        }
                    } else if (room.equals(higherCells.getKey())) {
                        if (room.equals(stairCellsPair2.getKey())) {
                            return stairCellsPair2.getValue();
                        } else return higherCells.getValue();
                    }
                }
            } else {
                if (isExit()) {
                    if (stairCellsPair1.getKey() == null) {
                        return stairCellsPair1.getValue();
                    } else if (higherCells.getKey() == null) {
                        return higherCells.getValue();
                    } else {
                        return lowerCells.getValue();
                    }
                }
            }
            return null;*/
        }

        @Override
        public String toString() {
            return "Stair: " + id + "\nStair Cells: " + getStairCells() + "\nlower Room: " + lowerCells.getKey() +
                    "\nhigher Room: " + higherCells.getKey();
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
        private Cell beginning;
        /**
         * cell in the right lower corner
         */
        private Cell end;

        private transient int floor;
        /**
         * the Room's unique id
         */
        final int id;
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
        private transient HashSet<Cell> cells;


        /**
         * constructor that builds up a new Room. Also assigns room to each cell that belongs
         * to this room. It already gets the id.
         *
         * @param beginning left upper grid-Cell
         * @param end       right lower grid-Cell
         * @param id        the id of this room
         */
        private Room(Cell beginning, Cell end, int id) {

            this.beginning = beginning;
            this.end = end;
            this.passages = new HashSet<>();
            this.exits = new HashSet<>();
            this.id = id;
            this.floor = beginning.getFloor();
            if (floor != end.getFloor()) throw new IllegalStateException("Room needs to be in one floor");
            cells = calculateCells();

            for (Cell cell : cells) {
                cell.setRoom(this);
            }

        }

        /**
         * specifies a new room
         *
         * @param beginning left upper grid-Cell
         * @param end       right lower grid-Cell
         */
        Room(Cell beginning, Cell end) {
            this(beginning, end, roomNumber++);
        }


        /**
         * calculates all Cells that belong to this Room
         *
         * @return the <code>HashSet</code> that contains all Cells in this room
         */
        private HashSet<Cell> calculateCells() {

            HashSet<Cell> tempCells = new HashSet<>();
            for (int i = beginning.getX(); i <= end.getX(); i++) {
                for (int j = beginning.getY(); j <= end.getY(); j++) {

                    tempCells.add(grid.getCell(i, j, floor));

                }
            }
            return tempCells;
        }

        /**
         * adds a new Passage to the room.
         *
         * @param newPassage passage to be added to this room.
         */
        void addPassage(Passage newPassage) {
            passages.add(newPassage);
        }

        /**
         * adds a new exit to this room.
         *
         * @param exit that will be added.
         */
        void addExit(Passage exit) {
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
        public Cell getBeginning() {
            return beginning;
        }

        /**
         * @return the right lower corner of this Room
         */
        public Cell getEnd() {
            return end;
        }

        public int getFloor() {
            return floor;
        }

        /**
         * @return all the passages that are connected to this room
         */
        public HashSet<Passage> getPassages() {
            return new HashSet<>(passages);
        }

        /**
         * calculates this room has a passage that can be passed by changing from
         * one cell to the other one
         *
         * @param cell1 first Cell
         * @param cell2 second Cell
         * @return whether there is a passage between the two
         */
        public boolean isPassage(Cell cell1, Cell cell2) {
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
            int floors = buildingObject.get("floors").getAsInt();
            Building building = new Building(name, gridSizeX, gridSizeY, floors);

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


            if (buildingObject.get("personsInBuilding") != null) {

                JsonArray people = buildingObject.getAsJsonArray("personsInBuilding");


                for (JsonElement temp : people) {
                    JsonObject person = temp.getAsJsonObject();
                    deserializePerson(person, building);
                }

                building.state = STATE.STANDSTILL;

            }

            return building;
        }

        /**
         * creates a new Room from a json-fragment describing a room
         *
         * @param json     the json that represents the room
         * @param building the building this room will be added to
         */
        private void deserializeRoom(JsonObject json, Building building) {
            int id = json.get("id").getAsInt();
            Cell beginning = deserializeCell(json.getAsJsonObject("beginning"), building);
            Cell end = deserializeCell(json.getAsJsonObject("end"), building);
            building.addRoom(beginning, end, id);
        }


        /**
         * creates a new Room from a json-fragment describing a room
         *
         * @param json     the json that represents the room
         * @param building the building this room will be added to
         */
        private void deserializePerson(JsonObject json, Building building) {
            int id = json.get("id").getAsInt();
            String name = json.get("name").getAsString();
            boolean isDisabled = json.get("isDisabled").getAsBoolean();
            Cell isOnCell = deserializeCell(json.get("isOnCell").getAsJsonObject(), building);

            building.addPerson(new Person(name, isOnCell, isDisabled, Person.STATE.STANDSTILL, building));
        }

        /**
         * creates a new Door from a json-fragment describing a Door
         *
         * @param json     the json that represents the Door
         * @param building the building this Door will be added to
         */
        private void deserializeDoor(JsonObject json, Building building) {
            int id = json.get("id").getAsInt();

            HashSet<CellPair> connectedCells = new HashSet<>();
            JsonArray tempCellPairs = json.getAsJsonArray("connectedCells");
            for (JsonElement temp : tempCellPairs) {
                JsonObject tempCellPair = temp.getAsJsonObject();
                Cell cell1 = deserializeCell(tempCellPair.getAsJsonObject("cell1"), building);
                Cell cell2 = deserializeCell(tempCellPair.getAsJsonObject("cell2"), building);
                connectedCells.add(building.getGrid().getCellPair(cell1, cell2));
            }
            building.addDoor(connectedCells, id);

        }

        /**
         * creates a new Stair from a json-fragment describing a Stair
         *
         * @param json     the json that represents the Stair
         * @param building the building this Stair will be added to
         */
        private void deserializeStair(JsonObject json, Building building) {
            int id = json.get("id").getAsInt();
            int direction = json.get("direction").getAsInt();
            boolean changeFloor = json.get("changeFloor").getAsBoolean();

            LinkedList<Cell> stairCells = new LinkedList<>();
            JsonArray tempStairCells = json.getAsJsonArray("lowerStairCells");
            for (JsonElement temp : tempStairCells) {
                JsonObject tempCell = temp.getAsJsonObject();
                stairCells.add(deserializeCell(tempCell, building));
            }
            building.addStair(stairCells, changeFloor, direction, id);
        }

        /**
         * creates a new Cell from a json-fragment describing a Cell
         *
         * @param tempCell the json that represents the Stair
         * @param building the building this Stair will be added to
         */
        private Cell deserializeCell(JsonObject tempCell, Building building) {
            return building.getCell(tempCell.get("x").getAsInt(), tempCell.get("y").getAsInt(),
                    tempCell.get("floor").getAsInt());
        }
    }

}