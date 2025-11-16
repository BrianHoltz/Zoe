# Zoe Performance Analysis & Optimization Recommendations

## Executive Summary

This document analyzes the Zoe artificial life simulation codebase and identifies performance bottlenecks with straightforward optimization opportunities. The primary performance issue is **O(n²) spatial queries** that dominate execution time as the number of bugs increases.

## Architecture Overview

Zoe is a Java-based artificial life simulation where:
- **World** manages the simulation and contains all **Bug** objects
- **Bug** objects execute genetic programs via **ZoelVM** virtual machines
- Bugs interact through spatial queries (finding closest objects, vision, etc.)
- Each cycle, every bug executes its phenotype, which may involve multiple spatial queries

## Critical Performance Bottlenecks

### 1. O(n²) Spatial Query Complexity ⚠️ **CRITICAL**

**Location**: `World.closestOf()` (lines 263-284)

**Problem**: 
- Every time a bug needs to find the closest object (for looking, biting, mating), it iterates through ALL bugs in the world
- With N bugs, this is O(N) per query
- Each bug may make multiple queries per cycle (look, bite, mate operations)
- **Total complexity: O(N²) per cycle**

**Impact**: 
- With 100 bugs: ~10,000 range calculations per cycle
- With 1000 bugs: ~1,000,000 range calculations per cycle
- Performance degrades quadratically as population grows

**Current Code**:
```java
private static ZObject closestOf(List<? extends ZObject> objects, Bug from, 
                                  double maxRange, double minRange) {
    Iterator<? extends ZObject> objItr = objects.listIterator();
    ZObject closest = null;
    double closestRange = 100000;
    while (objItr.hasNext()) {
        ZObject obj = objItr.next();
        if (obj == from) continue;
        double range = from.range(obj);  // Expensive calculation
        if (range > maxRange) continue;
        if (range <= minRange) continue;
        if (range >= closestRange) continue;
        if (!from.canSee(obj)) continue;  // Checked AFTER range calculation
        closest = obj;
        closestRange = range;
    }
    return closest;
}
```

**Optimization**: Implement spatial indexing (grid-based or quadtree)

### 2. Inefficient Range Calculations

**Location**: `ZObject.range()` and `Point.bearing()` 

**Problem**:
- Range calculations check 5 different toroidal wraparound paths (direct, top, sides, 2 corners) for EVERY comparison
- Most objects are close enough that only the direct path matters
- `canSee()` is called AFTER expensive range calculation

**Impact**: 
- Each range calculation does 5 distance computations
- With 1000 bugs making queries, this is 5,000,000+ distance calculations per cycle

**Optimization**: 
- Early exit when direct distance is clearly shortest
- Check `canSee()` before expensive range calculations
- Cache range calculations when possible

### 3. Redundant Object Creation

**Location**: `ZObject.location()` (line 30-32)

**Problem**:
- Creates new `Point` object on every call
- Called frequently in hot paths (range calculations, bearing calculations)

**Impact**: 
- Unnecessary object allocation and garbage collection pressure
- Millions of temporary objects created per cycle

**Current Code**:
```java
public Point location() {
    return new Point(x, y);  // New object every time!
}
```

**Optimization**: Cache location or use direct x/y access

### 4. Inefficient Data Structures

**Location**: `World.bugs` (line 96)

**Problem**:
- Uses `ArrayList<Bug>` which has O(n) removal cost
- Bugs are removed frequently (when they die)
- Iterator removal is O(n) due to array shifting

**Impact**: 
- Removing dead bugs becomes expensive with large populations

**Optimization**: Use `LinkedList` or batch removals

### 5. Redundant Mass Calculations

**Location**: `Bug.canSee()` (lines 382-404)

**Problem**:
- `mass()` is called multiple times per visibility check
- `mass()` involves floating-point calculations (area of circle)
- Called for every object in `closestOf()` loop

**Impact**: 
- Redundant calculations in hot path

**Optimization**: Cache mass or calculate once per cycle

## Recommended Optimizations (Priority Order)

### Priority 1: Spatial Grid Indexing (High Impact, Medium Effort)

**Implementation**: Add a spatial grid to `World` class

```java
// In World.java
private static final int GRID_SIZE = 50; // Adjust based on VisionRange
private Map<Integer, List<Bug>> spatialGrid = new HashMap<>();

private int gridKey(int gridX, int gridY) {
    return gridY * (width / GRID_SIZE + 1) + gridX;
}

private void updateSpatialGrid() {
    spatialGrid.clear();
    for (Bug bug : bugs) {
        if (bug.isGone()) continue;
        int gridX = (int)(bug.x() / GRID_SIZE);
        int gridY = (int)(bug.y() / GRID_SIZE);
        int key = gridKey(gridX, gridY);
        spatialGrid.computeIfAbsent(key, k -> new ArrayList<>()).add(bug);
    }
}

private ZObject closestOf(Bug from, double maxRange, double minRange) {
    // Only check bugs in nearby grid cells
    int centerX = (int)(from.x() / GRID_SIZE);
    int centerY = (int)(from.y() / GRID_SIZE);
    int radius = (int)Math.ceil(maxRange / GRID_SIZE) + 1;
    
    ZObject closest = null;
    double closestRange = maxRange;
    
    for (int dx = -radius; dx <= radius; dx++) {
        for (int dy = -radius; dy <= radius; dy++) {
            int key = gridKey(centerX + dx, centerY + dy);
            List<Bug> cellBugs = spatialGrid.get(key);
            if (cellBugs == null) continue;
            
            for (Bug obj : cellBugs) {
                if (obj == from || obj.isGone()) continue;
                // Early visibility check
                if (!from.canSee(obj)) continue;
                
                double range = from.range(obj);
                if (range > maxRange || range <= minRange || range >= closestRange) continue;
                
                closest = obj;
                closestRange = range;
            }
        }
    }
    return closest;
}
```

