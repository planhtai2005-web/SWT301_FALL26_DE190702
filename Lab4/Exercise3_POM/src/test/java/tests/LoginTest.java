
package tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import pages.LoginPage;
import utils.DriverFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoginTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private LoginPage loginPage;

    @BeforeAll
    void setUp() {
        driver = DriverFactory.createDriver();
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        loginPage = new LoginPage(driver);
    }

    @AfterAll
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    void testLoginSuccess() {
        loginPage.navigate();
        loginPage.login("tomsmith", "SuperSecretPassword!");

        By successLocator = loginPage.getSuccessLocator();

        wait.until(d -> d.findElement(successLocator).isDisplayed());

        assertTrue(driver.findElement(successLocator).isDisplayed());
    }

    @Test
    @Order(2)
    void testLoginFail() {
        loginPage.navigate();
        loginPage.login("wronguser", "wrongpassword");

        By errorLocator = loginPage.getErrorLocator();

        wait.until(d -> d.findElement(errorLocator).isDisplayed());

        assertTrue(driver.findElement(errorLocator).isDisplayed());
    }

    @ParameterizedTest
    @Order(3)
    @CsvSource({
            "tomsmith, SuperSecretPassword!, success",
            "wronguser, wrongpassword, error",
            "tomsmith, wrongpassword, error",
            "'', '', error"
    })
    void testLoginWithCsvSource(
            String username,
            String password,
            String expectedResult) {

        verifyLogin(username, password, expectedResult);
    }

    @ParameterizedTest
    @Order(4)
    @CsvFileSource(
            resources = "/login-data.csv",
            numLinesToSkip = 1
    )
    void testLoginWithCsvFile(
            String username,
            String password,
            String expectedResult) {

        verifyLogin(username, password, expectedResult);
    }

    private void verifyLogin(
            String username,
            String password,
            String expectedResult) {

        loginPage.navigate();

        if (username != null && !username.isEmpty()) {
            loginPage.login(username, password == null ? "" : password);
        } else {
            loginPage.login("", password == null ? "" : password);
        }

        if ("success".equalsIgnoreCase(expectedResult)) {
            By successLocator = loginPage.getSuccessLocator();

            wait.until(d -> d.findElement(successLocator).isDisplayed());

            assertTrue(
                    driver.findElement(successLocator).isDisplayed(),
                    "Expected successful login"
            );
        } else {
            By errorLocator = loginPage.getErrorLocator();

            wait.until(d -> d.findElement(errorLocator).isDisplayed());

            assertTrue(
                    driver.findElement(errorLocator).isDisplayed(),
                    "Expected login error"
            );
        }
    }
}
