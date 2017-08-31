package model.graph.useragent;

import model.graph.building.Building;
import model.graph.building.DIR;
import model.graph.building.Grid;
import model.graph.building.Grid.*;
import model.graph.Path;
import model.graph.PathOntology;
import model.graph.building.Building.Passage;
import model.graph.building.Building.Room;
import model.graph.evacuation.EvacuationStrategy;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Module is used to determine the next step for the an agent. This depends on the
 * agent's current goal and its current position.
 *
 * @author Malte Bossert
 * @version 3.2
 */
class MovementModule {

    private static LinkedList<MovementModule> existingInstances = new LinkedList<>();
    private Building building;
    private PathOntology po;
    private EvacuationStrategy evacuationStrategy;

    private MovementModule(Building building) {
        this.building = building;
        po = PathOntology.getInstance(building);
    }

    public static MovementModule getMovementModule (Building building) {

        for (MovementModule mm : existingInstances) {
            if (mm.building == building) {
                return mm;
            }
        }
        MovementModule temp = new MovementModule(building);
        existingInstances.add(temp);
        return temp;
    }

    /**
     * this class determines the person's next step.
     *
     * @param person the person that shall be moved
     * @return a direction, specified by an (TODO) integer
     */
    int move(Person person) {

        Grid.Cell cell = person.getIsOnCell();

        // if the person is outside the building and does not want to get in, it gets removed
        // from the list of persons
        if (cell.isOutside() && !(person.getState() == Person.STATE.GETINTOBUILDING)) {
            person.remove();
            return DIR.STAY;
        }

        // if the person should get into the building, the directions into the building
        // are searched and a valid one is found and returned if possible
        if (person.getState() == Person.STATE.GETINTOBUILDING) {

            LinkedList<Integer> dir = getDirectionsIntoBuilding(cell);
            for (int direction : dir) {
                Grid.Cell newCell = cell.getNextCell(direction);
                if (isValid(cell, newCell, person.isDisabled()) && !newCell.isOutside()) {
                    person.setState(Person.STATE.STAYINBUILDING);
                    return direction;
                }
            }
            return DIR.STAY;

        }
        // if the person's state is to stay in the room, a random direction is found and if
        // it's valid and leads to the same room, it is returned
        else if (person.getState() == Person.STATE.STAYINROOM) {

            int dir = DIR.getRandomDirection();
            Grid.Cell newCell = cell.getNextCell(dir);
            if (isValid(cell, newCell, person.isDisabled()) &&
                    cell.getRoom().equals(newCell.getRoom())) {
                return dir;
            } else return DIR.STAY;

        }
        // if the person's goal is to stay in the building, it does basically the same as
        // when staying in the same room.
        else if (person.getState() == Person.STATE.STAYINBUILDING) {

            int dir = DIR.getRandomDirection();
            Grid.Cell newCell = cell.getNextCell(dir);
            if (isValid(cell, newCell, person.isDisabled()) && !newCell.isOutside()) {
                return dir;
            } else return DIR.STAY;

        }
        // this is the same as the two above, just that the person can also leave the building
        else if (person.getState() == Person.STATE.WANDERRANDOMLY) {

            int dir = DIR.getRandomDirection();
            Grid.Cell newCell = cell.getNextCell(dir);
            if (isValid(cell, newCell, person.isDisabled())) {
                return dir;
            }
        }
        // if the person wants to go to a special room, the MovementModule assigns a
        // direction that is appropriate according to the PathOntology's saved shortest
        // paths
        // if the person's goal room is null, it is assumed that the person wants to
        // go outside
        else if (person.getState() == Person.STATE.GOTOROOM) {


            if (person.getGoalRoom() != null) {
                // if the person is already in the room it wants to go to
                if ((person.getGoalRoom()).equals(cell.getRoom())) {
                    // it deletes the Goal
                    person.setGoalRoom(null);
                    // it deletes the path
                    person.setPath(null);
                    // it changes its state to stay in the room
                    person.setState(Person.STATE.STAYINROOM);
                    // it returns any move that stays in that same room its in

                    return move(person);
                }
            } else {
                if (person.getIsOnCell().isOutside()) {
                    // it deletes the Goal
                    person.setGoalRoom(null);
                    // it deletes the path
                    person.setPath(null);
                    return DIR.STAY;
                }
            }

            // and sets the person's path to the shortest path it can find
            if (person.getPath() == null ||
                    person.getPath().getWay().get(0).getRoomChangingCells(person.getIsInRoom()) == null) {
                person.setPath(new Path(po.getShortestPath(cell, person.getGoalRoom(), person.isDisabled())));
            }


            // goalPassage is the first existing passage in the person's path,
            // which should be reachable!
            Passage goalPassage = person.getPath().getWay().get(0);

            // if the goalPassage's cells are already reached
            if (goalPassage.getRoomChangingCells().contains(cell)) {

                // dir is a List of directions that lead to the other side of the passage
                LinkedList<Integer> dir = getDirectionsChangeRoom(goalPassage, person);
                // for each of these directions
                for (int direction : dir) {
                    // newCell is the cell the person WOULD reach when taking this direction
                    Grid.Cell newCell = cell.getNextCell(direction);
                    // if the person is not disabled / it's no stair
                    if (isValid(cell, newCell, person.isDisabled())) {
                        // the first passage in the path gets deleted because the person has
                        // reached the next room
                        if (!newCell.isOutside()) {
                            person.setPath(new Path(po.getShortestPath(newCell, person.getGoalRoom(), person.isDisabled())));
                        }
                        // if the new Room is the Goal Room
                        if (person.getGoalRoom() != null) {
                            if (!newCell.isOutside()) {
                                if (newCell.getRoom().equals(person.getGoalRoom())) {

                                    // we delete Room and Path
                                    person.setGoalRoom(null);
                                    person.setPath(null);
                                    // say the person to stay in the room
                                    person.setState(Person.STATE.STAYINROOM);

                                }
                            }
                        } else {
                            if (newCell.isOutside()) {
                                // we delete Room and Path
                                person.setGoalRoom(null);
                                person.setPath(null);
                            }
                        }
                        // and return the new Cell
                        return direction;
                    }

                }
                LinkedList<Integer> dir2 = DIR.getDirections();
                dir2.removeAll(dir);
                for (int direction : dir2) {
                    // we calculate cell that would be reached
                    Grid.Cell newCell = cell.getNextCell(direction);
                    // see above
                    if (isValid(cell, newCell, person.isDisabled()) && !newCell.isOutside()) {
                        return direction;
                    }
                }
                // if the passage is not yet reached
            } else {

                // all directions that lead in the right direction
                LinkedList<Integer> dir = getDirections(goalPassage, person);
                // for each of the possible directions
                for (int direction : dir) {
                    // we calculate cell that would be reached
                    Grid.Cell newCell = cell.getNextCell(direction);
                    // see above
                    if (isValid(cell, newCell, person.isDisabled()) && !newCell.isOutside()) {
                        return direction;
                    }
                }

            }
        }
        // TODO: More advanced algorithms
        else if (person.getState() == Person.STATE.EVACUATION) {

            if (person.getIsOnCell().isOutside()) {
                return DIR.STAY;
            }

            if (person.getPath() == null) {
                person.setPath(evacuationStrategy.getPath(person));
            }




            // nextPassage is the first existing passage in the person's path,
            // which should be reachable!
            Passage nextPassage = person.getPath().getWay().get(0);

            // if the nextPassage's cells are already reached
            if (nextPassage.getRoomChangingCells().contains(cell)) {

                // dir is a List of directions that lead to the other side of the passage
                LinkedList<Integer> dir = getDirectionsChangeRoom(nextPassage, person);
                // for each of these directions
                for (int direction : dir) {
                    // newCell is the cell the person WOULD reach when taking this direction
                    Cell newCell = cell.getNextCell(direction);
                    // if the person is not disabled / it's no stair
                    if (isValid(cell, newCell, person.isDisabled())) {
                        // return the new Cell
                        person.nextInPath();
                        return direction;
                    }

                }
                LinkedList<Integer> dir2 = DIR.getDirections();

                dir2.removeAll(dir);

                Collections.shuffle(dir2);

                for (int direction : dir2) {
                    // we calculate cell that would be reached
                    Cell newCell = cell.getNextCell(direction);
                    // see above
                    if (isValid(cell, newCell, person.isDisabled())) {
                        return direction;
                    }
                }
                // if the passage is not yet reached
            } else {

                // all directions that lead in the right direction
                LinkedList<Integer> dir = getDirections(nextPassage, person);
                // for each of the possible directions
                for (int direction : dir) {
                    // we calculate cell that would be reached
                    Cell newCell = cell.getNextCell(direction);
                    // see above
                    if (isValid(cell, newCell, person.isDisabled()) && !newCell.isOutside()) {
                        return direction;
                    }
                }

            }



        } else throw new

                IllegalArgumentException("No correct state or no goal!");

        return DIR.STAY;

    }

