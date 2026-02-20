import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import '../styles/PlayerNamePage.css';

function PlayerNamePage() {

  const MAX_NAME_LENGTH = 20;
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (name.trim().length === 0) {
      setError('Please enter a name');
      return;
    }

    if (name.length > MAX_NAME_LENGTH) {
      setError('Name must be at most ' + MAX_NAME_LENGTH + ' characters long');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // start game session and get initial round data
      const { sessionId, roundId, barcode, imageUrl, name: productName } = await api.startGameSession(name);

      navigate(`/game/${sessionId}`, {
        state: {
          sessionId,
          currentRoundData: {
            roundId,
            barcode,
            imageUrl,
            name: productName
          }
        }
      });
    } catch (err) {
      console.error('Error starting game:', err);
      setError('Error starting the game. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="player-name-page">
      <div className="name-container">
        <h1 className="title">Welcome! 👋</h1>
        <p className="subtitle">Enter your name to start</p>

        <form onSubmit={handleSubmit} className="name-form">
          <div className="input-group">
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Your name"
              maxLength={MAX_NAME_LENGTH}
              className="name-input"
              disabled={loading}
              autoFocus
            />
            <div className="char-counter">
              {name.length}/{MAX_NAME_LENGTH}
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <button
            type="submit"
            className="submit-button"
            disabled={loading || name.trim().length === 0}
          >
            {loading ? 'Starting...' : 'Start game'}
          </button>
        </form>

        <button
          className="back-button"
          onClick={() => navigate('/')}
          disabled={loading}
        >
          Back
        </button>
      </div>
    </div>
  );
}

export default PlayerNamePage;
