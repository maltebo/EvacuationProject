package model.representation;

/**
 * convenience class for representing a pair of cells in the coordinate space, as needed when drawing a
 * line.
 *
 * @author Malte Bossert
 * @version 3.2
 * @see CoordCell
 */
class CoordCellPair {

    /**
     * first cell in the pair
     */
    private CoordCell cell1;
    /**
     * second cell in the pair
     */
    private CoordCell cell2;

    /**
     * standard constructor, takes two cells as input
     * @param cell1 first cell in the pair
     * @param cell2 second cell in the pair
     */
    CoordCellPair(CoordCell cell1, CoordCell cell2) {
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

    /**
     * @return the first cell of the pair
     */
    CoordCell getCell1() {
        return cell1;
    }

    /**
     * @return the second cell of the pair
     */
    CoordCell getCell2() {
        return cell2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordCellPair cellPair = (CoordCellPair) o;

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
        return "CoordCellPair:{Cell1:{" + cell1 + "}Cell2:{" + cell2 + "}}";
    }
}