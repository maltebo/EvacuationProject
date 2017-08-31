package model.representation;

/**
 * A convenience class to represent non-generic Integer-pairs in a coordinate space
 *
 * @author Malte Bossert
 * @version 3.2
 */
class CoordCell {

    /**
     * x-Value of the pair
     */
    private int x;
    /**
     * y-Value of the pair
     */
    private int y;

    /**
     * standard Contructor
     *
     * @param x x-Value of the pair
     * @param y y-Value of the pair
     */
    CoordCell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x-Value of the pair
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y-Value of the pair
     */
    public int getY() {
        return y;
    }

    /**
     * equals-Method, tests if both objects can be considered the same,
     * which is true if they consist of both the same x- and the same y-Value
     * @param o Object to be compared with
     * @return true if both objects are considered the same; else false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CoordCell that = (CoordCell) o;

        if (x != that.x) return false;
        return y == that.y;
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