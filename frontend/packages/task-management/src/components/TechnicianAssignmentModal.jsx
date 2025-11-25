import { useState, useEffect, useCallback } from 'react';
import { getTechnicians, assignTask } from '../services/taskApi';
import './TechnicianAssignmentModal.css';

const WORKLOAD_THRESHOLD = 10;

/**
 * TechnicianAssignmentModal - Modal dialog for assigning tasks to technicians
 * 
 * @param {Object} props
 * @param {Object} props.task - Task object to assign (requires id and title)
 * @param {boolean} props.isOpen - Whether modal is open
 * @param {Function} props.onClose - Callback when modal closes
 * @param {Function} props.onAssignmentComplete - Callback when assignment succeeds
 */
const TechnicianAssignmentModal = ({ task, isOpen, onClose, onAssignmentComplete }) => {
  const [technicians, setTechnicians] = useState([]);
  const [selectedTechnician, setSelectedTechnician] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isAssigning, setIsAssigning] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // Load technicians when modal opens
  /**
   * Load technicians from the API when modal opens.
   * Note: workload may be undefined in API response if the backend doesn't provide it,
   * so we default to 0 for sorting and display purposes.
   */
  const loadTechnicians = useCallback(async () => {
    if (!isOpen) return;
    
    setIsLoading(true);
    setError('');
    
    try {
      const technicianList = await getTechnicians();
      // Sort by workload (least loaded first)
      const sortedTechnicians = technicianList.map(tech => ({
        ...tech,
        workload: tech.workload || 0,  // Default to 0 if not provided by API
      })).sort((a, b) => a.workload - b.workload);
      
      setTechnicians(sortedTechnicians);
    } catch (err) {
      setError(err.message || 'Failed to load technicians');
    } finally {
      setIsLoading(false);
    }
  }, [isOpen]);

  useEffect(() => {
    if (isOpen) {
      loadTechnicians();
      setSelectedTechnician(null);
      setShowConfirmation(false);
      setError('');
      setSuccessMessage('');
    }
  }, [isOpen, loadTechnicians]);

  // Handle clicking outside modal to close
  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget && !isAssigning) {
      onClose();
    }
  };

  // Handle escape key to close modal
  useEffect(() => {
    const handleEscape = (e) => {
      if (e.key === 'Escape' && isOpen && !isAssigning) {
        onClose();
      }
    };
    
    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isOpen, isAssigning, onClose]);

  // Handle technician selection
  const handleSelectTechnician = (technician) => {
    setSelectedTechnician(technician);
    setError('');
  };

  // Handle proceeding to confirmation
  const handleProceedToConfirm = () => {
    if (selectedTechnician) {
      setShowConfirmation(true);
    }
  };

  // Handle going back from confirmation
  const handleBackToSelection = () => {
    setShowConfirmation(false);
  };

  // Handle assignment confirmation
  const handleConfirmAssignment = async () => {
    if (!selectedTechnician || !task) return;

    setIsAssigning(true);
    setError('');

    try {
      const result = await assignTask(task.id, selectedTechnician.id);
      setSuccessMessage(`Task successfully assigned to ${selectedTechnician.name}`);
      
      // Show success message briefly, then close
      setTimeout(() => {
        if (onAssignmentComplete) {
          onAssignmentComplete(result);
        }
        onClose();
      }, 1500);
    } catch (err) {
      setError(err.message || 'Failed to assign task');
      setShowConfirmation(false);
    } finally {
      setIsAssigning(false);
    }
  };

  if (!isOpen) return null;

  const isAtCapacity = (technician) => technician.workload >= WORKLOAD_THRESHOLD;

  return (
    <div 
      className="modal-overlay" 
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-labelledby="modal-title"
    >
      <div className="modal-container">
        <div className="modal-header">
          <h2 id="modal-title" className="modal-title">
            {showConfirmation ? 'Confirm Assignment' : 'Assign Task'}
          </h2>
          <button 
            className="modal-close-button" 
            onClick={onClose}
            disabled={isAssigning}
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        <div className="modal-content">
          {error && (
            <div className="modal-error-banner" role="alert">
              {error}
            </div>
          )}

          {successMessage && (
            <div className="modal-success-banner" role="status">
              {successMessage}
            </div>
          )}

          {isLoading ? (
            <div className="modal-loading" aria-live="polite">
              <div className="spinner" aria-hidden="true"></div>
              <span>Loading technicians...</span>
            </div>
          ) : showConfirmation && selectedTechnician ? (
            <div className="confirmation-view">
              <p className="confirmation-title">
                Are you sure you want to assign this task?
              </p>
              <div className="confirmation-details">
                <p className="confirmation-label">Task</p>
                <p className="confirmation-value">{task?.title}</p>
                <p className="confirmation-label">Technician</p>
                <p className="confirmation-value">{selectedTechnician.name}</p>
                <p className="confirmation-label">Current Workload</p>
                <p className="confirmation-value">
                  {selectedTechnician.workload} active task{selectedTechnician.workload !== 1 ? 's' : ''}
                </p>
                {isAtCapacity(selectedTechnician) && (
                  <div className="confirmation-warning">
                    <span role="img" aria-label="Warning">⚠️</span>
                    This technician is at or exceeds capacity ({WORKLOAD_THRESHOLD}+ tasks)
                  </div>
                )}
              </div>
            </div>
          ) : technicians.length === 0 && !error ? (
            <div className="empty-state">
              <div className="empty-state-icon" aria-hidden="true">👥</div>
              <p className="empty-state-text">No technicians available for assignment</p>
            </div>
          ) : (
            <ul className="technician-list" role="listbox" aria-label="Available technicians">
              {technicians.map((technician) => (
                <li
                  key={technician.id}
                  className={`technician-item ${selectedTechnician?.id === technician.id ? 'selected' : ''} ${isAtCapacity(technician) ? 'at-capacity' : ''}`}
                  onClick={() => handleSelectTechnician(technician)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                      e.preventDefault();
                      handleSelectTechnician(technician);
                    }
                  }}
                  role="option"
                  aria-selected={selectedTechnician?.id === technician.id}
                  tabIndex={0}
                >
                  <div className="technician-info">
                    <p className="technician-name">
                      {technician.name}
                      {isAtCapacity(technician) && (
                        <span className="warning-icon" role="img" aria-label="At capacity warning">⚠️</span>
                      )}
                    </p>
                    <p className="technician-email">{technician.email}</p>
                    {isAtCapacity(technician) && (
                      <div className="capacity-warning">
                        <span role="img" aria-hidden="true">⚠️</span>
                        At capacity ({technician.workload} tasks)
                      </div>
                    )}
                  </div>
                  <div className={`workload-badge ${isAtCapacity(technician) ? 'high-workload' : ''}`}>
                    <span className="workload-count">{technician.workload}</span>
                    <span className="workload-label">Tasks</span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="modal-actions">
          {showConfirmation ? (
            <>
              <button
                className="modal-button modal-button-secondary"
                onClick={handleBackToSelection}
                disabled={isAssigning}
              >
                Back
              </button>
              <button
                className="modal-button modal-button-primary"
                onClick={handleConfirmAssignment}
                disabled={isAssigning}
              >
                {isAssigning ? 'Assigning...' : 'Confirm Assignment'}
              </button>
            </>
          ) : (
            <>
              <button
                className="modal-button modal-button-secondary"
                onClick={onClose}
                disabled={isAssigning}
              >
                Cancel
              </button>
              <button
                className="modal-button modal-button-primary"
                onClick={handleProceedToConfirm}
                disabled={!selectedTechnician || isLoading}
              >
                Continue
              </button>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default TechnicianAssignmentModal;
