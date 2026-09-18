# Personal Reflections CSC360 (15/09/2026)
## Session Overview
This was a practical session where we worked on our group project. JavaFX and Swing were the toolkits assigned to us for this project, and this session was mainly focused on introducing both of them together for the first time and explaining why our project needed to combine them, rather than use just one.

## JavaFX basics
- JavaFX is the newer UI toolkit for Java. Components don't use the "J" prefix, e.g. `Button`, `Label`, `TextField`, unlike Swing's `JButton`, `JLabel`, `JTextField`.
- JavaFX supports CSS styling directly on components, and layouts can also be described separately using FXML, though our project does not use FXML.
- A JavaFX app is structured as `Stage` → `Scene` → scene graph. The `Stage` is the actual window, the `Scene` is the container attached to it, and the scene graph is the tree of nodes (layouts and controls) drawn inside that scene.
- JavaFX has its own single UI thread called the **JavaFX Application Thread**, and any change to a JavaFX component must happen on that thread.

## Swing basics
- Swing is the older toolkit, part of the JDK since the beginning, and still widely used in legacy enterprise software.
- Swing components draw themselves rather than relying on native OS widgets, which is why Swing apps look the same across operating systems.
- Just like JavaFX, Swing also expects all UI work to happen on a single thread, called the **Event Dispatch Thread (EDT)**, which connects back to the thread safety discussion from earlier sessions.

## Why our project needed both
- Since Swing and JavaFX were both assigned as the required toolkits for this project, we had to understand how to make legacy Swing components work inside a JavaFX window rather than choosing one over the other.
- JavaFX provides a bridge class called `SwingNode`, which lets a real Swing component sit inside a JavaFX scene graph, avoiding the need to rebuild Swing components from scratch in JavaFX.
- Since JavaFX and Swing each have their own separate single-thread system, combining them means updates must be manually routed to the correct thread: `SwingUtilities.invokeLater()` for Swing, and `Platform.runLater()` for JavaFX.
- As a first practical step for the project, we built a very basic prototype: a JavaFX window with one `Button`, and a single Swing `JButton` embedded next to it using `SwingNode`, where clicking the Swing button just printed a message to the console. This was only to confirm both toolkits could exist in the same window before attempting anything more complex for the actual project.