import { useState, useEffect, useRef, useCallback } from 'react';
import { getTasks } from '../services/taskApi';
import './TaskListView.css';

const STATUS_OPTIONS = [
  { value: '', label: 'All' },
  { value: 'UNASSIGNED', label: 'Unassigned' },
  { value: 'ASSIGNED', label: 'Assigned' },
  { value: 'IN_PROGRESS', label: 'In Progress' },
  { value: 'COMPLETED', label: 'Completed' },
];

const SORTABLE_COLUMNS = ['priority', 'createdAt', 'status'];

const TaskListView = ({ onCreateTask }) => {
  const [tasks, setTasks] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Filtering
  const [statusFilter, setStatusFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  
  // Sorting
  const [sortBy, setSortBy] = useState('priority');
  const [sortOrder, setSortOrder] = useState('desc');
  
  // Pagination
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  
  // Status counts
  const [statusCounts, setStatusCounts] = useState({});
  
  const debounceTimeoutRef = useRef(null);
  const searchInputRef = useRef(null);

  // Fetch tasks
  const fetchTasks = useCallback(async (search = searchQuery, status = statusFilter) => {
    setIsLoading(true);
    setError('');
    
    try {
      const response = await getTasks({
        status: status || undefined,
        search: search || undefined,
        sortBy,
        sortOrder,
        page,
        pageSize,
      });
      
      setTasks(response.tasks || []);
      setTotalPages(response.totalPages || 0);
      setTotalElements(response.totalElements || 0);
      setStatusCounts(response.statusCounts || {});
    } catch (err) {
      setError(err.message || 'Failed to fetch tasks');
      setTasks([]);
    } finally {
      setIsLoading(false);
    }
  }, [sortBy, sortOrder, page, pageSize, searchQuery, statusFilter]);

  // Debounced search
  const debouncedSearch = useCallback((value) => {
    if (debounceTimeoutRef.current) {
      clearTimeout(debounceTimeoutRef.current);
    }
    
    debounceTimeoutRef.current = setTimeout(() => {
      setPage(0); // Reset to first page when searching
      fetchTasks(value, statusFilter);
    }, 300);
  }, [statusFilter, fetchTasks]);

  // Cleanup debounce timeout on unmount
  useEffect(() => {
    return () => {
      if (debounceTimeoutRef.current) {
        clearTimeout(debounceTimeoutRef.current);
      }
    };
  }, []);

  // Initial load and when pagination/sorting changes
  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  const handleSearchChange = (e) => {
    const value = e.target.value;
    setSearchQuery(value);
    debouncedSearch(value);
  };

  const handleStatusChange = (e) => {
    const value = e.target.value;
    setStatusFilter(value);
    setPage(0); // Reset to first page when filtering
    // Immediately fetch with new status
    if (debounceTimeoutRef.current) {
      clearTimeout(debounceTimeoutRef.current);
    }
    fetchTasks(searchQuery, value);
  };

  const handleSort = (column) => {
    if (!SORTABLE_COLUMNS.includes(column)) return;
    
    if (sortBy === column) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortOrder('desc');
    }
    setPage(0);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  const getStatusLabel = (status) => {
    const option = STATUS_OPTIONS.find(opt => opt.value === status);
    return option ? option.label : status;
  };

  const getPriorityClass = (priority) => {
    switch (priority) {
      case 'URGENT': return 'priority-urgent';
      case 'HIGH': return 'priority-high';
      case 'MEDIUM': return 'priority-medium';
      case 'LOW': return 'priority-low';
      default: return '';
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'COMPLETED': return 'status-completed';
      case 'IN_PROGRESS': return 'status-in-progress';
      case 'ASSIGNED': return 'status-assigned';
      case 'UNASSIGNED': return 'status-unassigned';
      default: return '';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return '-';
    return new Date(dateString).toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getSortIcon = (column) => {
    if (sortBy !== column) return '↕';
    return sortOrder === 'asc' ? '↑' : '↓';
  };

  const getStatusCountLabel = (statusValue) => {
    const count = statusCounts[statusValue] || 0;
    return `(${count})`;
  };

  const renderPagination = () => {
    if (totalPages <= 1) return null;

    const pages = [];
    const maxVisiblePages = 5;
    let startPage = Math.max(0, page - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
    
    if (endPage - startPage + 1 < maxVisiblePages) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return (
      <div className="pagination" role="navigation" aria-label="Pagination">
        <button
          className="pagination-button"
          onClick={() => handlePageChange(page - 1)}
          disabled={page === 0}
          aria-label="Previous page"
        >
          Previous
        </button>
        
        {startPage > 0 && (
          <>
            <button
              className="pagination-button page-number"
              onClick={() => handlePageChange(0)}
              aria-label="Go to page 1"
            >
              1
            </button>
            {startPage > 1 && <span className="pagination-ellipsis">...</span>}
          </>
        )}
        
        {pages.map((pageNum) => (
          <button
            key={pageNum}
            className={`pagination-button page-number ${page === pageNum ? 'active' : ''}`}
            onClick={() => handlePageChange(pageNum)}
            aria-label={`Go to page ${pageNum + 1}`}
            aria-current={page === pageNum ? 'page' : undefined}
          >
            {pageNum + 1}
          </button>
        ))}
        
        {endPage < totalPages - 1 && (
          <>
            {endPage < totalPages - 2 && <span className="pagination-ellipsis">...</span>}
            <button
              className="pagination-button page-number"
              onClick={() => handlePageChange(totalPages - 1)}
              aria-label={`Go to page ${totalPages}`}
            >
              {totalPages}
            </button>
          </>
        )}
        
        <button
          className="pagination-button"
          onClick={() => handlePageChange(page + 1)}
          disabled={page >= totalPages - 1}
          aria-label="Next page"
        >
          Next
        </button>
      </div>
    );
  };

  return (
    <div className="task-list-page">
      <div className="task-list-container">
        <div className="task-list-header">
          <h1 className="task-list-title">Task List</h1>
          {onCreateTask && (
            <button className="create-task-button" onClick={onCreateTask}>
              + Create Task
            </button>
          )}
        </div>

        {error && (
          <div className="error-banner" role="alert">
            {error}
          </div>
        )}

        <div className="task-list-controls">
          <div className="search-container">
            <input
              ref={searchInputRef}
              type="text"
              className="search-input"
              placeholder="Search tasks..."
              value={searchQuery}
              onChange={handleSearchChange}
              aria-label="Search tasks"
            />
          </div>

          <div className="filter-container">
            <label htmlFor="status-filter" className="filter-label">
              Status:
            </label>
            <select
              id="status-filter"
              className="status-filter"
              value={statusFilter}
              onChange={handleStatusChange}
              aria-label="Filter by status"
            >
              {STATUS_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label} {option.value ? getStatusCountLabel(option.value) : ''}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="task-list-info">
          Showing {tasks.length} of {totalElements} tasks
        </div>

        {isLoading ? (
          <div className="loading-container" role="status" aria-live="polite">
            <div className="loading-spinner"></div>
            <span>Loading tasks...</span>
          </div>
        ) : tasks.length === 0 ? (
          <div className="empty-state">
            <p>No tasks found.</p>
            {(searchQuery || statusFilter) && (
              <p className="empty-state-hint">Try adjusting your search or filter criteria.</p>
            )}
          </div>
        ) : (
          <div className="table-wrapper">
            <table className="task-table" aria-label="Task list">
              <thead>
                <tr>
                  <th className="th-id">ID</th>
                  <th className="th-title">Title</th>
                  <th className="th-technician">Technician</th>
                  <th 
                    className={`th-status sortable ${sortBy === 'status' ? 'sorted' : ''}`}
                    onClick={() => handleSort('status')}
                    role="columnheader"
                    aria-sort={sortBy === 'status' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'}
                  >
                    Status <span className="sort-icon">{getSortIcon('status')}</span>
                  </th>
                  <th 
                    className={`th-priority sortable ${sortBy === 'priority' ? 'sorted' : ''}`}
                    onClick={() => handleSort('priority')}
                    role="columnheader"
                    aria-sort={sortBy === 'priority' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'}
                  >
                    Priority <span className="sort-icon">{getSortIcon('priority')}</span>
                  </th>
                  <th className="th-address">Client Address</th>
                  <th 
                    className={`th-created sortable ${sortBy === 'createdAt' ? 'sorted' : ''}`}
                    onClick={() => handleSort('createdAt')}
                    role="columnheader"
                    aria-sort={sortBy === 'createdAt' ? (sortOrder === 'asc' ? 'ascending' : 'descending') : 'none'}
                  >
                    Created <span className="sort-icon">{getSortIcon('createdAt')}</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {tasks.map((task) => (
                  <tr key={task.id} className="task-row">
                    <td className="td-id">{task.id}</td>
                    <td className="td-title" title={task.title}>{task.title}</td>
                    <td className="td-technician">{task.assignedTechnician || '-'}</td>
                    <td className="td-status">
                      <span className={`status-badge ${getStatusClass(task.status)}`}>
                        {getStatusLabel(task.status)}
                      </span>
                    </td>
                    <td className="td-priority">
                      <span className={`priority-badge ${getPriorityClass(task.priority)}`}>
                        {task.priority}
                      </span>
                    </td>
                    <td className="td-address" title={task.clientAddress}>{task.clientAddress}</td>
                    <td className="td-created">{formatDate(task.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {!isLoading && tasks.length > 0 && renderPagination()}
      </div>
    </div>
  );
};

export default TaskListView;
