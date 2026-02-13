import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import PlayerNamePage from './pages/PlayerNamePage';
import GamePage from './pages/GamePage';
import ResultPage from './pages/ResultPage';
import './styles/App.css';

function App() {
  return (
    <Router>
      <div className="app">
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/enter-name" element={<PlayerNamePage />} />
          <Route path="/game/:sessionId" element={<GamePage />} />
          <Route path="/result/:sessionId" element={<ResultPage />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
