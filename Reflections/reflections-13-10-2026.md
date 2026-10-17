# Personal Reflections CSC360 (13/10/2026)

## Session Overview
Today was the most anticipated session of the semester for our team—it was the final round of project presentations and group viva evaluations, and **our group presented our midterm project**! The session was intense and highly rewarding. The professor probed deep into software architecture, concurrency models, thread synchronization, memory management, and practical software engineering trade-offs.

Three projects were presented and evaluated during today's session:
1. A JavaFX application managing a tree of domain objects with support for in-place editing.
2. A JavaFX dashboard featuring a composite progress bar tracking multiple concurrent background jobs with real-time cancellation.
3. **Our Group Project**: A hybrid JavaFX and Swing Student Registration System integrating legacy Swing components inside a modern JavaFX container via `SwingNode` with bidirectional thread-safe synchronization.

---

## Project 1: JavaFX Program with a Tree of Objects with Support for Editing Each Object

### Class Presentation & Viva Discussion
- **Implementation Approach**: The presenting team developed an interactive hierarchical tree view (`TreeView<T>`) representing complex domain objects (such as an organizational structure or inventory catalog). Each node in the tree corresponds to a model object whose attributes can be edited directly within the tree view.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *The Cell Factory Pattern & Memory Leaks*: The professor questioned how the tree cells were generated. A common beginner mistake is to instantiate new UI widgets (`TextField`, `HBox`, `Button`) for every single node in the data model. The professor explained that in large hierarchies (thousands of nodes), this causes severe memory bloat and sluggish scrolling. JavaFX's `TreeView` uses virtualized rendering, recycling a small pool of `TreeCell` instances.
  - *Commit vs Cancel Lifecycle*: What happens if a user begins editing a node, inputs an invalid value (e.g. negative salary or empty name), and clicks away or presses `Escape`? The group explained the event flow of `startEdit()`, `commitEdit()`, and `cancelEdit()`, demonstrating input validation before updating the underlying model.
  - *Two-Way Data Binding*: How are changes in the UI synchronized back to the underlying domain model? The group demonstrated JavaFX property wrappers (`StringProperty`, `DoubleProperty`) and change listeners.

### Extra Research & Technical Deep Dive (AI Assisted)
- **Cell Virtualization Mechanics in JavaFX `TreeView`**:
  - In a tree with 50,000 items, JavaFX does not create 50,000 UI cells. It instantiates only enough `TreeCell` nodes to fill the visible viewport (plus a small buffer of 2–3 rows).
  - When the user scrolls, cells scrolling off the top are detached and reused at the bottom via `updateItem(T item, boolean empty)`.
  - Writing clean, custom editable cells requires overriding `startEdit()`, `commitEdit(T newValue)`, and `cancelEdit()`:
    ```java
    public class EditablePersonCell extends TreeCell<Person> {
        private TextField textField;
        
        @Override
        public void startEdit() {
            super.startEdit();
            if (textField == null) createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
        
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem().getName());
            setGraphic(null);
        }
        
        @Override
        public void updateItem(Person item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                if (textField != null) textField.setText(item.getName());
                setText(null);
                setGraphic(textField);
            } else {
                setText(item.getName());
                setGraphic(null);
            }
        }
    }
    ```
- **Observable Properties and Atomic Rollback**:
  - Utilizing `javafx.beans.property.Property<T>` allows automatic propagation of model changes to other UI views (like a side summary panel).
  - To support transactional editing, the cell should maintain the original model value upon `startEdit()`. If custom validation fails on `commitEdit()`, the edit is rejected and the cell reverts to the previous valid state without corrupting the domain model.

---

## Project 2: JavaFX Program with a Composite ProgressBar Tracking Multiple Jobs with Support for Cancellation

### Class Presentation & Viva Discussion
- **Implementation Approach**: The team presented a multi-threaded batch-processing dashboard. The interface allows users to spawn multiple concurrent background tasks (e.g., file downloads or cryptographic hashing). Each individual task displays its own progress bar and cancellation button, while a master "composite" progress bar tracks the weighted aggregate progress of all active jobs.
- **Viva Scrutiny & Questions Asked by Professor**:
  - *Cooperative Thread Cancellation*: The professor probed the team on how cancellation is executed under the hood. Does the code call `Thread.stop()`? The team correctly answered that `Thread.stop()` is deprecated and inherently dangerous because it releases locks arbitrarily, leaving shared objects in corrupted states. Instead, cancellation must be cooperative using `task.cancel(true)` and periodic `Thread.currentThread().isInterrupted()` checks.
  - *UI Event Queue Flooding*: If multiple high-speed worker threads call `updateProgress()` thousands of times per second, the JavaFX Application Thread becomes overwhelmed processing UI events, causing the entire interface to freeze. The professor asked how progress updates can be throttled or rate-limited.
  - *Dynamic Composite Progress Calculation*: How does the aggregate progress bar handle jobs that are cancelled midway or fail unexpectedly? If 1 of 5 jobs is cancelled, does the composite bar still expect 100% from that job, or does it re-normalize over the remaining jobs?

