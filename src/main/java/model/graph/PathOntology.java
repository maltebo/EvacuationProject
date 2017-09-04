package model.graph;

import model.graph.building.Grid;
import model.graph.building.Grid.*;
import model.graph.building.Building;
import model.graph.building.Building.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * This saves all (shortest) paths in the building, both for
 * disabled and for other persons
 *
 * @author Malte Bossert
 * @version 3.2
 */
public class PathOntology {

    /**
     * the corresponding building
     */
    private Building building;

    /**
     * the grid that this pathOntology belongs to
     */
    private Grid grid;

    /**
     * the Singleton-existingInstances
     */
    private static LinkedList<PathOntology> existingInstances = new LinkedList<>();

    /**
     * a matrix that contains all shortest paths
     */
    private Path[][] shortestPaths;

    /**
     * a matrix that contains all shortest paths for disabled persons
     */
    private Path[][] shortestPathsDisabled;

    /**
     * private singleton constructor that gets initiated once; calculates all the available shortest paths.
     * Both for disabled and for not-disabled people.
     */
    private PathOntology(Building building) {
        this.building = building;
        this.grid = building.getGrid();
        initializePaths();
        initializeDisabledPaths();

    }

    /**
     * @return the Path-Ontology-Instance, either a new one if none is initiated or
     * the already existing one; contains all the shortest paths between every passage
     */
    public static PathOntology getInstance(Building building) {
        for (PathOntology mm : existingInstances) {
            if (mm.building == building) {
                return mm;
            }
        }
        PathOntology temp = new PathOntology(building);
        existingInstances.add(temp);
        return temp;
    }

    /**
     * initializes the shortest paths for people without a disability, using the dijkstra-algorithm
     */
    private void initializePaths() {
        // all passages in the building
        LinkedList<Passage> allPassages = new LinkedList<>(building.getPassages());

        // a matrix which will contain the shortest paths between any pair of
        // passages
        shortestPaths = new Path[allPassages.size()][allPassages.size()];

        // initialization, every passage-pair is initialized with 0 (if it is two times the same
        // passage), infinity (if they are not directly connected through one room) or the value of the
        // distance between them.
        for (Passage p1 : allPassages) {
            for (Passage p2 : allPassages) {

                LinkedList<Passage> temp = new LinkedList<>();
                temp.add(p1);
                if (!p1.equals(p2)) {
                    temp.add(p2);
                }
                shortestPaths[p1.id][p2.id] = new Path(temp, directDistanceBetween(p1, p2));

            }
        }
//TODO
        // Dijkstra-algorithm for calculating the shortest paths
        for (Passage p0 : allPassages) {
            for (Passage p1 : allPassages) {
                for (Passage p2 : allPassages) {

                    if ((long) shortestPaths[p1.id][p0.id].getCosts() +
                            (long) shortestPaths[p0.id][p2.id].getCosts() + 1
                            < (long) shortestPaths[p1.id][p2.id].getCosts()) {

                        LinkedList<Passage> tempPath = new LinkedList<>();
                        tempPath.addAll(shortestPaths[p1.id][p0.id].getWay());
                        tempPath.removeLast();
                        tempPath.addAll(shortestPaths[p0.id][p2.id].getWay());
                        shortestPaths[p1.id][p2.id] = new Path(tempPath,
                                shortestPaths[p1.id][p0.id].getCosts() +
                                        shortestPaths[p0.id][p2.id].getCosts() + 1);

                    }

                }
            }
        }
    }

    /**
     * initializes shortest paths for people with a disability, using the dijkstra-algorithm
     */
    private void initializeDisabledPaths() {

        LinkedList<Door> allDoors = new LinkedList<>(building.getDoors());

        allDoors.sort(new Comparator<Door>() {
            @Override
            public int compare(Door o1, Door o2) {
                return (o1.id - o2.id);
            }
        });
        shortestPathsDisabled = new Path[allDoors.size()][allDoors.size()];


        for (int i = 0; i < allDoors.size(); i++) {
            for (int j = 0; j < allDoors.size(); j++) {

                LinkedList<Passage> temp = new LinkedList<>();
                temp.add(allDoors.get(i));
                if (!allDoors.get(i).equals(allDoors.get(j))) {
                    temp.add(allDoors.get(j));
                }
                shortestPathsDisabled[i][j] = new Path(temp, directDistanceBetween(allDoors.get(i),
                        allDoors.get(j)));

            }
        }

        for (int i = 0; i < allDoors.size(); i++) {
            for (int j = 0; j < allDoors.size(); j++) {
                for (int k = 0; k < allDoors.size(); k++) {

                    if ((long) shortestPathsDisabled[j][i].getCosts() +
                            (long) shortestPathsDisabled[i][k].getCosts() +
                            (allDoors.get(0).getRoomChangingCells().size() / 2)
                            < (long) shortestPathsDisabled[j][k].getCosts()) {

                        LinkedList<Passage> tempPath = new LinkedList<>();
                        tempPath.addAll(shortestPathsDisabled[j][i].getWay());
                        tempPath.removeLast();
                        tempPath.addAll(shortestPathsDisabled[i][k].getWay());
                        shortestPathsDisabled[j][k] = new Path(tempPath,
                                shortestPathsDisabled[j][i].getCosts() +
                                        shortestPathsDisabled[i][k].getCosts() +
                                        (allDoors.get(0).getRoomChangingCells().size() / 2));

                    }

                }
            }
        }

    }

