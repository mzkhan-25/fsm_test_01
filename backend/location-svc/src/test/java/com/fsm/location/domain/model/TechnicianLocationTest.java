package com.fsm.location.domain.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnicianLocation entity.
 * Tests domain invariants and business logic.
 */
class TechnicianLocationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    void testTechnicianLocationBuilderCreatesValidLocation() {
        LocalDateTime now = LocalDateTime.now();
        TechnicianLocation location = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now)
                .batteryLevel(85)
                .build();
        
        assertNotNull(location);
        assertEquals(1L, location.getId());
        assertEquals(101L, location.getTechnicianId());
        assertEquals(39.7817, location.getLatitude());
        assertEquals(-89.6501, location.getLongitude());
        assertEquals(5.0, location.getAccuracy());
        assertEquals(now, location.getTimestamp());
        assertEquals(85, location.getBatteryLevel());
    }
    
    @Test
    void testValidationWithNullTechnicianId() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(null)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("technicianId")));
    }
    
    @Test
    void testValidationWithNullLatitude() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(null)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testValidationWithLatitudeTooLow() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(-91.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testValidationWithLatitudeTooHigh() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(91.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testValidationWithValidLatitudeBoundaries() {
        // Test minimum valid latitude (-90)
        TechnicianLocation locationMin = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(-90.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violationsMin = validator.validate(locationMin);
        assertTrue(violationsMin.isEmpty() || violationsMin.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("latitude")));
        
        // Test maximum valid latitude (90)
        TechnicianLocation locationMax = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(90.0)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violationsMax = validator.validate(locationMax);
        assertTrue(violationsMax.isEmpty() || violationsMax.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("latitude")));
    }
    
    @Test
    void testValidationWithNullLongitude() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(null)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testValidationWithLongitudeTooLow() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-181.0)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testValidationWithLongitudeTooHigh() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(181.0)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testValidationWithValidLongitudeBoundaries() {
        // Test minimum valid longitude (-180)
        TechnicianLocation locationMin = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-180.0)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violationsMin = validator.validate(locationMin);
        assertTrue(violationsMin.isEmpty() || violationsMin.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("longitude")));
        
        // Test maximum valid longitude (180)
        TechnicianLocation locationMax = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(180.0)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violationsMax = validator.validate(locationMax);
        assertTrue(violationsMax.isEmpty() || violationsMax.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("longitude")));
    }
    
    @Test
    void testValidationWithNullAccuracy() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(null)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accuracy")));
    }
    
    @Test
    void testValidationWithNegativeAccuracy() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(-5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accuracy")));
    }
    
    @Test
    void testValidationWithZeroAccuracy() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(0.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accuracy")));
    }
    
    @Test
    void testValidationWithPositiveAccuracy() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(0.001)
                .timestamp(LocalDateTime.now())
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("accuracy")));
    }
    
    @Test
    void testValidationWithNullTimestamp() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(null)
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("timestamp")));
    }
    
    @Test
    void testValidationWithFutureTimestamp() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().plusDays(1))
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("timestamp")));
    }
    
    @Test
    void testValidationWithPastTimestamp() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("timestamp")));
    }
    
    @Test
    void testValidationWithBatteryLevelTooLow() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(-1)
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
    }
    
    @Test
    void testValidationWithBatteryLevelTooHigh() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(101)
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
    }
    
    @Test
    void testValidationWithValidBatteryLevelBoundaries() {
        // Test minimum valid battery level (0)
        TechnicianLocation locationMin = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(0)
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violationsMin = validator.validate(locationMin);
        assertTrue(violationsMin.isEmpty() || violationsMin.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
        
        // Test maximum valid battery level (100)
        TechnicianLocation locationMax = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(100)
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violationsMax = validator.validate(locationMax);
        assertTrue(violationsMax.isEmpty() || violationsMax.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
    }
    
    @Test
    void testValidationWithNullBatteryLevel() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(null)
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        // Battery level is optional, so null should be valid
        assertTrue(violations.isEmpty() || violations.stream()
                .noneMatch(v -> v.getPropertyPath().toString().equals("batteryLevel")));
    }
    
    @Test
    void testIsRecentMethod() {
        // Recent location (2 minutes ago)
        TechnicianLocation recentLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusMinutes(2))
                .build();
        
        assertTrue(recentLocation.isRecent());
        
        // Not recent location (10 minutes ago)
        TechnicianLocation oldLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusMinutes(10))
                .build();
        
        assertFalse(oldLocation.isRecent());
    }
    
    @Test
    void testIsRecentMethodWithNullTimestamp() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(null)
                .build();
        
        assertFalse(location.isRecent());
    }
    
    @Test
    void testIsStaleMethod() {
        // Stale location (20 minutes ago)
        TechnicianLocation staleLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusMinutes(20))
                .build();
        
        assertTrue(staleLocation.isStale());
        
        // Not stale location (5 minutes ago)
        TechnicianLocation recentLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now().minusMinutes(5))
                .build();
        
        assertFalse(recentLocation.isStale());
    }
    
    @Test
    void testIsStaleMethodWithNullTimestamp() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(null)
                .build();
        
        assertFalse(location.isStale());
    }
    
    @Test
    void testIsLowBatteryMethod() {
        // Low battery (15%)
        TechnicianLocation lowBatteryLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(15)
                .build();
        
        assertTrue(lowBatteryLocation.isLowBattery());
        
        // Normal battery (50%)
        TechnicianLocation normalBatteryLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(50)
                .build();
        
        assertFalse(normalBatteryLocation.isLowBattery());
    }
    
    @Test
    void testIsLowBatteryMethodBoundary() {
        // At boundary (20%)
        TechnicianLocation boundaryLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(20)
                .build();
        
        assertFalse(boundaryLocation.isLowBattery());
        
        // Just below boundary (19%)
        TechnicianLocation lowLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(19)
                .build();
        
        assertTrue(lowLocation.isLowBattery());
    }
    
    @Test
    void testIsLowBatteryMethodWithNullBatteryLevel() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(null)
                .build();
        
        assertFalse(location.isLowBattery());
    }
    
    @Test
    void testIsHighAccuracyMethod() {
        // High accuracy (5 meters)
        TechnicianLocation highAccuracyLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertTrue(highAccuracyLocation.isHighAccuracy());
        
        // Low accuracy (20 meters)
        TechnicianLocation lowAccuracyLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(20.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertFalse(lowAccuracyLocation.isHighAccuracy());
    }
    
    @Test
    void testIsHighAccuracyMethodBoundary() {
        // At boundary (10 meters)
        TechnicianLocation boundaryLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(10.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertTrue(boundaryLocation.isHighAccuracy());
        
        // Just above boundary (10.1 meters)
        TechnicianLocation lowLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(10.1)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertFalse(lowLocation.isHighAccuracy());
    }
    
    @Test
    void testIsHighAccuracyMethodWithNullAccuracy() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(null)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertFalse(location.isHighAccuracy());
    }
    
    @Test
    void testDistanceToMethod() {
        // Springfield, IL (approximately)
        TechnicianLocation location1 = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        // Chicago, IL (approximately) - about 300 km from Springfield
        TechnicianLocation location2 = TechnicianLocation.builder()
                .technicianId(102L)
                .latitude(41.8781)
                .longitude(-87.6298)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        double distance = location1.distanceTo(location2);
        
        // Distance should be approximately 290-310 km
        assertTrue(distance > 280 && distance < 320, 
                "Distance between Springfield and Chicago should be approximately 300 km, was: " + distance);
    }
    
    @Test
    void testDistanceToMethodSameLocation() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        double distance = location.distanceTo(location);
        
        assertEquals(0.0, distance, 0.001, "Distance to same location should be 0");
    }
    
    @Test
    void testDistanceToMethodWithNullOther() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        double distance = location.distanceTo(null);
        
        assertTrue(Double.isNaN(distance), "Distance to null should be NaN");
    }
    
    @Test
    void testDistanceToMethodWithNullLatitude() {
        TechnicianLocation location1 = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(null)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .technicianId(102L)
                .latitude(41.8781)
                .longitude(-87.6298)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        double distance = location1.distanceTo(location2);
        
        assertTrue(Double.isNaN(distance), "Distance with null latitude should be NaN");
    }
    
    @Test
    void testDistanceToMethodWithNullLongitude() {
        TechnicianLocation location1 = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(null)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .technicianId(102L)
                .latitude(41.8781)
                .longitude(-87.6298)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        double distance = location1.distanceTo(location2);
        
        assertTrue(Double.isNaN(distance), "Distance with null longitude should be NaN");
    }
    
    @Test
    void testDistanceToMethodWithNullOtherLatitude() {
        TechnicianLocation location1 = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .technicianId(102L)
                .latitude(null)
                .longitude(-87.6298)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        double distance = location1.distanceTo(location2);
        
        assertTrue(Double.isNaN(distance), "Distance with null other latitude should be NaN");
    }
    
    @Test
    void testDistanceToMethodWithNullOtherLongitude() {
        TechnicianLocation location1 = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .technicianId(102L)
                .latitude(41.8781)
                .longitude(null)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        double distance = location1.distanceTo(location2);
        
        assertTrue(Double.isNaN(distance), "Distance with null other longitude should be NaN");
    }
    
    @Test
    void testOnCreateMethod() {
        TechnicianLocation location = new TechnicianLocation();
        assertNull(location.getCreatedAt());
        assertNull(location.getTimestamp());
        
        location.onCreate();
        
        assertNotNull(location.getCreatedAt());
        assertNotNull(location.getTimestamp());
    }
    
    @Test
    void testOnCreateMethodWithExistingTimestamp() {
        LocalDateTime existingTimestamp = LocalDateTime.now().minusHours(1);
        TechnicianLocation location = new TechnicianLocation();
        location.setTimestamp(existingTimestamp);
        
        location.onCreate();
        
        assertNotNull(location.getCreatedAt());
        assertEquals(existingTimestamp, location.getTimestamp(), 
                "Existing timestamp should not be overwritten");
    }
    
    @Test
    void testTechnicianLocationEqualsAndHashCode() {
        LocalDateTime now = LocalDateTime.now();
        TechnicianLocation location1 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now)
                .batteryLevel(85)
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now)
                .batteryLevel(85)
                .build();
        
        assertEquals(location1, location2);
        assertEquals(location1.hashCode(), location2.hashCode());
    }
    
    @Test
    void testTechnicianLocationToString() {
        TechnicianLocation location = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        String toString = location.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("39.7817"));
        assertTrue(toString.contains("-89.6501"));
    }
    
    @Test
    void testTechnicianLocationEqualsWithNull() {
        TechnicianLocation location = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertNotEquals(location, null);
    }
    
    @Test
    void testTechnicianLocationEqualsWithDifferentClass() {
        TechnicianLocation location = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertNotEquals(location, "not a location");
    }
    
    @Test
    void testTechnicianLocationEqualsSameObject() {
        TechnicianLocation location = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertEquals(location, location);
    }
    
    @Test
    void testTechnicianLocationAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        TechnicianLocation location = new TechnicianLocation(1L, 101L, 39.7817, -89.6501, 
                5.0, now, 85, null, now);
        
        assertNotNull(location);
        assertEquals(1L, location.getId());
        assertEquals(101L, location.getTechnicianId());
        assertEquals(39.7817, location.getLatitude());
        assertEquals(-89.6501, location.getLongitude());
        assertEquals(5.0, location.getAccuracy());
        assertEquals(now, location.getTimestamp());
        assertEquals(85, location.getBatteryLevel());
        assertEquals(now, location.getCreatedAt());
    }
    
    @Test
    void testTechnicianLocationSetters() {
        TechnicianLocation location = new TechnicianLocation();
        LocalDateTime now = LocalDateTime.now();
        
        location.setId(1L);
        location.setTechnicianId(101L);
        location.setLatitude(39.7817);
        location.setLongitude(-89.6501);
        location.setAccuracy(5.0);
        location.setTimestamp(now);
        location.setBatteryLevel(85);
        location.setCreatedAt(now);
        
        assertEquals(1L, location.getId());
        assertEquals(101L, location.getTechnicianId());
        assertEquals(39.7817, location.getLatitude());
        assertEquals(-89.6501, location.getLongitude());
        assertEquals(5.0, location.getAccuracy());
        assertEquals(now, location.getTimestamp());
        assertEquals(85, location.getBatteryLevel());
        assertEquals(now, location.getCreatedAt());
    }
    
    @Test
    void testTechnicianLocationBuilder() {
        TechnicianLocation location = TechnicianLocation.builder().build();
        
        assertNotNull(location);
    }
    
    @Test
    void testTechnicianLocationCanEqual() {
        TechnicianLocation location1 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .build();
        
        assertTrue(location1.canEqual(location2));
        assertTrue(location2.canEqual(location1));
    }
    
    @Test
    void testTechnicianLocationBuilderToString() {
        TechnicianLocation.TechnicianLocationBuilder builder = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817);
        
        String builderStr = builder.toString();
        assertNotNull(builderStr);
        assertTrue(builderStr.contains("TechnicianLocation") || builderStr.contains("TechnicianLocationBuilder"));
    }
    
    @Test
    void testTechnicianLocationNotEqualsWithDifferentId() {
        LocalDateTime now = LocalDateTime.now();
        TechnicianLocation location1 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now)
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .id(2L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now)
                .build();
        
        assertNotEquals(location1, location2);
    }
    
    @Test
    void testTechnicianLocationNotEqualsWithDifferentTechnicianId() {
        LocalDateTime now = LocalDateTime.now();
        TechnicianLocation location1 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now)
                .build();
        
        TechnicianLocation location2 = TechnicianLocation.builder()
                .id(1L)
                .technicianId(102L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now)
                .build();
        
        assertNotEquals(location1, location2);
    }
    
    @Test
    void testValidationWithValidLocation() {
        TechnicianLocation location = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(85)
                .build();
        
        Set<ConstraintViolation<TechnicianLocation>> violations = validator.validate(location);
        assertTrue(violations.isEmpty(), "Valid location should have no violations");
    }
}
