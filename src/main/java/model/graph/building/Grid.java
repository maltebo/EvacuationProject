package model.graph.building;

import model.helper.Pair;
import model.graph.building.Building.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * a grid for a building, consists of cells.
 *
 * @author Malte
 * @see Building
 */
public class Grid {

    /**
     * the size of the Grid in x- and y-Direction
     */
    int xSize, ySize;

    /**
     * the number of Floors
     */
    int floors;

    final Building building;

    /**
     * all the existing cells in this grid (basically a Singleton, we do not want to create
     * any cell twice
     */
    private final HashMap<Pair<Pair<Integer, Integer>, Integer>, Cell> existingCells = new HashMap<>();

    Grid(Building building) {

        this.building = building;
        xSize = building.gridSizeX;
        ySize = building.gridSizeY;
        floors = building.floors;

        for (int i = -1; i <= xSize; i++) {
            for (int j = -1; j <= ySize; j++) {
                for (int k = 0; k < floors; k++) {
                    addCell(i, j, k);
                }
            }
        }

    }

    Cell getCell(int x, int y) {

        return getCell(x, y, 0);

    }

    Cell getCell(int x, int y, int f) {
        Cell cell = existingCells.get(new Pair<>(new Pair<>(x, y), f));
        if (cell == null) {
            cell = addCell(x, y, f);
        }
        return cell;
    }

    public Cell getCell(Cell cell) {

        Cell cell1 = existingCells.get(new Pair<>(new Pair<>(cell.x, cell.y), cell.floor));
        if (cell1 == null) {
            cell1 = addCell(cell.x, cell.y, cell.floor);
        }
        return cell1;

    }

    public CellPair getCellPair(Cell c1, Cell c2) {

        return new CellPair(c1, c2);

    }

    private Cell addCell(int x, int y, int f) {

        Cell newCell = new Cell(x, y, f);
        existingCells.put(new Pair<>(new Pair<>(x, y), f), newCell);

        return newCell;

    }

    public void unblock() {

        for (Cell cell : existingCells.values()) {
            cell.block(false);
        }

    }


    public Room getRoom(Collection<Cell> cells) {

        boolean first = true;
        Room room = null;
        for (Cell cell : cells) {

            if (first) {
                room = cell.isInRoom;
                first = false;
            } else {
                if (room != null) {
                    if (!cell.isInRoom.equals(room)) {
                        throw new IllegalArgumentException("Not all entranceCells in same Room");
                    }
                } else {
                    if (!cell.isOutside()) {
                        throw new IllegalArgumentException("Not all Cells are outside");
                    }
                }
            }
        }
        return room;
    }

    public int distance(Cell c1, Cell c2) {
        if (c1.floor == c2.floor) {
            int dX = Math.abs(c1.x - c2.x);
            int dY = Math.abs(c1.y - c2.y);
            return dX < dY ? dY : dX;
        } else {
            //TODO
            throw new IllegalStateException("Cells are not on same floor");
        }
    }


    public float distance(Cell c1, HashSet<Cell> c2) {
        float distance = 0;
        for (Cell cell : c2) {
            distance += distance(c1, cell);
        }
        return distance / c2.size();
    }

    /**
     * Created by Malte on 10/7/17.
     */
    public class Cell {

        private int x;
        private int y;
        private int floor;

        transient private Room isInRoom = null;

        private transient boolean isStair = false;

        private transient boolean isOccupied = false;

        private transient boolean isBlocked = false;

