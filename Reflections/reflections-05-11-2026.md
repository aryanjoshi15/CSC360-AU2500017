# Personal Reflections CSC360 (05/11/2026)

## Session Overview
Today's session marked the commencement of the final project phase for CSC360. Following the completion of our midterm evaluations on hybrid JavaFX/Swing architectures and 2D canvas graphics, today was dedicated to brainstorming, scoping, and proposing our **End-Semester Group Project**. Each student group was tasked with pitching a comprehensive project concept to the professor and class, outlining its problem statement, graphical architecture, algorithm requirements, and implementation feasibility.

Our group presented a formal proposal for a **Desktop Pixel Art & Sprite Editor** built from scratch in Java and JavaFX. The core motivation was to construct a specialized raster manipulation studio tailored for retro 2D game assets, icon design, and low-resolution sprite animation. Rather than simply assembling existing UI buttons, our proposal focused on the low-level graphics engineering: custom discrete grid coordinate mapping, integer-only rasterization algorithms, multi-layer alpha compositing, memory-efficient transactional undo/redo stacks, and indexed color quantization.

The professor provided rigorous feedback, probing our design on rendering bottlenecks (such as the performance trap of drawing individual pixel rectangles), recursive stack overflows during flood fill operations, and memory consumption during continuous canvas editing. Importantly, the professor challenged us to consider whether a pure raster drawing tool offered sufficient algorithmic depth, suggesting that we should either implement advanced spatial convolution and quantization filters or expand our scope toward automated digital image processing. This critique proved to be the crucial catalyst that later inspired our computer vision and classroom head counter direction.

---

## End-Semester Project Proposal: Desktop Pixel Art & Sprite Editor

### 1. Proposal Motivation & Problem Statement
Modern digital illustration tools (such as Adobe Photoshop or Illustrator) are heavily optimized for continuous high-resolution imagery and vector paths. However, for indie game developers, retro game artists, and UI designers working on pixel-art games or embedded displays, continuous filtering and anti-aliasing are actively detrimental. Pixel art requires:
1. **Discrete Grid Precision**: Complete control over individual square pixels without accidental sub-pixel anti-aliasing or fractional color bleeding.
2. **Strict Palette Constraints**: Working within restricted color spaces (such as 8-bit indexed palettes, PICO-8, or custom 16-color swatches).
3. **Integer Scalability**: Clean nearest-neighbor integer scaling ($1\times, 2\times, 4\times, 8\times, 16\times$) so that sprites scale up sharply on modern 4K monitors without bilinear blur.

Our goal was to implement a lightweight, self-contained desktop pixel editor in Java that executes all raster operations directly on raw memory buffers without third-party graphics frameworks.

---

### 2. Proposed Architectural Subsystems & Toolset

```
┌─────────────────────────────────────────────────────────────┐
│                    JavaFX UI Shell                          │
│   [Tool Palette]   [Color Swatches]   [Layer Panel]         │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                  Viewport & Canvas Engine                   │
│   • World-to-Screen Affine Mapping (Pan & Zoom)             │
│   • Discrete Pixel Grid Overlay (Toggleable Lines)          │
│   • Nearest-Neighbor Scaling (Smoothing Disabled)           │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                 Rasterization & Drawing Engine              │
│   • Bresenham Line & Midpoint Circle Rasterization          │
│   • Iterative Scanline Flood Fill (Span Filling)            │
│   • Color Picker & Eyedropper Tool                          │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│             Layer Compositing & Memory Management           │
│   • Multi-Layer Stack (Porter-Duff Source-Over Blending)    │
│   • Bounding-Box Dirty-Rect Undo/Redo Engine                │
│   • Indexed Color Quantization & Palette Management         │
└─────────────────────────────────────────────────────────────┘
```

1. **Interactive Viewport**: Supports continuous zooming (from $1\times$ up to $64\times$) and panning using mouse-wheel and middle-click drag, with an optional 1-pixel grid overlay to demarcate discrete cell boundaries.
2. **Rasterization Tools**:
   - *Pencil & Eraser*: Single-pixel and customizable brush stamp editing.
   - *Line & Shape Tools*: Bresenham-based line and circle drawing.
   - *Bucket / Flood Fill*: Replacing contiguous regions of identical color.
   - *Color Eyedropper*: Sampling pixel colors directly from the active composite raster.
