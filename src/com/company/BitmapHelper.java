package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BitmapHelper {
    private static final String LOG_TAG = BitmapHelper.class.getCanonicalName();
    private static final int IMAGE_WIDTH = 100;
    private static final int IMAGE_HEIGHT = 100;
    private static final double R_COEF = 0.3;
    private static final double G_COEF = 0.59;
    private static final double B_COEF = 0.11;
    private static final double THRESHOLD = 250;
    private int[][] outherTopLeftMatrix = new int[2][2];
    private int[][] outherTopRightMatrix = new int[2][2];
    private int[][] outherBottomRightMatrix = new int[2][2];
    private int[][] outherBottomLeftMatrix = new int[2][2];
    private int[][] innerTopLeftMatrix = new int[2][2];
    private int[][] innerTopRightMatrix = new int[2][2];
    private int[][] innerBottomLeftMatrix = new int[2][2];
    private int[][] innerBottomRigthMatrix = new int[2][2];
    private int[][] noiseMatrix = new int[3][3];
    List<int[][]> outherMatrix = new ArrayList<>();
    List<int[][]> innerMatrix = new ArrayList<>();
    private int innerAngle = 0;
    private int outherAngle = 0;
    private int label = 0;
    private static BitmapHelper instance = null;

    public static BitmapHelper getInstance() {
        if (instance == null)
            instance = new BitmapHelper();
        return instance;
    }

    private BitmapHelper() {
    }

    public BufferedImage getImage(String nameFile){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(nameFile));
        } catch (IOException e) {
        }

        return img;
    }

    private int[][] clearNoise(int[][] pixels){
        boolean isOk = true;
        int[][] res = new int[3][3];

        for (int i = 0; i < 3; i++){
            for (int j = 0; j < 3; j++){
                int curPixel = pixels[i][j];
                int curNoise = noiseMatrix[i][j];

                if (curNoise == curPixel && isOk)
                    isOk = true;
                else
                    isOk = false;
            }
        }

        if (isOk)
            pixels[1][1] = 1;
        res = pixels;

        return res;
    }

    public void analyzeNoise(int[][] pixels){
        int[][] currentMatrix = new int[3][3];

        initOutherInnerMatrix();

        for (int i = 0; i < IMAGE_WIDTH; i++){
            for (int j = 0; j < IMAGE_HEIGHT; j++){

                if (!((i - 1 == -1) || (j - 1 == -1) || (i + 1 >= IMAGE_WIDTH) || (j + 1 >= IMAGE_HEIGHT))) {
                    currentMatrix[0][0] = pixels[i - 1][j - 1];
                    currentMatrix[0][1] = pixels[i - 1][j];
                    currentMatrix[0][2] = pixels[i - 1][j + 1];

                    currentMatrix[1][0] = pixels[i][j - 1];
                    currentMatrix[1][1] = pixels[i][j];
                    currentMatrix[1][2] = pixels[i][j + 1];

                    currentMatrix[2][0] = pixels[i + 1][j - 1];
                    currentMatrix[2][1] = pixels[i + 1][j];
                    currentMatrix[2][2] = pixels[i + 1][j + 1];

                    int[][] refMatrix = clearNoise(currentMatrix);

                    pixels[i - 1][j - 1] = refMatrix[0][0];
                    pixels[i - 1][j] = refMatrix[0][1];
                    pixels[i - 1][j + 1] = refMatrix[0][2];

                    pixels[i][j - 1] = refMatrix[1][0];
                    pixels[i][j] = refMatrix[1][1];
                    pixels[i][j + 1] = refMatrix[1][2];

                    pixels[i + 1][j - 1] = refMatrix[2][0];
                    pixels[i + 1][j] = refMatrix[2][1];
                    pixels[i + 1][j + 1] = refMatrix[2][2];
                }
            }
        }
    }

    private void printMarkers(int[][] pixels){
        for (int i = 0; i < IMAGE_WIDTH; i++){
            for (int j = 0; j < IMAGE_HEIGHT; j++){
                System.out.print(pixels[j][i]);
            }
            System.out.println();
        }
    }

    public void getMarkers(int[][] pixels){
        int[][] negPixels = negativePixels(pixels);
        findComponent(negPixels, label);
        System.out.println(String.valueOf(negPixels));
        printMarkers(negPixels);
        //Log.d(LOG_TAG, String.valueOf(negPixels));
    }

    private void findComponent(int[][] pixels, int label) {
        for (int i = 0; i < IMAGE_WIDTH; i++){
            for (int j = 0; j < IMAGE_HEIGHT; j++){
                if (pixels[i][j] == -1){
                    label++;
                    searchComponent(pixels, label, i, j);
                }
            }
        }
    }

    private void searchComponent(int[][] pixels, int label, int i, int j) {
        pixels[i][j] = label;
        List<NeighboardSet> nSets = neighboardSets(i, j, pixels);
        if (nSets.size() != 0) {
            for (NeighboardSet set : nSets) {
                int nI = set.getI();
                int nJ = set.getJ();
                if (pixels[nI][nJ] == -1) {
                    searchComponent(pixels, label, nI, nJ);
                }
            }
            nSets.clear();
        }
        else{
            /*Log.d(LOG_TAG, "Size empty");*/
        }
    }

    private List<NeighboardSet> neighboardSets(int i, int j, int[][] pixels){
        List<NeighboardSet> neighboards = new ArrayList<>();
        if (!((i - 1 == -1) || (j - 1 == -1) || (i + 1 >= IMAGE_WIDTH) || (j + 1 >= IMAGE_HEIGHT))) {
            if (pixels[i-1][j-1] == -1)
                neighboards.add(new NeighboardSet(i-1, j-1));
            if (pixels[i-1][j] == -1)
                neighboards.add(new NeighboardSet(i-1, j));
            if (pixels[i-1][j+1] == -1)
                neighboards.add(new NeighboardSet(i-1, j+1));

            if (pixels[i][j-1] == -1)
                neighboards.add(new NeighboardSet(i, j-1));
            if (pixels[i][j+1] == -1)
                neighboards.add(new NeighboardSet(i, j+1));

            if (pixels[i+1][j-1] == -1)
                neighboards.add(new NeighboardSet(i+1, j-1));
            if (pixels[i+1][j] == -1)
                neighboards.add(new NeighboardSet(i+1, j));
            if (pixels[i+1][j+1] == -1)
                neighboards.add(new NeighboardSet(i+1, j+1));
        }

        return neighboards;
    }

    private int[][] negativePixels(int[][] pixels) {
        int[][] resPixels = new int[IMAGE_WIDTH][IMAGE_HEIGHT];

        for (int i = 0; i < IMAGE_WIDTH; i++){
            for (int j = 0; j < IMAGE_HEIGHT; j++){
                if (pixels[i][j] == 0)
                    resPixels[i][j] = -1;
                else
                    resPixels[i][j] = 0;
            }
        }

        return resPixels;
    }

    public int getNumObjects(int[][] pixels){
        innerAngle = 0;
        outherAngle = 0;
        int[][] currentMatrix = new int[3][3];

        /*initOutherInnerMatrix();*/

        for (int i = 0; i < IMAGE_WIDTH; i++){
            for (int j = 0; j < IMAGE_HEIGHT; j++) {

                if (!((i - 1 == -1) || (j - 1 == -1) || (i + 1 >= IMAGE_WIDTH) || (j + 1 >= IMAGE_HEIGHT))) {
                    currentMatrix[0][0] = pixels[i][j];
                    currentMatrix[0][1] = pixels[i][j + 1];

                    currentMatrix[1][0] = pixels[i + 1][j];
                    currentMatrix[1][1] = pixels[i + 1][j + 1];

                    equalMatrixs(currentMatrix, outherMatrix, TypeAngle.OUTHER_ANGLE);
                    equalMatrixs(currentMatrix, innerMatrix, TypeAngle.INNER_ANGLE);
                }
            }
        }

        Logger.log("Num outher angles = " + String.valueOf(outherAngle));
        Logger.log("Num inner angles = " + String.valueOf(innerAngle));
/*        Log.d(LOG_TAG, String.valueOf(outherAngle));
        Log.d(LOG_TAG, String.valueOf(innerAngle));*/

        int res = (outherAngle - innerAngle) / 4;
        Logger.log("Num of objects = " + res);

        return res;
    }

    private void equalMatrixs(int[][] currentMatrix, List<int[][]> compareMatrix, TypeAngle type) {
        for (int[][] matrix : compareMatrix) {
            boolean isOk = true;
            for (int i = 0; i < 2; i++){
                for (int j = 0; j < 2; j++){
                    if ((matrix[i][j] == currentMatrix[i][j]) && isOk)
                        isOk = true;
                    else
                        isOk = false;

                }
            }

            if (isOk && type == TypeAngle.OUTHER_ANGLE)
                outherAngle++;

            if (isOk && type == TypeAngle.INNER_ANGLE)
                innerAngle++;
        }
    }

    private void initOutherInnerMatrix(){
        //init top left matrix
        outherTopLeftMatrix[0][0] = 1;
        outherTopLeftMatrix[0][1] = 1;

        outherTopLeftMatrix[1][0] = 1;
        outherTopLeftMatrix[1][1] = 0;

        //init top right matrix
        outherTopRightMatrix[0][0] = 1;
        outherTopRightMatrix[0][1] = 1;

        outherTopRightMatrix[1][0] = 0;
        outherTopRightMatrix[1][1] = 1;

        //init bottom right matrix
        outherBottomRightMatrix[0][0] = 0;
        outherBottomRightMatrix[0][1] = 1;

        outherBottomRightMatrix[1][0] = 1;
        outherBottomRightMatrix[1][1] = 1;

        //init bottom left matrix
        outherBottomLeftMatrix[0][0] = 1;
        outherBottomLeftMatrix[0][1] = 0;

        outherBottomLeftMatrix[1][0] = 1;
        outherBottomLeftMatrix[1][1] = 1;

        //init inner top left matrix
        innerTopLeftMatrix[0][0] = 0;
        innerTopLeftMatrix[0][1] = 0;

        innerTopLeftMatrix[1][0] = 0;
        innerTopLeftMatrix[1][1] = 1;

        //init inner top right matrix
        innerTopRightMatrix[0][0] = 0;
        innerTopRightMatrix[0][1] = 0;

        innerTopRightMatrix[1][0] = 1;
        innerTopRightMatrix[1][1] = 0;

        //init inner bottom right matrix
        innerBottomRigthMatrix[0][0] = 1;
        innerBottomRigthMatrix[0][1] = 0;

        innerBottomRigthMatrix[1][0] = 0;
        innerBottomRigthMatrix[1][1] = 0;

        //init inner bottom left matrix
        innerBottomLeftMatrix[0][0] = 0;
        innerBottomLeftMatrix[0][1] = 1;

        innerBottomLeftMatrix[1][0] = 0;
        innerBottomLeftMatrix[1][1] = 0;

        //init noise matrix
        noiseMatrix[0][0] = 1;
        noiseMatrix[0][1] = 1;
        noiseMatrix[0][2] = 1;

        noiseMatrix[1][0] = 1;
        noiseMatrix[1][1] = 0;
        noiseMatrix[1][2] = 1;

        noiseMatrix[2][0] = 1;
        noiseMatrix[2][1] = 1;
        noiseMatrix[2][2] = 1;

        //initialize array of masks
        outherMatrix = new ArrayList<>();
        outherMatrix.add(outherTopLeftMatrix);
        outherMatrix.add(outherTopRightMatrix);
        outherMatrix.add(outherBottomRightMatrix);
        outherMatrix.add(outherBottomLeftMatrix);

        innerMatrix = new ArrayList<>();
        innerMatrix.add(innerTopLeftMatrix);
        innerMatrix.add(innerTopRightMatrix);
        innerMatrix.add(innerBottomRigthMatrix);
        innerMatrix.add(innerBottomLeftMatrix);
    }

    /**
     * Get pixels in binary format after semiton and binarization
     * @param img           Bitmap with image
     * @return              Pixel's array in binarization format
     * */
    public int[][] getPixels(BufferedImage img){
        int[][] resPixels = new int[IMAGE_WIDTH][IMAGE_HEIGHT];
        int rows = IMAGE_WIDTH;
        int colms = IMAGE_HEIGHT;

        for (int i = 0; i < rows; i++){
            for (int j = 0; j < colms; j++){
                //get hex of color
                String hexPixel = Integer.toHexString(img.getRGB(i, j));
                //get rbg of color in array
                int[] rgb = getRGB(hexPixel);
                //get semiton color
                double semitonColor = semitonColor(rgb);
                //get binary color of pixel
                int binaryColor = binarizationColor(semitonColor);
                resPixels[i][j] = binaryColor;
            }

        }

        return resPixels;
    }

    /**
     * Binarization of semiton color
     * @param semitonColor        Semiton color
     * @return                    Binarization color
     * */
    private int binarizationColor(double semitonColor) {
        int binaryColor;
        if (semitonColor > THRESHOLD)
            binaryColor = 1;
        else
            binaryColor = 0;

        return binaryColor;
    }

    /**
     * Refactor rgb color to semiton format
     * @param rgb        Array of rgb colors
     * @return           Semiton color
     * */
    private double semitonColor(int[] rgb) {
        int red = rgb[0];
        int green = rgb[1];
        int blue = rgb[2];

        double res = R_COEF * red + G_COEF * green + B_COEF * blue;
        return res;
    }

    /**
     * Refactor hex color into rbg colors
     * @param rgb        Rgb color string
     * @return           Array of rgb color
     * */
    private int[] getRGB(final String rgb) {
        final int[] ret = new int[3];
        for (int i = 0; i < 3; i++) {
            ret[i] = Integer.parseInt(rgb.substring(i * 2, i * 2 + 2), 16);
        }
        return ret;
    }
}
