package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.ServiceTask;
import com.fsm.task.domain.model.ServiceTask.Priority;
import com.fsm.task.domain.model.ServiceTask.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for TaskRepository with actual database operations.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class TaskRepositoryTest {
    
    @Autowired
    private TaskRepository taskRepository;
    
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
        
        assertTrue(unassignedTasks.size() >= 1);
        assertTrue(completedTasks.size() >= 1);
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
        
        assertTrue(highPriorityTasks.size() >= 1);
        assertTrue(lowPriorityTasks.size() >= 1);
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
        
        assertTrue(tasks.size() >= 1);
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
        long initialCount = taskRepository.count();
        
        ServiceTask task = ServiceTask.builder()
                .title("Count Test Task")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .build();
        
        taskRepository.save(task);
        
        long newCount = taskRepository.count();
        assertEquals(initialCount + 1, newCount);
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
        
        assertTrue(allTasks.size() >= 2);
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
}