### Extra Research & Technical Deep Dive (AI Assisted)
- **Cooperative Cancellation Mechanics in Java**:
  - Calling `task.cancel(true)` sets the task's state to `CANCELLED` and interrupts the underlying worker thread (`Thread.interrupt()`).
  - The worker loop must explicitly check for cancellation at safe execution boundaries:
    ```java
    @Override
    protected Double call() throws Exception {
        for (int i = 0; i < totalSteps; i++) {
            if (isCancelled()) {
                break; // Clean up resources, close streams, and exit
            }
            performWorkStep(i);
            updateProgress(i + 1, totalSteps);
        }
        return 1.0;
    }
    ```
  - If a blocking I/O or sleep call is encountered, `InterruptedException` must be caught, local state cleaned up, and the thread allowed to terminate gracefully.
- **Weighted Composite Progress Mathematics**:
  - For $N$ concurrent tasks with individual progress $p_i \in [0.0, 1.0]$ and assigned weights $w_i$:
    $$\bar{P}_{\text{composite}} = \frac{\sum_{i \in \text{Active}} w_i \cdot p_i}{\sum_{i \in \text{Active}} w_i}$$
  - When a task is cancelled, its weight can either be subtracted from the denominator (re-normalizing the remaining active tasks to 100%), or its progress frozen at its last recorded value. Dynamically updating this composite metric requires listening to `progressProperty()` and `stateProperty()` across all task instances.
- **High-Frequency Update Throttling via `AnimationTimer`**:
  - Rather than letting worker threads directly enqueue hundreds of `Platform.runLater()` calls every millisecond, worker threads update volatile or `AtomicDouble` counters.
  - A single JavaFX `AnimationTimer` running on the UI thread samples these atomic counters at display refresh rate (60 Hz / 16.6ms intervals), guaranteeing that UI repainting never consumes more CPU than the monitor can physically display.

---

## Project 3: Our Group Presentation - Hybrid JavaFX and Swing Student Registration System

### Our Presentation & Viva Experience
Today, our group stood before the class and the professor to demonstrate our hybrid UI project. Building upon our initial prototypes and earlier architectural planning (documented in our September reflections), our application is a comprehensive Student Registration Form that seamlessly bridges legacy Swing components into a modern JavaFX application window.

- **Demonstration Walkthrough**:
  - **Outer Frame & Layout**: Managed entirely in JavaFX using a clean `VBox` layout, custom fonts, styled header labels, and a JavaFX `TextField` for the student's name.
  - **Input Sanitization**: Demonstrated our JavaFX `TextFormatter` utilizing regular expressions (`^[a-zA-Z\\s]*$`) to actively reject numeric digits and special symbols at the keystroke level.
  - **Embedded Swing Controls via `SwingNode`**:
    - `JComboBox`: For course selection (Java, Python, C++), initialized and manipulated on the Event Dispatch Thread (EDT).
    - `ButtonGroup` with `JRadioButton`: For mutually exclusive semester selection.
    - `JSlider`: For SGPA selection, configured internally from 0 to 40 (divided by 10 to give one decimal precision: 0.0 to 4.0). We customized tick marks with a `Hashtable<Integer, JLabel>` to label major intervals (`0.0`, `1.0`, `2.0`, `3.0`, `4.0`).
    - `JCheckBox`: For the terms and conditions agreement.
  - **Live Bidirectional Synchronization**:
    - As the user slides the Swing `JSlider`, a `ChangeListener` intercepts the value change on the Swing side, determines the letter grade, and dispatches a thread-safe update to the JavaFX SGPA and Grade `Label` in real time.
  - **Form Validation & Color-Coded Feedback**:
    - On clicking the JavaFX "Register" button, the system coordinates thread-safe queries to all Swing components, validates that name is non-empty, course is selected, and agreement is checked, displaying a green registration summary or explicit red error messages.

### Professor's In-Depth Viva Scrutiny & How We Answered
The professor conducted an extensive viva, focusing particularly on concurrency boundaries, event queuing, and real-world architectural justifications:

1. **The Dual-Thread Model & Thread Confinement**:
   - *Professor's Question*: "Why can't you simply call `slider.getValue()` directly inside your JavaFX button's action handler?"
   - *Our Answer*: JavaFX and Swing each maintain their own independent, single-threaded execution environments. JavaFX runs on the **JavaFX Application Thread**, while Swing runs on the **Event Dispatch Thread (EDT)**. Swing components are fundamentally not thread-safe; accessing or mutating Swing component state directly from the JavaFX thread introduces race conditions, dirty reads, and intermittent paint glitches. Therefore, queries to Swing must be marshaled to the EDT via `SwingUtilities.invokeLater()`, and responses must be passed back to JavaFX via `Platform.runLater()`.

