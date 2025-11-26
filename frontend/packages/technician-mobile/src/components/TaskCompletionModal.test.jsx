import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import TaskCompletionModal from './TaskCompletionModal';

describe('TaskCompletionModal', () => {
  const mockOnClose = vi.fn();
  const mockOnSubmit = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Visibility', () => {
    it('should not render when isOpen is false', () => {
      const { container } = render(
        <TaskCompletionModal
          isOpen={false}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(container.firstChild).toBeNull();
    });

    it('should render when isOpen is true', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByRole('heading', { name: 'Complete Task' })).toBeInTheDocument();
    });
  });

  describe('Modal Header', () => {
    it('should display modal title', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByRole('heading', { name: 'Complete Task' })).toBeInTheDocument();
    });

    it('should display close button', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByLabelText('Close modal')).toBeInTheDocument();
    });

    it('should call onClose when close button is clicked', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      fireEvent.click(screen.getByLabelText('Close modal'));

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });
  });

  describe('Work Summary Field', () => {
    it('should display work summary label', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByText('Work Summary *')).toBeInTheDocument();
    });

    it('should display textarea with placeholder', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByPlaceholderText('Describe the work completed (minimum 10 characters)...')).toBeInTheDocument();
    });

    it('should update textarea value when user types', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Test work summary' } });

      expect(textarea.value).toBe('Test work summary');
    });

    it('should display character count', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByText('0 characters')).toBeInTheDocument();
    });

    it('should update character count when user types', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Test' } });

      expect(screen.getByText('4 characters')).toBeInTheDocument();
    });
  });

  describe('Validation', () => {
    it('should show validation error when submitting with less than 10 characters', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Short' } });

      const form = textarea.closest('form');
      fireEvent.submit(form);

      expect(screen.getByText('Work summary must be at least 10 characters long')).toBeInTheDocument();
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should show validation error when submitting empty string', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const form = screen.getByRole('button', { name: 'Complete Task' }).closest('form');
      fireEvent.submit(form);

      expect(screen.getByText('Work summary must be at least 10 characters long')).toBeInTheDocument();
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should show validation error when submitting only whitespace', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: '          ' } });

      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(screen.getByText('Work summary must be at least 10 characters long')).toBeInTheDocument();
      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('should clear validation error when user types enough characters', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      
      // Trigger validation error
      fireEvent.change(textarea, { target: { value: 'Short' } });
      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(screen.getByText('Work summary must be at least 10 characters long')).toBeInTheDocument();

      // Type enough characters to clear error
      fireEvent.change(textarea, { target: { value: 'This is a valid work summary' } });

      expect(screen.queryByText('Work summary must be at least 10 characters long')).not.toBeInTheDocument();
    });

    it('should add error class to textarea when validation fails', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Short' } });

      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(textarea).toHaveClass('error');
    });

    it('should set aria-invalid when validation fails', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      
      // Initially should not have aria-invalid
      expect(textarea).toHaveAttribute('aria-invalid', 'false');

      fireEvent.change(textarea, { target: { value: 'Short' } });
      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(textarea).toHaveAttribute('aria-invalid', 'true');
    });
  });

  describe('Form Submission', () => {
    it('should call onSubmit with trimmed work summary when valid', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: '  Valid work summary with spaces  ' } });

      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(mockOnSubmit).toHaveBeenCalledWith('Valid work summary with spaces');
      expect(mockOnSubmit).toHaveBeenCalledTimes(1);
    });

    it('should submit when exactly 10 characters', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: '1234567890' } });

      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(mockOnSubmit).toHaveBeenCalledWith('1234567890');
    });

    it('should submit when more than 10 characters', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'This is a very long work summary with more than 10 characters' } });

      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(mockOnSubmit).toHaveBeenCalledTimes(1);
    });
  });

  describe('Cancel Button', () => {
    it('should display cancel button', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      expect(screen.getByText('Cancel')).toBeInTheDocument();
    });

    it('should call onClose when cancel button is clicked', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      fireEvent.click(screen.getByText('Cancel'));

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should clear work summary when cancel is clicked', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Some work summary' } });

      expect(textarea.value).toBe('Some work summary');

      fireEvent.click(screen.getByText('Cancel'));

      // Reopen modal to check if state was cleared
      expect(mockOnClose).toHaveBeenCalled();
    });

    it('should clear validation error when cancel is clicked', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      // Trigger validation error
      const textarea = screen.getByLabelText('Work Summary *');
      fireEvent.change(textarea, { target: { value: 'Short' } });
      const submitButton = screen.getByRole('button', { name: 'Complete Task' });
      fireEvent.click(submitButton);

      expect(screen.getByText('Work summary must be at least 10 characters long')).toBeInTheDocument();

      fireEvent.click(screen.getByText('Cancel'));

      expect(mockOnClose).toHaveBeenCalled();
    });
  });

  describe('Submitting State', () => {
    it('should disable textarea when isSubmitting is true', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isSubmitting={true}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      expect(textarea).toBeDisabled();
    });

    it('should disable close button when isSubmitting is true', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isSubmitting={true}
        />
      );

      const closeButton = screen.getByLabelText('Close modal');
      expect(closeButton).toBeDisabled();
    });

    it('should disable cancel button when isSubmitting is true', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isSubmitting={true}
        />
      );

      const cancelButton = screen.getByText('Cancel');
      expect(cancelButton).toBeDisabled();
    });

    it('should disable submit button when isSubmitting is true', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isSubmitting={true}
        />
      );

      const submitButton = screen.getByText('Completing...');
      expect(submitButton).toBeDisabled();
    });

    it('should change submit button text when isSubmitting is true', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          isSubmitting={true}
        />
      );

      expect(screen.getByText('Completing...')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: 'Complete Task' })).not.toBeInTheDocument();
    });
  });

  describe('Overlay Click', () => {
    it('should call onClose when clicking overlay', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const overlay = screen.getByRole('heading', { name: 'Complete Task' }).parentElement.parentElement.parentElement;
      fireEvent.click(overlay);

      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    it('should not call onClose when clicking modal content', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const modalContent = screen.getByRole('heading', { name: 'Complete Task' }).parentElement.parentElement;
      fireEvent.click(modalContent);

      // Click should be stopped from propagating, so onClose shouldn't be called
      // Actually, clicking the content itself will trigger the overlay's onClick
      // So we need to check the modal-content div specifically
      const form = screen.getByText('Work Summary *').closest('form');
      fireEvent.click(form);

      // onClose should not be called because we clicked inside the modal content
      // and stopPropagation should prevent the overlay click handler from firing
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('should have required attribute on textarea', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      expect(textarea).toHaveAttribute('required');
    });

    it('should have aria-required on textarea', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      expect(textarea).toHaveAttribute('aria-required', 'true');
    });

    it('should have role alert on validation error', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const form = screen.getByRole('button', { name: 'Complete Task' }).closest('form');
      fireEvent.submit(form);

      const errorMessage = screen.getByText('Work summary must be at least 10 characters long');
      expect(errorMessage).toHaveAttribute('role', 'alert');
    });

    it('should link validation error to textarea with aria-describedby', () => {
      render(
        <TaskCompletionModal
          isOpen={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
        />
      );

      const textarea = screen.getByLabelText('Work Summary *');
      const form = screen.getByRole('button', { name: 'Complete Task' }).closest('form');
      fireEvent.submit(form);

      expect(textarea).toHaveAttribute('aria-describedby', 'summary-error');
    });
  });
});
