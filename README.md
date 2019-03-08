# LR0 parsing visualization

A tool to visualize the LR0 parsing as described in http://amor.cms.hu-berlin.de/~kunert/papers/lr-analyse/.
See also https://en.wikipedia.org/w/index.php?title=LR_parser&oldid=852499017.

Basic features:

* Full implementation of LR0 parsing
* Graphical user interface with the features:
  - Allows to insert, edit and remove rules of the context free grammar
  - Allows to store and load gramar to/from disk (uses json format)
  - Visualizes generation of the state automaton
  - Visualizes generation of the parse table
  - Interactive syntax analysis of sequences using the generated parse table

Use the continue button to get to the next phase. The step button can be used to see more detailled information about what happens in each phase.

![Screenshot 1](/img/screenshot.png "Screenshot 1")
![Screenshot 2](/img/screenshot2.png "Screenshot 2")
![Screenshot 3](/img/screenshot3.png "Screenshot 3")
![Screenshot 4](/img/screenshot4.png "Screenshot 4")
