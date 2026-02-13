const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export const api = {
  // Leaderboard abrufen
  getLeaderboard: async () => {
    const response = await fetch(`${API_BASE_URL}/leaderboard`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch leaderboard: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // Neuen Spieler erstellen
  createPlayer: async (name) => {
    console.log('Creating player with name:', name);
    const response = await fetch(`${API_BASE_URL}/players`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name })
    });
    if (!response.ok) {
      const error = await response.text();
      console.error('Create player failed:', response.status, error);
      throw new Error(`Failed to create player: ${response.status} - ${error}`);
    }
    const player = await response.json();
    console.log('Player created:', player);
    return player;
  },

  // Neue Spielsession starten
  startGameSession: async (playerId) => {
    console.log('Starting game session for player:', playerId);
    const response = await fetch(`${API_BASE_URL}/game/start?playerId=${playerId}`, {
      method: 'POST'
    });
    if (!response.ok) {
      const error = await response.text();
      console.error('Start game session failed:', response.status, error);
      throw new Error(`Failed to start game session: ${response.status} - ${error}`);
    }
    const session = await response.json();
    console.log('Game session started:', session);
    return session;
  },

  // Nächstes Produkt abrufen
  getNextProduct: async (sessionId) => {
    const response = await fetch(`${API_BASE_URL}/game/next-product?sessionId=${sessionId}`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch next product: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // Schätzung abgeben
  submitGuess: async (sessionId, productId, minCalories, maxCalories) => {
    const response = await fetch(`${API_BASE_URL}/game/guess`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        sessionId,
        productId,
        minCalories,
        maxCalories
      })
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to submit guess: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // Spielergebnis abrufen
  getResult: async (sessionId) => {
    const response = await fetch(`${API_BASE_URL}/result?sessionId=${sessionId}`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch result: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // OpenFoodFacts (interner Service) - Zufallsbild
  getOffRandom: async () => {
    const response = await fetch(`${API_BASE_URL}/off/random`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch OFF random: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // OpenFoodFacts (interner Service) - Produktdetails
  getOffProduct: async (barcode) => {
    const response = await fetch(`${API_BASE_URL}/off/product/${barcode}`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch OFF product: ${response.status} - ${error}`);
    }
    return response.json();
  }
};
