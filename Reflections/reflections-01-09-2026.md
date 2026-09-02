# Personal Reflections CSC360 (01/09/2026)
## Session Overview
The groups were assigned with an project to complete. This class's objective was to understand, and analayse the problem statement by breaking it down into simpler complex which overall contributed to learning new methology/ concepts for graphics.
## Drawing traingles (by equations)
- We learned about basics of linear equations i.e, A(x)=(y), vectors; row and column,
- to draw a triangle, we need three equations such that, each of them should have on unique solution with other two.
- we learned what mathematical constraints we are required to implement on the user input
- user may enter the equation in different form, i.e, such as 3x+2y=5 or 3x+2y-5=0, one must check this, as both are valid input.

## Circles, deletion, and arrows
- We dwelled on another project, where the programs draws an circle, with the users click, and connects thoes circle with arrow.

- To find the position at which the circle is supposed to be drawn, we use listener component, which basically denotes the point of click as center of the circle

- deletion: to integrate this feature, there are a lot of approachs, but the most simple one is to repaint the element to be deleted, by creating an identitical element, but keeping the colour as that of the backgroud.

- for connecting arrows, the best way is let user drag the arrow from one circle to other, which is another component, (drag.start, drag.end). We also have to ensure that the area of the circle cant be the starting point, as it will make the visual messy.

- undo: to undo a drawn graphic, we can use stack datatype, as it will store the last graphic used making it efficient to recall and operate

## Drawing Binary tree and Printing ascii tree
- A Binary Tree is a type of tree data structure where each node can have a maximum of two child nodes. Ideas from the circle project can be implemented here.
- To print an ASCII, it will challenging as we dont have to deal with an actual graphic, rather we need the tree on the console it self. One must learned the basic patterns and figures, to draw on console, using Nested loops, matrices etc.
