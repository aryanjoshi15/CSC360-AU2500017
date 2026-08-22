# Personal Reflections CSC360 (20/08/2026) 
## Session Overview
The session started with an brief discussion on writing reflection, and some basic conventions and features to use in markdown file while composing the reflection. Later, we continued with the previous session, i.e, logic behind creating a basic sqaure. While this session focused on implementing the logic through code, understanding new terms and functions, and why are they used.  

## Code breakdown
- jframe, is an container class, which is basically the window on the screen, used to display graphics.
- to display the actual frame on screen, we must excute setvisble(true)
- later on, we can use .add; to import the graphic, .size; to input the desired size, etc.
- we use @override, as an safety parameter, to ensure the code gives compile error, if any error is made by user.
- later, we use super,paintcomponent(), basically calling an method from parent class, which in these case would paint a blank screen (as jpanel is blank)
-  then comes painting the graphic(sqaure), one can also use rgb value, but the program already have some basic colours, which one can use. eg:.red, .black etc
-  later to draw the sqaure one can directly use draw.rect() ,adding the position and size, but i draw indivival lines following the previous sessions psuedocode.
- finally by using jframe.add, one will have to add the sqaure into the frame.

## Drawing other shapes: triangle
- mostly everything on the coding part remains same, except we are now dealing with 3 points instead of four
-to find the points to draw an random traingle, we can use the boundary method; a+b<c<a-b, input an random a,b value, we can find range for c, i.e, the thrid point