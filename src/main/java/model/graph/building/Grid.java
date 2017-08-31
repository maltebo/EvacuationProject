package model.graph.building;

import model.helper.Pair;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Grid {

    int xSize, ySize;

    private final HashMap<Pair<Integer, Integer>, Cell> existingCells = new HashMap<>();

    public Grid(Building building) {

        xSize = building.gridSizeX;
        ySize = building.gridSizeY;

        for (int i = -1; i <= xSize; i++) {
            for (int j = -1; j <= ySize; j++) {
                addCell(i,j);
            }
        }

    }

    public Cell getCell(int x, int y) {

        Cell cell = existingCells.get(new Pair<>(x, y));
        if(cell == null) {
            cell = addCell(x, y);
        }
        return cell;

    }

    public Cell getCell(Cell cell) {

        Cell cell1 = existingCells.get(new Pair<>(cell.x, cell.y));
        if(cell1 == null) {
            cell1 = addCell(cell.x, cell.y);
        }
        return cell1;

    }

    public CellPair getCellPair(Cell c1, Cell c2) {

        return new CellPair(c1, c2);

    }

    private Cell addCell(int x, int y) {

        Cell newCell = new Cell(x, y);
        existingCells.put(new Pair<>(x, y), newCell);

        return newCell;

    }

    public void unblock() {

        for (Cell cell : existingCells.values()) {
            cell.block(false);
        }

    }


    public Building.Room getRoom(Collection<Cell> cells) {

        boolean first = true;
        Building.Room room = null;
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
        int dX = Math.abs(c1.x - c2.x);
        int dY = Math.abs(c1.y - c2.y);
        return dX < dY ? dY : dX;
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

        transient private Building.Room isInRoom = null;

        private transient boolean isStair = false;

        private transient boolean isOccupied = false;

        private transient boolean isBlocked = false;

        private Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }

        private Cell(Cell cell) {
            this.x = cell.x;
            this.y = cell.y;
        }


        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }


        public Cell getNextCell(int dir) {

            int newX = x;
            int newY = y;

            if (dir % DIR.UP == 0) newY -= 1;
            if (dir % DIR.DOWN == 0) newY += 1;
            if (dir % DIR.LEFT == 0) newX -= 1;
            if (dir % DIR.RIGHT == 0) newX += 1;

            return getCell(newX, newY);

        }

        public boolean isOutside() {

            return isInRoom == null;

        }


        public void setRoom(Building.Room isInRoom) {
            if (this.isInRoom == null) {
                this.isInRoom = isInRoom;
            } else throw new IllegalStateException("Cell is already in a room!");
        }

        public Building.Room getRoom() {
            return isInRoom;
        }

        public boolean isStair() {
            return isStair;
        }

        public void setStair() {
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
            return "x = " + x + "\ty = " + y;
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
            return "CellPair:{Cell1:{"+cell1+"}Cell2:{"+cell2+"}}";
        }
    }

}
