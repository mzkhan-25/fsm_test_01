package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.Assignment.AssignmentStatus;
import com.fsm.task.domain.model.AssignmentHistory;
import com.fsm.task.domain.model.AssignmentHistory.HistoryAction;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AssignmentHistoryRepository with actual database operations.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AssignmentHistoryRepositoryTest {
    
    @Autowired
    private AssignmentHistoryRepository historyRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private TaskRepository taskRepository;
    
    private ServiceTask testTask1;
    private ServiceTask testTask2;
    private Assignment testAssignment1;
    private Assignment testAssignment2;
    
    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        assignmentRepository.deleteAll();
        taskRepository.deleteAll();
        
        // Create test tasks for foreign key constraints
        testTask1 = taskRepository.save(ServiceTask.builder()
                .title("Test Task 1")
                .clientAddress("123 Test St")
                .priority(Priority.HIGH)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("test@example.com")
                .build());
        
        testTask2 = taskRepository.save(ServiceTask.builder()
                .title("Test Task 2")
                .clientAddress("456 Test Ave")
                .priority(Priority.MEDIUM)
                .status(TaskStatus.UNASSIGNED)
                .createdBy("test@example.com")
                .build());
        
        // Create test assignments
        testAssignment1 = assignmentRepository.save(Assignment.builder()
                .taskId(testTask1.getId())
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build());
        
        testAssignment2 = assignmentRepository.save(Assignment.builder()
                .taskId(testTask2.getId())
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build());
    }
    
    @Test
    void testRepositoryIsInjected() {
        assertNotNull(historyRepository);
    }
    
    @Test
    void testSaveAndFindHistory() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build();
        
        AssignmentHistory saved = historyRepository.save(history);
        
        assertNotNull(saved.getId());
        assertEquals(testAssignment1.getId(), saved.getAssignmentId());
        assertEquals(testTask1.getId(), saved.getTaskId());
        assertEquals(101L, saved.getTechnicianId());
        assertEquals(HistoryAction.CREATED, saved.getAction());
    }
    
    @Test
    void testFindByAssignmentIdOrderByActionAtDesc() {
        AssignmentHistory history1 = historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(2))
                .build());
        
        AssignmentHistory history2 = historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.COMPLETED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        List<AssignmentHistory> historyList = historyRepository.findByAssignmentIdOrderByActionAtDesc(testAssignment1.getId());
        
        assertEquals(2, historyList.size());
        assertEquals(HistoryAction.COMPLETED, historyList.get(0).getAction());
        assertEquals(HistoryAction.CREATED, historyList.get(1).getAction());
    }
    
    @Test
    void testFindByTaskIdOrderByActionAtDesc() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(1))
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(102L)
                .previousTechnicianId(101L)
                .action(HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .reason("Reassigned")
                .build());
        
        List<AssignmentHistory> historyList = historyRepository.findByTaskIdOrderByActionAtDesc(testTask1.getId());
        
        assertEquals(2, historyList.size());
        assertEquals(HistoryAction.REASSIGNED, historyList.get(0).getAction());
    }
    
    @Test
    void testFindByTechnicianIdOrderByActionAtDesc() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(1))
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment2.getId())
                .taskId(testTask2.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        List<AssignmentHistory> historyList = historyRepository.findByTechnicianIdOrderByActionAtDesc(101L);
        
        assertEquals(2, historyList.size());
        assertTrue(historyList.stream().allMatch(h -> h.getTechnicianId().equals(101L)));
    }
    
    @Test
    void testFindByAction() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment2.getId())
                .taskId(testTask2.getId())
                .technicianId(102L)
                .action(HistoryAction.COMPLETED)
                .actionBy("technician@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        List<AssignmentHistory> createdList = historyRepository.findByAction(HistoryAction.CREATED);
        List<AssignmentHistory> completedList = historyRepository.findByAction(HistoryAction.COMPLETED);
        
        assertEquals(1, createdList.size());
        assertEquals(1, completedList.size());
        assertTrue(createdList.stream().allMatch(h -> h.getAction() == HistoryAction.CREATED));
        assertTrue(completedList.stream().allMatch(h -> h.getAction() == HistoryAction.COMPLETED));
    }
    
    @Test
    void testFindByTaskIdAndActionAtBetween() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        List<AssignmentHistory> historyList = historyRepository.findByTaskIdAndActionAtBetween(
                testTask1.getId(), yesterday, tomorrow);
        
        assertEquals(1, historyList.size());
    }
    
    @Test
    void testFindByTechnicianIdAndActionAtBetween() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        List<AssignmentHistory> historyList = historyRepository.findByTechnicianIdAndActionAtBetween(
                101L, yesterday, tomorrow);
        
        assertEquals(1, historyList.size());
    }
    
    @Test
    void testCountByTaskIdAndAction() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(2))
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(102L)
                .previousTechnicianId(101L)
                .action(HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(1))
                .reason("First reassignment")
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(103L)
                .previousTechnicianId(102L)
                .action(HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .reason("Second reassignment")
                .build());
        
        int reassignmentCount = historyRepository.countByTaskIdAndAction(
                testTask1.getId(), HistoryAction.REASSIGNED);
        
        assertEquals(2, reassignmentCount);
    }
    
    @Test
    void testFindLatestByTaskId() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(1))
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.COMPLETED)
                .actionBy("technician@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        AssignmentHistory latest = historyRepository.findLatestByTaskId(testTask1.getId());
        
        assertNotNull(latest);
        assertEquals(HistoryAction.COMPLETED, latest.getAction());
    }
    
    @Test
    void testFindByActionByOrderByActionAtDesc() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(1))
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment2.getId())
                .taskId(testTask2.getId())
                .technicianId(102L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.COMPLETED)
                .actionBy("technician@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        List<AssignmentHistory> dispatcherHistory = historyRepository.findByActionByOrderByActionAtDesc("dispatcher@fsm.com");
        
        assertEquals(2, dispatcherHistory.size());
        assertTrue(dispatcherHistory.stream().allMatch(h -> h.getActionBy().equals("dispatcher@fsm.com")));
    }
    
    @Test
    void testCountByActionBy() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment2.getId())
                .taskId(testTask2.getId())
                .technicianId(102L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.COMPLETED)
                .actionBy("technician@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        long dispatcherCount = historyRepository.countByActionBy("dispatcher@fsm.com");
        long technicianCount = historyRepository.countByActionBy("technician@fsm.com");
        
        assertEquals(2, dispatcherCount);
        assertEquals(1, technicianCount);
    }
    
    @Test
    void testHistoryWithPreviousTechnicianId() {
        AssignmentHistory history = historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(102L)
                .previousTechnicianId(101L)
                .action(HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .reason("Original technician unavailable")
                .build());
        
        AssignmentHistory found = historyRepository.findById(history.getId()).orElseThrow();
        
        assertEquals(102L, found.getTechnicianId());
        assertEquals(101L, found.getPreviousTechnicianId());
        assertEquals("Original technician unavailable", found.getReason());
    }
    
    @Test
    void testTimestampsAreSet() {
        AssignmentHistory history = AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .build();
        
        AssignmentHistory saved = historyRepository.save(history);
        
        assertNotNull(saved.getActionAt());
    }
    
    @Test
    void testFindAll() {
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment2.getId())
                .taskId(testTask2.getId())
                .technicianId(102L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        List<AssignmentHistory> allHistory = historyRepository.findAll();
        
        assertEquals(2, allHistory.size());
    }
    
    @Test
    void testCount() {
        long initialCount = historyRepository.count();
        
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        long newCount = historyRepository.count();
        assertEquals(initialCount + 1, newCount);
    }
    
    @Test
    void testHistoryAuditTrailIntegrity() {
        // Create initial assignment history
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(101L)
                .action(HistoryAction.CREATED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(3))
                .build());
        
        // First reassignment
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(102L)
                .previousTechnicianId(101L)
                .action(HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(2))
                .reason("First technician on leave")
                .build());
        
        // Second reassignment
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(103L)
                .previousTechnicianId(102L)
                .action(HistoryAction.REASSIGNED)
                .actionBy("dispatcher@fsm.com")
                .actionAt(LocalDateTime.now().minusDays(1))
                .reason("Better match for skillset")
                .build());
        
        // Completion
        historyRepository.save(AssignmentHistory.builder()
                .assignmentId(testAssignment1.getId())
                .taskId(testTask1.getId())
                .technicianId(103L)
                .action(HistoryAction.COMPLETED)
                .actionBy("technician@fsm.com")
                .actionAt(LocalDateTime.now())
                .build());
        
        // Verify complete audit trail
        List<AssignmentHistory> taskHistory = historyRepository.findByTaskIdOrderByActionAtDesc(testTask1.getId());
        
        assertEquals(4, taskHistory.size());
        assertEquals(HistoryAction.COMPLETED, taskHistory.get(0).getAction());
        assertEquals(HistoryAction.REASSIGNED, taskHistory.get(1).getAction());
        assertEquals(HistoryAction.REASSIGNED, taskHistory.get(2).getAction());
        assertEquals(HistoryAction.CREATED, taskHistory.get(3).getAction());
        
        // Verify reassignment count
        int reassignmentCount = historyRepository.countByTaskIdAndAction(testTask1.getId(), HistoryAction.REASSIGNED);
        assertEquals(2, reassignmentCount);
    }
}