**Expected Improvement**: O(n²) → O(n) for spatial queries
**Complexity**: Medium (requires grid maintenance)

### Priority 2: Optimize Range Calculations (High Impact, Low Effort)

**Implementation**: Early exit and visibility check reordering

```java
// In World.closestOf()
private static ZObject closestOf(...) {
    // ... existing code ...
    while (objItr.hasNext()) {
        ZObject obj = objItr.next();
        if (obj == from) continue;
        
        // Check visibility FIRST (cheaper than range calculation)
        if (!from.canSee(obj)) continue;
        
        // Quick distance check before expensive toroidal calculation
        double dx = from.x() - obj.x();
        double dy = from.y() - obj.y();
        double quickDist = Math.sqrt(dx*dx + dy*dy);
        if (quickDist > maxRange * 1.5) continue; // Rough check
        
        // Now do expensive toroidal range calculation
        double range = from.range(obj);
        // ... rest of logic ...
    }
}
```

**Expected Improvement**: 30-50% reduction in range calculation overhead
**Complexity**: Low

### Priority 3: Cache Location Objects (Medium Impact, Low Effort)

**Implementation**: Cache Point in ZObject

```java
// In ZObject.java
private Point cachedLocation;
private boolean locationDirty = true;

public Point location() {
    if (locationDirty || cachedLocation == null) {
        cachedLocation = new Point(x, y);
        locationDirty = false;
    }
    return cachedLocation;
}

public void setXY(double newX, double newY) {
    x = newX;
    y = newY;
    // ... existing wrap logic ...
    locationDirty = true;  // Mark cache as invalid
    repaint();
}
```

**Expected Improvement**: Reduced GC pressure, 10-20% improvement
**Complexity**: Low

### Priority 4: Optimize canSee() Checks (Medium Impact, Low Effort)

**Implementation**: Cache mass calculations

```java
// In Bug.java
private double cachedMass = -1;
private boolean massDirty = true;

public double mass() {
    if (massDirty) {
        cachedMass = Math.PI * diameter * diameter / 4;
        massDirty = false;
    }
    return cachedMass;
}

// Mark mass dirty when diameter changes
private void grow(double extraStrength) {
    // ... existing code ...
    massDirty = true;  // Mark cache invalid
}
```

**Expected Improvement**: 5-10% improvement in visibility checks
**Complexity**: Low

### Priority 5: Batch Bug Removals (Low Impact, Low Effort)

**Implementation**: Collect dead bugs and remove in batch

```java
// In World.java
public void nextWorldCycle() {
    // ... existing code ...
    
    // Batch remove dead bugs at end of cycle
    if (cycle % 10 == 0) {  // Every 10 cycles
        bugs.removeIf(Bug::isGone);
    }
}
```

**Expected Improvement**: Slight improvement with large populations
**Complexity**: Very Low

## Performance Testing Recommendations

1. **Benchmark current performance**:
   - Measure cycles per second with different bug counts (100, 500, 1000)
   - Profile with JProfiler or VisualVM to identify hot spots

2. **Test optimizations incrementally**:
   - Apply one optimization at a time
   - Measure improvement after each change
   - Ensure correctness (same simulation results)

3. **Key metrics to track**:
   - Cycles per second
   - Memory usage (heap)
   - GC frequency and duration
   - CPU usage per core

## Implementation Priority

1. **Start with Priority 2** (range optimization) - easiest, immediate benefit
2. **Then Priority 3** (location caching) - easy, reduces GC
3. **Then Priority 1** (spatial grid) - biggest impact, requires more testing
4. **Finally Priority 4 & 5** - polish and fine-tuning

## Additional Considerations

- **Thread Safety**: Current code is single-threaded. Spatial grid should remain single-threaded unless full threading is added
- **Memory vs Speed**: Spatial grid uses more memory but dramatically improves speed
- **Grid Size Tuning**: Grid cell size should be ~2x VisionRange for optimal performance
- **Backward Compatibility**: All optimizations should maintain same simulation behavior

## Conclusion

The primary performance bottleneck is the O(n²) spatial query complexity. Implementing a spatial grid (Priority 1) will provide the largest performance improvement, potentially allowing 10x more bugs to run at the same speed. The other optimizations provide incremental improvements and are easier to implement.

