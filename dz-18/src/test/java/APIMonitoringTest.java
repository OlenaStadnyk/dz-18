import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class APIMonitoringTest {

    private static final String API_PING_ENDPOINT = "https://restful-booker.herokuapp.com/ping";

    @Test
    public void testAPIMonitoring() throws InterruptedException {
        // Setting the check interval (10 minutes)
        long checkIntervalMinutes = 10;
        long checkIntervalMillis = TimeUnit.MINUTES.toMillis(checkIntervalMinutes);

        while (true) {
            // Make a request to the API to check the status via ping
            Response response = RestAssured.given()
                    .contentType(ContentType.JSON)
                    .get(API_PING_ENDPOINT);

            // Check the success response status (example: 201 OK)
            if (response.getStatusCode() == 201) {
                System.out.println("API is accessible.");
                break;
            } else {
                System.out.println("API is not accessible yet. Retrying in 10 minutes.");
                Thread.sleep(checkIntervalMillis);
            }
        }
    }
}
