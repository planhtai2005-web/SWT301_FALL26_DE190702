
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DynamicLoadingPage extends BasePage {

    private final By startBtn =
            By.cssSelector("#start button");

    private final By finish =
            By.cssSelector("#finish h4");

    public DynamicLoadingPage(WebDriver driver) {
        super(driver);
    }

    public DynamicLoadingPage open(int example) {
        navigateTo(
                utils.ConfigReader.get(
                        "baseUrl",
                        "https://the-internet.herokuapp.com"
                ) + "/dynamic_loading/" + example
        );
        return this;
    }

    public String startAndGetResult() {
        click(startBtn);
        return getText(finish);
    }
}
