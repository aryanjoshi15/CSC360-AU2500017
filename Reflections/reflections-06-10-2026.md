# Personal Reflections CSC360 (06/10/2026)

## Session Overview
Today marked the first session of our CSC360 midterm project presentations and group viva evaluations. Each group was called up to demonstrate their working software, walk through their codebase, explain their architectural and design decisions, and respond to rigorous viva questions from the professor. The evaluation went far beyond whether the code simply compiled or ran—the professor scrutinized design patterns, coordinate mapping mathematics, event listener lifecycles, edge cases, thread safety, and UI responsiveness.

Three distinct projects were presented and critically reviewed during this session:
1. A JavaFX application to draw triangles by inserting three linear equations as text.
2. A JavaFX canvas application to draw circles on right-click and connect them with directional arrows.
3. A JavaFX program to render a dynamic binary tree structure visually.

---

## Project 1: JavaFX Program to Draw Triangles by Inserting 3 Equations as Text

### Class Presentation & Viva Discussion
- **Implementation Approach**: The presenting team built a JavaFX interface where users input three linear equations (such as $a_1x + b_1y = c_1$, $a_2x + b_2y = c_2$, and $a_3x + b_3y = c_3$) into text fields. On clicking a "Draw" button, the application parses the equations, computes the three intersection points, and draws the resulting triangle on a canvas or polygon shape.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *Equation Parsing & Input Validation*: How does the parser handle arbitrary forms such as $3x + 2y = 5$, $3x + 2y - 5 = 0$, $y = 2x + 1$, or missing coefficients like $x - y = 4$? The group had hardcoded fixed regular expressions that broke when spaces were omitted or when signs were reversed.
  - *Mathematical Degeneracy*: What happens if two lines are parallel (no intersection point, determinant is 0) or coincident (infinitely many intersections)? What if all three lines intersect at a single concurrent point, forming a point rather than a triangle?
  - *Coordinate System Inversion*: In standard Cartesian geometry, the origin $(0, 0)$ is at the center with $+y$ pointing upwards. In JavaFX, $(0, 0)$ is the top-left corner with $+y$ pointing downwards. The group explained their manual transformation formula: $X_{screen} = X_{center} + x \cdot \text{scale}$, $Y_{screen} = Y_{center} - y \cdot \text{scale}$.
  - *Auto-scaling & Viewport Clipping*: When lines intersect at coordinates like $(250, 400)$ on an $800 \times 600$ window, the vertices either clip off-screen or render as microscopic specks without dynamic viewport normalization.

### Extra Research & Technical Deep Dive (AI Assisted)
- **Linear Algebra & System Solvers**:
  - For each pair of lines represented in standard matrix form $A\mathbf{x} = \mathbf{b}$:
    $$\begin{pmatrix} a_1 & b_1 \\ a_2 & b_2 \end{pmatrix} \begin{pmatrix} x \\ y \end{pmatrix} = \begin{pmatrix} c_1 \\ c_2 \end{pmatrix}$$
  - The unique intersection exists if and only if the determinant is non-zero:
    $$\det(A) = a_1 b_2 - a_2 b_1 \neq 0$$
  - Using **Cramer's Rule**, the vertex coordinates $(x, y)$ can be evaluated analytically in $O(1)$:
    $$x = \frac{c_1 b_2 - c_2 b_1}{a_1 b_2 - a_2 b_1}, \quad y = \frac{a_1 c_2 - a_2 c_1}{a_1 b_2 - a_2 b_1}$$
  - In enterprise production code, using libraries like **Apache Commons Math** (`LUDecomposition` or `SingularValueDecomposition`) avoids numerical instability when lines are nearly parallel (ill-conditioned systems with condition number $\kappa(A) \gg 1$).
- **Degeneracy & Collinearity Testing via the Shoelace Formula**:
  - Once the three intersection points $P_1(x_1, y_1)$, $P_2(x_2, y_2)$, and $P_3(x_3, y_3)$ are computed, non-degeneracy requires the signed area of the triangle to exceed an epsilon threshold ($\epsilon \approx 10^{-6}$):
    $$\text{Area} = \frac{1}{2} |x_1(y_2 - y_3) + x_2(y_3 - y_1) + x_3(y_1 - y_2)| > \epsilon$$
  - If $\text{Area} \le \epsilon$, the three vertices are collinear, meaning the lines are concurrent and no valid polygon can be formed.
