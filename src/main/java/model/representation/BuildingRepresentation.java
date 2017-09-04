package model.representation;

import model.graph.building.DIR;
import model.graph.building.Grid;
import model.graph.useragent.Person;
import model.helper.TestShow;
import model.graph.building.Building;
import model.graph.building.Building.Passage;
import model.graph.building.Building.Room;
import model.graph.building.Building.Stair;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferStrategy;

/**
 * This is the representation of a specified <code>building.Building</code>.
 * It also contains listeners, that communicate with the building.
 *
 * @author Malte Bossert
 * @version 3.2
 * @see Building
 */
public class BuildingRepresentation extends Canvas {

    private static final long serialVersionUID = 79327893799847L;

    public Path2D.Float buildingForm;

    private int width, height;

    /**
     * the offsets of the building; specifies how much space in the window is occupied by
     * the building-representation
     */
    private int offsetSide, offsetTop;

    /**
     * the grid size of the building
     */
    private final int gridSizeX, gridSizeY;

    /**
     * the width and height of one cell in the grid, measured in pixels depending on the
     * window size
     */
    private int gridWidth, gridHeight;

    /**
     * the building that is represented by this representation
     */
    private Building building;

    /**
     * the floor that is drawn by this representation
     */
    private int floor;

    /**
     * the constructor of this building. specifies this representation
     *
     * @param building the building that is being represented
     * @see Building
     */
    public BuildingRepresentation(Building building, int floor) {

        this.building = building;
        this.floor = floor;
        this.gridSizeX = building.gridSizeX;
        this.gridSizeY = building.gridSizeY;
        width = getSize().width;
        height = getSize().height;

        if (width == 0) width = 600;
        if (height == 0) height = 400;

        buildingForm = new Path2D.Float();
        update();

    }

    /**
     * renders the building.
     * Creates smoothness.
     *
     * @param percentage the value, how much of the actual tick already passed
     */
    public void render(float percentage) {

        Graphics2D g2d = (Graphics2D) getBufferStrategy().getDrawGraphics();

        try {

            if (!this.getSize().equals(new Dimension(width, height))) {
                update();
                width = getSize().width;
                height = getSize().height;
            }

            paint(g2d);

            paintPeople(g2d, percentage);


        } finally {
            g2d.dispose();
        }
        getBufferStrategy().show();
    }

    /**
     * renders the building.
     */
    public void render() {

        Graphics2D g2d = (Graphics2D) getBufferStrategy().getDrawGraphics();

        try {

            if (!this.getSize().equals(new Dimension(width, height))) {
                update();
                width = getSize().width;
                height = getSize().height;
            }

            paint(g2d);


        } finally {
            g2d.dispose();
        }
        getBufferStrategy().show();
    }

