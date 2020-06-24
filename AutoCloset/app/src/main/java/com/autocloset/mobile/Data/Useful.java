package com.autocloset.mobile.Data;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;

public class Useful {

    public static int m;

    public static Bitmap cropImage(Bitmap image) {

        int x = image.getWidth();
        int y = image.getHeight();
        double ratio;

        if (x < y) {

            System.out.println("DEBUG U 18 x: " + x);
            ratio = 100.0 / x;
            x = 100;
            y = (int) (ratio * ((double) y));

            image = Bitmap.createScaledBitmap(image, x, y, true);

            return Bitmap.createBitmap(image, 0, (y - 100) /2, 100, 100);
        } else {

            System.out.println("DEBUG U 28 y: " + y);
            ratio = 100.0 / y;
            y = 100;
            x = (int) (ratio * ((double) x));

            image = Bitmap.createScaledBitmap(image, x, y, true);

            return Bitmap.createBitmap(image, (x - 100) /2, 0, 100, 100);
        }

    }

    public static Bitmap monoChromeImage(Bitmap image) {

        for (int i = 0; i < 100; i++) {

            for (int j = 0; j < 100; j++) {

                int c = image.getPixel(j, i);

                int r = (int) (((double) Color.red(c)) * 0.299);
                int g = (int) (((double)Color.green(c)) * 0.587);
                int b = (int) (((double)Color.blue(c)) * 0.114);

                int newColor = r + g + b;
                int newColor2 = Color.rgb(newColor, newColor, newColor);

                System.out.println("DEBUG U 46 " + newColor2);

                image.setPixel(j, i, newColor2);
            }
        }

        int b = 0;
        int w = 0;

        for (int i = 0; i < 100; i ++) {

            int c = image.getPixel(i, 0);

            if (Color.red(c) <= 80) {

                b++;
            }

            else if (Color.red(c) >= 175) {

                w++;
            }

            c = image.getPixel(i, 99);

            if (Color.red(c) <= 80) {

                b++;
            }

            else if (Color.red(c) >= 175) {

                w++;
            }
        }

        for (int i = 1; i < 99; i ++) {

            int c = image.getPixel(0, i);

            if (Color.red(c) <= 80) {

                b++;
            }

            else if (Color.red(c) >= 175) {

                w++;
            }

            c = image.getPixel(99, i);

            if (Color.red(c) <= 80) {

                b++;
            }

            else if (Color.red(c) >= 175) {

                w++;
            }
        }

        if (b < w) {

            for (int i = 0; i < 100; i++) {

                for (int j = 0; j < 100; j++) {

                    int c = image.getPixel(j, i);

                    int r = Color.red(c);
                    int g = Color.green(c);
                    int bl = Color.blue(c);

                    int newColor = Color.rgb(r, g, bl);

                    image.setPixel(j, i, newColor);
                }
            }
        }

        return image;
    }

    public static Bitmap blackWhiteImage(Bitmap image) {

        boolean[][] searched = new boolean[100][100];

        m = 0;

        int black = 0;
        int white = 0;

        for (int i = 0; i < 100; i ++) {

            int p = image.getPixel(0, i);

            int r = (short) ((p >> 16) & 0xFF);
            int g = (short) ((p >> 8) & 0xFF);
            int b = (short) (p & 0xFF);

            if (r + g + b <= 200) {

                black ++;
            }

            else if (r + g + b >= 665) {

                white ++;
            }

            p = image.getPixel(i, 0);

            r = (short) ((p >> 16) & 0xFF);
            g = (short) ((p >> 8) & 0xFF);
            b = (short) (p & 0xFF);

            if (r + g + b <= 200) {

                black ++;
            }

            else if (r + g + b >= 665) {

                white ++;
            }

            p = image.getPixel(i, 99);

            r = (short) ((p >> 16) & 0xFF);
            g = (short) ((p >> 8) & 0xFF);
            b = (short) (p & 0xFF);

            if (r + g + b <= 200) {

                black ++;
            }

            else if (r + g + b >= 665) {

                white ++;
            }

            p = image.getPixel(99, i);

            r = (short) ((p >> 16) & 0xFF);
            g = (short) ((p >> 8) & 0xFF);
            b = (short) (p & 0xFF);

            if (r + g + b < 382) {

                black ++;
            }

            else {

                white ++;
            }
        }

        System.out.println("DEBUG U 216 " + white + ", " + black);

        ArrayList<int[]> queue = new ArrayList<>();
        queue.add(new int[]{0, 0, -1});

        blackWhiteBFS(image, searched, queue, new int[]{0, 1, 1}, new int[]{1, 0, 1});

        queue.clear();
        queue.add(new int[]{99, 99, -1});
        blackWhiteBFS(image, searched, queue, new int[]{0, -1, -1}, new int[]{-1, 0, -1});

        queue.clear();
        queue.add(new int[]{0, 99, -1});
        blackWhiteBFS(image, searched, queue, new int[]{0, 1, 1}, new int[]{-1, 0, -1});

        queue.clear();
        queue.add(new int[]{99, 0, -1});
        blackWhiteBFS(image, searched, queue, new int[]{0, -1, -1}, new int[]{1, 0, 1});

        for (int k = 0; k < 100; k++) {

            for (int l = 0; l < 100; l ++) {

                int c = image.getPixel(k, l);

                int r = Color.red(c);
                int g = Color.green(c);
                int b = Color.blue(c);

                if (b + g + r > 30) {

                    image.setPixel(k, l, Color.WHITE);
                }
            }
        }

        System.out.println("DEBUG U 212 " + m);

        return image;
    }

