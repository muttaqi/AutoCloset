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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class Main {

    public static int m;

    public static void main(String[] args) {

        for (int k = 0; k < 4; k ++) {

            String myDirectoryPath = "C:\\Users\\Home-PC_2\\dl4j-examples - Copy\\dl4j-examples\\src\\main\\resources\\clothes\\train-data\\" + k + " - Copy";

            File dir = new File(myDirectoryPath);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {

                    System.out.println("Name: " + child.getName());

                    try {

                        BufferedImage image = ImageIO.read(child);
                        String path = child.getPath();

                        if (image.getWidth() == 100 && image.getHeight() == 100) {
                            //cropImage(image);

                            /*monoChromeImage(image);

                            blackWhiteImage(image);*/

                            image = detectClothing(image);

                            ImageIO.write(image, "jpg", new File(path));
                        }

                    } catch (IOException e) {
                        System.out.println("Error: " + e);
                    }
                }
            }
        }
    }

    public static void cropImage(BufferedImage image) {

        int x = image.getWidth();
        int y = image.getHeight();
        double ratio;

        if (x < y) {

            System.out.println("x: " + x);
            ratio = 100.0 / x;
            x = 100;
            y = (int) (ratio * ((double) y));
        } else {

            System.out.println("y: " + y);
            ratio = 100.0 / y;
            y = 100;
            x = (int) (ratio * ((double) x));
        }

        int type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();

        BufferedImage newImage = new BufferedImage(x, y, type);
        Graphics2D g = newImage.createGraphics();
        g.drawImage(image, 0, 0, x, y, null);
        g.dispose();

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

    public static void monoChromeImage(BufferedImage image) {

        for (int i = 0; i < 100; i++) {

            for (int j = 0; j < 100; j++) {

                //System.out.println("coords: " + i + " " + j);
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

    public static void blackWhiteImage(BufferedImage image) {

        boolean[][] searched = new boolean[100][100];

        m = 0;
        blackWhiteBFS(image, searched, 0, 0);
        blackWhiteBFSReverse(image, searched, 99, 99);

        for (int k = 0; k < 100; k++) {

            for (int l = 0; l < 100; l ++) {

                Color c = new Color(image.getRGB(k, l));

                if (c.getBlue() + c.getGreen() + c.getRed() > 50) {

                    image.setRGB(k, l, new Color(255,255,255).getRGB());
                }
            }
        }

        System.out.println("DEBUG 212 " + m);
    }

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

    public BufferedImage detectClothing(BufferedImage bi) {

        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] pixels = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();

        mat.put(0, 0, pixels);

        Mat blurredMat = new Mat();
        Mat hsvMat = new Mat();
        Mat mask = new Mat();
        Mat morphOutput = new Mat();

        Imgproc.blur(mat, blurredMat, new Size(7, 7));
        Imgproc.cvtColor(blurredMat, hsvMat, Imgproc.COLOR_BGR2HSV);

        Core.inRange(hsvMat, new Scalar(50, 60, 50), new Scalar(130, 180, 255), mask);

        Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 24));
        Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(12, 12));

        Imgproc.erode(mask, morphOutput, erodeElement);
        Imgproc.erode(mask, morphOutput, erodeElement);
        
        Imgproc.dilate(mask, morphOutput, dilateElement);
        Imgproc.dilate(mask, morphOutput, dilateElement);

        return matToBufferedImage(morphOutput);
    }
    
	/**
	 * Support for the {@link mat2image()} method
	 * 
	 * @param original
	 *            the {@link Mat} object in BGR or grayscale
	 * @return the corresponding {@link BufferedImage}
     * taken from https://github.com/opencv-java/object-detection/blob/master/src/it/polito/elite/teaching/cv/utils/Utils.java
     * credits to Luigi De Russis: github.com/luigidr
	 */
	private static BufferedImage matToBufferedImage(Mat original)
	{
		// init
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);
		
		if (original.channels() > 1)
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}
		else
		{
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
		
		return image;
	}
}