package demo.chat.spring;

import demo.catalogue.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public final class OfferToDocumentsConverter implements DocumentConvertor<OfferEntity> {

    // Keep chunks ~300 tokens with a small overlap for context continuity.
    private final TokenTextSplitter splitter = TokenTextSplitter.builder()
            .withChunkSize(350)
            .withMinChunkLengthToEmbed(80)   // optional: skip tiny fragments
            .withMaxNumChunks(10_000)        // optional safety cap
            .withKeepSeparator(false)
            .build();

    @Override
    public Stream<Document> apply(final OfferEntity offer) {
        final Map<String, Object> metadata = Map.of(
                "slug", offer.getSlug(),
                "title", offer.getTitle(),
                "subtitle", offer.getSubtitle(),
                "path", "/offer/" + offer.getSlug());

        return Stream.<Function<OfferEntity, Stream<String>>>of(OfferToDocumentsConverter::buildOverview,
                        OfferToDocumentsConverter::buildHighlights,
                        OfferToDocumentsConverter::buildPackageDetails,
                        OfferToDocumentsConverter::buildSeasons,
                        OfferToDocumentsConverter::buildItinerary)
                .flatMap(a -> a.apply(offer))
                .flatMap(chunk -> splitWithMeta(chunk, metadata));
    }

    private static Stream<String> buildOverview(final OfferEntity offer) {
        return Stream.of("Offer: " + offer.getTitle() + '\n' +
                         offer.getSubtitle() + "\n\n" +
                         offer.getDescription());
    }

    private static Stream<String> buildHighlights(final OfferEntity offer) {
        if (isNullOrEmpty(offer.getHighlights())) {
            return Stream.empty();
        }

        final StringBuilder buffer = new StringBuilder();
        buffer.append("Offer: ").append(offer.getTitle()).append(" — Highlights:");
        for (final TripHighlightEntity entity : offer.getHighlights()) {
            buffer.append("\n• ").append(entity.getHighlight());
        }

        return Stream.of(buffer.toString());
    }

    private static Stream<String> buildPackageDetails(final OfferEntity offer) {
        if (isNullOrEmpty(offer.getPackageDetails())) {
            return Stream.empty();
        }

        final StringBuilder buffer = new StringBuilder();
        buffer.append("Offer: ").append(offer.getTitle()).append(" — What’s Included\n");
        buffer.append("Pricing: ").append(offer.getPrice()).append(" — ").append(offer.getPriceDescription()).append("\n\n");
        buffer.append("Included in the package:");
        for (final PackageDetailEntity entity : offer.getPackageDetails()) {
            buffer.append("\n• ").append(entity.getDescription());
        }

        return Stream.of(buffer.toString());
    }

    private static Stream<String> buildSeasons(final OfferEntity offer) {
        if (isNullOrEmpty(offer.getBestTravelSeasons())) {
            return Stream.empty();
        }

        final StringBuilder buffer = new StringBuilder();
        buffer.append("Offer: ").append(offer.getTitle()).append(" — Best Travel Season");
        for (final BestTravelSeasonEntity entity : offer.getBestTravelSeasons()) {
            buffer.append("\n• ").append(entity.getSeason());
            if (hasText(entity.getDescription())) {
                buffer.append(" — ").append(entity.getDescription());
            }
        }

        return Stream.of(buffer.toString());
    }

    private static Stream<String> buildItinerary(final OfferEntity offer) {
        return offer.getItinerary().stream()
                .flatMap(e -> buildItineraryDay(offer, e));
    }

    private static Stream<String> buildItineraryDay(final OfferEntity offer, final ItineraryEntity day) {
        if (isNullOrEmpty(day.getEntries())) {
            return Stream.empty();
        }

        final StringBuilder buffer = new StringBuilder();
        buffer.append("Offer: ").append(offer.getTitle()).append(" — Itinerary ").append(day.getTitle());
        for (final ItineraryEntryEntity entity : day.getEntries()) {
            buffer.append("\n• ").append(entity.getDescription());
        }

        return Stream.of(buffer.toString());
    }

    private Stream<Document> splitWithMeta(final String text, final Map<String, Object> metadata) {
        return Stream.of(new Document(text, metadata))
                .map(List::of)
                .map(splitter)
                .flatMap(List::stream);
    }

    private static boolean hasText(final String string) {
        return !(string == null || string.isBlank());
    }

    private static boolean isNullOrEmpty(final Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
}
