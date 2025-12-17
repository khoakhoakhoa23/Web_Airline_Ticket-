import './LoadingSpinner.css';

/**
 * Loading Spinner Component
 * 
 * Reusable loading indicator with optional text
 */
const LoadingSpinner = ({ text = 'Loading...', size = 'medium', fullScreen = false }) => {
  if (fullScreen) {
    return (
      <div className="loading-spinner-overlay">
        <div className="loading-spinner-container">
          <div className={`loading-spinner ${size}`}></div>
          {text && <p className="loading-text">{text}</p>}
        </div>
      </div>
    );
  }

  return (
    <div className="loading-spinner-inline">
      <div className={`loading-spinner ${size}`}></div>
      {text && <p className="loading-text">{text}</p>}
    </div>
  );
};

export default LoadingSpinner;

