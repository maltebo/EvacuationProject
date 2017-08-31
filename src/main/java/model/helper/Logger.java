package model.helper;

public class Logger {

    private static boolean isActive = true;

    public static void print(String string) {
        if (isActive) {
            System.out.println(string);
        }
    }

    public static void print(Object string) {
        if (isActive) {
            System.out.println(string);
        }
    }

}