    /**
     * gets directions that lead to the next room, only applicable if the person is already on
     * one side of the passage
     *
     * @param goalPassage the passage that should be crossed in the next step
     * @param person the person for which we would like to get the correct directions
     * @return a List with the directions that lead to the next room
     * @throws IllegalArgumentException if the person is not on the right cells of this passage
     */
    private LinkedList<Integer> getDirectionsChangeRoom(Passage goalPassage, Person person) {

        // in the end, cells contains all cells that are in the other room as the one the person
        // is in
        HashSet<Grid.Cell> cells = new HashSet<>(goalPassage.getRoomChangingCells());
        HashSet<Grid.Cell> personCells = new HashSet<>(goalPassage.getRoomChangingCells(person.getIsInRoom()));
        cells.removeAll(personCells);

        // in the end, dir contains all directions that lead, when followed, to the next room
        LinkedList<Integer> dir = new LinkedList<>();

        for (int direction : DIR.getDirections()) {
            if (cells.contains(person.getIsOnCell().getNextCell(direction))) {
                dir.add(direction);
            }
        }
        if (dir.isEmpty()) throw new IllegalArgumentException("You seem not to be on right Cells!");

        return dir;

    }

    /**
     * gets all directions, ordered so that good directions for reaching the goal are more
     * up front than the ones that do not lead in the right direction
     *
     * @param goalPassage the passage the person should reach
     * @param person the person that has a field its on and a goal it wants to reach
     * @return a list of directions, ordered descending according to their quality
     */
    private LinkedList<Integer> getDirections(Passage goalPassage, Person person) {

        float dX = getAverageDiff(person,
                goalPassage.getRoomChangingCells(person.getIsInRoom()), true);
        float dY = getAverageDiff(person,
                goalPassage.getRoomChangingCells(person.getIsInRoom()), false);

        LinkedList<Integer> dir = new LinkedList<>();

        int xChange = (Math.abs(dX) <= 0.5) ? DIR.STAY : (dX < 0) ? DIR.LEFT : DIR.RIGHT;
        int yChange = (Math.abs(dY) <= 0.5) ? DIR.STAY : (dY < 0) ? DIR.UP : DIR.DOWN;

        dir.add(xChange * yChange);
        if (xChange != 1 && yChange != 1) {
            if (Math.abs(dX) > Math.abs(dY)) {
                dir.add(xChange);
                dir.add(yChange);
                dir.add(xChange * DIR.getComplement(yChange));
                dir.add(yChange * DIR.getComplement(xChange));
                dir.add(DIR.getComplement(yChange));
                dir.add(DIR.getComplement(xChange));
                dir.add(DIR.getComplement(xChange) * DIR.getComplement(yChange));

            } else {
                dir.add(yChange);
                dir.add(xChange);
                dir.add(yChange * DIR.getComplement(xChange));
                dir.add(xChange * DIR.getComplement(yChange));
                dir.add(DIR.getComplement(xChange));
                dir.add(DIR.getComplement(yChange));
                dir.add(DIR.getComplement(xChange) * DIR.getComplement(yChange));
            }
        } else if (xChange == 1 ^ yChange == 1) {
            if (xChange == 1) {
                if (dX < 0) {
                    dir.add(yChange * DIR.LEFT);
                    dir.add(yChange * DIR.RIGHT);
                } else {
                    dir.add(yChange * DIR.RIGHT);
                    dir.add(yChange * DIR.LEFT);
                }
                dir.add(DIR.LEFT);
                dir.add(DIR.RIGHT);
                dir.add(DIR.getComplement(yChange));
                dir.add(DIR.getComplement(yChange) * DIR.LEFT);
                dir.add(DIR.getComplement(yChange) * DIR.RIGHT);
            } else {
                if (dY < 0) {
                    dir.add(xChange * DIR.UP);
                    dir.add(xChange * DIR.DOWN);
                } else {
                    dir.add(xChange * DIR.DOWN);
                    dir.add(xChange * DIR.UP);
                }
                dir.add(DIR.UP);
                dir.add(DIR.DOWN);
                dir.add(DIR.getComplement(xChange));
                dir.add(DIR.getComplement(xChange) * DIR.UP);
                dir.add(DIR.getComplement(xChange) * DIR.DOWN);
            }
        } else throw new IllegalStateException("You already seem to be there!");

        dir.add(DIR.STAY);
        return dir;
    }

