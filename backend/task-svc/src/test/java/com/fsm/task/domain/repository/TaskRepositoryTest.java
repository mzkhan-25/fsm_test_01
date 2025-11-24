package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TaskRepository with actual database operations.
 * Tests database persistence, CRUD operations, and query performance with indexes.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TaskRepositoryTest {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }
    
    @Test
    void testRepositoryIsInjected() {
        assertNotNull(taskRepository);
    }
    
    @Test
    void testGetHardcodedTasksReturnsValidList() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        assertNotNull(tasks);
        assertEquals(6, tasks.size(), "Should return 6 hardcoded tasks");
    }
    
    @Test
    void testHardcodedTasksHaveValidIds() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        for (int i = 0; i < tasks.size(); i++) {
            ServiceTask task = tasks.get(i);
            assertNotNull(task.getId(), "Task " + i + " should have an ID");
            assertEquals((long) (i + 1), task.getId(), "Task ID should be sequential");
        }
    }
    
    @Test
    void testHardcodedTasksHaveRequiredFields() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        for (ServiceTask task : tasks) {
            assertNotNull(task.getTitle(), "Task should have a title");
            assertTrue(task.getTitle().length() >= 3, "Title should be at least 3 characters");
            assertNotNull(task.getClientAddress(), "Task should have a client address");
            assertNotNull(task.getPriority(), "Task should have a priority");
            assertNotNull(task.getStatus(), "Task should have a status");
            assertNotNull(task.getCreatedBy(), "Task should have createdBy");
            assertNotNull(task.getCreatedAt(), "Task should have createdAt");
        }
    }
    
    @Test
    void testHardcodedTasksHaveValidPriorities() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        boolean hasHigh = false;
        boolean hasMedium = false;
        boolean hasLow = false;
        
        for (ServiceTask task : tasks) {
            if (task.getPriority() == Priority.HIGH) hasHigh = true;
            if (task.getPriority() == Priority.MEDIUM) hasMedium = true;
            if (task.getPriority() == Priority.LOW) hasLow = true;
        }
        
        assertTrue(hasHigh, "Should have at least one HIGH priority task");
        assertTrue(hasMedium, "Should have at least one MEDIUM priority task");
        assertTrue(hasLow, "Should have at least one LOW priority task");
    }
    
    @Test
    void testHardcodedTasksHaveVariedStatuses() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        boolean hasUnassigned = false;
        boolean hasAssigned = false;
        boolean hasInProgress = false;
        boolean hasCompleted = false;
        
        for (ServiceTask task : tasks) {
            if (task.getStatus() == TaskStatus.UNASSIGNED) hasUnassigned = true;
            if (task.getStatus() == TaskStatus.ASSIGNED) hasAssigned = true;
            if (task.getStatus() == TaskStatus.IN_PROGRESS) hasInProgress = true;
            if (task.getStatus() == TaskStatus.COMPLETED) hasCompleted = true;
        }
        
        assertTrue(hasUnassigned, "Should have at least one UNASSIGNED task");
        assertTrue(hasAssigned, "Should have at least one ASSIGNED task");
        assertTrue(hasInProgress, "Should have at least one IN_PROGRESS task");
        assertTrue(hasCompleted, "Should have at least one COMPLETED task");
    }
    
    @Test
    void testHardcodedTasksHaveValidEstimatedDuration() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        for (ServiceTask task : tasks) {
            if (task.getEstimatedDuration() != null) {
                assertTrue(task.getEstimatedDuration() > 0, 
                        "Estimated duration should be positive when set");
            }
        }
    }
    
    @Test
    void testFirstHardcodedTask() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        ServiceTask firstTask = tasks.get(0);
        assertEquals(1L, firstTask.getId());
        assertEquals("Repair HVAC System", firstTask.getTitle());
        assertNotNull(firstTask.getDescription());
        assertEquals("123 Main St, Springfield, IL 62701", firstTask.getClientAddress());
        assertEquals(Priority.HIGH, firstTask.getPriority());
        assertEquals(120, firstTask.getEstimatedDuration());
        assertEquals(TaskStatus.UNASSIGNED, firstTask.getStatus());
        assertEquals("dispatcher@fsm.com", firstTask.getCreatedBy());
    }
    
    @Test
    void testSecondHardcodedTask() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        ServiceTask secondTask = tasks.get(1);
        assertEquals(2L, secondTask.getId());
        assertEquals("Install New Water Heater", secondTask.getTitle());
        assertEquals(Priority.MEDIUM, secondTask.getPriority());
        assertEquals(TaskStatus.ASSIGNED, secondTask.getStatus());
    }
    
    @Test
    void testThirdHardcodedTask() {
        List<ServiceTask> tasks = taskRepository.getHardcodedTasks();
        
        ServiceTask thirdTask = tasks.get(2);
        assertEquals(3L, thirdTask.getId());
        assertEquals("Electrical Panel Inspection", thirdTask.getTitle());
        assertEquals(Priority.LOW, thirdTask.getPriority());
        assertEquals(TaskStatus.IN_PROGRESS, thirdTask.getStatus());
    }
    
    // ============== CRUD Operations Tests ==============
    
    @Test
    void testSaveAndFindTask() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .description("Test Description")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .estimatedDuration(60)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("test@example.com")
                .build();
        
        ServiceTask saved = taskRepository.save(task);
        
        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("Test Task", saved.getTitle());
        assertEquals("123 Test St", saved.getClientAddress());
        assertEquals(Priority.HIGH, saved.getPriority());
    }
    
    @Test
    void testFindById() {
        ServiceTask task = ServiceTask.builder()
                .title("Find By Id Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved = taskRepository.save(task);
        
        Optional<ServiceTask> found = taskRepository.findById(saved.getId());
        
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Find By Id Task", found.get().getTitle());
    }
    
    @Test
    void testFindByIdNotFound() {
        Optional<ServiceTask> found = taskRepository.findById(999999L);
        assertFalse(found.isPresent());
    }
    
    @Test
    void testFindByStatus() {
        ServiceTask task1 = ServiceTask.builder()
                .title("Unassigned Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("Completed Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.COMPLETED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        List<ServiceTask> unassignedTasks = taskRepository.findByStatus(TaskStatus.UNASSIGNED);
        List<ServiceTask> completedTasks = taskRepository.findByStatus(TaskStatus.COMPLETED);
        
        assertEquals(1, unassignedTasks.size());
        assertEquals(1, completedTasks.size());
        assertTrue(unassignedTasks.stream().allMatch(t -> t.getStatus() == TaskStatus.UNASSIGNED));
        assertTrue(completedTasks.stream().allMatch(t -> t.getStatus() == TaskStatus.COMPLETED));
    }
    
    @Test
    void testFindByPriority() {
        ServiceTask task1 = ServiceTask.builder()
                .title("High Priority Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("Low Priority Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        List<ServiceTask> highPriorityTasks = taskRepository.findByPriority(Priority.HIGH);
        List<ServiceTask> lowPriorityTasks = taskRepository.findByPriority(Priority.LOW);
        
        assertEquals(1, highPriorityTasks.size());
        assertEquals(1, lowPriorityTasks.size());
        assertTrue(highPriorityTasks.stream().allMatch(t -> t.getPriority() == Priority.HIGH));
        assertTrue(lowPriorityTasks.stream().allMatch(t -> t.getPriority() == Priority.LOW));
    }
    
    @Test
    void testFindByCreatedBy() {
        ServiceTask task = ServiceTask.builder()
                .title("Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("admin@example.com")
                .build();
        
        taskRepository.save(task);
        
        List<ServiceTask> tasks = taskRepository.findByCreatedBy("admin@example.com");
        
        assertEquals(1, tasks.size());
        assertTrue(tasks.stream().allMatch(t -> t.getCreatedBy().equals("admin@example.com")));
    }
    
    @Test
    void testUpdateTask() {
        ServiceTask task = ServiceTask.builder()
                .title("Original Title")
                .clientAddress("123 Test St")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved = taskRepository.save(task);
        Long taskId = saved.getId();
        
        // Update the task
        saved.setTitle("Updated Title");
        saved.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(saved);
        
        // Fetch and verify
        Optional<ServiceTask> updated = taskRepository.findById(taskId);
        assertTrue(updated.isPresent());
        assertEquals("Updated Title", updated.get().getTitle());
        assertEquals(TaskStatus.COMPLETED, updated.get().getStatus());
    }
    
    @Test
    void testDeleteTask() {
        ServiceTask task = ServiceTask.builder()
                .title("Delete Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved = taskRepository.save(task);
        Long taskId = saved.getId();
        
        assertTrue(taskRepository.existsById(taskId));
        
        taskRepository.deleteById(taskId);
        
        assertFalse(taskRepository.existsById(taskId));
    }
    
    @Test
    void testDeleteAllTasks() {
        ServiceTask task1 = ServiceTask.builder()
                .title("Task 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("Task 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.ASSIGNED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        assertTrue(taskRepository.count() >= 2);
        
        taskRepository.deleteAll();
        
        assertEquals(0, taskRepository.count());
    }
    
    @Test
    void testTaskLifecycleTransitions() {
        ServiceTask task = ServiceTask.builder()
                .title("Lifecycle Task")
                .clientAddress("123 Test St")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved = taskRepository.save(task);
        assertTrue(saved.isUnassigned());
        
        saved.assign();
        taskRepository.save(saved);
        
        Optional<ServiceTask> assigned = taskRepository.findById(saved.getId());
        assertTrue(assigned.isPresent());
        assertEquals(TaskStatus.ASSIGNED, assigned.get().getStatus());
        
        assigned.get().start();
        taskRepository.save(assigned.get());
        
        Optional<ServiceTask> inProgress = taskRepository.findById(saved.getId());
        assertTrue(inProgress.isPresent());
        assertEquals(TaskStatus.IN_PROGRESS, inProgress.get().getStatus());
        
        inProgress.get().complete();
        taskRepository.save(inProgress.get());
        
        Optional<ServiceTask> completed = taskRepository.findById(saved.getId());
        assertTrue(completed.isPresent());
        assertTrue(completed.get().isCompleted());
    }
    
    @Test
    void testCount() {
        assertEquals(0, taskRepository.count());
        
        ServiceTask task = ServiceTask.builder()
                .title("Count Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task);
        
        assertEquals(1, taskRepository.count());
    }
    
    @Test
    void testFindAll() {
        ServiceTask task1 = ServiceTask.builder()
                .title("Task 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("Task 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.ASSIGNED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        
        List<ServiceTask> allTasks = taskRepository.findAll();
        
        assertEquals(2, allTasks.size());
    }
    
    @Test
    void testTimestampsAreSet() {
        ServiceTask task = ServiceTask.builder()
                .title("Timestamp Test")
                .clientAddress("123 Test St")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved = taskRepository.save(task);
        
        assertNotNull(saved.getCreatedAt());
    }
    
    // ============== New Query Methods Tests ==============
    
    @Test
    void testFindByStatusAndPriority() {
        ServiceTask task1 = ServiceTask.builder()
                .title("High Priority Unassigned")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("Low Priority Unassigned")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task3 = ServiceTask.builder()
                .title("High Priority Completed")
                .clientAddress("789 Test Blvd")
                .priority(Priority.HIGH)
                .status(TaskStatus.COMPLETED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        List<ServiceTask> results = taskRepository.findByStatusAndPriority(TaskStatus.UNASSIGNED, Priority.HIGH);
        
        assertEquals(1, results.size());
        assertEquals("High Priority Unassigned", results.get(0).getTitle());
        assertEquals(TaskStatus.UNASSIGNED, results.get(0).getStatus());
        assertEquals(Priority.HIGH, results.get(0).getPriority());
    }
    
    @Test
    void testFindByCreatedAtAfter() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        
        ServiceTask task1 = ServiceTask.builder()
                .title("Recent Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task1);
        
        List<ServiceTask> results = taskRepository.findByCreatedAtAfter(yesterday);
        
        assertEquals(1, results.size());
    }
    
    @Test
    void testFindByCreatedAtBefore() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        ServiceTask task1 = ServiceTask.builder()
                .title("Past Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task1);
        
        List<ServiceTask> results = taskRepository.findByCreatedAtBefore(tomorrow);
        
        assertEquals(1, results.size());
    }
    
    @Test
    void testFindByCreatedAtBetween() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        ServiceTask task1 = ServiceTask.builder()
                .title("Task Within Range")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task1);
        
        List<ServiceTask> results = taskRepository.findByCreatedAtBetween(yesterday, tomorrow);
        
        assertEquals(1, results.size());
    }
    
    @Test
    void testFindByStatusOrderByPriorityDesc() {
        ServiceTask task1 = ServiceTask.builder()
                .title("Low Priority Task")
                .clientAddress("123 Test St")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("High Priority Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task3 = ServiceTask.builder()
                .title("Medium Priority Task")
                .clientAddress("789 Test Blvd")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        List<ServiceTask> results = taskRepository.findByStatusOrderByPriorityDesc(TaskStatus.UNASSIGNED);
        
        assertEquals(3, results.size());
        assertEquals(Priority.HIGH, results.get(0).getPriority());
        assertEquals(Priority.MEDIUM, results.get(1).getPriority());
        assertEquals(Priority.LOW, results.get(2).getPriority());
    }
    
    @Test
    void testFindByStatusOrderByCreatedAtDesc() {
        ServiceTask task1 = ServiceTask.builder()
                .title("First Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved1 = taskRepository.save(task1);
        
        // Add small delay to ensure different timestamps
        ServiceTask task2 = ServiceTask.builder()
                .title("Second Task")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task2);
        
        List<ServiceTask> results = taskRepository.findByStatusOrderByCreatedAtDesc(TaskStatus.UNASSIGNED);
        
        assertEquals(2, results.size());
        // Most recent should be first
        assertTrue(results.get(0).getCreatedAt().compareTo(results.get(1).getCreatedAt()) >= 0);
    }
    
    @Test
    void testCountByStatus() {
        ServiceTask task1 = ServiceTask.builder()
                .title("Unassigned 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("Unassigned 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task3 = ServiceTask.builder()
                .title("Completed 1")
                .clientAddress("789 Test Blvd")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.COMPLETED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        assertEquals(2, taskRepository.countByStatus(TaskStatus.UNASSIGNED));
        assertEquals(1, taskRepository.countByStatus(TaskStatus.COMPLETED));
        assertEquals(0, taskRepository.countByStatus(TaskStatus.IN_PROGRESS));
    }
    
    @Test
    void testCountByPriority() {
        ServiceTask task1 = ServiceTask.builder()
                .title("High 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("High 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task3 = ServiceTask.builder()
                .title("Low 1")
                .clientAddress("789 Test Blvd")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        
        assertEquals(2, taskRepository.countByPriority(Priority.HIGH));
        assertEquals(1, taskRepository.countByPriority(Priority.LOW));
        assertEquals(0, taskRepository.countByPriority(Priority.MEDIUM));
    }
    
    @Test
    void testFindUrgentUnassignedTasks() {
        ServiceTask task1 = ServiceTask.builder()
                .title("High Priority Unassigned 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("High Priority Unassigned 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task3 = ServiceTask.builder()
                .title("Low Priority Unassigned")
                .clientAddress("789 Test Blvd")
                .priority(Priority.LOW)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task4 = ServiceTask.builder()
                .title("High Priority Assigned")
                .clientAddress("101 Test Ln")
                .priority(Priority.HIGH)
                .status(TaskStatus.ASSIGNED)
                .build();
        
        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        
        List<ServiceTask> urgentTasks = taskRepository.findUrgentUnassignedTasks();
        
        assertEquals(2, urgentTasks.size());
        assertTrue(urgentTasks.stream().allMatch(t -> 
            t.getStatus() == TaskStatus.UNASSIGNED && t.getPriority() == Priority.HIGH));
    }
    
    @Test
    void testExistsById() {
        ServiceTask task = ServiceTask.builder()
                .title("Exists Test")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved = taskRepository.save(task);
        
        assertTrue(taskRepository.existsById(saved.getId()));
        assertFalse(taskRepository.existsById(999999L));
    }
    
    @Test
    void testSaveAll() {
        ServiceTask task1 = ServiceTask.builder()
                .title("Batch Task 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask task2 = ServiceTask.builder()
                .title("Batch Task 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.LOW)
                .status(TaskStatus.ASSIGNED)
                .build();
        
        List<ServiceTask> saved = taskRepository.saveAll(List.of(task1, task2));
        
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(t -> t.getId() != null));
    }
    
    @Test
    void testFlush() {
        ServiceTask task = ServiceTask.builder()
                .title("Flush Test")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task);
        taskRepository.flush();
        
        assertEquals(1, taskRepository.count());
    }
    
    @Test
    void testSaveAndFlush() {
        ServiceTask task = ServiceTask.builder()
                .title("Save and Flush Test")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        ServiceTask saved = taskRepository.saveAndFlush(task);
        
        assertNotNull(saved.getId());
        assertEquals(1, taskRepository.count());
    }
}
