package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Main {

    public static int m;

    public static void main(String[] args) {

        Random rand = new Random();
        String[] folders = {"hats", "tops", "bottoms", "shoes"};
        int i = 0;
        for (String folder : folders) {

            String myDirectoryPath = "../clothing/" + folder;

            File dir = new File(myDirectoryPath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                // loop through files in the folder
                for (File child : directoryListing) {
                    try {
                        BufferedImage image = ImageIO.read(child);
                        String fileName = child.getName();

                        // prepare image and write it to the same file
                        cropImage(image);
                        monoChromeImage(image);
                        blackWhiteImage(image);

                        // 20-80 split for test data to train data
                        int r = rand.nextInt(10);
                        ImageIO.write(image, "jpg", new File("../clothing/" + (r < 2 ? "test-data" : "train-data") + "/" + i + "/" + fileName));

                    } catch (IOException e) {
                        System.out.println("Error: " + e);
                    }
                }
            }
            i += 1;
        }
    }

    // given an image crop it
    public static void cropImage(BufferedImage image) {

        int x = image.getWidth();
        int y = image.getHeight();
        double ratio;

        // maintain ratio and set smaller dimension to 100
        if (x < y) {
            ratio = 100.0 / x;
            x = 100;
            y = (int) (ratio * ((double) y));
        } else {
            ratio = 100.0 / y;
            y = 100;
            x = (int) (ratio * ((double) x));
        }

        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();

        BufferedImage newImage = new BufferedImage(x, y, type);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, x, y, null);
        g.dispose();

        // crop smaller dimension to 100, centered
        if (x != 100) {
            int dif;
            if ((dif = x - 100) > 0) {
                newImage = newImage.getSubimage(dif / 2, 0, 100, 100);
            }
        } else {
            int dif;
            if ((dif = y - 100) > 0) {
                newImage = newImage.getSubimage(0, dif / 2, 100, 100);
            }
        }
    }

    // make a buffered image grayscale and the foreground lighter than the background
    public static void monoChromeImage(BufferedImage image) {

        // grayscale
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                Color c = new Color(image.getRGB(j, i));
                int red = (int) (c.getRed() * 0.299);
                int green = (int) (c.getGreen() * 0.587);
                int blue = (int) (c.getBlue() * 0.114);
                Color newColor = new Color(
                        red + green + blue,
                        red + green + blue,
                        red + green + blue);

                image.setRGB(j, i, newColor.getRGB());
            }
        }

        int b = 0;
        int w = 0;

        // count # of dark and light pixels on the border of the image
        for (int i = 0; i < 100; i ++) {

            Color c = new Color(image.getRGB(i, 0));

            if (c.getRed() <= 80) {
                b++;
            }

            else if (c.getRed() >= 175) {
                w++;
            }

            c = new Color(image.getRGB(i, 99));

            if (c.getRed() <= 80) {
                b++;
            }

            else if (c.getRed() >= 175) {
                w++;
            }
        }

        for (int i = 1; i < 99; i ++) {

            Color c = new Color(image.getRGB(0, i));

            if (c.getRed() <= 80) {
                b++;
            }

            else if (c.getRed() >= 175) {
                w++;
            }

            c = new Color(image.getRGB(99, i));

            if (c.getRed() <= 80) {
                b++;
            }

            else if (c.getRed() >= 175) {
                w++;
            }
        }

        // flip dark and light pixels if there are more light pixels
        if (b < w) {
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {

                    Color c = new Color(image.getRGB(j, i));
                    Color newColor = new Color(
                            255 - c.getRed(),
                            255 - c.getGreen(),
                            255 - c.getBlue());

                    image.setRGB(j, i, newColor.getRGB());
                }
            }
        }
    }

    // make a buffered image black and white
    public static void blackWhiteImage(BufferedImage image) {

        // BFS algorithm to convert dark pixels to black
        boolean[][] searched = new boolean[100][100];

        m = 0;
        blackWhiteBFS(image, searched, 0, 0);
        blackWhiteBFSReverse(image, searched, 99, 99);

        // convert remaining pixels to white
        for (int k = 0; k < 100; k++) {
            for (int l = 0; l < 100; l ++) {
                Color c = new Color(image.getRGB(k, l));
                if (c.getBlue() + c.getGreen() + c.getRed() > 50) {

                    image.setRGB(k, l, new Color(255,255,255).getRGB());
                }
            }
        }
    }

    // BFS from top-left
    public static boolean[][] blackWhiteBFS(BufferedImage image, boolean[][] searched, int i, int j) {
        if (i < 100 && j < 100 && !searched[i][j]) {
            searched[i][j] = true;

            Color c = new Color(image.getRGB(i, j));

            if (c.getBlue() + c.getGreen() + c.getRed() <= 100) {

                m++;

                image.setRGB(i, j, new Color(0, 0, 0).getRGB());

                searched = blackWhiteBFS(image, searched, i + 1, j);
                searched = blackWhiteBFS(image, searched, i, j + 1);
                searched = blackWhiteBFS(image, searched, i + 1, j + 1);
            }
        }

        return searched;
    }

    // BFS from bottom-right
    public static boolean[][] blackWhiteBFSReverse(BufferedImage image, boolean[][] searched, int i, int j) {

        if (i >= 0 && j >= 0 && !searched[i][j]) {

            searched[i][j] = true;

            Color c = new Color(image.getRGB(i, j));

            if (c.getBlue() + c.getGreen() + c.getRed() <= 100) {

                m++;

                image.setRGB(i, j, new Color(0, 0, 0).getRGB());

                searched = blackWhiteBFSReverse(image, searched, i - 1, j);
                searched = blackWhiteBFSReverse(image, searched, i, j - 1);
                searched = blackWhiteBFSReverse(image, searched, i - 1, j - 1);
            }
        }

        return searched;
    }
}