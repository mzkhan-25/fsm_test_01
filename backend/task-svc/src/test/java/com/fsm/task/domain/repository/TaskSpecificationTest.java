package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TaskSpecification with actual database operations.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TaskSpecificationTest {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        
        // Create test tasks
        taskRepository.save(ServiceTask.builder()
                .title("HVAC Repair")
                .clientAddress("123 Main St, Springfield")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build());
        
        taskRepository.save(ServiceTask.builder()
                .title("Plumbing Fix")
                .clientAddress("456 Oak Ave, Chicago")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.ASSIGNED)
                .build());
        
        taskRepository.save(ServiceTask.builder()
                .title("Electrical Inspection")
                .clientAddress("789 Pine St, Springfield")
                .priority(Priority.LOW)
                .status(TaskStatus.COMPLETED)
                .build());
    }
    
    @Test
    void testHasStatusFiltersCorrectly() {
        Specification<ServiceTask> spec = TaskSpecification.hasStatus(TaskStatus.UNASSIGNED);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertEquals(TaskStatus.UNASSIGNED, results.get(0).getStatus());
    }
    
    @Test
    void testHasStatusWithNullReturnsAll() {
        Specification<ServiceTask> spec = TaskSpecification.hasStatus(null);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(3, results.size());
    }
    
    @Test
    void testHasPriorityFiltersCorrectly() {
        Specification<ServiceTask> spec = TaskSpecification.hasPriority(Priority.HIGH);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertEquals(Priority.HIGH, results.get(0).getPriority());
    }
    
    @Test
    void testHasPriorityWithNullReturnsAll() {
        Specification<ServiceTask> spec = TaskSpecification.hasPriority(null);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(3, results.size());
    }
    
    @Test
    void testContainsSearchTermInTitle() {
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm("HVAC");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTitle().contains("HVAC"));
    }
    
    @Test
    void testContainsSearchTermCaseInsensitive() {
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm("hvac");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTitle().toLowerCase().contains("hvac"));
    }
    
    @Test
    void testContainsSearchTermInClientAddress() {
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm("Springfield");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(t -> t.getClientAddress().contains("Springfield")));
    }
    
    @Test
    void testContainsSearchTermById() {
        // Get the first task's ID
        List<ServiceTask> allTasks = taskRepository.findAll();
        Long firstTaskId = allTasks.get(0).getId();
        
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm(firstTaskId.toString());
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertTrue(results.stream().anyMatch(t -> t.getId().equals(firstTaskId)));
    }
    
    @Test
    void testContainsSearchTermWithNullReturnsAll() {
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm(null);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(3, results.size());
    }
    
    @Test
    void testContainsSearchTermWithEmptyStringReturnsAll() {
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm("");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(3, results.size());
    }
    
    @Test
    void testContainsSearchTermWithWhitespaceReturnsAll() {
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm("   ");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(3, results.size());
    }
    
    @Test
    void testWithFiltersNoFilters() {
        Specification<ServiceTask> spec = TaskSpecification.withFilters(null, null, null);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(3, results.size());
    }
    
    @Test
    void testWithFiltersStatusOnly() {
        Specification<ServiceTask> spec = TaskSpecification.withFilters(TaskStatus.ASSIGNED, null, null);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertEquals(TaskStatus.ASSIGNED, results.get(0).getStatus());
    }
    
    @Test
    void testWithFiltersPriorityOnly() {
        Specification<ServiceTask> spec = TaskSpecification.withFilters(null, Priority.MEDIUM, null);
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertEquals(Priority.MEDIUM, results.get(0).getPriority());
    }
    
    @Test
    void testWithFiltersSearchOnly() {
        Specification<ServiceTask> spec = TaskSpecification.withFilters(null, null, "Plumbing");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTitle().contains("Plumbing"));
    }
    
    @Test
    void testWithFiltersCombined() {
        // Add another HIGH priority UNASSIGNED task
        taskRepository.save(ServiceTask.builder()
                .title("Urgent HVAC Emergency")
                .clientAddress("999 Emergency Ln")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build());
        
        Specification<ServiceTask> spec = TaskSpecification.withFilters(
                TaskStatus.UNASSIGNED, Priority.HIGH, "HVAC"
        );
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(t -> 
                t.getStatus() == TaskStatus.UNASSIGNED && 
                t.getPriority() == Priority.HIGH &&
                t.getTitle().toLowerCase().contains("hvac")
        ));
    }
    
    @Test
    void testWithFiltersNoMatchingResults() {
        Specification<ServiceTask> spec = TaskSpecification.withFilters(
                TaskStatus.IN_PROGRESS, Priority.HIGH, "nonexistent"
        );
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertTrue(results.isEmpty());
    }
    
    @Test
    void testContainsSearchTermPartialMatch() {
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm("repair");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertEquals(1, results.size());
        assertTrue(results.get(0).getTitle().toLowerCase().contains("repair"));
    }
    
    @Test
    void testSearchInMultipleFields() {
        // Search for "123" - should match the client address "123 Main St"
        Specification<ServiceTask> spec = TaskSpecification.containsSearchTerm("123");
        
        List<ServiceTask> results = taskRepository.findAll(spec);
        
        assertTrue(results.stream().anyMatch(t -> t.getClientAddress().contains("123")));
    }
}