3. **Multi-Layer Alpha Compositing**: Enables separate background, character outline, and shading layers, blended via Porter-Duff "Source Over" equations.
4. **History Engine (Undo/Redo)**: A transactional command stack enabling non-destructive editing with bounded memory consumption.
5. **Export Pipeline**: Saving projects in a custom lightweight format, alongside exporting final flattened sprites as standard PNGs at both native ($1\times$) and scaled ($16\times$) resolutions.

---

### 3. Class Discussion & Professor's Critical Evaluation

During our presentation, the professor questioned several architectural assumptions and raised critical implementation caveats:

- **The 60 FPS Render Loop Trap**:
  - *Professor's Question*: "How do you plan to render the canvas? If the user zooms in $32\times$ on a $128 \times 128$ image, that is 16,384 discrete cells. Will you iterate through your data model and call `gc.fillRect()` for every visible pixel on every frame?"
  - *Our Realization*: Calling `gc.fillRect()` thousands of times per frame completely overwhelms the JavaFX Scene Graph and drops frame rates below 10 FPS. Instead, the canvas must maintain an off-screen `WritableImage` or `PixelBuffer`. Individual drawing actions mutate the backing integer buffer, and the entire viewport is rendered using a single `gc.drawImage()` call with image smoothing explicitly disabled (`gc.setImageSmoothing(false)`), allowing the GPU hardware to handle nearest-neighbor upscaling instantly.

- **Stack Overflow Vulnerability in Flood Fill**:
  - *Professor's Question*: "If a user clicks the bucket tool inside an empty $512 \times 512$ canvas, what happens if your flood fill algorithm uses standard 4-way recursive Depth-First Search (DFS)?"
  - *Our Defense*: A naive recursive flood fill requires $512 \times 512 = 262,144$ stack frames. Since the default JVM thread stack size (`-Xss`) is typically 1MB, this immediately causes a fatal `java.lang.StackOverflowError`. We explained that flood fill must be implemented iteratively using an explicit heap-allocated queue/stack, or preferably using **Scanline Span Filling** to minimize memory overhead.

- **Undo/Redo Memory Explosion**:
  - *Professor's Question*: "If your undo stack stores a full copy of the canvas for every single mouse drag or stroke, what is the memory footprint after 100 operations on a 4-layer $512 \times 512$ canvas?"
  - *Calculation*: $512 \times 512 \times 4\text{ bytes} \times 4\text{ layers} \times 100\text{ states} \approx 419\text{ MB}$ of raw heap memory!
  - *Our Strategy*: Rather than full raster snapshots, the undo engine must utilize a **Dirty-Rect Sparse Diff approach**: recording only the bounding box $[x_{\text{min}}, y_{\text{min}}, x_{\text{max}}, y_{\text{max}}]$ and the prior pixel array of the affected region per stroke.

- **Algorithmic Scope & Depth (The Evolutionary Pivot)**:
  - *Professor's Feedback*: "A pixel editor is an engaging software engineering project, but at its heart it is primarily an interactive GUI application with well-known raster primitives. To fulfill the advanced requirements of an end-semester computer graphics course, your project needs substantial algorithmic depth—such as spatial convolution filtering, automatic palette generation via color quantization (Median Cut/Octree), dithering, or automated computer vision analysis."
  - *Impact*: This evaluation was pivotal. It made our group realize that rather than confining ourselves to manual pixel drawing tools, we should explore digital image processing and spatial mathematical analysis—which ultimately led to our classroom head counter project focused on edge detection, thresholding, and morphology.

---

## Extra Research & Technical Deep Dive (AI Assisted)

To thoroughly evaluate our proposal's technical feasibility, we conducted an in-depth mathematical and algorithmic investigation into the core components of raster manipulation engines.

---

### 1. Viewport Coordinate Transformations & Nearest-Neighbor Interpolation

An interactive pixel editor must translate fluidly between two distinct coordinate systems:
1. **Screen/Viewport Space**: Continuous floating-point coordinates $(x_s, y_s)$ representing mouse events on the JavaFX UI canvas.
2. **World/Raster Space**: Discrete integer grid coordinates $(x_w, y_w) \in [0, W-1] \times [0, H-1]$ representing individual array indices in the pixel buffer.

