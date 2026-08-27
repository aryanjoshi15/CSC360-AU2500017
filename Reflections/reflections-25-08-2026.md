# Personal Reflections CSC360 (25/08/2026) 
## Session Overview
This session was about basic java fundamentals, and the structure of an java repository in intellj. Essentially an dissection of repo into maven files, pom.xml and the entire structure, later we talked about threads, thread safety, and threads in java swing.
## Basic fundamentals of java
- Maven is a building tool which simplifies the process by reducing setup time. 
- Pom.xml is a tool file which guides maven to the data which is to be compiled, i.e, the source file. its plays a role in explaining the entire structure to the system.
- One single name change in pom.xml can cause error in the program.
- In maven one can also view the exact stage of the process by acessing the lifecycle folder in maven.
- To finally clear all the lifecycle files, one can use the clean function.

## Threads
- Thread is the smallest single step of execution in an java program.
- The machine can run a single or multiple threads at same time.
- why multiple threads at same time?  
Because in several cases it is diffcult for single thread to handle the entire process. for eg: while making a click button on gui, we use two thread: main thread which does the actual function, and a ui thread which adds some sort of movement, zoom, colour change to the button, letting user know the program is working. 
- _Thread safety_: When two thread works on the same program, they might face difficulty as both of them will try to access same data. Thread safety is essentially an design to counter this.
- java swing has no thread safety as we are mostly working with single thread. 
- We work will single thread because it is easier for the computer. for eg: while creating an merge figure of sqaure and circle, using an single thread would be an better option as it helps to keep the format same. for eg, colour, border size etc.
same can be done with two thread but it reduces the efficiency of program.