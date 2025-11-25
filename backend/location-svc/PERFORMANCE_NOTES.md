# Performance Notes

## Current Implementation

The LocationRepository uses correlated subqueries to find the latest location for each technician. This approach is:
- **Correct**: Returns accurate results
- **Simple**: Easy to understand and maintain
- **Adequate**: Performs well for typical FSM deployments (dozens to hundreds of technicians)

## Future Optimization Opportunities

### 1. Window Functions (PostgreSQL 8.4+)
For large-scale deployments with thousands of technicians:

```sql
SELECT * FROM (
  SELECT *,
         ROW_NUMBER() OVER (PARTITION BY technician_id ORDER BY timestamp DESC) as rn
  FROM technician_locations
  WHERE timestamp >= :since
) ranked
WHERE rn = 1;
```

### 2. Materialized View
For very high-frequency queries:

```sql
CREATE MATERIALIZED VIEW latest_technician_locations AS
SELECT DISTINCT ON (technician_id) *
FROM technician_locations
ORDER BY technician_id, timestamp DESC;

-- Refresh periodically or on-demand
REFRESH MATERIALIZED VIEW latest_technician_locations;
```

### 3. Partitioning
For historical data management:

```sql
-- Partition by timestamp (monthly)
CREATE TABLE technician_locations_2024_01 PARTITION OF technician_locations
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');
```

## When to Optimize

Consider these optimizations when:
- Location updates exceed 1000/second
- Active technician count exceeds 5000
- Query response time exceeds 100ms
- Database CPU usage consistently > 60%

## Current Benchmarks

With spatial index on location column:
- Radius queries (5km): < 10ms
- Latest positions query: < 50ms
- Typical dataset: 500 technicians, 1M historical records
