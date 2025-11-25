import { useState, useEffect, useRef, useCallback } from 'react';
import { createTask, getAddressSuggestions } from '../services/taskApi';
import './TaskCreationForm.css';

const PRIORITY_OPTIONS = [
  { value: 'LOW', label: 'Low' },
  { value: 'MEDIUM', label: 'Medium' },
  { value: 'HIGH', label: 'High' },
  { value: 'URGENT', label: 'Urgent' },
];

const TaskCreationForm = ({ onSuccess, onCancel }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    clientAddress: '',
    priority: 'MEDIUM',
    estimatedDuration: '',
  });
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  
  // Address autocomplete state
  const [addressSuggestions, setAddressSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [selectedSuggestionIndex, setSelectedSuggestionIndex] = useState(-1);
  
  const addressInputRef = useRef(null);
  const suggestionsRef = useRef(null);
  const debounceTimeoutRef = useRef(null);

  // Debounced address search
  const debouncedFetchSuggestions = useCallback((query) => {
    if (debounceTimeoutRef.current) {
      clearTimeout(debounceTimeoutRef.current);
    }

    if (!query || query.length < 3) {
      setAddressSuggestions([]);
      setShowSuggestions(false);
      return;
    }

    debounceTimeoutRef.current = setTimeout(async () => {
      setIsLoadingSuggestions(true);
      try {
        const suggestions = await getAddressSuggestions(query);
        setAddressSuggestions(suggestions);
        setShowSuggestions(suggestions.length > 0);
        setSelectedSuggestionIndex(-1);
      } catch (error) {
        console.error('Failed to fetch address suggestions:', error);
        setAddressSuggestions([]);
      } finally {
        setIsLoadingSuggestions(false);
      }
    }, 300);
  }, []);

  // Cleanup debounce timeout on unmount
  useEffect(() => {
    return () => {
      if (debounceTimeoutRef.current) {
        clearTimeout(debounceTimeoutRef.current);
      }
    };
  }, []);

  // Close suggestions when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        suggestionsRef.current &&
        !suggestionsRef.current.contains(event.target) &&
        addressInputRef.current &&
        !addressInputRef.current.contains(event.target)
      ) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.title.trim()) {
      newErrors.title = 'Title is required';
    } else if (formData.title.trim().length < 3) {
      newErrors.title = 'Title must be at least 3 characters';
    } else if (formData.title.trim().length > 200) {
      newErrors.title = 'Title must be less than 200 characters';
    }

    if (!formData.clientAddress.trim()) {
      newErrors.clientAddress = 'Client address is required';
    }

    if (!formData.priority) {
      newErrors.priority = 'Priority is required';
    }

    if (formData.estimatedDuration !== '' && formData.estimatedDuration !== null) {
      const duration = Number(formData.estimatedDuration);
      if (isNaN(duration) || duration <= 0) {
        newErrors.estimatedDuration = 'Estimated duration must be a positive number';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      const taskData = {
        title: formData.title.trim(),
        description: formData.description.trim() || null,
        clientAddress: formData.clientAddress.trim(),
        priority: formData.priority,
        estimatedDuration: formData.estimatedDuration ? Number(formData.estimatedDuration) : null,
      };

      await createTask(taskData);
      setSuccessMessage('Task created successfully!');
      
      // Reset form
      setFormData({
        title: '',
        description: '',
        clientAddress: '',
        priority: 'MEDIUM',
        estimatedDuration: '',
      });

      // Call onSuccess callback after short delay for user to see success message
      if (onSuccess) {
        setTimeout(() => {
          onSuccess();
        }, 1500);
      }
    } catch (error) {
      setErrorMessage(error.message || 'Failed to create task. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: '' }));
    }
  };

  const handleAddressChange = (e) => {
    const value = e.target.value;
    handleChange(e);
    debouncedFetchSuggestions(value);
  };

  const handleSuggestionSelect = (suggestion) => {
    setFormData((prev) => ({
      ...prev,
      clientAddress: suggestion.formattedAddress,
    }));
    setShowSuggestions(false);
    setAddressSuggestions([]);
    if (errors.clientAddress) {
      setErrors((prev) => ({ ...prev, clientAddress: '' }));
    }
  };

  const handleAddressKeyDown = (e) => {
    if (!showSuggestions || addressSuggestions.length === 0) return;

    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        setSelectedSuggestionIndex((prev) =>
          prev < addressSuggestions.length - 1 ? prev + 1 : prev
        );
        break;
      case 'ArrowUp':
        e.preventDefault();
        setSelectedSuggestionIndex((prev) => (prev > 0 ? prev - 1 : -1));
        break;
      case 'Enter':
        e.preventDefault();
        if (selectedSuggestionIndex >= 0) {
          handleSuggestionSelect(addressSuggestions[selectedSuggestionIndex]);
        }
        break;
      case 'Escape':
        setShowSuggestions(false);
        break;
    }
  };

  return (
    <div className="task-creation-page">
      <div className="task-creation-container">
        <h1 className="task-creation-title">Create New Task</h1>

        {errorMessage && (
          <div className="error-banner" role="alert">
            {errorMessage}
          </div>
        )}

        {successMessage && (
          <div className="success-banner" role="status">
            {successMessage}
          </div>
        )}

        <form className="task-creation-form" onSubmit={handleSubmit} noValidate>
          {/* Title Field */}
          <div className="form-group">
            <label htmlFor="title" className="form-label">
              Title <span className="required">*</span>
            </label>
            <input
              id="title"
              name="title"
              type="text"
              className={`form-input ${errors.title ? 'input-error' : ''}`}
              value={formData.title}
              onChange={handleChange}
              placeholder="Enter task title"
              aria-invalid={errors.title ? 'true' : 'false'}
              aria-describedby={errors.title ? 'title-error' : undefined}
              maxLength={200}
            />
            {errors.title && (
              <span id="title-error" className="error-text" role="alert">
                {errors.title}
              </span>
            )}
          </div>

          {/* Description Field */}
          <div className="form-group">
            <label htmlFor="description" className="form-label">
              Description
            </label>
            <textarea
              id="description"
              name="description"
              className="form-input form-textarea"
              value={formData.description}
              onChange={handleChange}
              placeholder="Enter task description (optional)"
              rows={4}
            />
          </div>

          {/* Client Address Field with Autocomplete */}
          <div className="form-group address-group">
            <label htmlFor="clientAddress" className="form-label">
              Client Address <span className="required">*</span>
            </label>
            <div className="address-input-container">
              <input
                ref={addressInputRef}
                id="clientAddress"
                name="clientAddress"
                type="text"
                className={`form-input ${errors.clientAddress ? 'input-error' : ''}`}
                value={formData.clientAddress}
                onChange={handleAddressChange}
                onKeyDown={handleAddressKeyDown}
                onFocus={() => {
                  if (addressSuggestions.length > 0) {
                    setShowSuggestions(true);
                  }
                }}
                placeholder="Start typing an address..."
                aria-invalid={errors.clientAddress ? 'true' : 'false'}
                aria-describedby={errors.clientAddress ? 'clientAddress-error' : undefined}
                aria-expanded={showSuggestions}
                aria-autocomplete="list"
                aria-controls="address-suggestions-list"
                autoComplete="off"
              />
              {isLoadingSuggestions && (
                <span className="address-loading" aria-live="polite">
                  Loading...
                </span>
              )}
              {showSuggestions && addressSuggestions.length > 0 && (
                <ul
                  ref={suggestionsRef}
                  id="address-suggestions-list"
                  className="address-suggestions"
                  role="listbox"
                >
                  {addressSuggestions.map((suggestion, index) => (
                    <li
                      key={suggestion.placeId || index}
                      className={`suggestion-item ${index === selectedSuggestionIndex ? 'selected' : ''}`}
                      onClick={() => handleSuggestionSelect(suggestion)}
                      role="option"
                      aria-selected={index === selectedSuggestionIndex}
                    >
                      {suggestion.formattedAddress}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            {errors.clientAddress && (
              <span id="clientAddress-error" className="error-text" role="alert">
                {errors.clientAddress}
              </span>
            )}
          </div>

          {/* Priority Field */}
          <div className="form-group">
            <label htmlFor="priority" className="form-label">
              Priority <span className="required">*</span>
            </label>
            <select
              id="priority"
              name="priority"
              className={`form-input form-select ${errors.priority ? 'input-error' : ''}`}
              value={formData.priority}
              onChange={handleChange}
              aria-invalid={errors.priority ? 'true' : 'false'}
              aria-describedby={errors.priority ? 'priority-error' : undefined}
            >
              {PRIORITY_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            {errors.priority && (
              <span id="priority-error" className="error-text" role="alert">
                {errors.priority}
              </span>
            )}
          </div>

          {/* Estimated Duration Field */}
          <div className="form-group">
            <label htmlFor="estimatedDuration" className="form-label">
              Estimated Duration (minutes)
            </label>
            <input
              id="estimatedDuration"
              name="estimatedDuration"
              type="number"
              min="1"
              className={`form-input ${errors.estimatedDuration ? 'input-error' : ''}`}
              value={formData.estimatedDuration}
              onChange={handleChange}
              placeholder="Enter estimated duration"
              aria-invalid={errors.estimatedDuration ? 'true' : 'false'}
              aria-describedby={errors.estimatedDuration ? 'estimatedDuration-error' : undefined}
            />
            {errors.estimatedDuration && (
              <span id="estimatedDuration-error" className="error-text" role="alert">
                {errors.estimatedDuration}
              </span>
            )}
          </div>

          {/* Form Actions */}
          <div className="form-actions">
            {onCancel && (
              <button
                type="button"
                className="cancel-button"
                onClick={onCancel}
                disabled={isLoading}
              >
                Cancel
              </button>
            )}
            <button
              type="submit"
              className="submit-button"
              disabled={isLoading}
            >
              {isLoading ? 'Creating...' : 'Create Task'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default TaskCreationForm;
