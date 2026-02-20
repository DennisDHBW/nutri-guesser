const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export const api = {
  // Get leaderboard
  getLeaderboard: async () => {
    const response = await fetch(`${API_BASE_URL}/leaderboard`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch leaderboard: ${response.status} - ${error}`);
    }
    return response.json();
  },


  startGameSession: async (nickname) => {
    console.log('Starting game session for player:', nickname);
    const response = await fetch(`${API_BASE_URL}/game/start`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        nickname: nickname
      })
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


  startNextRound: async (sessionId) => {
    const response = await fetch(`${API_BASE_URL}/game/nextround?sessionId=${sessionId}`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch next round: ${response.status} - ${error}`);
    }
    return response.json();
  },


  submitGuess: async (scoreRequest) => {
    const response = await fetch(`${API_BASE_URL}/score`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(scoreRequest)
    });
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to submit guess: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // Get game result
  getResult: async (sessionId) => {
    const response = await fetch(`${API_BASE_URL}/result?sessionId=${sessionId}`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch result: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // OpenFoodFacts (internal service) - Random image
  getOffRandom: async () => {
    const response = await fetch(`${API_BASE_URL}/off/random`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch OFF random: ${response.status} - ${error}`);
    }
    return response.json();
  },

  // OpenFoodFacts (internal service) - Product details
  getOffProduct: async (barcode) => {
    const response = await fetch(`${API_BASE_URL}/off/product/${barcode}`);
    if (!response.ok) {
      const error = await response.text();
      throw new Error(`Failed to fetch OFF product: ${response.status} - ${error}`);
    }
    return response.json();
  }
};
