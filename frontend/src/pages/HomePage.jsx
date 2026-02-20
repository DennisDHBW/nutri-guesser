import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import '../styles/HomePage.css';

function HomePage() {
  const navigate = useNavigate();
  const [leaderboard, setLeaderboard] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadLeaderboard();
  }, []);

  const loadLeaderboard = async () => {
    try {
      const data = await api.getLeaderboard();
      setLeaderboard(data);
    } catch (error) {
      console.error('Error loading leaderboard:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleStartGame = () => {
    navigate('/enter-name');
  };

  return (
    <div className="home-page">
      <div className="home-container">
        <div className="header">
          <h1 className="title">🍔 NutriGuesser 🥗</h1>
          <p className="subtitle">Guess the calories - Learn while playing!</p>
        </div>

        <button className="start-button" onClick={handleStartGame}>
          Start Game
        </button>

        <div className="leaderboard-section">
          <h2 className="leaderboard-title">🏆 Leaderboard</h2>
          <div className="leaderboard-container">
            {loading ? (
              <div className="loading">Loading leaderboard...</div>
            ) : leaderboard.length === 0 ? (
              <div className="empty">No entries yet</div>
            ) : (
              <div className="leaderboard-list">
                {leaderboard.map((entry, index) => (
                  <div
                    key={entry.id || index}
                    className={`leaderboard-entry ${index < 3 ? `rank-${index + 1}` : ''}`}
                  >
                    <div className="rank">
                      {index === 0 && '🥇'}
                      {index === 1 && '🥈'}
                      {index === 2 && '🥉'}
                      {index > 2 && `#${index + 1}`}
                    </div>
                    <div className="player-name">{entry.playerName || entry.player?.nickname || 'Unknown'}</div>
                    <div className="score">{entry.score || 0} pts</div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default HomePage;
