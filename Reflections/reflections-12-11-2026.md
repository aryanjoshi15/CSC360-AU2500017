# Personal Reflections CSC360 (12/11/2026)

## Session Overview
Today's session marked an important transition in CSC360. Having concluded our midterm evaluations and presentations on JavaFX, Swing, multi-threaded event models, and 2D canvas graphics in October, the second half of the semester shifted focus toward digital image processing (DIP) and spatial image analysis. During today's lab and seminar, our group had an in-depth project discussion and conceptual review with the professor regarding our **End-Semester Group Project: Automated Classroom Head Counter from Still Images**.

Rather than relying on heavy, black-box deep learning frameworks (such as YOLO or Faster R-CNN), our project requirement is rooted in fundamental computer vision and discrete image processing principles implemented directly in Java. We explored the core image processing pipeline necessary to extract, segment, and count human heads from static classroom photographs using a structured sequence of transformations: **grayscale conversion, spatial filtering, gradient-based edge detection, binarization, mathematical morphology, and connected component / blob analysis**.

The professor scrutinized our proposed architecture, highlighted critical real-world edge cases in classroom environments (such as non-uniform lighting, perspective scale distortion, and occlusions), and guided us on how classical mathematical operators can be combined into a robust, high-performance detection pipeline.

---

## End-Semester Project Discussion: Classroom Head Counter from Images

### 1. Project Formulation & Classroom Environmental Challenges
The primary objective of our project is to develop a standalone Java application that takes a static photograph of a classroom or lecture hall as input and outputs the total count of students present by detecting their heads, along with visual bounding annotations highlighting each detected individual.

During our discussion, we identified several major computer vision challenges inherent to classroom imagery:
1. **Severe Illumination Gradients**: Classrooms rarely possess uniform lighting. Windows on one side introduce bright ambient sunlight, while ceiling fluorescent fixtures create local glare and deep shadows beneath desks and under chins.
2. **Perspective Projection & Scale Variance**: Due to camera perspective, students seated in the front row appear significantly larger (often $60 \times 60$ pixels or more per head) compared to students seated in distant rear rows ($15 \times 15$ pixels). A fixed-size detection window or static structuring element will inevitably fail across different depths.
3. **Complex Background Clutter & False Positives**: Lecture halls are densely packed with visual noise—curved chair backs, backpacks, coats hung over seats, blackboard borders, and overhead projector hardware—many of which exhibit curved edges that closely resemble human head contours.
4. **Hair, Skin Tone, and Contrast Variability**: Students exhibit diverse hair textures, colors (dark, blonde, grey), or baldness, wearing clothes that may have high or near-zero contrast against dark auditorium seating.
5. **Occlusion and Touching Silhouettes**: In crowded rows, adjacent students often sit shoulder-to-shoulder or lean toward each other, causing their binary contours to merge into a single contiguous blob.

---

### 2. Proposed Architectural Pipeline
To address these challenges methodically, our group presented a modular six-stage digital image processing pipeline:

```
[Input RGB Image]
       │
       ▼
[1. Grayscale Conversion] (Perceptual Luminance Weighting)
       │
       ▼
[2. Spatial Noise Filtering] (Gaussian Smoothing Kernel)
       │
       ▼
[3. Gradient & Edge Detection] (Sobel Operators / Canny Edge Detection)
       │
       ▼
[4. Image Binarization] (Otsu's Thresholding & Local Adaptive Filtering)
       │
       ▼
[5. Mathematical Morphology] (Dilation, Erosion, Opening, Closing & Hole Filling)
       │
       ▼
[6. Segmentation & Blob Counting] (Connected Component Labeling & Circularity Analysis)
       │
       ▼
[Output: Total Head Count & Annotated Bounding Boxes]
```

