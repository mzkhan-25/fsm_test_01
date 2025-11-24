package com.fsm.task.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RequireRole annotation
 */
class RequireRoleTest {
    
    @Test
    void testAnnotationRetention() {
        Retention retention = RequireRole.class.getAnnotation(Retention.class);
        assertNotNull(retention);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }
    
    @Test
    void testAnnotationTargets() {
        Target target = RequireRole.class.getAnnotation(Target.class);
        assertNotNull(target);
        assertEquals(2, target.value().length);
        boolean hasMethod = false;
        boolean hasType = false;
        for (ElementType type : target.value()) {
            if (type == ElementType.METHOD) hasMethod = true;
            if (type == ElementType.TYPE) hasType = true;
        }
        assertTrue(hasMethod, "Should target METHOD");
        assertTrue(hasType, "Should target TYPE");
    }
    
    @Test
    void testAnnotationOnMethod() throws NoSuchMethodException {
        // Create a test class with the annotation
        class TestClass {
            @RequireRole({Role.ADMIN, Role.DISPATCHER})
            public void testMethod() {}
        }
        
        RequireRole annotation = TestClass.class
                .getMethod("testMethod")
                .getAnnotation(RequireRole.class);
        
        assertNotNull(annotation);
        assertEquals(2, annotation.value().length);
        assertEquals(Role.ADMIN, annotation.value()[0]);
        assertEquals(Role.DISPATCHER, annotation.value()[1]);
    }
    
    @Test
    void testAnnotationOnClass() {
        @RequireRole({Role.SUPERVISOR})
        class TestClass {}
        
        RequireRole annotation = TestClass.class.getAnnotation(RequireRole.class);
        
        assertNotNull(annotation);
        assertEquals(1, annotation.value().length);
        assertEquals(Role.SUPERVISOR, annotation.value()[0]);
    }
}
