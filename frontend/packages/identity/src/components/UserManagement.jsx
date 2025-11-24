import { useState, useEffect } from 'react';
import * as userApi from '../services/userApi';
import './UserManagement.css';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('create'); // 'create' or 'edit'
  const [currentUser, setCurrentUser] = useState(null);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [userToDeactivate, setUserToDeactivate] = useState(null);
  
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    password: '',
    role: 'TECHNICIAN',
  });
  
  const [formErrors, setFormErrors] = useState({});

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setIsLoading(true);
      setError('');
      const data = await userApi.getAllUsers();
      setUsers(data);
    } catch (err) {
      setError(err.message || 'Failed to load users');
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddUser = () => {
    setModalMode('create');
    setCurrentUser(null);
    setFormData({
      name: '',
      email: '',
      phone: '',
      password: '',
      role: 'TECHNICIAN',
    });
    setFormErrors({});
    setShowModal(true);
  };

  const handleEditUser = (user) => {
    setModalMode('edit');
    setCurrentUser(user);
    setFormData({
      name: user.name,
      email: user.email,
      phone: user.phone || '',
      password: '',
      role: user.role,
    });
    setFormErrors({});
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setCurrentUser(null);
    setFormData({
      name: '',
      email: '',
      phone: '',
      password: '',
      role: 'TECHNICIAN',
    });
    setFormErrors({});
  };

  const validateForm = () => {
    const errors = {};
    
    if (!formData.name.trim()) {
      errors.name = 'Name is required';
    }
    
    if (!formData.email.trim()) {
      errors.email = 'Email is required';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = 'Email must be valid';
    }
    
    if (formData.phone && !/^\+?[1-9]\d{0,14}$/.test(formData.phone)) {
      errors.phone = 'Phone number must be in E.164 format (e.g., +12025551234)';
    }
    
    if (modalMode === 'create' && !formData.password.trim()) {
      errors.password = 'Password is required';
    }
    
    if (!formData.role) {
      errors.role = 'Role is required';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    
    if (!validateForm()) {
      return;
    }
    
    try {
      if (modalMode === 'create') {
        await userApi.createUser(formData);
        setSuccessMessage('User created successfully');
      } else {
        const updateData = {
          name: formData.name,
          email: formData.email,
          phone: formData.phone,
          role: formData.role,
        };
        if (formData.password.trim()) {
          updateData.password = formData.password;
        }
        await userApi.updateUser(currentUser.id, updateData);
        setSuccessMessage('User updated successfully');
      }
      
      handleCloseModal();
      await loadUsers();
    } catch (err) {
      setError(err.message || 'Operation failed');
    }
  };

  const handleDeactivateClick = (user) => {
    setUserToDeactivate(user);
    setShowConfirmDialog(true);
  };

  const handleConfirmDeactivate = async () => {
    if (!userToDeactivate) return;
    
    try {
      setError('');
      setSuccessMessage('');
      await userApi.deactivateUser(userToDeactivate.id);
      setSuccessMessage(`User ${userToDeactivate.name} deactivated successfully`);
      setShowConfirmDialog(false);
      setUserToDeactivate(null);
      await loadUsers();
    } catch (err) {
      setError(err.message || 'Failed to deactivate user');
      setShowConfirmDialog(false);
      setUserToDeactivate(null);
    }
  };

  const handleCancelDeactivate = () => {
    setShowConfirmDialog(false);
    setUserToDeactivate(null);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (formErrors[name]) {
      setFormErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  if (isLoading) {
    return (
      <div className="user-management">
        <div className="loading">Loading users...</div>
      </div>
    );
  }

  return (
    <div className="user-management">
      <div className="header">
        <h1>User Management</h1>
        <button className="add-button" onClick={handleAddUser}>
          Add User
        </button>
      </div>

      {error && (
        <div className="message error-message" role="alert">
          {error}
        </div>
      )}

      {successMessage && (
        <div className="message success-message" role="alert">
          {successMessage}
        </div>
      )}

      <div className="user-list">
        <table>
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Phone</th>
              <th>Role</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.length === 0 ? (
              <tr>
                <td colSpan="6" className="no-users">No users found</td>
              </tr>
            ) : (
              users.map(user => (
                <tr key={user.id}>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td>{user.phone || '-'}</td>
                  <td>{user.role}</td>
                  <td>
                    <span className={`status-badge ${user.status.toLowerCase()}`}>
                      {user.status}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      <button 
                        className="edit-button"
                        onClick={() => handleEditUser(user)}
                      >
                        Edit
                      </button>
                      {user.status === 'ACTIVE' && (
                        <button 
                          className="deactivate-button"
                          onClick={() => handleDeactivateClick(user)}
                        >
                          Deactivate
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{modalMode === 'create' ? 'Add User' : 'Edit User'}</h2>
              <button className="close-button" onClick={handleCloseModal}>×</button>
            </div>
            
            <form className="user-form" onSubmit={handleSubmit}>
              <div className="form-group">
                <label htmlFor="name">Name *</label>
                <input
                  id="name"
                  name="name"
                  type="text"
                  value={formData.name}
                  onChange={handleInputChange}
                  className={formErrors.name ? 'input-error' : ''}
                  aria-invalid={formErrors.name ? 'true' : 'false'}
                  aria-describedby={formErrors.name ? 'name-error' : undefined}
                />
                {formErrors.name && (
                  <span id="name-error" className="error-text" role="alert">
                    {formErrors.name}
                  </span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="email">Email *</label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleInputChange}
                  className={formErrors.email ? 'input-error' : ''}
                  aria-invalid={formErrors.email ? 'true' : 'false'}
                  aria-describedby={formErrors.email ? 'email-error' : undefined}
                />
                {formErrors.email && (
                  <span id="email-error" className="error-text" role="alert">
                    {formErrors.email}
                  </span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="phone">Phone</label>
                <input
                  id="phone"
                  name="phone"
                  type="text"
                  value={formData.phone}
                  onChange={handleInputChange}
                  placeholder="+12025551234"
                  className={formErrors.phone ? 'input-error' : ''}
                  aria-invalid={formErrors.phone ? 'true' : 'false'}
                  aria-describedby={formErrors.phone ? 'phone-error' : undefined}
                />
                {formErrors.phone && (
                  <span id="phone-error" className="error-text" role="alert">
                    {formErrors.phone}
                  </span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="role">Role *</label>
                <select
                  id="role"
                  name="role"
                  value={formData.role}
                  onChange={handleInputChange}
                  className={formErrors.role ? 'input-error' : ''}
                  aria-invalid={formErrors.role ? 'true' : 'false'}
                  aria-describedby={formErrors.role ? 'role-error' : undefined}
                >
                  <option value="ADMIN">ADMIN</option>
                  <option value="SUPERVISOR">SUPERVISOR</option>
                  <option value="DISPATCHER">DISPATCHER</option>
                  <option value="TECHNICIAN">TECHNICIAN</option>
                </select>
                {formErrors.role && (
                  <span id="role-error" className="error-text" role="alert">
                    {formErrors.role}
                  </span>
                )}
              </div>

              <div className="form-group">
                <label htmlFor="password">
                  Password {modalMode === 'create' ? '*' : '(leave blank to keep current)'}
                </label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  className={formErrors.password ? 'input-error' : ''}
                  aria-invalid={formErrors.password ? 'true' : 'false'}
                  aria-describedby={formErrors.password ? 'password-error' : undefined}
                />
                {formErrors.password && (
                  <span id="password-error" className="error-text" role="alert">
                    {formErrors.password}
                  </span>
                )}
              </div>

              <div className="form-actions">
                <button type="button" className="cancel-button" onClick={handleCloseModal}>
                  Cancel
                </button>
                <button type="submit" className="submit-button">
                  {modalMode === 'create' ? 'Create User' : 'Update User'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showConfirmDialog && (
        <div className="modal-overlay" onClick={handleCancelDeactivate}>
          <div className="modal confirm-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Confirm Deactivation</h2>
            </div>
            <div className="modal-body">
              <p>
                Are you sure you want to deactivate user <strong>{userToDeactivate?.name}</strong>?
              </p>
              <p className="warning-text">
                This action will set the user status to INACTIVE.
              </p>
            </div>
            <div className="form-actions">
              <button className="cancel-button" onClick={handleCancelDeactivate}>
                Cancel
              </button>
              <button className="deactivate-button" onClick={handleConfirmDeactivate}>
                Deactivate
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;
