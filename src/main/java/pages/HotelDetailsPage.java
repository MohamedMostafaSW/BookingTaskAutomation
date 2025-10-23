package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class HotelDetailsPage extends BasePage {

    // Page Elements
    @FindBy(xpath = "//button[contains(@class, 'js-dismiss-banner')]")
    private WebElement dismissBannerButton;

    @FindBy(xpath = "//button[contains(@aria-label, 'Close')]")
    private WebElement closePopupButton;

    // Constructor
    public HotelDetailsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Handle popups on hotel details page
     */
    private void handlePopups() {
        try {
            waitFor(2);
            try {
                if (isDisplayed(dismissBannerButton, "Dismiss Banner")) {
                    click(dismissBannerButton, "Dismiss Banner");
                }
            } catch (Exception e) {
                logger.info("No banner to dismiss");
            }

            try {
                if (isDisplayed(closePopupButton, "Close Popup")) {
                    click(closePopupButton, "Close Popup");
                }
            } catch (Exception e) {
                logger.info("No popup to close");
            }
        } catch (Exception e) {
            logger.info("No popups found");
        }
    }

    /**
     * Click See Availability button
     */
    public void clickSeeAvailability() {
        handlePopups();
        waitFor(2);
        ((JavascriptExecutor) driver)
                .executeScript("window.scrollBy(0, 500)");
        waitFor(1);
        List<WebElement> availabilityButtons = driver.findElements(
                By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'availability') or contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'see prices')]")
        );
        WebElement button = availabilityButtons.get(0);
        scrollToElement(button, "See Availability Button");
        waitFor(1);
        clickUsingJS(button, "See Availability Button");
    }

    /**
     * Get check-in date displayed on page
     */
    public String getDisplayedCheckInDate() {
        waitFor(1);
        String date = null;

        WebElement checkInElement = driver.findElement(
                By.xpath("//span[@data-testid='date-display-field-start'] | //div[contains(@class,'check-in')]//span"));
        date = checkInElement.getText().trim();

        logger.info("Displayed Check-in Date: " + date);
        return date;
    }

    /**
     * Get check-out date displayed on page
     */
    public String getDisplayedCheckOutDate() {
        waitFor(1);
        String date = null;
        WebElement checkOutElement = driver.findElement(
                By.xpath("//span[@data-testid='date-display-field-end'] | //div[contains(@class,'check-out')]//span"));
        date = checkOutElement.getText().trim();
        logger.info("Displayed Check-out Date: " + date);
        return date;
    }

    /**
     * Select room and amount, then click reserve
     */
    public void selectRoomAndReserve(int roomIndex) {
        handlePopups();
        waitFor(1);
        scrollToRoomsSection();
        expandRoomOptions();
        selectRoom(roomIndex);
        clickReserveButton();
        logger.info("Room selected and reserve button clicked");
        waitForPageLoad();
    }

    /**
     * Scroll to rooms section
     */
    private void scrollToRoomsSection() {
        try {
            ((JavascriptExecutor) driver)
                    .executeScript("window.scrollTo(0, document.body.scrollHeight/2)");
            waitFor(1);
        } catch (Exception e) {
            logger.warn("Could not scroll to rooms section", e);
        }
    }

    /**
     * Expand room options if needed
     */
    private void expandRoomOptions() {
        try {
            List<WebElement> selectRoomsButtons = driver.findElements(
                    By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'select room')]")
            );

            if (!selectRoomsButtons.isEmpty()) {
                click(selectRoomsButtons.get(0), "Select Rooms Button");
                waitFor(1);
            }
        } catch (Exception e) {
            logger.info("No need to expand room options");
        }
    }

    /**
     * Select specific room
     */
    private void selectRoom(int roomIndex) {
        try {
            List<WebElement> roomSelectors = driver.findElements(
                    By.xpath("//select[contains(@name, 'room') or contains(@class, 'room')] | //input[@type='radio' and contains(@name, 'room')]")
            );

            if (!roomSelectors.isEmpty() && roomIndex < roomSelectors.size()) {
                WebElement selector = roomSelectors.get(roomIndex);
               scrollToElement(selector,"room dropDown");
                waitFor(1);
                clickUsingJS(selector,"Select numbers of room");
                logger.info("Clicked Room Selector " + roomIndex);

                if (selector.getTagName().equals("select")) {
                    selectByIndex(selector, 1, "Room Amount");
                }
            } else {
                logger.warn("Using default room selection");
            }

            waitFor(1);
        } catch (Exception e) {
            logger.warn("Could not select specific room, using default", e);
        }
    }

    /**
     * Click "I'll reserve" button
     */
    private void clickReserveButton() {
        try {
            List<WebElement> reserveButtons = driver.findElements(
                    By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reserve')] | " +
                            "//a[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), 'reserve')]")
            );

            if (!reserveButtons.isEmpty()) {
                WebElement button = reserveButtons.get(0);
                scrollToElement(button, "Reserve Button");
                waitFor(1);
                clickUsingJS(button, "Reserve Button");
                logger.info("Clicked Reserve button");
                waitForPageLoad();
                waitFor(1);
            } else {
                logger.error("Reserve button not found");
                throw new RuntimeException("Reserve button not found");
            }

        } catch (Exception e) {
            logger.error("Failed to click reserve button", e);
            throw e;
        }
    }

    /**
     * Verify hotel details page is loaded
     */
    public boolean isHotelDetailsPageLoaded() {
        try {
            waitFor(1);
            String url = driver.getCurrentUrl();
            return url.contains("hotel") || url.contains("property");
        } catch (Exception e) {
            logger.error("Hotel details page not loaded", e);
            return false;
        }
    }
}