    @Override
    public void paint(Graphics g) {

        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);
        try {
            ((Graphics2D) g).draw(buildingForm);
        } catch (ArrayIndexOutOfBoundsException e) {

        }
    }

    private void paintPeople(Graphics2D g, float percentage) {

        for (Person person : building.getPersonsInBuilding()) {

            if (person.getIsOnCell().getFloor() == floor) {
                g.setColor(person.COLOR);
                CoordCell oldCoordinates = adjustCoordinates(getCoordinatesCenter(person.getWasOnCell()), person.SIZE);
                CoordCell newcoordinates = adjustCoordinates(getCoordinatesCenter(person.getIsOnCell()), person.SIZE);
                int dX = (int) ((float) (newcoordinates.getX() - oldCoordinates.getX()) * percentage);
                int dY = (int) ((float) (newcoordinates.getY() - oldCoordinates.getY()) * percentage);
                CoordCell coordinates = new CoordCell(oldCoordinates.getX() + dX, oldCoordinates.getY() + dY);
                g.fillOval(coordinates.getX(), coordinates.getY(), person.SIZE, person.SIZE);
            }

        }

    }

    /**
     * updates the building-size and fits it to the actual window
     */
    private void update() {

        this.offsetSide = this.getSize().width / 10;
        this.offsetTop = this.getSize().height / 10;
        this.gridWidth = (this.getSize().width - (2 * offsetSide)) / gridSizeX;
        this.gridHeight = (this.getSize().height - (2 * offsetTop)) / gridSizeY;
        buildingForm = new Path2D.Float();
        for (Room room : building.getRooms()) {
            if (room.getFloor() == floor) {
                Path2D roomPath = updateRoom(room);
                buildingForm.append(roomPath, false);
            }
        }

        for (Stair stair : building.getStairs()) {
            if (stair.isInFloor(floor)) {
                Path2D stairPath = updateStair(stair);
                buildingForm.append(stairPath, false);
            }
        }

    }

    private Path2D updateStair(Stair stair) {

        Path2D.Float stairPath = new Path2D.Float();

        int dX, dY;
        double StairParts = 5.0;

        if (stair.getDirection() == DIR.UP || stair.getDirection() == DIR.DOWN) {

            dX = (int) (gridWidth / 8.0);
            dY = (int) (gridHeight / StairParts);
            for (Grid.Cell cell : stair.getStairCellsPair1().getValue()) {
                CoordCellPair ccp = getCoordinates(cell);
                int x1 = ccp.getCell1().getX() + dX;
                int x2 = ccp.getCell2().getX() - dX;
                int y = ccp.getCell1().getY();

                for (int i = 0; i <= StairParts; i++) {
                    stairPath.append(new Line2D.Float(x1, y, x2, y), false);
                    y += dY;
                }
            }

        } else if (stair.getDirection() == DIR.LEFT || stair.getDirection() == DIR.RIGHT) {

            dX = (int) (gridWidth / StairParts);
            dY = (int) (gridHeight / 8.0);
            for (Grid.Cell cell : stair.getStairCellsPair1().getValue()) {
                CoordCellPair ccp = getCoordinates(cell);
                int x = ccp.getCell1().getX();
                int y1 = ccp.getCell1().getY() + dY;
                int y2 = ccp.getCell2().getY() - dY;

                for (int i = 0; i <= StairParts; i++) {
                    stairPath.append(new Line2D.Float(x, y1, x, y2), false);
                    x += dX;
                }
            }

        }

        return stairPath;

    }

    private Path2D updateRoom(Room room) {

        Path2D.Float roomPath = new Path2D.Float();

        for (int x = room.getBeginning().getX(); x <= room.getEnd().getX(); x++) {

            Grid.Cell cell1 = building.getCell(x, room.getBeginning().getY(), room.getFloor());
            if (!room.isPassage(cell1, cell1.getNextCell(DIR.UP))) {
                CoordCellPair ccp = getWallCoordinates(cell1, cell1.getNextCell(DIR.UP));
                Line2D line = new Line2D.Float(ccp.getCell1().getX(), ccp.getCell1().getY(),
                        ccp.getCell2().getX(), ccp.getCell2().getY());
                roomPath.append(line, false);


            }
            Grid.Cell cell2 = building.getCell(x, room.getEnd().getY(), room.getFloor());
            if (!room.isPassage(cell2, cell2.getNextCell(DIR.DOWN))) {
                CoordCellPair ccp = getWallCoordinates(cell2, cell2.getNextCell(DIR.DOWN));
                roomPath.append(new Line2D.Float(ccp.getCell1().getX(), ccp.getCell1().getY(),
                        ccp.getCell2().getX(), ccp.getCell2().getY()), false);
            }
        }

        for (int y = room.getBeginning().getY(); y <= room.getEnd().getY(); y++) {

            Grid.Cell cell1 = building.getCell(room.getBeginning().getX(), y, room.getFloor());
            if (!room.isPassage(cell1, cell1.getNextCell(DIR.LEFT))) {
                CoordCellPair ccp = getWallCoordinates(cell1, cell1.getNextCell(DIR.LEFT));
                roomPath.append(new Line2D.Float(ccp.getCell1().getX(), ccp.getCell1().getY(),
                        ccp.getCell2().getX(), ccp.getCell2().getY()), false);
            }
            Grid.Cell cell2 = building.getCell(room.getEnd().getX(), y, room.getFloor());
            if (!room.isPassage(cell2, cell2.getNextCell(DIR.RIGHT))) {
                CoordCellPair ccp = getWallCoordinates(cell2, cell2.getNextCell(DIR.RIGHT));
                roomPath.append(new Line2D.Float(ccp.getCell1().getX(), ccp.getCell1().getY(),
                        ccp.getCell2().getX(), ccp.getCell2().getY()), false);
            }

        }

        return roomPath;
    }

    /**
     * @param matrixCoordinates the cell with its coordinates
     * @return the coordinates of this cell's center in terms of pixels and the window
     */
    private CoordCell getCoordinatesCenter(Grid.Cell matrixCoordinates) {

        int tempX = offsetSide + (matrixCoordinates.getX() * gridWidth) + (gridWidth / 2);
        int tempY = offsetTop + (matrixCoordinates.getY() * gridHeight) + (gridHeight / 2);
        return new CoordCell(tempX, tempY);

    }

    //TODO
    private CoordCellPair getCoordinates(Grid.Cell matrixCoordinates) {

        int tempX = offsetSide + (matrixCoordinates.getX() * gridWidth);
        int tempY = offsetTop + (matrixCoordinates.getY() * gridHeight);
        CoordCell tempUpLeft = new CoordCell(tempX, tempY);
        CoordCell tempDownRight = new CoordCell(tempX + gridWidth, tempY + gridHeight);
        return new CoordCellPair(tempUpLeft, tempDownRight);

    }


    /**
     * @return the width of one cell in pixels
     */
    public int getGridWidth() {
        return gridWidth;
    }

    /**
     * @return the height of one cell in pixels
     */
    public int getGridHeight() {
        return gridHeight;
    }

    /**
     * calculates the coordinates for drawing a wall between two cells
     *
     * @param cell1 first cell
     * @param cell2 second cell
     * @return the pair of coordinates used to draw a wall
     */
    public CoordCellPair getWallCoordinates(Grid.Cell cell1, Grid.Cell cell2) {

        int xStart, yStart, xEnd, yEnd;

        if (cell1.getX() == cell2.getX()) {
            if (Math.abs(cell1.getY() - cell2.getY()) == 1) {
                xStart = offsetSide + cell1.getX() * gridWidth;
                xEnd = xStart + gridWidth;
                yStart = yEnd = offsetTop + Math.max(cell1.getY(), cell2.getY()) * gridHeight;
            } else throw new IllegalArgumentException("Wall cannot be built, Cells are not next to each other");
        } else if (cell1.getY() == cell2.getY()) {
            if (Math.abs(cell1.getX() - cell2.getX()) == 1) {
                yStart = offsetTop + cell1.getY() * gridHeight;
                yEnd = yStart + gridHeight;
                xStart = xEnd = offsetSide + Math.max(cell1.getX(), cell2.getX()) * gridWidth;
            } else throw new IllegalArgumentException("Wall cannot be built, Cells are not next to each other");
        } else throw new IllegalArgumentException("Wall cannot be built, Cells are not next to each other");

        return new CoordCellPair(new CoordCell(xStart, yStart), new CoordCell(xEnd, yEnd));
    }

    private CoordCell adjustCoordinates(CoordCell coordCenter, int size) {

        return new CoordCell(coordCenter.getX() - (size / 2), coordCenter.getY() - (size / 2));

    }

    @Override
    public BufferStrategy getBufferStrategy() {
        BufferStrategy bs = super.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return super.getBufferStrategy();
        } else {
            return bs;
        }
    }

    private void addFrame(Shape shape) {
        JFrame jf = new JFrame();
        jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        jf.setSize(1100, 600);
        jf.add(new TestShow(shape));
        jf.setVisible(true);
    }
}