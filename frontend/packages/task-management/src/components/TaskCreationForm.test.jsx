import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import TaskCreationForm from './TaskCreationForm';
import * as taskApi from '../services/taskApi';

// Mock the taskApi module
vi.mock('../services/taskApi', () => ({
  createTask: vi.fn(),
  getAddressSuggestions: vi.fn(),
}));

describe('TaskCreationForm', () => {
  let mockOnSuccess;
  let mockOnCancel;

  beforeEach(() => {
    mockOnSuccess = vi.fn();
    mockOnCancel = vi.fn();
    vi.clearAllMocks();
    // Default mock to avoid unhandled promise rejections
    taskApi.getAddressSuggestions.mockResolvedValue([]);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Component Rendering', () => {
    it('renders the form with all required fields', () => {
      render(<TaskCreationForm onSuccess={mockOnSuccess} onCancel={mockOnCancel} />);

      expect(screen.getByText('Create New Task')).toBeInTheDocument();
      expect(screen.getByLabelText(/Title/)).toBeInTheDocument();
      expect(screen.getByLabelText(/Description/)).toBeInTheDocument();
      expect(screen.getByLabelText(/Client Address/)).toBeInTheDocument();
      expect(screen.getByLabelText(/Priority/)).toBeInTheDocument();
      expect(screen.getByLabelText(/Estimated Duration/)).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Create Task/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /Cancel/i })).toBeInTheDocument();
    });

    it('renders input fields with correct attributes', () => {
      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const descriptionInput = screen.getByLabelText(/Description/);
      const addressInput = screen.getByLabelText(/Client Address/);
      const durationInput = screen.getByLabelText(/Estimated Duration/);

      expect(titleInput).toHaveAttribute('type', 'text');
      expect(titleInput).toHaveAttribute('placeholder', 'Enter task title');
      expect(titleInput).toHaveAttribute('maxLength', '200');
      expect(descriptionInput.tagName).toBe('TEXTAREA');
      expect(addressInput).toHaveAttribute('type', 'text');
      expect(addressInput).toHaveAttribute('autocomplete', 'off');
      expect(durationInput).toHaveAttribute('type', 'number');
      expect(durationInput).toHaveAttribute('min', '1');
    });

    it('renders priority dropdown with all options', () => {
      render(<TaskCreationForm />);

      const prioritySelect = screen.getByLabelText(/Priority/);
      expect(prioritySelect).toBeInTheDocument();

      const options = prioritySelect.querySelectorAll('option');
      expect(options).toHaveLength(4);
      expect(options[0]).toHaveValue('LOW');
      expect(options[1]).toHaveValue('MEDIUM');
      expect(options[2]).toHaveValue('HIGH');
      expect(options[3]).toHaveValue('URGENT');
    });

    it('has Medium priority selected by default', () => {
      render(<TaskCreationForm />);

      const prioritySelect = screen.getByLabelText(/Priority/);
      expect(prioritySelect).toHaveValue('MEDIUM');
    });

    it('does not render cancel button when onCancel is not provided', () => {
      render(<TaskCreationForm onSuccess={mockOnSuccess} />);

      expect(screen.queryByRole('button', { name: /Cancel/i })).not.toBeInTheDocument();
    });
  });

  describe('Form Validation', () => {
    it('displays error when title is empty', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Title is required')).toBeInTheDocument();
    });

    it('displays error when title is less than 3 characters', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      await user.type(titleInput, 'ab');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Title must be at least 3 characters')).toBeInTheDocument();
    });

    it('displays error when client address is empty', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Client address is required')).toBeInTheDocument();
    });

    it('displays error when estimated duration is not positive', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);
      const durationInput = screen.getByLabelText(/Estimated Duration/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');
      await user.type(durationInput, '-5');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Estimated duration must be a positive number')).toBeInTheDocument();
    });

    it('displays error when estimated duration is zero', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);
      const durationInput = screen.getByLabelText(/Estimated Duration/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');
      await user.type(durationInput, '0');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Estimated duration must be a positive number')).toBeInTheDocument();
    });

    it('clears title error when user starts typing', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Title is required')).toBeInTheDocument();

      const titleInput = screen.getByLabelText(/Title/);
      await user.type(titleInput, 'T');

      expect(screen.queryByText('Title is required')).not.toBeInTheDocument();
    });

    it('clears address error when user starts typing', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Client address is required')).toBeInTheDocument();

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '1');

      expect(screen.queryByText('Client address is required')).not.toBeInTheDocument();
    });

    it('does not submit form when validation fails', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(taskApi.createTask).not.toHaveBeenCalled();
    });
  });

  describe('Form Submission', () => {
    it('submits form with correct data on successful validation', async () => {
      const user = userEvent.setup();
      taskApi.createTask.mockResolvedValueOnce({ id: 1 });

      render(<TaskCreationForm onSuccess={mockOnSuccess} />);

      const titleInput = screen.getByLabelText(/Title/);
      const descriptionInput = screen.getByLabelText(/Description/);
      const addressInput = screen.getByLabelText(/Client Address/);
      const prioritySelect = screen.getByLabelText(/Priority/);
      const durationInput = screen.getByLabelText(/Estimated Duration/);

      await user.type(titleInput, 'Test Task');
      await user.type(descriptionInput, 'Test description');
      await user.type(addressInput, '123 Main St');
      await user.selectOptions(prioritySelect, 'HIGH');
      await user.type(durationInput, '60');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(taskApi.createTask).toHaveBeenCalledWith({
          title: 'Test Task',
          description: 'Test description',
          clientAddress: '123 Main St',
          priority: 'HIGH',
          estimatedDuration: 60,
        });
      });
    });

    it('sends null for optional fields when empty', async () => {
      const user = userEvent.setup();
      taskApi.createTask.mockResolvedValueOnce({ id: 1 });

      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(taskApi.createTask).toHaveBeenCalledWith(
          expect.objectContaining({
            description: null,
            estimatedDuration: null,
          })
        );
      });
    });

    it('displays success message on successful submission', async () => {
      const user = userEvent.setup();
      taskApi.createTask.mockResolvedValueOnce({ id: 1 });

      render(<TaskCreationForm onSuccess={mockOnSuccess} />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Task created successfully!')).toBeInTheDocument();
    });

    it('resets form after successful submission', async () => {
      const user = userEvent.setup();
      taskApi.createTask.mockResolvedValueOnce({ id: 1 });

      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(titleInput).toHaveValue('');
        expect(addressInput).toHaveValue('');
      });
    });

    it('displays error message on failed submission', async () => {
      const user = userEvent.setup();
      taskApi.createTask.mockRejectedValueOnce(new Error('Server error'));

      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Server error')).toBeInTheDocument();
    });

    it('displays generic error message when error has no message', async () => {
      const user = userEvent.setup();
      taskApi.createTask.mockRejectedValueOnce(new Error());

      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(await screen.findByText('Failed to create task. Please try again.')).toBeInTheDocument();
    });
  });

  describe('Loading State', () => {
    it('shows loading state while submitting', async () => {
      const user = userEvent.setup();
      let resolvePromise;
      taskApi.createTask.mockImplementation(() => new Promise((resolve) => {
        resolvePromise = resolve;
      }));

      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      expect(submitButton).toBeDisabled();
      expect(submitButton).toHaveTextContent('Creating...');

      // Resolve the promise
      await act(async () => {
        resolvePromise({ id: 1 });
      });

      await waitFor(() => {
        expect(submitButton).not.toBeDisabled();
      });
    });

    it('disables cancel button while loading', async () => {
      const user = userEvent.setup();
      let resolvePromise;
      taskApi.createTask.mockImplementation(() => new Promise((resolve) => {
        resolvePromise = resolve;
      }));

      render(<TaskCreationForm onCancel={mockOnCancel} />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      const cancelButton = screen.getByRole('button', { name: /Cancel/i });
      expect(cancelButton).toBeDisabled();

      // Resolve the promise
      await act(async () => {
        resolvePromise({ id: 1 });
      });
    });
  });

  describe('Cancel Button', () => {
    it('calls onCancel when cancel button is clicked', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm onCancel={mockOnCancel} />);

      const cancelButton = screen.getByRole('button', { name: /Cancel/i });
      await user.click(cancelButton);

      expect(mockOnCancel).toHaveBeenCalled();
    });
  });

  describe('Address Autocomplete', () => {
    const mockSuggestions = [
      {
        formattedAddress: '123 Main St, Springfield, IL 62701',
        latitude: 39.7817,
        longitude: -89.6501,
        placeId: 'place-1',
      },
      {
        formattedAddress: '123 Main Ave, Chicago, IL 60601',
        latitude: 41.8781,
        longitude: -87.6298,
        placeId: 'place-2',
      },
    ];

    it('fetches suggestions when typing in address field', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockResolvedValue(mockSuggestions);

      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '123 Main');

      // Wait for debounce and API call
      await waitFor(() => {
        expect(taskApi.getAddressSuggestions).toHaveBeenCalledWith('123 Main');
      }, { timeout: 500 });
    });

    it('displays suggestions dropdown', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockResolvedValue(mockSuggestions);

      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '123 Main');

      await waitFor(() => {
        expect(screen.getByText('123 Main St, Springfield, IL 62701')).toBeInTheDocument();
        expect(screen.getByText('123 Main Ave, Chicago, IL 60601')).toBeInTheDocument();
      }, { timeout: 500 });
    });

    it('selects suggestion when clicked', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockResolvedValue(mockSuggestions);

      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '123 Main');

      await waitFor(() => {
        expect(screen.getByText('123 Main St, Springfield, IL 62701')).toBeInTheDocument();
      }, { timeout: 500 });

      await user.click(screen.getByText('123 Main St, Springfield, IL 62701'));

      expect(addressInput).toHaveValue('123 Main St, Springfield, IL 62701');
      expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
    });

    it('navigates suggestions with keyboard', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockResolvedValue(mockSuggestions);

      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '123 Main');

      await waitFor(() => {
        expect(screen.getByText('123 Main St, Springfield, IL 62701')).toBeInTheDocument();
      }, { timeout: 500 });

      // Navigate down
      fireEvent.keyDown(addressInput, { key: 'ArrowDown' });
      
      await waitFor(() => {
        const firstSuggestion = screen.getByText('123 Main St, Springfield, IL 62701');
        expect(firstSuggestion.closest('li')).toHaveClass('selected');
      });

      // Navigate down again
      fireEvent.keyDown(addressInput, { key: 'ArrowDown' });
      
      await waitFor(() => {
        const secondSuggestion = screen.getByText('123 Main Ave, Chicago, IL 60601');
        expect(secondSuggestion.closest('li')).toHaveClass('selected');
      });

      // Navigate up
      fireEvent.keyDown(addressInput, { key: 'ArrowUp' });
      
      await waitFor(() => {
        const firstSuggestion = screen.getByText('123 Main St, Springfield, IL 62701');
        expect(firstSuggestion.closest('li')).toHaveClass('selected');
      });
    });

    it('selects suggestion with Enter key', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockResolvedValue(mockSuggestions);

      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '123 Main');

      await waitFor(() => {
        expect(screen.getByText('123 Main St, Springfield, IL 62701')).toBeInTheDocument();
      }, { timeout: 500 });

      fireEvent.keyDown(addressInput, { key: 'ArrowDown' });
      fireEvent.keyDown(addressInput, { key: 'Enter' });

      expect(addressInput).toHaveValue('123 Main St, Springfield, IL 62701');
    });

    it('closes suggestions with Escape key', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockResolvedValue(mockSuggestions);

      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '123 Main');

      await waitFor(() => {
        expect(screen.getByRole('listbox')).toBeInTheDocument();
      }, { timeout: 500 });

      fireEvent.keyDown(addressInput, { key: 'Escape' });

      expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
    });

    it('does not fetch suggestions for queries less than 3 characters', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockClear();
      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      await user.type(addressInput, '12');

      // Wait a bit to ensure no call is made
      await new Promise(resolve => setTimeout(resolve, 400));

      expect(taskApi.getAddressSuggestions).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('marks invalid fields with aria-invalid', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await waitFor(() => {
        expect(titleInput).toHaveAttribute('aria-invalid', 'true');
        expect(addressInput).toHaveAttribute('aria-invalid', 'true');
      });
    });

    it('provides aria-describedby for error messages', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await waitFor(() => {
        expect(titleInput).toHaveAttribute('aria-describedby', 'title-error');
        expect(addressInput).toHaveAttribute('aria-describedby', 'clientAddress-error');
      });
    });

    it('uses role="alert" for error messages', async () => {
      const user = userEvent.setup();
      render(<TaskCreationForm />);

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      const titleError = await screen.findByText('Title is required');
      const addressError = await screen.findByText('Client address is required');

      expect(titleError).toHaveAttribute('role', 'alert');
      expect(addressError).toHaveAttribute('role', 'alert');
    });

    it('uses role="status" for success message', async () => {
      const user = userEvent.setup();
      taskApi.createTask.mockResolvedValueOnce({ id: 1 });

      render(<TaskCreationForm />);

      const titleInput = screen.getByLabelText(/Title/);
      const addressInput = screen.getByLabelText(/Client Address/);

      await user.type(titleInput, 'Test Task');
      await user.type(addressInput, '123 Main St');

      const submitButton = screen.getByRole('button', { name: /Create Task/i });
      await user.click(submitButton);

      const successMessage = await screen.findByText('Task created successfully!');
      expect(successMessage).toHaveAttribute('role', 'status');
    });

    it('has proper aria attributes for address autocomplete', async () => {
      const user = userEvent.setup();
      taskApi.getAddressSuggestions.mockResolvedValue([
        { formattedAddress: 'Test Address', placeId: 'test-1', latitude: 0, longitude: 0 },
      ]);

      render(<TaskCreationForm />);

      const addressInput = screen.getByLabelText(/Client Address/);
      expect(addressInput).toHaveAttribute('aria-autocomplete', 'list');
      expect(addressInput).toHaveAttribute('aria-controls', 'address-suggestions-list');

      await user.type(addressInput, '123 Main');

      await waitFor(() => {
        const listbox = screen.getByRole('listbox');
        expect(listbox).toBeInTheDocument();
        expect(listbox).toHaveAttribute('id', 'address-suggestions-list');
      }, { timeout: 500 });
    });
  });
});
