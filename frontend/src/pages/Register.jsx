import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/pages/Register.css';

// Sử dụng hình ảnh từ public folder
// File: public/images/airplane-sky.jpg
const airplaneImage = '/images/airplane-sky.jpg';

const Register = () => {
  const [formData, setFormData] = useState({
    userName: '',
    family: '',
    email: '',
    password: '',
    phone: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { register } = useAuth();
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

    // Validate required fields
    if (!formData.email || !formData.password) {
      setError('Email and password are required.');
      setLoading(false);
      return;
    }

    // Validate email format
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('Please enter a valid email address.');
      setLoading(false);
      return;
    }

    // Validate password length
    if (formData.password.length < 6) {
      setError('Password must be at least 6 characters.');
      setLoading(false);
      return;
    }

    try {
      const registerData = {
        email: formData.email.trim(),
        password: formData.password,
        phone: formData.phone?.trim() || null,
        role: 'USER',
      };
      
      console.log('Registering with data:', registerData);
      await register(registerData);
      navigate('/');
    } catch (err) {
      // Handle validation errors
      if (err.validationErrors) {
        // Display all validation errors
        const errorMessages = Object.entries(err.validationErrors)
          .filter(([key]) => key !== 'status')
          .map(([field, message]) => `${field}: ${message}`);
        setError(errorMessages.length > 0 ? errorMessages.join(', ') : 'Dữ liệu không hợp lệ.');
      } else {
        setError(err.message || err.response?.data?.message || 'Registration failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-page">
      <div 
        className="register-background"
        style={airplaneImage ? {
          backgroundImage: `url(${airplaneImage})`
        } : {}}
      ></div>
      <div className="register-container">
        <div className="register-form-wrapper">
          <div className="register-logo">
            <div className="logo-icon-large">✈</div>
            <h1 className="logo-title">FLIGHT NETWORK</h1>
          </div>
          <form onSubmit={handleSubmit} className="register-form">
            {error && <div className="error-message">{error}</div>}
            <div className="form-group">
              <label htmlFor="userName">User Name:</label>
              <input
                type="text"
                id="userName"
                name="userName"
                value={formData.userName}
                onChange={handleChange}
                placeholder="test"
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="family">Family:</label>
              <input
                type="text"
                id="family"
                name="family"
                value={formData.family}
                onChange={handleChange}
                placeholder="Passenger"
                required
              />
            </div>
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
              <label htmlFor="phone">Phone:</label>
              <input
                type="tel"
                id="phone"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                placeholder="0123456789"
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
                minLength={6}
              />
            </div>
            <button type="submit" className="register-button" disabled={loading}>
              {loading ? 'Registering...' : 'Register'}
            </button>
            <div className="login-prompt">
              <span>Already have an account?</span>
              <Link to="/login" className="login-link">Login</Link>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Register;

