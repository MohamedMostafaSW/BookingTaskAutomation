package utils;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotUtil {

    private static final Logger logger = LogManager.getLogger(ScreenshotUtil.class);
    private static final String SCREENSHOT_FOLDER = System.getProperty("user.dir") + "/screenshots/";

    /**
     * Take screenshot with status (PASS/FAIL/SKIP)
     *
     * @param driver WebDriver instance
     * @param testName Name of the test
     * @param status Test status to include in file name
     * @return Screenshot file path
     */
    public static String takeScreenshotWithStatus(WebDriver driver, String testName, String status) {
        String screenshotPath = null;

        try {
            File screenshotDir = new File(SCREENSHOT_FOLDER);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String cleanTestName = testName.replaceAll("[^a-zA-Z0-9]", "_");

            String fileName = cleanTestName + "_" + status + "_" + timestamp + ".png";
            screenshotPath = SCREENSHOT_FOLDER + fileName;
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File dest = new File(screenshotPath);
            FileUtils.copyFile(src, dest);

            logger.info("📸 Screenshot saved: " + fileName);

        } catch (IOException e) {
            logger.error("Failed to capture screenshot", e);
        } catch (Exception e) {
            logger.error("Unexpected error while capturing screenshot", e);
        }

        return screenshotPath;
    }

    /**
     * Take screenshot with custom file name
     */
    public static String takeScreenshotWithCustomName(WebDriver driver, String fileName) {
        String screenshotPath = null;

        try {
            File screenshotDir = new File(SCREENSHOT_FOLDER);
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }

            screenshotPath = SCREENSHOT_FOLDER + fileName + ".png";

            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            File destinationFile = new File(screenshotPath);

            FileUtils.copyFile(sourceFile, destinationFile);

            logger.info("Screenshot saved: " + fileName);

        } catch (Exception e) {
            logger.error("Failed to capture screenshot with custom name", e);
        }

        return screenshotPath;
    }
}