#### A. Affine Transformation Equations
Let $\text{scale} \ge 1.0$ be the current zoom magnification factor, and let $(\text{pan}_x, \text{pan}_y)$ represent the viewport displacement vector in screen pixels:
- **World to Screen Mapping**:
  $$x_s = (x_w \cdot \text{scale}) + \text{pan}_x$$
  $$y_s = (y_w \cdot \text{scale}) + \text{pan}_y$$
- **Screen to World Mapping (Inverse Transformation)**:
  $$x_w = \left\lfloor \frac{x_s - \text{pan}_x}{\text{scale}} \right\rfloor$$
  $$y_w = \left\lfloor \frac{y_s - \text{pan}_y}{\text{scale}} \right\rfloor$$

Any calculated coordinate $(x_w, y_w)$ falling outside $[0, W-1] \times [0, H-1]$ is clipped, preventing `ArrayIndexOutOfBoundsException`.

#### B. Nearest-Neighbor vs Bilinear Interpolation
Standard graphics contexts apply bilinear filtering when scaling images, interpolating four neighboring pixels to smooth jagged edges:
$$I(x, y) \approx (1 - \Delta x)(1 - \Delta y) I_{00} + \Delta x (1 - \Delta y) I_{10} + (1 - \Delta x)\Delta y I_{01} + \Delta x \Delta y I_{11}$$
While ideal for photographic rendering, bilinear filtering destroys pixel art by generating unwanted muddy gradients between neighboring colored pixels. 

In pixel art, rendering must strictly enforce **Nearest-Neighbor Interpolation**:
$$I(x, y) = I\left(\operatorname{round}(x), \operatorname{round}(y)\right)$$
In JavaFX, setting `graphicsContext.setImageSmoothing(false)` configures the underlying Prism graphics pipeline to use nearest-neighbor texture sampling on the GPU, guaranteeing razor-sharp pixel edges at any zoom level.

---

### 2. Discrete Integer Rasterization: Bresenham's Algorithms

When a user drags a mouse rapidly across the canvas, operating systems report discrete mouse events separated by multiple pixels. Connecting these points with naive floating-point lines ($y = mx + c$) is computationally inefficient and introduces rounding gaps.

#### A. Bresenham's Line Algorithm
Bresenham's algorithm rasterizes lines using strictly integer addition, subtraction, and bit-shifts.

For a line segment from $(x_0, y_0)$ to $(x_1, y_1)$ with slope $0 \le m \le 1$:
Let $\Delta x = x_1 - x_0$ and $\Delta y = y_1 - y_0$.
The decision parameter $d_k$ at step $k$ determines whether the next pixel is $(x_k + 1, y_k)$ or $(x_k + 1, y_k + 1)$:
$$d_0 = 2\Delta y - \Delta x$$
At each subsequent iteration:
- If $d_k < 0$: The next pixel is $(x_k + 1, y_k)$, and:
  $$d_{k+1} = d_k + 2\Delta y$$
- If $d_k \ge 0$: The next pixel is $(x_k + 1, y_k + 1)$, and:
  $$d_{k+1} = d_k + 2\Delta y - 2\Delta x$$

By generalizing across all octants using absolute coordinate differences and directional step variables ($\text{step}_x = \pm 1, \text{step}_y = \pm 1$), continuous unbroken strokes are drawn regardless of cursor velocity.

#### B. Midpoint Circle Algorithm
For drawing circular brush stamps or circular selection shapes, the Midpoint Circle Algorithm evaluates the implicit circle equation:
$$f(x, y) = x^2 + y^2 - r^2$$
Exploiting 8-way octant symmetry, points are computed for a single $45^\circ$ arc and reflected across all 8 octants:
$$( \pm x, \pm y ), \quad ( \pm y, \pm x )$$
With initial decision parameter:
$$p_0 = 1 - r$$
At each step:
- If $p_k < 0$: Next midpoint is inside the circle; choose $(x_k + 1, y_k)$:
  $$p_{k+1} = p_k + 2x_{k+1} + 1$$
- If $p_k \ge 0$: Midpoint is outside or on the circle; choose $(x_k + 1, y_k - 1)$:
  $$p_{k+1} = p_k + 2x_{k+1} + 1 - 2y_{k+1}$$

---

### 3. High-Performance Flood Fill: Scanline Span Filling

Naive 4-way flood fill pushes individual adjacent pixels to an explicit queue. While avoiding JVM recursion limits, queue sizes can still balloon to hundreds of thousands of elements.

