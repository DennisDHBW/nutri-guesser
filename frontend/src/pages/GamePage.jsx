import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import CalorieSlider from '../components/CalorieSlider';
import RoundHistory from '../components/RoundHistory';
import '../styles/GamePage.css';

function GamePage() {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState(null);
  const [minCalories, setMinCalories] = useState(0);
  const [maxCalories, setMaxCalories] = useState(1000);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [rounds, setRounds] = useState([]);
  const [currentRound, setCurrentRound] = useState(1);
  const [error, setError] = useState('');

  const loadNextProduct = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const random = await api.getOffRandom();
      if (!random?.barcode) {
        throw new Error('No barcode from OFF');
      }

      const offResponse = await api.getOffProduct(random.barcode);
      const offProduct = offResponse?.product;

      if (!offProduct) {
        throw new Error('No product data from OFF');
      }

      const calories = offProduct.nutriments?.energyKcal100G
        ?? offProduct.nutriments?.energyKcal
        ?? offProduct.nutriments?.energyKcalValue
        ?? 0;

      const mapped = {
        id: offProduct.code || random.barcode,
        productName: offProduct.productName || 'Unbekanntes Produkt',
        brand: offProduct.brands || '',
        imageUrl: random.imageUrl || offProduct.imageUrl || offProduct.imageFrontUrl,
        calories
      };

      setProduct(mapped);
       // Reset Schätzwerte
       setMinCalories(0);
       setMaxCalories(1000);
    } catch (err) {
      console.error('Error loading product:', err);
      try {
        const fallback = await api.getNextProduct(sessionId);
        setProduct(fallback);
      } catch (fallbackError) {
        console.error('Error loading fallback product:', fallbackError);
        setError('Fehler beim Laden des Produkts');
      }
    } finally {
      setLoading(false);
    }
  }, [sessionId]);

  useEffect(() => {
    loadNextProduct();
  }, [loadNextProduct]);

  const handleSubmitGuess = async () => {
    if (minCalories >= maxCalories) {
      setError('Minimale Kalorien müssen kleiner als maximale Kalorien sein');
      return;
    }

    setSubmitting(true);
    setError('');

    try {
      const result = await api.submitGuess(
        sessionId,
        product.id,
        minCalories,
        maxCalories
      );

      // Füge Runde zur Historie hinzu
      const newRound = {
        productName: product.productName || product.name,
        actualCalories: product.calories || product.energyKcal100g,
        guessMin: minCalories,
        guessMax: maxCalories,
        points: result.points || 0
      };

      const updatedRounds = [...rounds, newRound];
      setRounds(updatedRounds);

      // Prüfe ob Spiel beendet ist (5 Runden)
      if (updatedRounds.length >= 5) {
        navigate(`/result/${sessionId}`);
      } else {
        setCurrentRound(updatedRounds.length + 1);
        await loadNextProduct();
      }
    } catch (err) {
      console.error('Error submitting guess:', err);
      setError('Fehler beim Absenden der Schätzung');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading && !product) {
    return (
      <div className="game-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Lade Produkt...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="game-page">
      <div className="game-container">
        <div className="game-header">
          <h1>NutriGuesser</h1>
          <div className="round-indicator">
            Runde {currentRound} von 5
          </div>
        </div>

        {error && <div className="error-banner">{error}</div>}

        <div className="game-content">
          <div className="product-section">
            <div className="product-card">
              {product?.imageUrl ? (
                <img
                  src={product.imageUrl}
                  alt={product.productName || 'Produkt'}
                  className="product-image"
                  onError={(e) => {
                    e.target.src = 'https://via.placeholder.com/300x300?text=Kein+Bild';
                  }}
                />
              ) : (
                <div className="product-image-placeholder">
                  <span>📦</span>
                  <p>Kein Bild verfügbar</p>
                </div>
              )}
              <h2 className="product-name">
                {product?.productName || product?.name || 'Unbekanntes Produkt'}
              </h2>
            </div>
          </div>

          <div className="guess-section">
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
              disabled={submitting || minCalories >= maxCalories}
            >
              {submitting ? 'Sende...' : 'Schätzung abgeben'}
            </button>
          </div>
        </div>

        {rounds.length > 0 && (
          <RoundHistory rounds={rounds} />
        )}
      </div>
    </div>
  );
}

export default GamePage;