1. **Grayscale Conversion**: Eliminates redundant chrominance information while preserving structural luminance and spatial textures.
2. **Noise Reduction**: Applies spatial low-pass filtering (Gaussian convolution) to eliminate high-frequency sensor noise and minor texture artifacts without blurring critical boundary edges.
3. **Edge Detection**: Computes directional spatial intensity gradients ($G_x, G_y$) to outline the characteristic elliptical or circular perimeter of human heads and upper shoulder arches.
4. **Binarization**: Converts continuous gradient or intensity maps into discrete foreground/background binary images ($I(x, y) \in \{0, 1\}$).
5. **Morphological Filtering**: Employs structuring elements to eliminate isolated salt-and-pepper noise (opening), close discontinuities in fragmented head perimeters (closing), and fill internal voids.
6. **Feature Extraction & Counting**: Executes Connected Component Labeling (CCL) or the Circular Hough Transform (CHT), filtering candidates by geometric compactness, aspect ratio, and area thresholds to deliver an accurate final tally.

---

### 3. Class Discussion & Professor's Viva Scrutiny
The professor probed our methodology with several pointed technical questions during our project pitch:

- **Why Classical DIP instead of Deep Learning / CNNs?**
  - *Professor's Question*: "Why write an entire classical pipeline using grayscale, edges, and morphology when a modern YOLO or MobileNet model could detect people with an existing pre-trained checkpoint?"
  - *Our Group's Defense*:
    1. *Computational Efficiency & Portability*: Classical DIP algorithms run efficiently on standard CPU architectures within the Java Virtual Machine (JVM) without requiring dedicated CUDA-enabled GPUs, large runtime dependencies, or multi-gigabyte neural network weights.
    2. *Explainability & Determinism*: Every stage of a classical pipeline is governed by transparent mathematical formulations (convolution matrices, set theory, histogram statistics). In contrast to deep neural networks, intermediate outputs can be inspected, debugged, and mathematically tuned.
    3. *Pedagogical Value*: The core objective of CSC360 is mastering geometric transformations, spatial filters, and low-level raster manipulation rather than treating vision as an opaque black box.

- **Handling Scale Variance across Seating Rows:**
  - *Professor's Question*: "If a student in row 1 has a head radius of 30 pixels, but a student in row 8 has a head radius of 8 pixels, what structuring element size or area threshold will you choose? If you pick large, you delete the back rows; if you pick small, you split front-row heads into twenty tiny blobs."
  - *Our Strategy*: We discussed implementing a **depth-aware scale function** or a **Gaussian Scale Pyramid**. Because classroom cameras are typically mounted at a fixed elevated angle, the pixel height ($y$-coordinate) correlates directly with real-world distance from the lens. We can dynamically scale our structuring elements and area acceptance thresholds as a linear or quadratic function of the vertical image coordinate:
    $$r_{\text{expected}}(y) = r_{\text{min}} + (r_{\text{max}} - r_{\text{min}}) \cdot \left(\frac{y}{H}\right)^\gamma$$

- **Resolving Merged/Touching Heads:**
  - *Professor's Question*: "When two students sit adjacent and their heads form a single conjoined binary blob, standard connected component labeling will count them as '1'. How will your system isolate them?"
  - *Our Solution*: We discussed pairing mathematical morphology with the **Euclidean Distance Transform (EDT)** and **Watershed Segmentation**, or applying the **Circular Hough Transform (CHT)**. Even when the outer perimeter merges into an hourglass shape, the distance transform exhibits two distinct local maxima corresponding to the geometric centers of each head, allowing watershed ridges to cleanly bisect the conjoined components.

---

## Extra Research & Technical Deep Dive (AI Assisted)

To build a robust theoretical foundation for our implementation, we conducted an in-depth mathematical and algorithmic investigation into each stage of the head counting pipeline.

---

### 1. Grayscale Conversion & Perceptual Photometry

Digital color cameras capture light across three discrete spectral bands: Red ($R$), Green ($G$), and Blue ($B$). Transforming an RGB image into a single-channel grayscale representation requires mapping a 3D color vector $(R, G, B)$ to a scalar luminance value $Y \in [0, 255]$.

#### A. Flaws of Simple Channel Averaging
A naive approach is simple arithmetic averaging:
$$Y_{\text{naive}} = \frac{R + G + B}{3}$$
This formulation is fundamentally flawed for human feature detection. Human eye physiology contains photoreceptor cone cells (specifically L, M, and S cones) with non-uniform spectral sensitivities. Photopic human vision peaks at approximately $555\text{ nm}$ in the green spectrum, meaning human perception detects green light as significantly brighter than red or blue of identical physical radiance.