    /**
     * calculates the direct distance between two passages
     *
     * @param p1 the first passage
     * @param p2 the second passage
     * @return 0, if both passages are identical; (integer)-infinity, if they are not connected via a room;
     * the longer distance in x / y - direction if there is a direct connecting room
     */
    private float directDistanceBetween(Passage p1, Passage p2) {

        if (p1.equals(p2)) {
            return 0;
        }
        Room connection = p1.getConnected(p2);
        float distance = 0;
        if (connection != null) {

            HashSet<Cell> p1Cells = p1.getRoomChangingCells(connection);
            HashSet<Cell> p2Cells = p2.getRoomChangingCells(connection);
            for (Cell c1 : p1Cells) {
                distance += building.distance(c1, p2Cells);
            }
            return distance / p1Cells.size();

        }
        return Integer.MAX_VALUE;
    }

    /**
     * retruns the shortest path for a person, depending on the position and whether the person is disabled or not.
     *
     * @param start    the actual cell of the person
     * @param end      the room the person wants to get to
     * @param disabled a boolean; whether the person is disabled or not
     * @return the shortest path for this person
     */
    public Path getShortestPath(Cell start, Room end, boolean disabled) {

        // from the starting Cell, it gets a copy of all reachable passages
        HashSet<Passage> startPass = start.getRoom().getPassages();

        if (end == null) {
            startPass.addAll(start.getRoom().getExits());
        }
        // from the Room we want to go to, it gets all outgoing Passages
        HashSet<Passage> endPass;
        if (end != null) {
            endPass = end.getPassages();
        } else {
            endPass = building.getExits();
        }
        if (disabled) {
            HashSet<Passage> copy = new HashSet<>(endPass);
            for (Passage p : copy) {
                if (p.isStair()) endPass.remove(p);
            }
            copy = new HashSet<>(startPass);
            for (Passage p : copy) {
                if (p.isStair()) startPass.remove(p);
            }


        }

        // it creates a new dummy path
        Path temp = new Path(new LinkedList<>(), Integer.MAX_VALUE);

        // it goes through all passage - passage - combinations
        for (Passage p1 : startPass) {
            for (Passage p2 : endPass) {

                // costs are distance of the cell to the cells of the passage + 1
                // + the costs of the shortest path between the passages
                float costs = grid.distance(start, p1.getRoomChangingCells(start.getRoom())) +
                        1;

                if (!p1.equals(p2)) {
                    if (disabled) {
                        costs += getDisabledShortestPath(p1, p2).getCosts() + 1;
                    } else {
                        costs += shortestPaths[p1.id][p2.id].getCosts() + 1;
                    }
                }

                if (!p1.equals(p2) && start.getRoom().equals(p1.getConnected(p2))) {
                    costs += 1;
                }

                // if those costs are lower than the costs of our temporal Path
                if (costs < temp.getCosts()) {
                    // temp will become a new Path, that is, the shortest path between the
                    // found Passages + the costs we estimated
                    if (disabled) {
                        temp = new Path(new LinkedList<>(getDisabledShortestPath(p1, p2).getWay()), costs);
                    } else {
                        temp = new Path(new LinkedList<>(shortestPaths[p1.id][p2.id].getWay()), costs);
                    }
                }

            }
        }

        // the found shortest Path is returned
        return temp;

    }

    /**
     * returns the shortest path for a disabled person between two passages
     *
     * @param p1 first passage
     * @param p2 second passage
     * @return the shortest path without stairs
     */
    private Path getDisabledShortestPath(Passage p1, Passage p2) {

        LinkedList<Passage> allPassagesWithoutStairs = new LinkedList<>(building.getPassages());
        LinkedList<Passage> copy = new LinkedList<>(allPassagesWithoutStairs);
        for (Passage passage : copy) {
            if (passage.isStair()) allPassagesWithoutStairs.remove(passage);
        }

        allPassagesWithoutStairs.sort(new Comparator<Passage>() {
            @Override
            public int compare(Passage o1, Passage o2) {
                return (o1.id - o2.id);
            }
        });
        int posP1 = allPassagesWithoutStairs.indexOf(p1);
        int posP2 = allPassagesWithoutStairs.indexOf(p2);
        return shortestPathsDisabled[posP1][posP2];

    }


    @Override
    public String toString() {

        StringBuilder str = new StringBuilder();
        str.append("PathOntology{" + "shortestPaths=\n");
        for (Path[] shortestPathPassage : shortestPathsDisabled) {
            for (Path shortestPath : shortestPathPassage) {
                str.append(shortestPath);
                str.append("\n");
            }
            str.append("\n");
        }
        str.append("}\n");
        return str.toString();
    }
}