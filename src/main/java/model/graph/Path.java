package model.graph;

import model.graph.building.Building;

import java.util.LinkedList;

/**
 * Represents a path between passages with some amount of costs.
 *
 * @author Malte Bossert
 * @version 3.2
 * @see Building.Passage
 * @see PathOntology
 */
public class Path {

    /**
     * the way of the passages in correct order
     */
    private LinkedList<Building.Passage> way;
    /**
     * the costs of this path
     */
    private float costs;

    /**
     * constructor that creates new path based on a given way and given costs
     *
     * @param way the list of passages
     * @param costs the costs as a float-value
     */
    public Path (LinkedList<Building.Passage> way, float costs) {
        this.way = way;
        this.costs = costs;
    }

    /**
     * copy-constructor, creates a deep copy
     * @param path the path to be copied
     */
    public Path(Path path) {

        this.way = new LinkedList<>(path.getWay());
        this.costs = path.costs;

    }

    /**
     *
     * @return the way as a new <code>LinkedList</code>
     */
    public LinkedList<Building.Passage> getWay() {
        return new LinkedList<>(way);
    }

    /**
     *
     * @return the costs of this way
     */
    public float getCosts() {
        return costs;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Path{" + "way=\n");
        for (Building.Passage passage : way) {
            str.append("Psg" + passage.id + " -> ");
        }
        str.append("costs = " + costs);
        str.append("}\n");
        return str.toString();
    }

    public void remove(int i) {
        way.remove(i);
    }

}