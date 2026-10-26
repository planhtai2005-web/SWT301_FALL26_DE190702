
import io.github.bonigarcia.wdm.WebDriverManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.CsvFileSource;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login Tests for the-internet.herokuapp.com")
public class LoginTest {

    static WebDriver driver;
    static WebDriverWait wait;

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        // Chan JavaScript theo cau hinh bai lab
        Map<String, Object> prefs = new HashMap<>();
        prefs.put(
                "profile.managed_default_content_settings.javascript",
                2
        );

        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--incognito");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.manage().window().maximize();
    }

    // TODO 2: Dang nhap thanh cong
    @Test
    @Order(1)
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        driver.get("https://the-internet.herokuapp.com/login");

        driver.findElement(By.id("username")).sendKeys("tomsmith");
        driver.findElement(By.id("password"))
                .sendKeys("SuperSecretPassword!");

        driver.findElement(
                By.cssSelector("button[type='submit']")
        ).click();

        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".flash.success")
                )
        );

        assertTrue(
                successMsg.getText().contains(
                        "You logged into a secure area!"
                ),
                "Khong thay thong bao dang nhap thanh cong"
        );
    }

    // TODO 2: Dang nhap that bai
    @Test
    @Order(2)
    @DisplayName("Should display error with invalid credentials")
    void testLoginFail() {
        driver.get("https://the-internet.herokuapp.com/login");

        driver.findElement(By.id("username")).sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("wrongpassword");

        driver.findElement(
                By.cssSelector("button[type='submit']")
        ).click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector(".flash.error")
                )
        );

        assertTrue(
                errorMsg.getText().contains("Your username is invalid!"),
                "Khong thay thong bao loi username"
        );
    }

    // TODO 3: Parameterized test voi @CsvSource
    @Order(3)
    @ParameterizedTest(
            name = "[{index}] user=''{0}'' pass=''{1}'' -> {2}"
    )
    @CsvSource({
            "tomsmith, SuperSecretPassword!, success",
            "wronguser, SuperSecretPassword!, error",
            "tomsmith, wrongpassword, error",
            "'', '', error"
    })
    @DisplayName("Multiple login attempts using @CsvSource")
    void testLoginWithMultipleParameters(
            String username,
            String password,
            String expectedResult
    ) {
        verifyLogin(username, password, expectedResult);
    }

    // TODO 4: Parameterized test doc du lieu tu file CSV
    @Order(4)
    @ParameterizedTest(
            name = "CSV [{index}]: {0} / {1} -> {2}"
    )
    @CsvFileSource(
            resources = "/login-data.csv",
            numLinesToSkip = 1
    )
    @DisplayName("Login with data from external CSV file")
    void testLoginWithCSV(
            String username,
            String password,
            String expectedResult
    ) {
        verifyLogin(username, password, expectedResult);
    }

    // Ham dung chung cho TODO 3 va TODO 4
    private void verifyLogin(
            String username,
            String password,
            String expectedResult
    ) {
        driver.get("https://the-internet.herokuapp.com/login");

        // Xu ly gia tri null va khoang trang
        username = (username == null) ? "" : username.trim();
        password = (password == null) ? "" : password.trim();

        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);

        driver.findElement(
                By.cssSelector("button[type='submit']")
        ).click();

        By messageLocator = "success".equals(expectedResult)
                ? By.cssSelector(".flash.success")
                : By.cssSelector(".flash.error");

        WebElement message = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        messageLocator
                )
        );

        if ("success".equals(expectedResult)) {
            assertTrue(
                    message.getText().contains(
                            "You logged into a secure area!"
                    ),
                    "Khong thay thong bao dang nhap thanh cong"
            );
        } else {
            assertTrue(
                    message.getText().toLowerCase().contains("invalid"),
                    "Khong thay thong bao loi dang nhap"
            );
        }
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