#### B. Perceptual Luminance Formulations
Standard image processing adheres to ITU-R recommendations:
- **ITU-R BT.601 (Standard Definition / NTSC)**:
  $$Y_{601} = 0.299 \cdot R + 0.587 \cdot G + 0.114 \cdot B$$
- **ITU-R BT.709 (High Definition / sRGB)**:
  $$Y_{709} = 0.2126 \cdot R + 0.7152 \cdot G + 0.0722 \cdot B$$

In an uncalibrated classroom image where hair may be dark brown against a blue-painted wall, perceptual weighting prevents artificial contrast inversion and retains true edge boundaries.

#### C. High-Speed Integer Arithmetic in Java
Floating-point multiplication across millions of pixels introduces unnecessary CPU cycle overhead. In performance-critical Java code, fixed-point integer arithmetic using bit-shifts provides identical precision:
$$Y \approx (77 \cdot R + 150 \cdot G + 29 \cdot B) \gg 8$$
*(Note: $77 + 150 + 29 = 256 = 2^8$, replacing floating-point multiplication with integer multiplication and a fast single-cycle bit-shift).*

---

### 2. Spatial Noise Suppression & Discrete 2D Convolution

Classroom sensors introduce thermal noise, photon shot noise, and sensor grain, which manifest as high-frequency intensity spikes. Applying gradient edge detection directly to raw grayscale images amplifies this high-frequency noise, producing thousands of false edges.

#### A. Mathematical 2D Convolution
Linear spatial filtering is defined by discrete 2D convolution between an image matrix $I$ and a filter kernel $K$ of dimensions $(2k+1) \times (2k+1)$:
$$(I * K)(x, y) = \sum_{u=-k}^k \sum_{v=-k}^k I(x - u, y - v) \cdot K(u, v)$$

#### B. The 2D Gaussian Kernel
The continuous 2D isotropic Gaussian distribution with standard deviation $\sigma$ is expressed as:
$$G(x, y) = \frac{1}{2\pi\sigma^2} \exp\left(-\frac{x^2 + y^2}{2\sigma^2}\right)$$
In discrete space, a $5 \times 5$ Gaussian kernel with $\sigma = 1.0$ approximates this distribution, normalized such that the sum of all coefficients equals 1:
$$K_G = \frac{1}{273} \begin{bmatrix}
1 & 4 & 7 & 4 & 1 \\
4 & 16 & 26 & 16 & 4 \\
7 & 26 & 41 & 26 & 7 \\
4 & 16 & 26 & 16 & 4 \\
1 & 4 & 7 & 4 & 1
\end{bmatrix}$$
Gaussian smoothing effectively suppresses random sensor noise while acting as an optimal scale-space filter for subsequent edge extraction.

---

### 3. Gradient Vector Operators & Edge Detection

An edge corresponds to an abrupt local discontinuity in image brightness. Mathematically, intensity changes are characterized by the spatial gradient vector $\nabla I$:
$$\nabla I(x, y) = \begin{bmatrix} G_x \\ G_y \end{bmatrix} = \begin{bmatrix} \frac{\partial I}{\partial x} \\ \frac{\partial I}{\partial y} \end{bmatrix}$$

#### A. The Sobel Operator
The Sobel operator approximates first-order spatial derivatives by combining 1D Gaussian smoothing in one axis with discrete central differencing in the orthogonal axis:
$$K_x = \begin{bmatrix} -1 & 0 & +1 \\ -2 & 0 & +2 \\ -1 & 0 & +1 \end{bmatrix}, \quad K_y = \begin{bmatrix} +1 & +2 & +1 \\ 0 & 0 & 0 \\ -1 & -2 & -1 \end{bmatrix}$$

For every pixel $(x, y)$:
- Horizontal Gradient: $G_x = (I * K_x)(x, y)$
- Vertical Gradient: $G_y = (I * K_y)(x, y)$
- **Gradient Magnitude**:
  $$\|\nabla I\| = \sqrt{G_x^2 + G_y^2} \quad \approx \quad |G_x| + |G_y| \quad \text{(Manhattan approximation for fast computation)}$$
- **Gradient Direction**:
  $$\theta(x, y) = \operatorname{atan2}(G_y, G_x)$$

