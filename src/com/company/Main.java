package com.company;

import java.awt.image.BufferedImage;

public class Main {

    public static void main(String[] args) {
        BitmapHelper helper = BitmapHelper.getInstance();
        BufferedImage img = helper.getImage("C:\\Users\\Gleb\\IdeaProjects\\MorfologyImageAnalyze\\circle1.png");
        int[][] binaryPixels = helper.getPixels(img);
        Logger.log("Usual");
        helper.printPixels(binaryPixels);

        int[][] increasePixels = helper.increase(binaryPixels);
        Logger.log("Increase");
        helper.printPixels(increasePixels);

        int[][] erosionPixels = helper.erosion(binaryPixels);
        Logger.log("Erosion");
        helper.printPixels(erosionPixels);

        int[][] closurePixels = helper.closure(binaryPixels);
        Logger.log("Closure");
        helper.printPixels(closurePixels);

        int[][] breakingPixels = helper.breaking(binaryPixels);
        Logger.log("Breaking");
        helper.printPixels(breakingPixels);
    }
}
