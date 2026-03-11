package service.result;

import client.cataas.CataasClient;
import client.cataas.dto.CataasResponse;
import lombok.Getter;
import service.result.dto.ResultResponseDTO;
import model.GameSession;
import model.LeaderboardEntry;
import model.Round;
import repository.*;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
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
    GameSessionRepository gameSessionRepository;

    @Inject
    LeaderboardRepository leaderboardRepository;

    @Inject
    RoundRepository roundRepository;

    private static final Logger LOG = Logger.getLogger(ResultService.class.getName());
    private static final int MAX_RETRIES = 2;
    private static final int MAX_ATTEMPTS = MAX_RETRIES + 1;
    private static final long RETRY_DELAY_MS = 250L;
    private static final String FALLBACK_IMAGE_CLASSPATH = "/frontend/src/static/default-cat.svg";

    private static final int CATAAS_FONT_SIZE = 50;
    private static final String CATAAS_FONT_COLOR = "red";
    private static final int CATAAS_IMAGE_WIDTH = 800;
    private static final int CATAAS_IMAGE_HEIGHT = 800;

    private static final float TIER2_THRESHOLD = 25.0f;
    private static final float TIER3_THRESHOLD = 50.0f;
    private static final float TIER4_THRESHOLD = 75.0f;
    private static final float TIER5_THRESHOLD = 90.0f;

    private LeaderboardEntry persistGameResult(GameSession session, Integer totalScore) {
        if (session.endedAt == null) {
            session.endedAt = LocalDateTime.now();
        }

        session.totalScore = totalScore;

        LeaderboardEntry existingEntry = leaderboardRepository.findBySession(session);
        if (totalScore > 0 && existingEntry == null) {
            int placement = leaderboardRepository.getPredictedPlacement(totalScore.longValue());

            LeaderboardEntry entry = new LeaderboardEntry();
            entry.entryId = UUID.randomUUID();
            entry.session = session;
            entry.score = totalScore;
            entry.achievedAt = LocalDateTime.now();
            entry.rank = placement;
            entry.player = session.player;
            leaderboardRepository.persist(entry);
            return entry;
        }
        return existingEntry;
    }

    private ResultTierSelection getTagAndTextForPercentile(float percentile) {
        ResultTier resultTier = getResultTier(percentile);
        List<String> shuffledTags = new ArrayList<>(resultTier.getTags());
        Collections.shuffle(shuffledTags);
        String tag = shuffledTags.getFirst();
        String text = getRandomElement(resultTier.getTexts());
        return new ResultTierSelection(tag, text, shuffledTags);
    }

    private record ResultTierSelection(String tag, String text, List<String> alternativeTags) {}

    public ResultResponseDTO fetchResultResponseForSession(UUID sessionId) {
        ResultData resultData = persistAndFetchResultData(sessionId);
        CataasResponse catResponse = fetchCatJsonWithText(resultData.tag, resultData.text, resultData.alternativeTags);

        return new ResultResponseDTO(
                catResponse,
                resultData.rank,
                resultData.totalScore,
                resultData.betterThanPercentage
        );
    }

    @Transactional
    ResultData persistAndFetchResultData(UUID sessionId) {
        GameSession session = requireSession(sessionId);
        Integer totalScore = calculateTotalScore(session);

        LeaderboardEntry entry = persistGameResult(session, totalScore);

        Float betterThanPercentage = leaderboardRepository.calculatePercentile(totalScore.longValue());
        ResultTierSelection selection = getTagAndTextForPercentile(betterThanPercentage);

        Integer rank = entry != null ? entry.rank : null;

        return new ResultData(selection.tag, selection.text, selection.alternativeTags, rank, totalScore, betterThanPercentage);
    }

    private record ResultData(
            String tag,
            String text,
            List<String> alternativeTags,
            Integer rank,
            Integer totalScore,
            Float betterThanPercentage
    ) {}

    CataasResponse fetchCatJsonWithText(String tag, String text, List<String> alternativeTags) {
        List<String> tagsToTry = new ArrayList<>();
        tagsToTry.add(tag);
        for (String alt : alternativeTags) {
            if (!alt.equals(tag) && !tagsToTry.contains(alt)) {
                tagsToTry.add(alt);
            }
            if (tagsToTry.size() >= MAX_ATTEMPTS) break;
        }

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String currentTag = tagsToTry.get(Math.min(attempt - 1, tagsToTry.size() - 1));
            try {
                CataasResponse dto = cataasClient.getCatByTagAndText(
                        currentTag,
                        text,
                        true,
                        CATAAS_FONT_SIZE,
                        CATAAS_FONT_COLOR,
                        CATAAS_IMAGE_WIDTH,
                        CATAAS_IMAGE_HEIGHT);
                if (dto == null) throw new RuntimeException("Cataas returned null for tag: " + currentTag + " with text: " + text);
                return normalize(dto);
            } catch (Exception e) {
                lastException = e;
                LOG.warning("Failed to fetch JSON with text from Cataas (attempt " + attempt + ") for tag '" + currentTag + "' and text '" + text + "': " + e.getMessage());
                if (attempt < MAX_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        return fallbackCatJson(tag, lastException);
    }

    private CataasResponse normalize(CataasResponse dto) {
        List<String> normalizedTags = dto.tags() == null ? List.of() : dto.tags().stream().map(t -> t == null ? "" : t.toLowerCase()).toList();
        String createdAt = dto.createdAt() == null ? Instant.now().toString() : dto.createdAt();
        return new CataasResponse(dto.id(), normalizedTags, createdAt, dto.url(), dto.mimetype());
    }

    private CataasResponse fallbackCatJson(String tag, Throwable t) {
        LOG.warning("Using fallback DTO for tag '" + tag + "' because: " + (t == null ? "<unknown>" : t.getMessage()));
        return new CataasResponse(
                "fallback",
                List.of("fallback"),
                Instant.now().toString(),
                FALLBACK_IMAGE_CLASSPATH,
                "image/svg+xml"
        );
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
        if (sessionId == null) {
            throw new IllegalArgumentException("Session ID must be provided");
        }
        GameSession session = gameSessionRepository.findById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Game session not found.");
        }
        return session;
    }

    private String getRandomElement(List<String> list) {
        int randomIndex = ThreadLocalRandom.current().nextInt(list.size());
        return list.get(randomIndex);
    }

}
