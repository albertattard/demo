package demo.catalogue;

import java.math.BigDecimal;

public interface OfferSummary {
    String getSlug();

    String getTitle();

    String getSubtitle();

    BigDecimal getPrice();
}
