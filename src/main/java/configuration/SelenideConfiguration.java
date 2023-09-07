package configuration;

import static com.codeborne.selenide.Configuration.*;

public class SelenideConfiguration {

    private SelenideConfiguration() {
        throw new IllegalStateException();
    }

    public static void configurationSelenide() {
        headless = false;
        browser = "chrome";
        timeout = 10000;
        pageLoadTimeout = 60000;
    }
}
