package base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected static final Logger logger = LogManager.getLogger(BasePage.class);
    private static final int DEFAULT_WAIT_TIME = 20;
    /**
     * Constructor - Initialize page elements
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_WAIT_TIME));
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    /**
     * Click on element
     */
    protected void click(WebElement element, String elementName) {
        try {
            waitForElementToBeClickable(element);
            element.click();
            logger.info("Clicked on: " + elementName);
        } catch (Exception e) {
            logger.error("Unable to click on: " + elementName, e);
            throw e;
        }
    }

    /**
     * Click using JavaScript
     */
    protected void clickUsingJS(WebElement element, String elementName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].click();", element);
            logger.info("Clicked on (using JS): " + elementName);
        } catch (Exception e) {
            logger.error("Unable to click using JS on: " + elementName, e);
            throw e;
        }
    }

    /**
     * Type text into element
     */
    protected void type(WebElement element, String text, String elementName) {
        try {
            waitForElementToBeVisible(element);
            element.clear();
            element.sendKeys(text);
            logger.info("Typed '" + text + "' into: " + elementName);
        } catch (Exception e) {
            logger.error("Unable to type into: " + elementName, e);
            throw e;
        }
    }
    /**
     * Check if element is displayed
     */
    protected boolean isDisplayed(WebElement element, String elementName) {
        try {
            boolean displayed = element.isDisplayed();
            logger.info(elementName + " is displayed: " + displayed);
            return displayed;
        } catch (Exception e) {
            logger.warn(elementName + " is not displayed");
            return false;
        }
    }

    /**
     * Select dropdown by index
     */
    protected void selectByIndex(WebElement element, int index, String elementName) {
        try {
            Select select = new Select(element);
            select.selectByIndex(index);
            logger.info("Selected index " + index + " from dropdown: " + elementName);
        } catch (Exception e) {
            logger.error("Unable to select by index from dropdown: " + elementName, e);
            throw e;
        }
    }

    /**
     * Scroll to element
     */
    protected void scrollToElement(WebElement element, String elementName) {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);

            logger.info("Scrolled to: " + elementName);
        } catch (Exception e) {
            logger.error("Unable to scroll to: " + elementName, e);
            throw e;
        }
    }

    /**
     * Wait for element to be visible
     */
    protected void waitForElementToBeVisible(WebElement element) {
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Wait for element to be clickable
     */
    protected void waitForElementToBeClickable(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Wait for page to load
     */
    protected void waitForPageLoad() {
        wait.until(driver -> ((JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
        logger.info("Page loaded completely");
    }

    /**
     * Custom wait
     */
    protected void waitFor(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            logger.error("Wait interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
    /**
     * Convert date from Excel format (yyyy-MM-dd) to UI display format
     *
     * @param excelDate Date string from Excel
     * @return Formatted date string
     */
    public static String formatDateForUI(String excelDate) {
        try {
            LocalDate date = LocalDate.parse(excelDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH);
            return date.format(formatter);
        } catch (Exception e) {
            logger.error("❌ Failed to format date: " + excelDate, e);
            return excelDate;
        }
    }


}