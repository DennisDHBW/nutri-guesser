import '../styles/RoundHistory.css';
function RoundHistory({ rounds }) {
  if (!rounds || rounds.length === 0) {
    return null;
  }
  return (
    <div className="round-history">
      <h3>Round Overview</h3>
      <div className="rounds-list">
        {rounds.map((round, index) => {
          const isCorrect = round.actualCalories >= round.guessMin && 
                           round.actualCalories <= round.guessMax;
          return (
            <div key={index} className={`round-item ${isCorrect ? 'correct' : 'incorrect'}`}>
              <div className="round-number">Round {index + 1}</div>
              <div className="round-details">
                <div className="product-name">{round.productName}</div>
                <div className="calories-info">
                  <div className="info-row">
                    <span className="label">Actual:</span>
                    <span className="value actual">{round.actualCalories} kcal</span>
                  </div>
                  <div className="info-row">
                    <span className="label">Your guess:</span>
                    <span className="value guess">
                      {round.guessMin} - {round.guessMax} kcal
                    </span>
                  </div>
                </div>
              </div>
              <div className="round-points">
                {round.points} pts
              </div>
              {isCorrect && <div className="correct-badge">✓</div>}
              {!isCorrect && <div className="incorrect-badge">✗</div>}
            </div>
          );
        })}
      </div>
    </div>
  );
}
export default RoundHistory;
