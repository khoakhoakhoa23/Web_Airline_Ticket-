import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/pages/Login.css';

// Sử dụng hình ảnh từ public folder
// File: public/images/airplane-sky.jpg
const airplaneImage = '/images/airplane-sky.jpg';

const Login = () => {
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const userData = await login(formData.email, formData.password);
      
      // Redirect based on user role
      if (userData.role === 'ADMIN') {
        navigate('/admin/dashboard');
      } else {
        navigate('/');
      }
    } catch (err) {
      setError(err.message || err.response?.data?.message || 'Login failed. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div 
        className="login-background"
        style={airplaneImage ? {
          backgroundImage: `url(${airplaneImage})`
        } : {}}
      ></div>
      <div className="login-container">
        <div className="login-form-wrapper">
          <div className="login-logo">
            <div className="logo-icon-large">✈</div>
            <h1 className="logo-title">FLIGHT NETWORK</h1>
          </div>
          <form onSubmit={handleSubmit} className="login-form">
            {error && <div className="error-message">{error}</div>}
            <div className="form-group">
              <label htmlFor="email">Email:</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="Email@yahoo.com"
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">Password:</label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="************"
                required
              />
            </div>
            <button type="submit" className="login-button" disabled={loading}>
              {loading ? 'Logging in...' : 'Login'}
            </button>
            <div className="register-prompt">
              <span>Do you want to register?</span>
              <Link to="/register" className="register-link">Register</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;