#### B. Canny Edge Detection Pipeline
For identifying crisp, single-pixel-wide head contours, the Canny edge detector builds upon the Sobel operator through three rigorous post-processing stages:
1. **Non-Maximum Suppression (NMS)**: Thins thick edge ridges. The gradient direction $\theta(x, y)$ is quantized into one of four principal directions ($0^\circ, 45^\circ, 90^\circ, 135^\circ$). A pixel is preserved if and only if its gradient magnitude is strictly greater than its two neighbors along the gradient vector:
   $$\text{NMS}(x, y) = \begin{cases} \|\nabla I(x, y)\| & \text{if } \|\nabla I(x, y)\| \ge \max(\|\nabla I(p_1)\|, \|\nabla I(p_2)\|) \\ 0 & \text{otherwise} \end{cases}$$
2. **Double Thresholding**: Defines two scalar cutoffs, high threshold $T_{\text{high}}$ and low threshold $T_{\text{low}}$ (typically $T_{\text{high}} \approx 2 \cdot T_{\text{low}}$):
   - Strong edge: $\|\nabla I(x, y)\| \ge T_{\text{high}}$
   - Weak edge: $T_{\text{low}} \le \|\nabla I(x, y)\| < T_{\text{high}}$
   - Suppressed: $\|\nabla I(x, y)\| < T_{\text{low}}$
3. **Hysteresis Tracking**: Weak edges are retained if and only if they are connected to a strong edge through an 8-connected contiguous path, eliminating spurious isolated noise lines while preventing fragmented head boundaries.

---

### 4. Image Binarization & Adaptive Thresholding

Binarization segments an 8-bit grayscale image $I(x, y) \in [0, 255]$ into a binary mask $B(x, y) \in \{0, 1\}$:
$$B(x, y) = \begin{cases} 1 & \text{if } I(x, y) \ge T \\ 0 & \text{if } I(x, y) < T \end{cases}$$

#### A. Otsu's Global Optimal Thresholding
Otsu's algorithm automatically determines the optimal threshold $T^*$ by maximizing inter-class variance between background pixels ($C_0$) and foreground pixels ($C_1$).

Let normalized histogram probabilities be $p_i = \frac{n_i}{N}$ for intensity levels $i \in [0, 255]$.
For a candidate threshold $t$:
- Class probabilities:
  $$\omega_0(t) = \sum_{i=0}^t p_i, \quad \omega_1(t) = \sum_{i=t+1}^{255} p_i = 1 - \omega_0(t)$$
- Class means:
  $$\mu_0(t) = \frac{\sum_{i=0}^t i \cdot p_i}{\omega_0(t)}, \quad \mu_1(t) = \frac{\sum_{i=t+1}^{255} i \cdot p_i}{\omega_1(t)}$$
- Total image mean: $\mu_T = \omega_0(t)\mu_0(t) + \omega_1(t)\mu_1(t) = \sum_{i=0}^{255} i \cdot p_i$
- **Between-Class Variance**:
  $$\sigma_B^2(t) = \omega_0(t) \cdot \omega_1(t) \cdot \left(\mu_0(t) - \mu_1(t)\right)^2$$

The optimal threshold $T^*$ is selected as:
$$T^* = \arg\max_{0 \le t < 255} \sigma_B^2(t)$$

#### B. Limitation in Classrooms & Sauvola's Local Adaptive Thresholding
While Otsu is mathematically optimal for bimodal histograms with uniform backdrops, it degrades when classroom lighting exhibits strong spatial gradients (e.g., windows on the left casting deep shadows on the right). A global threshold $T^*$ causes the shadowed half of the classroom to be categorized entirely as foreground.

To solve this, **Sauvola's Local Adaptive Thresholding** computes a dynamic threshold $T(x, y)$ inside a local neighborhood window of size $W \times W$ (e.g., $25 \times 25$ pixels):
$$T(x, y) = m(x, y) \cdot \left[1 + k \cdot \left(\frac{s(x, y)}{R} - 1\right)\right]$$
Where:
- $m(x, y)$ is the local window mean intensity.
- $s(x, y)$ is the local window standard deviation.
- $R$ is the dynamic range of standard deviation ($R = 128$ for 8-bit images).
- $k$ is a user parameter (typically $k \in [0.2, 0.5]$).

