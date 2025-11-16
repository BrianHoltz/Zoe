# Belly Fullness — design notes

This document summarizes where belly fullness is drawn in the codebase, two practical rendering options (fast linear-height "rising water" and area-proportional chord), the mathematics behind the area-proportional option, and an analysis of a pixel-by-pixel (scanline) filling strategy and its cost when the belly is oriented by an arbitrary heading.

## Where the code lives
- File: `src/org/holtz/zoe/zoeswing/BugIcon.java`
- Current method used to draw belly: `paintStomachPie(Graphics g, int xCorner, int yCorner)` — this draws a pie-slice using `g.fillArc(...)` based on `bug.strengthRatio()` and `bug.heading`.
- There is also an unused `paintStomachLevel(...)` that sketches a rectangular clipping approach; it is not used by `paintIcon(...)` currently.

## Two recommended visualization techniques
### 1) Linear-height rising-water (fast)
- Map fill fraction f ∈ [0,1] to a linear vertical height inside the circular thorax: h_pixels = f * diameter.
- Place a chord at distance d = r - h_pixels from the circle center along the bug's heading axis (r = diameter/2).
- Draw the filled region as the intersection of the circle and the half-plane on the "ass" (rear) side of the chord. Efficiently implemented by:
  1. Constructing a large polygon that represents the half-plane (or just a long rectangle) oriented so its edge is the chord through point p = center + n * d where n is the unit vector pointing toward the rear.
  2. Save the old Graphics2D clip, setClip(halfPlane), call g2.fill(new Ellipse2D.Double(...)) to fill the circle while clipped, then restore the clip.
- Pros: very cheap (one clip + one fill), visually intuitive as a water level, easy to implement and reuse, good for many icons.
- Cons: area inside the circle is not linear in height, so f does not map to exact fraction of area.

### Clarification: What the linear-height technique actually shows

The linear-height technique treats the filled region as a rectangle (or half‑plane oriented by heading) that is filled to height h = f × diameter, and then the circle is used as a viewport (clip) so you see rectangle ∩ circle. 

Equivalently: a rectangular bargraph is being viewed through the circular belly window; the four corner regions of the rectangle that lie outside the circle are clipped away, so the visible filled fraction inside the circle is not exactly f (unless f = 0, 0.5, or 1).

### 2) Area-proportional chord (accurate)
- Goal: choose chord height h so the area of the circular segment equals f * (π r^2).

Mathematics (continuous exact formulas)
- r = radius = diameter / 2
- A_total = π r^2
- A_target = f * A_total

Using the central angle θ (radians, 0..π) that subtends the segment:
- A_segment(θ) = (r^2 / 2) * (θ - sin θ)
- Relationship to chord height h:
  h = r * (1 - cos(θ/2))

To get h for a given f:
1. Compute A_target = f * π * r^2.
2. Numerically invert A_segment(θ) = A_target for θ ∈ [0,π]. Binary search (bisection) is robust:
   - low = 0, high = π
   - repeat ~30–40 iterations (or until error small): mid = (low+high)/2; if A_segment(mid) < A_target then low = mid else high = mid
   - θ = (low+high)/2; h = r * (1 - cos(θ/2))
3. As with the linear mode, place the chord at distance d = r - h along the heading axis and clip the circle with the half-plane to draw the filled region.

Performance notes
- The numeric solver cost is small: ~30 double-precision iterations of trigonometric functions per change in f. For typical icon sizes this is negligible compared to a per-pixel loop, and you can avoid repeating the solve when f hasn't changed.
- Use the clip+fill approach (setClip then draw ellipse) rather than constructing Area intersections to avoid extra allocations.
- Reuse shapes (Ellipse2D.Double, Path2D) as instance fields to reduce GC churn.

Recommendation
- Default to the linear-height method for UI performance and responsiveness.
- Offer area-proportional as an opt-in mode (toggle) for users who want exact area rendering. Cache results per (diameter, f) or only recompute when f changes.

## Is a pixel-by-pixel scanline fill equivalent? What's the cost?
A third possibility is to render the belly by filling pixels (or scanline segments) inside the circular thorax until you've painted the desired number of pixels equal to the target area fraction.

Two main variants of pixel approach:
1. Row-by-row on screen Y (screen-aligned rows): iterate horizontal scanlines (y) from rear to front, for each y compute the x-interval inside the circle, paint that span until the accumulated pixel count reaches the desired pixel count.
2. Row-by-row on heading-aligned rows: rotate coordinates so the "rear→head" axis becomes the positive Y' axis, then iterate Y' rows from rear toward head, for each Y' compute the span inside the circle in the rotated coordinate system and paint mapped back to screen.

Equivalence to chord method
- At infinite resolution, the chord method (area-proportional) is mathematically equivalent to placing a single chord such that the entire segment area equals the target area; filling pixels row-by-row until a target area is reached will converge to the same chord location as resolution → ∞. In other words, the final filled region will approximate the segment produced by the chord method.

Practical computational cost and complexity
- Screen-aligned scanlines (option 1) are simple and fast if the heading is vertical (or near vertical), but when the belly must fill along the bug's heading (which can be arbitrary), screen-aligned scanlines will not respect the intended ordering from ass→head; you'd be filling in a direction inconsistent with the heading.
- Heading-aligned scanlines (option 2) are conceptually simple but require a coordinate rotation per icon (to map screen pixels into the bug-local frame), and converting back to screen coordinates to paint. The per-pixel bounds (x-range for each scanline) are computed with the circle equation x^2 + y^2 ≤ r^2 in local coordinates. That means either:
  - Compute the rotated x-range for each local Y' and paint horizontal spans back on screen (must map spans to possibly non-integer screen-aligned pixels, so you likely must stroke small rectangles or individual pixels), or
  - Build a Mask/BufferedImage in local coordinates once and draw it transformed into screen coordinates (drawImage with AffineTransform) — this can be efficient but requires extra memory and handling for each diameter/heading combination.

