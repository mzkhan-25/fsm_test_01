package com.fsm.location.domain.repository;

import com.fsm.location.domain.model.TechnicianLocation;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for TechnicianLocation entity.
 * Provides database persistence operations for technician locations.
 * 
 * This repository is part of the Location Services bounded context and
 * supports real-time tracking of technician positions.
 */
@Repository
public interface LocationRepository extends JpaRepository<TechnicianLocation, Long> {
    
    /**
     * Find the latest location for a specific technician.
     * This is the most common query for displaying a technician's current position.
     * 
     * @param technicianId the ID of the technician
     * @return the most recent location for the technician, if any
     */
    Optional<TechnicianLocation> findFirstByTechnicianIdOrderByTimestampDesc(Long technicianId);
    
    /**
     * Find all locations for a specific technician, ordered by timestamp (most recent first).
     * Useful for viewing location history.
     * 
     * @param technicianId the ID of the technician
     * @return list of all locations for the technician
     */
    List<TechnicianLocation> findByTechnicianIdOrderByTimestampDesc(Long technicianId);
    
    /**
     * Find locations for a technician within a time range.
     * Useful for viewing location history within a specific period.
     * 
     * @param technicianId the ID of the technician
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return list of locations within the time range
     */
    List<TechnicianLocation> findByTechnicianIdAndTimestampBetweenOrderByTimestampDesc(
            Long technicianId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Get the latest location for each technician (for map display).
     * This query retrieves only the most recent location record for each technician.
     * 
     * @return list of the latest locations for all technicians
     */
    @Query("SELECT tl FROM TechnicianLocation tl WHERE tl.timestamp = " +
           "(SELECT MAX(tl2.timestamp) FROM TechnicianLocation tl2 WHERE tl2.technicianId = tl.technicianId)")
    List<TechnicianLocation> findLatestLocationsForAllTechnicians();
    
    /**
     * Find all technicians with recent locations (within the last N minutes).
     * Useful for determining which technicians are currently active.
     * 
     * @param since the timestamp to check from
     * @return list of recent locations
     */
    @Query("SELECT tl FROM TechnicianLocation tl WHERE tl.timestamp >= :since AND tl.timestamp = " +
           "(SELECT MAX(tl2.timestamp) FROM TechnicianLocation tl2 WHERE tl2.technicianId = tl.technicianId)")
    List<TechnicianLocation> findRecentLocations(@Param("since") LocalDateTime since);
    
    /**
     * Count locations for a specific technician.
     * 
     * @param technicianId the ID of the technician
     * @return the number of location records for this technician
     */
    long countByTechnicianId(Long technicianId);
    
    /**
     * Check if a technician has any location records.
     * 
     * @param technicianId the ID of the technician
     * @return true if the technician has at least one location record
     */
    boolean existsByTechnicianId(Long technicianId);
    
    /**
     * Find all technicians within a specified radius (in meters) of a given point.
     * Uses PostGIS ST_DWithin function for efficient geospatial queries.
     * This is a key query for technician assignment based on proximity.
     * 
     * Note: For geography type, ST_DWithin distance is in meters by default.
     * 
     * @param point the center point to search from (longitude, latitude)
     * @param radiusMeters the search radius in meters
     * @return list of technician locations within the radius, ordered by distance (closest first)
     */
    @Query(value = "SELECT tl.* FROM technician_locations tl " +
           "WHERE tl.location IS NOT NULL " +
           "AND ST_DWithin(tl.location, CAST(:point AS geography), :radiusMeters) " +
           "AND tl.timestamp = (SELECT MAX(tl2.timestamp) FROM technician_locations tl2 " +
           "                    WHERE tl2.technician_id = tl.technician_id) " +
           "ORDER BY ST_Distance(tl.location, CAST(:point AS geography))",
           nativeQuery = true)
    List<TechnicianLocation> findTechniciansWithinRadius(
            @Param("point") Point point,
            @Param("radiusMeters") double radiusMeters);
    
    /**
     * Find all technicians within a specified radius of a given point,
     * filtering by recent activity (locations within a time threshold).
     * This ensures we only consider currently active technicians.
     * 
     * @param point the center point to search from (longitude, latitude)
     * @param radiusMeters the search radius in meters
     * @param since the timestamp threshold for recent activity
     * @return list of active technician locations within the radius, ordered by distance
     */
    @Query(value = "SELECT tl.* FROM technician_locations tl " +
           "WHERE tl.location IS NOT NULL " +
           "AND tl.timestamp >= :since " +
           "AND ST_DWithin(tl.location, CAST(:point AS geography), :radiusMeters) " +
           "AND tl.timestamp = (SELECT MAX(tl2.timestamp) FROM technician_locations tl2 " +
           "                    WHERE tl2.technician_id = tl.technician_id) " +
           "ORDER BY ST_Distance(tl.location, CAST(:point AS geography))",
           nativeQuery = true)
    List<TechnicianLocation> findActiveTechniciansWithinRadius(
            @Param("point") Point point,
            @Param("radiusMeters") double radiusMeters,
            @Param("since") LocalDateTime since);
    
    /**
     * Get the last known position for each active technician.
     * Active is defined as having a location update within the specified time threshold.
     * Returns only the most recent location for each technician.
     * 
     * @param since the timestamp threshold for active status
     * @return list of the latest locations for all active technicians
     */
    @Query("SELECT tl FROM TechnicianLocation tl " +
           "WHERE tl.timestamp >= :since " +
           "AND tl.timestamp = (SELECT MAX(tl2.timestamp) FROM TechnicianLocation tl2 " +
           "                    WHERE tl2.technicianId = tl.technicianId)")
    List<TechnicianLocation> findLastKnownPositionsForActiveTechnicians(
            @Param("since") LocalDateTime since);
    
    /**
     * Convenience method to get the latest location for a technician.
     * Returns an Optional containing the most recent location.
     * 
     * @param technicianId the ID of the technician
     * @return the latest location for the technician
     */
    default Optional<TechnicianLocation> getLatestLocationForTechnician(Long technicianId) {
        return findFirstByTechnicianIdOrderByTimestampDesc(technicianId);
    }
    
    /**
     * Convenience method to get all latest locations for map display.
     * Returns the most recent location for each technician.
     * 
     * @return list of the latest locations for all technicians
     */
    default List<TechnicianLocation> getAllTechnicianLocations() {
        return findLatestLocationsForAllTechnicians();
    }
    
    /**
     * Returns hardcoded sample locations for initial development and testing.
     * This method provides sample locations for 6 technicians in the Springfield, IL area.
     * 
     * @return list of hardcoded technician locations
     */
    default List<TechnicianLocation> getHardcodedLocations() {
        LocalDateTime now = LocalDateTime.now();
        
        return Arrays.asList(
            // Technician 1 - Downtown Springfield
            TechnicianLocation.builder()
                .id(1L)
                .technicianId(101L)
                .latitude(39.7817)
                .longitude(-89.6501)
                .accuracy(5.0)
                .timestamp(now.minusMinutes(2))
                .batteryLevel(85)
                .createdAt(now.minusMinutes(2))
                .build(),
            
            // Technician 2 - East Springfield
            TechnicianLocation.builder()
                .id(2L)
                .technicianId(102L)
                .latitude(39.7845)
                .longitude(-89.6302)
                .accuracy(8.0)
                .timestamp(now.minusMinutes(5))
                .batteryLevel(62)
                .createdAt(now.minusMinutes(5))
                .build(),
            
            // Technician 3 - West Springfield
            TechnicianLocation.builder()
                .id(3L)
                .technicianId(103L)
                .latitude(39.7789)
                .longitude(-89.6720)
                .accuracy(12.0)
                .timestamp(now.minusMinutes(8))
                .batteryLevel(45)
                .createdAt(now.minusMinutes(8))
                .build(),
            
            // Technician 4 - North Springfield
            TechnicianLocation.builder()
                .id(4L)
                .technicianId(104L)
                .latitude(39.8025)
                .longitude(-89.6489)
                .accuracy(6.5)
                .timestamp(now.minusMinutes(1))
                .batteryLevel(92)
                .createdAt(now.minusMinutes(1))
                .build(),
            
            // Technician 5 - South Springfield
            TechnicianLocation.builder()
                .id(5L)
                .technicianId(105L)
                .latitude(39.7612)
                .longitude(-89.6550)
                .accuracy(15.0)
                .timestamp(now.minusMinutes(12))
                .batteryLevel(18)
                .createdAt(now.minusMinutes(12))
                .build(),
            
            // Technician 6 - Near State Capitol
            TechnicianLocation.builder()
                .id(6L)
                .technicianId(106L)
                .latitude(39.7983)
                .longitude(-89.6544)
                .accuracy(4.0)
                .timestamp(now.minusMinutes(3))
                .batteryLevel(78)
                .createdAt(now.minusMinutes(3))
                .build()
        );
    }
}
