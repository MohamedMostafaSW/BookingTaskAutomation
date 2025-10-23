package base;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import utils.ScreenshotUtil;
import utils.VideoRecorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Properties;

public class BaseTest {
    protected static WebDriver driver;
    protected static Properties config;
    protected static Logger logger = LogManager.getLogger(BaseTest.class);
    private VideoRecorder videoRecorder;
    private static ExtentReports extent;
    private static ExtentTest test;
    private static ExtentSparkReporter spark;

    @BeforeSuite
    public void setUpSuite() {
        loadConfiguration();
        createOutputDirectories();
        String reportPath = System.getProperty("user.dir") + "/reports/ExtentReport.html";
        spark = new ExtentSparkReporter(reportPath);
        extent = new ExtentReports();
        extent.attachReporter(spark);

        logger.info("Extent report initialized at: " + reportPath);
    }

    @BeforeMethod
    public void setup(Method method) throws Exception {
        setupDriver();
        startVideoRecording();
        test = extent.createTest(method.getName());
        test.info("Test started: " + method.getName());

        logger.info("Browser launched and video recording started");
    }

    @AfterMethod
    public void tearDown(ITestResult result) throws Exception {
        if (result.getStatus() == ITestResult.FAILURE) {
            test.fail("❌ Test Failed: " + result.getThrowable());
            ScreenshotUtil.takeScreenshotWithStatus(driver, result.getName(), "FAIL");
        } else if (result.getStatus() == ITestResult.SUCCESS) {
            test.pass("✅ Test Passed: " + result.getName());
            ScreenshotUtil.takeScreenshotWithStatus(driver, result.getName(), "PASS");
        } else if (result.getStatus() == ITestResult.SKIP) {
            test.skip("⚠️ Test Skipped: " + result.getName());
        }
        stopVideoRecording();

        if (driver != null) {
            driver.quit();
            logger.info("Browser closed");
        }
    }

    @AfterSuite
    public void tearDownSuite() {
        if (extent != null) {
            extent.flush();
            logger.info("Extent report flushed successfully");
        }
    }

    /**
     * Setup Chrome WebDriver with options
     */
    private void setupDriver() {
        logger.info("Setting up Chrome WebDriver");
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");
        String headless = config.getProperty("headless", "true");
        String maximize = config.getProperty("maximize", "true");
        if (headless.equalsIgnoreCase("true")) {
            options.addArguments("--headless=new");
            logger.info("Running in headless mode");
        }

        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");

        driver = new ChromeDriver(options);

        int implicitWait = Integer.parseInt(config.getProperty("implicit.wait", "10"));
        int pageLoadTimeout = Integer.parseInt(config.getProperty("page.load.timeout", "30"));

        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        if (maximize.equalsIgnoreCase("true")) {
            driver.manage().window().maximize();
        }

        logger.info("Chrome driver setup completed");
    }

    /**
     * Load configuration from config.properties
     */
    private void loadConfiguration() {
        try {
            config = new Properties();
            String configPath = System.getProperty("user.dir") + "/src/main/resources/config.properties";
            FileInputStream fis = new FileInputStream(configPath);
            config.load(fis);
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load configuration file", e);
            // Set default properties
            config = new Properties();
            config.setProperty("base.url", "https://www.google.com");
            config.setProperty("implicit.wait", "10");
            config.setProperty("page.load.timeout", "30");
            config.setProperty("headless", "false");
            config.setProperty("maximize", "true");
        }
    }

    /**
     * Create output directories for screenshots, videos, and reports
     */
    private void createOutputDirectories() {
        String[] directories = {"screenshots", "videos", "reports", "test-output"};

        for (String dir : directories) {
            File directory = new File(System.getProperty("user.dir") + "/" + dir);
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    logger.info("Created directory: " + dir);
                }
            }
        }
    }

    /**
     * Start video recording
     */
    private void startVideoRecording() throws Exception {
        videoRecorder = new VideoRecorder();
        videoRecorder.startRecording();
        logger.info("Video recording started");
    }

    /**
     * Stop video recording
     */
    private void stopVideoRecording() throws Exception {
        if (videoRecorder != null) {
            videoRecorder.stopRecording();
            logger.info("Video recording stopped");
        }
    }

    /**
     * Get WebDriver instance
     */
    public static WebDriver getDriver() {
        return driver;
    }
}