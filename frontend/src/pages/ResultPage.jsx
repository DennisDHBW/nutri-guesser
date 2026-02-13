import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import RoundHistory from '../components/RoundHistory';
import '../styles/ResultPage.css';

function ResultPage() {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadResult = useCallback(async () => {
    try {
      const data = await api.getResult(sessionId);
      setResult(data);
    } catch (err) {
      console.error('Error loading result:', err);
      setError('Fehler beim Laden des Ergebnisses');
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  useEffect(() => {
    loadResult();
  }, [loadResult]);

  const handlePlayAgain = () => {
    navigate('/');
  };

  if (loading) {
    return (
      <div className="result-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Lade Ergebnis...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="result-page">
        <div className="error-container">
          <h2>Fehler</h2>
          <p>{error}</p>
          <button onClick={handlePlayAgain}>Zurück zur Startseite</button>
        </div>
      </div>
    );
  }

  return (
    <div className="result-page">
      <div className="result-container">
        <h1 className="result-title">Spiel beendet! 🎉</h1>

        {/* Katzen-Bild von Cataas */}
        {result?.catImageUrl && (
          <div className="cat-image-container">
            <img
              src={result.catImageUrl}
              alt="Cat result"
              className="cat-image"
              onError={(e) => {
                e.target.style.display = 'none';
              }}
            />
            {result.catText && (
              <div className="cat-text">{result.catText}</div>
            )}
          </div>
        )}

        {/* Gesamtpunktzahl */}
        <div className="total-score-section">
          <h2>Deine Punktzahl</h2>
          <div className="total-score">{result?.totalScore || 0}</div>
          <div className="score-subtitle">von max. 500 Punkten</div>
        </div>

        {/* Rundenübersicht */}
        {result?.rounds && result.rounds.length > 0 && (
          <div className="rounds-overview">
            <h3>Rundenübersicht</h3>
            <RoundHistory rounds={result.rounds} showDetails={true} />
          </div>
        )}

        {/* Platzierung im Leaderboard */}
        {result?.leaderboardRank && (
          <div className="leaderboard-rank">
            <p>
              {result.leaderboardRank <= 10
                ? `🏆 Du bist auf Platz ${result.leaderboardRank} im Leaderboard!`
                : `Du hast es nicht in die Top 10 geschafft. Versuche es nochmal!`
              }
            </p>
          </div>
        )}

        <div className="result-actions">
          <button className="play-again-button" onClick={handlePlayAgain}>
            Nochmal spielen
          </button>
        </div>
      </div>
    </div>
  );
}

export default ResultPage;

