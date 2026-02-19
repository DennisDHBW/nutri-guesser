import { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import { api } from '../services/api';
import CalorieSlider from '../components/CalorieSlider';
import RoundHistory from '../components/RoundHistory';
import '../styles/GamePage.css';

function GamePage() {
  const { sessionId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  // Initialdaten kommen aus dem Navigation-State (von der Startseite)
  const [roundData, setRoundData] = useState(location.state?.currentRoundData || null);

  const [minCalories, setMinCalories] = useState(0);
  const [maxCalories, setMaxCalories] = useState(1000);
  const [loading, setLoading] = useState(!roundData);
  const [submitting, setSubmitting] = useState(false);
  const [showResult, setShowResult] = useState(false); // Zeigt Ergebnis nach dem Schätzen
  const [lastScore, setLastScore] = useState(null);

  const [rounds, setRounds] = useState([]);
  const [currentRoundNumber, setCurrentRoundNumber] = useState(1);
  const [error, setError] = useState('');

  // Falls die Seite neu geladen wird und der State weg ist:
  useEffect(() => {
    if (!roundData) {
      setError("Sitzung ungültig. Bitte starte das Spiel von vorne.");
    }
  }, [roundData]);

  const handleSubmitGuess = async () => {
    setSubmitting(true);
    setError('');

    try {
      // Neuer Request Record: int guessed_Min, int guessed_Max, UUID roundId, String barcode
      const scoreRequest = {
        guessed_Min: Math.round(minCalories),
        guessed_Max: Math.round(maxCalories),
        roundId: roundData.roundId,
        barcode: roundData.barcode
      };

      const result = await api.submitGuess(scoreRequest);
      // result enthält: { points, actualKcal, isLastRound }

      setLastScore(result);

      const newRoundHistoryEntry = {
        actualCalories: result.actualKcal,
        guessMin: minCalories,
        guessMax: maxCalories,
        points: result.points
      };

      setRounds(prev => [...prev, newRoundHistoryEntry]);
      setShowResult(true); // Schaltet die Ansicht um (Ergebnis anzeigen)

    } catch (err) {
      console.error('Error submitting guess:', err);
      setError('Fehler beim Absenden der Schätzung');
    } finally {
      setSubmitting(false);
    }
  };

  const handleNextRound = async () => {
    if (lastScore?.isLastRound) {
      navigate(`/result/${sessionId}`);
      return;
    }

    setLoading(true);
    try {
      // Aufruf von /nextround?sessionId=...
      const nextData = await api.startNextRound(sessionId);
      // nextData: { roundId, barcode, imageUrl }

      setRoundData(nextData);
      setCurrentRoundNumber(prev => prev + 1);
      setShowResult(false);
      setMinCalories(0);
      setMaxCalories(1000);
    } catch (err) {
      setError('Konnte nächste Runde nicht laden');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Lade...</div>;

  return (
      <div className="game-page">
        <div className="game-container">
          <div className="game-header">
            <h1>NutriGuesser</h1>
            <div className="round-indicator">Runde {currentRoundNumber} von 5</div>
          </div>

          {error && <div className="error-banner">{error}</div>}

          <div className="game-content">
            <div className="product-section">
              <div className="product-card">
                <img src={roundData?.imageUrl} alt="Produkt" className="product-image" />
              </div>
            </div>

            <div className="guess-section">
              {!showResult ? (
                  <>
                    <h3>Schätze die Kalorien (pro 100g)</h3>
                    <CalorieSlider
                        min={minCalories}
                        max={maxCalories}
                        onMinChange={setMinCalories}
                        onMaxChange={setMaxCalories}
                    />
                    <button
                        className="submit-guess-button"
                        onClick={handleSubmitGuess}
                        disabled={submitting}
                    >
                      {submitting ? 'Sende...' : 'Schätzung abgeben'}
                    </button>
                  </>
              ) : (
                  <div className="result-display">
                    <h3>Ergebnis:</h3>
                    <p>Tatsächliche Kalorien: <strong>{lastScore.actualKcal} kcal</strong></p>
                    <p>Erhaltene Punkte: <strong>{lastScore.points}</strong></p>
                    <button className="next-round-button" onClick={handleNextRound}>
                      {lastScore.isLastRound ? 'Zum Endergebnis' : 'Nächste Runde'}
                    </button>
                  </div>
              )}
            </div>
          </div>

          {rounds.length > 0 && <RoundHistory rounds={rounds} />}
        </div>
      </div>
  );
}

export default GamePage;