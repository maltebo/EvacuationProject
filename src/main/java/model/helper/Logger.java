package model.helper;

public class Logger {

    private static boolean isActive = true;

    public static void print(Object string) {
        if (isActive) {
            System.out.println(string);
        }
    }

}