    /**
     * gets all directions that lead a person (that is favorably outside the building)
     * inside it
     *
     * @param cell the cell the person is standing on
     * @return a list of all possible directions inside the building
     */
    private LinkedList<Integer> getDirectionsIntoBuilding(Grid.Cell cell) {

        LinkedList<Integer> directions = new LinkedList<>();

        for (int dir : DIR.getDirections()) {

            if (cell.getNextCell(dir).getRoom() != null) {
                directions.add(dir);
            }

        }

        return directions;


    }

    /**
     * checks if a move from cell oldCell to cell newCell is generally possible
     *
     * @param oldCell the cell from which the movement starts
     * @param newCell the cell in which the movement ends
     * @param isDisabled whether the person that wants to make the step is disabled
     * @return whether this step is applicable and generally valid
     */
    private boolean isValid(Grid.Cell oldCell, Grid.Cell newCell, boolean isDisabled) {

        // cell must not be a stair and the person at the same time disabled
        if (!isDisabled || !newCell.isStair()) {
            // cell must not be occupied, ther must not be a wall between the two cells
            if (!newCell.isOccupied() && !isWall(oldCell, newCell)) {
                // not both of the cells can be blocked at the same time. That is so for avoiding
                // two people to go both diagonally in different directions so that they cross.
                if (!oldCell.isBlocked() || !newCell.isBlocked()) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * gets the average difference between the cell a person stands on and a (field) of cells
     * that the person wants to reach next, either in x- or in y-Direction.
     *
     * @param person the person that wants to move
     * @param roomChangingCells the cells the person must reach in order to be able
     *                          to cross the <code>Passage</code>
     * @param XOrY              true = xDifference, false = yDifference
     * @return the difference as a float
     */

    private float getAverageDiff(Person person, HashSet<Grid.Cell> roomChangingCells, boolean XOrY) {

        float diff = 0;

        if (roomChangingCells != null) {
            if (XOrY) {
                for (Grid.Cell cell : roomChangingCells) {
                    diff += (cell.getX() - person.getIsOnCell().getX());
                }

            } else {
                for (Grid.Cell cell : roomChangingCells) {
                    diff += (cell.getY() - person.getIsOnCell().getY());
                }
            }
            diff /= roomChangingCells.size();
            return diff;
        } else {
            throw new IllegalArgumentException(person.getPath().toString() +
                    person.getPath().getWay().get(0).getConnectsInOut() + "\n" +
                    person.getIsOnCell() + "\n" + "Person:" + person.getId());
        }

    }

    /**
     * checks if there is a Wall between two cells in the grid
     *
     * @param cell1 the first cell
     * @param cell2 the second cell
     * @return if between cell1 and cell2, there is a wall, it returns true,
     * else it returns false
     */
    private boolean isWall(Grid.Cell cell1, Grid.Cell cell2) {

        // if both cells are outside, there is no wall between the two
        if (cell1.isOutside() && cell2.isOutside()) return false;

        // if only one cell is outside and there is no exit that contains both cells,
        // there is no wall between the two cells
        if (cell1.isOutside() ^ cell2.isOutside()) {
            Room room = cell1.isOutside() ? cell2.getRoom() : cell1.getRoom();
            HashSet<Passage> exits = room.getExits();
            for (Passage exit : exits) {
                HashSet<Grid.Cell> changeCells = exit.getRoomChangingCells();
                if (changeCells.contains(cell1) && changeCells.contains(cell2)) {
                    return false;
                }
            }
        }
        // if both cells aren't outside and both belong to the same room, there is no wall
        // between the two rooms
        else if (cell1.getRoom().equals(cell2.getRoom())) {
            return false;
        }
        // else, all passages are checked to see whether the cells are belonging to one of
        // those that go from the one room to the other room
        else {
            Room r1 = cell1.getRoom();
            Room r2 = cell2.getRoom();
            HashSet<Passage> passages1 = r1.getPassages();
            HashSet<Passage> passages2 = r2.getPassages();
            for (Passage p1 : passages1) {
                for (Passage p2 : passages2) {
                    if (p1.equals(p2)) {
                        HashSet<Grid.Cell> changeCells = p1.getRoomChangingCells();
                        if (changeCells.contains(cell1) && changeCells.contains(cell2)) {
                            return false;
                        }
                    }
                }
            }
        }


        return true;

    }

}