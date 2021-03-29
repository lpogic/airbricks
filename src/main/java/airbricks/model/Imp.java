package airbricks.model;

public class Imp {

    public static boolean rising(boolean prev, boolean after) {
        return !prev && after;
    }

    public static boolean falling(boolean prev, boolean after) {
        return prev && !after;
    }
}
