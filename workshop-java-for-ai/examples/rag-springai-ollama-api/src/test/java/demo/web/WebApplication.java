package demo.web;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.openqa.selenium.support.ui.ExpectedConditions.textToBePresentInElement;

final class WebApplication implements AutoCloseable {

    private final int port;
    private final WebDriver driver;
    private Process process;

    static WebApplication launch() {
        try {
            final Path application = Path.of("target", "rag-springai-ollama-api-1.0.0.jar");
            if (!Files.isRegularFile(application)) {
                throw new RuntimeException("The application JAR file (" + application + ") is missing. Please make sure to package the application first!");
            }

            final int port = findFreePort();

            final Path log = Path.of("target", "rag-springai-ollama-api-%d.log".formatted(System.currentTimeMillis()));

            final ProcessBuilder builder = new ProcessBuilder("java",
                    "-jar", application.toString(),
                    "--server.port=" + port);
            builder.redirectErrorStream(true);
            builder.redirectOutput(log.toFile());

            final Process process = builder.start();
            waitForPort(port);

            final ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");              // Run in headless mode (no UI)
            options.addArguments("--disable-gpu");           // Recommended for Windows
            options.addArguments("--window-size=1920,1080"); // Set a standard size
            final ChromeDriver driver = new ChromeDriver(options);

            return new WebApplication(port, driver, process);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to launch the application", e);
        }
    }

    private WebApplication(final int port, final WebDriver driver, final Process process) {
        this.port = port;
        this.driver = driver;
        this.process = process;
    }

    HomePage openHomePage() {
        driver.get("http://localhost:%d/".formatted(port));
        return new HomePage(this);
    }

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to find a free port");
        }
    }

    private static void waitForPort(final int port) {
        for (int i = 0; i < 100; i++) {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", port), 200);
                return;
            } catch (final IOException e) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for the port to respond", ie);
                }
            }
        }

        throw new RuntimeException("Failed to start the web application");
    }

    @Override
    public void close() {
        driver.quit();

        if (process != null) {
            try {
                process.destroy();
                try {
                    process.waitFor();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting for the process to exit", e);
                }
            } finally {
                this.process = null;
            }
        }
    }

    record HomePage(WebApplication application) implements WebContainer {
        private static final By USE_CASE_ONE_SEARCH_BUTTON = By.cssSelector("a[id=search-use-case-one-button]");

        public HomePage waitForUseCaseOneTraditionalButtonToBeVisible() {
            waitForElementToBeVisible(() -> findElement(USE_CASE_ONE_SEARCH_BUTTON));
            return this;
        }

        SearchPage clickOnUseCaseOneTraditionalButton() {
            clickOn(USE_CASE_ONE_SEARCH_BUTTON);
            return new SearchPage(application);
        }

        public WebDriver driver() {
            return application.driver;
        }
    }

    record SearchPage(WebApplication application) implements WebContainer {

        OfferPage clickOfferByTitle(final String title) {
            offers()
                    .map(e -> e.findElement(By.cssSelector("a[href]")))
                    .filter(e -> e.getText().trim().equals(title))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException("No offer titled: " + title))
                    .click();

            return new OfferPage(application);
        }

        SearchPage assertNumberOfOffersIs(final int expectedNumberOfOffers) {
            Assertions.assertThat(offers().count())
                    .isEqualTo(expectedNumberOfOffers);
            return this;
        }

        private Stream<WebElement> offers() {
            return new WebDriverWait(driver(), Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("table[data-search-results] > tbody > tr[data-search-result]")))
                    .stream();
        }

        public WebDriver driver() {
            return application.driver;
        }
    }

    record OfferPage(WebApplication application) implements WebContainer {

        OfferPage assertTitle(final String title) {
            assertElementTextContains(findElement(By.cssSelector("h1[data-offer-title]")), title);
            return this;
        }

        public WebDriver driver() {
            return application.driver;
        }
    }

    interface WebContainer {

        default void waitForElementToBeVisible(final Supplier<WebElement> supplier) {
            new WebDriverWait(driver(), Duration.ofSeconds(1))
                    .until((ExpectedCondition<WebElement>) driver -> {
                                try {
                                    final WebElement element = supplier.get();
                                    return element != null && element.isDisplayed()
                                            ? element
                                            : null;
                                } catch (final NoSuchElementException | StaleElementReferenceException e) {
                                    System.out.println("Element not found!");
                                    printPage();
                                    return null;
                                }
                            }
                    );
        }

        default void setInputValue(final By by, final String value) {
            final WebElement element = findElement(by);
            element.clear();
            element.sendKeys(value);
        }

        default void clickOn(final By by) {
            findElement(by).click();
        }

        default void selectOption(final By by, final String text) {
            new Select(findElement(by)).selectByVisibleText(text);
        }

        default void assertVisible(final By by) {
            assertVisible(by, true);
        }

        default void assertVisible(final By by, final boolean visible) {
            if (visible != findElement(by).isDisplayed()) {
                throw new AssertionError("Element '" + by + "' is not visible");
            }
        }

        default void assertElementTextContains(final WebElement element, final String expected) {
            assertThat(textToBePresentInElement(element, expected));
        }

        default void assertThat(final Function<WebDriver, Boolean> isTrue) {
            new WebDriverWait(driver(), Duration.ofSeconds(2)).until(isTrue);
        }

        default WebContainer printPage() {
            System.out.println("---");
            System.out.println(driver().getPageSource());
            System.out.println("---");
            return this;
        }

        default WebElement findElement(final By by) {
            return element().findElement(by);
        }

        default WebElement element() {
            final By cssSelector = By.cssSelector("body");
            return driver().findElement(cssSelector);
        }

        WebDriver driver();
    }
}
