# Personal Reflections CSC360 (08/10/2026)

## Session Overview
The project presentations and viva evaluations continued into our second session today. While Tuesday's session focused heavily on fundamental geometric rendering and tree layouts on the 2D canvas, today's presentations explored non-graphical terminal-based rendering, visual graph mapping across distinct UI lists, and advanced JavaFX application initialization with custom FXML splash screens and asynchronous multithreading.

The three projects reviewed and evaluated by the professor were:
1. A pure Java program to render and print an ASCII tree structure on the console.
2. A Java program for drawing directional arrows between two separate lists containing common elements.
3. A JavaFX application featuring a custom, asynchronous splash screen constructed using FXML and worker threads.

---

## Project 1: Java Program to Print an ASCII Tree

### Class Presentation & Viva Discussion
- **Implementation Approach**: The presenting group demonstrated a command-line program that prints tree structures directly to standard output (`System.out`) without utilizing any graphical libraries (AWT, Swing, or JavaFX). The program used recursive pre-order traversal with formatted ASCII strings (`|--`, `\--`, `|  `) to depict parent-child relationships and folder-style directory hierarchies.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *Standard ASCII vs Extended Unicode*: The professor probed the group on character encodings. Using raw ASCII characters (`|`, `-`, `\`) ensures compatibility with legacy 7-bit ASCII teletype terminals, but extended Unicode box-drawing characters (such as `┌`, `├`, `└`, `─`, `│` in UTF-8) provide cleaner, contiguous lines without gaps. How does the program ensure portable output across Windows (`cp1252` or `cmd.exe`) and UNIX/Linux shells?
  - *Vertical vs Horizontal Orientations*: The group opted for a horizontal directory layout (like UNIX `tree`). The professor asked what the algorithmic complexity and buffering challenges would be if they were required to print an inverted-V binary tree vertically on the terminal.
  - *Variable Node Label Lengths*: What happens when node names have varying string lengths (e.g., single digit `"5"` versus a long filename `"very_long_file_name_backup.tar.gz"`)? If branch widths are hardcoded, longer labels shift subsequent branches horizontally, breaking columnar alignment.

### Extra Research & Technical Deep Dive (AI Assisted)
- **The Linux `tree` Prefix Algorithm**:
  - Printing an aesthetic horizontal hierarchy recursively without trailing dangling vertical bars requires maintaining a stack or boolean list indicating whether the current node is the last child of its parent:
    ```java
    public void printTree(Node node, String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.getValue());
        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size() - 1; i++) {
            printTree(children.get(i), prefix + (isTail ? "    " : "│   "), false);
        }
        if (!children.isEmpty()) {
            printTree(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true);
        }
    }
    ```
  - By passing `prefix + (isTail ? "    " : "│   ")`, ancestor levels that have already completed their children omit the vertical line (`│`), preventing "dangling" branches.
- **2D Character Matrix Buffering for Vertical Binary Trees**:
  - Unlike horizontal printing (which streams line-by-line sequentially), vertical binary tree printing cannot be done strictly sequentially because parent nodes appear above both left and right children on lines printed earlier.
  - *Solution*: Pre-allocate a 2D text buffer `char[][] canvas = new char[H][W]`.
    1. Calculate maximum depth $D$ and width $W = (2^{D+1} - 1) \times \text{cellWidth}$.
    2. Recursively populate node characters and diagonal slashes (`/` and `\`) into `canvas[row][col]`.
    3. Flush each row of the matrix to `System.out`.
  - While this incurs an $O(2^D)$ memory overhead, it guarantees pixel-perfect (character-perfect) vertical alignment.
- **Systems Engineering Advantages of ASCII Rendering**:
  - Extremely low overhead: zero GPU allocation, zero Scene Graph memory overhead, and near-zero CPU cycles.
  - Universal observability: Operates seamlessly in headless environments, Docker containers, remote SSH sessions, and standard log files (`log4j` / `SLF4J`), proving that graphics concepts can be applied effectively in pure text mediums.

---

## Project 2: Java Program for Drawing Arrows Between Two Lists with Common Elements

### Class Presentation & Viva Discussion
- **Implementation Approach**: The team built a visualization featuring two vertical columns: List A on the left and List B on the right. Both lists contain items, some of which are shared. The program detects common items and renders connecting arrows from the item in List A across the central gap to the corresponding item in List B.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *Algorithmic Complexity of Matching*: How are the matching elements discovered? A naive nested loop runs in $O(N \times M)$ time. With large lists, this stalls the rendering pipeline. The professor pressed the group to use hash-based index lookups ($O(N + M)$).
  - *Coordinate Transformation across Separate Containers*: How are arrow endpoint coordinates computed? Since List A and List B reside in distinct layout containers (e.g. separate `VBox` or `ListView` containers), their item coordinates are local to their respective containers. Drawing an arrow across containers requires mapping local coordinates to a shared ancestor coordinate space (`localToScene` or `sceneToLocal`).
  - *Visual Clutter and Edge Crossings*: When list items are unsorted, straight connecting lines cross over each other repeatedly, creating a chaotic visual "spaghetti" effect. The professor asked how curved lines or layout heuristics could alleviate visual overlap.

### Extra Research & Technical Deep Dive (AI Assisted)
- **Coordinate Transformation in JavaFX**:
  - When an item $N_A$ is inside a left container and $N_B$ is inside a right container, their `layoutX` and `layoutY` are relative to their immediate parents.
  - To draw a line in an overarching overlay `Pane`, compute absolute scene coordinates:
    ```java
    Point2D ptA = nodeA.localToScene(nodeA.getBoundsInLocal().getWidth(), nodeA.getBoundsInLocal().getHeight() / 2.0);
    Point2D ptB = nodeB.localToScene(0.0, nodeB.getBoundsInLocal().getHeight() / 2.0);
    
    Point2D startOverlay = overlayPane.sceneToLocal(ptA);
    Point2D endOverlay = overlayPane.sceneToLocal(ptB);
    ```
  - This guarantees that even if padding, borders, or margins change on either container, arrow anchors stay perfectly pinned to the right edge of Item A and the left edge of Item B.
- **Cubic Bézier Curves (`CubicCurve`) for Edge Routing**:
  - Straight lines crossing each other at shallow angles make it impossible for the human eye to track which element connects to which.
  - Replacing straight lines with cubic Bézier curves creates smooth, legible "S-curves":
    - Start point: $P_0 = (x_0, y_0)$
    - First control point: $P_1 = (x_0 + \Delta x \cdot 0.5, y_0)$ (pulling horizontally right)
    - Second control point: $P_2 = (x_1 - \Delta x \cdot 0.5, y_1)$ (pulling horizontally left)
    - End point: $P_3 = (x_1, y_1)$
  - This horizontal tangency at both list boundaries gives the connection an organic flow and visually separates overlapping paths.
- **Bipartite Crossing Minimization Heuristics**:
  - In graph drawing theory, this setup is formally a **2-layer bipartite graph drawing problem**.
  - Minimizing edge crossings is NP-complete, but heuristic algorithms significantly improve readability:
    - **Barycenter Heuristic**: For each vertex $v$ in List B, assign its position based on the average index (barycenter) of its neighbors in List A, then sort List B accordingly.
    - Applying this heuristic dramatically reduces line intersections and declutters the interface.

---

## Project 3: JavaFX Program with a Custom Splash Screen Using FXML

### Class Presentation & Viva Discussion
- **Implementation Approach**: The team created a modern startup experience for a JavaFX application. Instead of launching directly into the main window, the application shows a borderless splash screen designed in FXML with custom CSS styling, containing an application logo, a progress bar, and status labels that update as simulated resources are loaded. Once initialization completes, the splash screen fades out and the primary window opens.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *The UI Freezing Anti-Pattern*: Why can't resource loading (such as reading configuration files, establishing database connections, or pre-caching images) be performed directly in `start()` or `initialize()`? The professor highlighted that executing blocking I/O on the **JavaFX Application Thread** freezes the OS event pump, causing the progress bar to stutter, the window to become unresponsive ("Not Responding"), and the operating system to prompt to kill the process.
  - *Thread-Safe UI Binding*: How does the background worker thread communicate progress back to the JavaFX `ProgressBar`? The team demonstrated the use of `javafx.concurrent.Task` and property binding.
  - *FXML vs Imperative Java UI*: Why use FXML over writing raw Java layout code? The professor discussed the architectural benefits of decoupling declarative UI structure (FXML) from styling (CSS) and behavioral logic (Controller).

### Extra Research & Technical Deep Dive (AI Assisted)
- **JavaFX Concurrency Model (`Task<V>` and `Service<V>`)**:
  - JavaFX provides the `javafx.concurrent` package specifically designed to bridge background worker threads with the single-threaded UI model.
  - Under the hood, `Task<V>` implements `RunnableFuture<V>` and exposes observable properties:
    - `progressProperty()`: Bound directly to `progressBar.progressProperty().bind(task.progressProperty())`.
    - `messageProperty()`: Bound directly to `statusLabel.textProperty().bind(task.messageProperty())`.
  - When the worker calls `updateProgress(workDone, maxWork)` or `updateMessage("Loading modules...")`, `Task` internally marshals these updates onto the JavaFX Application Thread via `Platform.runLater()`, completely preventing race conditions without requiring manual lock synchronization.
- **FXML Lifecycle and Dependency Injection**:
  - Understanding how `FXMLLoader` works internally:
    1. Reads XML tags and maps them to JavaFX classes (e.g., `<ProgressBar>` $\to$ `javafx.scene.control.ProgressBar`).
    2. Instantiates the controller class specified by `fx:controller`.
    3. Injects references into fields annotated with `@FXML` matching `fx:id` via reflection.
    4. Calls `initialize()` on the controller after all fields are injected.
  - This strict separation of concerns follows the **Model-View-Controller (MVC)** architectural pattern.
- **Window Aesthetics & Seamless Stage Transitions**:
  - To create a sleek, borderless splash screen:
    ```java
    splashStage.initStyle(StageStyle.UNDECORATED); // Removes title bar and OS borders
    // or StageStyle.TRANSPARENT with a transparent Scene fill for rounded corners
    ```
  - Transitioning between stages without visual glitching or window flicker:
    ```java
    task.setOnSucceeded(event -> {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), splashPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            splashStage.close();
            primaryStage.show();
        });
        fadeOut.play();
    });
    ```

---

## Key Takeaways & Viva Insights
- **The Primacy of UI Concurrency**: Modern graphics and UI development is fundamentally concurrent. Long-running computational or I/O operations must always be isolated on worker threads, with updates safely dispatched to the UI thread via observable properties or event dispatch queues.
- **Form Follows Function in Visual Design**: Whether using ASCII characters on a terminal grid or cubic Bézier curves across JavaFX list containers, visual design must reduce cognitive load and clearly communicate structural relationships.
- **Declarative Architecture**: Separating layout declaration (FXML), presentation style (CSS), and control flow (Java Controller) yields significantly cleaner, more testable, and more maintainable code than monolithic imperative GUI scripting.
