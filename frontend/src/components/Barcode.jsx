import './Barcode.css';

/**
 * Simple Barcode Component
 * Displays a simple barcode visualization using CSS
 * 
 * @param {string} value - The value to encode in the barcode
 * @param {string} format - Format: 'simple' (default) or 'detailed'
 */
const Barcode = ({ value, format = 'simple' }) => {
  if (!value) return null;

  // Generate barcode pattern from value
  const generateBarcodePattern = (str) => {
    // Simple hash-based pattern generation
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      const char = str.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // Convert to 32bit integer
    }
    
    // Generate bars based on hash
    const pattern = [];
    const numBars = format === 'detailed' ? 60 : 40;
    
    for (let i = 0; i < numBars; i++) {
      const barWidth = Math.abs(hash + i) % 4 + 1; // 1-4px width
      const isWide = barWidth >= 3;
      pattern.push({
        width: isWide ? '3px' : '1px',
        height: format === 'detailed' ? '60px' : '40px',
        marginRight: '1px'
      });
      hash = (hash * 31 + i) & 0x7FFFFFFF;
    }
    
    return pattern;
  };

  const pattern = generateBarcodePattern(value);

  return (
    <div className="barcode-container">
      <div className="barcode-value">{value}</div>
      <div className="barcode-bars">
        {pattern.map((bar, index) => (
          <div
            key={index}
            className="barcode-bar"
            style={{
              width: bar.width,
              height: bar.height,
              marginRight: bar.marginRight,
              backgroundColor: '#000'
            }}
          />
        ))}
      </div>
      <div className="barcode-label">Mã vạch vé</div>
    </div>
  );
};

export default Barcode;

