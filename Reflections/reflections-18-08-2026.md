# Personal Reflections CSC360 (18/08/2026)
Following the previous session, in which we were asked to make an sqaure by ai assistance, this session was more about the basic logic behind it and breaking down the code for it.

_logic behind it_: to make an sqaure we need to have a few basic known, i.e, the lenght of the side of sqaure, the position of sqaure.  
The unit used for lenght in a graphic is pixel. Although the actual physical lenght of graphic depends on the resolution of the display, and size of the dsiplay (greater size=more physical lenght, but decrease in crispness of the graphic)

_Making the sqaure_: We will start by defining a point on the frame, which will be the center of the sqaure. we will suppose an  'L' for lenght of the sides.  
Now we will find the four corners in reference to the center. we can assume the center 'C' to be an point in the fourth quadrant of graph.  
accordingly, the first top-left point (near to origin) will be 'L'/2 lesser than Cx on x axis and 'L'/2 lesser than Cx on y axis. Repeating the same logic to find other 3 point we get;    
top-left=(Cx-'L'/2,Cy-'L'/2).  
top-right=(Cx+'L'/2,Cy-'L'/2).   
bottom-left=(Cx-'L'/2,Cy+'L'/2).  
bottom-right=(Cx+'L'/2,Cy+'L'/2).  
Now we simply have to use drawline command to connect the following point and have our sqaure.

In the later part of session, we discussed about the libraries we imported to the code i.e,swing, awt, etc. Also discussed a few basic concept of jdk, and how are java files and library  stored.
