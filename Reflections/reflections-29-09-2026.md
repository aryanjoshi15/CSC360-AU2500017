# Personal Reflections CSC360 (29/09/2026)
## Session Overview
This practical session was spent building out the rest of the project's registration form on top of the prototype from the previous class, adding the remaining Swing components and the validation logic.

## New Swing components
- `JComboBox` was added for the course dropdown, letting the user pick between Java, Python and C++.
- `JRadioButton` was used for semester selection, with all three buttons added to a single `ButtonGroup` so only one could be selected at a time — this was more involved than the basic slider/button prototypes from before since it needed grouping logic.
- The `JSlider` prototype from the previous session was expanded into the actual SGPA input for our project, scaled from 0–40 internally and divided by 10 when read, to allow one decimal of precision since `JSlider` only supports integer values. A `Hashtable` was used to relabel the slider ticks as 0.0, 1.0, 2.0, 3.0, 4.0.
- The slider's `ChangeListener` was connected to a JavaFX `Label` so the SGPA and letter grade updated live as the slider moved, again wrapped in `Platform.runLater()` since the listener itself runs on the Swing side.

## Validation logic
- `TextFormatter` was added to the JavaFX name field to block numbers and special characters as the user types.
- On submit, Swing values had to be read inside `SwingUtilities.invokeLater()`, and the result passed back using `Platform.runLater()`, following the same thread-handoff pattern from the earlier prototype but now across several components at once instead of just one text field.
- Basic checks were added for an empty name, an invalid name, no course selected, and the agreement checkbox not ticked, with error messages shown in red and the success summary shown in green.

## Takeaway
- Going from the small button/slider prototypes to the full form built for our project made it clear why keeping each Swing component in its own thread-safe block mattered — with only one component it was easy to get away with sloppy thread handling, but with five or six components all interacting with JavaFX labels, following `invokeLater()` and `runLater()` consistently was what kept the form from behaving unpredictably.