package service;

import jakarta.inject.Singleton;
import java.util.Random;
import java.util.Arrays;
import java.util.List;

@Singleton
public class ScoreTagService {

    int TIER_1_THRESHOLD = 500;
    int TIER_2_THRESHOLD = 1500;
    int TIER_3_THRESHOLD = 3000;
    int TIER_4_THRESHOLD = 5000;

    // Enum with Score-Tiers: Tags and  texts for each tier
    enum ScoreTier {
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

        ScoreTier(List<String> tags, List<String> texts) {
            this.tags = tags;
            this.texts = texts;
        }

        public List<String> getTags() {
            return tags;
        }

        public List<String> getTexts() {
            return texts;
        }
    }

    private final Random random = new Random();


    private ScoreTier getScoreTier(int score) {
        if (score >= TIER_4_THRESHOLD) {
            return ScoreTier.TIER_5;
        } else if (score >= TIER_3_THRESHOLD) {
            return ScoreTier.TIER_4;
        } else if (score >= TIER_2_THRESHOLD) {
            return ScoreTier.TIER_3;
        } else if (score >= TIER_1_THRESHOLD) {
            return ScoreTier.TIER_2;
        } else {
            return ScoreTier.TIER_1;
        }
    }

    public String determineTag(int score) {
        ScoreTier tier = getScoreTier(score);
        List<String> tierTags = tier.getTags();
        int randomIndex = random.nextInt(tierTags.size());
        return tierTags.get(randomIndex);
    }

    public String determineText(int score) {
        ScoreTier tier = getScoreTier(score);
        List<String> tierTexts = tier.getTexts();
        int randomIndex = random.nextInt(tierTexts.size());
        return tierTexts.get(randomIndex);
    }
}