Performance implications
- Per-pixel loops are more expensive than the clip+fill chord approach. The chord approach computes d (numeric solves if area-accurate), constructs a single polygon for the half-plane, then does one clipped ellipse fill — O(1) geometric ops and one rasterized shape fill inside the graphics pipeline.
- Pixel-by-pixel scanline filling does O(R) arithmetic per scanline where R ≈ diameter (number of rows) and then per-pixel operations across spans. For a diameter of 30–100 this is still small per icon, but if you draw hundreds or thousands of icons per frame, it adds up. The overhead increases further if you need to rotate per icon and can't reuse a mask.
- If headings are quantized (e.g., to a dozen discrete orientations), you can precompute masks per (diameter, heading) or per (diameter, heading-quantum) and reuse them to make the pixel approach cheap at draw time. Without quantization or caching, per-frame per-icon rasterization is costly.

Artifacts and precision
- Pixel filling naturally quantizes area to integer pixels and can give a stair-step look around the chord; you can mitigate with anti-aliased drawing or by using higher-resolution masks.
- The continuous chord method, rendered by Java2D, will produce correctly anti-aliased edges (assuming Graphics2D is configured) and avoids per-pixel dithering code.

When pixel-based approach makes sense
- You want an explicit discrete per-pixel control (for precise visual equal-area pixel counts), or you want to produce a cached raster mask for many identical icons, or you need to support a platform without convenient shape clipping.
- If you draw very few icons and prefer to avoid geometry math, the pixel approach is simple to reason about but still likely not necessary given the simplicity and speed of the chord+clip approach.

Summary of trade-offs
- Linear-height rising-water: simplest, fastest, visually pleasing. Not area-accurate.
- Area-proportional chord: slightly more math (numeric inversion) but exact in continuous geometry and efficient if you do a few iterations and cache results.
- Pixel-by-pixel scanline: possible and equivalent at high resolution; more CPU and memory cost unless you precompute masks for (diameter, heading) buckets. More work and (usually) unnecessary if you can rely on Java2D clipping and the chord math.

## Quantitative accuracy of the linear-height technique

I measured how far the linear-height (rectangle clipped by the circle) visualization can deviate from the true circular-segment area (the continuous, area-proportional value). Using a dense numeric scan over f ∈ [0,1] (continuous geometry, no rasterization), the results are:

- Worst-case absolute error: approximately 0.057710507 (fraction of the circle), i.e. about 5.771051 percentage points.
- Where it occurs:
  - Maximum under-count (visible < intended): at f ≈ 0.190505 the visible fraction g ≈ 0.13279449, so g − f ≈ −0.05771051 (understates fullness by ≈ 5.77 percentage points).
  - Maximum over-count (visible > intended): symmetric at f ≈ 0.809495 where g − f ≈ +0.05771051.
- Relative error context:
  - Near the under-count peak (f ≈ 0.19) the absolute error is large relative to the small fill: the visible fill is ≈ 30.3% smaller than intended (−30.3% relative error at that f).
  - Near the over-count peak (f ≈ 0.81) the relative error is ≈ +7.1%.

Notes and practical implications
- These numbers are for the continuous geometric area difference; rasterization (small diameters, integer pixels) will add quantization error which can either slightly increase or reduce the observed discrepancy. For very small icons (few pixels across) pixel rounding often dominates.
- The worst-case absolute error (~5.77 percentage points) is modest for many UI uses; if that magnitude is acceptable visually, the linear method is a good default because it is very cheap and simple.
- If you require the belly to represent area accurately, use the area-proportional chord method (invert the segment-area formula to compute chord height h for given f) — it's cheap (a small binary search over θ with ~30 iterations) and gives exact continuous-area matching; let the rasterizer handle pixelization.
- An alternative for pixel-perfect rendering is to precompute raster masks per (diameter, heading) and fill pixels until the desired pixel count; that is exact at raster level but costs memory and/or precomputation. Without caching, per-frame per-icon per-pixel filling is more expensive than the chord+clip approach.

Summary
- Linear-height = rectangle clipped by circle (fast, intuitive) with worst-case continuous-area error ≈ ±5.77 percentage points (peaks near f ≈ 0.19 and 0.81).
- Area-proportional = inexpensive to compute (numeric inversion) and recommended if you want the belly to represent fraction-of-area precisely.

## Implementation notes / checklist for a drop-in change in `BugIcon.java`
- Keep `paintStomachPie(...)` for backwards compatibility.
- Add a new `paintStomachRising(Graphics g, int xCorner, int yCorner)` that accepts a mode parameter (LINEAR or AREA_PROPORTIONAL).
- Reuse instance fields for `Ellipse2D.Double circleShape` and `Path2D.Double halfPlanePath` to avoid allocations.
- Use the clip+fill approach (save old clip, setClip(halfPlanePath), fill(circleShape), restore clip).
- For AREA_PROPORTIONAL implement a small binary-search inversion to compute θ and then h.
- Cache h per (diameter, f) or compute only when f changes.

## Recommendation
- Implement and default to the linear-height rising-water view. Add an area-proportional mode for accuracy and enable it only when requested (or when debugging). If you need pixel-level equivalence, precompute masks for a small set of diameters/headings and draw those images.

---

If you want, I can now implement the change in `BugIcon.java` (adding both modes, reusing shapes, and wiring a setter to toggle modes) and run a quick build/check. Which mode should be the default? (I recommend `LINEAR`.)
