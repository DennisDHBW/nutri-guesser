import { useState, useRef, useEffect } from 'react';
import '../styles/CalorieSlider.css';
function CalorieSlider({ min, max, onMinChange, onMaxChange }) {
  const sliderRef = useRef(null);
  const [isDraggingMin, setIsDraggingMin] = useState(false);
  const [isDraggingMax, setIsDraggingMax] = useState(false);
  const [editingMin, setEditingMin] = useState(false);
  const [editingMax, setEditingMax] = useState(false);
  const [tempMin, setTempMin] = useState(min.toString());
  const [tempMax, setTempMax] = useState(max.toString());
  const MAX_CALORIES = 1000;
  useEffect(() => {
    setTempMin(min.toString());
  }, [min]);
  useEffect(() => {
    setTempMax(max.toString());
  }, [max]);
  const handleMouseDown = (type) => (e) => {
    e.preventDefault();
    if (type === 'min') {
      setIsDraggingMin(true);
    } else {
      setIsDraggingMax(true);
    }
  };
  const handleMouseUp = () => {
    setIsDraggingMin(false);
    setIsDraggingMax(false);
  };
  useEffect(() => {
    if (isDraggingMin || isDraggingMax) {
      const handleMouseMove = (e) => {
        if (!isDraggingMin && !isDraggingMax) return;
        const slider = sliderRef.current;
        if (!slider) return;
        const rect = slider.getBoundingClientRect();
        const x = Math.max(0, Math.min(e.clientX - rect.left, rect.width));
        const percentage = x / rect.width;
        const value = Math.round(percentage * MAX_CALORIES);
        if (isDraggingMin) {
          const newMin = Math.max(0, Math.min(value, max));
          onMinChange(newMin);
        } else if (isDraggingMax) {
          const newMax = Math.min(MAX_CALORIES, Math.max(value, min));
          onMaxChange(newMax);
        }
      };
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      return () => {
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [isDraggingMin, isDraggingMax, min, max, onMinChange, onMaxChange]);
  const handleMinInputChange = (e) => {
    setTempMin(e.target.value);
  };
  const handleMaxInputChange = (e) => {
    setTempMax(e.target.value);
  };
  const handleMinInputBlur = () => {
    const value = parseInt(tempMin) || 0;
    const newMin = Math.max(0, Math.min(value, max, MAX_CALORIES));
    onMinChange(newMin);
    setTempMin(newMin.toString());
    setEditingMin(false);
  };
  const handleMaxInputBlur = () => {
    const value = parseInt(tempMax) || 0;
    const newMax = Math.min(MAX_CALORIES, Math.max(value, min, 0));
    onMaxChange(newMax);
    setTempMax(newMax.toString());
    setEditingMax(false);
  };
  const handleMinInputKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleMinInputBlur();
    }
  };
  const handleMaxInputKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleMaxInputBlur();
    }
  };
  const minPercent = (min / MAX_CALORIES) * 100;
  const maxPercent = (max / MAX_CALORIES) * 100;
  return (
    <div className="calorie-slider">
      <div className="slider-labels">
        <div className="label-group">
          <span className="label-text">Minimum:</span>
          {editingMin ? (
            <input
              type="number"
              value={tempMin}
              onChange={handleMinInputChange}
              onBlur={handleMinInputBlur}
              onKeyPress={handleMinInputKeyPress}
              className="value-input"
              autoFocus
              min="0"
              max={max - 1}
            />
          ) : (
            <span 
              className="value-display clickable" 
              onClick={() => setEditingMin(true)}
            >
              {min} kcal
            </span>
          )}
        </div>
        <div className="label-group">
          <span className="label-text">Maximum:</span>
          {editingMax ? (
            <input
              type="number"
              value={tempMax}
              onChange={handleMaxInputChange}
              onBlur={handleMaxInputBlur}
              onKeyPress={handleMaxInputKeyPress}
              className="value-input"
              autoFocus
              min={min + 1}
              max={MAX_CALORIES}
            />
          ) : (
            <span 
              className="value-display clickable" 
              onClick={() => setEditingMax(true)}
            >
              {max} kcal
            </span>
          )}
        </div>
      </div>
      <div className="slider-container" ref={sliderRef}>
        <div className="slider-track">
          <div 
            className="slider-range" 
            style={{
              left: `${minPercent}%`,
              width: `${maxPercent - minPercent}%`
            }}
          />
          <div 
            className="slider-handle slider-handle-min"
            style={{ left: `${minPercent}%` }}
            onMouseDown={handleMouseDown('min')}
          >
            <div className="handle-tooltip">{min}</div>
          </div>
          <div 
            className="slider-handle slider-handle-max"
            style={{ left: `${maxPercent}%` }}
            onMouseDown={handleMouseDown('max')}
          >
            <div className="handle-tooltip">{max}</div>
          </div>
        </div>
        <div className="slider-scale">
          <span>0</span>
          <span>250</span>
          <span>500</span>
          <span>750</span>
          <span>1000</span>
        </div>
      </div>
      <div className="range-info">
        Estimated range: <strong>{min} - {max} kcal</strong> (Span: {max - min} kcal)
      </div>
    </div>
  );
}
export default CalorieSlider;
