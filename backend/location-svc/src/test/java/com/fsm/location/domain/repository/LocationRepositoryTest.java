package com.fsm.location.domain.repository;

import com.fsm.location.domain.model.TechnicianLocation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LocationRepository.
 * Tests the hardcoded data methods and convenience methods.
 */
class LocationRepositoryTest {
    
    /**
     * Mock implementation of LocationRepository for testing default methods
     */
    private final LocationRepository repository = new MockLocationRepository();
    
    @Test
    void testGetHardcodedLocationsReturns6Locations() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        assertNotNull(locations);
        assertEquals(6, locations.size(), "Should return 6 hardcoded locations");
    }
    
    @Test
    void testGetHardcodedLocationsHaveUniqueIds() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        long distinctIds = locations.stream()
                .map(TechnicianLocation::getId)
                .distinct()
                .count();
        
        assertEquals(6, distinctIds, "All locations should have unique IDs");
    }
    
    @Test
    void testGetHardcodedLocationsHaveUniqueTechnicianIds() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        long distinctTechnicianIds = locations.stream()
                .map(TechnicianLocation::getTechnicianId)
                .distinct()
                .count();
        
        assertEquals(6, distinctTechnicianIds, "All locations should have unique technician IDs");
    }
    
    @Test
    void testGetHardcodedLocationsHaveValidLatitudes() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        for (TechnicianLocation location : locations) {
            assertNotNull(location.getLatitude(), 
                    "Latitude should not be null for location " + location.getId());
            assertTrue(location.getLatitude() >= -90.0 && location.getLatitude() <= 90.0,
                    "Latitude should be between -90 and 90 for location " + location.getId());
        }
    }
    
    @Test
    void testGetHardcodedLocationsHaveValidLongitudes() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        for (TechnicianLocation location : locations) {
            assertNotNull(location.getLongitude(), 
                    "Longitude should not be null for location " + location.getId());
            assertTrue(location.getLongitude() >= -180.0 && location.getLongitude() <= 180.0,
                    "Longitude should be between -180 and 180 for location " + location.getId());
        }
    }
    
    @Test
    void testGetHardcodedLocationsHavePositiveAccuracy() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        for (TechnicianLocation location : locations) {
            assertNotNull(location.getAccuracy(), 
                    "Accuracy should not be null for location " + location.getId());
            assertTrue(location.getAccuracy() > 0,
                    "Accuracy should be positive for location " + location.getId());
        }
    }
    
    @Test
    void testGetHardcodedLocationsHaveValidTimestamps() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        LocalDateTime now = LocalDateTime.now();
        
        for (TechnicianLocation location : locations) {
            assertNotNull(location.getTimestamp(), 
                    "Timestamp should not be null for location " + location.getId());
            assertTrue(location.getTimestamp().isBefore(now.plusSeconds(1)),
                    "Timestamp should not be in the future for location " + location.getId());
        }
    }
    
    @Test
    void testGetHardcodedLocationsHaveValidBatteryLevels() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        for (TechnicianLocation location : locations) {
            assertNotNull(location.getBatteryLevel(), 
                    "Battery level should not be null for location " + location.getId());
            assertTrue(location.getBatteryLevel() >= 0 && location.getBatteryLevel() <= 100,
                    "Battery level should be between 0 and 100 for location " + location.getId());
        }
    }
    
    @Test
    void testGetHardcodedLocationsHaveCreatedAt() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        for (TechnicianLocation location : locations) {
            assertNotNull(location.getCreatedAt(), 
                    "Created at should not be null for location " + location.getId());
        }
    }
    
    @Test
    void testGetHardcodedLocationsAreInSpringfieldArea() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        // Springfield, IL is approximately at latitude 39.7817, longitude -89.6501
        // All locations should be within a reasonable distance
        double springfieldLat = 39.7817;
        double springfieldLon = -89.6501;
        double maxDelta = 0.1; // About 10 km range
        
        for (TechnicianLocation location : locations) {
            assertTrue(Math.abs(location.getLatitude() - springfieldLat) < maxDelta,
                    "Latitude should be near Springfield for location " + location.getId());
            assertTrue(Math.abs(location.getLongitude() - springfieldLon) < maxDelta,
                    "Longitude should be near Springfield for location " + location.getId());
        }
    }
    
    @Test
    void testGetHardcodedLocationsHaveVariedBatteryLevels() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        // Check that we have at least one low battery and one high battery
        boolean hasLowBattery = locations.stream()
                .anyMatch(TechnicianLocation::isLowBattery);
        boolean hasHighBattery = locations.stream()
                .anyMatch(l -> l.getBatteryLevel() != null && l.getBatteryLevel() > 80);
        
        assertTrue(hasLowBattery, "Should have at least one location with low battery");
        assertTrue(hasHighBattery, "Should have at least one location with high battery");
    }
    
    @Test
    void testGetHardcodedLocationsHaveVariedAccuracy() {
        List<TechnicianLocation> locations = repository.getHardcodedLocations();
        
        // Check that we have at least one high accuracy and one low accuracy
        boolean hasHighAccuracy = locations.stream()
                .anyMatch(TechnicianLocation::isHighAccuracy);
        boolean hasLowAccuracy = locations.stream()
                .anyMatch(l -> l.getAccuracy() != null && l.getAccuracy() > 10);
        
        assertTrue(hasHighAccuracy, "Should have at least one location with high accuracy");
        assertTrue(hasLowAccuracy, "Should have at least one location with lower accuracy");
    }
    
    @Test
    void testGetLatestLocationForTechnicianConvenienceMethod() {
        // This tests the default convenience method
        Optional<TechnicianLocation> latest = repository.getLatestLocationForTechnician(101L);
        
        // The mock implementation returns empty, but the method should not throw
        assertNotNull(latest);
    }
    
    @Test
    void testGetAllTechnicianLocationsConvenienceMethod() {
        // This tests the default convenience method
        List<TechnicianLocation> locations = repository.getAllTechnicianLocations();
        
        // The mock implementation returns empty list, but the method should not throw
        assertNotNull(locations);
    }
    
    /**
     * Mock implementation for testing default methods only.
     * Real repository tests would use @DataJpaTest with an actual database.
     */
    private static class MockLocationRepository implements LocationRepository {
        
        @Override
        public Optional<TechnicianLocation> findFirstByTechnicianIdOrderByTimestampDesc(Long technicianId) {
            return Optional.empty();
        }
        
        @Override
        public List<TechnicianLocation> findByTechnicianIdOrderByTimestampDesc(Long technicianId) {
            return List.of();
        }
        
        @Override
        public List<TechnicianLocation> findByTechnicianIdAndTimestampBetweenOrderByTimestampDesc(
                Long technicianId, LocalDateTime startTime, LocalDateTime endTime) {
            return List.of();
        }
        
        @Override
        public List<TechnicianLocation> findLatestLocationsForAllTechnicians() {
            return List.of();
        }
        
        @Override
        public List<TechnicianLocation> findRecentLocations(LocalDateTime since) {
            return List.of();
        }
        
        @Override
        public long countByTechnicianId(Long technicianId) {
            return 0;
        }
        
        @Override
        public boolean existsByTechnicianId(Long technicianId) {
            return false;
        }
        
        @Override
        public List<TechnicianLocation> findTechniciansWithinRadius(
                org.locationtech.jts.geom.Point point, double radiusMeters) {
            return List.of();
        }
        
        @Override
        public List<TechnicianLocation> findActiveTechniciansWithinRadius(
                org.locationtech.jts.geom.Point point, double radiusMeters, LocalDateTime since) {
            return List.of();
        }
        
        @Override
        public List<TechnicianLocation> findLastKnownPositionsForActiveTechnicians(LocalDateTime since) {
            return List.of();
        }
        
        @Override
        public List<TechnicianLocation> findAll() {
            return List.of();
        }
        
        @Override
        public List<TechnicianLocation> findAllById(Iterable<Long> longs) {
            return List.of();
        }
        
        @Override
        public long count() {
            return 0;
        }
        
        @Override
        public void deleteById(Long aLong) {
        }
        
        @Override
        public void delete(TechnicianLocation entity) {
        }
        
        @Override
        public void deleteAllById(Iterable<? extends Long> longs) {
        }
        
        @Override
        public void deleteAll(Iterable<? extends TechnicianLocation> entities) {
        }
        
        @Override
        public void deleteAll() {
        }
        
        @Override
        public <S extends TechnicianLocation> S save(S entity) {
            return entity;
        }
        
        @Override
        public <S extends TechnicianLocation> List<S> saveAll(Iterable<S> entities) {
            return List.of();
        }
        
        @Override
        public Optional<TechnicianLocation> findById(Long aLong) {
            return Optional.empty();
        }
        
        @Override
        public boolean existsById(Long aLong) {
            return false;
        }
        
        @Override
        public void flush() {
        }
        
        @Override
        public <S extends TechnicianLocation> S saveAndFlush(S entity) {
            return entity;
        }
        
        @Override
        public <S extends TechnicianLocation> List<S> saveAllAndFlush(Iterable<S> entities) {
            return List.of();
        }
        
        @Override
        public void deleteAllInBatch(Iterable<TechnicianLocation> entities) {
        }
        
        @Override
        public void deleteAllByIdInBatch(Iterable<Long> longs) {
        }
        
        @Override
        public void deleteAllInBatch() {
        }
        
        @Override
        public TechnicianLocation getOne(Long aLong) {
            return null;
        }
        
        @Override
        public TechnicianLocation getById(Long aLong) {
            return null;
        }
        
        @Override
        public TechnicianLocation getReferenceById(Long aLong) {
            return null;
        }
        
        @Override
        public <S extends TechnicianLocation> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
            return Optional.empty();
        }
        
        @Override
        public <S extends TechnicianLocation> List<S> findAll(org.springframework.data.domain.Example<S> example) {
            return List.of();
        }
        
        @Override
        public <S extends TechnicianLocation> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) {
            return List.of();
        }
        
        @Override
        public <S extends TechnicianLocation> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) {
            return org.springframework.data.domain.Page.empty();
        }
        
        @Override
        public <S extends TechnicianLocation> long count(org.springframework.data.domain.Example<S> example) {
            return 0;
        }
        
        @Override
        public <S extends TechnicianLocation> boolean exists(org.springframework.data.domain.Example<S> example) {
            return false;
        }
        
        @Override
        public <S extends TechnicianLocation, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            return null;
        }
        
        @Override
        public List<TechnicianLocation> findAll(org.springframework.data.domain.Sort sort) {
            return List.of();
        }
        
        @Override
        public org.springframework.data.domain.Page<TechnicianLocation> findAll(org.springframework.data.domain.Pageable pageable) {
            return org.springframework.data.domain.Page.empty();
        }
    }
}
