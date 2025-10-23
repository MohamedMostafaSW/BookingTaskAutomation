package tests;

import base.BasePage;
import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.BookingHomePage;
import pages.HotelDetailsPage;
import pages.SearchResultsPage;
import utils.ExcelUtil;

import java.time.LocalDate;

public class HotelBooking extends BaseTest {

    private BookingHomePage homePage;
    private SearchResultsPage searchResultsPage;
    private HotelDetailsPage hotelDetailsPage;

    /**
     * Data Provider - Read test data from Excel
     */
    @DataProvider(name = "bookingData")
    public Object[][] getBookingTestData() {
        // Excel should have columns: Location, CheckInDate, CheckOutDate, HotelName
        ExcelUtil.updateDates("BookingTestData.xlsx", "TestData", 1, 2);
        return ExcelUtil.getTestData("BookingTestData.xlsx", "TestData");
    }

    /**
     * Test Case: Book hotel on Booking.com
     * Steps:
     * 1. Open www.booking.com
     * 2. Search for location with check-in and check-out dates
     * 3. Select specific hotel (Tolip Hotel Alexandria)
     * 4. Click See Availability
     * 5. Verify dates are displayed correctly
     * 6. Select room and click I'll reserve button
     */
    @Test(dataProvider = "bookingData")
    public void testHotelBooking(String location, String checkInDate, String checkOutDate, String hotelName) {

        logger.info("========== Starting Hotel Booking Test ==========");
        // Step 1: Initialize pages
        homePage = new BookingHomePage(driver);
        searchResultsPage = new SearchResultsPage(driver);
        hotelDetailsPage = new HotelDetailsPage(driver);

        // Step 2: Navigate to Booking.com
        homePage.navigateToBooking();
        Assert.assertTrue(homePage.isHomePageLoaded(), "Home page not loaded");
        logger.info("✓ Home page loaded successfully");

        // Step 3: Search for hotel
        homePage.searchHotel(location, checkInDate, checkOutDate);
        logger.info("✓ Hotel search completed");

        // Step 4: Verify search results page loaded
        Assert.assertTrue(searchResultsPage.isSearchResultsPageLoaded(), "Search results page not loaded");
        logger.info("✓ Search results page loaded");

        // Step 5: Select hotel from search results
        searchResultsPage.selectHotel(hotelName);
        logger.info("✓ Hotel selected: " + hotelName);

        // Step 6: Verify hotel details page loaded
        Assert.assertTrue(hotelDetailsPage.isHotelDetailsPageLoaded(), "Hotel details page not loaded");
        logger.info("✓ Hotel details page loaded");

        // Step 7: Click See Availability
        hotelDetailsPage.clickSeeAvailability();
        logger.info("✓ Clicked See Availability button");

        // Step 8: Verify dates are displayed correctly (ASSERTION REQUIRED)
        String displayedCheckIn = hotelDetailsPage.getDisplayedCheckInDate();
        String displayedCheckOut = hotelDetailsPage.getDisplayedCheckOutDate();

        String expectedCheckInFormatted = BasePage.formatDateForUI(checkInDate);
        String expectedCheckOutFormatted = BasePage.formatDateForUI(checkOutDate);

        Assert.assertTrue(
                displayedCheckIn.contains(expectedCheckInFormatted.split(",")[1].trim()) &&
                        displayedCheckIn.contains(String.valueOf(LocalDate.parse(checkInDate).getDayOfMonth())),
                "❌ Check-in date mismatch. Expected (formatted): " + expectedCheckInFormatted + ", Got: " + displayedCheckIn);
        Assert.assertTrue(
                displayedCheckOut.contains(expectedCheckOutFormatted.split(",")[1].trim()) &&
                        displayedCheckOut.contains(String.valueOf(LocalDate.parse(checkOutDate).getDayOfMonth())),
                "❌ Check-out date mismatch. Expected (formatted): " + expectedCheckOutFormatted + ", Got: " + displayedCheckOut);

        logger.info("✓ Dates verified successfully");

        // Step 9: Select room and reserve
        hotelDetailsPage.selectRoomAndReserve(0);
        logger.info("✓ Room selected and reserve button clicked");

        logger.info("========== Hotel Booking Test Completed Successfully ==========");

    }
}
