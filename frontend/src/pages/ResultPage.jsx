import { useState, useEffect, useCallback, useMemo } from 'react';
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
      const cacheKey = `result_${sessionId}`;
      const cachedData = sessionStorage.getItem(cacheKey);

      let data;
      if (cachedData) {
        console.log('Using cached result data for sessionId:', sessionId);
        data = JSON.parse(cachedData);
      } else {
        console.log('Fetching fresh result data for sessionId:', sessionId);
        data = await api.getResult(sessionId);
        sessionStorage.setItem(cacheKey, JSON.stringify(data));
      }

      setResult(data);
    } catch (err) {
      console.error('Error loading result:', err);
      setError('Failed to load result');
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  useEffect(() => {
    loadResult();
  }, [loadResult]);

  const handlePlayAgain = () => {
    const cacheKey = `result_${sessionId}`;
    sessionStorage.removeItem(cacheKey);
    navigate('/');
  };

  const normalizedResultUrl = useMemo(() => {
    if (!result?.url) return null;
    if (result.url.startsWith('http://') || result.url.startsWith('https://')) {
      return result.url;
    }
    const base = 'https://cataas.com';
    return `${base}${result.url.startsWith('/') ? '' : '/'}${result.url}`;
  }, [result]);

  const catImageUrl = useMemo(() => {
    if (result?.url) {
      return normalizedResultUrl;
    }
    if (sessionId) {
      return `/api/result/image?sessionId=${sessionId}`;
    }
    return null;
  }, [normalizedResultUrl, result?.url, sessionId]);

  const [imageSrc, setImageSrc] = useState(catImageUrl);
  const [imageLoading, setImageLoading] = useState(Boolean(catImageUrl));

  useEffect(() => {
    setImageSrc(catImageUrl);
    setImageLoading(Boolean(catImageUrl));
  }, [catImageUrl]);

  const formattedBetterThan = useMemo(() => {
    if (result?.betterThanPercentage == null || Number.isNaN(Number(result.betterThanPercentage))) {
      return null;
    }
    return Number(result.betterThanPercentage).toLocaleString('en-US', {
      minimumFractionDigits: 1,
      maximumFractionDigits: 1
    });
  }, [result]);

  if (loading) {
    return (
      <div className="result-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Loading result...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="result-page">
        <div className="error-container">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={handlePlayAgain}>Back to home</button>
        </div>
      </div>
    );
  }

  return (
    <div className="result-page">
      <div className="result-container">
        <h1 className="result-title">Game over! 🎉</h1>

        {catImageUrl && (
          <div className="cat-image-container">
            {imageLoading && (
              <div className="cat-image-loading">
                <div className="spinner"></div>
                <span>Loading image...</span>
              </div>
            )}
            {imageSrc && (
              <img
                src={imageSrc}
                alt="Cat result"
                className="cat-image"
                onLoad={() => setImageLoading(false)}
                onError={() => {
                  if (imageSrc && normalizedResultUrl && imageSrc !== normalizedResultUrl) {
                    setImageSrc(normalizedResultUrl);
                    setImageLoading(true);
                    return;
                  }
                  setImageLoading(false);
                  setImageSrc(null);
                }}
              />
            )}
          </div>
        )}
        {!imageLoading && imageSrc == null && (
          <div className="cat-image-fallback">Cat image could not be loaded.</div>
        )}

        {(result?.rank != null || formattedBetterThan != null) && (
          <div className="result-placement">
            {result?.rank != null && (
              <div className="placement-text">Placement: {result.rank}</div>
            )}
            {formattedBetterThan != null && (
              <div className="placement-text">
                Better than: {formattedBetterThan}%
              </div>
            )}
          </div>
        )}

        <div className="total-score-section">
          <h2>Your score</h2>
          <div className="total-score">{result?.totalScore || 0}</div>
          <div className="score-subtitle">out of 500 max points</div>
        </div>

        {result?.rounds && result.rounds.length > 0 && (
          <div className="rounds-overview">
            <h3>Round overview</h3>
            <RoundHistory rounds={result.rounds} showDetails={true} />
          </div>
        )}

        {result?.rank != null && (
          <div className="leaderboard-rank">
            <p>
              {result.rank <= 10
                ? `🏆 You are ranked #${result.rank} on the leaderboard!`
                : 'You did not make the top 10. Try again!'
              }
            </p>
          </div>
        )}

        <div className="result-actions">
          <button className="play-again-button" onClick={handlePlayAgain}>
            Play again
          </button>
        </div>
      </div>
    </div>
  );
}

export default ResultPage;