Using integral images (summed-area tables), both $m(x, y)$ and $s(x, y)$ can be calculated in $O(1)$ constant time per pixel, enabling real-time local binarization regardless of image dimensions.

---

### 5. Mathematical Morphology & Structuring Elements

Binary images derived from raw thresholding or edge detection frequently suffer from broken perimeter lines, internal holes from hair shine, and false isolated specks. Mathematical morphology provides a rigorous set-theoretic framework for cleaning these geometric structures.

Let binary image $A \subset \mathbb{Z}^2$ and structuring element $B \subset \mathbb{Z}^2$. The reflection of $B$ is defined as $\hat{B} = \{-b \mid b \in B\}$, and translation by vector $z$ is $(B)_z = \{b + z \mid b \in B\}$.

#### A. Fundamental Morphological Operators
1. **Dilation ($A \oplus B$)**:
   $$A \oplus B = \{z \in \mathbb{Z}^2 \mid (\hat{B})_z \cap A \neq \emptyset\}$$
   *Effect*: Expands foreground boundaries, bridges small breaks in contours, and fills narrow gulfs.
2. **Erosion ($A \ominus B$)**:
   $$A \ominus B = \{z \in \mathbb{Z}^2 \mid (B)_z \subseteq A\}$$
   *Effect*: Shrinks foreground objects, eliminates thin extraneous filaments, and strips away isolated single-pixel noise.
3. **Morphological Opening ($A \circ B$)**:
   $$A \circ B = (A \ominus B) \oplus B$$
   *Effect*: An erosion followed by a dilation. Strips away fine foreground protrusions, disconnects weakly joined objects, and eliminates salt noise without substantially altering the size or geometric area of large objects.
4. **Morphological Closing ($A \bullet B$)**:
   $$A \bullet B = (A \oplus B) \ominus B$$
   *Effect*: A dilation followed by an erosion. Fills internal pepper holes (e.g. hair reflections or skin highlights) and fuses discontinuous edge contours into closed, solid shapes.

```
       DILATION                     EROSION
  [Expand Boundaries]         [Shrink Boundaries]
        A ⊕ B                       A ⊖ B
         ┌─┐                         ┌─┐
      ┌──┘ └──┐                      │ │
      │       │                   ┌──┘ └──┐
      └──┐ ┌──┘                   └──┐ ┌──┘
         └─┘                         └─┘
          │                           │
          ▼                           ▼
       CLOSING                      OPENING
   (A ⊕ B) ⊖ B                 (A ⊖ B) ⊕ B
 [Bridge Gaps & Holes]      [Remove Spurious Noise]
```

#### B. Structuring Element Geometry
In head detection, the geometric kernel shape is critical. A standard square kernel introduces rectangular distortion at object corners. Because human heads exhibit curved, circular, or elliptical contours, a **disk-shaped structuring element** $B_{\text{disk}}(r)$ is vastly superior:
$$B_{\text{disk}}(r) = \{(u, v) \in \mathbb{Z}^2 \mid u^2 + v^2 \le r^2\}$$

A disk structuring element preserves the smooth circular curvature of heads while rejecting non-circular geometric artifacts.

---

### 6. Segmentation, Connected Component Labeling, and Hough Transform

Once the image has been transformed into clean binary silhouettes, individual head candidates must be segmented, evaluated, and counted.

#### A. Two-Pass Connected Component Labeling (CCL) with Union-Find
To assign a unique identifier to every discrete foreground blob, we use the classical two-pass CCL algorithm over 8-connected neighborhoods:
1. **Pass 1 (Raster Scan)**:
   - Traverse pixels from top-left to bottom-right.
   - For every foreground pixel $p$, examine already-visited neighbors (North, North-West, North, North-East).
   - If no neighbors are labeled, assign a new label: $\text{label}(p) = \text{new\_label}()$.
   - If exactly one neighbor has a label, assign: $\text{label}(p) = \text{label}(\text{neighbor})$.
   - If multiple neighbors have different labels, assign the minimum label and record an equivalence relation in a **Disjoint-Set (Union-Find) data structure**.
2. **Pass 2 (Equivalence Resolution)**:
   - Flatten the disjoint-set trees using path compression: $\text{label}(p) = \text{find\_root}(\text{label}(p))$.
   - Re-index components into consecutive integer IDs $\{1, 2, \dots, N\}$.

