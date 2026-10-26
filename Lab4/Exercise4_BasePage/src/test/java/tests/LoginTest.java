
package tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login Tests using BasePage and BaseTest")
public class LoginTest extends BaseTest {

    private static LoginPage loginPage;

    @BeforeAll
    public static void setUpLoginPage() {
        loginPage = new LoginPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Login successfully with valid credentials")
    void testLoginSuccess() {
        loginPage.navigate();
        loginPage.login("tomsmith", "SuperSecretPassword!");

        String message =
                loginPage.getMessageText(loginPage.getSuccessLocator());

        assertTrue(
                message.contains("You logged into a secure area!"),
                "Khong thay thong bao dang nhap thanh cong"
        );
    }

    @Test
    @Order(2)
    @DisplayName("Login fails with invalid credentials")
    void testLoginFail() {
        loginPage.navigate();
        loginPage.login("wronguser", "wrongpassword");

        String message =
                loginPage.getMessageText(loginPage.getErrorLocator());

        assertTrue(
                message.toLowerCase().contains("invalid"),
                "Khong thay thong bao dang nhap that bai"
        );
    }

    @ParameterizedTest(name = "CSV Source: {0} / {1} -> {2}")
    @Order(3)
    @CsvSource({
            "tomsmith, SuperSecretPassword!, success",
            "wronguser, SuperSecretPassword!, error",
            "tomsmith, wrongpassword, error",
            "'', '', error"
    })
    @DisplayName("Login with multiple credentials")
    void testLoginWithCsvSource(
            String username,
            String password,
            String expected) {

        verifyLogin(username, password, expected);
    }

    @ParameterizedTest(name = "CSV File: {0} / {1} -> {2}")
    @Order(4)
    @CsvFileSource(
            resources = "/login-data.csv",
            numLinesToSkip = 1
    )
    @DisplayName("Login with external CSV file")
    void testLoginFromCSV(
            String username,
            String password,
            String expected) {

        verifyLogin(username, password, expected);
    }

    private void verifyLogin(
            String username,
            String password,
            String expected) {

        loginPage.navigate();

        username = username == null ? "" : username.trim();
        password = password == null ? "" : password.trim();

        loginPage.login(username, password);

        if ("success".equalsIgnoreCase(expected)) {
            String message =
                    loginPage.getMessageText(
                            loginPage.getSuccessLocator()
                    );

            assertTrue(
                    message.contains("You logged into a secure area!"),
                    "Expected successful login"
            );
        } else {
            String message =
                    loginPage.getMessageText(
                            loginPage.getErrorLocator()
                    );

            assertTrue(
                    message.toLowerCase().contains("invalid"),
                    "Expected login error"
            );
        }
    }
}
