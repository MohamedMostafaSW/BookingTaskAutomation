package pages;

import base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchResultsPage extends BasePage {

    // Page Elements
    @FindBy(xpath = "//button[contains(@aria-label, 'Dismiss') or contains(@class, 'dismiss')]")
    private WebElement dismissButton;

    @FindBy(xpath = "//button[contains(@aria-label, 'Close') or contains(@class, 'close')]")
    private WebElement closePopupButton;

    // Constructor
    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    /**
     * Handle popups on search results page
     */
    private void handlePopups() {
        try {
            waitFor(1);
            if (isDisplayed(closePopupButton, "Close Popup Button")) {
                click(closePopupButton, "Close Popup Button");
            }
        } catch (Exception e) {
            logger.warn("Close Popup Button is not displayed");
        }

        try {
            if (isDisplayed(dismissButton, "Dismiss Button")) {
                click(dismissButton, "Dismiss Button");
            }
        } catch (Exception e) {
            logger.info("No dismiss button found");
        }
    }

    /**
     * Select hotel from search results
     */
    public void selectHotel(String hotelName) {
        handlePopups();

        logger.info("Searching for hotel: " + hotelName);

        String mainWindow = driver.getWindowHandle();
        int maxPages = 20;
        boolean hotelFound = false;

        for (int page = 1; page <= maxPages; page++) {
            logger.info("Searching on page: " + page);
            waitFor(1);

            try {
                hotelFound = searchHotel(hotelName, page);

                if (hotelFound) {
                    break;
                }

                // Go to next page
                if (page < maxPages) {
                    boolean hasNextPage = goToNextPage();
                    if (!hasNextPage) {
                        logger.warn("No more pages available at page " + page);
                        break;
                    }
                } else {
                    logger.error("Reached maximum page limit (" + maxPages + ") without finding hotel");
                }

            } catch (Exception e) {
                logger.error("Error while searching hotels on page " + page, e);
            }
        }

        if (!hotelFound) {
            logger.error("Hotel not found: " + hotelName);
            throw new RuntimeException("Hotel not found: " + hotelName);
        }

        waitFor(1);
        Set<String> allWindows = driver.getWindowHandles();
        if (allWindows.size() > 1) {
            for (String window : allWindows) {
                if (!window.equals(mainWindow)) {
                    driver.switchTo().window(window);
                    logger.info("Switched to hotel details window");
                    break;
                }
            }
        }

        waitForPageLoad();
        waitFor(1);
        logger.info("Hotel selected successfully: " + hotelName);
    }

    /**
     * FAST SEARCH - Loads all page content instantly then searches
     */
    private boolean searchHotel(String hotelName, int pageNumber) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        logger.info("Loading all page content...");
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitFor(1);
        js.executeScript("window.scrollTo(0, 0);");
        waitFor(1);
        List<WebElement> hotelCards = driver.findElements(
                By.xpath("//div[@data-testid='property-card'] | " +
                        "//div[contains(@class, 'property-card')] | " +
                        "//div[contains(@data-testid, 'property')]")
        );

        logger.info("Found " + hotelCards.size() + " total hotels on page " + pageNumber);
        Set<String> checkedHotels = new HashSet<>();

        for (int i = 0; i < hotelCards.size(); i++) {
            WebElement card = hotelCards.get(i);
                String cardText = card.getText();
                String[] lines = cardText.split("\n");
                String hotelTitle = lines.length > 0 ? lines[0].trim() : "";
                if (checkedHotels.contains(hotelTitle)) {
                    continue;
                }
                checkedHotels.add(hotelTitle);
                if (hotelTitle.equalsIgnoreCase(hotelName)) {
                    logger.info("✓✓✓ EXACT MATCH FOUND: " + hotelName + " on page " + pageNumber);
                    scrollToElement(card, "Hotel Card: " + hotelName);
                    waitFor(1);
                    WebElement hotelLink = card.findElement(
                            By.xpath(".//a[@data-testid='title-link'] | " +
                                    ".//a[contains(@href, 'hotel')] | " +
                                    ".//h3/a | .//div[@data-testid='title']/a")
                    );

                    clickUsingJS(hotelLink, "Hotel Link: " + hotelName);
                    return true;
                }
            }


        logger.info("Hotel not found on page " + pageNumber);
        return false;
    }

    /**
     * Go to next page of search results - OPTIMIZED
     */
    public boolean goToNextPage() {
        try {
            scrollToBottom();
            waitFor(1);

            List<WebElement> nextButtons = driver.findElements(
                    By.xpath("//button[@aria-label='Next page' or contains(@aria-label, 'Next')] | " +
                            "//a[@aria-label='Next page' or contains(@aria-label, 'Next')] | " +
                            "//button[contains(@class, 'pagination-next')] | " +
                            "//a[contains(@class, 'pagination-next')]")
            );

            if (nextButtons.isEmpty()) {
                logger.info("Next page button not found - likely last page");
                return false;
            }

            WebElement nextButton = nextButtons.get(0);
            String ariaDisabled = nextButton.getAttribute("aria-disabled");
            String disabled = nextButton.getAttribute("disabled");
            String classes = nextButton.getAttribute("class");

            if ("true".equals(ariaDisabled) || disabled != null ||
                    (classes != null && classes.contains("disabled"))) {
                logger.info("Next page button is disabled - reached last page");
                return false;
            }

            scrollToElement(nextButton, "Next Page Button");
            waitFor(1);
            clickUsingJS(nextButton, "Next Page Button");
            waitForPageLoad();
            handlePopups();

            logger.info("✓ Successfully navigated to next page");
            return true;

        } catch (Exception e) {
            logger.error("Failed to go to next page", e);
            return false;
        }
    }

    /**
     * Scroll to bottom of page
     */
    private void scrollToBottom() {
        try {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
            waitFor(1);
        } catch (Exception e) {
            logger.warn("Failed to scroll to bottom", e);
        }
    }

    /**
     * Verify search results page is loaded
     */
    public boolean isSearchResultsPageLoaded() {
        try {
            handlePopups();
            waitFor(1);
            String url = driver.getCurrentUrl();
            boolean loaded = url.contains("searchresults") || url.contains("search");

            if (!loaded) {
                List<WebElement> hotelCards = driver.findElements(
                        By.xpath("//div[@data-testid='property-card']")
                );
                loaded = !hotelCards.isEmpty();
            }

            return loaded;
        } catch (Exception e) {
            logger.error("Search results page not loaded", e);
            return false;
        }
    }
}