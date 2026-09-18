package demo.web;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
@Disabled("The tests is failing on the Linux VM")
class SearchPageTest {

    @Test
    void loadAllOffers() {
        try (WebApplication application = WebApplication.launch()) {
            application.openHomePage()
                    .waitForUseCaseOneTraditionalButtonToBeVisible()
                    .clickOnUseCaseOneTraditionalButton()
                    .assertNumberOfOffersIs(5)
                    .clickOfferByTitle("7-Day Adventure & Extreme Sports - Alps")
                    .assertTitle("7-Day Adventure & Extreme Sports - Alps");
        }
    }
}
