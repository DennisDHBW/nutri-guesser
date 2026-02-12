package service.result;

import client.cataas.CataasClient;
import client.cataas.dto.CataasResponse;
import lombok.Getter;
import model.GameSession;
import model.Round;
import repository.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

@ApplicationScoped
public class ResultService {

    @Inject
    @RestClient
    CataasClient cataasClient;

    @Inject
    @ConfigProperty(name = "quarkus.rest-client.cataas-api.url")
    String cataasBaseUrl;

    @Inject
    GameSessionRepository gameSessionRepository;

    @Inject
    LeaderboardRepository leaderboardRepository;

    @Inject
    RoundRepository roundRepository;

    private static final Logger LOG = Logger.getLogger(ResultService.class.getName());
    private static final int MAX_RETRIES = 2;
    private static final int MAX_ATTEMPTS = MAX_RETRIES + 1;
    private static final long RETRY_DELAY_MS = 250L;
    private static final String FALLBACK_IMAGE_CLASSPATH = "/static/default-cat.jpg";

    private static final float TIER2_THRESHOLD = 25.0f;
    private static final float TIER3_THRESHOLD = 50.0f;
    private static final float TIER4_THRESHOLD = 75.0f;
    private static final float TIER5_THRESHOLD = 90.0f;

    private String[] getTagAndTextForSession(UUID sessionId) {
        long totalPoints = calculateTotalScore(sessionId);
        float percentile = leaderboardRepository.calculatePercentile(totalPoints);
        long placement = leaderboardRepository.getPredictedPlacement(totalPoints);
        ResultTier resultTier = getResultTier(percentile);
        String tag = getResultTagForPercentile(resultTier);
        String text = getCataasTextForPercentile(resultTier);
        text = text + " | placement: " + placement + " (better than " + percentile + "% of players)";
        text = text + "?fontSize=30&fontColor=red";
        return new     String[]{tag, text};
    }

    public CataasResponse fetchCatJsonForSession(UUID sessionId) {
        String[] tagAndText = getTagAndTextForSession(sessionId);
        return fetchCatJsonWithText(tagAndText[0], tagAndText[1]);
    }

    public byte[]  fetchCatImageForSession(UUID sessionId) {
        String[] tagAndText = getTagAndTextForSession(sessionId);
        return fetchCatImageWithText(tagAndText[0], tagAndText[1]);
    }