**Scanline Span Filling** drastically optimizes this process by processing continuous horizontal segments ("spans") of identical color in a single pass:
1. From a starting seed $(x, y)$, scan left and right along row $y$ to determine the contiguous horizontal boundaries $[x_{\text{start}}, x_{\text{end}}]$ possessing the target color.
2. Fill the entire segment $[x_{\text{start}}, x_{\text{end}}]$ with the replacement color immediately.
3. Inspect rows $y - 1$ and $y + 1$ directly above and below the segment.
4. For each contiguous sub-span of target color found in the adjacent rows, push only the rightmost coordinate of that sub-span onto an explicit stack.
5. Pop the stack and repeat until the stack is empty.

```
       Row y-1:   [░░░░░░░░░░░░░░░░░░░░░░░]  <- Inspect & push sub-spans
       Row y:     █████████████████████████  <- Fill continuous horizontal span
       Row y+1:   [░░░░░░░░░░░░░░░░░░░░░░░]  <- Inspect & push sub-spans
                  x_start             x_end
```

By pushing entire rows instead of individual coordinates, stack operations are reduced by a factor of 10 to 50, guaranteeing instantaneous flood fills even on large canvases.

---

### 4. Multi-Layer Alpha Compositing & Porter-Duff Mathematics

A modern pixel editor requires multiple transparent layers. Each pixel is stored as a 32-bit ARGB value (`0xAARRGGBB`), with alpha $\alpha \in [0, 255]$ representing opacity.

#### A. Porter-Duff "Source Over" Blending
When rendering a source layer ($S$) over an underlying destination layer ($D$), the resulting color $C_{\text{out}}$ and opacity $\alpha_{\text{out}}$ follow the Porter-Duff **Source Over** equation:
$$\alpha_{\text{out}} = \alpha_S + \alpha_D \cdot (1 - \alpha_S)$$
For normalized color components $C \in [0.0, 1.0]$:
$$C_{\text{out}} = \frac{C_S \cdot \alpha_S + C_D \cdot \alpha_D \cdot (1 - \alpha_S)}{\alpha_{\text{out}}}$$

