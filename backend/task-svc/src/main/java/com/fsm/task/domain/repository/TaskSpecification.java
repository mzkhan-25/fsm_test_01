package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification builder for dynamic ServiceTask queries.
 * Supports filtering by status, priority, and search term.
 */
public final class TaskSpecification {
    
    private TaskSpecification() {
        // Utility class
    }
    
    /**
     * Creates a specification for filtering tasks by status.
     * 
     * @param status the task status to filter by
     * @return a specification that filters by status, or null if status is null
     */
    public static Specification<ServiceTask> hasStatus(TaskStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }
    
    /**
     * Creates a specification for filtering tasks by priority.
     * 
     * @param priority the task priority to filter by
     * @return a specification that filters by priority, or null if priority is null
     */
    public static Specification<ServiceTask> hasPriority(Priority priority) {
        return (root, query, cb) -> {
            if (priority == null) {
                return null;
            }
            return cb.equal(root.get("priority"), priority);
        };
    }
    
    /**
     * Creates a specification for case-insensitive search across title, id, and client address.
     * 
     * @param searchTerm the search term
     * @return a specification that searches across multiple fields, or null if searchTerm is blank
     */
    public static Specification<ServiceTask> containsSearchTerm(String searchTerm) {
        return (root, query, cb) -> {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return null;
            }
            String lowerSearchTerm = "%" + searchTerm.toLowerCase().trim() + "%";
            
            List<Predicate> predicates = new ArrayList<>();
            
            // Search in title (case-insensitive)
            predicates.add(cb.like(cb.lower(root.get("title")), lowerSearchTerm));
            
            // Search in client address (case-insensitive)
            predicates.add(cb.like(cb.lower(root.get("clientAddress")), lowerSearchTerm));
            
            // Search in id (convert to string for comparison)
            // Try to parse searchTerm as Long for exact ID match
            try {
                Long idValue = Long.parseLong(searchTerm.trim());
                predicates.add(cb.equal(root.get("id"), idValue));
            } catch (NumberFormatException e) {
                // Ignore - searchTerm is not a valid ID
            }
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Combines multiple filters into a single specification.
     * 
     * @param status the task status to filter by (optional)
     * @param priority the task priority to filter by (optional)
     * @param searchTerm the search term for title, id, and client address (optional)
     * @return a combined specification for all filters
     */
    public static Specification<ServiceTask> withFilters(TaskStatus status, Priority priority, String searchTerm) {
        return Specification.where(hasStatus(status))
                .and(hasPriority(priority))
                .and(containsSearchTerm(searchTerm));
    }
}
