package oth.shipeditor;

public class Utility {

    public static double round(double value, int scale) {
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }

}
