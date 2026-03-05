package service.result;

import client.cataas.CataasClient;
import client.cataas.dto.CataasResponse;
import api.result.dto.CatImageDTO;
import lombok.Getter;
import model.GameSession;
import model.LeaderboardEntry;
import model.Round;
import repository.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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
    private static final String FALLBACK_IMAGE_CLASSPATH = "/static/default-cat.svg";

    private static final int CATAAS_FONT_SIZE = 50;
    private static final String CATAAS_FONT_COLOR = "red";
    private static final int CATAAS_IMAGE_WIDTH = 800;
    private static final int CATAAS_IMAGE_HEIGHT = 800;

    private static final float TIER2_THRESHOLD = 25.0f;
    private static final float TIER3_THRESHOLD = 50.0f;
    private static final float TIER4_THRESHOLD = 75.0f;
    private static final float TIER5_THRESHOLD = 90.0f;

    @Transactional
    void persistGameResult(GameSession session, Integer totalScore) {
        if (session.endedAt == null) {
            session.endedAt = LocalDateTime.now();
        }

        if (totalScore > 0 && leaderboardRepository.findBySession(session) == null) {
            int placement = leaderboardRepository.getPredictedPlacement(totalScore.longValue());

            LeaderboardEntry entry = new LeaderboardEntry();
            entry.entryId = UUID.randomUUID();
            entry.session = session;
            entry.score = totalScore;
            entry.achievedAt = LocalDateTime.now();
            entry.rank = placement;
            entry.player = session.player;
            leaderboardRepository.persist(entry);
        }
    }

    private String[] getTagAndTextForSession(Integer totalScore) {
        float percentile = leaderboardRepository.calculatePercentile(totalScore.longValue());
        ResultTier resultTier = getResultTier(percentile);
        String tag = getCataasTagForPercentile(resultTier);
        String text = getCataasTextForPercentile(resultTier);
        return new String[]{tag, text};
    }

    public ResultSummary fetchResultResponseForSession(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID must be provided");
        }

        ResultData resultData = persistAndFetchResultData(sessionId);
        CataasResponse catResponse = fetchCatJsonWithText(resultData.tag, resultData.text);

        return new ResultSummary(
                catResponse.id(),
                catResponse.tags(),
                catResponse.createdAt(),
                catResponse.url(),
                catResponse.mimetype(),
                resultData.rank,
                resultData.totalScore,
                resultData.betterThanPercentage
        );
    }

    @Transactional
    ResultData persistAndFetchResultData(UUID sessionId) {
        GameSession session = requireSession(sessionId);
        Integer totalScore = calculateTotalScore(session);

        persistGameResult(session, totalScore);

        String[] tagAndText = getTagAndTextForSession(totalScore);

        LeaderboardEntry entry = leaderboardRepository.findBySession(session);
        Integer rank = entry != null ? entry.rank : null;
        Float betterThanPercentage = leaderboardRepository.calculatePercentile(totalScore.longValue());

        return new ResultData(tagAndText[0], tagAndText[1], rank, totalScore, betterThanPercentage);
    }

    private record ResultData(
            String tag,
            String text,
            Integer rank,
            Integer totalScore,
            Float betterThanPercentage
    ) {}

    public CatImageDTO fetchCatImageForSession(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID must be provided");
        }

        GameSession session = requireSession(sessionId);
        Integer totalScore = calculateTotalScore(session);
        String[] tagAndText = getTagAndTextForSession(totalScore);
        return fetchCatImageWithText(tagAndText[0], tagAndText[1]);
    }

    public CataasResponse fetchCatJsonForSession(UUID sessionId) {
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID must be provided");
        }

        GameSession session = requireSession(sessionId);
        Integer totalScore = calculateTotalScore(session);
        String[] tagAndText = getTagAndTextForSession(totalScore);
        return fetchCatJsonWithText(tagAndText[0], tagAndText[1]);
    }

    public CataasResponse fetchCatJsonWithText(String tag, String text) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                CataasResponse dto = cataasClient.getCatByTagAndText(
                        tag,
                        text,
                        true,
                        CATAAS_FONT_SIZE,
                        CATAAS_FONT_COLOR,
                        CATAAS_IMAGE_WIDTH,
                        CATAAS_IMAGE_HEIGHT);
                if (dto == null) throw new RuntimeException("Cataas returned null for tag: " + tag + " with text: " + text);
                return normalize(dto);
            } catch (Exception e) {
                String msg = "Failed to fetch JSON with text from Cataas (attempt " + attempt + ") for tag '" + tag + "' and text '" + text + "': " + e.getMessage();
                LOG.warning(msg);
                if (attempt == MAX_ATTEMPTS) {
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

    public CatImageDTO fetchCatImageWithText(String tag, String text) {
        CataasResponse dto = fetchCatJsonWithText(tag, text);
        byte[] data = fetchCatImageForResponse(tag, text, dto);
        return new CatImageDTO(data, resolveMimeType(dto));
    }

    private String resolveMimeType(CataasResponse dto) {
        if (dto != null && dto.mimetype() != null && dto.mimetype().startsWith("image/")) {
            return dto.mimetype();
        }
        return "image/jpeg";
    }

    private byte[] fetchCatImageForResponse(String tag, String text, CataasResponse dto) {
        String urlPart = dto != null ? dto.url() : null;
        String fullUrl = buildFullUrl(urlPart);
        if (fullUrl == null || fullUrl.isEmpty()) {
            String safeText = encodePathSegment(text);
            fullUrl = buildFullUrl("/cat/" + tag + "/says/" + safeText);
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

    private String encodePathSegment(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private String buildFullUrl(String urlPart) {
        if (urlPart == null) return null;
        if (urlPart.startsWith("http://") || urlPart.startsWith("https://")) return urlPart;
        String base = cataasBaseUrl != null ? cataasBaseUrl.replaceAll("/$", "") : "https://cataas.com";
        if (!urlPart.startsWith("/")) urlPart = "/" + urlPart;
        return base + urlPart;
    }

    private CataasResponse normalize(CataasResponse dto) {
        List<String> normalizedTags = dto.tags() == null ? List.of() : dto.tags().stream().map(t -> t == null ? "" : t.toLowerCase()).toList();
        String createdAt = dto.createdAt() == null ? Instant.now().toString() : dto.createdAt();
        return new CataasResponse(dto.id(), normalizedTags, createdAt, dto.url(), dto.mimetype());
    }

    public CataasResponse fallbackCatJson(String tag, Throwable t) {
        String msg = "Using fallback DTO for tag '" + tag + "' because: " + (t == null ? "<unknown>" : t.getMessage());
        LOG.warning(msg);
        return new CataasResponse(
                "fallback",
                List.of("fallback"),
                Instant.now().toString(),
                FALLBACK_IMAGE_CLASSPATH,
                "image/svg+xml"
        );
    }

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
                List.of("Sleep", "Sleeping", "Sleepy", "tired", "lazy", "bored", "Grumpy", "Sad", "nap", "Lay", "Bed", "blanket"),
                List.of("Not my best...", "Ouch!", "That hurt", "Did I do that?", "What was that?")
        ),
        TIER_2(
                List.of("Cute", "Kitten", "kittens", "Baby", "Smol", "tiny", "Fluffy", "floof", "soft", "flower", "pretty", "lovely", "hug", "basket"),
                List.of("Pretty good!", "Not bad!", "Getting better!", "Nice try!", "Keep going!")
        ),
        TIER_3(
                List.of("Zoomies", "jump", "jumping", "running", "crazy", "Silly", "Funny", "tongue", "blep", "derp", "play", "playing", "attack", "surprised", "shocked", "scream"),
                List.of("Amazing!", "Fantastic!", "You rock!", "Impressive!", "Wow!")
        ),
        TIER_4(
                List.of("sunglasses", "cool", "business", "work", "working", "office", "tie", "Tuxedo", "computer", "laptop", "bossy", "rich", "money", "regal"),
                List.of("Business time", "Let's get serious", "Professional mode", "Show time", "All business")
        ),
        TIER_5(
                List.of("Meme", "space", "Trippy", "psychedelic", "bread", "loaf", "burrito", "party", "nyan", "nyancat", "food", "chonker", "FAT", "unit", "alien", "glow"),
                List.of("LEGENDARY MODE", "ULTIMATE POWER", "UNSTOPPABLE", "ELITE LEVEL", "GODMODE ACTIVATED")
        );

        private final List<String> tags;
        private final List<String> texts;

        ResultTier(List<String> tags, List<String> texts) {
            this.tags = tags;
            this.texts = texts;
        }

    }



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

    private Integer calculateTotalScore(GameSession session) {
        List<Round> rounds = roundRepository.listBySession(session);
        return rounds.stream()
                .map(round -> round.points)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    private GameSession requireSession(UUID sessionId) {
        GameSession session = gameSessionRepository.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Game session not found.");
        }
        return session;
    }

    private String getCataasTagForPercentile(ResultTier resultTier) {
        List<String> tierTags = resultTier.getTags();
        int randomIndex = ThreadLocalRandom.current().nextInt(tierTags.size());
        return tierTags.get(randomIndex);
    }

    private String getCataasTextForPercentile(ResultTier resultTier) {
        List<String> tierTexts = resultTier.getTexts();
        int randomIndex = ThreadLocalRandom.current().nextInt(tierTexts.size());
        return tierTexts.get(randomIndex);
    }

}