    public static boolean[][] blackWhiteBFS(Bitmap image, boolean[][] searched, ArrayList<int[]> queue, int[] dI, int[] dJ) {

        if (queue.size() > 0) {

            int i = queue.get(0)[0];
            int j = queue.get(0)[1];
            int lastC = queue.get(0)[2];

            if (i < 100 && i >= 0 && j < 100 && j >= 0 && !searched[i][j]) {

                searched[i][j] = true;

                int p = image.getPixel(i, j);

                int r = (short) ((p >> 16) & 0xFF);
                int g = (short) ((p >> 8) & 0xFF);
                int b = (short) (p & 0xFF);

                int c = r + g + b;

                System.out.println("DEBUG U 182 " + lastC + ", " + c);

                if (lastC == -1) {

                    image.setPixel(i, j, Color.BLACK);

                    queue.add(new int[]{i + dI[0], j + dJ[0], c});
                    queue.add(new int[]{i + dI[1], j + dJ[1], c});
                    queue.add(new int[]{i + dI[2], j + dJ[2], c});

                } else if (Math.abs(c - lastC) > 100) {

                    image.setPixel(i, j, Color.WHITE);

                    ArrayList<int[]> subQ = new ArrayList<>();
                    subQ.add(new int[]{i, j, c});

                    searched = subBFS(image, searched, subQ, dI, dJ);
                } else {

                    image.setPixel(i, j, Color.BLACK);

                    queue.add(new int[]{i + dI[0], j + dJ[0], c});
                    queue.add(new int[]{i + dI[1], j + dJ[1], c});
                    queue.add(new int[]{i + dI[2], j + dJ[2], c});
                }
            }

            queue.remove(0);
            searched = blackWhiteBFS(image, searched, queue, dI, dJ);
        }

        return searched;
    }

    public static boolean[][] subBFS(Bitmap image, boolean[][] searched, ArrayList<int[]> queue, int[] dI, int[] dJ) {

        if (queue.size() > 0) {

            int i = queue.get(0)[0];
            int j = queue.get(0)[1];
            int lastC = queue.get(0)[2];

            if (i < 100 && i >= 0 && j < 100 && j >= 0 && !searched[i][j]) {

                searched[i][j] = true;

                int p = image.getPixel(i, j);

                int r = (short) ((p >> 16) & 0xFF);
                int g = (short) ((p >> 8) & 0xFF);
                int b = (short) (p & 0xFF);

                int c = r + g + b;

                System.out.println("DEBUG U 182 " + lastC + ", " + c);

                if (Math.abs(c - lastC) <= 100) {

                    image.setPixel(i, j, Color.WHITE);

                    queue.add(new int[]{i + dI[0], j + dJ[0], c});
                    queue.add(new int[]{i + dI[1], j + dJ[1], c});
                    queue.add(new int[]{i + dI[2], j + dJ[2], c});
                }

                else {

                    searched[i][j] = false;
                }
            }

            queue.remove(0);
            searched = subBFS(image, searched, queue, dI, dJ);
        }

        return searched;
    }
}