#### B. Geometric Feature Filtering (Blob Pruning)
Not every connected component represents a student's head. We calculate geometric shape descriptors for each component $k$:
1. **Area ($A_k$)**: Total pixel count of component $k$.
2. **Perimeter ($P_k$)**: Count of boundary pixels with at least one 4-connected background neighbor.
3. **Bounding Box Aspect Ratio ($AR$)**:
   $$AR = \frac{\text{Width}}{\text{Height}}$$
   Human heads have an aspect ratio close to unity; components with $AR > 2.0$ (desks) or $AR < 0.4$ (vertical poles) are immediately pruned.
4. **Circularity / Isoperimetric Quotient ($C$)**:
   $$C = \frac{4\pi \cdot A_k}{P_k^2}$$
   - For a perfect circle, $C = 1.0$.
   - For human heads, empirical values typically fall within $0.65 \le C \le 0.95$.
   - Highly irregular background objects (chairs, bags, clothing folds) exhibit high perimeters relative to their area, yielding $C < 0.40$ and allowing effortless rejection.

#### C. Circular Hough Transform (CHT)
For crowded classrooms where heads overlap or touch, the Circular Hough Transform provides an alternative parameter-space voting mechanism.
A circle is represented parametrically by:
$$(x - a)^2 + (y - b)^2 = r^2$$
Where $(a, b)$ is the center coordinate and $r$ is the radius.

To avoid a prohibitive $O(W \cdot H \cdot R)$ 3D accumulator voting space, we utilize **gradient-directed voting**. For an edge pixel $(x, y)$ with gradient direction $\theta(x, y)$:
$$a = x \pm r \cdot \cos(\theta), \quad b = y \pm r \cdot \sin(\theta)$$
Accumulator cells $A[a, b, r]$ with vote counts exceeding a calibrated threshold indicate strong circular head hypotheses, enabling accurate center localization even when 40% of the head boundary is occluded.

---

### 7. Java High-Performance Implementation Architecture

Executing image processing in pure Java requires strict attention to memory layouts. A common novice pitfall is using `BufferedImage.getRGB(x, y)` and `setRGB(x, y)` in nested loops. Each invocation incurs coordinate bounds-checking, color model translation, bit-shifting, and method call overhead, which degrades frame processing from milliseconds to multiple seconds.

#### Direct Raster Memory Access Pattern
By accessing the underlying `DataBufferByte` or `DataBufferInt` directly, the image memory is mapped to a contiguous 1D array, allowing cache-friendly sequential scans:

