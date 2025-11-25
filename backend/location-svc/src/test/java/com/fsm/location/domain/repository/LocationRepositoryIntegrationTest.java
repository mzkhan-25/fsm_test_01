package com.fsm.location.domain.repository;

import com.fsm.location.domain.model.TechnicianLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for LocationRepository focusing on non-PostGIS functionality.
 * Tests geospatial queries that work with H2 in-memory database.
 */
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class LocationRepositoryIntegrationTest {
    
    @Autowired
    private LocationRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        // Setup test data - Create locations in Springfield, IL area
        LocalDateTime now = LocalDateTime.now();
        
        // Technician 1 - Recent location
        TechnicianLocation location1 = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now.minusMinutes(2))
                .batteryLevel(85)
                .build();
        
        // Technician 2 - Recent location
        TechnicianLocation location2 = TechnicianLocation.builder()
                .technicianId(102L)
                .latitude(39.7845)
                .longitude(-89.6302)
                .accuracy(8.0)
                .timestamp(now.minusMinutes(5))
                .batteryLevel(62)
                .build();
        
        // Technician 3 - Recent location
        TechnicianLocation location3 = TechnicianLocation.builder()
                .technicianId(103L)
                .latitude(39.7789)
                .longitude(-89.6720)
                .accuracy(12.0)
                .timestamp(now.minusMinutes(8))
                .batteryLevel(45)
                .build();
        
        // Technician 4 - Recent location
        TechnicianLocation location4 = TechnicianLocation.builder()
                .technicianId(104L)
                .latitude(39.8025)
                .longitude(-89.6489)
                .accuracy(6.5)
                .timestamp(now.minusMinutes(1))
                .batteryLevel(92)
                .build();
        
        // Technician 5 - Old location (not recent)
        TechnicianLocation location5 = TechnicianLocation.builder()
                .technicianId(105L)
                .latitude(39.7612)
                .longitude(-89.6550)
                .accuracy(15.0)
                .timestamp(now.minusMinutes(20))
                .batteryLevel(18)
                .build();
        
        // Technician 6 - Recent location
        TechnicianLocation location6 = TechnicianLocation.builder()
                .technicianId(106L)
                .latitude(40.0)
                .longitude(-90.0)
                .accuracy(4.0)
                .timestamp(now.minusMinutes(3))
                .batteryLevel(78)
                .build();
        
        repository.saveAll(List.of(location1, location2, location3, location4, location5, location6));
    }
    
    @Test
    void testFindLastKnownPositionsForActiveTechnicians() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(10);
        List<TechnicianLocation> activeLocations = 
                repository.findLastKnownPositionsForActiveTechnicians(since);
        
        assertNotNull(activeLocations);
        // Should find 5 technicians (all except technician 105 who has a 20-minute-old location)
        assertEquals(5, activeLocations.size());
        
        // Verify none of the results are from technician 105
        assertTrue(activeLocations.stream()
                .noneMatch(loc -> loc.getTechnicianId().equals(105L)));
    }
    
    @Test
    void testFindLastKnownPositionsForActiveTechniciansWithShortTimeWindow() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);
        List<TechnicianLocation> activeLocations = 
                repository.findLastKnownPositionsForActiveTechnicians(since);
        
        assertNotNull(activeLocations);
        // Should find 3 or 4 technicians (depending on exact timing of -5 minute location)
        // Technician 102 location is at -5 minutes, which might be included or excluded
        assertTrue(activeLocations.size() >= 3 && activeLocations.size() <= 4,
                "Expected 3 or 4 active technicians, found " + activeLocations.size());
    }
    
    @Test
    void testFindLastKnownPositionsForActiveTechniciansWithNoResults() {
        LocalDateTime since = LocalDateTime.now().plusMinutes(1);
        List<TechnicianLocation> activeLocations = 
                repository.findLastKnownPositionsForActiveTechnicians(since);
        
        assertNotNull(activeLocations);
        // Should find no technicians (all locations are in the past)
        assertEquals(0, activeLocations.size());
    }
    
    @Test
    void testLocationPointIsSetAutomaticallyOnSave() {
        TechnicianLocation newLocation = TechnicianLocation.builder()
                .technicianId(201L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(85)
                .build();
        
        TechnicianLocation saved = repository.save(newLocation);
        
        // Verify Point is created automatically
        assertNotNull(saved.getLocation());
        assertEquals(-89.6501, saved.getLocation().getX(), 0.0001);
        assertEquals(39.7817, saved.getLocation().getY(), 0.0001);
    }
    
    @Test
    void testLocationPointUpdatesWhenLatitudeLongitudeChange() {
        TechnicianLocation location = repository.findAll().get(0);
        Long locationId = location.getId();
        
        // Update latitude and longitude
        location.setLatitude(40.0);
        location.setLongitude(-90.0);
        repository.save(location);
        
        // Retrieve and verify
        TechnicianLocation updated = repository.findById(locationId).orElseThrow();
        assertNotNull(updated.getLocation());
        assertEquals(-90.0, updated.getLocation().getX(), 0.0001);
        assertEquals(40.0, updated.getLocation().getY(), 0.0001);
    }
    
    @Test
    void testGetLatestLocationForTechnician() {
        // Add a newer location for technician 101
        LocalDateTime now = LocalDateTime.now();
        TechnicianLocation newerLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7900)
                .longitude(-89.6600)
                .accuracy(5.0)
                .timestamp(now)
                .batteryLevel(80)
                .build();
        repository.save(newerLocation);
        
        // Get latest location
        TechnicianLocation latest = repository.getLatestLocationForTechnician(101L).orElseThrow();
        
        assertEquals(101L, latest.getTechnicianId());
        assertEquals(39.7900, latest.getLatitude());
        assertEquals(-89.6600, latest.getLongitude());
    }
    
    @Test
    void testGetAllTechnicianLocations() {
        List<TechnicianLocation> allLocations = repository.getAllTechnicianLocations();
        
        assertNotNull(allLocations);
        // Should return 6 locations (one per technician)
        assertEquals(6, allLocations.size());
        
        // Verify all technician IDs are unique
        long uniqueTechnicianIds = allLocations.stream()
                .map(TechnicianLocation::getTechnicianId)
                .distinct()
                .count();
        assertEquals(6, uniqueTechnicianIds);
    }
    
    @Test
    void testFindByTechnicianIdAndTimestampBetween() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusMinutes(30);
        LocalDateTime endTime = now;
        
        List<TechnicianLocation> locations = repository
                .findByTechnicianIdAndTimestampBetweenOrderByTimestampDesc(101L, startTime, endTime);
        
        assertNotNull(locations);
        assertTrue(locations.size() > 0);
        assertEquals(101L, locations.get(0).getTechnicianId());
    }
    
    @Test
    void testCountByTechnicianId() {
        long count = repository.countByTechnicianId(101L);
        assertEquals(1, count);
        
        // Add another location for same technician
        TechnicianLocation newLocation = TechnicianLocation.builder()
                .technicianId(101L)
                .latitude(39.7900)
                .longitude(-89.6600)
                .accuracy(5.0)
                .timestamp(LocalDateTime.now())
                .batteryLevel(80)
                .build();
        repository.save(newLocation);
        
        count = repository.countByTechnicianId(101L);
        assertEquals(2, count);
    }
    
    @Test
    void testExistsByTechnicianId() {
        assertTrue(repository.existsByTechnicianId(101L));
        assertFalse(repository.existsByTechnicianId(999L));
    }
    
    @Test
    void testFindRecentLocations() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(10);
        List<TechnicianLocation> recentLocations = repository.findRecentLocations(since);
        
        assertNotNull(recentLocations);
        // Should find 5 recent locations (all except technician 105)
        assertEquals(5, recentLocations.size());
        
        // Verify all returned locations are recent
        for (TechnicianLocation location : recentLocations) {
            assertTrue(location.getTimestamp().isAfter(since) || 
                      location.getTimestamp().isEqual(since));
        }
    }
    
    @Test
    void testFindLatestLocationsForAllTechnicians() {
        List<TechnicianLocation> latestLocations = repository.findLatestLocationsForAllTechnicians();
        
        assertNotNull(latestLocations);
        assertEquals(6, latestLocations.size());
        
        // Verify all technician IDs are unique
        long uniqueCount = latestLocations.stream()
                .map(TechnicianLocation::getTechnicianId)
                .distinct()
                .count();
        assertEquals(6, uniqueCount);
    }
}