#### B. Premultiplied Alpha for High-Speed Integer Blending
Performing floating-point division per color channel per pixel across multiple layers degrades performance. Pre-multiplying color components by alpha ($C' = C \cdot \alpha$) simplifies compositing into a purely additive operation:
$$C'_{\text{out}} = C'_S + C'_D \cdot (1 - \alpha_S)$$

In 8-bit integer arithmetic, channel extraction and blending can be executed using fast bitwise shifts:
```java
int aS = (srcColor >>> 24) & 0xFF;
int invA = 255 - aS;
int rOut = (rS * aS + rD * invA) / 255;
int gOut = (gS * aS + gD * invA) / 255;
int bOut = (bS * aS + bD * invA) / 255;
```

---

### 5. Color Quantization & Floyd-Steinberg Dithering

Restricting continuous 24-bit true-color images ($16.7$ million colors) to a compact palette (such as 16 or 32 colors) requires **Color Quantization**.

#### A. The Median Cut Algorithm
1. Enclose all unique RGB colors present in the artwork within a 3D bounding box in RGB space: $[\min(R), \max(R)] \times [\min(G), \max(G)] \times [\min(B), \max(B)]$.
2. Identify the color channel exhibiting the greatest dynamic range (longest box dimension).
3. Sort the colors along this channel and split the box at the median color into two equal sub-boxes.
4. Recursively repeat the splitting process until $K$ leaf boxes are obtained (where $K$ is the desired palette size, e.g., $K = 16$).
5. The representative palette color for each box is the arithmetic mean of all colors contained within it.

#### B. Floyd-Steinberg Error Diffusion Dithering
Quantization often causes harsh banding across smooth gradients. **Floyd-Steinberg Dithering** diffuses quantization error to adjacent unprocessed pixels, creating the optical illusion of intermediate shades:
When pixel $(x, y)$ with color $P$ is mapped to nearest palette color $Q$, the quantization error vector is:
$$e = P - Q$$
This error is distributed across four neighboring spatial pixels according to the diffusion matrix:
$$\begin{matrix}
 & \bullet & \frac{7}{16} \\
\frac{3}{16} & \frac{5}{16} & \frac{1}{16}
\end{matrix}$$

- Pixel $(x + 1, y) \leftarrow \text{Pixel}(x + 1, y) + e \cdot \frac{7}{16}$
- Pixel $(x - 1, y + 1) \leftarrow \text{Pixel}(x - 1, y + 1) + e \cdot \frac{3}{16}$
- Pixel $(x, y + 1) \leftarrow \text{Pixel}(x, y + 1) + e \cdot \frac{5}{16}$
- Pixel $(x + 1, y + 1) \leftarrow \text{Pixel}(x + 1, y + 1) + e \cdot \frac{1}{16}$

---

### 6. Java Implementation Prototype: Core Raster Engine

Below is our prototype implementation demonstrating direct 1D raster array mutation, generalized Bresenham line drawing, and iterative scanline flood filling:

```java
import java.util.ArrayDeque;
import java.util.Deque;

public class RasterCanvasModel {
    private final int width;
    private final int height;
    private final int[] pixels; // Stored as 32-bit ARGB (0xAARRGGBB)

    public RasterCanvasModel(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width * height];
    }

    public int getPixel(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 0;
        return pixels[y * width + x];
    }

    public void setPixel(int x, int y, int argb) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[y * width + x] = argb;
        }
    }

    /**
     * Bresenham's generalized line algorithm across all octants
     */
    public void drawLineBresenham(int x0, int y0, int x1, int y1, int color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            setPixel(x0, y0, color);
            if (x0 == x1 && y0 == y1) break;
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    /**
     * High-speed iterative Scanline Span Flood Fill
     */
    public void floodFillScanline(int startX, int startY, int fillColor) {
        if (startX < 0 || startX >= width || startY < 0 || startY >= height) return;
        int targetColor = getPixel(startX, startY);
        if (targetColor == fillColor) return;

        // Stack stores [x, y] coordinates of span seeds
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{startX, startY});

        while (!stack.isEmpty()) {
            int[] point = stack.pop();
            int px = point[0];
            int py = point[1];

            if (py < 0 || py >= height) continue;
            if (getPixel(px, py) != targetColor) continue;

            // Step 1: Scan west to find left boundary of span
            int left = px;
            while (left > 0 && getPixel(left - 1, py) == targetColor) {
                left--;
            }

            // Step 2: Scan east to find right boundary of span
            int right = px;
            while (right < width - 1 && getPixel(right + 1, py) == targetColor) {
                right++;
            }

            // Step 3: Fill horizontal span
            for (int x = left; x <= right; x++) {
                setPixel(x, py, fillColor);
            }

            // Step 4: Inspect rows above and below for adjacent seeds
            scanAdjacentRow(left, right, py - 1, targetColor, stack);
            scanAdjacentRow(left, right, py + 1, targetColor, stack);
        }
    }

    private void scanAdjacentRow(int left, int right, int y, int targetColor, Deque<int[]> stack) {
        if (y < 0 || y >= height) return;
        boolean spanFound = false;

        for (int x = left; x <= right; x++) {
            if (getPixel(x, y) == targetColor) {
                if (!spanFound) {
                    stack.push(new int[]{x, y});
                    spanFound = true;
                }
            } else {
                spanFound = false;
            }
        }
    }

    public int[] getRawPixels() {
        return pixels;
    }
}
```

---

## Key Takeaways & Project Evolution

1. **Low-Level Raster Mechanics vs High-Level UI**: Proposing the pixel editor deepened our understanding of why discrete pixel manipulation requires fundamentally different architectures than standard vector or retained-mode UI frameworks. Directly controlling contiguous 1D memory buffers (`int[] pixels`) and avoiding individual node instantiation is the only way to sustain 60 FPS performance.
2. **Algorithmic Safeguards in Graphics**: Designing algorithms like Scanline Flood Fill and Bresenham line rasterization demonstrated how memory constraints (avoiding recursion stack overflows) and numerical integer math (avoiding floating-point division) are critical in low-level systems engineering.
3. **The Pivot to Digital Image Processing**: The professor's evaluation was instrumental in elevating our project's ambition. While a pixel art editor is an exceptional demonstration of interactive graphics tools, the professor emphasized the importance of algorithmic analysis and computer vision. This feedback challenged our team to look beyond manual raster drawing tools and shift our focus toward automated spatial analysis, directly paving the way for our end-semester project on **Classroom Head Counting via Grayscale, Edge Detection, and Morphology**.