```java
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

public class ImageProcessingPipeline {

    /**
     * Converts a 3-byte BGR BufferedImage into a high-speed 1D Grayscale byte array
     * using ITU-R BT.601 integer fixed-point arithmetic: Y = (77*R + 150*G + 29*B) >> 8
     */
    public static byte[] toGrayscaleFast(BufferedImage src) {
        final int width = src.getWidth();
        final int height = src.getHeight();
        final byte[] pixels = ((DataBufferByte) src.getRaster().getDataBuffer()).getData();
        final byte[] gray = new byte[width * height];

        final int pixelLength = (src.getType() == BufferedImage.TYPE_3BYTE_BGR) ? 3 : 4;
        int grayIdx = 0;

        for (int i = 0; i < pixels.length; i += pixelLength) {
            int b = pixels[i] & 0xFF;
            int g = pixels[i + 1] & 0xFF;
            int r = pixels[i + 2] & 0xFF;
            
            // Fixed-point ITU-R BT.601 formula
            int y = (77 * r + 150 * g + 29 * b) >> 8;
            gray[grayIdx++] = (byte) y;
        }
        return gray;
    }

    /**
     * Performs a 3x3 Sobel edge detection pass directly over a 1D grayscale buffer
     */
    public static byte[] applySobelEdge(byte[] gray, int width, int height) {
        byte[] edges = new byte[width * height];

        for (int y = 1; y < height - 1; y++) {
            int rowAbove = (y - 1) * width;
            int rowCurr  = y * width;
            int rowBelow = (y + 1) * width;

            for (int x = 1; x < width - 1; x++) {
                // Compute horizontal gradient Gx
                int gx = -(gray[rowAbove + x - 1] & 0xFF) + (gray[rowAbove + x + 1] & 0xFF)
                         - 2 * (gray[rowCurr + x - 1] & 0xFF) + 2 * (gray[rowCurr + x + 1] & 0xFF)
                         -(gray[rowBelow + x - 1] & 0xFF) + (gray[rowBelow + x + 1] & 0xFF);

                // Compute vertical gradient Gy
                int gy =  (gray[rowAbove + x - 1] & 0xFF) + 2 * (gray[rowAbove + x] & 0xFF) + (gray[rowAbove + x + 1] & 0xFF)
                         -(gray[rowBelow + x - 1] & 0xFF) - 2 * (gray[rowBelow + x] & 0xFF) - (gray[rowBelow + x + 1] & 0xFF);

                // Manhattan gradient magnitude approximation: |Gx| + |Gy|
                int mag = Math.min(255, Math.abs(gx) + Math.abs(gy));
                edges[rowCurr + x] = (byte) mag;
            }
        }
        return edges;
    }

    /**
     * Computes Otsu's optimal threshold on an 8-bit grayscale buffer
     */
    public static int computeOtsuThreshold(byte[] gray) {
        int[] histogram = new int[256];
        for (byte b : gray) {
            histogram[b & 0xFF]++;
        }

        int totalPixels = gray.length;
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        double maxVariance = 0.0;
        int optimalThreshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histogram[t];
            if (wB == 0) continue;
            int wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += (double) (t * histogram[t]);
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;

            // Between-class variance
            double varBetween = (double) wB * (double) wF * (mB - mF) * (mB - mF);

            if (varBetween > maxVariance) {
                maxVariance = varBetween;
                optimalThreshold = t;
            }
        }
        return optimalThreshold;
    }

    /**
     * Executes binary morphological dilation using a flat disk structuring element
     */
    public static byte[] dilateBinary(byte[] binary, int width, int height, int radius) {
        byte[] output = new byte[width * height];
        Arrays.fill(output, (byte) 0);

        for (int y = radius; y < height - radius; y++) {
            for (int x = radius; x < width - radius; x++) {
                if (binary[y * width + x] == (byte) 1) {
                    // Spread footprint across circular disk
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dx = -radius; dx <= radius; dx++) {
                            if (dx * dx + dy * dy <= radius * radius) {
                                output[(y + dy) * width + (x + dx)] = (byte) 1;
                            }
                        }
                    }
                }
            }
        }
        return output;
    }
}
```

---

## Key Takeaways & Implementation Roadmap

1. **Shift from Synthesis to Analysis**: The midterm project centered on geometric synthesis—drawing primitives, dispatching UI events, and managing rendering threads. The end-semester project tests an entirely complementary skill: spatial signal analysis, feature extraction, and computer vision heuristics.
2. **Pipelines are Multiplicative in Quality**: In a sequential image processing pipeline, errors compound rapidly. Poor grayscale conversion undermines gradient accuracy; noisy edge detection ruins binarization; fragmented binarization causes morphology to miscluster blobs. Investing in robust preprocessing (Gaussian smoothing and adaptive thresholding) is vital to downstream counting accuracy.
3. **Algorithmic Explainability**: Classical DIP techniques provide total visibility into why a given student was detected or missed. Adjusting a mathematical threshold or morphological kernel size offers immediate, predictable feedback that is impossible when dealing with black-box neural networks.
4. **Immediate Project Milestones**:
   - **Week 1**: Implement pure Java `ImageMatrix` wrappers around `DataBufferByte` for fast grayscale conversion, Gaussian convolution, and Sobel edge detection.
   - **Week 2**: Integrate Otsu and Sauvola adaptive thresholding routines, followed by disk-based morphological dilation, erosion, opening, and closing operations.
   - **Week 3**: Develop Two-Pass Connected Component Labeling with Disjoint-Set Union-Find and calculate geometric circularity/compactness metrics to prune non-head blobs.
   - **Week 4**: Construct the JavaFX user interface allowing users to load classroom images, view intermediate pipeline stages (grayscale $\to$ edges $\to$ binary $\to$ morphology), adjust sensitivity sliders dynamically, and display the final head count with overlay bounding circles.

