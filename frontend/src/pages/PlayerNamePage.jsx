import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import '../styles/PlayerNamePage.css';

function PlayerNamePage() {
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (name.trim().length === 0) {
      setError('Bitte gib einen Namen ein');
      return;
    }

    if (name.length > 12) {
      setError('Name darf maximal 12 Zeichen lang sein');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // start game session and get initial round data
      const { sessionId, roundId, barcode, imageUrl } = await api.startGameSession(name);

      navigate(`/game/${session.sessionId}`, {
        state: {
          sessionId,
          currentRoundData: {
            roundId,
            barcode,
            imageUrl
          }
        }
      });
    } catch (err) {
      console.error('Error starting game:', err);
      setError('Fehler beim Starten des Spiels. Bitte versuche es erneut.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="player-name-page">
      <div className="name-container">
        <h1 className="title">Willkommen! 👋</h1>
        <p className="subtitle">Gib deinen Namen ein, um zu starten</p>

        <form onSubmit={handleSubmit} className="name-form">
          <div className="input-group">
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Dein Name"
              maxLength={12}
              className="name-input"
              disabled={loading}
              autoFocus
            />
            <div className="char-counter">
              {name.length}/12
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <button
            type="submit"
            className="submit-button"
            disabled={loading || name.trim().length === 0}
          >
            {loading ? 'Starte...' : 'Spiel beginnen'}
          </button>
        </form>

        <button
          className="back-button"
          onClick={() => navigate('/')}
          disabled={loading}
        >
          Zurück
        </button>
      </div>
    </div>
  );
}

export default PlayerNamePage;
