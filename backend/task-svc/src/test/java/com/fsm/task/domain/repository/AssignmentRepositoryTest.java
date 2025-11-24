package com.fsm.task.domain.repository;

import com.fsm.task.domain.model.Assignment;
import com.fsm.task.domain.model.Assignment.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AssignmentRepository with actual database operations.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class AssignmentRepositoryTest {
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Test
    void testRepositoryIsInjected() {
        assertNotNull(assignmentRepository);
    }
    
    @Test
    void testGetHardcodedAssignmentsReturnsValidList() {
        List<Assignment> assignments = assignmentRepository.getHardcodedAssignments();
        
        assertNotNull(assignments);
        assertEquals(6, assignments.size(), "Should return 6 hardcoded assignments");
    }
    
    @Test
    void testHardcodedAssignmentsHaveValidIds() {
        List<Assignment> assignments = assignmentRepository.getHardcodedAssignments();
        
        for (int i = 0; i < assignments.size(); i++) {
            Assignment assignment = assignments.get(i);
            assertNotNull(assignment.getId(), "Assignment " + i + " should have an ID");
            assertEquals((long) (i + 1), assignment.getId(), "Assignment ID should be sequential");
        }
    }
    
    @Test
    void testHardcodedAssignmentsHaveRequiredFields() {
        List<Assignment> assignments = assignmentRepository.getHardcodedAssignments();
        
        for (Assignment assignment : assignments) {
            assertNotNull(assignment.getTaskId(), "Assignment should have a task ID");
            assertNotNull(assignment.getTechnicianId(), "Assignment should have a technician ID");
            assertNotNull(assignment.getAssignedAt(), "Assignment should have an assignedAt timestamp");
            assertNotNull(assignment.getAssignedBy(), "Assignment should have assignedBy");
            assertNotNull(assignment.getStatus(), "Assignment should have a status");
        }
    }
    
    @Test
    void testHardcodedAssignmentsHaveVariedStatuses() {
        List<Assignment> assignments = assignmentRepository.getHardcodedAssignments();
        
        boolean hasActive = false;
        boolean hasReassigned = false;
        boolean hasCompleted = false;
        boolean hasCancelled = false;
        
        for (Assignment assignment : assignments) {
            if (assignment.getStatus() == AssignmentStatus.ACTIVE) hasActive = true;
            if (assignment.getStatus() == AssignmentStatus.REASSIGNED) hasReassigned = true;
            if (assignment.getStatus() == AssignmentStatus.COMPLETED) hasCompleted = true;
            if (assignment.getStatus() == AssignmentStatus.CANCELLED) hasCancelled = true;
        }
        
        assertTrue(hasActive, "Should have at least one ACTIVE assignment");
        assertTrue(hasReassigned, "Should have at least one REASSIGNED assignment");
        assertTrue(hasCompleted, "Should have at least one COMPLETED assignment");
        assertTrue(hasCancelled, "Should have at least one CANCELLED assignment");
    }
    
    @Test
    void testFirstHardcodedAssignment() {
        List<Assignment> assignments = assignmentRepository.getHardcodedAssignments();
        
        Assignment firstAssignment = assignments.get(0);
        assertEquals(1L, firstAssignment.getId());
        assertEquals(2L, firstAssignment.getTaskId());
        assertEquals(101L, firstAssignment.getTechnicianId());
        assertEquals("dispatcher@fsm.com", firstAssignment.getAssignedBy());
        assertEquals(AssignmentStatus.ACTIVE, firstAssignment.getStatus());
    }
    
    @Test
    void testHardcodedAssignmentWithReason() {
        List<Assignment> assignments = assignmentRepository.getHardcodedAssignments();
        
        // Assignment 5 should have a reason (it's REASSIGNED)
        Assignment reassignedAssignment = assignments.get(4);
        assertEquals(AssignmentStatus.REASSIGNED, reassignedAssignment.getStatus());
        assertNotNull(reassignedAssignment.getReason());
    }
    
    @Test
    void testSaveAndFindAssignment() {
        Assignment assignment = Assignment.builder()
                .taskId(100L)
                .technicianId(200L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("test@example.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment saved = assignmentRepository.save(assignment);
        
        assertNotNull(saved.getId());
        assertEquals(100L, saved.getTaskId());
        assertEquals(200L, saved.getTechnicianId());
        assertEquals(AssignmentStatus.ACTIVE, saved.getStatus());
    }
    
    @Test
    void testFindByTaskId() {
        Assignment assignment1 = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .taskId(10L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now().minusDays(1))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.REASSIGNED)
                .reason("Reassigned to new technician")
                .build();
        
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        
        List<Assignment> assignments = assignmentRepository.findByTaskId(10L);
        
        assertEquals(2, assignments.size());
        assertTrue(assignments.stream().allMatch(a -> a.getTaskId().equals(10L)));
    }
    
    @Test
    void testFindByTechnicianId() {
        Assignment assignment1 = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .taskId(20L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.COMPLETED)
                .build();
        
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        
        List<Assignment> assignments = assignmentRepository.findByTechnicianId(101L);
        
        assertEquals(2, assignments.size());
        assertTrue(assignments.stream().allMatch(a -> a.getTechnicianId().equals(101L)));
    }
    
    @Test
    void testFindByTechnicianIdAndStatus() {
        Assignment activeAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment completedAssignment = Assignment.builder()
                .taskId(20L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.COMPLETED)
                .build();
        
        assignmentRepository.save(activeAssignment);
        assignmentRepository.save(completedAssignment);
        
        List<Assignment> activeAssignments = assignmentRepository.findByTechnicianIdAndStatus(101L, AssignmentStatus.ACTIVE);
        List<Assignment> completedAssignments = assignmentRepository.findByTechnicianIdAndStatus(101L, AssignmentStatus.COMPLETED);
        
        assertEquals(1, activeAssignments.size());
        assertEquals(1, completedAssignments.size());
        assertTrue(activeAssignments.get(0).isActive());
        assertTrue(completedAssignments.get(0).isCompleted());
    }
    
    @Test
    void testFindByTaskIdAndStatus() {
        Assignment activeAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment reassignedAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now().minusDays(1))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.REASSIGNED)
                .reason("Reassigned")
                .build();
        
        assignmentRepository.save(activeAssignment);
        assignmentRepository.save(reassignedAssignment);
        
        Optional<Assignment> active = assignmentRepository.findByTaskIdAndStatus(10L, AssignmentStatus.ACTIVE);
        
        assertTrue(active.isPresent());
        assertEquals(101L, active.get().getTechnicianId());
    }
    
    @Test
    void testFindByStatus() {
        Assignment activeAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment completedAssignment = Assignment.builder()
                .taskId(20L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.COMPLETED)
                .build();
        
        assignmentRepository.save(activeAssignment);
        assignmentRepository.save(completedAssignment);
        
        List<Assignment> activeAssignments = assignmentRepository.findByStatus(AssignmentStatus.ACTIVE);
        List<Assignment> completedAssignments = assignmentRepository.findByStatus(AssignmentStatus.COMPLETED);
        
        assertTrue(activeAssignments.size() >= 1);
        assertTrue(completedAssignments.size() >= 1);
    }
    
    @Test
    void testCountByTechnicianIdAndStatus() {
        Assignment assignment1 = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .taskId(20L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment3 = Assignment.builder()
                .taskId(30L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.COMPLETED)
                .build();
        
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);
        
        int activeCount = assignmentRepository.countByTechnicianIdAndStatus(101L, AssignmentStatus.ACTIVE);
        
        assertEquals(2, activeCount);
    }
    
    @Test
    void testGetTechnicianWorkload() {
        Assignment assignment1 = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .taskId(20L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment3 = Assignment.builder()
                .taskId(30L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.COMPLETED)
                .build();
        
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        assignmentRepository.save(assignment3);
        
        int workload = assignmentRepository.getTechnicianWorkload(101L);
        
        assertEquals(2, workload, "Workload should count only ACTIVE assignments");
    }
    
    @Test
    void testGetTechnicianWorkloadForTechnicianWithNoAssignments() {
        int workload = assignmentRepository.getTechnicianWorkload(999L);
        
        assertEquals(0, workload, "Workload for technician with no assignments should be 0");
    }
    
    @Test
    void testUpdateAssignment() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment saved = assignmentRepository.save(assignment);
        Long assignmentId = saved.getId();
        
        // Mark as completed
        saved.complete();
        assignmentRepository.save(saved);
        
        // Fetch and verify
        Optional<Assignment> updated = assignmentRepository.findById(assignmentId);
        assertTrue(updated.isPresent());
        assertEquals(AssignmentStatus.COMPLETED, updated.get().getStatus());
    }
    
    @Test
    void testDeleteAssignment() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment saved = assignmentRepository.save(assignment);
        Long assignmentId = saved.getId();
        
        assertTrue(assignmentRepository.existsById(assignmentId));
        
        assignmentRepository.deleteById(assignmentId);
        
        assertFalse(assignmentRepository.existsById(assignmentId));
    }
    
    @Test
    void testCount() {
        long initialCount = assignmentRepository.count();
        
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        assignmentRepository.save(assignment);
        
        long newCount = assignmentRepository.count();
        assertEquals(initialCount + 1, newCount);
    }
    
    @Test
    void testFindAll() {
        Assignment assignment1 = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment assignment2 = Assignment.builder()
                .taskId(20L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.COMPLETED)
                .build();
        
        assignmentRepository.save(assignment1);
        assignmentRepository.save(assignment2);
        
        List<Assignment> allAssignments = assignmentRepository.findAll();
        
        assertTrue(allAssignments.size() >= 2);
    }
    
    @Test
    void testTimestampsAreSet() {
        Assignment assignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        Assignment saved = assignmentRepository.save(assignment);
        
        assertNotNull(saved.getAssignedAt());
    }
    
    @Test
    void testAssignmentHistoryPreservation() {
        // Create initial assignment
        Assignment initialAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(101L)
                .assignedAt(LocalDateTime.now().minusDays(1))
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        assignmentRepository.save(initialAssignment);
        
        // Mark as reassigned
        initialAssignment.markAsReassigned("New technician needed");
        assignmentRepository.save(initialAssignment);
        
        // Create new assignment
        Assignment newAssignment = Assignment.builder()
                .taskId(10L)
                .technicianId(102L)
                .assignedAt(LocalDateTime.now())
                .assignedBy("dispatcher@fsm.com")
                .status(AssignmentStatus.ACTIVE)
                .build();
        
        assignmentRepository.save(newAssignment);
        
        // Verify history is preserved
        List<Assignment> taskAssignments = assignmentRepository.findByTaskId(10L);
        assertEquals(2, taskAssignments.size(), "Should have 2 assignments for the task (original + new)");
        
        long activeCount = taskAssignments.stream().filter(Assignment::isActive).count();
        long reassignedCount = taskAssignments.stream()
                .filter(a -> a.getStatus() == AssignmentStatus.REASSIGNED).count();
        
        assertEquals(1, activeCount, "Should have exactly 1 active assignment");
        assertEquals(1, reassignedCount, "Should have exactly 1 reassigned assignment");
    }
}
