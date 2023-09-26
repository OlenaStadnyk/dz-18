import io.restassured.parsing.Parser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static java.sql.Date.valueOf;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingServiceTest {

    public static final String URL = "https://restful-booker.herokuapp.com/";
    private String authToken; // A variable to store the token


    @BeforeClass
    public void setUp() {
        RestAssured.baseURI = URL;
        RestAssured.defaultParser = Parser.JSON;
        // Data for auth
        String username = "admin";
        String password = "password123";

        // try to log in (POST request)
        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .post("auth");

        // Verify the response status
        assertEquals(response.getStatusCode(), 200);

        // get token from response
        authToken = response.jsonPath().getString("token");
        System.out.println("Token: " + authToken);
    }
    @Test
    public void testGetAllBookings() {
        // Get information about booking books (GET /booking)
        // Receiving a list of reservations with the mandatory header "Accept" and checking the result
        Response response = given()
                .header("Accept", "application/json")
                .get("booking");
        // Check response status and other result checks
        assertEquals(response.getStatusCode(), 200);


        // Checking the content type of the response
        assertEquals(response.getContentType(), "application/json; charset=utf-8");
        System.out.println("API Response Body: " + response.getBody().asString());
        // Parsing the response content into an object (assume you have a Booking model)
        List<BookingTest> bookings = response.jsonPath().getList("$", BookingTest.class);
        // Checking if the bookings were read successfully
        assertNotNull(bookings);
        assertFalse(bookings.isEmpty(), "List of bookings is empty");
    }
    @Test(priority = 1)
    public void testCreateBooking() {
        // Modify LocalDate to java.sql.Date
        LocalDate localDate = LocalDate.parse("2023-09-26", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        java.sql.Date date = java.sql.Date.valueOf(localDate);

        // Create the BookingTest object and set the date values
        BookingTest bookingData = new BookingTest();
        bookingData.setFirstname("Rachel");
        bookingData.setLastname("Green");
        bookingData.setTotalprice(111);
        bookingData.setDepositpaid(true);
        bookingData.setCheckin(date);
        bookingData.setCheckout(date);
        bookingData.setAdditionalneeds("Breakfast");

        // Creating a reservation with the mandatory header "Accept" and checking the result
        Response response = given()
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .body(bookingData) // pass the reservation object as a JSON request
                .post("booking");

        // Check response status
        assertEquals(response.getStatusCode(), 200, "HTTP Status should be 200");

        // Print out server response
        System.out.println("Response Body: " + response.getBody().asString());
        // Obtaining and displaying on the console the ID of the new reservation
        int bookingId = response.jsonPath().getInt("bookingid");
        System.out.println("New Booking ID: " + bookingId);
    }
    @Test(priority = 2)
    public void testUpdateBookingPrice() {
        // Get a list of all bookings
        Response getAllBookingsResponse = given()
                .header("Accept", "application/json")
                .get("booking");

        // Check response status
        assertEquals(getAllBookingsResponse.getStatusCode(), 200);

        // Get a list of booking IDs from the response
        List<Integer> bookingIds = getAllBookingsResponse.jsonPath().getList("bookingid");

        // Select one of the reservation IDs from the list (for example, the first ID)
        if (!bookingIds.isEmpty()) {
            int bookingIdToUpdate = bookingIds.get(0);

            // Get the current price of the booking before updating
            Response getBookingResponse = given()
                    .header("Accept", "application/json")
                    .get("booking/" + bookingIdToUpdate);

            // Check response status
            assertEquals(getBookingResponse.getStatusCode(), 200);

            // Get the current price from the response
            int currentPrice = getBookingResponse.jsonPath().getInt("totalprice");

            // Prepare data to update the booking price (in this case, it can be a JSON object)
            Map<String, Object> updateData = new HashMap<>();
            int newPrice = 555; // this is the new reservation price
            updateData.put("totalprice", newPrice); // Update the totalprice field

            // Execute a PATCH request to update the reservation price
            Response updateBookingResponse = given()
                    .contentType(ContentType.JSON)
                    .body(updateData)
                    .patch("booking/" + bookingIdToUpdate);

            // Checking the status of the update response
            assertEquals(updateBookingResponse.getStatusCode(), 200);
            // Get the updated price of the booking
            Response getUpdatedBookingResponse = given()
                    .header("Accept", "application/json")
                    .get("booking/" + bookingIdToUpdate);

            // Check response status
            assertEquals(getUpdatedBookingResponse.getStatusCode(), 200);

            // Get the updated price from the response
            int updatedPrice = getUpdatedBookingResponse.jsonPath().getInt("totalprice");

            // Check if the price was updated correctly
            assertEquals(updatedPrice, newPrice, "Booking price should be updated correctly.");
        } else {
            System.out.println("No bookings available to update.");
        }
    }
    @Test(priority = 3)
    public void testUpdateBookingDetails() {
        // Select another id from the ones obtained in point 2 and change the name and additionalneeds or any other parameter (PUT)

        // Get a list of all bookings
        Response getAllBookingsResponse = given()
                .header("Accept", "application/json")
                .get("booking");

        // Check the response status
        assertEquals(getAllBookingsResponse.getStatusCode(), 200);
        // Get a list of booking IDs from the response
        List<Integer> bookingIds = getAllBookingsResponse.jsonPath().getList("bookingid");

        // Select a different booking ID from the list (for example, a second ID)
        if (bookingIds.size() > 1) {
            int bookingIdToUpdate = bookingIds.get(1); // Get another ID from the list

            // Prepare the data to update the reservation data (in this case, it can be a JSON object)
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("firstname", "Nicole"); // Updated name
            updateData.put("additionalneeds", "Updated Needs:Sweet tea"); // Updated additional needs


            // Execute a PUT request to update the reservation data
            Response updateBookingResponse = given()
                    .contentType(ContentType.JSON)
                    .header("Accept", "application/json")
                    .body(updateData)
                    .put("booking/" + bookingIdToUpdate);

            // Checking the status of the update response
            assertEquals(updateBookingResponse.getStatusCode(), 200);
            // Get the updated booking details
            Response getUpdatedBookingResponse = given()
                    .header("Accept", "application/json")
                    .get("booking/" + bookingIdToUpdate);

            // Check response status
            assertEquals(getUpdatedBookingResponse.getStatusCode(), 200);

            // Get the updated name from the response
            String updatedName = getUpdatedBookingResponse.jsonPath().getString("firstname");

            // Get the updated additional needs from the response
            String updatedAdditionalNeeds = getUpdatedBookingResponse.jsonPath().getString("additionalneeds");

            // Check if the name was updated correctly
            assertEquals(updatedName, "Nicole", "Booking name should be updated correctly.");

            // Check if the additional needs were updated correctly
            assertEquals(updatedAdditionalNeeds, "Updated Needs:Sweet tea", "Booking additional needs should be updated correctly.");
        } else {
            System.out.println("Not enough bookings available to update.");
        }
    }
    @Test(priority = 4)
    public void testDeleteBooking() {
        // Get a list of all bookings
        Response getAllBookingsResponse = given()
                .header("Accept", "application/json")
                .get("booking");

        // Checking the response status
        assertEquals(getAllBookingsResponse.getStatusCode(), 200);

        // Get a list of booking IDs from the response
        List<Integer> bookingIds = getAllBookingsResponse.jsonPath().getList("bookingid");

        // Select one booking ID from the list (if available)
        if (!bookingIds.isEmpty()) {
            int bookingIdToDelete = bookingIds.get(0); // Get the first ID from the list

            // Execute a DELETE request to delete a reservation
            Response deleteBookingResponse = given()
                    .header("Accept", "application/json")
                    .delete("booking/" + bookingIdToDelete);

            // Checking the status of the successful removal response
            assertEquals(deleteBookingResponse.getStatusCode(), 204);
        } else {
            System.out.println("No bookings available to delete.");
        }
    }

}
