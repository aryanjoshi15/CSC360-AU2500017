# Personal Reflections CSC360 (17/09/2026)
## Session Overview
This was another practical session working on the group project, where we moved from theory into building a small working prototype before adding the full registration form logic.

## Early prototype
- Before building the final form, we made a simple test version with a JavaFX `TextField` and `Button` on one side, and a Swing `JTextField` and `JButton` on the other side, to confirm two-way communication worked correctly for our project.
- Clicking the JavaFX button sent text into the Swing text field, and clicking the Swing button sent text back into a JavaFX `Label`. This was a much smaller version of what our final project needed, just to test the pattern before committing to it.
- We also briefly tested a basic `JSlider` on its own, moving it and printing its value to the console, before deciding how to use it properly in the actual form for SGPA input.

## Planning the actual form
- Once the basic prototypes worked, we planned out our project's registration form: JavaFX would handle the outer window, title, name input and the submit button, while specific input components (dropdown, radio buttons, slider, checkbox) would be built in Swing and embedded individually using separate `SwingNode` instances.
- Keeping each Swing component in its own `SwingNode` (rather than one big Swing panel) meant we could build each one independently on the EDT using `SwingUtilities.invokeLater()`, then place them into the JavaFX `VBox` layout as needed.

## Threading in practice
- Since the early prototype already relied on `Platform.runLater()` and `SwingUtilities.invokeLater()`, this session helped confirm that the same pattern would scale up to more components in our project without needing a different approach — just more places where the two threads needed to hand off data to each other.
