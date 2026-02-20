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

  // Initial data comes from navigation state (from start page)
  const [roundData, setRoundData] = useState(location.state?.currentRoundData || null);

  const [minCalories, setMinCalories] = useState(0);
  const [maxCalories, setMaxCalories] = useState(1000);
  const [loading, setLoading] = useState(!roundData);
  const [submitting, setSubmitting] = useState(false);
  const [showResult, setShowResult] = useState(false); // Shows result after guessing
  const [lastScore, setLastScore] = useState(null);

  const [rounds, setRounds] = useState([]);
  const [currentRoundNumber, setCurrentRoundNumber] = useState(1);
  const [error, setError] = useState('');
  const [imageError, setImageError] = useState(false);

  // If page is reloaded and state is lost:
  useEffect(() => {
    if (!roundData) {
      setError("Session invalid. Please start the game again.");
    }
  }, [roundData]);

  useEffect(() => {
    setImageError(false);
  }, [roundData?.imageUrl]);

  const handleSubmitGuess = async () => {
    if (!roundData?.roundId || !roundData?.barcode) {
      setError('Product data missing. Please start the game again.');
      return;
    }
    setSubmitting(true);
    setError('');

    try {
      // New request record: int guessedMin, int guessedMax, UUID roundId, String barcode
      const scoreRequest = {
        guessedMin: Math.round(minCalories),
        guessedMax: Math.round(maxCalories),
        roundId: roundData.roundId,
        barcode: roundData.barcode
      };

      const result = await api.submitGuess(scoreRequest);
      // result contains: { points, actualKcal, isLastRound }

      setLastScore(result);

      const newRoundHistoryEntry = {
        actualCalories: result.actualKcal,
        guessMin: minCalories,
        guessMax: maxCalories,
        points: result.points
      };

      setRounds(prev => [...prev, newRoundHistoryEntry]);
      setShowResult(true); // Switches view to show result

    } catch (err) {
      console.error('Error submitting guess:', err);
      setError('Error submitting your guess');
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
      // Call /nextround?sessionId=...
      const nextData = await api.startNextRound(sessionId);
      // nextData: { roundId, barcode, imageUrl }

      setRoundData(nextData);
      setCurrentRoundNumber(prev => prev + 1);
      setShowResult(false);
      setMinCalories(0);
      setMaxCalories(1000);
    } catch (err) {
      console.error('Error loading next round:', err);
      setError('Could not load next round');
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div className="loading">Loading...</div>;

  return (
      <div className="game-page">
        <div className="game-container">
          <div className="game-header">
            <h1>NutriGuesser</h1>
            <div className="round-indicator">Round {currentRoundNumber} of 5</div>
          </div>

          {error && <div className="error-banner">{error}</div>}

          <div className="game-content">
            <div className="product-section">
              <div className="product-card">
                {roundData?.imageUrl && !imageError ? (
                  <img
                    src={roundData.imageUrl}
                    alt={roundData?.name ?? 'Produkt'}
                    className="product-image"
                    onError={() => setImageError(true)}
                  />
                ) : (
                  <div className="product-image-placeholder">
                    <span>🍽️</span>
                    <p>No image available</p>
                  </div>
                )}
                {roundData?.name && <div className="product-name">{roundData.name}</div>}
              </div>
            </div>

            <div className="guess-section">
              {!showResult ? (
                  <>
                    <h3>Guess the calories (per 100g)</h3>
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
                      {submitting ? 'Sending...' : 'Submit guess'}
                    </button>
                  </>
              ) : (
                  <div className="result-display">
                    <div className="result-title">Round Result</div>
                    <div className="result-content">
                      <div className="result-item">
                        <span className="result-label">Actual Calories:</span>
                        <span className="result-value">{lastScore.actualKcal}</span>
                        <span className="result-unit">kcal</span>
                      </div>
                      <div className="result-item">
                        <span className="result-label">Points Earned:</span>
                        <span className="result-value points">{lastScore.points}</span>
                      </div>
                    </div>
                    <button className="next-round-button" onClick={handleNextRound}>
                      {lastScore.isLastRound ? 'View final result' : 'Next round'}
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
