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

    public static void main(String[] args) {

        //change link based on type of clothing
        WebDriver driver = openSite("https://www.google.com/search?q=shoes&source=lnms&tbm=isch&sa=X&ved=0ahUKEwjA2_bB2L_iAhVoiOAKHR8JBGEQ_AUIDigB&biw=1920&bih=937");

        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {

            Thread.sleep(3000);
        } catch (InterruptedException ie ) {}
        js.executeScript("window.scrollBy(0,5000)");


        int i = 0;
        for (WebElement we: driver.findElements(By.tagName("img"))) {

            if (!we.getAttribute("alt").equals("Google")) {

                try {

                    URL imageUrl = new URL(we.getAttribute("src"));

                    System.out.println(imageUrl.toString());

                    BufferedImage image = ImageIO.read(imageUrl);

                    if (image != null) {

                        i++;
                                                                                                                                                                            //change this based on type of clothing
                        ImageIO.write(image, "jpg", new File("C:\\Users\\Home-PC_2\\dl4j-examples - Copy\\dl4j-examples\\src\\main\\resources\\clothes\\shoes\\shoes-" + i + ".jpg"));
                    }
                } catch (IOException ioe) {
                    }
            }
        }
    }
}
