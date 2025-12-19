import { useState, useEffect } from 'react';
import { getDashboardStats } from '../../services/adminService';
import './AdminDashboard.css';

const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDashboardStats();
  }, []);

  const loadDashboardStats = async () => {
    try {
      setLoading(true);
      const data = await getDashboardStats();
      setStats(data);
      setError(null);
    } catch (err) {
      console.error('Failed to load dashboard stats:', err);
      setError('Failed to load dashboard statistics');
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    if (!amount && amount !== 0) return '0 VND';
    
    // Handle both number and string (BigDecimal from backend might be string)
    const numAmount = typeof amount === 'string' ? parseFloat(amount) : Number(amount);
    
    if (isNaN(numAmount)) return '0 VND';
    
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(numAmount);
  };

  const statCards = [
    {
      title: 'Total Users',
      value: stats?.totalUsers || 0,
      icon: '👥',
      color: '#3b82f6',
      bgColor: '#dbeafe',
      trend: '+12%'
    },
    {
      title: 'Total Bookings',
      value: stats?.totalBookings || 0,
      icon: '🎫',
      color: '#8b5cf6',
      bgColor: '#ede9fe',
      trend: '+8%'
    },
    {
      title: 'Total Flights',
      value: stats?.totalFlights || 0,
      icon: '✈️',
      color: '#10b981',
      bgColor: '#d1fae5',
      trend: '+5%'
    },
    {
      title: 'Total Revenue',
      value: formatCurrency(stats?.totalRevenue),
      icon: '💰',
      color: '#f59e0b',
      bgColor: '#fef3c7',
      trend: '+15%'
    }
  ];

  const todayStats = [
    {
      title: 'Bookings Today',
      value: stats?.bookingsToday || 0,
      icon: '📊',
      color: '#06b6d4'
    },
    {
      title: 'Revenue Today',
      value: formatCurrency(stats?.revenueToday),
      icon: '💵',
      color: '#14b8a6'
    }
  ];

  const bookingStatusData = [
    {
      status: 'Confirmed',
      count: stats?.confirmedBookings || 0,
      color: '#10b981',
      icon: '✅'
    },
    {
      status: 'Pending',
      count: stats?.pendingBookings || 0,
      color: '#f59e0b',
      icon: '⏳'
    },
    {
      status: 'Cancelled',
      count: stats?.cancelledBookings || 0,
      color: '#ef4444',
      icon: '❌'
    }
  ];

  if (loading) {
    return (
      <div className="dashboard-container">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-container">
        <div className="error-state">
          <span className="error-icon">⚠️</span>
          <p>{error}</p>
          <button onClick={loadDashboardStats} className="retry-btn">
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-container">
      {/* Header */}
      <div className="dashboard-header">
        <div>
          <h2 className="dashboard-title">Dashboard Overview</h2>
          <p className="dashboard-subtitle">Welcome back! Here's what's happening today.</p>
        </div>
        <button onClick={loadDashboardStats} className="refresh-btn">
          🔄 Refresh
        </button>
      </div>

      {/* Main Stats Grid */}
      <div className="stats-grid">
        {statCards.map((card, index) => (
          <div key={index} className="stat-card" style={{ '--card-color': card.color }}>
            <div className="stat-icon" style={{ background: card.bgColor, color: card.color }}>
              {card.icon}
            </div>
            <div className="stat-content">
              <p className="stat-title">{card.title}</p>
              <h3 className="stat-value">{card.value}</h3>
              {/* <span className="stat-trend positive">{card.trend}</span> */}
            </div>
          </div>
        ))}
      </div>

      {/* Today's Stats */}
      <div className="today-section">
        <h3 className="section-title">Today's Performance</h3>
        <div className="today-stats">
          {todayStats.map((stat, index) => (
            <div key={index} className="today-card">
              <span className="today-icon" style={{ color: stat.color }}>{stat.icon}</span>
              <div className="today-content">
                <p className="today-title">{stat.title}</p>
                <p className="today-value">{stat.value}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Booking Status */}
      <div className="status-section">
        <h3 className="section-title">Booking Status Distribution</h3>
        <div className="status-grid">
          {bookingStatusData.map((item, index) => (
            <div key={index} className="status-card">
              <div className="status-header">
                <span className="status-icon">{item.icon}</span>
                <span className="status-name" style={{ color: item.color }}>
                  {item.status}
                </span>
              </div>
              <p className="status-count">{item.count}</p>
              <div className="status-bar">
                <div 
                  className="status-bar-fill" 
                  style={{ 
                    width: `${(item.count / (stats?.totalBookings || 1)) * 100}%`,
                    background: item.color 
                  }}
                ></div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Quick Actions */}
      <div className="quick-actions">
        <h3 className="section-title">Quick Actions</h3>
        <div className="actions-grid">
          <button className="action-btn" onClick={() => window.location.href = '/admin/bookings'}>
            <span className="action-icon">🎫</span>
            <span>View All Bookings</span>
          </button>
          <button className="action-btn" onClick={() => window.location.href = '/admin/users'}>
            <span className="action-icon">👥</span>
            <span>Manage Users</span>
          </button>
          <button className="action-btn" onClick={() => window.location.href = '/admin/flights'}>
            <span className="action-icon">✈️</span>
            <span>Manage Flights</span>
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;

