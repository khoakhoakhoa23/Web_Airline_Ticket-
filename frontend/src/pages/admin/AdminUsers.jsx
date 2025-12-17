import { useState, useEffect } from 'react';
import { getAllUsers, updateUserRole, updateUserStatus } from '../../services/adminService';
import './AdminUsers.css';

const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadUsers();
  }, [page]);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const data = await getAllUsers(page, 20);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
      setError(null);
    } catch (err) {
      console.error('Failed to load users:', err);
      setError('Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  const handleChangeRole = async (userId, currentRole) => {
    const newRole = currentRole === 'USER' ? 'ADMIN' : 'USER';
    if (!confirm(`Thay đổi quyền user thành ${newRole}?`)) return;

    try {
      await updateUserRole(userId, newRole);
      alert('Cập nhật quyền thành công!');
      loadUsers();
    } catch (err) {
      console.error('Failed to update role:', err);
      alert('Lỗi khi cập nhật quyền: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleToggleStatus = async (userId, currentStatus) => {
    const newStatus = currentStatus === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    const action = newStatus === 'ACTIVE' ? 'Kích hoạt' : 'Vô hiệu hóa';
    if (!confirm(`${action} user này?`)) return;

    try {
      await updateUserStatus(userId, newStatus);
      alert('Cập nhật trạng thái thành công!');
      loadUsers();
    } catch (err) {
      console.error('Failed to update status:', err);
      alert('Lỗi khi cập nhật trạng thái: ' + (err.response?.data?.message || err.message));
    }
  };

  const getRoleBadge = (role) => {
    return (
      <span className={`role-badge ${role.toLowerCase()}`}>
        {role === 'ADMIN' ? '👑' : '👤'} {role}
      </span>
    );
  };

  const getStatusBadge = (status) => {
    return (
      <span className={`status-badge ${status.toLowerCase()}`}>
        {status === 'ACTIVE' ? '✅' : '🚫'} {status}
      </span>
    );
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  if (loading && users.length === 0) {
    return (
      <div className="admin-users">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Đang tải danh sách users...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-users">
      {/* Header */}
      <div className="page-header">
        <div>
          <h2 className="page-title">Quản Lý Users</h2>
          <p className="page-subtitle">Quản lý người dùng và phân quyền hệ thống</p>
        </div>
        <button onClick={loadUsers} className="refresh-btn">
          🔄 Làm mới
        </button>
      </div>

      {/* Stats Summary */}
      <div className="users-stats">
        <div className="stat-card">
          <span className="stat-icon">👥</span>
          <div>
            <p className="stat-label">Tổng Users</p>
            <p className="stat-value">{users.length}</p>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-icon">👑</span>
          <div>
            <p className="stat-label">Admins</p>
            <p className="stat-value">{users.filter(u => u.role === 'ADMIN').length}</p>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-icon">✅</span>
          <div>
            <p className="stat-label">Active Users</p>
            <p className="stat-value">{users.filter(u => u.status === 'ACTIVE').length}</p>
          </div>
        </div>
      </div>

      {error && (
        <div className="error-message">
          <span className="error-icon">⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {/* Users Table */}
      <div className="table-container">
        <table className="users-table">
          <thead>
            <tr>
              <th>Email</th>
              <th>Quyền</th>
              <th>Trạng thái</th>
              <th>Số điện thoại</th>
              <th>Ngày tạo</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {users.map((user) => (
              <tr key={user.id}>
                <td>
                  <div className="user-cell">
                    <span className="user-icon">
                      {user.role === 'ADMIN' ? '👑' : '👤'}
                    </span>
                    <span className="user-email">{user.email}</span>
                  </div>
                </td>
                <td>{getRoleBadge(user.role)}</td>
                <td>{getStatusBadge(user.status)}</td>
                <td>
                  <span className="phone-number">{user.phone || 'Chưa có'}</span>
                </td>
                <td>{formatDate(user.createdAt)}</td>
                <td>
                  <div className="actions">
                    <button 
                      onClick={() => handleChangeRole(user.id, user.role)}
                      className="action-btn role"
                      title={`Đổi thành ${user.role === 'USER' ? 'ADMIN' : 'USER'}`}
                    >
                      👑
                    </button>
                    <button 
                      onClick={() => handleToggleStatus(user.id, user.status)}
                      className={`action-btn status ${user.status === 'ACTIVE' ? 'active' : 'inactive'}`}
                      title={user.status === 'ACTIVE' ? 'Vô hiệu hóa' : 'Kích hoạt'}
                    >
                      {user.status === 'ACTIVE' ? '🚫' : '✅'}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {users.length === 0 && !loading && (
          <div className="empty-state">
            <span className="empty-icon">👤</span>
            <p>Không tìm thấy user nào</p>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button 
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="page-btn"
          >
            ← Trước
          </button>
          <span className="page-info">
            Trang {page + 1} / {totalPages}
          </span>
          <button 
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="page-btn"
          >
            Sau →
          </button>
        </div>
      )}
    </div>
  );
};

export default AdminUsers;