2. **Deadlock Vulnerability & Asynchronous vs Synchronous Dispatching**:
   - *Professor's Question*: "What would happen if you used `SwingUtilities.invokeAndWait()` instead of `invokeLater()`?"
   - *Our Answer*: Using `invokeAndWait()` from the JavaFX Application Thread forces the JavaFX thread to block until the EDT finishes processing. If the EDT at that exact moment happens to be waiting on a JavaFX resource or waiting for `Platform.runLater()` to complete, a classic **circular lock inversion deadlock** occurs—both UI threads freeze permanently, locking the entire application. Asynchronous handoffs (`invokeLater()` and `runLater()`) completely prevent this deadlock risk.

3. **Granular `SwingNode`s vs Single Monolithic `JPanel`**:
   - *Professor's Question*: "Why did you embed each Swing component inside its own individual `SwingNode`, rather than creating a single large Swing `JPanel` containing everything and wrapping that in one `SwingNode`?"
   - *Our Answer*: Granular embedding allows the overarching layout, styling, responsive spacing, and tab-focus traversal to be governed natively by the JavaFX scene graph. Wrapping individual components gives us fine-grained control, modular error handling, and allows JavaFX controls (like our `TextField` with `TextFormatter` and submit `Button`) to be interspersed naturally between Swing elements.

4. **Real-World Engineering Justification for Hybrid Toolkits**:
   - *Professor's Question*: "Why bother combining them at all? Why not just write the entire UI in pure JavaFX?"
   - *Our Answer*: In large-scale enterprise environments (banking, healthcare, CAD software), organizations have legacy Swing codebases comprising millions of lines of battle-tested, certified code—such as custom medical imaging viewports, complex financial data grids, or scientific graphing widgets. Rewriting these systems completely in JavaFX is prohibitively expensive and introduces severe regression risks. `SwingNode` allows enterprises to modernize their outer UI shells and adoption pipelines while seamlessly preserving critical legacy assets.

### Extra Research & Technical Deep Dive (AI Assisted)
- **Internal Mechanics of `SwingNode`**:
  - How does JavaFX actually render a Swing component? Under the hood, `SwingNode` does not simply pin an OS native window handle (`HWND` / `X11 Window`).
  - Instead, `SwingNode` utilizes a lightweight off-screen Swing container called **`JLightweightFrame`**:
    1. Swing draws its components into an off-screen pixel buffer (`BufferedImage`) managed on the EDT.
    2. When a repaint occurs in Swing, `SwingNode` intercepts the dirty rectangular region.
    3. The pixels are uploaded into a JavaFX texture (`javafx.scene.image.PixelBuffer` or Prism graphics texture).
    4. JavaFX's rendering engine (Prism) composites this texture into the Scene Graph on the Quantum Renderer thread.
- **Event Translation and Dispatch Pipeline**:
  - User inputs (mouse clicks, drags, key presses) are initially captured by JavaFX as `javafx.scene.input.MouseEvent`.
  - `SwingNode` translates JavaFX coordinates into local Swing coordinates and creates synthesized `java.awt.event.MouseEvent` instances.
  - These synthesized AWT events are posted directly into the AWT `EventQueue` via `EventQueue.postEvent()`, ensuring that Swing listeners (`ActionListener`, `ChangeListener`, `MouseListener`) receive standard AWT events without knowing they are hosted inside JavaFX.
- **Performance Trade-Offs & Memory Overhead**:
  - While `SwingNode` provides seamless integration, it carries an inherent performance penalty compared to pure JavaFX:
    - *Memory Overhead*: Maintaining separate off-screen backing buffers and duplicating event queues increases memory footprint by approximately 15–25% per complex component.
    - *Frame Latency*: Inter-thread communication between the EDT and JavaFX Application Thread introduces a 1–2 frame rendering delay (~16–32ms).
    - *Conclusion*: For form inputs and interactive dashboards, this overhead is negligible, but for 60 FPS real-time games or high-throughput video playback, native JavaFX nodes or Canvas rendering is preferred.

---

## Key Takeaways & Project Viva Conclusion
- **Mastery of Multi-Threaded GUI Architecture**: Successfully presenting our project and answering the professor's viva questions reinforced how critical thread safety, non-blocking dispatching, and thread confinement are in graphical software.
- **Validation as First-Class Concern**: Defensive input formatting (`TextFormatter`) combined with comprehensive cross-toolkit submission checks ensures robust software that resists malformed user inputs.
- **Bridging the Old and the New**: Software engineering is often about pragmatic integration rather than dogmatic rewrites. Learning how to bridge Swing and JavaFX via `SwingNode` provided invaluable insight into enterprise software modernization patterns.
