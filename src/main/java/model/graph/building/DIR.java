package model.graph.building;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by Malte on 10/7/17.
 */
public class DIR {

    private DIR() {}

    public static final int UP = 11;
    public static final int LEFT = 17;
    public static final int RIGHT = 37;
    public static final int DOWN = 31;
    public static final int UPLEFT = UP * LEFT;
    public static final int UPRIGHT = UP * RIGHT;
    public static final int DOWNLEFT = DOWN * LEFT;
    public static final int DOWNRIGHT = DOWN * RIGHT;
    public static final int STAY = 1;

    private static final Integer[] DIRECTIONS = {
            UP, LEFT, RIGHT, DOWN, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT, STAY
    };

    public static int getRandomDirection() {

        Random r = new Random();
        return DIRECTIONS[r.nextInt(9)];

    }

    public static int getComplement(int dir) {

        if (dir == UP || dir == RIGHT || dir == LEFT || dir == DOWN || dir == STAY) {
            if (dir == STAY) {
                return STAY;
            } else {
                return (dir + 20) % 40;
            }
        } else throw new IllegalArgumentException("not possible to get Complement");
    }

    public static LinkedList<Integer> getDirections() {

        return new LinkedList<>(Arrays.asList(DIRECTIONS));

    }

}