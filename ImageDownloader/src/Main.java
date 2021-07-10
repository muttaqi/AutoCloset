package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Main {

    static final String projectLocation = System.getProperty("user.dir");
    static final String SHOES_URL = "https://www.google.com/search?q=shoes&source=lnms&tbm=isch&sa=X&ved=0ahUKEwjA2_bB2L_iAhVoiOAKHR8JBGEQ_AUIDigB&biw=1920&bih=937";
    static final String TOPS_URL = "https://www.google.com/search?q=shirts&tbm=isch&ved=2ahUKEwimjICr2NfxAhURiJ4KHf1VAyoQ2-cCegQIABAA&oq=shirts&gs_lcp=CgNpbWcQAzIHCAAQsQMQQzIECAAQQzIHCAAQsQMQQzIECAAQQzICCAAyAggAMgIIADICCAAyAggAMgIIADoECCMQJzoFCAAQsQNQ9gRYsAxgrw1oAHAAeACAAYQBiAHJBJIBAzUuMZgBAKABAaoBC2d3cy13aXotaW1nwAEB&sclient=img&ei=TCXpYKa2FJGQ-gT9q43QAg&bih=937&biw=1920";
    static final String BOTTOMS_URL = "https://www.google.com/search?q=pants&sxsrf=ALeKk012Mnd1BSOznmFl7dZifeFmF24a-g:1625892202673&source=lnms&tbm=isch&sa=X&ved=2ahUKEwj2sLu52NfxAhVYuZ4KHdYcDasQ_AUoAXoECAIQAw&biw=1280&bih=577&dpr=1.5";
    static final String HATS_URL = "https://www.google.com/search?q=hats&tbm=isch&ved=2ahUKEwin8vC62NfxAhWXlZ4KHS7MDzEQ2-cCegQIABAA&oq=hats&gs_lcp=CgNpbWcQAzIECCMQJzIHCAAQsQMQQzIECAAQQzIECAAQQzIECAAQQzICCAAyBQgAELEDMgIIADICCAAyAggAUPh9WLuAAWCthgFoAHAAeACAAWeIAfECkgEDMy4xmAEAoAEBqgELZ3dzLXdpei1pbWfAAQE&sclient=img&ei=bSXpYOeHJ5er-gSumL-IAw&bih=577&biw=1280";

    // given a url, open it as a Selenium driver
    public static WebDriver openSite(String url) {

        System.setProperty("webdriver.chrome.driver", projectLocation + "\\libs\\chromedriver\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();

        try {
            driver.manage().deleteAllCookies();
            driver.manage().window().maximize();
            driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

            driver.get(url);
        } catch (Exception e) {

            e.printStackTrace();
        }

        return driver;
    }

    public static void download(String url, String folderName) {

        WebDriver driver = openSite(url);

        // use an executor to scroll down the url
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // wait for page to load
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie ) {}
        // scroll down
        js.executeScript("window.scrollBy(0,5000)");

        int i = 0;
        // find img tags
        for (WebElement we: driver.findElements(By.tagName("img"))) {
            // skip google image
            if (!we.getAttribute("alt").equals("Google")) {
                try {
                    // get src of the img
                    URL imageUrl = new URL(we.getAttribute("src"));

                    // read in img url
                    BufferedImage image = ImageIO.read(imageUrl);

                    // if successful read, write to a file
                    if (image != null) {
                        i++;
                        ImageIO.write(image, "jpg", new File("../clothing/" + folderName + "-" + i + ".jpg"));
                    }
                } catch (IOException ioe) {
                    }
            }
        }
    }

    public static void main(String[] args) {
        download(HATS_URL, "hats");
        download(TOPS_URL, "tops");
        download(BOTTOMS_URL, "bottoms");
        download(TOPS_URL, "tops");
    }
}
