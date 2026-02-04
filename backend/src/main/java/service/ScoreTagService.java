package service;

import jakarta.inject.Singleton;

@Singleton
public class ScoreTagService {

    /*
        1. Ermitteln:
            - Gesamtpunktzahl
            - Prozentuale Fehlerquote (wie viele Fragen haben 0 Punkte)
            - Wie groß wurde der Bereich in Prozent gewaehlt dann Faktor ermitteln (=Risikolevel)

     */


    public String determineTag(int score) {
        if (score < 10) {
            return "kitten";
        }
        if (score < 50) {
            return "cute";
        }
        if (score < 100) {
            return "funny";
        }
        return "meme";
    }
}