- **Affine Transformation Matrix for Viewport Auto-Fitting**:
  - Rather than hardcoding scale factors, compute the bounding box of the three vertices: $[x_{min}, x_{max}] \times [y_{min}, y_{max}]$.
  - The required scale factor $S = \min\left(\frac{W_{canvas} \cdot (1 - 2\cdot \text{margin})}{x_{max} - x_{min}}, \frac{H_{canvas} \cdot (1 - 2\cdot \text{margin})}{y_{max} - y_{min}}\right)$.
  - The world-to-screen mapping can be expressed as a single $3 \times 3$ affine matrix:
    $$\begin{bmatrix} X_{screen} \\ Y_{screen} \\ 1 \end{bmatrix} = \begin{bmatrix} S & 0 & T_x \\ 0 & -S & T_y \\ 0 & 0 & 1 \end{bmatrix} \begin{bmatrix} x_{world} \\ y_{world} \\ 1 \end{bmatrix}$$
  - JavaFX provides native support for this through `javafx.scene.transform.Affine`, enabling hardware-accelerated matrix transformations directly on the canvas context.

---

## Project 2: JavaFX Program to Draw a Circle on a Canvas on Right-Click and Connect Them with Arrows

### Class Presentation & Viva Discussion
- **Implementation Approach**: The group utilized a JavaFX `Canvas` with event handlers attached to mouse clicks. When a right-click (`MouseButton.SECONDARY`) is detected, a circle with fixed radius $R$ is drawn centered at the cursor coordinates. Users can subsequently click on a source circle and drag/click to a destination circle to draw a connecting directional arrow.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *Immediate Mode vs Retained Mode*: The group implemented this directly on a `Canvas` using `GraphicsContext2D` (immediate mode rendering). The professor pointed out that whenever the window resized or minimized, the canvas did not automatically repaint unless the group manually retained a list of all circle coordinates and re-executed all draw calls.
  - *Arrowhead Geometry*: In their early prototype, arrows were drawn directly from circle center $C_1$ to center $C_2$. This caused the arrowhead to be hidden underneath the destination circle or to awkwardly overlap text inside the node. The professor asked how to mathematically terminate the arrow right at the circumference of the destination circle.
  - *Hit Testing & Node Selection*: The professor queried how the code determines which circle was clicked. Since canvas shapes are not discrete Scene Graph nodes, the group had to manually compute the Euclidean distance $d = \sqrt{(x - x_c)^2 + (y - y_c)^2} \le R$ across an internal collection of circle objects.

### Extra Research & Technical Deep Dive (AI Assisted)
- **Vector Mathematics for Perimeter-Constrained Arrows**:
  - Let $C_1 = (x_1, y_1)$ be the source center with radius $r_1$, and $C_2 = (x_2, y_2)$ be the destination center with radius $r_2$.
  - The displacement vector is $\vec{D} = C_2 - C_1 = (x_2 - x_1, y_2 - y_1)$, with Euclidean norm $\|\vec{D}\| = \sqrt{(x_2 - x_1)^2 + (y_2 - y_1)^2}$.
  - The unit vector pointing from $C_1$ to $C_2$ is $\hat{u} = \frac{\vec{D}}{\|\vec{D}\|}$.
  - The clean start point on $C_1$'s boundary is $P_{start} = C_1 + r_1 \hat{u}$.
  - The clean tip point on $C_2$'s boundary is $P_{end} = C_2 - r_2 \hat{u}$.
  - For the arrowhead wings of length $L$ and spread angle $\theta$ (typically $30^\circ$ or $\pi/6$ rad):
    - Compute the reverse vector $\vec{B} = -\hat{u}$.
    - Rotate $\vec{B}$ by $+\theta$ and $-\theta$ using a 2D rotation matrix:
      $$W_1 = P_{end} + L \begin{pmatrix} \cos\theta & -\sin\theta \\ \sin\theta & \cos\theta \end{pmatrix} \begin{pmatrix} -u_x \\ -u_y \end{pmatrix}$$
      $$W_2 = P_{end} + L \begin{pmatrix} \cos(-\theta) & -\sin(-\theta) \\ \sin(-\theta) & \cos(-\theta) \end{pmatrix} \begin{pmatrix} -u_x \\ -u_y \end{pmatrix}$$
  - This ensures that regardless of the angle or distance between nodes, the arrow head always touches the circle perimeter at the exact angle of approach.