        private Cell(int x, int y, int floor) {
            this.x = x;
            this.y = y;
            this.floor = floor;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getFloor() {
            return floor;
        }


        public Cell getNextCell(int dir) {

            int newX = x;
            int newY = y;

            if (dir % DIR.UP == 0) newY -= 1;
            if (dir % DIR.DOWN == 0) newY += 1;
            if (dir % DIR.LEFT == 0) newX -= 1;
            if (dir % DIR.RIGHT == 0) newX += 1;

            Cell newCell = getCell(newX, newY, floor);

            return newCell;
        }

        public Cell getNextStairCell(int dir) {

            Cell nextCell = getNextCell(dir);
            if (isStair && getNextCell(dir).isStair) {
                return nextCell;
            } else {
                Cell tempCell = isStair ? this : nextCell;

                Stair stair = null;
                for (Stair tempStair : building.getStairs()) {
                    if (tempStair.getStairCells().contains(tempCell)) {

                        stair = tempStair;
                        break;

                    }
                }

                if (stair == null) {
                    System.err.println("STAIR NOT FOUND");
                    return null;
                }

                if (this.isStair) {

                    if (dir == stair.direction) {

                        if (stair.getHigherCells().contains(tempCell)) {

                            int newX = tempCell.x;
                            int newY = tempCell.y;

                            return getCell(newX, newY, stair.getHigherFloor());

                        }

                    } else if (dir == DIR.getComplement(stair.getDirection())) {

                        if (stair.getLowerCells().contains(tempCell)) {

                            int newX = tempCell.x;
                            int newY = tempCell.y;

                            return getCell(newX, newY, stair.getLowerFloor());

                        }

                    } else return null;


                } else {

                    if (dir == stair.getDirection()) {

                        if (stair.getLowerCells().contains(this)) {

                            int newX = tempCell.x;
                            int newY = tempCell.y;

                            return getCell(newX, newY, floor);

                        }

                    } else if (dir == DIR.getComplement(stair.getDirection())) {

                        if (stair.getHigherCells().contains(this)) {

                            int newX = tempCell.x;
                            int newY = tempCell.y;

                            return getCell(newX, newY, floor);

                        }

                    } else return null;

                }
                return null;
            }

        }


        public Cell getNextCell(int dir, boolean up) {

            int newX = x;
            int newY = y;
            int newFloor = floor;

            if (dir % DIR.UP == 0) newY -= 1;
            if (dir % DIR.DOWN == 0) newY += 1;
            if (dir % DIR.LEFT == 0) newX -= 1;
            if (dir % DIR.RIGHT == 0) newX += 1;

            if (up) {
                newFloor++;
            } else {
                newFloor--;
            }

            return getCell(newX, newY, newFloor);

        }

        public boolean isOutside() {

            return isInRoom == null;

        }


        void setRoom(Room isInRoom) {
            if (this.isInRoom == null) {
                this.isInRoom = isInRoom;
            } else throw new IllegalStateException("Cell " + this + " is already in a room!");
        }

        public Room getRoom() {
            return isInRoom;
        }

        public boolean isStair() {
            return isStair;
        }

        void setStair() {
            isStair = true;
        }

        public boolean isOccupied() {
            return isOccupied;
        }

        public void setOccupied(boolean occupied) {
            isOccupied = occupied;
        }

        public boolean isBlocked() {
            return isBlocked;
        }

        public void block(boolean blocked) {
            isBlocked = blocked;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cell cell = (Cell) o;

            if (x != cell.x) return false;
            return y == cell.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;

        }

        @Override
        public String toString() {
            return "x = " + x + "\ty = " + y + "\tfloor = " + floor;
        }
    }

    /**
     * Created by Malte on 13/7/17.
     */
    public class CellPair {

        private Cell cell1;
        private Cell cell2;

        public CellPair(Cell cell1, Cell cell2) {
            if (cell1.getX() < cell2.getX()) {
                this.cell1 = cell1;
                this.cell2 = cell2;
            } else if (cell1.getX() == cell2.getX()) {
                if (cell1.getY() < cell2.getY()) {
                    this.cell1 = cell1;
                    this.cell2 = cell2;
                } else {
                    this.cell1 = cell2;
                    this.cell2 = cell1;
                }
            } else {
                this.cell1 = cell2;
                this.cell2 = cell1;
            }
        }

        public Cell getCell1() {
            return cell1;
        }

        public Cell getCell2() {
            return cell2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CellPair cellPair = (CellPair) o;

            if (cell1.equals(cellPair.cell1)) {
                return cell2.equals(cellPair.cell2);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = cell1.hashCode();
            result = 31 * result + cell2.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "CellPair:{Cell1:{" + cell1 + "}Cell2:{" + cell2 + "}}";
        }
    }

}
