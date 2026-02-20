import { useNavigate } from 'react-router-dom';
import '../styles/InfoPage.css';

function InfoPage() {
  const navigate = useNavigate();

  return (
    <div className="info-page">
      <div className="info-container">
        <button className="back-button-info" onClick={() => navigate('/')}>
          ← Back to Home
        </button>

        <div className="info-content">
          <h1 className="info-title">About NutriGuesser</h1>

          <section className="info-section">
            <h2>About this Project</h2>
            <p>
              This project implements an interactive learning game that raises awareness of the calorie density of foods.
            </p>
            <ul className="about-list">
              <li>
                <strong>Game mechanics:</strong> A random food item, including an image, is loaded from the OpenFoodFacts API.
              </li>
              <li>
                <strong>Interaction:</strong> The player must estimate the range of actual calories (per 100g). The more accurate the guess, the more points are awarded.
              </li>
              <li>
                <strong>Conclusion:</strong> After the game, a score is displayed. A cat GIF (via the Cataas API) reflects the player's performance.
              </li>
              <li>
                <strong>High score:</strong> Players with top scores can have their names immortalized on a global leaderboard.
              </li>
            </ul>
          </section>

          <section className="info-section">
            <h2>How to Play</h2>
            <ol className="instructions-list">
              <li>
                <strong>Enter Your Name:</strong> Start by entering your player name (max 12 characters)
              </li>
              <li>
                <strong>Guess the Calories:</strong> For each product shown, estimate the caloric content per 100g
                using the slider to set your min and max calorie range
              </li>
              <li>
                <strong>Submit Your Guess:</strong> Click "Submit guess" to check your answer
              </li>
              <li>
                <strong>View Results:</strong> See the actual calorie content and points earned for your guess
              </li>
              <li>
                <strong>Play 5 Rounds:</strong> Complete all 5 rounds to finish the game
              </li>
              <li>
                <strong>Check the Leaderboard:</strong> Your final score is added to the leaderboard and ranked
                against other players
              </li>
            </ol>
          </section>

          <section className="info-section">
            <h2>External APIs Used</h2>
            <div className="api-list">
              <div className="api-card">
                <h3>Cataas API</h3>
                <p>
                  <strong>Purpose:</strong> Provides cute cat images as rewards when you complete the game
                </p>
                <p>
                  <strong>Website:</strong> <a href="https://cataas.com" target="_blank" rel="noopener noreferrer">cataas.com</a>
                </p>
                <p>
                  A free API that delivers delightful cat images to celebrate your gaming accomplishment!
                </p>
              </div>

              <div className="api-card">
                <h3>Open Food Facts API</h3>
                <p>
                  <strong>Purpose:</strong> Provides a vast database of real food products with their nutritional information
                </p>
                <p>
                  <strong>Website:</strong> <a href="https://world.openfoodfacts.org" target="_blank" rel="noopener noreferrer">world.openfoodfacts.org</a>
                </p>
                <p>
                  A community-driven, free and open database of food products from around the world. It helps players
                  learn about real nutritional values of commonly available products.
                </p>
              </div>
            </div>
          </section>

          <section className="info-section">
            <h2>Technology Stack</h2>
            <p>
              <strong>Frontend:</strong> React, Vite, CSS3
            </p>
            <p>
              <strong>Backend:</strong> Java, Quarkus, Spring Data JPA, H2 Database
            </p>
            <p>
              <strong>External Services:</strong> Open Food Facts API, Cataas API
            </p>
          </section>
        </div>
      </div>
    </div>
  );
}

export default InfoPage;