- **Architectural Comparison: Canvas vs Scene Graph (`Pane` + `Circle` + `Line`)**:
  - *Canvas (Immediate Mode)*: Extremely fast for tens of thousands of static particles because there is no node overhead. However, it requires custom hit-testing, dirty-region tracking, and manual repaint loops.
  - *Scene Graph (Retained Mode)*: Wrapping each circle as a `javafx.scene.shape.Circle` and each edge as a custom `Group` containing a `Line` and `Polygon`. This enables automatic CSS styling, built-in event handling (`circle.setOnMouseClicked(...)`), and dynamic property bindings (`line.startXProperty().bind(circle1.centerXProperty())`).
- **Command Pattern for Undo/Redo & Deletion**:
  - Storing mutations in a `Stack<Command>` where each command implements `execute()` and `undo()`. For deletion, repainting the background color is a fragile hack; instead, removing the node from the data model and refreshing the display is the clean, maintainable approach.

---

## Project 3: JavaFX Program to Draw a Binary Tree

### Class Presentation & Viva Discussion
- **Implementation Approach**: The team created an interactive Binary Search Tree (BST) visualizer. Users enter an integer value into a text box, click "Insert", and the program recursively traverses the tree to place the node, updating the visual layout with circles for nodes and lines for branches.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *The Node Overlap Problem*: The group initially divided horizontal space by $W / 2^{\text{depth}}$. While this works for balanced trees of depth 3 or 4, for depth $\ge 5$, horizontal spacing $\Delta x$ drops below node diameter ($2R$), causing sibling nodes to overlap and branch lines to become unreadable.
  - *Skewed Tree Degradation*: If values are inserted in sorted order ($1, 2, 3, 4, 5$), the tree degrades into a linked list, running diagonally off the bottom-right of the screen. The professor pressed on how layout algorithms should handle unbalanced trees.
  - *Animation and State Synchronization*: The group was asked whether tree rotations (such as in AVL or Red-Black trees) could be animated smoothly, or if the entire canvas must be wiped and redrawn from scratch.

### Extra Research & Technical Deep Dive (AI Assisted)
- **Aesthetic Tree Layout Algorithms (The Reingold-Tilford / Walker Algorithm)**:
  - Naive geometric division ($x = x_{parent} \pm \frac{\text{width}}{2^{\text{depth}}}$) allocates space based on theoretical maximum depth rather than actual subtree width, leading to exponential width explosion.
  - The **Reingold-Tilford algorithm** (extended by Walker and Buchheim et al.) provides $O(N)$ aesthetic node placement adhering to strict visual aesthetics:
    1. Nodes at the same depth lie on the same horizontal line.
    2. A parent node is centered directly above its children.
    3. Left and right subtrees of a node are placed as close together as possible without their contours colliding.
    4. Identical subtrees are displayed identically regardless of their global position.
  - *Two-Pass Tree Traversal*:
    - **Post-order traversal (bottom-up)**: Computes relative displacements, subtree left/right contours, and assigns tentative horizontal positions.
    - **Pre-order traversal (top-down)**: Accumulates absolute horizontal offsets to calculate final $X$ positions for all nodes.
- **JavaFX Dynamic Scene Graph Binding for Tree Nodes**:
  - Representing each tree node as a `StackPane` containing a `Circle` (with radial gradient fill) and a `Text` label.
  - Connecting edges using `javafx.scene.shape.Line`. By binding edge endpoints to node layout properties:
    ```java
    edgeLine.startXProperty().bind(parentNode.layoutXProperty());
    edgeLine.startYProperty().bind(parentNode.layoutYProperty());
    edgeLine.endXProperty().bind(childNode.layoutXProperty());
    edgeLine.endYProperty().bind(childNode.layoutYProperty());
    ```
  - When a tree rebalance occurs, applying a `TranslateTransition` to the `StackPane` automatically causes all connected lines to animate smoothly in real time without any frame-by-frame manual coordinate math.

---

## Key Takeaways & Viva Insights
- **Separate Data Model from View**: Writing graphics code directly mixed with mathematical parsing or tree balancing leads to unmaintainable software. A clean architecture isolates the computational model from the rendering layer.
- **Defensive Graphics Programming**: Graphics code must account for edge cases: zero determinants, collinear points, zero-length vectors in arrow calculations, and window resizing.
- **Immediate vs Retained Mode**: Choosing between `Canvas` and Scene Graph nodes is not just stylistic—it dictates how event handling, memory consumption, repainting, and animations operate under the hood.
