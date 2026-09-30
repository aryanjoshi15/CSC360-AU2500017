# Personal Reflections CSC360 (03/09/2026)

## Session Overview
This session was a continuation of the previous class and focused on breaking down a problem statement into smaller, more manageable parts. This approach helped connect several concepts related to graphics that we had already covered, while introducing a few new methods for handling text-based and visual output. Compared to earlier sessions, this one was more practical, as it tied directly into planning for the midsem project.

## Printing an ASCII tree
- Drawing a tree visually isn't always practical, especially in high-computing or terminal-based environments where only text output is available. This is where printing a tree using ASCII characters becomes necessary.
- Characters like `/`, `\`, and `|` are used to represent the edges connecting parent and child nodes, allowing the tree's structure to be understood just from text.
- Printing a tree usually relies on recursion — each node prints itself, then calls the same function on its children with increased indentation, which is what gives the tree its branching appearance.
- For larger trees, printing them horizontally is generally more readable than the traditional vertical format, since a vertical layout can quickly become too wide or too tall to display cleanly on a limited screen or console width.
- This connects back to the idea of keeping output efficient and lightweight, similar to the ASCII vs UTF-8 discussion from an earlier session — a plain text tree uses far less memory and processing than rendering an actual graphical tree structure.

## Creating a splash screen
- A splash screen is the introductory screen shown when an application or game is first launched, before the main interface loads.
- It typically includes the brand logo, a background poster or image, a welcome message, and sometimes simple live graphics or animations to visually indicate that the app is in the process of loading.
- A progress bar is a common feature in splash screens, giving the user a clear indication of how much of the loading process has been completed.
- A well-designed splash screen should also display basic information such as the app's version number and build details, giving the user context about what exactly is loading.
- In JavaFX specifically, a splash screen is usually shown on its own `Stage` while the actual resources (files, database connections, configuration) load in a background thread. Once loading finishes, the background thread has to hand control back to the JavaFX Application Thread using `Platform.runLater()` to switch to the main window — which ties directly back into the threading concepts from earlier sessions.
- If the loading work is done directly on the JavaFX Application Thread instead of a background thread, the splash screen animation or progress bar can freeze, since that thread would be busy loading resources instead of updating the UI.

## Unit testing vs acceptance testing
- Following up from the last class, unit testing happens at the code level and is usually performed by the developer. It is detailed and focuses on verifying that individual, smaller units of the program work correctly.
- Acceptance testing, on the other hand, evaluates the program as a whole. It focuses more on usability and whether the software meets the end user's expectations, and is generally carried out by the user rather than the developer.
- Between these two extremes, integration testing was also briefly relevant here  it checks whether different units (which passed their individual unit tests) work correctly when combined together, which is relevant to a project combining multiple components like a splash screen, a tree structure, and the main UI.

## Takeaway
- This session reinforced the idea that breaking a large problem into smaller components whether it's printing a tree structure or designing a splash screen  makes it easier to plan and implement. It also showed how earlier concepts like threading and memory-efficient output aren't isolated topics, but keep resurfacing in different parts of the course, which was useful context going into the midsem project.