    // Fetch cat JSON with text
    public CataasResponse fetchCatJsonWithText(String tag, String text) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                CataasResponse dto = cataasClient.getCatByTagAndText(tag, text, true);
                if (dto == null) throw new RuntimeException("Cataas returned null for tag: " + tag + " with text: " + text);
                return normalize(dto);
            } catch (Exception e) {
                String msg = "Failed to fetch JSON with text from Cataas (attempt " + attempt + ") for tag '" + tag + "' and text '" + text + "': " + e.getMessage();
                LOG.warning(msg);
                if (attempt == MAX_RETRIES) {
                    return fallbackCatJson(tag, e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return fallbackCatJson(tag, new RuntimeException("unreachable"));
    }

    // Fetch image bytes for a tag with text. Falls back to default image when remote fetch fails.
    public byte[] fetchCatImageWithText(String tag, String text) {
        CataasResponse dto = fetchCatJsonWithText(tag, text);
        String urlPart = dto.url();
        String fullUrl = buildFullUrl(urlPart);
        if (fullUrl == null || fullUrl.isEmpty()) {
            fullUrl = buildFullUrl("/cat/" + tag + "/says/" + text);
        }

        try (InputStream in = URI.create(fullUrl).toURL().openStream();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (Exception e) {
            String msg = "Failed to fetch image with text from '" + fullUrl + "': " + e.getMessage();
            LOG.warning(msg);
            return loadDefaultImageBytes();
        }
    }

    private String buildFullUrl(String urlPart) {
        if (urlPart == null) return null;
        if (urlPart.startsWith("http://") || urlPart.startsWith("https://")) return urlPart;
        String base = cataasBaseUrl != null ? cataasBaseUrl.replaceAll("/$", "") : "https://cataas.com";
        if (!urlPart.startsWith("/")) urlPart = "/" + urlPart;
        return base + urlPart;
    }

    // Normalize DTO (lowercase tags etc.) — erzeugt neues DTO (record ist immutable)
    private CataasResponse normalize(CataasResponse dto) {
        List<String> normalizedTags = dto.tags() == null ? List.of() : dto.tags().stream().map(t -> t == null ? "" : t.toLowerCase()).toList();
        String createdAt = dto.createdAt() == null ? Instant.now().toString() : dto.createdAt();
        return new CataasResponse(dto.id(), normalizedTags, createdAt, dto.url(), dto.mimetype());
    }

    // Simple fallback DTO when remote calls fail
    public CataasResponse fallbackCatJson(String tag, Throwable t) {
        String msg = "Using fallback DTO for tag '" + tag + "' because: " + (t == null ? "<unknown>" : t.getMessage());
        LOG.warning(msg);
        return new CataasResponse(
                "fallback",
                List.of("fallback"),
                Instant.now().toString(),
                FALLBACK_IMAGE_CLASSPATH,
                "image/jpeg"
        );
    }

    // Load default image bytes from classpath
    private byte[] loadDefaultImageBytes() {
        try (InputStream in = getClass().getResourceAsStream(FALLBACK_IMAGE_CLASSPATH); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (in == null) {
                LOG.warning("Default cat image not found on classpath: " + FALLBACK_IMAGE_CLASSPATH);
                return new byte[0];
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } catch (Exception e) {
            LOG.warning("Failed to load default image: " + e.getMessage());
            return new byte[0];
        }
    }


    @Getter
    enum ResultTier {
        TIER_1(
                Arrays.asList("sleep", "sleeping", "sleepy", "tired", "lazy", "bored", "grumpy", "sad", "nap", "lay", "bed", "blanket"),
                Arrays.asList("Not my best...", "Ouch!", "That hurt", "Did I do that?", "What was that?")
        ),
        TIER_2(
                Arrays.asList("cute", "kitten", "kittens", "baby", "smol", "tiny", "fluffy", "floof", "soft", "flower", "pretty", "lovely", "hug", "basket"),
                Arrays.asList("Pretty good!", "Not bad!", "Getting better!", "Nice try!", "Keep going!")
        ),
        TIER_3(
                Arrays.asList("zoomies", "jump", "jumping", "running", "crazy", "silly", "funny", "tongue", "blep", "derp", "play", "playing", "attack", "surprised", "shocked", "scream"),
                Arrays.asList("Amazing!", "Fantastic!", "You rock!", "Impressive!", "Wow!")
        ),
        TIER_4(
                Arrays.asList("sunglasses", "glasses", "cool", "business", "work", "working", "office", "tie", "tuxedo", "computer", "laptop", "bossy", "rich", "money", "king", "regal"),
                Arrays.asList("Business time", "Let's get serious", "Professional mode", "Show time", "All business")
        ),
        TIER_5(
                Arrays.asList("meme", "space", "trippy", "psychedelic", "bread", "loaf", "burrito", "party", "nyan", "nyancat", "popcorn", "food", "chonker", "fat", "unit", "laser", "alien", "glow"),
                Arrays.asList("LEGENDARY MODE", "ULTIMATE POWER", "UNSTOPPABLE", "ELITE LEVEL", "GODMODE ACTIVATED")
        );

        private final List<String> tags;
        private final List<String> texts;

        ResultTier(List<String> tags, List<String> texts) {
            this.tags = tags;
            this.texts = texts;
        }

    }


    private final Random random = new Random();

    private ResultTier getResultTier(float percentile) {
        if (percentile >= TIER5_THRESHOLD) {
            return ResultTier.TIER_5;
        } else if (percentile >= TIER4_THRESHOLD) {
            return ResultTier.TIER_4;
        } else if (percentile >= TIER3_THRESHOLD) {
            return ResultTier.TIER_3;
        } else if (percentile >= TIER2_THRESHOLD) {
            return ResultTier.TIER_2;
        } else {
            return ResultTier.TIER_1;
        }
    }

    private Integer calculateTotalScore(UUID sessionId) {

        GameSession session = gameSessionRepository.findById(sessionId);
        if (session == null) {
            throw new RuntimeException("ERROR: Game Session not found.");
        }
        List<Round> rounds = roundRepository.listBySession(session);
        return rounds.stream()
                .map(round -> round.points)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private String getResultTagForPercentile(ResultTier resultTier) {
        List<String> tierTags = resultTier.getTags();
        int randomIndex = random.nextInt(tierTags.size());
        return tierTags.get(randomIndex);
    }

    private String getCataasTextForPercentile(ResultTier resultTier) {
        List<String> tierTexts = resultTier.getTexts();
        int randomIndex = random.nextInt(tierTexts.size());
        return tierTexts.get(randomIndex);
    }

}
