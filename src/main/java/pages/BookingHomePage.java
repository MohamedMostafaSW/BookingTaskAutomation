package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class BookingHomePage extends BasePage {

    // Page Elements
    @FindBy(xpath = "//input[@name='ss' or @placeholder='Where are you going?']")
    private WebElement searchDestinationField;

    @FindBy(css = "button[data-testid='searchbox-dates-container']")
    private WebElement datePickerContainer;

    @FindBy(xpath = "//button[@type='submit']//span[contains(text(),'Search')]")
    private WebElement searchButton;

    @FindBy(xpath = "//button[@aria-label='Dismiss sign-in info.']")
    private WebElement dismissSignInButton;

    @FindBy(xpath = "//button[@aria-label='Close']")
    private WebElement closePopupButton;

    // Constructor
    public BookingHomePage(WebDriver driver) {
        super(driver);
    }

    /**
     * Navigate to Booking.com
     */
    public void navigateToBooking() {
        driver.get("https://www.booking.com");
        logger.info("Navigated to Booking.com");
        handlePopups();
    }

    /**
     * Handle any popups that appear on page load
     */
    private void handlePopups() {
        try {
            waitFor(1);
            if (isDisplayed(dismissSignInButton, "Dismiss Sign In Button")) {
                click(dismissSignInButton, "Dismiss Sign In Button");
            }
        } catch (Exception e) {
            logger.info("No popup to dismiss");
        }

        try {
            if (isDisplayed(closePopupButton, "Close Popup Button")) {
                click(closePopupButton, "Close Popup Button");
            }
        } catch (Exception e) {
            logger.info("No close popup button found");
        }
    }

    /**
     * Enter destination in search field
     */
    public void enterDestination(String destination) {
        try {
            click(searchDestinationField, "Search Destination Field");
            type(searchDestinationField, destination, "Search Destination Field");
        } catch (Exception e) {
            logger.error("Failed to enter destination", e);
            throw e;
        }
    }

    public void selectDates(String checkIn, String checkOut) {
        try {
            click(datePickerContainer, "Date Picker Container");
            waitFor(1);

            selectDateByValue(checkIn, "Check-in Date");
            waitFor(1);
            selectDateByValue(checkOut, "Check-out Date");

            logger.info("Dates selected - Check-in: " + checkIn + ", Check-out: " + checkOut);
        } catch (Exception e) {
            logger.error("Failed to select dates", e);
            throw e;
        }
    }

    private void selectDateByValue(String date, String dateName) {
        try {
            String xpath = "//span[@data-date='" + date + "']";
            By dateLocator = By.xpath(xpath);
            By nextButton = By.cssSelector("button[aria-label='Next month']");

            int safetyCounter = 0;
            boolean dateFound = false;

            while (!dateFound && safetyCounter < 12) {
                safetyCounter++;

                List<WebElement> elements = driver.findElements(dateLocator);
                if (!elements.isEmpty()) {
                    WebElement dateElement = elements.get(0);
                    scrollToElement(dateElement, dateName);
                    click(dateElement, dateName + ": " + date);
                    logger.info("✅ Selected " + dateName + ": " + date);
                    dateFound = true;
                } else {
                    try {
                        WebElement next = driver.findElement(nextButton);
                        next.click();
                        logger.info("➡️ Navigating to next month to find " + dateName + ": " + date);
                        waitFor(2);
                    } catch (Exception e) {
                        logger.error("❌ Could not click 'Next month' button while finding " + dateName, e);
                        break;
                    }
                }
            }

            if (!dateFound) {
                logger.error("❌ Could not find " + dateName + " on calendar after navigating 12 months: " + date);
                throw new RuntimeException("Date not found in calendar: " + date);
            }

        } catch (Exception e) {
            logger.error("❌ Failed to select date: " + date, e);
            throw e;
        }
    }

    /**
     * Click search button
     */
    public void clickSearch() {
        try {
            waitFor(1);
            scrollToElement(searchButton, "Search Button");
            click(searchButton, "Search Button");
            logger.info("Clicked Search button");
            waitForPageLoad();
            waitFor(1);
        } catch (Exception e) {
            logger.error("Failed to click search button", e);
            throw e;
        }
    }

    /**
     * Perform complete search
     */
    public void searchHotel(String destination, String checkIn, String checkOut) {
        enterDestination(destination);
        selectDates(checkIn, checkOut);
        clickSearch();
        logger.info("Hotel search completed for: " + destination);
    }

    /**
     * Verify home page is loaded
     */
    public boolean isHomePageLoaded() {
        try {
            waitForElementToBeVisible(searchDestinationField);
            return isDisplayed(searchDestinationField, "Search Destination Field");
        } catch (Exception e) {
            logger.error("Home page not loaded properly", e);
            return false;
        }
    }
}